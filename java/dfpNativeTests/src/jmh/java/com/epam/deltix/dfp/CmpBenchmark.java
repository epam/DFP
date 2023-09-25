package com.epam.deltix.dfp;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

import static com.epam.deltix.dfp.JavaImplCmp.*;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(time = 3, iterations = 3)
@Measurement(time = 3, iterations = 3)
@Fork(3)
@State(Scope.Thread)
public class CmpBenchmark {
    /* 0., 1., 1. (non-canonized), 1000., Inf, NaN */
    @Param({"3584865303386914816", "3584865303386914817", "3450757314565799936", "3584865303386915816", "8646911284551352320", "8935141660703064064"})
    private long x;

    /* 0., 1., 1. (non-canonized), 1000., Inf, NaN */
    @Param({"3584865303386914816", "3584865303386914817", "3450757314565799936", "3584865303386915816", "8646911284551352320", "8935141660703064064"})
    private long y;

    @Benchmark
    public int compareToOrig() {
        return compareOrig(x, y);
    }

    @Benchmark
    public int compareToNew() {
        return JavaImplCmp.compare(x, y);
    }

    public static int compareOrig(final long /*BID_UINT64*/ x, final long /*BID_UINT64*/ y) {
        int exp_x, exp_y;
        long /*BID_UINT64*/ sig_x, sig_y;
        long /*BID_UINT128*/ sig_n_prime_w0, sig_n_prime_w1;
        boolean x_is_zero = false, y_is_zero = false, non_canon_x, non_canon_y;

        final boolean x_mask_sign = (x & MASK_SIGN) == MASK_SIGN;
        final boolean y_mask_sign = (y & MASK_SIGN) == MASK_SIGN;

        // NaN (CASE1)
        // if either number is NAN, the comparison is unordered,
        // rather than equal : return 0
        final boolean xIsNaN = ((x & MASK_NAN) == MASK_NAN);
        final boolean yIsNaN = ((y & MASK_NAN) == MASK_NAN);
        if (xIsNaN || yIsNaN) {
            return (xIsNaN ? 1 : 0) - (yIsNaN ? 1 : 0);
        }
        // SIMPLE (CASE2)
        // if all the bits are the same, these numbers are equivalent.
        if (x == y) {
            return 0;
        }
        // INFINITY (CASE3)
        if ((x & MASK_INF) == MASK_INF) {
            if (x_mask_sign) {
                // x is -inf, so it is less than y unless y is -inf
                return (((y & MASK_INF) != MASK_INF)
                    || !y_mask_sign) ? -1 : 0;
            } else {
                // x is pos infinity, it is greater, unless y is positive
                // infinity => return y!=pos_infinity
                return (((y & MASK_INF) != MASK_INF)
                    || (y_mask_sign)) ? 1 : 0;
            }
        } else if ((y & MASK_INF) == MASK_INF) {
            // x is finite, so if y is positive infinity, then x is less
            //                 if y is negative infinity, then x is greater
            return (y_mask_sign) ? 1 : -1;
        }
        // if steering bits are 11 (condition will be 0), then exponent is G[0:w+1] =>
        if ((x & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            exp_x = (int) ((x & MASK_BINARY_EXPONENT2) >>> 51);
            sig_x = (x & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
            non_canon_x = sig_x > 9999999999999999L;
        } else {
            exp_x = (int) ((x & MASK_BINARY_EXPONENT1) >>> 53);
            sig_x = (x & MASK_BINARY_SIG1);
            non_canon_x = false;
        }
        // if steering bits are 11 (condition will be 0), then exponent is G[0:w+1] =>
        if ((y & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            exp_y = (int) ((y & MASK_BINARY_EXPONENT2) >>> 51);
            sig_y = (y & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
            non_canon_y = sig_y > 9999999999999999L;
        } else {
            exp_y = (int) ((y & MASK_BINARY_EXPONENT1) >>> 53);
            sig_y = (y & MASK_BINARY_SIG1);
            non_canon_y = false;
        }
        // ZERO (CASE4)
        // some properties:
        // (+ZERO==-ZERO) => therefore ignore the sign, and neither number is greater
        // (ZERO x 10^A == ZERO x 10^B) for any valid A, B =>
        //  therefore ignore the exponent field
        // (Any non-canonical # is considered 0)
        if (non_canon_x || sig_x == 0) {
            x_is_zero = true;
        }
        if (non_canon_y || sig_y == 0) {
            y_is_zero = true;
        }
        if (x_is_zero && y_is_zero) {
            // if both numbers are zero, they are equal
            return 0;
        } else if (x_is_zero) {
            // if x is zero, it is greater if Y is negative
            return (y_mask_sign) ? 1 : -1;
        } else if (y_is_zero) {
            // if y is zero, X is greater if it is positive
            return (!x_mask_sign) ? 1 : -1;
        }
        // OPPOSITE SIGN (CASE5)
        // now, if the sign bits differ, x is greater if y is negative
        if (((x ^ y) & MASK_SIGN) == MASK_SIGN) {
            return (y_mask_sign) ? 1 : -1;
        }
        // REDUNDANT REPRESENTATIONS (CASE6)
        // if both components are either bigger or smaller,
        // it is clear what needs to be done
        final long sig_x_unsigned = sig_x + Long.MIN_VALUE;
        final long sig_y_unsigned = sig_y + Long.MIN_VALUE;
        if (sig_x_unsigned > sig_y_unsigned && exp_x >= exp_y) {
            return (!x_mask_sign) ? 1 : -1;
        }
        if (sig_x_unsigned < sig_y_unsigned && exp_x <= exp_y) {
            return (x_mask_sign) ? 1 : -1;
        }
        // if exp_x is 15 greater than exp_y, no need for compensation
        if (exp_x - exp_y > 15) {
            // difference cannot be greater than 10^15
            return !x_mask_sign ? 1 : -1;// both are negative or positive
        }
        // if exp_x is 15 less than exp_y, no need for compensation
        if (exp_y - exp_x > 15) {
            return x_mask_sign ? 1 : -1; // both are negative or positive
        }
        // if |exp_x - exp_y| < 15, it comes down to the compensated significand
        if (exp_x > exp_y) {    // to simplify the loop below,
            // otherwise adjust the x significand upwards
            // __mul_64x64_to_128MACH (sig_n_prime, sig_x, bid_mult_factor[exp_x - exp_y]); // @AD: Note: The __mul_64x64_to_128MACH macro is the same as __mul_64x64_to_128
            {
                final long __CY = bid_mult_factor[exp_x - exp_y];
                sig_n_prime_w1 = Mul64Impl.unsignedMultiplyHigh(sig_x, __CY);
                sig_n_prime_w0 = sig_x * __CY;
            }

            // if values are equal
            if (sig_n_prime_w1 == 0 && (sig_n_prime_w0 == sig_y)) {
                return 0;
            }
            // if positive, return whichever significand abs is smaller
            // (converse if negative)
            return (((sig_n_prime_w1 == 0)
                && UnsignedLong.isLess(sig_n_prime_w0, sig_y)) ^ (!x_mask_sign)) ? 1 : -1; // @AD: TODO: Check this case carefully
        }
        // adjust the y significand upwards
        // __mul_64x64_to_128MACH (sig_n_prime, sig_y, bid_mult_factor[exp_y - exp_x]); // @AD: Note: The __mul_64x64_to_128MACH macro is the same as __mul_64x64_to_128
        {
            final long __CY = bid_mult_factor[exp_y - exp_x];
            sig_n_prime_w1 = Mul64Impl.unsignedMultiplyHigh(sig_y, __CY);
            sig_n_prime_w0 = sig_y * __CY;
        }

        // if values are equal
        if (sig_n_prime_w1 == 0 && (sig_n_prime_w0 == sig_x)) {
            return 0;
        }
        // if positive, return whichever significand abs is smaller
        // (converse if negative)
        return (((/*UnsignedLong.isGreater*/(sig_n_prime_w1 != 0))
            || (UnsignedLong.isLess(sig_x, sig_n_prime_w0))) ^ (!x_mask_sign)) ? 1 : -1;  // @AD: TODO: Check this case carefully
    }
}
