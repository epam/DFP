package com.epam.deltix.dfp;

import static com.epam.deltix.dfp.JavaImplAdd.*;
import static com.epam.deltix.dfp.JavaImplCmp.*;
import static com.epam.deltix.dfp.JavaImplMul.*;
import static com.epam.deltix.dfp.JavaImplParse.*;

class JavaImplFma {
    private JavaImplFma() {
    }

    /*****************************************************************************
     *    BID64 fma
     *****************************************************************************
     *
     *  Algorithm description:
     *
     *  if multiplication is guranteed exact (short coefficients)
     *     call the unpacked arg. equivalent of bid64_add(x*y, z)
     *  else
     *     get full coefficient_x*coefficient_y product
     *     call subroutine to perform addition of 64-bit argument
     *                                         to 128-bit product
     *
     ****************************************************************************/

    /**
     * _  Intel's original
     * _ Algorithm description:
     * _
     * _ if multiplication is guranteed exact (short coefficients)
     * _    call the unpacked arg. equivalent of bid64_add(x*y, z)
     * _ else
     * _    get full coefficient_x*coefficient_y product
     * _    call subroutine to perform addition of 64-bit argument
     * _                                        to 128-bit product
     *
     * @param x Value to be multiplied.
     * @param y Value to be multiplied.
     * @param z Value to be added.
     * @return x * y + z
     */
    public static long /*BID_UINT64*/ bid64_fma(long /*BID_UINT64*/ x, long /*BID_UINT64*/ y, long /*BID_UINT64*/ z
        /*, final int rnd_mode, final JavaImplParse.FloatingPointStatusFlag pfpsf*/) {
        long /*BID_UINT128*/ P_w0, P_w1, CT_w0, CT_w1, CZ_w0, CZ_w1;
        long /*BID_UINT64*/ sign_x, sign_y, coefficient_x, coefficient_y, sign_z, coefficient_z;
        long /*BID_UINT64*/ C64, remainder_y, res;
        long /*BID_UINT64*/ CYh, CY0L, T, valid_x, valid_y, valid_z;
        //int_double tempx, tempy;
        int extra_digits, exponent_x, exponent_y, bin_expon_cx, bin_expon_cy, bin_expon_product;
        int digits_p, bp, final_exponent, exponent_z, digits_z, ez, ey, scale_z/*, uf_status*/;

        //valid_x = unpack_BID64 (&sign_x, &exponent_x, &coefficient_x, x);
        //valid_y = unpack_BID64 (&sign_y, &exponent_y, &coefficient_y, y);
        // long valid_x = unpack_BID64(&sign_x, &exponent_x, &coefficient_x, x);
        {
            sign_x = x & 0x8000000000000000L;

            if ((x & SPECIAL_ENCODING_MASK64) != SPECIAL_ENCODING_MASK64) {
                // exponent
                final long tmp = x >>> EXPONENT_SHIFT_SMALL64;
                exponent_x = (int) (tmp & EXPONENT_MASK64);
                // coefficient
                coefficient_x = (x & SMALL_COEFF_MASK64);

                valid_x = coefficient_x;
            } else {
                // special encodings
                // coefficient
                long coeff = (x & LARGE_COEFF_MASK64) | LARGE_COEFF_HIGH_BIT64;

                if ((x & INFINITY_MASK64) == INFINITY_MASK64) {
                    exponent_x = 0;
                    coefficient_x = x & 0xfe03ffffffffffffL;
                    if ((x & 0x0003ffffffffffffL) >= 1000000000000000L)
                        coefficient_x = x & 0xfe00000000000000L;
                    if ((x & NAN_MASK64) == INFINITY_MASK64)
                        coefficient_x = x & SINFINITY_MASK64;
                    valid_x = 0;    // NaN or Infinity
                } else {
                    // check for non-canonical values
                    if (coeff >= 10000000000000000L)
                        coeff = 0;
                    coefficient_x = coeff;
                    // get exponent
                    final long tmp = x >>> EXPONENT_SHIFT_LARGE64;
                    exponent_x = (int) (tmp & EXPONENT_MASK64);
                    valid_x = coeff;
                }
            }
        }

        // long valid_y = unpack_BID64(&sign_y, &exponent_y, &coefficient_y, y);
        {
            sign_y = y & 0x8000000000000000L;

            if ((y & SPECIAL_ENCODING_MASK64) != SPECIAL_ENCODING_MASK64) {
                // exponent
                final long tmp = y >>> EXPONENT_SHIFT_SMALL64;
                exponent_y = (int) (tmp & EXPONENT_MASK64);
                // coefficient
                coefficient_y = (y & SMALL_COEFF_MASK64);

                valid_y = coefficient_y;
            } else {
                // special encodings
                // coefficient
                long coeff = (y & LARGE_COEFF_MASK64) | LARGE_COEFF_HIGH_BIT64;

                if ((y & INFINITY_MASK64) == INFINITY_MASK64) {
                    exponent_y = 0;
                    coefficient_y = y & 0xfe03ffffffffffffL;
                    if ((y & 0x0003ffffffffffffL) >= 1000000000000000L)
                        coefficient_y = y & 0xfe00000000000000L;
                    if ((y & NAN_MASK64) == INFINITY_MASK64)
                        coefficient_y = y & SINFINITY_MASK64;
                    valid_y = 0;    // NaN or Infinity
                } else {
                    // check for non-canonical values
                    if (coeff >= 10000000000000000L)
                        coeff = 0;
                    coefficient_y = coeff;
                    // get exponent
                    final long tmp = y >>> EXPONENT_SHIFT_LARGE64;
                    exponent_y = (int) (tmp & EXPONENT_MASK64);
                    valid_y = coeff;
                }
            }
        }

        //valid_z = unpack_BID64 (&sign_z, &exponent_z, &coefficient_z, z);
        {
            sign_z = z & 0x8000000000000000L;

            if ((z & SPECIAL_ENCODING_MASK64) != SPECIAL_ENCODING_MASK64) {
                // exponent
                final long tmp = z >>> EXPONENT_SHIFT_SMALL64;
                exponent_z = (int) (tmp & EXPONENT_MASK64);
                // coefficient
                coefficient_z = (z & SMALL_COEFF_MASK64);

                valid_z = coefficient_z;
            } else {
                // special encodings
                // coefficient
                long coeff = (z & LARGE_COEFF_MASK64) | LARGE_COEFF_HIGH_BIT64;

                if ((z & INFINITY_MASK64) == INFINITY_MASK64) {
                    exponent_z = 0;
                    coefficient_z = z & 0xfe03ffffffffffffL;
                    if ((z & 0x0003ffffffffffffL) >= 1000000000000000L)
                        coefficient_z = z & 0xfe00000000000000L;
                    if ((z & NAN_MASK64) == INFINITY_MASK64)
                        coefficient_z = z & SINFINITY_MASK64;
                    valid_z = 0;    // NaN or Infinity
                } else {
                    // check for non-canonical values
                    if (coeff >= 10000000000000000L)
                        coeff = 0;
                    coefficient_z = coeff;
                    // get exponent
                    final long tmp = z >>> EXPONENT_SHIFT_LARGE64;
                    exponent_z = (int) (tmp & EXPONENT_MASK64);
                    valid_z = coeff;
                }
            }
        }

        // unpack arguments, check for NaN, Infinity, or 0
        if (valid_x == 0 || valid_y == 0 || valid_z == 0) {

            if ((y & MASK_NAN) == MASK_NAN) {    // y is NAN
                // if x = {0, f, inf, NaN}, y = NaN, z = {0, f, inf, NaN} then res = Q (y)
                // check first for non-canonical NaN payload
                y = y & 0xfe03ffffffffffffL;    // clear G6-G12
                if ((y & 0x0003ffffffffffffL) >= 1000000000000000L) {
                    y = y & 0xfe00000000000000L;    // clear G6-G12 and the payload bits
                }
                if ((y & MASK_SNAN) == MASK_SNAN) {    // y is SNAN
                    // set invalid flag
//                    __set_status_flags(pfpsf, BID_INVALID_EXCEPTION);
                    // return quiet (y)
                    res = y & 0xfdffffffffffffffL;
                } else {    // y is QNaN
                    // return y
                    res = y;
                    // if z = SNaN or x = SNaN signal invalid exception
//                    if ((z & MASK_SNAN) == MASK_SNAN || (x & MASK_SNAN) == MASK_SNAN) {
//                        // set invalid flag
//                        __set_status_flags(pfpsf, BID_INVALID_EXCEPTION);
//                    }
                }
                return res;
            } else if ((z & MASK_NAN) == MASK_NAN) {    // z is NAN
                // if x = {0, f, inf, NaN}, y = {0, f, inf}, z = NaN then res = Q (z)
                // check first for non-canonical NaN payload
                z = z & 0xfe03ffffffffffffL;    // clear G6-G12
                if ((z & 0x0003ffffffffffffL) >= 1000000000000000L) {
                    z = z & 0xfe00000000000000L;    // clear G6-G12 and the payload bits
                }
                if ((z & MASK_SNAN) == MASK_SNAN) {    // z is SNAN
                    // set invalid flag
//                    __set_status_flags(pfpsf, BID_INVALID_EXCEPTION);
                    // return quiet (z)
                    res = z & 0xfdffffffffffffffL;
                } else {    // z is QNaN
                    // return z
                    res = z;
                    // if x = SNaN signal invalid exception
//                    if ((x & MASK_SNAN) == MASK_SNAN) {
//                        // set invalid flag
//                        __set_status_flags(pfpsf, BID_INVALID_EXCEPTION);
//                    }
                }
                return res;
            } else if ((x & MASK_NAN) == MASK_NAN) {    // x is NAN
                // if x = NaN, y = {0, f, inf}, z = {0, f, inf} then res = Q (x)
                // check first for non-canonical NaN payload
                x = x & 0xfe03ffffffffffffL;    // clear G6-G12
                if ((x & 0x0003ffffffffffffL) >= 1000000000000000L) {
                    x = x & 0xfe00000000000000L;    // clear G6-G12 and the payload bits
                }
                if ((x & MASK_SNAN) == MASK_SNAN) {    // x is SNAN
                    // set invalid flag
//                    __set_status_flags(pfpsf, BID_INVALID_EXCEPTION);
                    // return quiet (x)
                    res = x & 0xfdffffffffffffffL;
                } else {    // x is QNaN
                    // return x
                    res = x;    // clear out G[6]-G[16]
                }
                return res;
            }

            if (valid_x == 0) {
                // x is Inf. or 0

                // x is Infinity?
                if ((x & 0x7800000000000000L) == 0x7800000000000000L) {
                    // check if y is 0
                    if (coefficient_y == 0) {
                        // y==0, return NaN
//                        if ((z & 0x7e00000000000000L) != 0x7c00000000000000L)
//                            __set_status_flags(pfpsf, BID_INVALID_EXCEPTION);
                        return 0x7c00000000000000L;
                    }
                    // test if z is Inf of oposite sign
                    if (((z & 0x7c00000000000000L) == 0x7800000000000000L)
                        && (((x ^ y) ^ z) & 0x8000000000000000L) != 0) {
                        // return NaN
//                        __set_status_flags(pfpsf, BID_INVALID_EXCEPTION);
                        return 0x7c00000000000000L;
                    }
                    // otherwise return +/-Inf
                    return ((x ^ y) & 0x8000000000000000L) | 0x7800000000000000L;
                }
                // x is 0
                if (((y & 0x7800000000000000L) != 0x7800000000000000L)
                    && ((z & 0x7800000000000000L) != 0x7800000000000000L)) {

                    if (coefficient_z != 0) {
                        exponent_y = exponent_x - DECIMAL_EXPONENT_BIAS + exponent_y;

                        sign_z = z & 0x8000000000000000L;

                        if (exponent_y >= exponent_z)
                            return z;
                        return add_zero64(exponent_y, sign_z, exponent_z, coefficient_z/*, rnd_mode, pfpsf*/);
                    }
                }
            }
            if (valid_y == 0) {
                // y is Inf. or 0

                // y is Infinity?
                if ((y & 0x7800000000000000L) == 0x7800000000000000L) {
                    // check if x is 0
                    if (coefficient_x == 0) {
                        // y==0, return NaN
//                        __set_status_flags(pfpsf, BID_INVALID_EXCEPTION);
                        return 0x7c00000000000000L;
                    }
                    // test if z is Inf of oposite sign
                    if (((z & 0x7c00000000000000L) == 0x7800000000000000L)
                        && (((x ^ y) ^ z) & 0x8000000000000000L) != 0) {
//                        __set_status_flags(pfpsf, BID_INVALID_EXCEPTION);
                        // return NaN
                        return 0x7c00000000000000L;
                    }
                    // otherwise return +/-Inf
                    return ((x ^ y) & 0x8000000000000000L) | 0x7800000000000000L;
                }
                // y is 0
                if (((z & 0x7800000000000000L) != 0x7800000000000000L)) {

                    if (coefficient_z != 0) {
                        exponent_y += exponent_x - DECIMAL_EXPONENT_BIAS;

                        sign_z = z & 0x8000000000000000L;

                        if (exponent_y >= exponent_z)
                            return z;
                        return
                            add_zero64(exponent_y, sign_z, exponent_z, coefficient_z/*, rnd_mode, pfpsf*/);
                    }
                }
            }

            if (valid_z == 0) {
                // y is Inf. or 0

                // test if y is NaN/Inf
                if ((z & 0x7800000000000000L) == 0x7800000000000000L) {
                    return (coefficient_z & QUIET_MASK64);
                }
                // z is 0, return x*y
                if ((coefficient_x == 0) || (coefficient_y == 0)) {
                    //0+/-0
                    exponent_x += exponent_y - DECIMAL_EXPONENT_BIAS;
                    if (exponent_x > DECIMAL_MAX_EXPON_64)
                        exponent_x = DECIMAL_MAX_EXPON_64;
                    else if (exponent_x < 0)
                        exponent_x = 0;
                    if (exponent_x <= exponent_z)
                        res = ((long) exponent_x) << 53;
                    else
                        res = ((long) exponent_z) << 53;
                    if ((sign_x ^ sign_y) == sign_z)
                        res |= sign_z;
//                    else if (rnd_mode == BID_ROUNDING_DOWN)
//                        res |= 0x8000000000000000L;
                    return res;
                }
            }
        }

        /* get binary coefficients of x and y */

        //--- get number of bits in the coefficients of x and y ---
        // version 2 (original)
        long tempx_i = Double.doubleToRawLongBits((double) coefficient_x);
        bin_expon_cx = (int) ((tempx_i & MASK_BINARY_EXPONENT) >>> 52);

        final long tempy_i = Double.doubleToRawLongBits((double) coefficient_y);
        bin_expon_cy = (int) ((tempy_i & MASK_BINARY_EXPONENT) >>> 52);

        // magnitude estimate for coefficient_x*coefficient_y is
        //        2^(unbiased_bin_expon_cx + unbiased_bin_expon_cx)
        bin_expon_product = bin_expon_cx + bin_expon_cy;

        // check if coefficient_x*coefficient_y<2^(10*k+3)
        // equivalent to unbiased_bin_expon_cx + unbiased_bin_expon_cx < 10*k+1
        if (bin_expon_product < UPPER_EXPON_LIMIT + 2 * BINARY_EXPONENT_BIAS) {
            //  easy multiply
            C64 = coefficient_x * coefficient_y;
            final_exponent = exponent_x + exponent_y - DECIMAL_EXPONENT_BIAS;
            if ((final_exponent > 0) || (coefficient_z == 0)) {
                return bid_get_add64(sign_x ^ sign_y,
                    final_exponent, C64, sign_z, exponent_z, coefficient_z/*, rnd_mode, pfpsf*/);
            } else {
                P_w0 = C64;
                P_w1 = 0;
                extra_digits = 0;
            }
        } else {
            if (coefficient_z == 0) {
                return bid64_mul(x, y/*, rnd_mode*/);
            }
            // get 128-bit product: coefficient_x*coefficient_y

            //__mul_64x64_to_128(P, coefficient_x, coefficient_y);
            P_w1 = Mul64Impl.unsignedMultiplyHigh(coefficient_x, coefficient_y);
            P_w0 = coefficient_x * coefficient_y;


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
                    if ((UnsignedLong.isGreaterOrEqual(__P_w0, M)))
                        bp++;
                } else if (bp > 64) {
                    M <<= bp + 1 - 64;
                    if (((UnsignedLong.isGreater(__P_w1, M))) || (__P_w1 == M && __P_w0 != 0))
                        bp++;
                } else if (__P_w1 != 0)
                    bp++;
            }

