package com.epam.deltix.dfp;

import static com.epam.deltix.dfp.JavaImplAdd.*;

class JavaImplCmp {
    private JavaImplCmp() {
    }

    static final long MASK_STEERING_BITS = 0x6000000000000000L;
    static final long MASK_BINARY_EXPONENT1 = 0x7fe0000000000000L;
    static final long MASK_BINARY_SIG1 = 0x001fffffffffffffL;
    static final long MASK_BINARY_EXPONENT2 = 0x1ff8000000000000L;
    static final long MASK_BINARY_SIG2 = 0x0007ffffffffffffL;
    static final long MASK_BINARY_OR2 = 0x0020000000000000L;
    static final long MASK_EXP = 0x7ffe000000000000L;
    static final long MASK_EXP2 = 0x1fff800000000000L;
    static final long MASK_SPECIAL = 0x7800000000000000L;
    static final long MASK_NAN = 0x7c00000000000000L;
    static final long MASK_SNAN = 0x7e00000000000000L;
    static final long MASK_ANY_INF = 0x7c00000000000000L;
    static final long MASK_INF = 0x7800000000000000L;
    static final long MASK_SIGN = 0x8000000000000000L;
    static final long MASK_COEFF = 0x0001ffffffffffffL;
    static final long BIN_EXP_BIAS = (0x1820L << 49);

