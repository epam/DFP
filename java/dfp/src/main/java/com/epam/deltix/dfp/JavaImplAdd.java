package com.epam.deltix.dfp;

class JavaImplAdd {
    private JavaImplAdd() {
    }

    static final long SPECIAL_ENCODING_MASK64 = 0x6000000000000000L;
    static final long INFINITY_MASK64 = 0x7800000000000000L;
    static final long SINFINITY_MASK64 = 0xf800000000000000L;
    static final long SSNAN_MASK64 = 0xfc00000000000000L;
    static final long NAN_MASK64 = 0x7c00000000000000L;
    static final long SNAN_MASK64 = 0x7e00000000000000L;
    static final long QUIET_MASK64 = 0xfdffffffffffffffL;
    static final long LARGE_COEFF_MASK64 = 0x0007ffffffffffffL;
    static final long LARGE_COEFF_HIGH_BIT64 = 0x0020000000000000L;
    static final long SMALL_COEFF_MASK64 = 0x001fffffffffffffL;
    static final int EXPONENT_MASK64 = 0x3ff;
    static final int EXPONENT_SHIFT_LARGE64 = 51;
    static final int EXPONENT_SHIFT_SMALL64 = 53;
    //static final long LARGEST_BID64 =           0x77fb86f26fc0ffffL;
    //static final long SMALLEST_BID64 =          0xf7fb86f26fc0ffffL;
    //static final long SMALL_COEFF_MASK128 =     0x0001ffffffffffffL;
    //static final long LARGE_COEFF_MASK128 =     0x00007fffffffffffL;
    //static final long EXPONENT_MASK128 =        0x3fff
    //static final long LARGEST_BID128_HIGH =     0x5fffed09bead87c0L;
    //static final long LARGEST_BID128_LOW =      0x378d8e63ffffffffL;
    //static final long SPECIAL_ENCODING_MASK32 = 0x60000000ul
    //static final long SINFINITY_MASK32 =        0xf8000000ul
    //static final long INFINITY_MASK32 =         0x78000000ul
    //static final long LARGE_COEFF_MASK32 =      0x007ffffful
    //static final long LARGE_COEFF_HIGH_BIT32 =  0x00800000ul
    //static final long SMALL_COEFF_MASK32 =      0x001ffffful
    //static final long EXPONENT_MASK32 =         0xff
    //static final long LARGEST_BID32 =           0x77f8967f
    //static final long NAN_MASK32 =              0x7c000000
    //static final long SNAN_MASK32 =             0x7e000000
    //static final long SSNAN_MASK32 =            0xfc000000
    //static final long QUIET_MASK32 =            0xfdffffff
    static final long MASK_BINARY_EXPONENT = 0x7ff0000000000000L;
    static final int BINARY_EXPONENT_BIAS = 0x3ff;
    static final int UPPER_EXPON_LIMIT = 51;
    static final int DECIMAL_MAX_EXPON_64 = 767;
    static final int DECIMAL_EXPONENT_BIAS = 398;
    static final int MAX_FORMAT_DIGITS = 16;

    static final int BID_ROUNDING_TO_NEAREST = 0x00000;
    static final int BID_ROUNDING_DOWN = 0x00001;
    static final int BID_ROUNDING_UP = 0x00002;
    static final int BID_ROUNDING_TO_ZERO = 0x00003;
    static final int BID_ROUNDING_TIES_AWAY = 0x00004;

    static final long LARGEST_BID64 = 0x77fb86f26fc0ffffL;
    static final long SMALLEST_BID64 = 0xf7fb86f26fc0ffffL;

    static final long LONG_LOW_PART = 0xFFFFFFFFL;


    static long unpack_BID64(final Decimal64Parts p, final long x) {
        p.signMask = x & 0x8000000000000000L;

        if ((x & SPECIAL_ENCODING_MASK64) != SPECIAL_ENCODING_MASK64) {
            // exponent
            long tmp = x >>> EXPONENT_SHIFT_SMALL64;
            p.exponent = (int) (tmp & EXPONENT_MASK64);
            // coefficient
            p.coefficient = (x & SMALL_COEFF_MASK64);

            return p.coefficient;
        } else {
            // special encodings
            if ((x & INFINITY_MASK64) == INFINITY_MASK64) {
                p.exponent = 0;
                p.coefficient = x & 0xfe03ffffffffffffL;
                if ((x & 0x0003ffffffffffffL) >= 1000000000000000L)
                    p.coefficient = x & 0xfe00000000000000L;
                if ((x & NAN_MASK64) == INFINITY_MASK64)
                    p.coefficient = x & SINFINITY_MASK64;
                return 0;    // NaN or Infinity
            } else {
                // coefficient
                long coeff = (x & LARGE_COEFF_MASK64) | LARGE_COEFF_HIGH_BIT64;
                // check for non-canonical values
                if ((UnsignedLong.isGreaterOrEqual(coeff, 10000000000000000L)))
                    coeff = 0;
                p.coefficient = coeff;
                // get exponent
                long tmp = x >>> EXPONENT_SHIFT_LARGE64;
                p.exponent = (int) (tmp & EXPONENT_MASK64);
                return coeff;
            }
        }
    }