            // get number of decimal digits in the product
            digits_p = bid_estimate_decimal_digits[bp];
            final int bid_power10_table_128_index = digits_p << 1;
            if (!(__unsigned_compare_gt_128(bid_power10_table_128_BID_UINT128[bid_power10_table_128_index],
                bid_power10_table_128_BID_UINT128[bid_power10_table_128_index + 1], P_w0, P_w1)))
                digits_p++;    // if bid_power10_table_128[digits_p] <= P

            // determine number of decimal digits to be rounded out
            extra_digits = digits_p - MAX_FORMAT_DIGITS;
            final_exponent =
                exponent_x + exponent_y + extra_digits - DECIMAL_EXPONENT_BIAS;
        }

        if (UnsignedInteger.isGreaterOrEqual(final_exponent, 3 * 256)) {
            if (final_exponent < 0) {
                //--- get number of bits in the coefficients of z  ---
                tempx_i = Double.doubleToRawLongBits((double) coefficient_z);
                bin_expon_cx = (int) ((tempx_i & MASK_BINARY_EXPONENT) >>> 52) - 0x3ff;
                // get number of decimal digits in the coeff_x
                digits_z = bid_estimate_decimal_digits[bin_expon_cx];
                if (UnsignedLong.isGreaterOrEqual(coefficient_z, bid_power10_table_128_BID_UINT128[(digits_z << 1) /*+ 0*/]))
                    digits_z++;
                // underflow
                if ((final_exponent + 16 < 0)
                    || (exponent_z + digits_z > 33 + final_exponent)) {
                    return BID_normalize(sign_z, exponent_z, coefficient_z, sign_x ^ sign_y, 1/*, rnd_mode, pfpsf*/);
                }

                ez = exponent_z + digits_z - 16;
                if (ez < 0)
                    ez = 0;
                scale_z = exponent_z - ez;
                coefficient_z *= bid_power10_table_128_BID_UINT128[(scale_z << 1) /*+ 0*/];
                ey = final_exponent - extra_digits;
                extra_digits = ez - ey;

                if (extra_digits > 17) {
                    CYh = __truncate(P_w0, P_w1, 16);
                    // get remainder
                    T = bid_power10_table_128_BID_UINT128[(16 << 1) /*+ 0*/];

                    //__mul_64x64_to_64(CY0L, CYh, T);
                    CY0L = CYh * T;

                    remainder_y = P_w0 - CY0L;

                    extra_digits -= 16;
                    P_w0 = CYh;
                    P_w1 = 0;
                } else
                    remainder_y = 0;

                // align coeff_x, CYh
                //__mul_64x64_to_128(CZ, coefficient_z, bid_power10_table_128_flat[(extra_digits << 1) /*+ 0*/]);
                {
                    final long __CY = bid_power10_table_128_BID_UINT128[(extra_digits << 1) /*+ 0*/];
                    CZ_w1 = Mul64Impl.unsignedMultiplyHigh(coefficient_z, __CY);
                    CZ_w0 = coefficient_z * __CY;
                }

                if (sign_z == (sign_y ^ sign_x)) {
                    //__add_128_128(CT, CZ, P);
                    {
                        long Q128_w0, Q128_w1;
                        Q128_w1 = CZ_w1 + P_w1;
                        Q128_w0 = P_w0 + CZ_w0;
                        if ((UnsignedLong.isLess(Q128_w0, P_w0)))
                            Q128_w1++;
                        CT_w1 = Q128_w1;
                        CT_w0 = Q128_w0;
                    }

                    final int bid_power10_table_128_index = (16 + extra_digits) << 1;
                    if (__unsigned_compare_ge_128(CT_w0, CT_w1,
                        bid_power10_table_128_BID_UINT128[bid_power10_table_128_index],
                        bid_power10_table_128_BID_UINT128[bid_power10_table_128_index + 1])) {
                        extra_digits++;
                        ez++;
                    }
                } else {
                    if (remainder_y != 0 && (__unsigned_compare_ge_128(CZ_w0, CZ_w1, P_w0, P_w1))) {
                        P_w0++;
                        if (P_w0 == 0)
                            P_w1++;
                    }

                    //__sub_128_128(CT, CZ, P);
                    {
                        CT_w1 = CZ_w1 - P_w1;
                        CT_w0 = CZ_w0 - P_w0;
                        if (UnsignedLong.isLess(CZ_w0, P_w0))
                            CT_w1--;
                    }

                    if ((/*(BID_SINT64)*/ CT_w1) < 0) {
                        sign_z = sign_y ^ sign_x;
                        CT_w0 = -CT_w0;
                        CT_w1 = -CT_w1;
                        if (CT_w0 != 0)
                            CT_w1--;
                    } else if ((CT_w1 | CT_w0) == 0)
                        sign_z = /*(rnd_mode != BID_ROUNDING_DOWN) ?*/ 0 /*: 0x8000000000000000L*/;
                    final int bid_power10_table_128_index = (15 + extra_digits) << 1;
                    if (ez != 0 && __unsigned_compare_gt_128(bid_power10_table_128_BID_UINT128[bid_power10_table_128_index],
                        bid_power10_table_128_BID_UINT128[bid_power10_table_128_index + 1], CT_w0, CT_w1)) {
                        extra_digits--;
                        ez--;
                    }
                }

//                uf_status = 0;
//                final int bid_power10_table_128_index = (extra_digits + 15) << 1;
//                if (ez == 0 && __unsigned_compare_gt_128(bid_power10_table_128_BID_UINT128[bid_power10_table_128_index],
//                    bid_power10_table_128_BID_UINT128[bid_power10_table_128_index + 1], CT_w0, CT_w1)) {
//                    uf_status = BID_UNDERFLOW_EXCEPTION;
//                }

                return __bid_full_round64_remainder(sign_z, ez - extra_digits, CT_w0, CT_w1,
                    extra_digits, remainder_y/*, rnd_mode, pfpsf, uf_status*/);

            } else {
                if ((sign_z == (sign_x ^ sign_y)) || (final_exponent > 3 * 256 + 15)) {
                    return fast_get_BID64_check_OF(sign_x ^ sign_y, final_exponent,
                        1000000000000000L/*, rnd_mode, pfpsf*/);
                }
            }
        }

        if (extra_digits > 0) {
            return bid_get_add128(sign_z, exponent_z, coefficient_z, sign_x ^ sign_y,
                final_exponent, P_w0, P_w1, extra_digits/*, rnd_mode, pfpsf*/);
        }
        // go to convert_format and exit
        else {
            C64 = P_w0;

            return bid_get_add64(sign_x ^ sign_y, exponent_x + exponent_y - DECIMAL_EXPONENT_BIAS, C64,
                sign_z, exponent_z, coefficient_z/*, rnd_mode, pfpsf*/);
        }
    }

    /**
     * 0*10^ey + cz*10^ez,   ey&lt;ez
     */
    static long /*BID_UINT64*/add_zero64(final int exponent_y, final long /*BID_UINT64*/ sign_z, final int exponent_z,
                                         long /*BID_UINT64*/ coefficient_z/*, int rounding_mode, FloatingPointStatusFlag fpsc*/) {
        int bin_expon, scale_k, scale_cz;
        int diff_expon;

        diff_expon = exponent_z - exponent_y;

        final long tempx_i = UnsignedLong.longToDoubleRawBits(coefficient_z);
        bin_expon = (int) (((tempx_i & MASK_BINARY_EXPONENT) >>> 52) - 0x3ff);
        scale_cz = bid_estimate_decimal_digits[bin_expon];
        if (UnsignedLong.isGreaterOrEqual(coefficient_z, bid_power10_table_128_BID_UINT128[(scale_cz << 1) /*+ 0*/]))
            scale_cz++;

        scale_k = 16 - scale_cz;
        if (diff_expon < scale_k)
            scale_k = diff_expon;
        coefficient_z *= bid_power10_table_128_BID_UINT128[(scale_k << 1) /*+ 0*/];

        return get_BID64(sign_z, exponent_z - scale_k, coefficient_z/*, rounding_mode, fpsc*/);
    }

    /**
     * bid_get_add64() is essentially the same as bid_add(), except that the arguments are unpacked
     */
    static long /*BID_UINT64*/ bid_get_add64(final long /*BID_UINT64*/ sign_x, final int exponent_x, final long /*BID_UINT64*/ coefficient_x,
                                             final long /*BID_UINT64*/ sign_y, final int exponent_y, final long /*BID_UINT64*/ coefficient_y
        /*, final int rounding_mode, final FloatingPointStatusFlag fpsc*/) {
        long /*BID_UINT128*/ CA_w0, CA_w1, CT_w0, CT_w1, CT_new_w0, CT_new_w1;
        long /*BID_UINT64*/ sign_a, sign_b, coefficient_a, coefficient_b, sign_s, sign_ab, rem_a;
        long /*BID_UINT64*/ saved_ca, saved_cb, C0_64, C64, remainder_h, T1, carry, tmp, C64_new;
        long /*int_double*/ tempx_i;
        int exponent_a, exponent_b, diff_dec_expon;
        int bin_expon_ca, extra_digits, amount, scale_k, scale_ca;
        /*unsigned*/
        int rmode/*, status*/;

        // sort arguments by exponent
        if (exponent_x <= exponent_y) {
            sign_a = sign_y;
            exponent_a = exponent_y;
            coefficient_a = coefficient_y;
            sign_b = sign_x;
            exponent_b = exponent_x;
            coefficient_b = coefficient_x;
        } else {
            sign_a = sign_x;
            exponent_a = exponent_x;
            coefficient_a = coefficient_x;
            sign_b = sign_y;
            exponent_b = exponent_y;
            coefficient_b = coefficient_y;
        }

        // exponent difference
        diff_dec_expon = exponent_a - exponent_b;

        /* get binary coefficients of x and y */

        //--- get number of bits in the coefficients of x and y ---

        tempx_i = UnsignedLong.longToDoubleRawBits(coefficient_a);
        bin_expon_ca = (int) (((tempx_i & MASK_BINARY_EXPONENT) >>> 52) - 0x3ff);

        if (coefficient_a == 0) {
            return get_BID64(sign_b, exponent_b, coefficient_b/*, rounding_mode, fpsc*/);
        }
        if (diff_dec_expon > MAX_FORMAT_DIGITS) {
            // normalize a to a 16-digit coefficient

            scale_ca = bid_estimate_decimal_digits[bin_expon_ca];
            if (UnsignedLong.isGreaterOrEqual(coefficient_a, bid_power10_table_128_BID_UINT128[(scale_ca << 1) /*+ 0*/]))
                scale_ca++;

            scale_k = 16 - scale_ca;

            coefficient_a *= bid_power10_table_128_BID_UINT128[(scale_k << 1) /*+ 0*/];

            diff_dec_expon -= scale_k;
            exponent_a -= scale_k;

            /* get binary coefficients of x and y */

            //--- get number of bits in the coefficients of x and y ---
            tempx_i = UnsignedLong.longToDoubleRawBits(coefficient_a);
            bin_expon_ca = (int) (((tempx_i & MASK_BINARY_EXPONENT) >>> 52) - 0x3ff);

            if (diff_dec_expon > MAX_FORMAT_DIGITS) {
//                if (coefficient_b != 0) {
//                    __set_status_flags(fpsc, BID_INEXACT_EXCEPTION);
//                }

//                if (((rounding_mode) & 3) != 0 && coefficient_b != 0)    // not BID_ROUNDING_TO_NEAREST
//                {
//                    switch (rounding_mode) {
//                        case BID_ROUNDING_DOWN:
//                            if (sign_b != 0) {
//                                coefficient_a -= ((((BID_SINT64) sign_a) >> 63) | 1);
//                                if (UnsignedLong.isLess(coefficient_a, 1000000000000000L)) {
//                                    exponent_a--;
//                                    coefficient_a = 9999999999999999L;
//                                } else if (UnsignedLong.isGreaterOrEqual(coefficient_a, 10000000000000000L)) {
//                                    exponent_a++;
//                                    coefficient_a = 1000000000000000L;
//                                }
//                            }
//                            break;
//                        case BID_ROUNDING_UP:
//                            if (sign_b == 0) {
//                                coefficient_a += ((((BID_SINT64) sign_a) >> 63) | 1);
//                                if (UnsignedLong.isLess(coefficient_a, 1000000000000000L) {
//                                    exponent_a--;
//                                    coefficient_a = 9999999999999999L;
//                                } else if (UnsignedLong.isGreaterOrEqual(coefficient_a, 10000000000000000L) {
//                                    exponent_a++;
//                                    coefficient_a = 1000000000000000L;
//                                }
//                            }
//                            break;
//                        default:    // RZ
//                            if (sign_a != sign_b) {
//                                coefficient_a--;
//                                if (UnsignedLong.isLess(coefficient_a, 1000000000000000L) {
//                                    exponent_a--;
//                                    coefficient_a = 9999999999999999L;
//                                }
//                            }
//                            break;
//                    }
//                } else
                // check special case here
                if ((coefficient_a == 1000000000000000L)
                    && (diff_dec_expon == MAX_FORMAT_DIGITS + 1)
                    && (sign_a ^ sign_b) != 0
                    && (UnsignedLong.isGreater(coefficient_b, 5000000000000000L))) {
                    coefficient_a = 9999999999999999L;
                    exponent_a--;
                }

                return get_BID64(sign_a, exponent_a, coefficient_a/*, rounding_mode, fpsc*/);
            }
        }
        // test whether coefficient_a*10^(exponent_a-exponent_b)  may exceed 2^62
        if (bin_expon_ca + bid_estimate_bin_expon[diff_dec_expon] < 60) {
            // coefficient_a*10^(exponent_a-exponent_b)<2^63

            // multiply by 10^(exponent_a-exponent_b)
            coefficient_a *= bid_power10_table_128_BID_UINT128[(diff_dec_expon << 1) /*+ 0*/];

            // sign mask
            sign_b = (/*(BID_SINT64)*/sign_b) >> 63;
            // apply sign to coeff. of b
            coefficient_b = (coefficient_b + sign_b) ^ sign_b;

            // apply sign to coefficient a
            sign_a = (/*(BID_SINT64)*/sign_a) >> 63;
            coefficient_a = (coefficient_a + sign_a) ^ sign_a;

            coefficient_a += coefficient_b;
            // get sign
            sign_s = (/*(BID_SINT64)*/coefficient_a) >> 63;
            coefficient_a = (coefficient_a + sign_s) ^ sign_s;
            sign_s &= 0x8000000000000000L;

            // coefficient_a < 10^16 ?
            if (UnsignedLong.isLess(coefficient_a, bid_power10_table_128_BID_UINT128[(MAX_FORMAT_DIGITS << 1) /*+ 0*/])) {
//                if (rounding_mode == BID_ROUNDING_DOWN && (coefficient_a == 0) && sign_a != sign_b)
//                    sign_s = 0x8000000000000000L;
                return get_BID64(sign_s, exponent_b, coefficient_a/*, rounding_mode, fpsc*/);
            }
            // otherwise rounding is necessary

            // already know coefficient_a<10^19
            // coefficient_a < 10^17 ?
            if (UnsignedLong.isLess(coefficient_a, bid_power10_table_128_BID_UINT128[(17 << 1) /*+ 0*/]))
                extra_digits = 1;
            else if (UnsignedLong.isLess(coefficient_a, bid_power10_table_128_BID_UINT128[(18 << 1) /*+ 0*/]))
                extra_digits = 2;
            else
                extra_digits = 3;

            rmode = /*rounding_mode*/BID_ROUNDING_TO_NEAREST;
//            if (sign_s != 0 && (rmode == BID_ROUNDING_DOWN || rmode == BID_ROUNDING_UP) /*(uint)(rmode - 1) < 2*/)
//                rmode = 3 - rmode;
            coefficient_a += bid_round_const_table[rmode][extra_digits];

            // get P*(2^M[extra_digits])/10^extra_digits
            //__mul_64x64_to_128(CT, coefficient_a, bid_reciprocals10_64[extra_digits]);
            {
                final long __CY = bid_reciprocals10_64[extra_digits];
                CT_w1 = Mul64Impl.unsignedMultiplyHigh(coefficient_a, __CY);
                CT_w0 = coefficient_a * __CY;
            }

            // now get P/10^extra_digits: shift C64 right by M[extra_digits]-128
            amount = bid_short_recip_scale[extra_digits];
            C64 = CT_w1 >>> amount;

        } else {
            // coefficient_a*10^(exponent_a-exponent_b) is large
            sign_s = sign_a;

            rmode = /*rounding_mode*/BID_ROUNDING_TO_NEAREST;
//            if (sign_s != 0 && (rmode == BID_ROUNDING_DOWN || rmode == BID_ROUNDING_UP) /*(uint)(rmode - 1) < 2*/)
//                rmode = 3 - rmode;

            // check whether we can take faster path
            scale_ca = bid_estimate_decimal_digits[bin_expon_ca];

            sign_ab = sign_a ^ sign_b;
            sign_ab = (/*(BID_SINT64)*/ sign_ab) >> 63;

            // T1 = 10^(16-diff_dec_expon)
            T1 = bid_power10_table_128_BID_UINT128[((16 - diff_dec_expon) << 1) /*+ 0*/];

            // get number of digits in coefficient_a
            //P_ca = bid_power10_table_128[scale_ca].w[0];
            //P_ca_m1 = bid_power10_table_128[scale_ca-1].w[0];
            if (UnsignedLong.isGreaterOrEqual(coefficient_a, bid_power10_table_128_BID_UINT128[(scale_ca << 1) /*+ 0*/])) {
                scale_ca++;
                //P_ca_m1 = P_ca;
                //P_ca = bid_power10_table_128[scale_ca].w[0];
            }

            scale_k = 16 - scale_ca;

            // apply sign
            //Ts = (T1 + sign_ab) ^ sign_ab;

            // test range of ca
            //X = coefficient_a + Ts - P_ca_m1;

            // addition
            saved_ca = coefficient_a - T1;
            coefficient_a = (long /*BID_SINT64*/) saved_ca *
                (long /*BID_SINT64*/) bid_power10_table_128_BID_UINT128[(scale_k << 1) /*+ 0*/];
            extra_digits = diff_dec_expon - scale_k;

            // apply sign
            saved_cb = (coefficient_b + sign_ab) ^ sign_ab;
            // add 10^16 and rounding constant
            coefficient_b = saved_cb + 10000000000000000L + bid_round_const_table[rmode][extra_digits];

            // get P*(2^M[extra_digits])/10^extra_digits
            //__mul_64x64_to_128(CT, coefficient_b, bid_reciprocals10_64[extra_digits]);
            {
                final long __CY = bid_reciprocals10_64[extra_digits];
                CT_w1 = Mul64Impl.unsignedMultiplyHigh(coefficient_b, __CY);
                CT_w0 = coefficient_b * __CY;
            }

            // now get P/10^extra_digits: shift C64 right by M[extra_digits]-128
            amount = bid_short_recip_scale[extra_digits];
            C0_64 = CT_w1 >>> amount;

            // result coefficient
            C64 = C0_64 + coefficient_a;
            // filter out difficult (corner) cases
            // the following test is equivalent to
            // ( (initial_coefficient_a + Ts) < P_ca &&
            //     (initial_coefficient_a + Ts) > P_ca_m1 ),
            // which ensures the number of digits in coefficient_a does not change
            // after adding (the appropriately scaled and rounded) coefficient_b
            if (UnsignedLong.isGreater(C64 - 1000000000000000L - 1, 9000000000000000L - 2)) {
                if (UnsignedLong.isGreaterOrEqual(C64, 10000000000000000L)) {
                    // result has more than 16 digits
                    if (scale_k == 0) {
                        // must divide coeff_a by 10
                        saved_ca = saved_ca + T1;

                        //__mul_64x64_to_128(CA, saved_ca, 0x3333333333333334L);
                        CA_w1 = Mul64Impl.unsignedMultiplyHigh(saved_ca, 0x3333333333333334L);
                        // CA_w0 = saved_ca * 0x3333333333333334L; // @optimization

                        //reciprocals10_64[1]);
                        coefficient_a = CA_w1 >>> 1;
                        rem_a =
                            saved_ca - (coefficient_a << 3) - (coefficient_a << 1);
                        coefficient_a = coefficient_a - T1;

                        saved_cb +=                            /*90000000000000000 */ +rem_a *
                            bid_power10_table_128_BID_UINT128[(diff_dec_expon << 1) /*+ 0*/];
                    } else
                        coefficient_a = (long /*BID_SINT64*/) (saved_ca - T1 - (T1 << 3)) *
                            (long /*BID_SINT64*/) bid_power10_table_128_BID_UINT128[((scale_k - 1) << 1) /*+ 0*/];

                    extra_digits++;
                    coefficient_b =
                        saved_cb + 100000000000000000L +
                            bid_round_const_table[rmode][extra_digits];

                    // get P*(2^M[extra_digits])/10^extra_digits
                    //__mul_64x64_to_128(CT, coefficient_b, bid_reciprocals10_64[extra_digits]);
                    {
                        final long __CY = bid_reciprocals10_64[extra_digits];
                        CT_w1 = Mul64Impl.unsignedMultiplyHigh(coefficient_b, __CY);
                        CT_w0 = coefficient_b * __CY;
                    }

                    // now get P/10^extra_digits: shift C64 right by M[extra_digits]-128
                    amount = bid_short_recip_scale[extra_digits];
                    C0_64 = CT_w1 >>> amount;

                    // result coefficient
                    C64 = C0_64 + coefficient_a;
                } else if (UnsignedLong.isLessOrEqual(C64, 1000000000000000L)) {
                    // less than 16 digits in result
                    coefficient_a = (long/*BID_SINT64*/) saved_ca *
                        (long/*BID_SINT64*/) bid_power10_table_128_BID_UINT128[((scale_k + 1) << 1) /*+ 0*/];
                    //extra_digits --;
                    exponent_b--;
                    coefficient_b =
                        (saved_cb << 3) + (saved_cb << 1) + 100000000000000000L +
                            bid_round_const_table[rmode][extra_digits];

                    // get P*(2^M[extra_digits])/10^extra_digits
                    //__mul_64x64_to_128(CT_new, coefficient_b, bid_reciprocals10_64[extra_digits]);
                    {
                        long __CY = bid_reciprocals10_64[extra_digits];
                        CT_new_w1 = Mul64Impl.unsignedMultiplyHigh(coefficient_b, __CY);
                        CT_new_w0 = coefficient_b * __CY;
                    }

                    // now get P/10^extra_digits: shift C64 right by M[extra_digits]-128
                    amount = bid_short_recip_scale[extra_digits];
                    C0_64 = CT_new_w1 >>> amount;

                    // result coefficient
                    C64_new = C0_64 + coefficient_a;
                    if (UnsignedLong.isLess(C64_new, 10000000000000000L)) {
                        C64 = C64_new;
                        CT_w0 = CT_new_w0;
                        CT_w1 = CT_new_w1;
                    } else
                        exponent_b++;
                }

            }

        }

//        if (rmode == 0)    //BID_ROUNDING_TO_NEAREST
        if ((C64 & 1) != 0) {
            // check whether fractional part of initial_P/10^extra_digits
            // is exactly .5
            // this is the same as fractional part of
            //      (initial_P + 0.5*10^extra_digits)/10^extra_digits is exactly zero

            // get remainder
            remainder_h = CT_w1 << (64 - amount);

            // test whether fractional part is 0
            if (remainder_h == 0 && UnsignedLong.isLess(CT_w0, bid_reciprocals10_64[extra_digits])) {
                C64--;
            }
        }

//        status = BID_INEXACT_EXCEPTION;

        // get remainder
        remainder_h = CT_w1 << (64 - amount);

//        switch (rmode) {
//            case BID_ROUNDING_TO_NEAREST:
//            case BID_ROUNDING_TIES_AWAY:
//                // test whether fractional part is 0
//                if ((remainder_h == 0x8000000000000000L) && UnsignedLong.isLess(CT_w0, bid_reciprocals10_64[extra_digits]))
//                    status = BID_EXACT_STATUS;
//                break;
//            case BID_ROUNDING_DOWN:
//            case BID_ROUNDING_TO_ZERO:
//                if (remainder_h == 0 && UnsignedLong.isLess(CT_w0, bid_reciprocals10_64[extra_digits]))
//                    status = BID_EXACT_STATUS;
//                break;
//            default:
//                // round up
//                //__add_carry_out(tmp, carry, CT_w0, bid_reciprocals10_64[extra_digits]);
//            {
//                tmp = CT_w0 + bid_reciprocals10_64[extra_digits];
//                carry = UnsignedLong.isLess(tmp, CT_w0) ? 1 : 0;
//            }
//
//            if (UnsignedLong.isGreaterOrEqual((remainder_h >>> (64 - amount)) + carry, (((long) 1) << amount)))
//                status = BID_EXACT_STATUS;
//            break;
//        }
//        __set_status_flags(fpsc, status);

        return get_BID64(sign_s, exponent_b + extra_digits, C64/*, rounding_mode, fpsc*/);
    }

    /**
     * get P/10^extra_digits
     * result fits in 64 bits
     */
    static long /*BID_UINT64*/__truncate(final long /*BID_UINT128*/ P_w0, final long P_w1, final int extra_digits) {// extra_digits <= 16
        long /*BID_UINT128*/ Q_high_w0, Q_high_w1, Q_low_w0, Q_low_w1, C128_w0, C128_w1;
        long /*BID_UINT64*/ C64;
        int amount;

        // get P*(2^M[extra_digits])/10^extra_digits
        //__mul_128x128_full(Q_high, Q_low, P, bid_reciprocals10_128[extra_digits]);
        {
            final long _A_w0 = P_w0;
            final long _A_w1 = P_w1;
            final long _B_w0 = bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) /*+ 0*/];
            final long _B_w1 = bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1];

            long _ALBL_w0, _ALBH_w0, _AHBL_w0, _AHBH_w0, _QM_w0, _QM2_w0;
            long _ALBL_w1, _ALBH_w1, _AHBL_w1, _AHBH_w1, _QM_w1, _QM2_w1;

            //__mul_64x64_to_128(ALBH, (A)_w0, (B)_w1);
            _ALBH_w1 = Mul64Impl.unsignedMultiplyHigh(_A_w0, _B_w1);
            _ALBH_w0 = _A_w0 * _B_w1;

            //__mul_64x64_to_128(AHBL, (B)_w0, (A)_w1);
            _AHBL_w1 = Mul64Impl.unsignedMultiplyHigh(_B_w0, _A_w1);
            _AHBL_w0 = _B_w0 * _A_w1;

            //__mul_64x64_to_128(ALBL, (A)_w0, (B)_w0);
            _ALBL_w1 = Mul64Impl.unsignedMultiplyHigh(_A_w0, _B_w0);
            _ALBL_w0 = _A_w0 * _B_w0;

            //__mul_64x64_to_128(AHBH, (A)_w1,(B)_w1);
            _AHBH_w1 = Mul64Impl.unsignedMultiplyHigh(_A_w1, _B_w1);
            _AHBH_w0 = _A_w1 * _B_w1;

            //__add_128_128(QM, ALBH, AHBL); // add 128-bit value to 128-bit assume no carry-out
            {
                long Q128_w0, Q128_w1;
                Q128_w1 = _ALBH_w1 + _AHBL_w1;
                Q128_w0 = _AHBL_w0 + _ALBH_w0;
                if ((UnsignedLong.isLess(Q128_w0, _AHBL_w0)))
                    Q128_w1++;
                _QM_w1 = Q128_w1;
                _QM_w0 = Q128_w0;
            }

            Q_low_w0 = _ALBL_w0;

            //__add_128_64(_QM2, QM, ALBL_w1); // add 64-bit value to 128-bit
            {
                long __R64H = _QM_w1;
                _QM2_w0 = _ALBL_w1 + _QM_w0;
                if ((UnsignedLong.isLess(_QM2_w0, _ALBL_w1)))
                    __R64H++;
                _QM2_w1 = __R64H;
            }

            //__add_128_64((_Qh), AHBH, QM2_w1); // add 64-bit value to 128-bit
            {
                long __R64H = _AHBH_w1;
                Q_high_w0 = _QM2_w1 + _AHBH_w0;
                if ((UnsignedLong.isLess(Q_high_w0, _QM2_w1)))
                    __R64H++;
                Q_high_w1 = __R64H;
            }

            Q_low_w1 = _QM2_w0;
        }


        // now get P/10^extra_digits: shift Q_high right by M[extra_digits]-128
        amount = bid_recip_scale[extra_digits];
        //__shr_128(C128, Q_high, amount);
        {
            C128_w0 = Q_high_w0 >>> (long) amount;
            C128_w0 |= Q_high_w1 << (64 - (long) amount);
            C128_w1 = Q_high_w1 >>> (long) amount;
        }

        C64 = C128_w0;

        return C64;
    }

    /**
     * If coefficient_z is less than 16 digits long, normalize to 16 digits
     */
    static long /*BID_UINT64*/ BID_normalize(
        final long /*BID_UINT64*/ sign_z, int exponent_z, long /*BID_UINT64*/ coefficient_z,
        final long /*BID_UINT64*/ round_dir, int round_flag/*, int rounding_mode, FloatingPointStatusFlag fpsc*/) {
        long /*BID_SINT64*/ D;
        long /*int_double*/ tempx_i;
        int digits_z, bin_expon, scale, rmode = 0;

//        rmode = rounding_mode;
//        if (sign_z != 0 && (rmode == BID_ROUNDING_DOWN || rmode == BID_ROUNDING_UP) /*(uint)(rmode - 1) < 2*/)
//            rmode = 3 - rmode;

        //--- get number of bits in the coefficients of x and y ---
        tempx_i = UnsignedLong.longToDoubleRawBits(coefficient_z);
        bin_expon = (int) (((tempx_i & MASK_BINARY_EXPONENT) >>> 52) - 0x3ff);
        // get number of decimal digits in the coeff_x
        digits_z = bid_estimate_decimal_digits[bin_expon];
        if (UnsignedLong.isGreaterOrEqual(coefficient_z, bid_power10_table_128_BID_UINT128[(digits_z << 1) /*+ 0*/]))
            digits_z++;

        scale = 16 - digits_z;
        exponent_z -= scale;
        if (exponent_z < 0) {
            scale += exponent_z;
            exponent_z = 0;
        }
        coefficient_z *= bid_power10_table_128_BID_UINT128[(scale << 1) /*+ 0*/];

//        if (round_flag != 0) {
//            __set_status_flags(fpsc, BID_INEXACT_EXCEPTION);
//            if (UnsignedLong.isLess(coefficient_z, 1000000000000000L))
//                __set_status_flags(fpsc, BID_UNDERFLOW_EXCEPTION);
//            else if ((coefficient_z == 1000000000000000L) && exponent_z == 0
//                && (/*(BID_SINT64)*/ (round_dir ^ sign_z) < 0) && round_flag)
//                __set_status_flags(fpsc, BID_UNDERFLOW_EXCEPTION);
//        }
//
//        if (round_flag != 0 && (rmode & 3) != 0) {
//            D = round_dir ^ sign_z;
//
//            if (rmode == BID_ROUNDING_UP) {
//                if (D >= 0)
//                    coefficient_z++;
//            } else {
//                if (D < 0)
//                    coefficient_z--;
//                if (UnsignedLong.isLess(coefficient_z, 1000000000000000L) && exponent_z != 0) {
//                    coefficient_z = 9999999999999999L;
//                    exponent_z--;
//                }
//            }
//        }

        return get_BID64(sign_z, exponent_z, coefficient_z/*, rounding_mode, fpsc*/);
    }

    /**
     * round 128-bit coefficient and return result in BID64 format
     * do not worry about midpoint cases
     */
    static long /*BID_UINT64*/
    __bid_simple_round64_sticky(
        final long /*BID_UINT64*/ sign, final int exponent, long /*BID_UINT128*/ P_w0, long P_w1,
        final int extra_digits/*, final int rounding_mode, FloatingPointStatusFlag fpsc*/) {
        long /*BID_UINT128*/ Q_high_w0, Q_high_w1, Q_low_w0, Q_low_w1, C128_w0, C128_w1;
        long /*BID_UINT64*/ C64;
        int amount, rmode;

        rmode = BID_ROUNDING_TO_NEAREST; // rounding_mode;
//        if (sign != 0 && (rmode == BID_ROUNDING_DOWN || rmode == BID_ROUNDING_UP) /*(uint)(rmode - 1) < 2*/)
//            rmode = 3 - rmode;

        //__add_128_64(P, P, bid_round_const_table[rmode][extra_digits]);
        {
            long /*BID_UINT64*/ B64 = bid_round_const_table[rmode][extra_digits];
            long /*BID_UINT64*/ R64H = P_w1;
            P_w0 = B64 + P_w0;
            if (UnsignedLong.isLess(P_w0, B64))
                R64H++;
            P_w1 = R64H;
        }

        // get P*(2^M[extra_digits])/10^extra_digits
        //__mul_128x128_full(Q_high, Q_low, P, bid_reciprocals10_128[extra_digits]);
        {
            final long _A_w0 = P_w0;
            final long _A_w1 = P_w1;
            final long _B_w0 = bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) /*+ 0*/];
            final long _B_w1 = bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1];

            long _ALBL_w0, _ALBH_w0, _AHBL_w0, _AHBH_w0, _QM_w0, _QM2_w0;
            long _ALBL_w1, _ALBH_w1, _AHBL_w1, _AHBH_w1, _QM_w1, _QM2_w1;

            //__mul_64x64_to_128(ALBH, (A)_w0, (B)_w1);
            _ALBH_w1 = Mul64Impl.unsignedMultiplyHigh(_A_w0, _B_w1);
            _ALBH_w0 = _A_w0 * _B_w1;

            //__mul_64x64_to_128(AHBL, (B)_w0, (A)_w1);
            _AHBL_w1 = Mul64Impl.unsignedMultiplyHigh(_B_w0, _A_w1);
            _AHBL_w0 = _B_w0 * _A_w1;

            //__mul_64x64_to_128(ALBL, (A)_w0, (B)_w0);
            _ALBL_w1 = Mul64Impl.unsignedMultiplyHigh(_A_w0, _B_w0);
            _ALBL_w0 = _A_w0 * _B_w0;

            //__mul_64x64_to_128(AHBH, (A)_w1,(B)_w1);
            _AHBH_w1 = Mul64Impl.unsignedMultiplyHigh(_A_w1, _B_w1);
            _AHBH_w0 = _A_w1 * _B_w1;

            //__add_128_128(QM, ALBH, AHBL); // add 128-bit value to 128-bit assume no carry-out
            {
                long Q128_w0, Q128_w1;
                Q128_w1 = _ALBH_w1 + _AHBL_w1;
                Q128_w0 = _AHBL_w0 + _ALBH_w0;
                if ((UnsignedLong.isLess(Q128_w0, _AHBL_w0)))
                    Q128_w1++;
                _QM_w1 = Q128_w1;
                _QM_w0 = Q128_w0;
            }

            Q_low_w0 = _ALBL_w0;

            //__add_128_64(_QM2, QM, ALBL_w1); // add 64-bit value to 128-bit
            {
                long __R64H;
                __R64H = _QM_w1;
                _QM2_w0 = _ALBL_w1 + _QM_w0;
                if ((UnsignedLong.isLess(_QM2_w0, _ALBL_w1)))
                    __R64H++;
                _QM2_w1 = __R64H;
            }

            //__add_128_64((_Qh), AHBH, QM2_w1); // add 64-bit value to 128-bit
            {
                long __R64H;
                __R64H = _AHBH_w1;
                Q_high_w0 = _QM2_w1 + _AHBH_w0;
                if ((UnsignedLong.isLess(Q_high_w0, _QM2_w1)))
                    __R64H++;
                Q_high_w1 = __R64H;
            }

            Q_low_w1 = _QM2_w0;
        }

        // now get P/10^extra_digits: shift Q_high right by M[extra_digits]-128
        amount = bid_recip_scale[extra_digits];
        //__shr_128(C128, Q_high, amount);
        {
            C128_w0 = Q_high_w0 >>> (long) amount;
            C128_w0 |= Q_high_w1 << (64 - (long) amount);
            C128_w1 = Q_high_w1 >>> (long) amount;
        }

        C64 = C128_w0;