    public static int compare(final long /*BID_UINT64*/ x, final long /*BID_UINT64*/ y) {
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
            non_canon_x = UnsignedLong.isGreater(sig_x, 9999999999999999L);
        } else {
            exp_x = (int) ((x & MASK_BINARY_EXPONENT1) >>> 53);
            sig_x = (x & MASK_BINARY_SIG1);
            non_canon_x = false;
        }
        // if steering bits are 11 (condition will be 0), then exponent is G[0:w+1] =>
        if ((y & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            exp_y = (int) ((y & MASK_BINARY_EXPONENT2) >>> 51);
            sig_y = (y & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
            non_canon_y = UnsignedLong.isGreater(sig_y, 9999999999999999L);
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
                final long __CX = sig_x;
                final long __CY = bid_mult_factor[exp_x - exp_y];
                long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
                __CXH = __CX >>> 32;
                __CXL = LONG_LOW_PART & __CX;
                __CYH = __CY >>> 32;
                __CYL = LONG_LOW_PART & __CY;

                __PM = __CXH * __CYL;
                __PH = __CXH * __CYH;
                __PL = __CXL * __CYL;
                __PM2 = __CXL * __CYH;
                __PH += (__PM >>> 32);
                __PM = (LONG_LOW_PART & __PM) + __PM2 + (__PL >>> 32);

                sig_n_prime_w1 = __PH + (__PM >>> 32);
                sig_n_prime_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
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
            final long __CX = sig_y;
            final long __CY = bid_mult_factor[exp_y - exp_x];
            long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
            __CXH = __CX >>> 32;
            __CXL = LONG_LOW_PART & __CX;
            __CYH = __CY >>> 32;
            __CYL = LONG_LOW_PART & __CY;

            __PM = __CXH * __CYL;
            __PH = __CXH * __CYH;
            __PL = __CXL * __CYL;
            __PM2 = __CXL * __CYH;
            __PH += (__PM >>> 32);
            __PM = (LONG_LOW_PART & __PM) + __PM2 + (__PL >>> 32);

            sig_n_prime_w1 = __PH + (__PM >>> 32);
            sig_n_prime_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
        }

        // if values are equal
        if (sig_n_prime_w1 == 0 && (sig_n_prime_w0 == sig_x)) {
            return 0;
        }
        // if positive, return whichever significand abs is smaller
        // (converse if negative)
        return (((UnsignedLong.isGreater(sig_n_prime_w1, 0))
            || (UnsignedLong.isLess(sig_x, sig_n_prime_w0))) ^ (!x_mask_sign)) ? 1 : -1;  // @AD: TODO: Check this case carefully
    }

    public static boolean bid64_quiet_equal(final long /*BID_UINT64*/ x, final long /*BID_UINT64*/ y) {
        int exp_x, exp_y, exp_t;
        long /*BID_UINT64*/ sig_x, sig_y, sig_t;
        boolean x_is_zero = false, y_is_zero = false, non_canon_x, non_canon_y;

        // NaN (CASE1)
        // if either number is NAN, the comparison is unordered,
        // rather than equal : return 0
        if (((x & MASK_NAN) == MASK_NAN) || ((y & MASK_NAN) == MASK_NAN)) {
            return false;
        }
        // SIMPLE (CASE2)
        // if all the bits are the same, these numbers are equivalent.
        if (x == y) {
            return true;
        }
        // INFINITY (CASE3)
        if (((x & MASK_INF) == MASK_INF) && ((y & MASK_INF) == MASK_INF)) {
            return (((x ^ y) & MASK_SIGN) != MASK_SIGN);
        }
        // ONE INFINITY (CASE3')
        if (((x & MASK_INF) == MASK_INF) || ((y & MASK_INF) == MASK_INF)) {
            return false;
        }
        // if steering bits are 11 (condition will be 0), then exponent is G[0:w+1] =>
        if ((x & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            exp_x = (int) ((x & MASK_BINARY_EXPONENT2) >>> 51);
            sig_x = (x & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
            non_canon_x = UnsignedLong.isGreater(sig_x, 9999999999999999L);
        } else {
            exp_x = (int) ((x & MASK_BINARY_EXPONENT1) >>> 53);
            sig_x = (x & MASK_BINARY_SIG1);
            non_canon_x = false;
        }
        // if steering bits are 11 (condition will be 0), then exponent is G[0:w+1] =>
        if ((y & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            exp_y = (int) ((y & MASK_BINARY_EXPONENT2) >>> 51);
            sig_y = (y & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
            non_canon_y = UnsignedLong.isGreater(sig_y, 9999999999999999L);
        } else {
            exp_y = (int) ((y & MASK_BINARY_EXPONENT1) >>> 53);
            sig_y = (y & MASK_BINARY_SIG1);
            non_canon_y = false;
        }
        // ZERO (CASE4)
        // some properties:
        // (+ZERO==-ZERO) => therefore ignore the sign
        //    (ZERO x 10^A == ZERO x 10^B) for any valid A, B =>
        //    therefore ignore the exponent field
        //    (Any non-canonical # is considered 0)
        if (non_canon_x || sig_x == 0) {
            x_is_zero = true;
        }
        if (non_canon_y || sig_y == 0) {
            y_is_zero = true;
        }
        if (x_is_zero && y_is_zero) {
            return true;
        } else if ((x_is_zero && !y_is_zero) || (!x_is_zero && y_is_zero)) {
            return false;
        }
        // OPPOSITE SIGN (CASE5)
        // now, if the sign bits differ => not equal : return 0
        if (((x ^ y) & MASK_SIGN) != 0) {
            return false;
        }
        // REDUNDANT REPRESENTATIONS (CASE6)
        if (exp_x > exp_y) {    // to simplify the loop below,
            // SWAP (exp_x, exp_y, exp_t); // put the larger exp in y,
            {
                exp_t = exp_x;
                exp_x = exp_y;
                exp_y = exp_t;
            }
            // SWAP (sig_x, sig_y, sig_t); // and the smaller exp in x
            {
                sig_t = sig_x;
                sig_x = sig_y;
                sig_y = sig_t;
            }
        }
        if (exp_y - exp_x > 15) {
            return false;    // difference cannot be greater than 10^15
        }
        for (int lcv = 0, lce = exp_y - exp_x; lcv < lce; lcv++) {
            // recalculate y's significand upwards
            sig_y = sig_y * 10;
            if (UnsignedLong.isGreater(sig_y, 9999999999999999L)) {
                return false;
            }
        }
        return sig_y == sig_x;
    }

    public static boolean bid64_quiet_greater(final long /*BID_UINT64*/ x, final long /*BID_UINT64*/ y) {
        int exp_x, exp_y;
        long /*BID_UINT64*/ sig_x, sig_y;
        long /*BID_UINT128*/ sig_n_prime_w0, sig_n_prime_w1;
        boolean x_is_zero = false, y_is_zero = false, non_canon_x, non_canon_y;

        final boolean x_mask_sign = (x & MASK_SIGN) == MASK_SIGN;
        final boolean y_mask_sign = (y & MASK_SIGN) == MASK_SIGN;

        // NaN (CASE1)
        // if either number is NAN, the comparison is unordered, rather than equal :
        // return 0
        if (((x & MASK_NAN) == MASK_NAN) || ((y & MASK_NAN) == MASK_NAN)) {
            return false;
        }
        // SIMPLE (CASE2)
        // if all the bits are the same, these numbers are equal (not Greater).
        if (x == y) {
            return false;
        }
        // INFINITY (CASE3)
        if ((x & MASK_INF) == MASK_INF) {
            // if x is neg infinity, there is no way it is greater than y, return 0
            if ((x_mask_sign)) {
                return false;
            } else {
                // x is pos infinity, it is greater, unless y is positive
                // infinity => return y!=pos_infinity
                return (((y & MASK_INF) != MASK_INF)
                    || (y_mask_sign));
            }
        } else if ((y & MASK_INF) == MASK_INF) {
            // x is finite, so if y is positive infinity, then x is less, return 0
            //                 if y is negative infinity, then x is greater, return 1
            return (y_mask_sign);
        }
        // if steering bits are 11 (condition will be 0), then exponent is G[0:w+1] =>
        if ((x & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            exp_x = (int) ((x & MASK_BINARY_EXPONENT2) >>> 51);
            sig_x = (x & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
            non_canon_x = UnsignedLong.isGreater(sig_x, 9999999999999999L);
        } else {
            exp_x = (int) ((x & MASK_BINARY_EXPONENT1) >>> 53);
            sig_x = (x & MASK_BINARY_SIG1);
            non_canon_x = false;
        }
        // if steering bits are 11 (condition will be 0), then exponent is G[0:w+1] =>
        if ((y & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            exp_y = (int) ((y & MASK_BINARY_EXPONENT2) >>> 51);
            sig_y = (y & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
            non_canon_y = UnsignedLong.isGreater(sig_y, 9999999999999999L);
        } else {
            exp_y = (int) ((y & MASK_BINARY_EXPONENT1) >>> 53);
            sig_y = (y & MASK_BINARY_SIG1);
            non_canon_y = false;
        }
        // ZERO (CASE4)
        // some properties:
        //(+ZERO==-ZERO) => therefore ignore the sign, and neither number is greater
        //(ZERO x 10^A == ZERO x 10^B) for any valid A, B => therefore ignore the
        // exponent field
        // (Any non-canonical # is considered 0)
        if (non_canon_x || sig_x == 0) {
            x_is_zero = true;
        }
        if (non_canon_y || sig_y == 0) {
            y_is_zero = true;
        }
        // if both numbers are zero, neither is greater => return NOTGREATERTHAN
        if (x_is_zero && y_is_zero) {
            return false;
        } else if (x_is_zero) {
            // is x is zero, it is greater if Y is negative
            return (y_mask_sign);
        } else if (y_is_zero) {
            // is y is zero, X is greater if it is positive
            return (!x_mask_sign);
        }
        // OPPOSITE SIGN (CASE5)
        // now, if the sign bits differ, x is greater if y is negative
        if (((x ^ y) & MASK_SIGN) == MASK_SIGN) {
            return (y_mask_sign);
        }
        // REDUNDANT REPRESENTATIONS (CASE6)
        // if both components are either bigger or smaller,
        // it is clear what needs to be done
        final long sig_x_unsigned = sig_x + Long.MIN_VALUE;
        final long sig_y_unsigned = sig_y + Long.MIN_VALUE;
        if (sig_x_unsigned > sig_y_unsigned && exp_x > exp_y) {
            return (!x_mask_sign);
        }
        if (sig_x_unsigned < sig_y_unsigned && exp_x < exp_y) {
            return (x_mask_sign);
        }
        // if exp_x is 15 greater than exp_y, no need for compensation
        if (exp_x - exp_y > 15) {    // difference cannot be greater than 10^15
            return !x_mask_sign;// both are negative or positive
        }
        // if exp_x is 15 less than exp_y, no need for compensation
        if (exp_y - exp_x > 15) {
            return x_mask_sign; // both are negative or positive
        }
        // if |exp_x - exp_y| < 15, it comes down to the compensated significand
        if (exp_x > exp_y) {    // to simplify the loop below,
            // otherwise adjust the x significand upwards
            // __mul_64x64_to_128MACH (sig_n_prime, sig_x, bid_mult_factor[exp_x - exp_y]); // @AD: Note: The __mul_64x64_to_128MACH macro is the same as __mul_64x64_to_128
            {
                final long __CX = sig_x;
                final long __CY = bid_mult_factor[exp_x - exp_y];
                long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
                __CXH = __CX >>> 32;
                __CXL = LONG_LOW_PART & __CX;
                __CYH = __CY >>> 32;
                __CYL = LONG_LOW_PART & __CY;

                __PM = __CXH * __CYL;
                __PH = __CXH * __CYH;
                __PL = __CXL * __CYL;
                __PM2 = __CXL * __CYH;
                __PH += (__PM >>> 32);
                __PM = (LONG_LOW_PART & __PM) + __PM2 + (__PL >>> 32);

                sig_n_prime_w1 = __PH + (__PM >>> 32);
                sig_n_prime_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
            }

            // if postitive, return whichever significand is larger (converse if neg.)
            if (sig_n_prime_w1 == 0 && (sig_n_prime_w0 == sig_y)) {
                return false;
            }
            return (((UnsignedLong.isGreater(sig_n_prime_w1, 0))
                || UnsignedLong.isGreater(sig_n_prime_w0, sig_y)) ^ (x_mask_sign));
        }
        // adjust the y significand upwards
        // __mul_64x64_to_128MACH (sig_n_prime, sig_y, bid_mult_factor[exp_y - exp_x]); // @AD: Note: The __mul_64x64_to_128MACH macro is the same as __mul_64x64_to_128
        {
            final long __CX = sig_y;
            final long __CY = bid_mult_factor[exp_y - exp_x];
            long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
            __CXH = __CX >>> 32;
            __CXL = LONG_LOW_PART & __CX;
            __CYH = __CY >>> 32;
            __CYL = LONG_LOW_PART & __CY;

            __PM = __CXH * __CYL;
            __PH = __CXH * __CYH;
            __PL = __CXL * __CYL;
            __PM2 = __CXL * __CYH;
            __PH += (__PM >>> 32);
            __PM = (LONG_LOW_PART & __PM) + __PM2 + (__PL >>> 32);

            sig_n_prime_w1 = __PH + (__PM >>> 32);
            sig_n_prime_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
        }

        // if positive, return whichever significand is larger
        //     (converse if negative)
        if (sig_n_prime_w1 == 0 && (sig_n_prime_w0 == sig_x)) {
            return false;
        }
        return (((sig_n_prime_w1 == 0)
            && (UnsignedLong.isGreater(sig_x, sig_n_prime_w0))) ^ (x_mask_sign));
    }

    public static boolean bid64_quiet_greater_equal(final long /*BID_UINT64*/ x, final long /*BID_UINT64*/ y) {
        int exp_x, exp_y;
        long /*BID_UINT64*/ sig_x, sig_y;
        long /*BID_UINT128*/ sig_n_prime_w0, sig_n_prime_w1;
        boolean x_is_zero = false, y_is_zero = false, non_canon_x, non_canon_y;

        final boolean x_mask_sign = (x & MASK_SIGN) == MASK_SIGN;
        final boolean y_mask_sign = (y & MASK_SIGN) == MASK_SIGN;

        // NaN (CASE1)
        // if either number is NAN, the comparison is unordered : return 1
        if (((x & MASK_NAN) == MASK_NAN) || ((y & MASK_NAN) == MASK_NAN)) {
            return false;
        }
        // SIMPLE (CASE2)
        // if all the bits are the same, these numbers are equal.
        if (x == y) {
            return true;
        }
        // INFINITY (CASE3)
        if ((x & MASK_INF) == MASK_INF) {
            // if x==neg_inf, { res = (y == neg_inf)?1:0; BID_RETURN (res) }
            if (x_mask_sign) {
                // x is -inf, so it is less than y unless y is -inf
                return (((y & MASK_INF) == MASK_INF)
                    && y_mask_sign);
            } else {    // x is pos_inf, no way for it to be less than y
                return true;
            }
        } else if ((y & MASK_INF) == MASK_INF) {
            // x is finite, so:
            //    if y is +inf, x<y
            //    if y is -inf, x>y
            return (y_mask_sign);
        }
        // if steering bits are 11 (condition will be 0), then exponent is G[0:w+1] =>
        if ((x & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            exp_x = (int) ((x & MASK_BINARY_EXPONENT2) >>> 51);
            sig_x = (x & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
            non_canon_x = UnsignedLong.isGreater(sig_x, 9999999999999999L);
        } else {
            exp_x = (int) ((x & MASK_BINARY_EXPONENT1) >>> 53);
            sig_x = (x & MASK_BINARY_SIG1);
            non_canon_x = false;
        }
        // if steering bits are 11 (condition will be 0), then exponent is G[0:w+1] =>
        if ((y & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            exp_y = (int) ((y & MASK_BINARY_EXPONENT2) >>> 51);
            sig_y = (y & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
            non_canon_y = UnsignedLong.isGreater(sig_y, 9999999999999999L);
        } else {
            exp_y = (int) ((y & MASK_BINARY_EXPONENT1) >>> 53);
            sig_y = (y & MASK_BINARY_SIG1);
            non_canon_y = false;
        }
        // ZERO (CASE4)
        // some properties:
        // (+ZERO==-ZERO) => therefore ignore the sign, and neither number is greater
        // (ZERO x 10^A == ZERO x 10^B) for any valid A, B =>
        //   therefore ignore the exponent field
        //  (Any non-canonical # is considered 0)
        if (non_canon_x || sig_x == 0) {
            x_is_zero = true;
        }
        if (non_canon_y || sig_y == 0) {
            y_is_zero = true;
        }
        if (x_is_zero && y_is_zero) {
            // if both numbers are zero, they are equal
            return true;
        } else if (x_is_zero) {
            // if x is zero, it is lessthan if Y is positive
            return (y_mask_sign);
        } else if (y_is_zero) {
            // if y is zero, X is less if it is negative
            return (!x_mask_sign);
        }
        // OPPOSITE SIGN (CASE5)
        // now, if the sign bits differ, x is less than if y is positive
        if (((x ^ y) & MASK_SIGN) == MASK_SIGN) {
            return (y_mask_sign);
        }
        // REDUNDANT REPRESENTATIONS (CASE6)
        // if both components are either bigger or smaller
        final long sig_x_unsigned = sig_x + Long.MIN_VALUE;
        final long sig_y_unsigned = sig_y + Long.MIN_VALUE;
        if (sig_x_unsigned > sig_y_unsigned && exp_x >= exp_y) {
            return (!x_mask_sign);
        }
        if (sig_x_unsigned < sig_y_unsigned && exp_x <= exp_y) {
            return (x_mask_sign);
        }
        // if exp_x is 15 greater than exp_y, no need for compensation
        if (exp_x - exp_y > 15) {
            // difference cannot be greater than 10^15
            return (!x_mask_sign);
        }
        // if exp_x is 15 less than exp_y, no need for compensation
        if (exp_y - exp_x > 15) {
            return (x_mask_sign);
        }
        // if |exp_x - exp_y| < 15, it comes down to the compensated significand
        if (exp_x > exp_y) {    // to simplify the loop below,
            // otherwise adjust the x significand upwards
            // __mul_64x64_to_128MACH (sig_n_prime, sig_x, bid_mult_factor[exp_x - exp_y]); // @AD: Note: The __mul_64x64_to_128MACH macro is the same as __mul_64x64_to_128
            {
                final long __CX = sig_x;
                final long __CY = bid_mult_factor[exp_x - exp_y];
                long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
                __CXH = __CX >>> 32;
                __CXL = LONG_LOW_PART & __CX;
                __CYH = __CY >>> 32;
                __CYL = LONG_LOW_PART & __CY;

                __PM = __CXH * __CYL;
                __PH = __CXH * __CYH;
                __PL = __CXL * __CYL;
                __PM2 = __CXL * __CYH;
                __PH += (__PM >>> 32);
                __PM = (LONG_LOW_PART & __PM) + __PM2 + (__PL >>> 32);

                sig_n_prime_w1 = __PH + (__PM >>> 32);
                sig_n_prime_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
            }

            // return 1 if values are equal
            if (sig_n_prime_w1 == 0 && (sig_n_prime_w0 == sig_y)) {
                return true;
            }
            // if positive, return whichever significand abs is smaller
            // (converse if negative)
            return (((sig_n_prime_w1 == 0)
                && UnsignedLong.isLess(sig_n_prime_w0, sig_y)) ^ (!x_mask_sign));
        }
        // adjust the y significand upwards
        // __mul_64x64_to_128MACH (sig_n_prime, sig_y, bid_mult_factor[exp_y - exp_x]); // @AD: Note: The __mul_64x64_to_128MACH macro is the same as __mul_64x64_to_128
        {
            final long __CX = sig_y;
            final long __CY = bid_mult_factor[exp_y - exp_x];
            long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
            __CXH = __CX >>> 32;
            __CXL = LONG_LOW_PART & __CX;
            __CYH = __CY >>> 32;
            __CYL = LONG_LOW_PART & __CY;

            __PM = __CXH * __CYL;
            __PH = __CXH * __CYH;
            __PL = __CXL * __CYL;
            __PM2 = __CXL * __CYH;
            __PH += (__PM >>> 32);
            __PM = (LONG_LOW_PART & __PM) + __PM2 + (__PL >>> 32);

            sig_n_prime_w1 = __PH + (__PM >>> 32);
            sig_n_prime_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
        }

        // return 0 if values are equal
        if (sig_n_prime_w1 == 0 && (sig_n_prime_w0 == sig_x)) {
            return true;
        }
        // if positive, return whichever significand abs is smaller
        // (converse if negative)
        return (((UnsignedLong.isGreater(sig_n_prime_w1, 0))
            || (UnsignedLong.isLess(sig_x, sig_n_prime_w0))) ^ (!x_mask_sign));
    }

    public static boolean bid64_quiet_less(final long /*BID_UINT64*/ x, final long /*BID_UINT64*/y) {
        int exp_x, exp_y;
        long /*BID_UINT64*/ sig_x, sig_y;
        long /*BID_UINT128*/ sig_n_prime_w0, sig_n_prime_w1;
        boolean x_is_zero = false, y_is_zero = false, non_canon_x, non_canon_y;

        final boolean x_mask_sign = (x & MASK_SIGN) == MASK_SIGN;
        final boolean y_mask_sign = (y & MASK_SIGN) == MASK_SIGN;

        // NaN (CASE1)
        // if either number is NAN, the comparison is unordered : return 0
        if (((x & MASK_NAN) == MASK_NAN) || ((y & MASK_NAN) == MASK_NAN)) {
            return false;
        }
        // SIMPLE (CASE2)
        // if all the bits are the same, these numbers are equal.
        if (x == y) {
            return false;
        }
        // INFINITY (CASE3)
        if ((x & MASK_INF) == MASK_INF) {
            // if x==neg_inf, { res = (y == neg_inf)?0:1; BID_RETURN (res) }
            if (x_mask_sign) {
                // x is -inf, so it is less than y unless y is -inf
                return (((y & MASK_INF) != MASK_INF)
                    || !y_mask_sign);
            } else {
                // x is pos_inf, no way for it to be less than y
                return false;
            }
        } else if ((y & MASK_INF) == MASK_INF) {
            // x is finite, so:
            //    if y is +inf, x<y
            //    if y is -inf, x>y
            return (!y_mask_sign);
        }
        // if steering bits are 11 (condition will be 0), then exponent is G[0:w+1] =>
        if ((x & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            exp_x = (int) ((x & MASK_BINARY_EXPONENT2) >>> 51);
            sig_x = (x & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
            non_canon_x = UnsignedLong.isGreater(sig_x, 9999999999999999L);
        } else {
            exp_x = (int) ((x & MASK_BINARY_EXPONENT1) >>> 53);
            sig_x = (x & MASK_BINARY_SIG1);
            non_canon_x = false;
        }
        // if steering bits are 11 (condition will be 0), then exponent is G[0:w+1] =>
        if ((y & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            exp_y = (int) ((y & MASK_BINARY_EXPONENT2) >>> 51);
            sig_y = (y & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
            non_canon_y = UnsignedLong.isGreater(sig_y, 9999999999999999L);
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
        //    (Any non-canonical # is considered 0)
        if (non_canon_x || sig_x == 0) {
            x_is_zero = true;
        }
        if (non_canon_y || sig_y == 0) {
            y_is_zero = true;
        }
        if (x_is_zero && y_is_zero) {
            // if both numbers are zero, they are equal
            return false;
        } else if (x_is_zero) {
            // if x is zero, it is lessthan if Y is positive
            return (!y_mask_sign);
        } else if (y_is_zero) {
            // if y is zero, X is less if it is negative
            return (x_mask_sign);
        }
        // OPPOSITE SIGN (CASE5)
        // now, if the sign bits differ, x is less than if y is positive
        if (((x ^ y) & MASK_SIGN) == MASK_SIGN) {
            return (!y_mask_sign);
        }
        // REDUNDANT REPRESENTATIONS (CASE6)
        // if both components are either bigger or smaller,
        // it is clear what needs to be done
        final long sig_x_unsigned = sig_x + Long.MIN_VALUE;
        final long sig_y_unsigned = sig_y + Long.MIN_VALUE;
        if (sig_x_unsigned > sig_y_unsigned && exp_x >= exp_y) {
            return (x_mask_sign);
        }
        if (sig_x_unsigned < sig_y_unsigned && exp_x <= exp_y) {
            return (!x_mask_sign);
        }
        // if exp_x is 15 greater than exp_y, no need for compensation
        if (exp_x - exp_y > 15) {
            // difference cannot be greater than 10^15
            return (x_mask_sign);
        }
        // if exp_x is 15 less than exp_y, no need for compensation
        if (exp_y - exp_x > 15) {
            return (!x_mask_sign);
        }
        // if |exp_x - exp_y| < 15, it comes down to the compensated significand
        if (exp_x > exp_y) {    // to simplify the loop below,
            // otherwise adjust the x significand upwards
            // __mul_64x64_to_128MACH (sig_n_prime, sig_x, bid_mult_factor[exp_x - exp_y]); // @AD: Note: The __mul_64x64_to_128MACH macro is the same as __mul_64x64_to_128
            {
                final long __CX = sig_x;
                final long __CY = bid_mult_factor[exp_x - exp_y];
                long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
                __CXH = __CX >>> 32;
                __CXL = LONG_LOW_PART & __CX;
                __CYH = __CY >>> 32;
                __CYL = LONG_LOW_PART & __CY;

                __PM = __CXH * __CYL;
                __PH = __CXH * __CYH;
                __PL = __CXL * __CYL;
                __PM2 = __CXL * __CYH;
                __PH += (__PM >>> 32);
                __PM = (LONG_LOW_PART & __PM) + __PM2 + (__PL >>> 32);

                sig_n_prime_w1 = __PH + (__PM >>> 32);
                sig_n_prime_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
            }

            // return 0 if values are equal
            if (sig_n_prime_w1 == 0 && (sig_n_prime_w0 == sig_y)) {
                return false;
            }
            // if positive, return whichever significand abs is smaller
            // (converse if negative)
            return (((sig_n_prime_w1 == 0)
                && UnsignedLong.isLess(sig_n_prime_w0, sig_y)) ^ (x_mask_sign));
        }
        // adjust the y significand upwards
        // __mul_64x64_to_128MACH (sig_n_prime, sig_y, bid_mult_factor[exp_y - exp_x]); // @AD: Note: The __mul_64x64_to_128MACH macro is the same as __mul_64x64_to_128
        {
            final long __CX = sig_y;
            final long __CY = bid_mult_factor[exp_y - exp_x];
            long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
            __CXH = __CX >>> 32;
            __CXL = LONG_LOW_PART & __CX;
            __CYH = __CY >>> 32;
            __CYL = LONG_LOW_PART & __CY;

            __PM = __CXH * __CYL;
            __PH = __CXH * __CYH;
            __PL = __CXL * __CYL;
            __PM2 = __CXL * __CYH;
            __PH += (__PM >>> 32);
            __PM = (LONG_LOW_PART & __PM) + __PM2 + (__PL >>> 32);

            sig_n_prime_w1 = __PH + (__PM >>> 32);
            sig_n_prime_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
        }

        // return 0 if values are equal
        if (sig_n_prime_w1 == 0 && (sig_n_prime_w0 == sig_x)) {
            return false;
        }
        // if positive, return whichever significand abs is smaller
        // (converse if negative)
        return (((UnsignedLong.isGreater(sig_n_prime_w1, 0))
            || (UnsignedLong.isLess(sig_x, sig_n_prime_w0))) ^ (x_mask_sign));
    }

    public static boolean bid64_quiet_less_equal(final long /*BID_UINT64*/ x, final long /*BID_UINT64*/ y) {
        int exp_x, exp_y;
        long /*BID_UINT64*/ sig_x, sig_y;
        long /*BID_UINT128*/ sig_n_prime_w0, sig_n_prime_w1;
        boolean x_is_zero = false, y_is_zero = false, non_canon_x, non_canon_y;

        final boolean x_mask_sign = (x & MASK_SIGN) == MASK_SIGN;
        final boolean y_mask_sign = (y & MASK_SIGN) == MASK_SIGN;

        // NaN (CASE1)
        // if either number is NAN, the comparison is unordered, rather than equal :
        //     return 0
        if (((x & MASK_NAN) == MASK_NAN) || ((y & MASK_NAN) == MASK_NAN)) {
            return false;
        }
        // SIMPLE (CASE2)
        // if all the bits are the same, these numbers are equal (LESSEQUAL).
        if (x == y) {
            return true;
        }
        // INFINITY (CASE3)
        if ((x & MASK_INF) == MASK_INF) {
            if ((x_mask_sign)) {
                // if x is neg infinity, it must be lessthan or equal to y return 1
                return true;
            } else {
                // x is pos infinity, it is greater, unless y is positive infinity =>
                // return y==pos_infinity
                return !(((y & MASK_INF) != MASK_INF)
                    || (y_mask_sign));
            }
        } else if ((y & MASK_INF) == MASK_INF) {
            // x is finite, so if y is positive infinity, then x is less, return 1
            //                 if y is negative infinity, then x is greater, return 0
            return (!y_mask_sign);
        }
        // if steering bits are 11 (condition will be 0), then exponent is G[0:w+1] =>
        if ((x & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            exp_x = (int) ((x & MASK_BINARY_EXPONENT2) >>> 51);
            sig_x = (x & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
            non_canon_x = UnsignedLong.isGreater(sig_x, 9999999999999999L);
        } else {
            exp_x = (int) ((x & MASK_BINARY_EXPONENT1) >>> 53);
            sig_x = (x & MASK_BINARY_SIG1);
            non_canon_x = false;
        }
        // if steering bits are 11 (condition will be 0), then exponent is G[0:w+1] =>
        if ((y & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            exp_y = (int) ((y & MASK_BINARY_EXPONENT2) >>> 51);
            sig_y = (y & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
            non_canon_y = UnsignedLong.isGreater(sig_y, 9999999999999999L);
        } else {
            exp_y = (int) ((y & MASK_BINARY_EXPONENT1) >>> 53);
            sig_y = (y & MASK_BINARY_SIG1);
            non_canon_y = false;
        }
        // ZERO (CASE4)
        // some properties:
        // (+ZERO==-ZERO) => therefore ignore the sign, and neither number is greater
        // (ZERO x 10^A == ZERO x 10^B) for any valid A, B =>
        //     therefore ignore the exponent field
        //    (Any non-canonical # is considered 0)
        if (non_canon_x || sig_x == 0) {
            x_is_zero = true;
        }
        if (non_canon_y || sig_y == 0) {
            y_is_zero = true;
        }
        if (x_is_zero && y_is_zero) {
            // if both numbers are zero, they are equal -> return 1
            return true;
        } else if (x_is_zero) {
            // if x is zero, it is less than if Y is positive
            return (!y_mask_sign);
        } else if (y_is_zero) {
            // if y is zero, X is less if it is negative
            return (x_mask_sign);
        }
        // OPPOSITE SIGN (CASE5)
        // now, if the sign bits differ, x is less than if y is positive
        if (((x ^ y) & MASK_SIGN) == MASK_SIGN) {
            return (!y_mask_sign);
        }
        // REDUNDANT REPRESENTATIONS (CASE6)
        // if both components are either bigger or smaller
        final long sig_x_unsigned = sig_x + Long.MIN_VALUE;
        final long sig_y_unsigned = sig_y + Long.MIN_VALUE;
        if (sig_x_unsigned > sig_y_unsigned && exp_x >= exp_y) {
            return (x_mask_sign);
        }
        if (sig_x_unsigned < sig_y_unsigned && exp_x <= exp_y) {
            return (!x_mask_sign);
        }
        // if exp_x is 15 greater than exp_y, no need for compensation
        if (exp_x - exp_y > 15) {
            // difference cannot be greater than 10^15
            return (x_mask_sign);
        }
        // if exp_x is 15 less than exp_y, no need for compensation
        if (exp_y - exp_x > 15) {
            return (!x_mask_sign);
        }
        // if |exp_x - exp_y| < 15, it comes down to the compensated significand
        if (exp_x > exp_y) {    // to simplify the loop below,
            // otherwise adjust the x significand upwards
            // __mul_64x64_to_128MACH (sig_n_prime, sig_x, bid_mult_factor[exp_x - exp_y]); // @AD: Note: The __mul_64x64_to_128MACH macro is the same as __mul_64x64_to_128
            {
                final long __CX = sig_x;
                final long __CY = bid_mult_factor[exp_x - exp_y];
                long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
                __CXH = __CX >>> 32;
                __CXL = LONG_LOW_PART & __CX;
                __CYH = __CY >>> 32;
                __CYL = LONG_LOW_PART & __CY;

                __PM = __CXH * __CYL;
                __PH = __CXH * __CYH;
                __PL = __CXL * __CYL;
                __PM2 = __CXL * __CYH;
                __PH += (__PM >>> 32);
                __PM = (LONG_LOW_PART & __PM) + __PM2 + (__PL >>> 32);

                sig_n_prime_w1 = __PH + (__PM >>> 32);
                sig_n_prime_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
            }

            // return 1 if values are equal
            if (sig_n_prime_w1 == 0 && (sig_n_prime_w0 == sig_y)) {
                return true;
            }
            // if positive, return whichever significand abs is smaller
            //     (converse if negative)
            return (((sig_n_prime_w1 == 0)
                && UnsignedLong.isLess(sig_n_prime_w0, sig_y)) ^ (x_mask_sign));
        }
        // adjust the y significand upwards
        // __mul_64x64_to_128MACH (sig_n_prime, sig_y, bid_mult_factor[exp_y - exp_x]); // @AD: Note: The __mul_64x64_to_128MACH macro is the same as __mul_64x64_to_128
        {
            final long __CX = sig_y;
            final long __CY = bid_mult_factor[exp_y - exp_x];
            long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
            __CXH = __CX >>> 32;
            __CXL = LONG_LOW_PART & __CX;
            __CYH = __CY >>> 32;
            __CYL = LONG_LOW_PART & __CY;

            __PM = __CXH * __CYL;
            __PH = __CXH * __CYH;
            __PL = __CXL * __CYL;
            __PM2 = __CXL * __CYH;
            __PH += (__PM >>> 32);
            __PM = (LONG_LOW_PART & __PM) + __PM2 + (__PL >>> 32);

            sig_n_prime_w1 = __PH + (__PM >>> 32);
            sig_n_prime_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
        }

        // return 1 if values are equal
        if (sig_n_prime_w1 == 0 && (sig_n_prime_w0 == sig_x)) {
            return true;
        }
        // if positive, return whichever significand abs is smaller
        //     (converse if negative)
        return (((UnsignedLong.isGreater(sig_n_prime_w1, 0))
            || (UnsignedLong.isLess(sig_x, sig_n_prime_w0))) ^ (x_mask_sign));
    }

    public static boolean bid64_quiet_not_equal(final long /*BID_UINT64*/ x, final long /*BID_UINT64*/ y) {
        int exp_x, exp_y, exp_t;
        long /*BID_UINT64*/ sig_x, sig_y, sig_t;
        boolean x_is_zero = false, y_is_zero = false, non_canon_x, non_canon_y;

        // NaN (CASE1)
        // if either number is NAN, the comparison is unordered,
        // rather than equal : return 1
        if (((x & MASK_NAN) == MASK_NAN) || ((y & MASK_NAN) == MASK_NAN)) {
            return true;
        }
        // SIMPLE (CASE2)
        // if all the bits are the same, these numbers are equivalent.
        if (x == y) {
            return false;
        }
        // INFINITY (CASE3)
        if (((x & MASK_INF) == MASK_INF) && ((y & MASK_INF) == MASK_INF)) {
            return (((x ^ y) & MASK_SIGN) == MASK_SIGN);
        }
        // ONE INFINITY (CASE3')
        if (((x & MASK_INF) == MASK_INF) || ((y & MASK_INF) == MASK_INF)) {
            return true;
        }
        // if steering bits are 11 (condition will be 0), then exponent is G[0:w+1] =>
        if ((x & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            exp_x = (int) ((x & MASK_BINARY_EXPONENT2) >>> 51);
            sig_x = (x & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
            non_canon_x = UnsignedLong.isGreater(sig_x, 9999999999999999L);
        } else {
            exp_x = (int) ((x & MASK_BINARY_EXPONENT1) >>> 53);
            sig_x = (x & MASK_BINARY_SIG1);
            non_canon_x = false;
        }

        // if steering bits are 11 (condition will be 0), then exponent is G[0:w+1] =>
        if ((y & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            exp_y = (int) ((y & MASK_BINARY_EXPONENT2) >>> 51);
            sig_y = (y & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
            non_canon_y = UnsignedLong.isGreater(sig_y, 9999999999999999L);
        } else {
            exp_y = (int) ((y & MASK_BINARY_EXPONENT1) >>> 53);
            sig_y = (y & MASK_BINARY_SIG1);
            non_canon_y = false;
        }

        // ZERO (CASE4)
        // some properties:
        // (+ZERO==-ZERO) => therefore ignore the sign
        //    (ZERO x 10^A == ZERO x 10^B) for any valid A, B =>
        //        therefore ignore the exponent field
        //    (Any non-canonical # is considered 0)
        if (non_canon_x || sig_x == 0) {
            x_is_zero = true;
        }
        if (non_canon_y || sig_y == 0) {
            y_is_zero = true;
        }

        if (x_is_zero && y_is_zero) {
            return false;
        } else if ((x_is_zero && !y_is_zero) || (!x_is_zero && y_is_zero)) {
            return true;
        }
        // OPPOSITE SIGN (CASE5)
        // now, if the sign bits differ => not equal : return 1
        if (((x ^ y) & MASK_SIGN) != 0) {
            return true;
        }
        // REDUNDANT REPRESENTATIONS (CASE6)
        if (exp_x > exp_y) {    // to simplify the loop below,
            // SWAP (exp_x, exp_y, exp_t); // put the larger exp in y,
            {
                exp_t = exp_x;
                exp_x = exp_y;
                exp_y = exp_t;
            }
            // SWAP (sig_x, sig_y, sig_t); // and the smaller exp in x
            {
                sig_t = sig_x;
                sig_x = sig_y;
                sig_y = sig_t;
            }
        }

        if (exp_y - exp_x > 15) {
            return true;
        }
        // difference cannot be greater than 10^16

        for (int lcv = 0, lce = exp_y - exp_x; lcv < lce; lcv++) {

            // recalculate y's significand upwards
            sig_y = sig_y * 10;
            if (UnsignedLong.isGreater(sig_y, 9999999999999999L)) {
                return true;
            }
        }

        return sig_y != sig_x;
    }

    public static boolean bid64_isZero(final long /*BID_UINT64*/ x) {
        // if infinity or nan, return 0
        if ((x & MASK_INF) == MASK_INF) {
            return false;
        } else if ((x & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            // if steering bits are 11 (condition will be 0), then exponent is G[0:w+1]
            // => sig_x = (x & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
            // if(sig_x > 9999999999999999ull) {return 1;}
            return UnsignedLong.isGreater(((x & MASK_BINARY_SIG2) | MASK_BINARY_OR2), 9999999999999999L);
        } else {
            return (x & MASK_BINARY_SIG1) == 0;
        }
    }

    public static boolean bid64_isNormal(final long /*BID_UINT64*/ x) {
        long /*BID_UINT128*/ sig_x_prime_w0, sig_x_prime_w1;
        long /*BID_UINT64*/ sig_x;
        int exp_x;

        if ((x & MASK_INF) == MASK_INF) {    // x is either INF or NaN
            return false;
        } else {
            // decode number into exponent and significand
            if ((x & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
                sig_x = (x & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
                // check for zero or non-canonical
                if (UnsignedLong.isGreater(sig_x, 9999999999999999L) || sig_x == 0) {
                    return false;    // zero or non-canonical
                }
                exp_x = (int) ((x & MASK_BINARY_EXPONENT2) >>> 51);
            } else {
                sig_x = (x & MASK_BINARY_SIG1);
                if (sig_x == 0) {
                    return false;    // zero
                }
                exp_x = (int) ((x & MASK_BINARY_EXPONENT1) >>> 53);
            }
            // if exponent is less than -383, the number may be subnormal
            // if (exp_x - 398 = -383) the number may be subnormal
            if (exp_x < 15) {
                // __mul_64x64_to_128MACH (sig_x_prime, sig_x, bid_mult_factor[exp_x]); // @AD: Note: The __mul_64x64_to_128MACH macro is the same as __mul_64x64_to_128
                {
                    final long __CX = sig_x;
                    final long __CY = bid_mult_factor[exp_x];
                    long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
                    __CXH = __CX >>> 32;
                    __CXL = LONG_LOW_PART & __CX;
                    __CYH = __CY >>> 32;
                    __CYL = LONG_LOW_PART & __CY;

                    __PM = __CXH * __CYL;
                    __PH = __CXH * __CYH;
                    __PL = __CXL * __CYL;
                    __PM2 = __CXL * __CYH;
                    __PH += (__PM >>> 32);
                    __PM = (LONG_LOW_PART & __PM) + __PM2 + (__PL >>> 32);

                    sig_x_prime_w1 = __PH + (__PM >>> 32);
                    sig_x_prime_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
                }

                // normal
                return sig_x_prime_w1 != 0 || UnsignedLong.isGreater(sig_x_prime_w0, 999999999999999L);
            } else {
                return true;    // normal
            }
        }
    }

    public static boolean isPositive(final long value) {
        if ((value & MASK_NAN) == MASK_NAN)
            return false;
        if (bid64_isZero(value))
            return false;
        return (value & MASK_SIGN) == 0;
    }

    public static boolean isNegative(final long value) {
        if ((value & MASK_NAN) == MASK_NAN)
            return false;
        if (bid64_isZero(value))
            return false;
        return (value & MASK_SIGN) != 0;
    }

    public static boolean isNonPositive(final long value) {
        if ((value & MASK_NAN) == MASK_NAN)
            return false;
        if (bid64_isZero(value))
            return true;
        return (value & MASK_SIGN) != 0;
    }

    public static boolean isNonNegative(final long value) {
        if ((value & MASK_NAN) == MASK_NAN)
            return false;
        if (bid64_isZero(value))
            return true;
        return (value & MASK_SIGN) == 0;
    }

    static final long[] /*BID_UINT64*/ bid_mult_factor = {
        1L,
        10L,
        100L,
        1000L,
        10000L,
        100000L,
        1000000L,
        10000000L,
        100000000L,
        1000000000L,
        10000000000L,
        100000000000L,
        1000000000000L,
        10000000000000L,
        100000000000000L,
        1000000000000000L
    };
}