    public static long bid64_sub(final long x, long y) {
        // check if y is not NaN
        if (((y & NAN_MASK64) != NAN_MASK64))
            y ^= 0x8000000000000000L;

        return bid64_add(x, y);
    }

    /**
     * _  Intel's original
     * _  Algorithm description:
     * _
     * _   if(exponent_a < exponent_b)
     * _       switch a, b
     * _   diff_expon = exponent_a - exponent_b
     * _   if(diff_expon > 16)
     * _      return normalize(a)
     * _   if(coefficient_a*10^diff_expon guaranteed below 2^62)
     * _       S = sign_a*coefficient_a*10^diff_expon + sign_b*coefficient_b
     * _       if(|S|<10^16)
     * _           return get_BID64(sign(S),exponent_b,|S|)
     * _       else
     * _          determine number of extra digits in S (1, 2, or 3)
     * _            return rounded result
     * _   else // large exponent difference
     * _       if(number_digits(coefficient_a*10^diff_expon) +/- 10^16)
     * _          guaranteed the same as
     * _          number_digits(coefficient_a*10^diff_expon) )
     * _           S = normalize(coefficient_a + (sign_a^sign_b)*10^(16-diff_expon))
     * _           corr = 10^16 + (sign_a^sign_b)*coefficient_b
     * _           corr*10^exponent_b is rounded so it aligns with S*10^exponent_S
     * _           return get_BID64(sign_a,exponent(S),S+rounded(corr))
     * _       else
     * _         add sign_a*coefficient_a*10^diff_expon, sign_b*coefficient_b
     * _             in 128-bit integer arithmetic, then round to 16 decimal digits
     * <p>
     * Compilation flags:
     * BID_FUNCTION_SETS_BINARY_FLAGS undefined
     * DECIMAL_CALL_BY_REFERENCE defined to: 0
     * DECIMAL_GLOBAL_ROUNDING defined to: 1
     * BID_SET_STATUS_FLAGS defined to:
     * IEEE_ROUND_NEAREST_TIES_AWAY undefined
     * IEEE_ROUND_NEAREST undefined
     * rnd_mode = BID_ROUNDING_TO_NEAREST 0x00000
     *
     * @param x First term
     * @param y Second term
     * @return The sum of operands
     */
    public static long bid64_add(final long x, final long y) {

        long CA_w0, CA_w1, CT_w0, CT_w1, CT_new_w0, CT_new_w1;
        long sign_x, sign_y, coefficient_x, coefficient_y, C64_new;
        long valid_x, valid_y;
        long sign_a, sign_b, coefficient_a, coefficient_b, sign_s, sign_ab, rem_a;
        long saved_ca, saved_cb, C0_64, C64, remainder_h, T1, carry, tmp;
        int exponent_x, exponent_y, exponent_a, exponent_b, diff_dec_expon;
        int bin_expon_ca, extra_digits, amount, scale_k, scale_ca;

        // long valid_x = unpack_BID64(&sign_x, &exponent_x, &coefficient_x, x);
        {
            sign_x = x & 0x8000000000000000L;

            if ((x & SPECIAL_ENCODING_MASK64) != SPECIAL_ENCODING_MASK64) {
                // exponent
                tmp = x >>> EXPONENT_SHIFT_SMALL64;
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
                    if ((UnsignedLong.isGreaterOrEqual(coeff, 10000000000000000L)))
                        coeff = 0;
                    coefficient_x = coeff;
                    // get exponent
                    tmp = x >>> EXPONENT_SHIFT_LARGE64;
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
                tmp = y >>> EXPONENT_SHIFT_SMALL64;
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
                    if ((UnsignedLong.isGreaterOrEqual(coeff, 10000000000000000L)))
                        coeff = 0;
                    coefficient_y = coeff;
                    // get exponent
                    tmp = y >>> EXPONENT_SHIFT_LARGE64;
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
                // check if y is Inf
                if (((y & NAN_MASK64) == INFINITY_MASK64)) {
                    if (sign_x == (y & 0x8000000000000000L)) {
                        return coefficient_x;
                    }
                    // return NaN
                    {
                        return NAN_MASK64;
                    }
                }
                // check if y is NaN
                if (((y & NAN_MASK64) == NAN_MASK64)) {
                    return coefficient_y & QUIET_MASK64;
                }
                // otherwise return +/-Inf
                {
                    return coefficient_x;
                }
            }
            // x is 0
            {
                if (((y & INFINITY_MASK64) != INFINITY_MASK64) && (coefficient_y != 0)) {
                    if (exponent_y <= exponent_x) {
                        return y;
                    }
                }
            }

        }
        if (valid_y == 0) {
            // y is Inf. or NaN?
            if (((y & INFINITY_MASK64) == INFINITY_MASK64)) {
                return coefficient_y & QUIET_MASK64;
            }
            // y is 0
            if (coefficient_x == 0) {    // x==0
                long res;
                if (exponent_x <= exponent_y)
                    res = ((long) exponent_x) << 53;
                else
                    res = ((long) exponent_y) << 53;
                if (sign_x == sign_y)
                    res |= sign_x;
                return res;
            } else if (exponent_y >= exponent_x) {
                return x;
            }
        }
        // sort arguments by exponent
        if (exponent_x < exponent_y) {
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

        // version 2 (original)
        long tempxi = Double.doubleToRawLongBits((double) coefficient_a);
        bin_expon_ca = (int) ((tempxi & MASK_BINARY_EXPONENT) >>> 52) - 0x3ff;

        if (diff_dec_expon > MAX_FORMAT_DIGITS) {
            // normalize a to a 16-digit coefficient

            scale_ca = bid_estimate_decimal_digits[bin_expon_ca];
            if ((UnsignedLong.isGreaterOrEqual(coefficient_a, bid_power10_table_128_w0[scale_ca])))
                scale_ca++;

            scale_k = 16 - scale_ca;

            coefficient_a *= bid_power10_table_128_w0[scale_k];

            diff_dec_expon -= scale_k;
            exponent_a -= scale_k;

            /* get binary coefficients of x and y */

            //--- get number of bits in the coefficients of x and y ---
            tempxi = Double.doubleToRawLongBits((double) coefficient_a);
            bin_expon_ca = (int) ((tempxi & MASK_BINARY_EXPONENT) >>> 52) - 0x3ff;

            if (diff_dec_expon > MAX_FORMAT_DIGITS) {
                // check special case here
                if ((coefficient_a == 1000000000000000L)
                    && (diff_dec_expon == MAX_FORMAT_DIGITS + 1)
                    && ((sign_a ^ sign_b) != 0)
                    && ((UnsignedLong.isGreater(coefficient_b, 5000000000000000L)))) {
                    coefficient_a = 9999999999999999L;
                    exponent_a--;
                }

                return fast_get_BID64_check_OF(sign_a, exponent_a, coefficient_a);
            }
        }
        // test whether coefficient_a*10^(exponent_a-exponent_b)  may exceed 2^62
        if (bin_expon_ca + bid_estimate_bin_expon[diff_dec_expon] < 60) {
            // coefficient_a*10^(exponent_a-exponent_b)<2^63

            // multiply by 10^(exponent_a-exponent_b)
            coefficient_a *= bid_power10_table_128_w0[diff_dec_expon];

            // sign mask
            sign_b = sign_b >> 63;  // @AD: signed value shift
            // apply sign to coeff. of b
            coefficient_b = (coefficient_b + sign_b) ^ sign_b;

            // apply sign to coefficient a
            sign_a = sign_a >> 63;  // @AD: signed value shift
            coefficient_a = (coefficient_a + sign_a) ^ sign_a;

            coefficient_a += coefficient_b;
            // get sign
            sign_s = coefficient_a >> 63;  // @AD: signed value shift
            coefficient_a = (coefficient_a + sign_s) ^ sign_s;
            sign_s &= 0x8000000000000000L;

            // coefficient_a < 10^16 ?
            if ((UnsignedLong.isLess(coefficient_a, bid_power10_table_128_w0[MAX_FORMAT_DIGITS]))) {
                return very_fast_get_BID64(sign_s, exponent_b, coefficient_a);
            }
            // otherwise rounding is necessary

            // already know coefficient_a<10^19
            // coefficient_a < 10^17 ?
            if ((UnsignedLong.isLess(coefficient_a, bid_power10_table_128_w0[17])))
                extra_digits = 1;
            else if ((UnsignedLong.isLess(coefficient_a, bid_power10_table_128_w0[18])))
                extra_digits = 2;
            else
                extra_digits = 3;

            coefficient_a += bid_round_const_table_nearest[extra_digits];

            // get P*(2^M[extra_digits])/10^extra_digits
            //__mul_64x64_to_128(CT, coefficient_a, bid_reciprocals10_64[extra_digits]);
            {
                long __CX = coefficient_a;
                long __CY = bid_reciprocals10_64[extra_digits];
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

                CT_w1 = __PH + (__PM >>> 32);
                CT_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
            }

            // now get P/10^extra_digits: shift C64 right by M[extra_digits]-128
            amount = bid_short_recip_scale[extra_digits];
            C64 = CT_w1 >>> amount;

        } else {
            // coefficient_a*10^(exponent_a-exponent_b) is large
            sign_s = sign_a;

            // check whether we can take faster path
            scale_ca = bid_estimate_decimal_digits[bin_expon_ca];

            sign_ab = sign_a ^ sign_b;
            sign_ab = sign_ab >> 63;  // @AD: signed value shift

            // T1 = 10^(16-diff_dec_expon)
            T1 = bid_power10_table_128_w0[16 - diff_dec_expon];

            // get number of digits in coefficient_a
            if ((UnsignedLong.isGreaterOrEqual(coefficient_a, bid_power10_table_128_w0[scale_ca]))) {
                scale_ca++;
            }

            scale_k = 16 - scale_ca;

            // addition
            saved_ca = coefficient_a - T1;
            coefficient_a = saved_ca * bid_power10_table_128_w0[scale_k];
            extra_digits = diff_dec_expon - scale_k;

            // apply sign
            saved_cb = (coefficient_b + sign_ab) ^ sign_ab;
            // add 10^16 and rounding constant
            coefficient_b = saved_cb + 10000000000000000L + bid_round_const_table_nearest[extra_digits];

            // get P*(2^M[extra_digits])/10^extra_digits
            //__mul_64x64_to_128(CT, coefficient_b, bid_reciprocals10_64[extra_digits]);
            {
                final long __CX = coefficient_b;
                final long __CY = bid_reciprocals10_64[extra_digits];
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

                CT_w1 = __PH + (__PM >>> 32);
                CT_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
            }

            // now get P/10^extra_digits: shift C64 right by M[extra_digits]-128
            amount = bid_short_recip_scale[extra_digits];
            C0_64 = CT_w1 >>> amount;

            // result coefficient
            C64 = C0_64 + coefficient_a;
            // filter out difficult (corner) cases
            // this test ensures the number of digits in coefficient_a does not change
            // after adding (the appropriately scaled and rounded) coefficient_b
            if ((UnsignedLong.isGreater(C64 - 1000000000000000L - 1, 9000000000000000L - 2))) {
                if ((UnsignedLong.isGreaterOrEqual(C64, 10000000000000000L))) {
                    // result has more than 16 digits
                    if (scale_k == 0) {
                        // must divide coeff_a by 10
                        saved_ca = saved_ca + T1;
                        //__mul_64x64_to_128(CA, saved_ca, 0x3333333333333334L);
                        {
                            final long __CX = saved_ca;
                            final long __CY = 0x3333333333333334L;
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

                            CA_w1 = __PH + (__PM >>> 32);
                            CA_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
                        }
                        //reciprocals10_64[1]);
                        coefficient_a = CA_w1 >>> 1;
                        rem_a =
                            saved_ca - (coefficient_a << 3) - (coefficient_a << 1);
                        coefficient_a = coefficient_a - T1;

                        saved_cb += rem_a * bid_power10_table_128_w0[diff_dec_expon];
                    } else
                        coefficient_a = (saved_ca - T1 - (T1 << 3)) * bid_power10_table_128_w0[scale_k - 1];

                    extra_digits++;
                    coefficient_b = saved_cb + 100000000000000000L + bid_round_const_table_nearest[extra_digits];

                    // get P*(2^M[extra_digits])/10^extra_digits
                    //__mul_64x64_to_128(CT, coefficient_b, bid_reciprocals10_64[extra_digits]);
                    {
                        final long __CX = coefficient_b;
                        final long __CY = bid_reciprocals10_64[extra_digits];
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

                        CT_w1 = __PH + (__PM >>> 32);
                        CT_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
                    }
                    // now get P/10^extra_digits: shift C64 right by M[extra_digits]-128
                    amount = bid_short_recip_scale[extra_digits];
                    C0_64 = CT_w1 >>> amount;

                    // result coefficient
                    C64 = C0_64 + coefficient_a;
                } else if ((UnsignedLong.isLessOrEqual(C64, 1000000000000000L))) {
                    // less than 16 digits in result
                    coefficient_a = saved_ca * bid_power10_table_128_w0[scale_k + 1];
                    //extra_digits --;
                    exponent_b--;
                    coefficient_b = (saved_cb << 3) + (saved_cb << 1) + 100000000000000000L
                        + bid_round_const_table_nearest[extra_digits];

                    // get P*(2^M[extra_digits])/10^extra_digits
                    //__mul_64x64_to_128(CT_new, coefficient_b, bid_reciprocals10_64[extra_digits]);
                    {
                        final long __CX = coefficient_b;
                        final long __CY = bid_reciprocals10_64[extra_digits];
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

                        CT_new_w1 = __PH + (__PM >>> 32);
                        CT_new_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
                    }
                    // now get P/10^extra_digits: shift C64 right by M[extra_digits]-128
                    amount = bid_short_recip_scale[extra_digits];
                    C0_64 = CT_new_w1 >>> amount;

                    // result coefficient
                    C64_new = C0_64 + coefficient_a;
                    if ((UnsignedLong.isLess(C64_new, 10000000000000000L))) {
                        C64 = C64_new;
                        CT_w0 = CT_new_w0;
                        CT_w1 = CT_new_w1;
                    } else
                        exponent_b++;
                }

            }

        }

        // if (BID_ROUNDING_TO_NEAREST == 0)    //BID_ROUNDING_TO_NEAREST
        if ((C64 & 1) != 0) {
            // check whether fractional part of initial_P/10^extra_digits is
            // exactly .5
            // this is the same as fractional part of
            //      (initial_P + 0.5*10^extra_digits)/10^extra_digits is exactly zero

            // get remainder
            remainder_h = CT_w1 << (64 - amount);

            // test whether fractional part is 0
            if (remainder_h == 0 && ((UnsignedLong.isLess(CT_w0, bid_reciprocals10_64[extra_digits])))) {
                C64--;
            }
        }

        return
            fast_get_BID64_check_OF(sign_s, exponent_b + extra_digits, C64);
    }

    //
    //   no underflow checking
    //
    static long fast_get_BID64_check_OF(long sgn, int expon, long coeff) {
        long r, mask;

        if ((/*UnsignedInteger.compare*/(expon) + Integer.MIN_VALUE >= (3 * 256 - 1) + Integer.MIN_VALUE)) {
            if ((expon == 3 * 256 - 1) && coeff == 10000000000000000L) {
                expon = 3 * 256;
                coeff = 1000000000000000L;
            }

            if ((/*UnsignedInteger.compare*/(expon) + Integer.MIN_VALUE >= (3 * 256) + Integer.MIN_VALUE)) {
                while ((UnsignedLong.isLess(coeff, 1000000000000000L)) && expon >= 3 * 256) {
                    expon--;
                    coeff = (coeff << 3) + (coeff << 1);
                }
                if (expon > DECIMAL_MAX_EXPON_64) {
                    // overflow
                    return sgn | INFINITY_MASK64;
                }
            }
        }

        mask = 1;
        mask <<= EXPONENT_SHIFT_SMALL64;

        // check whether coefficient fits in 10*5+3 bits
        if ((UnsignedLong.isLess(coeff, mask))) {
            r = expon;
            r <<= EXPONENT_SHIFT_SMALL64;
            r |= (coeff | sgn);
            return r;
        }
        // special format

        // eliminate the case coeff==10^16 after rounding
        if (coeff == 10000000000000000L) {
            r = expon + 1;
            r <<= EXPONENT_SHIFT_SMALL64;
            r |= (1000000000000000L | sgn);
            return r;
        }

        r = expon;
        r <<= EXPONENT_SHIFT_LARGE64;
        r |= (sgn | SPECIAL_ENCODING_MASK64);
        // add coeff, without leading bits
        mask = (mask >>> 2) - 1;
        coeff &= mask;
        r |= coeff;

        return r;
    }


    //
    //   No overflow/underflow checking
    //   or checking for coefficients equal to 10^16 (after rounding)
    //
    static long very_fast_get_BID64(long sgn, int expon, long coeff) {
        long r, mask;

        mask = 1;
        mask <<= EXPONENT_SHIFT_SMALL64;

        // check whether coefficient fits in 10*5+3 bits
        if ((UnsignedLong.isLess(coeff, mask))) {
            r = expon;
            r <<= EXPONENT_SHIFT_SMALL64;
            r |= (coeff | sgn);
            return r;
        }
        // special format
        r = expon;
        r <<= EXPONENT_SHIFT_LARGE64;
        r |= (sgn | SPECIAL_ENCODING_MASK64);
        // add coeff, without leading bits
        mask = (mask >>> 2) - 1;
        coeff &= mask;
        r |= coeff;

        return r;
    }

    // tables used in computation
    static final int[] bid_estimate_decimal_digits = {
        1,    //2^0 =1     < 10^0
        1,    //2^1 =2     < 10^1
        1,    //2^2 =4     < 10^1
        1,    //2^3 =8     < 10^1
        2,    //2^4 =16    < 10^2
        2,    //2^5 =32    < 10^2
        2,    //2^6 =64    < 10^2
        3,    //2^7 =128   < 10^3
        3,    //2^8 =256   < 10^3
        3,    //2^9 =512   < 10^3
        4,    //2^10=1024  < 10^4
        4,    //2^11=2048  < 10^4
        4,    //2^12=4096  < 10^4
        4,    //2^13=8192  < 10^4
        5,    //2^14=16384 < 10^5
        5,    //2^15=32768 < 10^5

        5,    //2^16=65536     < 10^5
        6,    //2^17=131072    < 10^6
        6,    //2^18=262144    < 10^6
        6,    //2^19=524288    < 10^6
        7,    //2^20=1048576   < 10^7
        7,    //2^21=2097152   < 10^7
        7,    //2^22=4194304   < 10^7
        7,    //2^23=8388608   < 10^7
        8,    //2^24=16777216  < 10^8
        8,    //2^25=33554432  < 10^8
        8,    //2^26=67108864  < 10^8
        9,    //2^27=134217728 < 10^9
        9,    //2^28=268435456 < 10^9
        9,    //2^29=536870912 < 10^9
        10,    //2^30=1073741824< 10^10
        10,    //2^31=2147483648< 10^10

        10,    //2^32=4294967296     < 10^10
        10,    //2^33=8589934592     < 10^10
        11,    //2^34=17179869184    < 10^11
        11,    //2^35=34359738368    < 10^11
        11,    //2^36=68719476736    < 10^11
        12,    //2^37=137438953472   < 10^12
        12,    //2^38=274877906944   < 10^12
        12,    //2^39=549755813888   < 10^12
        13,    //2^40=1099511627776  < 10^13
        13,    //2^41=2199023255552  < 10^13
        13,    //2^42=4398046511104  < 10^13
        13,    //2^43=8796093022208  < 10^13
        14,    //2^44=17592186044416 < 10^14
        14,    //2^45=35184372088832 < 10^14
        14,    //2^46=70368744177664 < 10^14
        15,    //2^47=140737488355328< 10^15

        15,    //2^48=281474976710656    < 10^15
        15,    //2^49=562949953421312    < 10^15
        16,    //2^50=1125899906842624   < 10^16
        16,    //2^51=2251799813685248   < 10^16
        16,    //2^52=4503599627370496   < 10^16
        16,    //2^53=9007199254740992   < 10^16
        17,    //2^54=18014398509481984  < 10^17
        17,    //2^55=36028797018963968  < 10^17
        17,    //2^56=72057594037927936  < 10^17
        18,    //2^57=144115188075855872 < 10^18
        18,    //2^58=288230376151711744 < 10^18
        18,    //2^59=576460752303423488 < 10^18
        19,    //2^60=1152921504606846976< 10^19
        19,    //2^61=2305843009213693952< 10^19
        19,    //2^62=4611686018427387904< 10^19
        19,    //2^63=9223372036854775808< 10^19

        20,    //2^64=18446744073709551616
        20,    //2^65=36893488147419103232
        20,    //2^66=73786976294838206464
        21,    //2^67=147573952589676412928
        21,    //2^68=295147905179352825856
        21,    //2^69=590295810358705651712
        22,    //2^70=1180591620717411303424
        22,    //2^71=2361183241434822606848
        22,    //2^72=4722366482869645213696
        22,    //2^73=9444732965739290427392
        23,    //2^74=18889465931478580854784
        23,    //2^75=37778931862957161709568
        23,    //2^76=75557863725914323419136
        24,    //2^77=151115727451828646838272
        24,    //2^78=302231454903657293676544
        24,    //2^79=604462909807314587353088

        25,    //2^80=1208925819614629174706176
        25,    //2^81=2417851639229258349412352
        25,    //2^82=4835703278458516698824704
        25,    //2^83=9671406556917033397649408
        26,    //2^84=19342813113834066795298816
        26,    //2^85=38685626227668133590597632
        26,    //2^86=77371252455336267181195264
        27,    //2^87=154742504910672534362390528
        27,    //2^88=309485009821345068724781056
        27,    //2^89=618970019642690137449562112
        28,    //2^90=1237940039285380274899124224
        28,    //2^91=2475880078570760549798248448
        28,    //2^92=4951760157141521099596496896
        28,    //2^93=9903520314283042199192993792
        29,    //2^94=19807040628566084398385987584
        29,    //2^95=39614081257132168796771975168
        29,    //2^96=79228162514264337593543950336

        30,    //2^97=158456325028528675187087900672
        30,    //2^98=316912650057057350374175801344
        30,    //2^99=633825300114114700748351602688
        31,    //2^100=1267650600228229401496703205376
        31,    //2^101=2535301200456458802993406410752
        31,    //2^102=5070602400912917605986812821504
        32,    //2^103=10141204801825835211973625643008
        32,    //2^104=20282409603651670423947251286016
        32,    //2^105=40564819207303340847894502572032
        32,    //2^106=81129638414606681695789005144064
        33,    //2^107=162259276829213363391578010288128
        33,    // 2^108
        33,    // 2^109
        34,    // 2^110
        34,    // 2^111
        34,    // 2^112
        35,    // 2^113
        35,    // 2^114
        35,    // 2^115
        35,    // 2^116
        36,    // 2^117
        36,    // 2^118
        36,    // 2^119
        37,    // 2^120
        37,    // 2^121
        37,    // 2^122
        38,    // 2^123
        38,    // 2^124
        38,    // 2^125
        38,    // 2^126
        39,    // 2^127
        39    // 2^128
    };

    static final long[] bid_power10_table_128_w0 = {
        0x0000000000000001L,
        0x000000000000000aL,
        0x0000000000000064L,
        0x00000000000003e8L,
        0x0000000000002710L,
        0x00000000000186a0L,
        0x00000000000f4240L,
        0x0000000000989680L,
        0x0000000005f5e100L,
        0x000000003b9aca00L,
        0x00000002540be400L,
        0x000000174876e800L,
        0x000000e8d4a51000L,
        0x000009184e72a000L,
        0x00005af3107a4000L,
        0x00038d7ea4c68000L,
        0x002386f26fc10000L,
        0x016345785d8a0000L,
        0x0de0b6b3a7640000L,
        0x8ac7230489e80000L,
        0x6bc75e2d63100000L,
        0x35c9adc5dea00000L,
        0x19e0c9bab2400000L,
        0x02c7e14af6800000L,
        0x1bcecceda1000000L,
        0x161401484a000000L,
        0xdcc80cd2e4000000L,
        0x9fd0803ce8000000L,
        0x3e25026110000000L,
        0x6d7217caa0000000L,
        0x4674edea40000000L,
        0xc0914b2680000000L,
        0x85acef8100000000L,
        0x38c15b0a00000000L,
        0x378d8e6400000000L,
        0x2b878fe800000000L,
        0xb34b9f1000000000L,
        0x00f436a000000000L,
        0x098a224000000000L,
    };

    static final int[] bid_estimate_bin_expon = {
        0,    // 10^0
        3,    // 10^1
        6,    // 10^2
        9,    // 10^3
        13,    // 10^4
        16,    // 10^5
        19,    // 10^6
        23,    // 10^7
        26,    // 10^8
        29,    // 10^9
        33,    // 10^10
        36,    // 10^11
        39,    // 10^12
        43,    // 10^13
        46,    // 10^14
        49,    // 10^15
        53    // 10^16
    };

    static final long[] bid_round_const_table_nearest =
        {    // RN
            0L,    // 0 extra digits
            5L,    // 1 extra digits
            50L,    // 2 extra digits
            500L,    // 3 extra digits
            5000L,    // 4 extra digits
            50000L,    // 5 extra digits
            500000L,    // 6 extra digits
            5000000L,    // 7 extra digits
            50000000L,    // 8 extra digits
            500000000L,    // 9 extra digits
            5000000000L,    // 10 extra digits
            50000000000L,    // 11 extra digits
            500000000000L,    // 12 extra digits
            5000000000000L,    // 13 extra digits
            50000000000000L,    // 14 extra digits
            500000000000000L,    // 15 extra digits
            5000000000000000L,    // 16 extra digits
            50000000000000000L,    // 17 extra digits
            500000000000000000L    // 18 extra digits
        };

    static final long[][] /*BID_UINT64*/ bid_round_const_table = {
        bid_round_const_table_nearest,
        {    // RD
            0L,    // 0 extra digits
            0L,    // 1 extra digits
            0L,    // 2 extra digits
            00L,    // 3 extra digits
            000L,    // 4 extra digits
            0000L,    // 5 extra digits
            00000L,    // 6 extra digits
            000000L,    // 7 extra digits
            0000000L,    // 8 extra digits
            00000000L,    // 9 extra digits
            000000000L,    // 10 extra digits
            0000000000L,    // 11 extra digits
            00000000000L,    // 12 extra digits
            000000000000L,    // 13 extra digits
            0000000000000L,    // 14 extra digits
            00000000000000L,    // 15 extra digits
            000000000000000L,    // 16 extra digits
            0000000000000000L,    // 17 extra digits
            00000000000000000L    // 18 extra digits
        },
        {    // round to Inf
            0L,    // 0 extra digits
            9L,    // 1 extra digits
            99L,    // 2 extra digits
            999L,    // 3 extra digits
            9999L,    // 4 extra digits
            99999L,    // 5 extra digits
            999999L,    // 6 extra digits
            9999999L,    // 7 extra digits
            99999999L,    // 8 extra digits
            999999999L,    // 9 extra digits
            9999999999L,    // 10 extra digits
            99999999999L,    // 11 extra digits
            999999999999L,    // 12 extra digits
            9999999999999L,    // 13 extra digits
            99999999999999L,    // 14 extra digits
            999999999999999L,    // 15 extra digits
            9999999999999999L,    // 16 extra digits
            99999999999999999L,    // 17 extra digits
            999999999999999999L    // 18 extra digits
        },
        {    // RZ
            0L,    // 0 extra digits
            0L,    // 1 extra digits
            0L,    // 2 extra digits
            00L,    // 3 extra digits
            000L,    // 4 extra digits
            0000L,    // 5 extra digits
            00000L,    // 6 extra digits
            000000L,    // 7 extra digits
            0000000L,    // 8 extra digits
            00000000L,    // 9 extra digits
            000000000L,    // 10 extra digits
            0000000000L,    // 11 extra digits
            00000000000L,    // 12 extra digits
            000000000000L,    // 13 extra digits
            0000000000000L,    // 14 extra digits
            00000000000000L,    // 15 extra digits
            000000000000000L,    // 16 extra digits
            0000000000000000L,    // 17 extra digits
            00000000000000000L    // 18 extra digits
        },
        {    // round ties away from 0
            0L,    // 0 extra digits
            5L,    // 1 extra digits
            50L,    // 2 extra digits
            500L,    // 3 extra digits
            5000L,    // 4 extra digits
            50000L,    // 5 extra digits
            500000L,    // 6 extra digits
            5000000L,    // 7 extra digits
            50000000L,    // 8 extra digits
            500000000L,    // 9 extra digits
            5000000000L,    // 10 extra digits
            50000000000L,    // 11 extra digits
            500000000000L,    // 12 extra digits
            5000000000000L,    // 13 extra digits
            50000000000000L,    // 14 extra digits
            500000000000000L,    // 15 extra digits
            5000000000000000L,    // 16 extra digits
            50000000000000000L,    // 17 extra digits
            500000000000000000L    // 18 extra digits
        },
    };

    static final int[] bid_short_recip_scale = {
        1,
        65 - 64,
        69 - 64,
        71 - 64,
        75 - 64,
        78 - 64,
        81 - 64,
        85 - 64,
        88 - 64,
        91 - 64,
        95 - 64,
        98 - 64,
        101 - 64,
        105 - 64,
        108 - 64,
        111 - 64,
        115 - 64,    //114 - 64
        118 - 64
    };

    static final long[] bid_reciprocals10_64 = {
        1L,    // dummy value for 0 extra digits
        0x3333333333333334L,    // 1 extra digit
        0x51eb851eb851eb86L,
        0x20c49ba5e353f7cfL,
        0x346dc5d63886594bL,
        0x29f16b11c6d1e109L,
        0x218def416bdb1a6eL,
        0x35afe535795e90b0L,
        0x2af31dc4611873c0L,
        0x225c17d04dad2966L,
        0x36f9bfb3af7b7570L,
        0x2bfaffc2f2c92ac0L,
        0x232f33025bd42233L,
        0x384b84d092ed0385L,
        0x2d09370d42573604L,
        0x24075f3dceac2b37L,
        0x39a5652fb1137857L,
        0x2e1dea8c8da92d13L
    };
}
