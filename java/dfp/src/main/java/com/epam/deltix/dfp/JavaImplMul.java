package com.epam.deltix.dfp;

import static com.epam.deltix.dfp.JavaImplAdd.*;
import static com.epam.deltix.dfp.JavaImplAdd.BID_ROUNDING_DOWN;
import static com.epam.deltix.dfp.JavaImplAdd.BID_ROUNDING_TIES_AWAY;
import static com.epam.deltix.dfp.JavaImplAdd.BID_ROUNDING_TO_NEAREST;
import static com.epam.deltix.dfp.JavaImplAdd.BID_ROUNDING_TO_ZERO;
import static com.epam.deltix.dfp.JavaImplAdd.BID_ROUNDING_UP;
import static com.epam.deltix.dfp.JavaImplAdd.DECIMAL_EXPONENT_BIAS;
import static com.epam.deltix.dfp.JavaImplAdd.DECIMAL_MAX_EXPON_64;
import static com.epam.deltix.dfp.JavaImplAdd.EXPONENT_MASK64;
import static com.epam.deltix.dfp.JavaImplAdd.EXPONENT_SHIFT_LARGE64;
import static com.epam.deltix.dfp.JavaImplAdd.EXPONENT_SHIFT_SMALL64;
import static com.epam.deltix.dfp.JavaImplAdd.INFINITY_MASK64;
import static com.epam.deltix.dfp.JavaImplAdd.LARGEST_BID64;
import static com.epam.deltix.dfp.JavaImplAdd.LARGE_COEFF_HIGH_BIT64;
import static com.epam.deltix.dfp.JavaImplAdd.LARGE_COEFF_MASK64;
import static com.epam.deltix.dfp.JavaImplAdd.NAN_MASK64;
import static com.epam.deltix.dfp.JavaImplAdd.SINFINITY_MASK64;
import static com.epam.deltix.dfp.JavaImplAdd.SMALLEST_BID64;
import static com.epam.deltix.dfp.JavaImplAdd.SMALL_COEFF_MASK64;
import static com.epam.deltix.dfp.JavaImplAdd.SPECIAL_ENCODING_MASK64;
import static com.epam.deltix.dfp.JavaImplAdd.bid_round_const_table;
import static com.epam.deltix.dfp.JavaImplParse.*;

public class JavaImplMul {
    private JavaImplMul() {
    }