//        __set_status_flags(fpsc, BID_INEXACT_EXCEPTION);

        return get_BID64(sign, exponent, C64/*, rounding_mode, fpsc*/);
    }

    /**
     * round 128-bit coefficient and return result in BID64 format
     */
    static long /*BID_UINT64*/ __bid_full_round64(
        long /*BID_UINT64*/ sign, int exponent, long /*BID_UINT128*/ P_w0, long P_w1,
        int extra_digits /*, int rounding_mode, FloatingPointStatusFlag fpsc*/) {
        long /*BID_UINT128*/ Q_high_w0, Q_high_w1, Q_low_w0, Q_low_w1, C128_w0, C128_w1, Stemp_w0, Stemp_w1;
        long /*BID_UINT64*/ remainder_h, C64, carry, CY;
        int amount, amount2, rmode;
//        int status = 0;

        if (exponent < 0) {
            if (exponent >= -16 && (extra_digits + exponent < 0)) {
                extra_digits = -exponent;
//                status = BID_UNDERFLOW_EXCEPTION;
            }
        }

        if (extra_digits > 0) {
            exponent += extra_digits;
            rmode = BID_ROUNDING_TO_NEAREST; // rounding_mode;
//            if (sign != 0 && (rmode == BID_ROUNDING_DOWN || rmode == BID_ROUNDING_UP) /*(uint)(rmode - 1) < 2*/)
//                rmode = 3 - rmode;

            //__add_128_128(P, P, bid_round_const_table_128[rmode][extra_digits]);
            {
                long /*BID_UINT128*/ B128_w0 = bid_round_const_table_128_BID_UINT128[rmode][(extra_digits << 1) /*+ 0*/];
                long /*BID_UINT128*/ B128_w1 = bid_round_const_table_128_BID_UINT128[rmode][(extra_digits << 1) + 1];
                long /*BID_UINT128*/ Q128_w1 = P_w1 + B128_w1;
                long /*BID_UINT128*/ Q128_w0 = B128_w0 + P_w0;
                if (UnsignedLong.isLess(Q128_w0, B128_w0))
                    Q128_w1++;
                P_w1 = Q128_w1;
                P_w0 = Q128_w0;
            }

            // get P*(2^M[extra_digits])/10^extra_digits
            //__mul_128x128_full(Q_high, Q_low, P, bid_reciprocals10_128[extra_digits]);
            {
                final long _A_w0 = P_w0;
                final long _A_w1 = P_w1;
                final long _B_w0 = bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) /*+ 0*/];
                final long _B_w1 = bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1];

                long _ALBL_w0, _ALBH_w0, _AHBL_w0, _AHBH_w0, _QM_w0, _QM2_w0;
                long _ALBL_w1, _ALBH_w1, _AHBL_w1, _AHBH_w1, _QM_w1, _QM2_w1;

                //__mul_64x64_to_128(ALBH, (A)_w0, (B)_w1);
                _ALBH_w1 = Mul64Impl.unsignedMultiplyHigh(_A_w0, _B_w1);
                _ALBH_w0 = _A_w0 * _B_w1;

                //__mul_64x64_to_128(AHBL, (B)_w0, (A)_w1);
                _AHBL_w1 = Mul64Impl.unsignedMultiplyHigh(_B_w0, _A_w1);
                _AHBL_w0 = _B_w0 * _A_w1;

                //__mul_64x64_to_128(ALBL, (A)_w0, (B)_w0);
                _ALBL_w1 = Mul64Impl.unsignedMultiplyHigh(_A_w0, _B_w0);
                _ALBL_w0 = _A_w0 * _B_w0;

                //__mul_64x64_to_128(AHBH, (A)_w1,(B)_w1);
                _AHBH_w1 = Mul64Impl.unsignedMultiplyHigh(_A_w1, _B_w1);
                _AHBH_w0 = _A_w1 * _B_w1;

                //__add_128_128(QM, ALBH, AHBL); // add 128-bit value to 128-bit assume no carry-out
                {
                    long Q128_w0, Q128_w1;
                    Q128_w1 = _ALBH_w1 + _AHBL_w1;
                    Q128_w0 = _AHBL_w0 + _ALBH_w0;
                    if ((UnsignedLong.isLess(Q128_w0, _AHBL_w0)))
                        Q128_w1++;
                    _QM_w1 = Q128_w1;
                    _QM_w0 = Q128_w0;
                }

                Q_low_w0 = _ALBL_w0;

                //__add_128_64(_QM2, QM, ALBL_w1); // add 64-bit value to 128-bit
                {
                    long __R64H;
                    __R64H = _QM_w1;
                    _QM2_w0 = _ALBL_w1 + _QM_w0;
                    if ((UnsignedLong.isLess(_QM2_w0, _ALBL_w1)))
                        __R64H++;
                    _QM2_w1 = __R64H;
                }

                //__add_128_64((_Qh), AHBH, QM2_w1); // add 64-bit value to 128-bit
                {
                    long __R64H;
                    __R64H = _AHBH_w1;
                    Q_high_w0 = _QM2_w1 + _AHBH_w0;
                    if ((UnsignedLong.isLess(Q_high_w0, _QM2_w1)))
                        __R64H++;
                    Q_high_w1 = __R64H;
                }

                Q_low_w1 = _QM2_w0;
            }

            // now get P/10^extra_digits: shift Q_high right by M[extra_digits]-128
            amount = bid_recip_scale[extra_digits];

            //__shr_128_long(C128, Q_high, amount);
            {
                if (amount < 64) {
                    C128_w0 = Q_high_w0 >>> amount;
                    C128_w0 |= Q_high_w1 << (64 - amount);
                    C128_w1 = Q_high_w1 >>> amount;
                } else {
                    C128_w0 = Q_high_w1 >>> ((amount) - 64);
                    C128_w1 = 0;
                }
            }

            C64 = C128_w0;

