package com.epam.deltix.dfp;

import static com.epam.deltix.dfp.JavaImplAdd.*;
import static com.epam.deltix.dfp.JavaImplParse.*;

class JavaImplEtc {
    private JavaImplEtc() {
    }

    public static long bid64_scalbn(final long /*BID_UINT64*/ x, final int n/*, final int rnd_mode, final JavaImplParse.FloatingPointStatusFlag pfpsf*/) {
        long /*BID_SINT64*/ exp64;

        // unpack arguments, check for NaN or Infinity
        //valid_x = unpack_BID64 (&sign_x, &exponent_x, &coefficient_x, x);
        final long sign_x;
        int exponent_x;
        long coefficient_x;
        final long valid_x;
        {
            sign_x = x & 0x8000000000000000L;

            if ((x & SPECIAL_ENCODING_MASK64) != SPECIAL_ENCODING_MASK64) {
                // exponent
                long tmp = x >>> EXPONENT_SHIFT_SMALL64;
                exponent_x = (int) (tmp & EXPONENT_MASK64);
                // coefficient
                coefficient_x = (x & SMALL_COEFF_MASK64);

                valid_x = coefficient_x;
            } else {
                // special encodings
                if ((x & INFINITY_MASK64) == INFINITY_MASK64) {
                    exponent_x = 0;
                    coefficient_x = x & 0xfe03ffffffffffffL;
                    if ((UnsignedLong.isGreaterOrEqual(x & 0x0003ffffffffffffL, 1000000000000000L)))
                        coefficient_x = x & 0xfe00000000000000L;
                    if ((x & NAN_MASK64) == INFINITY_MASK64)
                        coefficient_x = x & SINFINITY_MASK64;
                    valid_x = 0;    // NaN or Infinity
                } else {
                    // coefficient
                    long coeff = (x & LARGE_COEFF_MASK64) | LARGE_COEFF_HIGH_BIT64;
                    // check for non-canonical values
                    if ((UnsignedLong.isGreaterOrEqual(coeff, 10000000000000000L)))
                        coeff = 0;
                    coefficient_x = coeff;
                    // get exponent
                    long tmp = x >>> EXPONENT_SHIFT_LARGE64;
                    exponent_x = (int) (tmp & EXPONENT_MASK64);
                    valid_x = coeff;
                }
            }
        }

        if (valid_x == 0){
            // x is Inf. or NaN or 0
//            if ((x & SNAN_MASK64) == SNAN_MASK64)    // y is sNaN
//                __set_status_flags(pfpsf, BID_INVALID_EXCEPTION);
            final long res;
            if (coefficient_x!=0)
                res = coefficient_x & QUIET_MASK64;
            else {
                exp64 = (long) exponent_x + (long) n;
                if (exp64 < 0 /* @SIGNED_COMPARE */)
                    exp64 = 0;
                if (exp64 > MAX_DECIMAL_EXPONENT /* @SIGNED_COMPARE */)
                    exp64 = MAX_DECIMAL_EXPONENT;
                exponent_x = (int) exp64;
                res = very_fast_get_BID64(sign_x, exponent_x, coefficient_x);    // 0
            }
            return res;
        }

        exp64 = (long) exponent_x + (long) n;
        exponent_x = (int) exp64;

        if (UnsignedInteger.isLessOrEqual(exponent_x, MAX_DECIMAL_EXPONENT)) {
            return very_fast_get_BID64(sign_x, exponent_x, coefficient_x);
        }
        // check for overflow
        if (exp64 > MAX_DECIMAL_EXPONENT /* @SIGNED_COMPARE */) {
            // try to normalize coefficient
            while (UnsignedLong.isLess  (coefficient_x , 1000000000000000L)
                && (exp64 > MAX_DECIMAL_EXPONENT /* @SIGNED_COMPARE */)) {
                // coefficient_x < 10^15, scale by 10
                coefficient_x = (coefficient_x << 1) + (coefficient_x << 3);
                exponent_x--;
                exp64--;
            }
            if (exp64 <= MAX_DECIMAL_EXPONENT /* @SIGNED_COMPARE */) {
                return very_fast_get_BID64(sign_x, exponent_x, coefficient_x);
            } else
                exponent_x = 0x7fffffff;    // overflow
        }
        // exponent < 0
        // the BID pack routine will round the coefficient
        return JavaImplMul.get_BID64(sign_x, exponent_x, coefficient_x/*, rnd_mode, pfpsf*/);
    }
}
