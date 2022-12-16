package deltix.dfp;

import static deltix.dfp.JavaImplAdd.*;
import static deltix.dfp.JavaImplCast.bid_nr_digits_flat;
import static deltix.dfp.JavaImplCast.bid_ten2k64;
import static deltix.dfp.JavaImplCmp.*;
import static deltix.dfp.JavaImplParse.*;

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

        if (valid_x == 0) {
            // x is Inf. or NaN or 0
//            if ((x & SNAN_MASK64) == SNAN_MASK64)    // y is sNaN
//                __set_status_flags(pfpsf, BID_INVALID_EXCEPTION);
            final long res;
            if (coefficient_x != 0)
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
            while (UnsignedLong.isLess(coefficient_x, 1000000000000000L)
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

    static int P16 = 16;
    static long /*BID_UINT64*/ EXP_MIN = 0x0000000000000000L;    // EXP_MIN = (-6176 + 6176) << 49

    public static long bid64_nextup(long /*BID_UINT64*/ x/*, final JavaImplParse.FloatingPointStatusFlag pfpsf*/) {
        long /*BID_UINT64*/ res;
        long /*BID_UINT64*/ x_sign;
        long /*BID_UINT64*/ x_exp;
        long /*BID_UI64DOUBLE*/ tmp1_ui64;
        int x_nr_bits;
        int q1, ind;
        long /*BID_UINT64*/ C1;            // C1 represents x_signif (BID_UINT64)

        // check for NaNs and infinities
        if ((x & MASK_NAN) == MASK_NAN) {    // check for NaN
            if (UnsignedLong.isGreater(x & 0x0003ffffffffffffL, 999999999999999L))
                x = x & 0xfe00000000000000L;    // clear G6-G12 and the payload bits
            else
                x = x & 0xfe03ffffffffffffL;    // clear G6-G12
            if ((x & MASK_SNAN) == MASK_SNAN) {    // SNaN
                // set invalid flag
//                __set_status_flags(pfpsf, BID_INVALID_EXCEPTION);
                // return quiet (SNaN)
                res = x & 0xfdffffffffffffffL;
            } else {    // QNaN
                res = x;
            }
            return res;
        } else if ((x & MASK_INF) == MASK_INF) {    // check for Infinity
            if ((x & 0x8000000000000000L) == 0) {    // x is +inf
                res = 0x7800000000000000L;
            } else {    // x is -inf
                res = 0xf7fb86f26fc0ffffL;    // -MAXFP = -999...99 * 10^emax
            }
            return res;
        }
        // unpack the argument
        x_sign = x & MASK_SIGN;    // 0 for positive, MASK_SIGN for negative
        // if steering bits are 11 (condition will be 0), then exponent is G[0:w+1] =>
        if ((x & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            x_exp = (x & MASK_BINARY_EXPONENT2) >>> 51;    // biased
            C1 = (x & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
            if (UnsignedLong.isGreater(C1, 9999999999999999L)) {    // non-canonical
                x_exp = 0;
                C1 = 0;
            }
        } else {
            x_exp = (x & MASK_BINARY_EXPONENT1) >>> 53;    // biased
            C1 = x & MASK_BINARY_SIG1;
        }

        // check for zeros (possibly from non-canonical values)
        if (C1 == 0x0L) {
            // x is 0
            res = 0x0000000000000001L;    // MINFP = 1 * 10^emin
        } else {    // x is not special and is not zero
            if (x == 0x77fb86f26fc0ffffL) {
                // x = +MAXFP = 999...99 * 10^emax
                res = 0x7800000000000000L;    // +inf
            } else if (x == 0x8000000000000001L) {
                // x = -MINFP = 1...99 * 10^emin
                res = 0x8000000000000000L;    // -0
            } else {    // -MAXFP <= x <= -MINFP - 1 ulp OR MINFP <= x <= MAXFP - 1 ulp
                // can add/subtract 1 ulp to the significand

                // Note: we could check here if x >= 10^16 to speed up the case q1 =16
                // q1 = nr. of decimal digits in x (1 <= q1 <= 54)
                //  determine first the nr. of bits in x
                if (UnsignedLong.isGreaterOrEqual(C1, MASK_BINARY_OR2)) {    // x >= 2^53
                    // split the 64-bit value in two 32-bit halves to avoid rounding errors
                    if (UnsignedLong.isGreaterOrEqual(C1, 0x0000000100000000L)) {    // x >= 2^32
                        tmp1_ui64 = Double.doubleToRawLongBits((double) (C1 >>> 32));    // exact conversion
                        x_nr_bits = 33 + ((((int) (tmp1_ui64 >>> 52)) & 0x7ff) - 0x3ff);
                    } else {    // x < 2^32
                        tmp1_ui64 = Double.doubleToRawLongBits((double) C1);    // exact conversion
                        x_nr_bits = 1 + ((((int) (tmp1_ui64 >>> 52)) & 0x7ff) - 0x3ff);
                    }
                } else {    // if x < 2^53
                    tmp1_ui64 = Double.doubleToRawLongBits((double) C1);    // exact conversion
                    x_nr_bits = 1 + ((((int) (tmp1_ui64 >>> 52)) & 0x7ff) - 0x3ff);
                }
                q1 = (int) bid_nr_digits_flat[((x_nr_bits - 1) << 2) /*+ 0 .digits*/];
                if (q1 == 0) {
                    q1 = (int) bid_nr_digits_flat[((x_nr_bits - 1) << 2) + 3/*.digits1*/];
                    if (UnsignedLong.isGreaterOrEqual(C1, bid_nr_digits_flat[((x_nr_bits - 1) << 2) + 2/*.threshold_lo*/]))
                        q1++;
                }
                // if q1 < P16 then pad the significand with zeros
                if (q1 < P16 /* @SIGNED_COMPARE */) {
                    if (UnsignedLong.isGreater(x_exp, P16 - q1)) {
                        ind = P16 - q1;    // 1 <= ind <= P16 - 1
                        // pad with P16 - q1 zeros, until exponent = emin
                        // C1 = C1 * 10^ind
                        C1 = C1 * bid_ten2k64[ind];
                        x_exp = x_exp - ind;
                    } else {    // pad with zeros until the exponent reaches emin
                        ind = (int) x_exp;
                        C1 = C1 * bid_ten2k64[ind];
                        x_exp = EXP_MIN;
                    }
                }
                if (x_sign == 0) {    // x > 0
                    // add 1 ulp (add 1 to the significand)
                    C1++;
                    if (C1 == 0x002386f26fc10000L) {    // if  C1 = 10^16
                        C1 = 0x00038d7ea4c68000L;    // C1 = 10^15
                        x_exp++;
                    }
                    // Ok, because MAXFP = 999...99 * 10^emax was caught already
                } else {    // x < 0
                    // subtract 1 ulp (subtract 1 from the significand)
                    C1--;
                    if (C1 == 0x00038d7ea4c67fffL && x_exp != 0) {    // if  C1 = 10^15 - 1
                        C1 = 0x002386f26fc0ffffL;    // C1 = 10^16 - 1
                        x_exp--;
                    }
                }
                // assemble the result
                // if significand has 54 bits
                if ((C1 & MASK_BINARY_OR2) != 0) {
                    res =
                        x_sign | (x_exp << 51) | MASK_STEERING_BITS | (C1 &
                            MASK_BINARY_SIG2);
                } else {    // significand fits in 53 bits
                    res = x_sign | (x_exp << 53) | C1;
                }
            }    // end -MAXFP <= x <= -MINFP - 1 ulp OR MINFP <= x <= MAXFP - 1 ulp
        }    // end x is not special and is not zero
        return res;
    }

    public static long bid64_nextdown(long /*BID_UINT64*/ x/*, final JavaImplParse.FloatingPointStatusFlag pfpsf*/) {
        long /*BID_UINT64*/ res;
        long /*BID_UINT64*/ x_sign;
        long /*BID_UINT64*/ x_exp;
        long /*BID_UI64DOUBLE*/ tmp1_ui64;
        int x_nr_bits;
        int q1, ind;
        long /*BID_UINT64*/ C1;            // C1 represents x_signif (BID_UINT64)

        // check for NaNs and infinities
        if ((x & MASK_NAN) == MASK_NAN) {    // check for NaN
            if (UnsignedLong.isGreater(x & 0x0003ffffffffffffL, 999999999999999L))
                x = x & 0xfe00000000000000L;    // clear G6-G12 and the payload bits
            else
                x = x & 0xfe03ffffffffffffL;    // clear G6-G12
            if ((x & MASK_SNAN) == MASK_SNAN) {    // SNaN
                // set invalid flag
//                __set_status_flags(pfpsf, BID_INVALID_EXCEPTION);
                // return quiet (SNaN)
                res = x & 0xfdffffffffffffffL;
            } else {    // QNaN
                res = x;
            }
            return res;
        } else if ((x & MASK_INF) == MASK_INF) {    // check for Infinity
            if ((x & 0x8000000000000000L) != 0) {    // x is -inf
                res = 0xf800000000000000L;
            } else {    // x is +inf
                res = 0x77fb86f26fc0ffffL;    // +MAXFP = +999...99 * 10^emax
            }
            return res;
        }
        // unpack the argument
        x_sign = x & MASK_SIGN;    // 0 for positive, MASK_SIGN for negative
        // if steering bits are 11 (condition will be 0), then exponent is G[0:w+1] =>
        if ((x & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            x_exp = (x & MASK_BINARY_EXPONENT2) >>> 51;    // biased
            C1 = (x & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
            if (UnsignedLong.isGreater(C1, 9999999999999999L)) {    // non-canonical
                x_exp = 0;
                C1 = 0;
            }
        } else {
            x_exp = (x & MASK_BINARY_EXPONENT1) >>> 53;    // biased
            C1 = x & MASK_BINARY_SIG1;
        }

        // check for zeros (possibly from non-canonical values)
        if (C1 == 0x0L) {
            // x is 0
            res = 0x8000000000000001L;    // -MINFP = -1 * 10^emin
        } else {    // x is not special and is not zero
            if (x == 0xf7fb86f26fc0ffffL) {
                // x = -MAXFP = -999...99 * 10^emax
                res = 0xf800000000000000L;    // -inf
            } else if (x == 0x0000000000000001L) {
                // x = +MINFP = 1...99 * 10^emin
                res = 0x0000000000000000L;    // -0
            } else {    // -MAXFP + 1ulp <= x <= -MINFP OR MINFP + 1 ulp <= x <= MAXFP
                // can add/subtract 1 ulp to the significand

                // Note: we could check here if x >= 10^16 to speed up the case q1 =16
                // q1 = nr. of decimal digits in x (1 <= q1 <= 16)
                //  determine first the nr. of bits in x
                if (UnsignedLong.isGreaterOrEqual(C1, 0x0020000000000000L)) {    // x >= 2^53
                    // split the 64-bit value in two 32-bit halves to avoid
                    // rounding errors
                    if (UnsignedLong.isGreaterOrEqual(C1, 0x0000000100000000L)) {    // x >= 2^32
                        tmp1_ui64 = Double.doubleToRawLongBits((double) (C1 >>> 32));    // exact conversion
                        x_nr_bits = 33 + ((((int) (tmp1_ui64 >>> 52)) & 0x7ff) - 0x3ff);
                    } else {    // x < 2^32
                        tmp1_ui64 = Double.doubleToRawLongBits((double) C1);    // exact conversion
                        x_nr_bits = 1 + ((((int) (tmp1_ui64 >>> 52)) & 0x7ff) - 0x3ff);
                    }
                } else {    // if x < 2^53
                    tmp1_ui64 = Double.doubleToRawLongBits((double) C1);    // exact conversion
                    x_nr_bits = 1 + ((((int) (tmp1_ui64 >>> 52)) & 0x7ff) - 0x3ff);
                }
                q1 = (int) bid_nr_digits_flat[((x_nr_bits - 1) << 2) /*+ 0 .digits*/];
                if (q1 == 0) {
                    q1 = (int) bid_nr_digits_flat[((x_nr_bits - 1) << 2) + 3 /*.digits1*/];
                    if (UnsignedLong.isGreaterOrEqual(C1, bid_nr_digits_flat[((x_nr_bits - 1) << 2) + 2 /*.threshold_lo*/]))
                        q1++;
                }
                // if q1 < P16 then pad the significand with zeros
                if (q1 < P16 /* @SIGNED_COMPARE */) {
                    if (UnsignedLong.isGreater(x_exp, P16 - q1)) {
                        ind = P16 - q1;    // 1 <= ind <= P16 - 1
                        // pad with P16 - q1 zeros, until exponent = emin
                        // C1 = C1 * 10^ind
                        C1 = C1 * bid_ten2k64[ind];
                        x_exp = x_exp - ind;
                    } else {    // pad with zeros until the exponent reaches emin
                        ind = (int) x_exp;
                        C1 = C1 * bid_ten2k64[ind];
                        x_exp = EXP_MIN;
                    }
                }
                if (x_sign != 0) {    // x < 0
                    // add 1 ulp (add 1 to the significand)
                    C1++;
                    if (C1 == 0x002386f26fc10000L) {    // if  C1 = 10^16
                        C1 = 0x00038d7ea4c68000L;    // C1 = 10^15
                        x_exp++;
                        // Ok, because -MAXFP = -999...99 * 10^emax was caught already
                    }
                } else {    // x > 0
                    // subtract 1 ulp (subtract 1 from the significand)
                    C1--;
                    if (C1 == 0x00038d7ea4c67fffL && x_exp != 0) {    // if  C1 = 10^15 - 1
                        C1 = 0x002386f26fc0ffffL;    // C1 = 10^16 - 1
                        x_exp--;
                    }
                }
                // assemble the result
                // if significand has 54 bits
                if ((C1 & MASK_BINARY_OR2) != 0) {
                    res =
                        x_sign | (x_exp << 51) | MASK_STEERING_BITS | (C1 &
                            MASK_BINARY_SIG2);
                } else {    // significand fits in 53 bits
                    res = x_sign | (x_exp << 53) | C1;
                }
            }    // end -MAXFP <= x <= -MINFP - 1 ulp OR MINFP <= x <= MAXFP - 1 ulp
        }    // end x is not special and is not zero
        return res;
    }
}