//            if (rmode == 0)    //BID_ROUNDING_TO_NEAREST
            if ((C64 & 1) != 0) {
                // check whether fractional part of initial_P/10^extra_digits
                // is exactly .5

                // get remainder
                amount2 = 64 - amount;
                remainder_h = 0;
                remainder_h--;
                remainder_h >>>= amount2;
                remainder_h = remainder_h & Q_high_w0;

                if (remainder_h == 0
                    && (UnsignedLong.isLess(Q_low_w1, bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1])
                    || (Q_low_w1 == bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1]
                    && UnsignedLong.isLess(Q_low_w0, bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) /*+ 0*/])))) {
                    C64--;
                }
            }

//            status |= BID_INEXACT_EXCEPTION;

            // get remainder
            remainder_h = Q_high_w0 << (64 - amount);

//            switch (rmode) {
//                case BID_ROUNDING_TO_NEAREST:
//                case BID_ROUNDING_TIES_AWAY:
//                    // test whether fractional part is 0
//                    if (remainder_h == 0x8000000000000000L
//                        && (UnsignedLong.isLess(Q_low_w1, bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1])
//                        || (Q_low_w1 == bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1]
//                        && UnsignedLong.isLess(Q_low_w0, bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) /*+ 0*/]))))
//                        status = BID_EXACT_STATUS;
//                    break;
//                case BID_ROUNDING_DOWN:
//                case BID_ROUNDING_TO_ZERO:
//                    if (remainder_h == 0
//                        && (UnsignedLong.isLess(Q_low_w1, bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1])
//                        || (Q_low_w1 == bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1]
//                        && UnsignedLong.isLess(Q_low_w0, bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) /*+ 0*/]))))
//                        status = BID_EXACT_STATUS;
//                    break;
//                default:
//                    // round up
//                    //__add_carry_out(Stemp_w0, CY, Q_low_w0, bid_reciprocals10_128[extra_digits].w[0]);
//                {
//                    Stemp_w0 = Q_low_w0 + bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) /*+  0*/];
//                    CY = UnsignedLong.isLess(Stemp_w0, Q_low_w0) ? 1 : 0;
//                }
//
//                //__add_carry_in_out(Stemp_w1, carry, Q_low_w1, bid_reciprocals10_128[extra_digits].w[1], CY);
//                {
//                    long /*BID_UINT64*/ X1 = Q_low_w1 + CY;
//                    Stemp_w1 = X1 + bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1];
//                    carry = (UnsignedLong.isLess(Stemp_w1, X1) || UnsignedLong.isLess(X1, CY)) ? 1 : 0;
//                }
//
//                if (UnsignedLong.isGreaterOrEqual((remainder_h >>> (64 - amount)) + carry, (((long /*BID_UINT64*/) 1) << amount)))
//                    status = BID_EXACT_STATUS;
//            }
//
//            __set_status_flags(fpsc, status);

        } else {
            C64 = P_w0;
            if (C64 == 0) {
                sign = 0;
//                if (rounding_mode == BID_ROUNDING_DOWN)
//                    sign = 0x8000000000000000L;
            }
        }
        return get_BID64(sign, exponent, C64/*, rounding_mode, fpsc*/);
    }

    /**
     * round 192-bit coefficient (P, remainder_P) and return result in BID64 format
     * the lowest 64 bits (remainder_P) are used for midpoint checking only
     */
    static long /*BID_UINT64*/ __bid_full_round64_remainder(
        final long /*BID_UINT64*/ sign, final int exponent, long /*BID_UINT128*/ P_w0, long P_w1,
        final int extra_digits, final long /*BID_UINT64*/ remainder_P
        /*, final int rounding_mode, FloatingPointStatusFlag fpsc, final /*unsigned* / int uf_status*/) {

        long /*BID_UINT128*/ Q_high_w0, Q_high_w1, Q_low_w0, Q_low_w1, C128_w0, C128_w1, Stemp_w0, Stemp_w1;
        long /*BID_UINT64*/ remainder_h, C64, carry, CY;
        int amount, amount2;
//        int status = uf_status;

        int rmode = /*rounding_mode*/ BID_ROUNDING_TO_NEAREST;
//        if (sign != 0 && (rmode == BID_ROUNDING_DOWN || rmode == BID_ROUNDING_UP) /*(uint)(rmode - 1) < 2*/)
//            rmode = 3 - rmode;
//        if (rmode == BID_ROUNDING_UP && remainder_P != 0) {
//            P_w0++;
//            if (P_w0 == 0)
//                P_w1++;
//        }

        if (extra_digits != 0) {
            //__add_128_64(P, P, bid_round_const_table[rmode][extra_digits]);
            {
                final long B64 = bid_round_const_table[rmode][extra_digits];
                long /*BID_UINT64*/ R64H = P_w1;
                P_w0 = B64 + P_w0;
                if (UnsignedLong.isLess(P_w0, B64))
                    R64H++;
                P_w1 = R64H;
            }

            // get P*(2^M[extra_digits])/10^extra_digits
            //__mul_128x128_full(Q_high, Q_low, P, bid_reciprocals10_128[extra_digits]);
            {
                final long _A_w0 = P_w0;
                final long _A_w1 = P_w1;
                final long _B_w0 = bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) /*+ 0*/];
                final long _B_w1 = bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1];

                long _ALBL_w0, _ALBH_w0, _AHBL_w0, _AHBH_w0, _QM_w0, _QM2_w0;
                long _ALBL_w1, _ALBH_w1, _AHBL_w1, _AHBH_w1, _QM_w1, _QM2_w1;

                //__mul_64x64_to_128(ALBH, (A)_w0, (B)_w1);
                _ALBH_w1 = Mul64Impl.unsignedMultiplyHigh(_A_w0, _B_w1);
                _ALBH_w0 = _A_w0 * _B_w1;

                //__mul_64x64_to_128(AHBL, (B)_w0, (A)_w1);
                _AHBL_w1 = Mul64Impl.unsignedMultiplyHigh(_B_w0, _A_w1);
                _AHBL_w0 = _B_w0 * _A_w1;

                //__mul_64x64_to_128(ALBL, (A)_w0, (B)_w0);
                _ALBL_w1 = Mul64Impl.unsignedMultiplyHigh(_A_w0, _B_w0);
                _ALBL_w0 = _A_w0 * _B_w0;

                //__mul_64x64_to_128(AHBH, (A)_w1,(B)_w1);
                _AHBH_w1 = Mul64Impl.unsignedMultiplyHigh(_A_w1, _B_w1);
                _AHBH_w0 = _A_w1 * _B_w1;

                //__add_128_128(QM, ALBH, AHBL); // add 128-bit value to 128-bit assume no carry-out
                {
                    _QM_w1 = _ALBH_w1 + _AHBL_w1;
                    _QM_w0 = _AHBL_w0 + _ALBH_w0;
                    if ((UnsignedLong.isLess(_QM_w0, _AHBL_w0)))
                        _QM_w1++;
                }

                Q_low_w0 = _ALBL_w0;

                //__add_128_64(_QM2, QM, ALBL_w1); // add 64-bit value to 128-bit
                {
                    _QM2_w1 = _QM_w1;
                    _QM2_w0 = _ALBL_w1 + _QM_w0;
                    if ((UnsignedLong.isLess(_QM2_w0, _ALBL_w1)))
                        _QM2_w1++;
                }

                //__add_128_64((_Qh), AHBH, QM2_w1); // add 64-bit value to 128-bit
                {
                    Q_high_w1 = _AHBH_w1;
                    Q_high_w0 = _QM2_w1 + _AHBH_w0;
                    if ((UnsignedLong.isLess(Q_high_w0, _QM2_w1)))
                        Q_high_w1++;
                }

                Q_low_w1 = _QM2_w0;
            }


            // now get P/10^extra_digits: shift Q_high right by M[extra_digits]-128
            amount = bid_recip_scale[extra_digits];
            //__shr_128(C128, Q_high, amount);
            {
                C128_w0 = Q_high_w0 >>> (long) amount;
                C128_w0 |= Q_high_w1 << (64 - (long) amount);
                C128_w1 = Q_high_w1 >>> (long) amount;
            }

            C64 = C128_w0;