    /**
     * _  Intel's original
     * _  Algorithm description:
     * _
     * _  if(number_digits(coefficient_x)+number_digits(coefficient_y) guaranteed
     * _       below 16)
     * _      return get_BID64(sign_x^sign_y, exponent_x + exponent_y - dec_bias,
     * _                     coefficient_x*coefficient_y)
     * _  else
     * _      get long product: coefficient_x*coefficient_y
     * _      determine number of digits to round off (extra_digits)
     * _      rounding is performed as a 128x128-bit multiplication by
     * _         2^M[extra_digits]/10^extra_digits, followed by a shift
     * _         M[extra_digits] is sufficiently large for required accuracy
     *
     * @param x Fist factor
     * @param y Second factor
     * @return The multiplication of operands
     * <p>
     * DECIMAL_TINY_DETECTION_AFTER_ROUNDING: 0
     */
    public static long bid64_mul(final long x, final long y) {
        long P_w0, P_w1, C128_w0, C128_w1, Q_high_w0, Q_high_w1, Q_low_w0, Q_low_w1;
        long C64, remainder_h;
        int extra_digits, bin_expon_cx, bin_expon_cy, bin_expon_product;
        int digits_p, bp, amount, amount2, final_exponent, round_up;
        //  unsigned status, uf_status;

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
                    if ((/*UnsignedLong.compare*/(x & 0x0003ffffffffffffL) + Long.MIN_VALUE >= (1000000000000000L) + Long.MIN_VALUE))
                        coefficient_x = x & 0xfe00000000000000L;
                    if ((x & NAN_MASK64) == INFINITY_MASK64)
                        coefficient_x = x & SINFINITY_MASK64;
                    valid_x = 0;    // NaN or Infinity
                } else {
                    // coefficient
                    long coeff = (x & LARGE_COEFF_MASK64) | LARGE_COEFF_HIGH_BIT64;
                    // check for non-canonical values
                    if ((/*UnsignedLong.compare*/(coeff) + Long.MIN_VALUE >= (10000000000000000L) + Long.MIN_VALUE))
                        coeff = 0;
                    coefficient_x = coeff;
                    // get exponent
                    long tmp = x >>> EXPONENT_SHIFT_LARGE64;
                    exponent_x = (int) (tmp & EXPONENT_MASK64);
                    valid_x = coeff;
                }
            }
        }

        //valid_y = unpack_BID64 (&sign_y, &exponent_y, &coefficient_y, y);
        long sign_y;
        int exponent_y;
        long coefficient_y;
        final long valid_y;
        {
            sign_y = y & 0x8000000000000000L;

            if ((y & SPECIAL_ENCODING_MASK64) != SPECIAL_ENCODING_MASK64) {
                // exponent
                long tmp = y >>> EXPONENT_SHIFT_SMALL64;
                exponent_y = (int) (tmp & EXPONENT_MASK64);
                // coefficient
                coefficient_y = (y & SMALL_COEFF_MASK64);

                valid_y = coefficient_y;
            } else {
                // special encodings
                if ((y & INFINITY_MASK64) == INFINITY_MASK64) {
                    exponent_y = 0;
                    coefficient_y = y & 0xfe03ffffffffffffL;
                    if ((/*UnsignedLong.compare*/(y & 0x0003ffffffffffffL) + Long.MIN_VALUE >= (1000000000000000L) + Long.MIN_VALUE))
                        coefficient_y = y & 0xfe00000000000000L;
                    if ((y & NAN_MASK64) == INFINITY_MASK64)
                        coefficient_y = y & SINFINITY_MASK64;
                    valid_y = 0;    // NaN or Infinity
                } else {
                    // coefficient
                    long coeff = (y & LARGE_COEFF_MASK64) | LARGE_COEFF_HIGH_BIT64;
                    // check for non-canonical values
                    if ((/*UnsignedLong.compare*/(coeff) + Long.MIN_VALUE >= (10000000000000000L) + Long.MIN_VALUE))
                        coeff = 0;
                    coefficient_y = coeff;
                    // get exponent
                    long tmp = y >>> EXPONENT_SHIFT_LARGE64;
                    exponent_y = (int) (tmp & EXPONENT_MASK64);
                    valid_y = coeff;
                }
            }
        }

        // unpack arguments, check for NaN or Infinity
        if (valid_x == 0) {
            // x is Inf. or NaN

            // test if x is NaN
            if ((x & NAN_MASK64) == NAN_MASK64) {
                return coefficient_x & QUIET_MASK64;
            }
            // x is Infinity?
            if ((x & INFINITY_MASK64) == INFINITY_MASK64) {
                // check if y is 0
                if (((y & INFINITY_MASK64) != INFINITY_MASK64) && coefficient_y == 0) {
                    // y==0 , return NaN
                    return NAN_MASK64;
                }
                // check if y is NaN
                if ((y & NAN_MASK64) == NAN_MASK64)
                    // y==NaN , return NaN
                    return coefficient_y & QUIET_MASK64;
                // otherwise return +/-Inf
                return (((x ^ y) & 0x8000000000000000L) | INFINITY_MASK64);
            }
            // x is 0
            if (((y & INFINITY_MASK64) != INFINITY_MASK64)) {
                if ((y & SPECIAL_ENCODING_MASK64) == SPECIAL_ENCODING_MASK64)
                    exponent_y = ((int) (y >>> 51)) & 0x3ff;
                else
                    exponent_y = ((int) (y >>> 53)) & 0x3ff;
                sign_y = y & 0x8000000000000000L;

                exponent_x += exponent_y - DECIMAL_EXPONENT_BIAS;
                if (exponent_x > DECIMAL_MAX_EXPON_64)
                    exponent_x = DECIMAL_MAX_EXPON_64;
                else if (exponent_x < 0)
                    exponent_x = 0;
                return (sign_x ^ sign_y) | (((long) exponent_x) << 53);
            }
        }
        if (valid_y == 0) {
            // y is Inf. or NaN

            // test if y is NaN
            if ((y & NAN_MASK64) == NAN_MASK64) {
                return coefficient_y & QUIET_MASK64;
            }
            // y is Infinity?
            if ((y & INFINITY_MASK64) == INFINITY_MASK64) {
                // check if x is 0
                if (coefficient_x == 0) {
                    // x==0, return NaN
                    return NAN_MASK64;
                }
                // otherwise return +/-Inf
                return (((x ^ y) & 0x8000000000000000L) | INFINITY_MASK64);
            }
            // y is 0
            exponent_x += exponent_y - DECIMAL_EXPONENT_BIAS;
            if (exponent_x > DECIMAL_MAX_EXPON_64)
                exponent_x = DECIMAL_MAX_EXPON_64;
            else if (exponent_x < 0)
                exponent_x = 0;
            return ((sign_x ^ sign_y) | (((long) exponent_x) << 53));
        }
        //--- get number of bits in the coefficients of x and y ---
        // version 2 (original)
        long tempxi = Double.doubleToLongBits((double) coefficient_x);
        bin_expon_cx = (int) ((tempxi & MASK_BINARY_EXPONENT) >>> 52);
        long tempyi = Double.doubleToLongBits((double) coefficient_y);
        bin_expon_cy = (int) ((tempyi & MASK_BINARY_EXPONENT) >>> 52);

        // magnitude estimate for coefficient_x*coefficient_y is
        //        2^(unbiased_bin_expon_cx + unbiased_bin_expon_cx)
        bin_expon_product = bin_expon_cx + bin_expon_cy;

        // check if coefficient_x*coefficient_y<2^(10*k+3)
        // equivalent to unbiased_bin_expon_cx + unbiased_bin_expon_cx < 10*k+1
        if (bin_expon_product < UPPER_EXPON_LIMIT + 2 * BINARY_EXPONENT_BIAS) {
            //  easy multiply
            C64 = coefficient_x * coefficient_y;

            return get_BID64_small_mantissa(sign_x ^ sign_y,
                exponent_x + exponent_y - DECIMAL_EXPONENT_BIAS, C64);
        } else {
            // get 128-bit product: coefficient_x*coefficient_y
            //__mul_64x64_to_128(P, coefficient_x, coefficient_y);
            {
                long __CX = coefficient_x;
                long __CY = coefficient_y;
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

                P_w1 = __PH + (__PM >>> 32);
                P_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
            }

            // tighten binary range of P:  leading bit is 2^bp
            // unbiased_bin_expon_product <= bp <= unbiased_bin_expon_product+1
            bin_expon_product -= 2 * BINARY_EXPONENT_BIAS;

            //__tight_bin_range_128(bp, P, bin_expon_product); // tighten exponent range
            {
                final long __P_w0 = P_w0;
                final long __P_w1 = P_w1;
                final int __bin_expon = bin_expon_product;
                long M = 1;
                bp = __bin_expon;
                if (bp < 63) {
                    M <<= bp + 1;
                    if ((/*UnsignedLong.compare*/(__P_w0) + Long.MIN_VALUE >= (M) + Long.MIN_VALUE))
                        bp++;
                } else if (bp > 64) {
                    M <<= bp + 1 - 64;
                    if (((/*UnsignedLong.compare*/(__P_w1) + Long.MIN_VALUE > (M) + Long.MIN_VALUE)) || (__P_w1 == M && __P_w0 != 0))
                        bp++;
                } else if (__P_w1 != 0)
                    bp++;
            }

            // get number of decimal digits in the product
            digits_p = bid_estimate_decimal_digits[bp];
            if (!(__unsigned_compare_gt_128(
                bid_power10_table_128_flat[(digits_p << 1) + 0],
                bid_power10_table_128_flat[(digits_p << 1) + 1], P_w0, P_w1)))
                digits_p++;    // if bid_power10_table_128[digits_p] <= P

            // determine number of decimal digits to be rounded out
            extra_digits = digits_p - MAX_FORMAT_DIGITS;
            final_exponent =
                exponent_x + exponent_y + extra_digits - DECIMAL_EXPONENT_BIAS;

            round_up = 0;
            if ((/*UnsignedInteger.compare*/(final_exponent) + Integer.MIN_VALUE >= (3 * 256) + Integer.MIN_VALUE)) {
                if (final_exponent < 0) {
                    // underflow
                    if (final_exponent + 16 < 0) {
                        return sign_x ^ sign_y;
                    }

                    extra_digits -= final_exponent;
                    final_exponent = 0;

                    if (extra_digits > 17) {
                        //__mul_128x128_full(Q_high, Q_low, P, bid_reciprocals10_128[16]);
                        {
                            final long _A_w0 = P_w0;
                            final long _A_w1 = P_w1;
                            final long _B_w0 = bid_reciprocals10_128_flat[(16 << 1) + 0];
                            final long _B_w1 = bid_reciprocals10_128_flat[(16 << 1) + 1];

                            long _ALBL_w0, _ALBH_w0, _AHBL_w0, _AHBH_w0, _QM_w0, _QM2_w0;
                            long _ALBL_w1, _ALBH_w1, _AHBL_w1, _AHBH_w1, _QM_w1, _QM2_w1;

                            //__mul_64x64_to_128(ALBH, (A)_w0, (B)_w1);
                            {
                                long __CX = _A_w0;
                                long __CY = _B_w1;
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

                                _ALBH_w1 = __PH + (__PM >>> 32);
                                _ALBH_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
                            }

                            //__mul_64x64_to_128(AHBL, (B)_w0, (A)_w1);
                            {
                                long __CX = _B_w0;
                                long __CY = _A_w1;
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

                                _AHBL_w1 = __PH + (__PM >>> 32);
                                _AHBL_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
                            }

                            //__mul_64x64_to_128(ALBL, (A)_w0, (B)_w0);
                            {
                                long __CX = _A_w0;
                                long __CY = _B_w0;
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

                                _ALBL_w1 = __PH + (__PM >>> 32);
                                _ALBL_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
                            }

                            //__mul_64x64_to_128(AHBH, (A)_w1,(B)_w1);
                            {
                                long __CX = _A_w1;
                                long __CY = _B_w1;
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

                                _AHBH_w1 = __PH + (__PM >>> 32);
                                _AHBH_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
                            }

                            //__add_128_128(QM, ALBH, AHBL); // add 128-bit value to 128-bit assume no carry-out
                            {
                                final long __A128_w0 = _ALBH_w0;
                                final long __A128_w1 = _ALBH_w1;
                                final long __B128_w0 = _AHBL_w0;
                                final long __B128_w1 = _AHBL_w1;
                                long Q128_w0, Q128_w1;
                                Q128_w1 = __A128_w1 + __B128_w1;
                                Q128_w0 = __B128_w0 + __A128_w0;
                                if ((/*UnsignedLong.compare*/(Q128_w0) + Long.MIN_VALUE < (__B128_w0) + Long.MIN_VALUE))
                                    Q128_w1++;
                                _QM_w1 = Q128_w1;
                                _QM_w0 = Q128_w0;
                            }

                            Q_low_w0 = _ALBL_w0;

                            //__add_128_64(_QM2, QM, ALBL_w1); // add 64-bit value to 128-bit
                            {
                                final long __A128_w0 = _QM_w0;
                                final long __A128_w1 = _QM_w1;
                                final long __B64 = _ALBL_w1;
                                long __R64H;
                                __R64H = __A128_w1;
                                _QM2_w0 = __B64 + __A128_w0;
                                if ((/*UnsignedLong.compare*/(_QM2_w0) + Long.MIN_VALUE < (__B64) + Long.MIN_VALUE))
                                    __R64H++;
                                _QM2_w1 = __R64H;
                            }

                            //__add_128_64((_Qh), AHBH, QM2_w1); // add 64-bit value to 128-bit
                            {
                                final long __A128_w0 = _AHBH_w0;
                                final long __A128_w1 = _AHBH_w1;
                                final long __B64 = _QM2_w1;
                                long __R64H;
                                __R64H = __A128_w1;
                                Q_high_w0 = __B64 + __A128_w0;
                                if ((/*UnsignedLong.compare*/(Q_high_w0) + Long.MIN_VALUE < (__B64) + Long.MIN_VALUE))
                                    __R64H++;
                                Q_high_w1 = __R64H;
                            }

                            Q_low_w1 = _QM2_w0;
                        }

                        amount = bid_recip_scale[16];
                        //__shr_128(P, Q_high, amount);
                        {
                            final long __A_w0 = Q_high_w0;
                            final long __A_w1 = Q_high_w1;
                            final long __k = amount;
                            P_w0 = __A_w0 >>> __k;
                            P_w0 |= __A_w1 << (64 - __k);
                            P_w1 = __A_w1 >>> __k;
                        }

                        // get sticky bits
                        amount2 = 64 - amount;
                        remainder_h = 0;
                        remainder_h--;
                        remainder_h >>>= amount2;
                        remainder_h = remainder_h & Q_high_w0;

                        extra_digits -= 16;
                        if (remainder_h != 0 || ((/*UnsignedLong.compare*/(Q_low_w1) + Long.MIN_VALUE > (bid_reciprocals10_128_flat[(16 << 1) + 1]) + Long.MIN_VALUE)
                            || (Q_low_w1 == bid_reciprocals10_128_flat[(16 << 1) + 1]
                            && (/*UnsignedLong.compare*/(Q_low_w0) + Long.MIN_VALUE >= (bid_reciprocals10_128_flat[(16 << 1) + 0]) + Long.MIN_VALUE)))) {
                            round_up = 1;
                            // __set_status_flags(pfpsf, BID_UNDERFLOW_EXCEPTION | BID_INEXACT_EXCEPTION);
                            P_w0 = (P_w0 << 3) + (P_w0 << 1);
                            P_w0 |= 1;
                            extra_digits++;
                        }
                    }
                } else {
                    return fast_get_BID64_check_OF(sign_x ^ sign_y, final_exponent,
                        1000000000000000L, BID_ROUNDING_TO_NEAREST/*, pfpsf*/);
                }
            }


            if (extra_digits > 0) {
                // will divide by 10^(digits_p - 16)

                // add a constant to P, depending on rounding mode
                // 0.5*10^(digits_p - 16) for round-to-nearest
                //__add_128_64(P, P, bid_round_const_table[rmode][extra_digits]); // add 64-bit value to 128-bit
                {
                    final long __A128_w0 = P_w0;
                    final long __A128_w1 = P_w1;
                    final long __B64 = bid_round_const_table[BID_ROUNDING_TO_NEAREST][extra_digits];
                    long __R64H;
                    __R64H = __A128_w1;
                    P_w0 = __B64 + __A128_w0;
                    if ((/*UnsignedLong.compare*/(P_w0) + Long.MIN_VALUE < (__B64) + Long.MIN_VALUE))
                        __R64H++;
                    P_w1 = __R64H;
                }

                // get P*(2^M[extra_digits])/10^extra_digits
                // __mul_128x128_full(Q_high, Q_low, P, bid_reciprocals10_128[extra_digits]);
                {
                    final long _A_w0 = P_w0;
                    final long _A_w1 = P_w1;
                    final long _B_w0 = bid_reciprocals10_128_flat[(extra_digits << 1) + 0];
                    final long _B_w1 = bid_reciprocals10_128_flat[(extra_digits << 1) + 1];

                    long _ALBL_w0, _ALBH_w0, _AHBL_w0, _AHBH_w0, _QM_w0, _QM2_w0;
                    long _ALBL_w1, _ALBH_w1, _AHBL_w1, _AHBH_w1, _QM_w1, _QM2_w1;

                    //__mul_64x64_to_128(ALBH, (A)_w0, (B)_w1);
                    {
                        long __CX = _A_w0;
                        long __CY = _B_w1;
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

                        _ALBH_w1 = __PH + (__PM >>> 32);
                        _ALBH_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
                    }

                    //__mul_64x64_to_128(AHBL, (B)_w0, (A)_w1);
                    {
                        long __CX = _B_w0;
                        long __CY = _A_w1;
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

                        _AHBL_w1 = __PH + (__PM >>> 32);
                        _AHBL_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
                    }

                    //__mul_64x64_to_128(ALBL, (A)_w0, (B)_w0);
                    {
                        long __CX = _A_w0;
                        long __CY = _B_w0;
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

                        _ALBL_w1 = __PH + (__PM >>> 32);
                        _ALBL_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
                    }

                    //__mul_64x64_to_128(AHBH, (A)_w1,(B)_w1);
                    {
                        long __CX = _A_w1;
                        long __CY = _B_w1;
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

                        _AHBH_w1 = __PH + (__PM >>> 32);
                        _AHBH_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
                    }

                    //__add_128_128(QM, ALBH, AHBL); // add 128-bit value to 128-bit assume no carry-out
                    {
                        final long __A128_w0 = _ALBH_w0;
                        final long __A128_w1 = _ALBH_w1;
                        final long __B128_w0 = _AHBL_w0;
                        final long __B128_w1 = _AHBL_w1;
                        long Q128_w0, Q128_w1;
                        Q128_w1 = __A128_w1 + __B128_w1;
                        Q128_w0 = __B128_w0 + __A128_w0;
                        if ((/*UnsignedLong.compare*/(Q128_w0) + Long.MIN_VALUE < (__B128_w0) + Long.MIN_VALUE))
                            Q128_w1++;
                        _QM_w1 = Q128_w1;
                        _QM_w0 = Q128_w0;
                    }

                    Q_low_w0 = _ALBL_w0;

                    //__add_128_64(_QM2, QM, ALBL_w1); // add 64-bit value to 128-bit
                    {
                        final long __A128_w0 = _QM_w0;
                        final long __A128_w1 = _QM_w1;
                        final long __B64 = _ALBL_w1;
                        long __R64H;
                        __R64H = __A128_w1;
                        _QM2_w0 = __B64 + __A128_w0;
                        if ((/*UnsignedLong.compare*/(_QM2_w0) + Long.MIN_VALUE < (__B64) + Long.MIN_VALUE))
                            __R64H++;
                        _QM2_w1 = __R64H;
                    }

                    //__add_128_64((_Qh), AHBH, QM2_w1); // add 64-bit value to 128-bit
                    {
                        final long __A128_w0 = _AHBH_w0;
                        final long __A128_w1 = _AHBH_w1;
                        final long __B64 = _QM2_w1;
                        long __R64H;
                        __R64H = __A128_w1;
                        Q_high_w0 = __B64 + __A128_w0;
                        if ((/*UnsignedLong.compare*/(Q_high_w0) + Long.MIN_VALUE < (__B64) + Long.MIN_VALUE))
                            __R64H++;
                        Q_high_w1 = __R64H;
                    }

                    Q_low_w1 = _QM2_w0;
                }

                // now get P/10^extra_digits: shift Q_high right by M[extra_digits]-128
                amount = bid_recip_scale[extra_digits];
                //__shr_128(C128, Q_high, amount);
                {
                    final long __A_w0 = Q_high_w0;
                    final long __A_w1 = Q_high_w1;
                    final long __k = amount;
                    C128_w0 = __A_w0 >>> __k;
                    C128_w0 |= __A_w1 << (64 - __k);
                    C128_w1 = __A_w1 >>> __k;
                }

                C64 = C128_w0;

                /*if (BID_ROUNDING_TO_NEAREST == 0)*/    //BID_ROUNDING_TO_NEAREST
                if ((C64 & 1) != 0 && round_up == 0) {
                    // check whether fractional part of initial_P/10^extra_digits
                    // is exactly .5
                    // this is the same as fractional part of
                    // (initial_P + 0.5*10^extra_digits)/10^extra_digits is exactly zero

                    // get remainder
                    remainder_h = Q_high_w0 << (64 - amount);

                    // test whether fractional part is 0
                    if (remainder_h == 0
                        && ((/*UnsignedLong.compare*/(Q_low_w1) + Long.MIN_VALUE < (bid_reciprocals10_128_flat[(extra_digits << 1) + 1]) + Long.MIN_VALUE)
                        || (Q_low_w1 == bid_reciprocals10_128_flat[(extra_digits << 1) + 1]
                        && (/*UnsignedLong.compare*/(Q_low_w0) + Long.MIN_VALUE < (bid_reciprocals10_128_flat[(extra_digits << 1) + 0]) + Long.MIN_VALUE)))) {
                        C64--;
                    }
                }

                // convert to BID and return
                return fast_get_BID64_check_OF(sign_x ^ sign_y, final_exponent, C64, BID_ROUNDING_TO_NEAREST);
            }
            // go to convert_format and exit
            C64 = P_w0;
            return get_BID64(sign_x ^ sign_y,
                exponent_x + exponent_y - DECIMAL_EXPONENT_BIAS, C64);
        }
    }

    static final long[] bid_power10_table_128_flat = {
        0x0000000000000001L, 0x0000000000000000L,    // 10^0
        0x000000000000000aL, 0x0000000000000000L,    // 10^1
        0x0000000000000064L, 0x0000000000000000L,    // 10^2
        0x00000000000003e8L, 0x0000000000000000L,    // 10^3
        0x0000000000002710L, 0x0000000000000000L,    // 10^4
        0x00000000000186a0L, 0x0000000000000000L,    // 10^5
        0x00000000000f4240L, 0x0000000000000000L,    // 10^6
        0x0000000000989680L, 0x0000000000000000L,    // 10^7
        0x0000000005f5e100L, 0x0000000000000000L,    // 10^8
        0x000000003b9aca00L, 0x0000000000000000L,    // 10^9
        0x00000002540be400L, 0x0000000000000000L,    // 10^10
        0x000000174876e800L, 0x0000000000000000L,    // 10^11
        0x000000e8d4a51000L, 0x0000000000000000L,    // 10^12
        0x000009184e72a000L, 0x0000000000000000L,    // 10^13
        0x00005af3107a4000L, 0x0000000000000000L,    // 10^14
        0x00038d7ea4c68000L, 0x0000000000000000L,    // 10^15
        0x002386f26fc10000L, 0x0000000000000000L,    // 10^16
        0x016345785d8a0000L, 0x0000000000000000L,    // 10^17
        0x0de0b6b3a7640000L, 0x0000000000000000L,    // 10^18
        0x8ac7230489e80000L, 0x0000000000000000L,    // 10^19
        0x6bc75e2d63100000L, 0x0000000000000005L,    // 10^20
        0x35c9adc5dea00000L, 0x0000000000000036L,    // 10^21
        0x19e0c9bab2400000L, 0x000000000000021eL,    // 10^22
        0x02c7e14af6800000L, 0x000000000000152dL,    // 10^23
        0x1bcecceda1000000L, 0x000000000000d3c2L,    // 10^24
        0x161401484a000000L, 0x0000000000084595L,    // 10^25
        0xdcc80cd2e4000000L, 0x000000000052b7d2L,    // 10^26
        0x9fd0803ce8000000L, 0x00000000033b2e3cL,    // 10^27
        0x3e25026110000000L, 0x00000000204fce5eL,    // 10^28
        0x6d7217caa0000000L, 0x00000001431e0faeL,    // 10^29
        0x4674edea40000000L, 0x0000000c9f2c9cd0L,    // 10^30
        0xc0914b2680000000L, 0x0000007e37be2022L,    // 10^31
        0x85acef8100000000L, 0x000004ee2d6d415bL,    // 10^32
        0x38c15b0a00000000L, 0x0000314dc6448d93L,    // 10^33
        0x378d8e6400000000L, 0x0001ed09bead87c0L,    // 10^34
        0x2b878fe800000000L, 0x0013426172c74d82L,    // 10^35
        0xb34b9f1000000000L, 0x00c097ce7bc90715L,    // 10^36
        0x00f436a000000000L, 0x0785ee10d5da46d9L,    // 10^37
        0x098a224000000000L, 0x4b3b4ca85a86c47aL,    // 10^38
    };

    //
    //   This pack macro does not check for coefficients above 2^53
    //
    static long get_BID64_small_mantissa(final long sgn, int expon, long coeff) {
        long C128_w0, C128_w1, Q_low_w0, Q_low_w1;
        long r, mask, _C64, remainder_h, QH;
        int extra_digits, amount, amount2;

        // check for possible underflow/overflow
        if ((/*UnsignedInteger.compare*/(expon) + Integer.MIN_VALUE >= (3 * 256) + Integer.MIN_VALUE)) {
            if (expon < 0) {
                // underflow
                if (expon + MAX_FORMAT_DIGITS < 0) {
                    // result is 0
                    return sgn;
                }
                // get digits to be shifted out
                extra_digits = -expon;
                C128_w0 = coeff + bid_round_const_table[BID_ROUNDING_TO_NEAREST][extra_digits];

                // get coeff*(2^M[extra_digits])/10^extra_digits
                //__mul_64x128_full (QH, Q_low, C128_w0, bid_reciprocals10_128[extra_digits]);
                {

                    final long _A = C128_w0;
                    final long _B_w0 = bid_reciprocals10_128_flat[extra_digits << 1];
                    final long _B_w1 = bid_reciprocals10_128_flat[(extra_digits << 1) + 1];

                    long _ALBL_w0, _ALBL_w1, _ALBH_w0, _ALBH_w1, _QM2_w0, _QM2_w1;

                    //__mul_64x64_to_128(out ALBH, A, B.w1);
                    {
                        final long __CX = _A;
                        final long __CY = _B_w1;
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

                        _ALBH_w1 = __PH + (__PM >>> 32);
                        _ALBH_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
                    }
                    //__mul_64x64_to_128(out ALBL, A, B.w0);
                    {
                        final long __CX = _A;
                        final long __CY = _B_w0;
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

                        _ALBL_w1 = __PH + (__PM >>> 32);
                        _ALBL_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
                    }

                    Q_low_w0 = _ALBL_w0;
                    //__add_128_64(out QM2, ALBH, ALBL.w1);
                    {
                        final long __A128_w0 = _ALBH_w0;
                        final long __A128_w1 = _ALBH_w1;
                        final long __B64 = _ALBL_w1;
                        long R64H;
                        R64H = __A128_w1;
                        _QM2_w0 = (__B64) + __A128_w0;
                        if ((/*UnsignedLong.compare*/(_QM2_w0) + Long.MIN_VALUE < (__B64) + Long.MIN_VALUE))
                            R64H++;
                        _QM2_w1 = R64H;
                    }
                    Q_low_w1 = _QM2_w0;
                    QH = _QM2_w1;
                }

                // now get P/10^extra_digits: shift Q_high right by M[extra_digits]-128
                amount = bid_recip_scale[extra_digits];

                _C64 = QH >>> amount;

                /*if (BID_ROUNDING_TO_NEAREST == 0)*/    //BID_ROUNDING_TO_NEAREST
                if ((_C64 & 1) != 0) {
                    // check whether fractional part of initial_P/10^extra_digits is exactly .5

                    // get remainder
                    amount2 = 64 - amount;
                    remainder_h = 0;
                    remainder_h--;
                    remainder_h >>>= amount2;
                    remainder_h = remainder_h & QH;

                    if (remainder_h == 0
                        && ((/*UnsignedLong.compare*/(Q_low_w1) + Long.MIN_VALUE < (bid_reciprocals10_128_flat[(extra_digits << 1) + 1]) + Long.MIN_VALUE)
                        || (Q_low_w1 == bid_reciprocals10_128_flat[(extra_digits << 1) + 1]
                        && (/*UnsignedLong.compare*/(Q_low_w0) + Long.MIN_VALUE < (bid_reciprocals10_128_flat[(extra_digits << 1) + 0]) + Long.MIN_VALUE)))) {
                        _C64--;
                    }
                }

                return sgn | _C64;
            }

            while ((/*UnsignedLong.compare*/(coeff) + Long.MIN_VALUE < (1000000000000000L) + Long.MIN_VALUE) && expon >= 3 * 256) {
                expon--;
                coeff = (coeff << 3) + (coeff << 1);
            }
            if (expon > DECIMAL_MAX_EXPON_64) {
                // overflow
                return sgn | INFINITY_MASK64;
            } else {
                mask = 1;
                mask <<= EXPONENT_SHIFT_SMALL64;
                if ((/*UnsignedLong.compare*/(coeff) + Long.MIN_VALUE >= (mask) + Long.MIN_VALUE)) {
                    r = expon;
                    r <<= EXPONENT_SHIFT_LARGE64;
                    r |= (sgn | SPECIAL_ENCODING_MASK64);
                    // add coeff, without leading bits
                    mask = (mask >>> 2) - 1;
                    coeff &= mask;
                    r |= coeff;
                    return r;
                }
            }
        }

        r = expon;
        r <<= EXPONENT_SHIFT_SMALL64;
        r |= (coeff | sgn);

        return r;
    }

    //
    //   BID64 pack macro (general form)
    //
    public static long get_BID64(long sgn, int expon, long coeff) {
        long Q_low_w0, Q_low_w1;
        long QH, r, mask, _C64, remainder_h, carry;
        int extra_digits, amount, amount2;

        if ((/*UnsignedLong.compare*/(coeff) + Long.MIN_VALUE > (9999999999999999L) + Long.MIN_VALUE)) {
            expon++;
            coeff = 1000000000000000L;
        }
        // check for possible underflow/overflow
        if ((/*UnsignedInteger.compare*/(expon) + Integer.MIN_VALUE >= (3 * 256) + Integer.MIN_VALUE)) {
            if (expon < 0) {
                // underflow
                if (expon + MAX_FORMAT_DIGITS < 0) {
                    // result is 0
                    return sgn;
                }
                // get digits to be shifted out
                extra_digits = -expon;
                coeff += bid_round_const_table[BID_ROUNDING_TO_NEAREST][extra_digits];

                // get coeff*(2^M[extra_digits])/10^extra_digits
                //__mul_64x128_full(out QH, out Q_low, coeff, bid_reciprocals10_128_flat[extra_digits << 1], bid_reciprocals10_128_flat[(extra_digits << 1) + 1]);
                //public static void __mul_64x128_full(/*out*/ long Ph, /*out*/ final BID_UINT128 Ql, final long A, final long B_w0, final long B_w1)
                {

                    final long _A = coeff;
                    final long _B_w0 = bid_reciprocals10_128_flat[extra_digits << 1];
                    final long _B_w1 = bid_reciprocals10_128_flat[(extra_digits << 1) + 1];

                    long _ALBL_w0, _ALBL_w1, _ALBH_w0, _ALBH_w1, _QM2_w0, _QM2_w1;

                    //__mul_64x64_to_128(out ALBH, A, B.w1);
                    {
                        final long __CX = _A;
                        final long __CY = _B_w1;
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

                        _ALBH_w1 = __PH + (__PM >>> 32);
                        _ALBH_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
                    }
                    //__mul_64x64_to_128(out ALBL, A, B.w0);
                    {
                        final long __CX = _A;
                        final long __CY = _B_w0;
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

                        _ALBL_w1 = __PH + (__PM >>> 32);
                        _ALBL_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
                    }

                    Q_low_w0 = _ALBL_w0;
                    //__add_128_64(out QM2, ALBH, ALBL.w1);
                    {
                        final long __A128_w0 = _ALBH_w0;
                        final long __A128_w1 = _ALBH_w1;
                        final long __B64 = _ALBL_w1;
                        long R64H;
                        R64H = __A128_w1;
                        _QM2_w0 = (__B64) + __A128_w0;
                        if ((/*UnsignedLong.compare*/(_QM2_w0) + Long.MIN_VALUE < (__B64) + Long.MIN_VALUE))
                            R64H++;
                        _QM2_w1 = R64H;
                    }
                    Q_low_w1 = _QM2_w0;
                    QH = _QM2_w1;
                }

                // now get P/10^extra_digits: shift Q_high right by M[extra_digits]-128
                amount = bid_recip_scale[extra_digits];

                _C64 = QH >>> amount;

                /*if (BID_ROUNDING_TO_NEAREST == 0)*/ //BID_ROUNDING_TO_NEAREST
                if ((_C64 & 1) != 0) {
                    // check whether fractional part of initial_P/10^extra_digits is exactly .5

                    // get remainder
                    amount2 = 64 - amount;
                    remainder_h = 0;
                    remainder_h--;
                    remainder_h >>>= amount2;
                    remainder_h = remainder_h & QH;

                    if (remainder_h == 0
                        && ((/*UnsignedLong.compare*/(Q_low_w1) + Long.MIN_VALUE < (bid_reciprocals10_128_flat[(extra_digits << 1) + 1]) + Long.MIN_VALUE)
                        || (Q_low_w1 == bid_reciprocals10_128_flat[(extra_digits << 1) + 1]
                        && (/*UnsignedLong.compare*/(Q_low_w0) + Long.MIN_VALUE < (bid_reciprocals10_128_flat[(extra_digits << 1) + 0]) + Long.MIN_VALUE)))) {
                        _C64--;
                    }
                }

                return sgn | _C64;
            }
            if (coeff == 0) {
                if (expon > DECIMAL_MAX_EXPON_64) expon = DECIMAL_MAX_EXPON_64;
            }
            while ((/*UnsignedLong.compare*/(coeff) + Long.MIN_VALUE < (1000000000000000L) + Long.MIN_VALUE) && expon >= 3 * 256) {
                expon--;
                coeff = (coeff << 3) + (coeff << 1);
            }
            if (expon > DECIMAL_MAX_EXPON_64) {
                // overflow
                return sgn | INFINITY_MASK64;
            }
        }

        mask = 1;
        mask <<= EXPONENT_SHIFT_SMALL64;

        // check whether coefficient fits in 10*5+3 bits
        if ((/*UnsignedLong.compare*/(coeff) + Long.MIN_VALUE < (mask) + Long.MIN_VALUE)) {
            r = LONG_LOW_PART & expon;
            r <<= EXPONENT_SHIFT_SMALL64;
            r |= (coeff | sgn);
            return r;
        }
        // special format

        // eliminate the case coeff==10^16 after rounding
        if (coeff == 10000000000000000L) {
            r = LONG_LOW_PART & (expon + 1);
            r <<= EXPONENT_SHIFT_SMALL64;
            r |= (1000000000000000L | sgn);
            return r;
        }

        r = LONG_LOW_PART & expon;
        r <<= EXPONENT_SHIFT_LARGE64;
        r |= (sgn | SPECIAL_ENCODING_MASK64);
        // add coeff, without leading bits
        mask = (mask >>> 2) - 1;
        coeff &= mask;
        r |= coeff;

        return r;
    }

    // greater than
    //  return 0 if A<=B
    //  non-zero if A>B
    static boolean __unsigned_compare_gt_128(final long A_w0, final long A_w1, final long B_w0, final long B_w1) {
        return ((/*UnsignedLong.compare*/(A_w1) + Long.MIN_VALUE > (B_w1) + Long.MIN_VALUE)) ||
            ((A_w1 == B_w1) && ((/*UnsignedLong.compare*/(A_w0) + Long.MIN_VALUE > (B_w0) + Long.MIN_VALUE)));
    }
}
