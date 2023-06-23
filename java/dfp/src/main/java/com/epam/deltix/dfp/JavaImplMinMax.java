package com.epam.deltix.dfp;

import static com.epam.deltix.dfp.JavaImplAdd.LONG_LOW_PART;
import static com.epam.deltix.dfp.JavaImplCmp.*;

class JavaImplMinMax {
    private JavaImplMinMax() {
    }

    /**
     * BID64 minimum function - returns greater of two numbers
     *
     * @param x First operand
     * @param y Second operand
     * @return The minimal value
     */
    public static long bid64_min_fix_nan(long /*BID_UINT64*/ x, long /*BID_UINT64*/ y) {
        int exp_x, exp_y;
        long /*BID_UINT64*/ sig_x, sig_y;
        long /*BID_UINT128*/ sig_n_prime_w0, sig_n_prime_w1;
        boolean x_is_zero = false, y_is_zero = false;

        // NaN (CASE1)
        if ((x & MASK_NAN) == MASK_NAN || (y & MASK_NAN) == MASK_NAN)
            return MASK_NAN;

        // check for non-canonical x
        if ((x & MASK_INF) == MASK_INF) {    // check for Infinity
            x = x & (MASK_SIGN | MASK_INF);
        } else {    // x is not special
            // check for non-canonical values - treated as zero
            if ((x & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
                // if the steering bits are 11, then the exponent is G[0:w+1]
                if (((x & MASK_BINARY_SIG2) | MASK_BINARY_OR2) > 9999999999999999L) {
                    // non-canonical
                    x = (x & MASK_SIGN) | ((x & MASK_BINARY_EXPONENT2) << 2);
                }    // else canonical
            }    // else canonical
        }

        // check for non-canonical y
        if ((y & MASK_INF) == MASK_INF) {    // check for Infinity
            y = y & (MASK_SIGN | MASK_INF);
        } else {    // y is not special
            // check for non-canonical values - treated as zero
            if ((y & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
                // if the steering bits are 11, then the exponent is G[0:w+1]
                if (((y & MASK_BINARY_SIG2) | MASK_BINARY_OR2) > 9999999999999999L) {
                    // non-canonical
                    y = (y & MASK_SIGN) | ((y & MASK_BINARY_EXPONENT2) << 2);
                }    // else canonical
            }    // else canonical
        }

        // SIMPLE (CASE2)
        // if all the bits are the same, these numbers are equal, return either number
        if (x == y) {
            return x;
        }
        // INFINITY (CASE3)
        if ((x & MASK_INF) == MASK_INF) {
            // if x is neg infinity, there is no way it is greater than y, return x
            if (((x & MASK_SIGN) == MASK_SIGN)) {
                return x;
            }
            // x is pos infinity, return y
            else {
                return y;
            }
        } else if ((y & MASK_INF) == MASK_INF) {
            // x is finite, so if y is positive infinity, then x is less, return y
            //                 if y is negative infinity, then x is greater, return x
            return ((y & MASK_SIGN) == MASK_SIGN) ? y : x;
        }
        // if steering bits are 11 (condition will be 0), then exponent is G[0:w+1] =>
        if ((x & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            exp_x = (int) ((x & MASK_BINARY_EXPONENT2) >>> 51);
            sig_x = (x & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
        } else {
            exp_x = (int) ((x & MASK_BINARY_EXPONENT1) >>> 53);
            sig_x = (x & MASK_BINARY_SIG1);
        }

        // if steering bits are 11 (condition will be 0), then exponent is G[0:w+1] =>
        if ((y & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            exp_y = (int) ((y & MASK_BINARY_EXPONENT2) >>> 51);
            sig_y = (y & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
        } else {
            exp_y = (int) ((y & MASK_BINARY_EXPONENT1) >>> 53);
            sig_y = (y & MASK_BINARY_SIG1);
        }

        // ZERO (CASE4)
        // some properties:
        //    (+ZERO == -ZERO) => therefore
        //        ignore the sign, and neither number is greater
        //    (ZERO x 10^A == ZERO x 10^B) for any valid A, B =>
        //        ignore the exponent field
        //    (Any non-canonical # is considered 0)
        if (sig_x == 0) {
            x_is_zero = true;
        }
        if (sig_y == 0) {
            y_is_zero = true;
        }

        if (x_is_zero && y_is_zero) {
            // if both numbers are zero, neither is greater => return either
            return y;
        } else if (x_is_zero) {
            // is x is zero, it is greater if Y is negative
            return ((y & MASK_SIGN) == MASK_SIGN) ? y : x;
        } else if (y_is_zero) {
            // is y is zero, X is greater if it is positive
            return ((x & MASK_SIGN) != MASK_SIGN) ? y : x;
        }
        // OPPOSITE SIGN (CASE5)
        // now, if the sign bits differ, x is greater if y is negative
        if (((x ^ y) & MASK_SIGN) == MASK_SIGN) {
            return ((y & MASK_SIGN) == MASK_SIGN) ? y : x;
        }
        // REDUNDANT REPRESENTATIONS (CASE6)

        // if both components are either bigger or smaller,
        // it is clear what needs to be done
        if (UnsignedLong.isGreater(sig_x, sig_y) && exp_x >= exp_y) {
            return ((x & MASK_SIGN) != MASK_SIGN) ? y : x;
        }
        if (UnsignedLong.isLess(sig_x, sig_y) && exp_x <= exp_y) {
            return ((x & MASK_SIGN) == MASK_SIGN) ? y : x;
        }
        // if exp_x is 15 greater than exp_y, no need for compensation
        if (exp_x - exp_y > 15) {
            return ((x & MASK_SIGN) != MASK_SIGN) ? y : x;    // difference cannot be >10^15
        }
        // if exp_x is 15 less than exp_y, no need for compensation
        if (exp_y - exp_x > 15) {
            return ((x & MASK_SIGN) == MASK_SIGN) ? y : x;
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


            // if postitive, return whichever significand is larger
            // (converse if negative)
            if (sig_n_prime_w1 == 0 && (sig_n_prime_w0 == sig_y)) {
                return y;
            }

            return ((/*UnsignedLong.isGreater*/(sig_n_prime_w1 != 0)
                || UnsignedLong.isGreater(sig_n_prime_w0, sig_y)) ^ ((x & MASK_SIGN) == MASK_SIGN)) ? y : x;
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

        // if postitive, return whichever significand is larger (converse if negative)
        if (sig_n_prime_w1 == 0 && (sig_n_prime_w0 == sig_x)) {
            return y;
        }
        return (((sig_n_prime_w1 == 0)
            && (UnsignedLong.isGreater(sig_x, sig_n_prime_w0))) ^ ((x & MASK_SIGN) == MASK_SIGN)) ? y : x;
    }

    /**
     * BID64 maximum function - returns greater of two numbers
     *
     * @param x First operand
     * @param y Second operand
     * @return The maximal value
     */
    public static long bid64_max_fix_nan(long /*BID_UINT64*/ x, long /*BID_UINT64*/ y) {
        int exp_x, exp_y;
        long /*BID_UINT64*/ sig_x, sig_y;
        long /*BID_UINT128*/ sig_n_prime_w0, sig_n_prime_w1;
        boolean x_is_zero = false, y_is_zero = false;

        // NaN (CASE1)
        if ((x & MASK_NAN) == MASK_NAN || (y & MASK_NAN) == MASK_NAN)
            return MASK_NAN;

        // check for non-canonical x
        if ((x & MASK_INF) == MASK_INF) {    // check for Infinity
            x = x & (MASK_SIGN | MASK_INF);
        } else {    // x is not special
            // check for non-canonical values - treated as zero
            if ((x & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
                // if the steering bits are 11, then the exponent is G[0:w+1]
                if (((x & MASK_BINARY_SIG2) | MASK_BINARY_OR2) > 9999999999999999L) {
                    // non-canonical
                    x = (x & MASK_SIGN) | ((x & MASK_BINARY_EXPONENT2) << 2);
                }    // else canonical
            }    // else canonical
        }

        // check for non-canonical y
        if ((y & MASK_INF) == MASK_INF) {    // check for Infinity
            y = y & (MASK_SIGN | MASK_INF);
        } else {    // y is not special
            // check for non-canonical values - treated as zero
            if ((y & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
                // if the steering bits are 11, then the exponent is G[0:w+1]
                if (((y & MASK_BINARY_SIG2) | MASK_BINARY_OR2) > 9999999999999999L) {
                    // non-canonical
                    y = (y & MASK_SIGN) | ((y & MASK_BINARY_EXPONENT2) << 2);
                }    // else canonical
            }    // else canonical
        }

        // SIMPLE (CASE2)
        // if all the bits are the same, these numbers are equal (not Greater).
        if (x == y) {
            return x;
        }
        // INFINITY (CASE3)
        if ((x & MASK_INF) == MASK_INF) { // x = +/-infinity
            // if x is neg infinity, there is no way it is greater than y, return y
            // x is pos infinity, it is greater, unless y is positive infinity =>
            // return y!=pos_infinity
            if (((x & MASK_SIGN) == MASK_SIGN)) { // x = -infinity
                return y;
            } else { // x = +infinity
                return x;
            }
        } else if ((y & MASK_INF) == MASK_INF) {
            // x is finite, so if y is positive infinity, then x is less, return y
            //                 if y is negative infinity, then x is greater, return x
            return ((y & MASK_SIGN) == MASK_SIGN) ? x : y;
        }
        // if steering bits are 11 (condition will be 0), then exponent is G[0:w+1] =>
        if ((x & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            exp_x = (int) ((x & MASK_BINARY_EXPONENT2) >>> 51);
            sig_x = (x & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
        } else {
            exp_x = (int) ((x & MASK_BINARY_EXPONENT1) >>> 53);
            sig_x = (x & MASK_BINARY_SIG1);
        }

        // if steering bits are 11 (condition will be 0), then exponent is G[0:w+1] =>
        if ((y & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            exp_y = (int) ((y & MASK_BINARY_EXPONENT2) >>> 51);
            sig_y = (y & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
        } else {
            exp_y = (int) ((y & MASK_BINARY_EXPONENT1) >>> 53);
            sig_y = (y & MASK_BINARY_SIG1);
        }

        // ZERO (CASE4)
        // some properties:
        //    (+ZERO == -ZERO) => therefore
        //        ignore the sign, and neither number is greater
        //    (ZERO x 10^A == ZERO x 10^B) for any valid A, B =>
        //        ignore the exponent field
        //    (Any non-canonical # is considered 0)
        if (sig_x == 0) {
            x_is_zero = true;
        }
        if (sig_y == 0) {
            y_is_zero = true;
        }

        if (x_is_zero && y_is_zero) {
            // if both numbers are zero, neither is greater => return NOTGREATERTHAN
            return y;
        } else if (x_is_zero) {
            // is x is zero, it is greater if Y is negative
            return ((y & MASK_SIGN) == MASK_SIGN) ? x : y;
        } else if (y_is_zero) {
            // is y is zero, X is greater if it is positive
            return ((x & MASK_SIGN) != MASK_SIGN) ? x : y;
        }
        // OPPOSITE SIGN (CASE5)
        // now, if the sign bits differ, x is greater if y is negative
        if (((x ^ y) & MASK_SIGN) == MASK_SIGN) {
            return ((y & MASK_SIGN) == MASK_SIGN) ? x : y;
        }
        // REDUNDANT REPRESENTATIONS (CASE6)

        // if both components are either bigger or smaller,
        //     it is clear what needs to be done
        if (UnsignedLong.isGreater(sig_x, sig_y) && exp_x >= exp_y) {
            return ((x & MASK_SIGN) != MASK_SIGN) ? x : y;
        }
        if (UnsignedLong.isLess(sig_x, sig_y) && exp_x <= exp_y) {
            return ((x & MASK_SIGN) == MASK_SIGN) ? x : y;
        }
        // if exp_x is 15 greater than exp_y, no need for compensation
        if (exp_x - exp_y > 15) {
            return ((x & MASK_SIGN) != MASK_SIGN) ? x : y;    // difference cannot be > 10^15
        }
        // if exp_x is 15 less than exp_y, no need for compensation
        if (exp_y - exp_x > 15) {
            return ((x & MASK_SIGN) == MASK_SIGN) ? x : y;
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

            // if postitive, return whichever significand is larger
            // (converse if negative)
            if (sig_n_prime_w1 == 0 && (sig_n_prime_w0 == sig_y)) {
                return y;
            }
            return ((/*UnsignedLong.isGreater*/(sig_n_prime_w1 != 0)
                || UnsignedLong.isGreater(sig_n_prime_w0, sig_y)) ^ ((x & MASK_SIGN) == MASK_SIGN)) ? x : y;
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

        // if postitive, return whichever significand is larger (converse if negative)
        if (sig_n_prime_w1 == 0 && (sig_n_prime_w0 == sig_x)) {
            return y;
        }
        return (((sig_n_prime_w1 == 0)
            && UnsignedLong.isGreater(sig_x, sig_n_prime_w0)) ^ ((x & MASK_SIGN) == MASK_SIGN)) ? x : y;
    }
}