//            if (rmode == 0)    //BID_ROUNDING_TO_NEAREST
            if (remainder_P == 0 && (C64 & 1) != 0) {
                // check whether fractional part of initial_P/10^extra_digits
                // is exactly .5

                // get remainder
                amount2 = 64 - amount;
                remainder_h = 0;
                remainder_h--;
                remainder_h >>>= amount2;
                remainder_h = remainder_h & Q_high_w0;

                if (remainder_h == 0
                    && (UnsignedLong.isLess(Q_low_w1, bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1])
                    || (Q_low_w1 == bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1]
                    && UnsignedLong.isLess(Q_low_w0, bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) /*+ 0*/])))) {
                    C64--;
                }
            }

//            status |= BID_INEXACT_EXCEPTION;
//
//            if (remainder_P == 0) {
//                // get remainder
//                remainder_h = Q_high_w0 << (64 - amount);
//
//                switch (rmode) {
//                    case BID_ROUNDING_TO_NEAREST:
//                    case BID_ROUNDING_TIES_AWAY:
//                        // test whether fractional part is 0
//                        if (remainder_h == 0x8000000000000000L
//                            && (UnsignedLong.isLess(Q_low_w1, bid_reciprocals10_128_flat[(extra_digits << 1) + 1])
//                            || (Q_low_w1 == bid_reciprocals10_128_flat[(extra_digits << 1) + 1]
//                            && UnsignedLong.isLess(Q_low_w0, bid_reciprocals10_128_flat[(extra_digits << 1) /*+ 0*/]))))
//                            status = BID_EXACT_STATUS;
//                        break;
//                    case BID_ROUNDING_DOWN:
//                    case BID_ROUNDING_TO_ZERO:
//                        if (remainder_h == 0
//                            && (UnsignedLong.isLess(Q_low_w1, bid_reciprocals10_128_flat[(extra_digits << 1) + 1])
//                            || (Q_low_w1 == bid_reciprocals10_128_flat[(extra_digits << 1) + 1]
//                            && UnsignedLong.isLess(Q_low_w0, bid_reciprocals10_128_flat[(extra_digits << 1) /*+ 0*/]))))
//                            status = BID_EXACT_STATUS;
//                        break;
//                    default:
//                        // round up
//                        //__add_carry_out(Stemp_w0, CY, Q_low_w0, bid_reciprocals10_128_flat[(extra_digits << 1) /*+  0*/]);
//                    {
//                        Stemp_w0 = Q_low_w0 + bid_reciprocals10_128_flat[(extra_digits << 1) /*+  0*/];
//                        CY = UnsignedLong.isLess(Stemp_w0, Q_low_w0) ? 1 : 0;
//                    }
//
//                        //__add_carry_in_out(Stemp_w1, carry, Q_low_w1, bid_reciprocals10_128_flat[(extra_digits << 1) + 1], CY);
//                    {
//                        long /*BID_UINT64*/ X1 = Q_low_w1 + CY;
//                        Stemp_w1 = X1 + bid_reciprocals10_128_flat[(extra_digits << 1) + 1];
//                        carry = (UnsignedLong.isLess(Stemp_w1, X1) || UnsignedLong.isLess(X1, CY)) ? 1 : 0;
//                    }
//
//                    if (UnsignedLong.isGreaterOrEqual((remainder_h >>> (64 - amount)) + carry, ((long /*BID_UINT64*/) 1) << amount))
//                        status = BID_EXACT_STATUS;
//                }
//            }
//            __set_status_flags(fpsc, status);

        } else {
            C64 = P_w0;
//            if (remainder_P != 0) {
//                __set_status_flags(fpsc, uf_status | BID_INEXACT_EXCEPTION);
//            }
        }

        return get_BID64(sign, exponent + extra_digits, C64/*, rounding_mode, fpsc*/);
    }

    /**
     * add 64-bit coefficient to 128-bit coefficient, return result in BID64 format
     */
    static long /*BID_UINT64*/ bid_get_add128(
        long /*BID_UINT64*/ sign_x, int exponent_x, long /*BID_UINT64*/ coefficient_x,
        long /*BID_UINT64*/ sign_y, int final_exponent_y, long /*BID_UINT128*/ CY_w0, long CY_w1,
        int extra_digits/*, final int rounding_mode, FloatingPointStatusFlag fpsc*/) {

        long /*BID_UINT128*/ CY_L_w0, CY_L_w1, CX_w0, CX_w1, FS_w0, FS_w1, F_w0, F_w1, CT_w0, CT_w1, ST_w0, ST_w1, T2_w0, T2_w1;
        long /*BID_UINT64*/ CYh, CY0L, T, S, coefficient_y, remainder_y;
        long /*BID_SINT64*/ D = 0;
        long /*int_double*/ tempx_i;
        int diff_dec_expon, extra_digits2, exponent_y;
//        int status;
        int extra_dx, diff_dec2, bin_expon_cx, digits_x, rmode;
        int table_index;

        final int rounding_mode = BID_ROUNDING_TO_NEAREST;

        // CY has more than 16 decimal digits

        exponent_y = final_exponent_y - extra_digits;

        if (exponent_x > exponent_y) {
            // normalize x
            //--- get number of bits in the coefficients of x and y ---
            tempx_i = UnsignedLong.longToDoubleRawBits(coefficient_x);
            bin_expon_cx = (int) (((tempx_i & MASK_BINARY_EXPONENT) >>> 52) - 0x3ff);
            // get number of decimal digits in the coeff_x
            digits_x = bid_estimate_decimal_digits[bin_expon_cx];
            if (UnsignedLong.isGreaterOrEqual(coefficient_x, bid_power10_table_128_BID_UINT128[(digits_x << 1) /*+ 0*/]))
                digits_x++;

            extra_dx = 16 - digits_x;
            coefficient_x *= bid_power10_table_128_BID_UINT128[(extra_dx << 1) /*+ 0*/];
            if ((sign_x ^ sign_y) != 0 && (coefficient_x == 1000000000000000L)) {
                extra_dx++;
                coefficient_x = 10000000000000000L;
            }
            exponent_x -= extra_dx;

            if (exponent_x > exponent_y) {

                // exponent_x > exponent_y
                diff_dec_expon = exponent_x - exponent_y;

                if (exponent_x <= final_exponent_y + 1) {
                    //__mul_64x64_to_128(CX, coefficient_x, bid_power10_table_128_flat[(diff_dec_expon << 1) /*+ 0*/]);
                    {
                        final long /*BID_UINT64*/ __CY = bid_power10_table_128_BID_UINT128[(diff_dec_expon << 1) /*+ 0*/];
                        long /*BID_UINT64*/ CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
                        CXH = coefficient_x >>> 32;
                        CXL = LONG_LOW_PART & coefficient_x;
                        CYH = __CY >>> 32;
                        CYL = LONG_LOW_PART & __CY;
                        PM = CXH * CYL;
                        PH = CXH * CYH;
                        PL = CXL * CYL;
                        PM2 = CXL * CYH;
                        PH += PM >>> 32;
                        PM = (LONG_LOW_PART & PM) + PM2 + (PL >>> 32);
                        CX_w1 = PH + (PM >>> 32);
                        CX_w0 = (PM << 32) + (LONG_LOW_PART & PL);
                    }

                    if (sign_x == sign_y) {
                        //__add_128_128(CT, CY, CX);
                        {
                            CT_w1 = CY_w1 + CX_w1;
                            CT_w0 = CX_w0 + CY_w0;
                            if ((UnsignedLong.isLess(CT_w0, CX_w0)))
                                CT_w1++;
                        }

                        if ((exponent_x > final_exponent_y) /*&& (final_exponent_y>0) */)
                            extra_digits++;
                        table_index = (16 + extra_digits) << 1;
                        if (__unsigned_compare_ge_128(CT_w0, CT_w1,
                            bid_power10_table_128_BID_UINT128[table_index],
                            bid_power10_table_128_BID_UINT128[table_index + 1]))
                            extra_digits++;
                    } else {
                        //__sub_128_128(CT, CY, CX);
                        {
                            CT_w1 = CY_w1 - CX_w1;
                            CT_w0 = CY_w0 - CX_w0;
                            if (UnsignedLong.isLess(CY_w0, CX_w0))
                                CT_w1--;
                        }

                        if ((/*(BID_SINT64)*/ CT_w1) < 0) {
                            CT_w0 = -CT_w0;
                            CT_w1 = -CT_w1;
                            if (CT_w0 != 0)
                                CT_w1--;
                            sign_y = sign_x;
                        } else if ((CT_w1 | CT_w0) == 0) {
                            sign_y = (rounding_mode != BID_ROUNDING_DOWN) ? 0 : 0x8000000000000000L;
                        }
                        if ((exponent_x + 1 >= final_exponent_y) /*&& (final_exponent_y>=0) */) {
                            extra_digits = __get_dec_digits64(CT_w0, CT_w1) - 16;
                            if (extra_digits <= 0) {
//                                if (CT_w0 == 0 && rounding_mode == BID_ROUNDING_DOWN)
//                                    sign_y = 0x8000000000000000L;
                                return get_BID64(sign_y, exponent_y, CT_w0/*, rounding_mode, fpsc*/);
                            }
                        } else {
                            table_index = (15 + extra_digits) << 1;
                            if (__unsigned_compare_gt_128(bid_power10_table_128_BID_UINT128[table_index],
                                bid_power10_table_128_BID_UINT128[table_index + 1], CT_w0, CT_w1))
                                extra_digits--;
                        }
                    }

                    return __bid_full_round64(sign_y, exponent_y, CT_w0, CT_w1, extra_digits/*, rounding_mode, fpsc*/);
                }
                // diff_dec2+extra_digits is the number of digits to eliminate from
                //                           argument CY
                diff_dec2 = exponent_x - final_exponent_y;

                if (diff_dec2 >= 17) {
//                    if (((rounding_mode) & 3) != 0) {
//                        switch (rounding_mode) {
//                            case BID_ROUNDING_UP:
//                                if (sign_y == 0) {
//                                    D = (/*(BID_SINT64)*/ (sign_x ^ sign_y)) >> 63;
//                                    D = D + D + 1;
//                                    coefficient_x += D;
//                                }
//                                break;
//                            case BID_ROUNDING_DOWN:
//                                if (sign_y != 0) {
//                                    D = (/*(BID_SINT64)*/ (sign_x ^ sign_y)) >> 63;
//                                    D = D + D + 1;
//                                    coefficient_x += D;
//                                }
//                                break;
//                            case BID_ROUNDING_TO_ZERO:
//                                if (sign_y != sign_x) {
//                                    D = -1;
//                                    coefficient_x += D;
//                                }
//                                break;
//                        }
//                        if (UnsignedLong.isLess(coefficient_x, 1000000000000000L)) {
//                            coefficient_x -= D;
//                            coefficient_x =
//                                D + (coefficient_x << 1) + (coefficient_x << 3);
//                            exponent_x--;
//                        }
//                    }
//                    if ((CY_w1 | CY_w0) != 0)
//                        __set_status_flags(fpsc, BID_INEXACT_EXCEPTION);
                    return get_BID64(sign_x, exponent_x, coefficient_x/*, rounding_mode, fpsc*/);
                }
                // here exponent_x <= 16+final_exponent_y

                // truncate CY to 16 dec. digits
                CYh = __truncate(CY_w0, CY_w1, extra_digits);

                // get remainder
                T = bid_power10_table_128_BID_UINT128[(extra_digits << 1) /*+ 0*/];

                //__mul_64x64_to_64(CY0L, CYh, T);
                CY0L = CYh * T;

                remainder_y = CY_w0 - CY0L;

                // align coeff_x, CYh
                //__mul_64x64_to_128(CX, coefficient_x, bid_power10_table_128_flat[(diff_dec2 << 1) /*+ 0*/]);
                {
                    long /*BID_UINT64*/ __CY = bid_power10_table_128_BID_UINT128[(diff_dec2 << 1) /*+ 0*/];
                    long /*BID_UINT64*/ CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
                    CXH = coefficient_x >>> 32;
                    CXL = LONG_LOW_PART & coefficient_x;
                    CYH = __CY >>> 32;
                    CYL = LONG_LOW_PART & __CY;
                    PM = CXH * CYL;
                    PH = CXH * CYH;
                    PL = CXL * CYL;
                    PM2 = CXL * CYH;
                    PH += PM >>> 32;
                    PM = (LONG_LOW_PART & PM) + PM2 + (PL >>> 32);
                    CX_w1 = PH + (PM >>> 32);
                    CX_w0 = (PM << 32) + (LONG_LOW_PART & PL);
                }


                if (sign_x == sign_y) {
                    //__add_128_64(CT, CX, CYh);
                    {
                        CT_w1 = CX_w1;
                        CT_w0 = CYh + CX_w0;
                        if (UnsignedLong.isLess(CT_w0, CYh))
                            CT_w1++;
                    }

                    table_index = (16 + diff_dec2) << 1;
                    if (__unsigned_compare_ge_128(CT_w0, CT_w1, bid_power10_table_128_BID_UINT128[table_index], bid_power10_table_128_BID_UINT128[table_index + 1]))
                        diff_dec2++;
                } else {
                    if (remainder_y != 0)
                        CYh++;

                    //__sub_128_64(CT, CX, CYh);
                    {
                        CT_w1 = CX_w1;
                        CT_w0 = CX_w0 - CYh;
                        if (UnsignedLong.isLess(CX_w0, CYh))
                            CT_w1--;
                    }

                    table_index = (15 + diff_dec2) << 1;
                    if (__unsigned_compare_gt_128(bid_power10_table_128_BID_UINT128[table_index], bid_power10_table_128_BID_UINT128[table_index + 1], CT_w0, CT_w1))
                        diff_dec2--;
                }

                return __bid_full_round64_remainder(sign_x, final_exponent_y, CT_w0, CT_w1,
                    diff_dec2, remainder_y/*, rounding_mode, fpsc, 0*/);
            }
        }
        // Here (exponent_x <= exponent_y)
        {
            diff_dec_expon = exponent_y - exponent_x;

            if (diff_dec_expon > MAX_FORMAT_DIGITS) {
                rmode = rounding_mode;

                if ((sign_x ^ sign_y) != 0) {
                    if (CY_w0 == 0)
                        CY_w1--;
                    CY_w0--;
                    table_index = (15 + extra_digits) << 1;
                    if (__unsigned_compare_gt_128(bid_power10_table_128_BID_UINT128[table_index],
                        bid_power10_table_128_BID_UINT128[table_index + 1], CY_w0, CY_w1)) {
                        if ((rmode & 3) != 0) {
                            extra_digits--;
                            final_exponent_y--;
                        } else {
                            CY_w0 = 1000000000000000L;
                            CY_w1 = 0;
                            extra_digits = 0;
                        }
                    }
                }

                //__scale128_10(CY, CY);
                {
                    long /*BID_UINT128*/ _TMP2_w0, _TMP2_w1, _TMP8_w0, _TMP8_w1;
                    _TMP2_w1 = (CY_w1 << 1) | (CY_w0 >>> 63);
                    _TMP2_w0 = CY_w0 << 1;
                    _TMP8_w1 = (CY_w1 << 3) | (CY_w0 >>> 61);
                    _TMP8_w0 = CY_w0 << 3;

                    //__add_128_128(CY, _TMP2, _TMP8);
                    {
                        CY_w1 = _TMP2_w1 + _TMP8_w1;
                        CY_w0 = _TMP8_w0 + _TMP2_w0;
                        if (UnsignedLong.isLess(CY_w0, _TMP8_w0))
                            CY_w1++;
                    }
                }

                extra_digits++;
                CY_w0 |= 1;

                return __bid_simple_round64_sticky(sign_y, final_exponent_y, CY_w0, CY_w1, extra_digits/*, rmode, fpsc*/);
            }
            // apply sign to coeff_x
            sign_x ^= sign_y;
            sign_x = (/*(BID_SINT64)*/ sign_x) >> 63;
            CX_w0 = (coefficient_x + sign_x) ^ sign_x;
            CX_w1 = sign_x;

            // check whether CY (rounded to 16 digits) and CX have
            //                     any digits in the same position
            diff_dec2 = final_exponent_y - exponent_x;

            if (diff_dec2 <= 17) {
                // align CY to 10^ex
                S = bid_power10_table_128_BID_UINT128[(diff_dec_expon << 1) /*+ 0*/];

                //__mul_64x128_short(CY_L, S, CY);
                {
                    //__mul_64x64_to_64(ALBH_L, S, CY_w1);
                    final long /*BID_UINT64*/ ALBH_L = S * CY_w1;

                    //__mul_64x64_to_128(CY_L, S, CY_w0);
                    CY_L_w1 = Mul64Impl.unsignedMultiplyHigh(S, CY_w0);
                    CY_L_w0 = S * CY_w0;

                    CY_L_w1 += ALBH_L;
                }

                //__add_128_128(ST, CY_L, CX);
                {
                    ST_w1 = CY_L_w1 + CX_w1;
                    ST_w0 = CX_w0 + CY_L_w0;
                    if ((UnsignedLong.isLess(ST_w0, CX_w0)))
                        ST_w1++;
                }

                extra_digits2 = __get_dec_digits64(ST_w0, ST_w1) - 16;
                return __bid_full_round64(sign_y, exponent_x, ST_w0, ST_w1, extra_digits2/*, rounding_mode, fpsc*/);
            }
            // truncate CY to 16 dec. digits
            CYh = __truncate(CY_w0, CY_w1, extra_digits);

            // get remainder
            T = bid_power10_table_128_BID_UINT128[(extra_digits << 1) /*+ 0*/];

            //__mul_64x64_to_64(CY0L, CYh, T);
            CY0L = CYh * T;

            coefficient_y = CY_w0 - CY0L;
            // add rounding constant
            rmode = rounding_mode;
//            if (sign_y != 0 && (rmode == BID_ROUNDING_DOWN || rmode == BID_ROUNDING_UP) /*(uint)(rmode - 1) < 2*/)
//                rmode = 3 - rmode;
//            if ((rmode & 3) == 0)    //BID_ROUNDING_TO_NEAREST
            {
                coefficient_y += bid_round_const_table[rmode][extra_digits];
            }
            // align coefficient_y,  coefficient_x
            S = bid_power10_table_128_BID_UINT128[(diff_dec_expon << 1) /*+ 0*/];

            //__mul_64x64_to_128(F, coefficient_y, S);
            {
                long /*BID_UINT64*/ CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
                CXH = coefficient_y >>> 32;
                CXL = LONG_LOW_PART & coefficient_y;
                CYH = S >>> 32;
                CYL = LONG_LOW_PART & S;
                PM = CXH * CYL;
                PH = CXH * CYH;
                PL = CXL * CYL;
                PM2 = CXL * CYH;
                PH += PM >>> 32;
                PM = (LONG_LOW_PART & PM) + PM2 + (PL >>> 32);
                F_w1 = PH + (PM >>> 32);
                F_w0 = (PM << 32) + (LONG_LOW_PART & PL);
            }

            // fraction
            //__add_128_128(FS, F, CX);
            {
                FS_w1 = F_w1 + CX_w1;
                FS_w0 = CX_w0 + F_w0;
                if ((UnsignedLong.isLess(FS_w0, CX_w0)))
                    FS_w1++;
            }

//            if (rmode == 0)    //BID_ROUNDING_TO_NEAREST
            {
                // rounding code, here RN_EVEN
                // 10^(extra_digits+diff_dec_expon)
                table_index = (diff_dec_expon + extra_digits) << 1;
                T2_w0 = bid_power10_table_128_BID_UINT128[table_index];
                T2_w1 = bid_power10_table_128_BID_UINT128[table_index + 1];
                if (__unsigned_compare_gt_128(FS_w0, FS_w1, T2_w0, T2_w1)
                    || ((CYh & 1) != 0 && (FS_w1 == T2_w1 && FS_w0 == T2_w0) /*__test_equal_128(FS, T2)*/)) {
                    CYh++;
                    //__sub_128_128(FS, FS, T2);
                    {
                        long /*BID_UINT128*/ Q128_w1 = FS_w1 - T2_w1;
                        long /*BID_UINT128*/ Q128_w0 = FS_w0 - T2_w0;
                        if (UnsignedLong.isLess(FS_w0, T2_w0))
                            Q128_w1--;
                        FS_w1 = Q128_w1;
                        FS_w0 = Q128_w0;
                    }
                }
            }
//            if (rmode == 4)    //BID_ROUNDING_TO_NEAREST
//            {
//                // rounding code, here RN_AWAY
//                // 10^(extra_digits+diff_dec_expon)
//                table_index = (diff_dec_expon + extra_digits) << 1;
//                T2_w0 = bid_power10_table_128_BID_UINT128[table_index];
//                T2_w1 = bid_power10_table_128_BID_UINT128[table_index + 1];
//                if (__unsigned_compare_ge_128(FS_w0, FS_w1, T2_w0, T2_w1)) {
//                    CYh++;
//                    //__sub_128_128(FS, FS, T2);
//                    {
//                        long /*BID_UINT128*/ Q128_w1 = FS_w1 - T2_w1;
//                        long /*BID_UINT128*/ Q128_w0 = FS_w0 - T2_w0;
//                        if (UnsignedLong.isLess(FS_w0, T2_w0))
//                            Q128_w1--;
//                        FS_w1 = Q128_w1;
//                        FS_w0 = Q128_w0;
//                    }
//                }
//            }
//            switch (rmode) {
//                case BID_ROUNDING_DOWN:
//                case BID_ROUNDING_TO_ZERO:
//                    if (/*(BID_SINT64)*/ FS_w1 < 0) {
//                        CYh--;
//                        if (UnsignedLong.isLess(CYh, 1000000000000000L)) {
//                            CYh = 9999999999999999L;
//                            final_exponent_y--;
//                        }
//                    } else {
//                        table_index = (diff_dec_expon + extra_digits) << 1;
//                        T2_w0 = bid_power10_table_128_BID_UINT128[table_index];
//                        T2_w1 = bid_power10_table_128_BID_UINT128[table_index + 1];
//                        if (__unsigned_compare_ge_128(FS_w0, FS_w1, T2_w0, T2_w1)) {
//                            CYh++;
//                            //__sub_128_128(FS, FS, T2);
//                            {
//                                long /*BID_UINT128*/ Q128_w1 = FS_w1 - T2_w1;
//                                long /*BID_UINT128*/ Q128_w0 = FS_w0 - T2_w0;
//                                if (UnsignedLong.isLess(FS_w0, T2_w0))
//                                    Q128_w1--;
//                                FS_w1 = Q128_w1;
//                                FS_w0 = Q128_w0;
//                            }
//                        }
//                    }
//                    break;
//                case BID_ROUNDING_UP:
//                    if (/*(BID_SINT64)*/ FS_w1 < 0)
//                        break;
//                    table_index = (diff_dec_expon + extra_digits) << 1;
//                    T2_w0 = bid_power10_table_128_BID_UINT128[table_index];
//                    T2_w1 = bid_power10_table_128_BID_UINT128[table_index + 1];
//                    if (__unsigned_compare_gt_128(FS_w0, FS_w1, T2_w0, T2_w1)) {
//                        CYh += 2;
//                        //__sub_128_128(FS, FS, T2);
//                        {
//                            long /*BID_UINT128*/ Q128_w1 = FS_w1 - T2_w1;
//                            long /*BID_UINT128*/ Q128_w0 = FS_w0 - T2_w0;
//                            if (UnsignedLong.isLess(FS_w0, T2_w0))
//                                Q128_w1--;
//                            FS_w1 = Q128_w1;
//                            FS_w0 = Q128_w0;
//                        }
//                    } else if ((FS_w1 == T2_w1) && (FS_w0 == T2_w0)) {
//                        CYh++;
//                        FS_w1 = FS_w0 = 0;
//                    } else if ((FS_w1 | FS_w0) != 0)
//                        CYh++;
//                    break;
//            }

//            status = BID_INEXACT_EXCEPTION;
//            if ((rmode & 3) == 0) {
//                // RN modes
//                table_index = (diff_dec_expon + extra_digits) << 1;
//                if ((FS_w1 == bid_round_const_table_128_BID_UINT128[0][table_index + 1])
//                    && (FS_w0 == bid_round_const_table_128_BID_UINT128[0][table_index /*+ 0*/]))
//                    status = BID_EXACT_STATUS;
//            } else if (FS_w1 == 0 && FS_w0 == 0)
//                status = BID_EXACT_STATUS;

//            __set_status_flags(fpsc, status);

            return get_BID64(sign_y, final_exponent_y, CYh/*, rounding_mode, fpsc*/);
        }
    }

    /**
     * return number of decimal digits in 128-bit value X
     */
    static int __get_dec_digits64(final long /*BID_UINT128*/ X_w0, final long X_w1) {
        long /*int_double*/ tempx_i;
        int digits_x, bin_expon_cx;

        if (X_w1 == 0) {
            if (X_w0 == 0) return 0;
            //--- get number of bits in the coefficients of x and y ---
            tempx_i = UnsignedLong.longToDoubleRawBits(X_w0);
            bin_expon_cx = (int) (((tempx_i & MASK_BINARY_EXPONENT) >>> 52) - 0x3ff);
            // get number of decimal digits in the coeff_x
            digits_x = bid_estimate_decimal_digits[bin_expon_cx];
            if (UnsignedLong.isGreaterOrEqual(X_w0, bid_power10_table_128_BID_UINT128[(digits_x << 1) /*+ 0*/]))
                digits_x++;
            return digits_x;
        }
        tempx_i = UnsignedLong.longToDoubleRawBits(X_w1);
        bin_expon_cx = (int) (((tempx_i & MASK_BINARY_EXPONENT) >>> 52) - 0x3ff);
        // get number of decimal digits in the coeff_x
        digits_x = bid_estimate_decimal_digits[bin_expon_cx + 64];
        final int table_index = digits_x << 1;
        if (__unsigned_compare_ge_128(X_w0, X_w1, bid_power10_table_128_BID_UINT128[table_index], bid_power10_table_128_BID_UINT128[table_index + 1]))
            digits_x++;

        return digits_x;
    }

    static long[][] /*BID_UINT128*/ bid_round_const_table_128_BID_UINT128 = {
        {    //RN
            0L, 0L,    // 0 extra digits
            5L, 0L,    // 1 extra digits
            50L, 0L,    // 2 extra digits
            500L, 0L,    // 3 extra digits
            5000L, 0L,    // 4 extra digits
            50000L, 0L,    // 5 extra digits
            500000L, 0L,    // 6 extra digits
            5000000L, 0L,    // 7 extra digits
            50000000L, 0L,    // 8 extra digits
            500000000L, 0L,    // 9 extra digits
            5000000000L, 0L,    // 10 extra digits
            50000000000L, 0L,    // 11 extra digits
            500000000000L, 0L,    // 12 extra digits
            5000000000000L, 0L,    // 13 extra digits
            50000000000000L, 0L,    // 14 extra digits
            500000000000000L, 0L,    // 15 extra digits
            5000000000000000L, 0L,    // 16 extra digits
            50000000000000000L, 0L,    // 17 extra digits
            500000000000000000L, 0L,    // 18 extra digits
            5000000000000000000L, 0L,    // 19 extra digits
            0xb5e3af16b1880000L, 2L,    //20
            0x1ae4d6e2ef500000L, 27L,    //21
            0xcf064dd59200000L, 271L,    //22
            0x8163f0a57b400000L, 2710L,    //23
            0xde76676d0800000L, 27105L,    //24
            0x8b0a00a425000000L, 0x422caL,    //25
            0x6e64066972000000L, 0x295be9L,    //26
            0x4fe8401e74000000L, 0x19d971eL,    //27
            0x1f12813088000000L, 0x1027e72fL,    //28
            0x36b90be550000000L, 0xa18f07d7L,    //29
            0x233a76f520000000L, 0x64f964e68L,    //30
            0x6048a59340000000L, 0x3f1bdf1011L,    //31
            0xc2d677c080000000L, 0x27716b6a0adL,    //32
            0x9c60ad8500000000L, 0x18a6e32246c9L,    //33
            0x1bc6c73200000000L, 0xf684df56c3e0L,    //34
            0x15c3c7f400000000L, 0x9a130b963a6c1L,    //35
        },
        {    //RD
            0L, 0L,    // 0 extra digits
            0L, 0L,    // 1 extra digits
            0L, 0L,    // 2 extra digits
            00L, 0L,    // 3 extra digits
            000L, 0L,    // 4 extra digits
            0000L, 0L,    // 5 extra digits
            00000L, 0L,    // 6 extra digits
            000000L, 0L,    // 7 extra digits
            0000000L, 0L,    // 8 extra digits
            00000000L, 0L,    // 9 extra digits
            000000000L, 0L,    // 10 extra digits
            0000000000L, 0L,    // 11 extra digits
            00000000000L, 0L,    // 12 extra digits
            000000000000L, 0L,    // 13 extra digits
            0000000000000L, 0L,    // 14 extra digits
            00000000000000L, 0L,    // 15 extra digits
            000000000000000L, 0L,    // 16 extra digits
            0000000000000000L, 0L,    // 17 extra digits
            00000000000000000L, 0L,    // 18 extra digits
            000000000000000000L, 0L,    // 19 extra digits
            0L, 0L,    //20
            0L, 0L,    //21
            0L, 0L,    //22
            0L, 0L,    //23
            0L, 0L,    //24
            0L, 0L,    //25
            0L, 0L,    //26
            0L, 0L,    //27
            0L, 0L,    //28
            0L, 0L,    //29
            0L, 0L,    //30
            0L, 0L,    //31
            0L, 0L,    //32
            0L, 0L,    //33
            0L, 0L,    //34
            0L, 0L,    //35
        },
        {    //RU
            0L, 0L,    // 0 extra digits
            9L, 0L,    // 1 extra digits
            99L, 0L,    // 2 extra digits
            999L, 0L,    // 3 extra digits
            9999L, 0L,    // 4 extra digits
            99999L, 0L,    // 5 extra digits
            999999L, 0L,    // 6 extra digits
            9999999L, 0L,    // 7 extra digits
            99999999L, 0L,    // 8 extra digits
            999999999L, 0L,    // 9 extra digits
            9999999999L, 0L,    // 10 extra digits
            99999999999L, 0L,    // 11 extra digits
            999999999999L, 0L,    // 12 extra digits
            9999999999999L, 0L,    // 13 extra digits
            99999999999999L, 0L,    // 14 extra digits
            999999999999999L, 0L,    // 15 extra digits
            9999999999999999L, 0L,    // 16 extra digits
            99999999999999999L, 0L,    // 17 extra digits
            999999999999999999L, 0L,    // 18 extra digits
            0x8AC7230489E7FFFFL/*9999999999999999999L*/, 0L,    // 19 extra digits
            0x6BC75E2D630FFFFFL, 0x5L,    //20
            0x35C9ADC5DE9FFFFFL, 0x36L,    //21
            0x19E0C9BAB23FFFFFL, 0x21eL,    //22
            0x2C7E14AF67FFFFFL, 0x152dL,    //23
            0x1BCECCEDA0FFFFFFL, 0xd3c2L,    //24
            0x1614014849FFFFFFL, 0x84595L,    //25
            0xDCC80CD2E3FFFFFFL, 0x52b7d2L,    //26
            0x9FD0803CE7FFFFFFL, 0x33B2E3CL,    //27
            0x3E2502610FFFFFFFL, 0x204FCE5EL,    //28
            0x6D7217CA9FFFFFFFL, 0x1431E0FAEL,    //29
            0x4674EDEA3FFFFFFFL, 0xC9F2C9CD0L,    //30
            0xC0914B267FFFFFFFL, 0x7E37BE2022L,    //31
            0x85ACEF80FFFFFFFFL, 0x4EE2D6D415BL,    //32
            0x38c15b09ffffffffL, 0x314dc6448d93L,    //33
            0x378d8e63ffffffffL, 0x1ed09bead87c0L,    //34
            0x2b878fe7ffffffffL, 0x13426172c74d82L,    //35
        },
        {    //RZ
            0L, 0L,    // 0 extra digits
            0L, 0L,    // 1 extra digits
            0L, 0L,    // 2 extra digits
            00L, 0L,    // 3 extra digits
            000L, 0L,    // 4 extra digits
            0000L, 0L,    // 5 extra digits
            00000L, 0L,    // 6 extra digits
            000000L, 0L,    // 7 extra digits
            0000000L, 0L,    // 8 extra digits
            00000000L, 0L,    // 9 extra digits
            000000000L, 0L,    // 10 extra digits
            0000000000L, 0L,    // 11 extra digits
            00000000000L, 0L,    // 12 extra digits
            000000000000L, 0L,    // 13 extra digits
            0000000000000L, 0L,    // 14 extra digits
            00000000000000L, 0L,    // 15 extra digits
            000000000000000L, 0L,    // 16 extra digits
            0000000000000000L, 0L,    // 17 extra digits
            00000000000000000L, 0L,    // 18 extra digits
            000000000000000000L, 0L,    // 19 extra digits
            0L, 0L,    //20
            0L, 0L,    //21
            0L, 0L,    //22
            0L, 0L,    //23
            0L, 0L,    //24
            0L, 0L,    //25
            0L, 0L,    //26
            0L, 0L,    //27
            0L, 0L,    //28
            0L, 0L,    //29
            0L, 0L,    //30
            0L, 0L,    //31
            0L, 0L,    //32
            0L, 0L,    //33
            0L, 0L,    //34
            0L, 0L,    //35
        },
        {    //RN, ties away
            0L, 0L,    // 0 extra digits
            5L, 0L,    // 1 extra digits
            50L, 0L,    // 2 extra digits
            500L, 0L,    // 3 extra digits
            5000L, 0L,    // 4 extra digits
            50000L, 0L,    // 5 extra digits
            500000L, 0L,    // 6 extra digits
            5000000L, 0L,    // 7 extra digits
            50000000L, 0L,    // 8 extra digits
            500000000L, 0L,    // 9 extra digits
            5000000000L, 0L,    // 10 extra digits
            50000000000L, 0L,    // 11 extra digits
            500000000000L, 0L,    // 12 extra digits
            5000000000000L, 0L,    // 13 extra digits
            50000000000000L, 0L,    // 14 extra digits
            500000000000000L, 0L,    // 15 extra digits
            5000000000000000L, 0L,    // 16 extra digits
            50000000000000000L, 0L,    // 17 extra digits
            500000000000000000L, 0L,    // 18 extra digits
            5000000000000000000L, 0L,    // 19 extra digits
            0xb5e3af16b1880000L, 2L,    //20
            0x1ae4d6e2ef500000L, 27L,    //21
            0xcf064dd59200000L, 271L,    //22
            0x8163f0a57b400000L, 2710L,    //23
            0xde76676d0800000L, 27105L,    //24
            0x8b0a00a425000000L, 0x422caL,    //25
            0x6e64066972000000L, 0x295be9L,    //26
            0x4fe8401e74000000L, 0x19d971eL,    //27
            0x1f12813088000000L, 0x1027e72fL,    //28
            0x36b90be550000000L, 0xa18f07d7L,    //29
            0x233a76f520000000L, 0x64f964e68L,    //30
            0x6048a59340000000L, 0x3f1bdf1011L,    //31
            0xc2d677c080000000L, 0x27716b6a0adL,    //32
            0x9c60ad8500000000L, 0x18a6e32246c9L,    //33
            0x1bc6c73200000000L, 0xf684df56c3e0L,    //34
            0x15c3c7f400000000L, 0x9a130b963a6c1L,    //35
        }
    };
}
