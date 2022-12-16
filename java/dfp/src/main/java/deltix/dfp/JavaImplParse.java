package com.epam.deltix.dfp;

import static com.epam.deltix.dfp.JavaImpl.NaN;
import static com.epam.deltix.dfp.JavaImpl.ZERO;
import static com.epam.deltix.dfp.JavaImplAdd.*;

class JavaImplParse {
    private JavaImplParse() {
    }

    public static final int BID_EXACT_STATUS = 0x00000000;

    public static final int DEC_FE_INVALID = 0x01;
    public static final int DEC_FE_UNNORMAL = 0x02;
    public static final int DEC_FE_DIVBYZERO = 0x04;
    public static final int DEC_FE_OVERFLOW = 0x08;
    public static final int DEC_FE_UNDERFLOW = 0x10;
    public static final int DEC_FE_INEXACT = 0x20;

    public static final int BID_INEXACT_EXCEPTION = DEC_FE_INEXACT;
    public static final int BID_UNDERFLOW_EXCEPTION = DEC_FE_UNDERFLOW;
    public static final int BID_OVERFLOW_EXCEPTION = DEC_FE_OVERFLOW;
    public static final int BID_ZERO_DIVIDE_EXCEPTION = DEC_FE_DIVBYZERO;
    public static final int BID_DENORMAL_EXCEPTION = DEC_FE_UNNORMAL;
    public static final int BID_INVALID_EXCEPTION = DEC_FE_INVALID;
    public static final int BID_UNDERFLOW_INEXACT_EXCEPTION = (DEC_FE_UNDERFLOW | DEC_FE_INEXACT);
    public static final int BID_OVERFLOW_INEXACT_EXCEPTION = (DEC_FE_OVERFLOW | DEC_FE_INEXACT);
    public static final int BID_INVALID_FORMAT = 0x10000;

    //public static final int MAX_FORMAT_DIGITS = 16;
    public static final int MAX_DECIMAL_EXPONENT = 767;

    private static char tolower_macro(char x) {
        return ((x - 'A') <= ('Z' - 'A')) ? (char) (x - 'A' + 'a') : x;
    }

    public static void __set_status_flags(FloatingPointStatusFlag fpsc, int status) {
        fpsc.status |= status;
    }

    public static boolean is_inexact(FloatingPointStatusFlag fpsc) {
        return (fpsc.status & BID_INEXACT_EXCEPTION) != 0;
    }

    private static boolean IsStrEqIgnoreCase(final CharSequence s, int ps, final int ei, final String str) {
        if (ei - ps != str.length())
            return false;

        for (int strInd = 0; ps < ei; ++ps, ++strInd) {
            if (tolower_macro(s.charAt(ps)) != tolower_macro(str.charAt(strInd)))
                return false;
        }

        return true;
    }

    static class FloatingPointStatusFlag {
        protected int status = BID_EXACT_STATUS;
    }

    //public static long parse(final CharSequence s, final int si, final int ei, final int roundingMode)
    public static long bid64_from_string(final CharSequence s, int si, int ei, final FloatingPointStatusFlag pfpsf, int rnd_mode /*= BID_ROUNDING_TO_NEAREST*/) {

        long coefficient_x = 0, rounded = 0;
        int expon_x = 0, sgn_expon, ndigits, add_expon = 0, midpoint = 0, rounded_up = 0;
        int dec_expon_scale = 0;
        char c;

        { // rTrim
            c = ei - 1 >= si ? s.charAt(ei - 1) : '\0';
            while (Character.isWhitespace(c)) {
                ei--;
                c = ei - 1 >= si ? s.charAt(ei - 1) : '\0';
            }
        }
        { // lTrim
            c = si < ei ? s.charAt(si) : '\0';
            while (Character.isWhitespace(c)) {
                si++;
                c = si < ei ? s.charAt(si) : '\0';
            }
        }

        int ps = si;


        if (c == '\0') {
            pfpsf.status = BID_INVALID_FORMAT;
            return 0x7c00000000000000L;                    // return qNaN
        }


        // determine sign
        long sign_x = c == '-' ? 0x8000000000000000L : 0;
        // get next character if leading +/- sign
        if (c == '-' || c == '+') {
            ps++;
            c = ps < ei ? s.charAt(ps) : '\0';
            if (c == '\0') {
                pfpsf.status = BID_INVALID_FORMAT;
                return 0x7c00000000000000L;                    // return qNaN
            }
        }

        // detect special cases (INF or NaN)
        if (c != '.' && (c < '0' || c > '9')) {
            if (IsStrEqIgnoreCase(s, ps, ei, "inf") || IsStrEqIgnoreCase(s, ps, ei, "infinity")) // Infinity?
            {
                pfpsf.status = BID_EXACT_STATUS;
                return 0x7800000000000000L | sign_x;
            }
            // return sNaN
            if (IsStrEqIgnoreCase(s, ps, ei, "snan")) // case insensitive check for snan
            {
                pfpsf.status = BID_EXACT_STATUS;
                return 0x7e00000000000000L | sign_x;
            }
            if (IsStrEqIgnoreCase(s, ps, ei, "nan")) // return qNaN
            {
                pfpsf.status = BID_EXACT_STATUS;
                return 0x7c00000000000000L | sign_x;
            } else // if c isn't a decimal point or a decimal digit, return NaN
            {
                pfpsf.status = BID_INVALID_FORMAT;
                return 0x7c00000000000000L;                    // return qNaN
            }
        }

        int rdx_pt_enc = 0;
        int right_radix_leading_zeros = 0;

        // detect zero (and eliminate/ignore leading zeros)
        if (c == '0' || c == '.') {

            if (c == '.') {
                rdx_pt_enc = 1;
                ps++;
                c = ps < ei ? s.charAt(ps) : '\0';
            }
            // if all numbers are zeros (with possibly 1 radix point, the number is zero
            // should catch cases such as: 000.0
            while (c == '0') {
                ps++;
                c = ps < ei ? s.charAt(ps) : '\0';
                // for numbers such as 0.0000000000000000000000000000000000001001,
                // we want to count the leading zeros
                if (rdx_pt_enc != 0) {
                    right_radix_leading_zeros++;
                }
                // if this character is a radix point, make sure we haven't already
                // encountered one
                if (c == '.') {
                    if (rdx_pt_enc == 0) {
                        rdx_pt_enc = 1;
                        // if this is the first radix point, and the next character is NULL,
                        // we have a zero
                        char c1 = ps + 1 < ei ? s.charAt(ps + 1) : '\0';
                        if (c1 == '\0') {
                            pfpsf.status = BID_EXACT_STATUS;
                            return ZERO | sign_x; // ((BID_UINT64)(398 - right_radix_leading_zeros) << 53) | sign_x;
                        }
                        ps++;
                        c = ps < ei ? s.charAt(ps) : '\0';
                    } else {
                        pfpsf.status = BID_INVALID_FORMAT;
                        return NaN; // 0x7c00000000000000L | sign_x; // if 2 radix points, return NaN
                    }
                } else if (c == '\0') {
                    pfpsf.status = BID_EXACT_STATUS;
                    return ZERO | sign_x; // ((BID_UINT64)(398 - right_radix_leading_zeros) << 53) | sign_x; //pres->w[1] = 0x3040000000000000L | sign_x;
                }
            }
        }

        c = ps < ei ? s.charAt(ps) : '\0';

        ndigits = 0;
        while ((c >= '0' && c <= '9') || c == '.') {
            if (c == '.') {
                if (rdx_pt_enc != 0) {
                    pfpsf.status = BID_INVALID_FORMAT;
                    return NaN; // 0x7c00000000000000L | sign_x; // return NaN
                }
                rdx_pt_enc = 1;
                ps++;
                c = ps < ei ? s.charAt(ps) : '\0';
                continue;
            }
            dec_expon_scale += rdx_pt_enc;

            ndigits++;
            if (ndigits <= 16) {
                coefficient_x = (coefficient_x << 1) + (coefficient_x << 3);
                coefficient_x += (c - '0');
            } else if (ndigits == 17) {
                // coefficient rounding
                switch (rnd_mode) {
                    case BID_ROUNDING_TO_NEAREST:
                        midpoint = (c == '5' && (coefficient_x & 1) == 0) ? 1 : 0;
                        // if coefficient is even and c is 5, prepare to round up if
                        // subsequent digit is nonzero
                        // if str[MAXDIG+1] > 5, we MUST round up
                        // if str[MAXDIG+1] == 5 and coefficient is ODD, ROUND UP!
                        if (c > '5' || (c == '5' && (coefficient_x & 1) != 0)) {
                            coefficient_x++;
                            rounded_up = 1;
                        }
                        break;

                    case BID_ROUNDING_DOWN:
                        if (sign_x != 0) {
                            coefficient_x++;
                            rounded_up = 1;
                        }
                        break;
                    case BID_ROUNDING_UP:
                        if (sign_x == 0) {
                            coefficient_x++;
                            rounded_up = 1;
                        }
                        break;
                    case BID_ROUNDING_TIES_AWAY:
                        if (c >= '5') {
                            coefficient_x++;
                            rounded_up = 1;
                        }
                        break;
                }
                if (coefficient_x == 10000000000000000L) {
                    coefficient_x = 1000000000000000L;
                    add_expon = 1;
                }
                if (c > '0')
                    rounded = 1;
                add_expon += 1;
            } else { // ndigits > 17
                add_expon++;
                if (midpoint != 0 && c > '0') {
                    coefficient_x++;
                    midpoint = 0;
                    rounded_up = 1;
                }
                if (c > '0')
                    rounded = 1;
            }
            ps++;
            c = ps < ei ? s.charAt(ps) : '\0';
        }

        add_expon -= (dec_expon_scale + right_radix_leading_zeros);

        if (c == '\0') {
            pfpsf.status = BID_EXACT_STATUS;
            if (rounded != 0)
                __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);
            return /*fast_get_BID64_check_OF*/get_BID64(sign_x,
                add_expon + DECIMAL_EXPONENT_BIAS,
                coefficient_x, 0, pfpsf);
        }

        if (c != 'E' && c != 'e') {
            pfpsf.status = BID_INVALID_FORMAT;
            return NaN; // 0x7c00000000000000L | sign_x; // return NaN
        }
        ps++;
        c = ps < ei ? s.charAt(ps) : '\0';
        sgn_expon = (c == '-') ? 1 : 0;
        if (c == '-' || c == '+') {
            ps++;
            c = ps < ei ? s.charAt(ps) : '\0';
        }
        if (c == '\0' || c < '0' || c > '9') {
            pfpsf.status = BID_INVALID_FORMAT;
            return NaN; // 0x7c00000000000000L | sign_x; // return NaN
        }

        while ((c >= '0') && (c <= '9')) {
            if (expon_x < (1 << 20)) {
                expon_x = (expon_x << 1) + (expon_x << 3);
                expon_x += (int) (c - '0');
            }

            ps++;
            c = ps < ei ? s.charAt(ps) : '\0';
        }

        if (c != '\0') {
            pfpsf.status = BID_INVALID_FORMAT;
            return NaN; // 0x7c00000000000000L | sign_x; // return NaN
        }

        if (rounded != 0) {
            pfpsf.status = BID_EXACT_STATUS;
            __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);
        }

        if (sgn_expon != 0)
            expon_x = -expon_x;

        expon_x += add_expon + DECIMAL_EXPONENT_BIAS;

        if (expon_x < 0) {
            if (rounded_up != 0)
                coefficient_x--;
            rnd_mode = 0;
            pfpsf.status = BID_EXACT_STATUS;
            return get_BID64_UF(sign_x, expon_x, coefficient_x, rounded, rnd_mode, pfpsf);
        }
        pfpsf.status = BID_EXACT_STATUS;
        return get_BID64(sign_x, expon_x, coefficient_x, rnd_mode, pfpsf);
    }

    //
    //   no underflow checking
    //
    public static long fast_get_BID64_check_OF(final long sgn, int expon, long coeff, final int rmode, final FloatingPointStatusFlag fpsc) {
        long r, mask;

        if ((/*UnsignedInteger.compare*/(expon) + Integer.MIN_VALUE >= (3 * 256 - 1) + Integer.MIN_VALUE)) {
            if ((expon == 3 * 256 - 1) && coeff == 10000000000000000L) {
                expon = 3 * 256;
                coeff = 1000000000000000L;
            }

            if ((/*UnsignedInteger.compare*/(expon) + Integer.MIN_VALUE >= (3 * 256) + Integer.MIN_VALUE)) {
                while (coeff < 1000000000000000L && expon >= 3 * 256) {
                    expon--;
                    coeff = (coeff << 3) + (coeff << 1);
                }
                if (expon > DECIMAL_MAX_EXPON_64) {
                    __set_status_flags(fpsc,
                        BID_OVERFLOW_EXCEPTION | BID_INEXACT_EXCEPTION);
                    // overflow
                    r = sgn | INFINITY_MASK64;
                    switch (rmode) {
                        case BID_ROUNDING_DOWN:
                            if (sgn == 0)
                                r = LARGEST_BID64;
                            break;
                        case BID_ROUNDING_TO_ZERO:
                            r = sgn | LARGEST_BID64;
                            break;
                        case BID_ROUNDING_UP:
                            // round up
                            if (sgn != 0)
                                r = SMALLEST_BID64;
                            break;
                    }
                    return r;
                }
            }
        }

        mask = 1;
        mask <<= EXPONENT_SHIFT_SMALL64;

        // check whether coefficient fits in 10*5+3 bits
        if ((UnsignedLong.isLess(coeff, mask))) {
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

    public static class BID_UINT128 {
        public BID_UINT128(final long w0, final long w1) {
            this.w0 = w0;
            this.w1 = w1;
        }

        public long w0, w1;
    }

    public static long[] /*BID_UINT128*/ bid_reciprocals10_128_BID_UINT128 = {
        0L, 0L,    // 0 extra digits
        0x3333333333333334L, 0x3333333333333333L,    // 1 extra digit
        0x51eb851eb851eb86L, 0x051eb851eb851eb8L,    // 2 extra digits
        0x3b645a1cac083127L, 0x0083126e978d4fdfL,    // 3 extra digits
        0x4af4f0d844d013aaL, 0x00346dc5d6388659L,    //  10^(-4) * 2^131
        0x08c3f3e0370cdc88L, 0x0029f16b11c6d1e1L,    //  10^(-5) * 2^134
        0x6d698fe69270b06dL, 0x00218def416bdb1aL,    //  10^(-6) * 2^137
        0xaf0f4ca41d811a47L, 0x0035afe535795e90L,    //  10^(-7) * 2^141
        0xbf3f70834acdaea0L, 0x002af31dc4611873L,    //  10^(-8) * 2^144
        0x65cc5a02a23e254dL, 0x00225c17d04dad29L,    //  10^(-9) * 2^147
        0x6fad5cd10396a214L, 0x0036f9bfb3af7b75L,    // 10^(-10) * 2^151
        0xbfbde3da69454e76L, 0x002bfaffc2f2c92aL,    // 10^(-11) * 2^154
        0x32fe4fe1edd10b92L, 0x00232f33025bd422L,    // 10^(-12) * 2^157
        0x84ca19697c81ac1cL, 0x00384b84d092ed03L,    // 10^(-13) * 2^161
        0x03d4e1213067bce4L, 0x002d09370d425736L,    // 10^(-14) * 2^164
        0x3643e74dc052fd83L, 0x0024075f3dceac2bL,    // 10^(-15) * 2^167
        0x56d30baf9a1e626bL, 0x0039a5652fb11378L,    // 10^(-16) * 2^171
        0x12426fbfae7eb522L, 0x002e1dea8c8da92dL,    // 10^(-17) * 2^174
        0x41cebfcc8b9890e8L, 0x0024e4bba3a48757L,    // 10^(-18) * 2^177
        0x694acc7a78f41b0dL, 0x003b07929f6da558L,    // 10^(-19) * 2^181
        0xbaa23d2ec729af3eL, 0x002f394219248446L,    // 10^(-20) * 2^184
        0xfbb4fdbf05baf298L, 0x0025c768141d369eL,    // 10^(-21) * 2^187
        0x2c54c931a2c4b759L, 0x003c7240202ebdcbL,    // 10^(-22) * 2^191
        0x89dd6dc14f03c5e1L, 0x00305b66802564a2L,    // 10^(-23) * 2^194
        0xd4b1249aa59c9e4eL, 0x0026af8533511d4eL,    // 10^(-24) * 2^197
        0x544ea0f76f60fd49L, 0x003de5a1ebb4fbb1L,    // 10^(-25) * 2^201
        0x76a54d92bf80caa1L, 0x00318481895d9627L,    // 10^(-26) * 2^204
        0x921dd7a89933d54eL, 0x00279d346de4781fL,    // 10^(-27) * 2^207
        0x8362f2a75b862215L, 0x003f61ed7ca0c032L,    // 10^(-28) * 2^211
        0xcf825bb91604e811L, 0x0032b4bdfd4d668eL,    // 10^(-29) * 2^214
        0x0c684960de6a5341L, 0x00289097fdd7853fL,    // 10^(-30) * 2^217
        0x3d203ab3e521dc34L, 0x002073accb12d0ffL,    // 10^(-31) * 2^220
        0x2e99f7863b696053L, 0x0033ec47ab514e65L,    // 10^(-32) * 2^224
        0x587b2c6b62bab376L, 0x002989d2ef743eb7L,    // 10^(-33) * 2^227
        0xad2f56bc4efbc2c5L, 0x00213b0f25f69892L,    // 10^(-34) * 2^230
        0x0f2abc9d8c9689d1L, 0x01a95a5b7f87a0efL,    // 35 extra digits
    };

    public static int[] bid_recip_scale = {
        129 - 128,    // 1
        129 - 128,    // 1/10
        129 - 128,    // 1/10^2
        129 - 128,    // 1/10^3
        3,    // 131 - 128
        6,    // 134 - 128
        9,    // 137 - 128
        13,    // 141 - 128
        16,    // 144 - 128
        19,    // 147 - 128
        23,    // 151 - 128
        26,    // 154 - 128
        29,    // 157 - 128
        33,    // 161 - 128
        36,    // 164 - 128
        39,    // 167 - 128
        43,    // 171 - 128
        46,    // 174 - 128
        49,    // 177 - 128
        53,    // 181 - 128
        56,    // 184 - 128
        59,    // 187 - 128
        63,    // 191 - 128

        66,    // 194 - 128
        69,    // 197 - 128
        73,    // 201 - 128
        76,    // 204 - 128
        79,    // 207 - 128
        83,    // 211 - 128
        86,    // 214 - 128
        89,    // 217 - 128
        92,    // 220 - 128
        96,    // 224 - 128
        99,    // 227 - 128
        102,    // 230 - 128
        109,    // 237 - 128, 1/10^35
    };

    //
    // This pack macro is used when underflow is known to occur
    //
    public static long get_BID64_UF(final long sgn, final int expon, long coeff, final long R, int rmode, final FloatingPointStatusFlag fpsc) {
        long C128_w0, C128_w1, Q_low_w0, Q_low_w1, Stemp_w0, Stemp_w1;
        long _C64, remainder_h, QH, carry;
        int extra_digits, amount, amount2;
        int status;

        // underflow
        if (expon + MAX_FORMAT_DIGITS < 0) {
            __set_status_flags(fpsc, BID_UNDERFLOW_EXCEPTION | BID_INEXACT_EXCEPTION);
            if (rmode == BID_ROUNDING_DOWN && sgn != 0)
                return 0x8000000000000001L;
            if (rmode == BID_ROUNDING_UP && sgn == 0)
                return 1L;
            // result is 0
            return sgn;
        }
        // 10*coeff
        coeff = (coeff << 3) + (coeff << 1);
        if (sgn != 0 && (rmode == BID_ROUNDING_DOWN || rmode == BID_ROUNDING_UP) /*(uint)(rmode - 1) < 2*/)
            rmode = 3 - rmode;
        if (R != 0)
            coeff |= 1;
        // get digits to be shifted out
        extra_digits = 1 - expon;
        C128_w0 = coeff + bid_round_const_table[rmode][extra_digits];

        // get coeff*(2^M[extra_digits])/10^extra_digits
        //__mul_64x128_full(out QH, out Q_low, C128.w0, bid_reciprocals10_128_flat[extra_digits << 1], bid_reciprocals10_128_flat[(extra_digits << 1) + 1]);
        //public static void __mul_64x128_full(/*out*/ long Ph, /*out*/ final BID_UINT128 Ql, final long A, final long B_w0, final long B_w1)
        {
            final long _B_w0 = bid_reciprocals10_128_BID_UINT128[extra_digits << 1];
            final long _B_w1 = bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1];

            long _ALBL_w0, _ALBL_w1, _ALBH_w0, _ALBH_w1, _QM2_w0, _QM2_w1;

            //__mul_64x64_to_128(out ALBH, A, B.w1);
            {
                long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
                __CXH = C128_w0 >>> 32;
                __CXL = LONG_LOW_PART & C128_w0;
                __CYH = _B_w1 >>> 32;
                __CYL = LONG_LOW_PART & _B_w1;

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
                long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
                __CXH = C128_w0 >>> 32;
                __CXL = LONG_LOW_PART & C128_w0;
                __CYH = _B_w0 >>> 32;
                __CYL = LONG_LOW_PART & _B_w0;

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
                long R64H;
                R64H = _ALBH_w1;
                _QM2_w0 = (_ALBL_w1) + _ALBH_w0;
                if ((UnsignedLong.isLess(_QM2_w0, _ALBL_w1)))
                    R64H++;
                _QM2_w1 = R64H;
            }
            Q_low_w1 = _QM2_w0;
            QH = _QM2_w1;
        }

        // now get P/10^extra_digits: shift Q_high right by M[extra_digits]-128
        amount = bid_recip_scale[extra_digits];

        _C64 = QH >>> amount;
        //__shr_128(C128, Q_high, amount);

        if (rmode == 0) //BID_ROUNDING_TO_NEAREST
            if ((_C64 & 1) != 0) {
                // check whether fractional part of initial_P/10^extra_digits is exactly .5

                // get remainder
                amount2 = 64 - amount;
                remainder_h = 0;
                remainder_h--;
                remainder_h >>>= amount2;
                remainder_h = remainder_h & QH;

                if (remainder_h == 0
                    && ((UnsignedLong.isLess(Q_low_w1, bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1]))
                    || (Q_low_w1 == bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1]
                    && (UnsignedLong.isLess(Q_low_w0, bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) /*+ 0*/]))))) {
                    _C64--;
                }
            }


        if (is_inexact(fpsc))
            __set_status_flags(fpsc, BID_UNDERFLOW_EXCEPTION);
        else {
            status = BID_INEXACT_EXCEPTION;
            // get remainder
            remainder_h = QH << (64 - amount);

            switch (rmode) {
                case BID_ROUNDING_TO_NEAREST:
                case BID_ROUNDING_TIES_AWAY:
                    // test whether fractional part is 0
                    if (remainder_h == 0x8000000000000000L
                        && ((UnsignedLong.isLess(Q_low_w1, bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1]))
                        || (Q_low_w1 == bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1]
                        && (UnsignedLong.isLess(Q_low_w0, bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) /*+ 0*/])))))
                        status = BID_EXACT_STATUS;
                    break;
                case BID_ROUNDING_DOWN:
                case BID_ROUNDING_TO_ZERO:
                    if (remainder_h == 0
                        && ((UnsignedLong.isLess(Q_low_w1, bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1]))
                        || (Q_low_w1 == bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1]
                        && (UnsignedLong.isLess(Q_low_w0, bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) /*+ 0*/])))))
                        status = BID_EXACT_STATUS;
                    break;
                default:
                    // round up
                    long CY;
                    //__add_carry_out(out Stemp_w0, out CY, Q_low_w0, bid_reciprocals10_128_flat[(extra_digits << 1) + 0]);
                {
                    final long __Y = bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) /*+ 0*/];

                    Stemp_w0 = Q_low_w0 + __Y;
                    CY = (UnsignedLong.isLess(Stemp_w0, Q_low_w0)) ? 1L : 0;
                }
                //__add_carry_in_out(out Stemp_w1, out carry, Q_low_w1, bid_reciprocals10_128_flat[(extra_digits << 1) + 1], CY);
                {
                    final long __Y = bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1];

                    final long __X1;
                    __X1 = Q_low_w1 + CY;
                    Stemp_w1 = __X1 + __Y;
                    carry = ((UnsignedLong.isLess(Stemp_w1, __X1)) || (UnsignedLong.isLess(__X1, CY))) ? 1L : 0;
                }

                if ((UnsignedLong.isGreaterOrEqual((remainder_h >>> (64 - amount)) + carry, 1L << amount)))
                    status = BID_EXACT_STATUS;
                break;
            }

            if (status != BID_EXACT_STATUS)
                __set_status_flags(fpsc, BID_UNDERFLOW_EXCEPTION | status);
        }


        return sgn | _C64;

    }


    //
    //   BID64 pack macro (general form)
    //
    public static long get_BID64(long sgn, int expon, long coeff, int rmode, FloatingPointStatusFlag fpsc) {
        long Stemp_w0, Stemp_w1, Q_low_w0, Q_low_w1;
        long QH, r, mask, _C64, remainder_h, carry;
        int extra_digits, amount, amount2;
        int status;

        if ((UnsignedLong.isGreater(coeff, 9999999999999999L))) {
            expon++;
            coeff = 1000000000000000L;
        }
        // check for possible underflow/overflow
        if ((/*UnsignedInteger.compare*/(expon) + Integer.MIN_VALUE >= (3 * 256) + Integer.MIN_VALUE)) {
            if (expon < 0) {
                // underflow
                if (expon + MAX_FORMAT_DIGITS < 0) {
                    __set_status_flags(fpsc, BID_UNDERFLOW_EXCEPTION | BID_INEXACT_EXCEPTION);
                    if (rmode == BID_ROUNDING_DOWN && sgn != 0)
                        return 0x8000000000000001L;
                    if (rmode == BID_ROUNDING_UP && sgn == 0)
                        return 1L;
                    // result is 0
                    return sgn;
                }
                if (sgn != 0 && (rmode == BID_ROUNDING_DOWN || rmode == BID_ROUNDING_UP) /*(uint)(rmode - 1) < 2*/)
                    rmode = 3 - rmode;
                // get digits to be shifted out
                extra_digits = -expon;
                coeff += bid_round_const_table[rmode][extra_digits];

                // get coeff*(2^M[extra_digits])/10^extra_digits
                //__mul_64x128_full(out QH, out Q_low, coeff, bid_reciprocals10_128_flat[extra_digits << 1], bid_reciprocals10_128_flat[(extra_digits << 1) + 1]);
                //public static void __mul_64x128_full(/*out*/ long Ph, /*out*/ final BID_UINT128 Ql, final long A, final long B_w0, final long B_w1)
                {

                    final long _B_w0 = bid_reciprocals10_128_BID_UINT128[extra_digits << 1];
                    final long _B_w1 = bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1];

                    long _ALBL_w0, _ALBL_w1, _ALBH_w0, _ALBH_w1, _QM2_w0, _QM2_w1;

                    //__mul_64x64_to_128(out ALBH, A, B.w1);
                    {
                        long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
                        __CXH = coeff >>> 32;
                        __CXL = LONG_LOW_PART & coeff;
                        __CYH = _B_w1 >>> 32;
                        __CYL = LONG_LOW_PART & _B_w1;

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
                        long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
                        __CXH = coeff >>> 32;
                        __CXL = LONG_LOW_PART & coeff;
                        __CYH = _B_w0 >>> 32;
                        __CYL = LONG_LOW_PART & _B_w0;

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
                        long R64H;
                        R64H = _ALBH_w1;
                        _QM2_w0 = (_ALBL_w1) + _ALBH_w0;
                        if ((UnsignedLong.isLess(_QM2_w0, _ALBL_w1)))
                            R64H++;
                        _QM2_w1 = R64H;
                    }
                    Q_low_w1 = _QM2_w0;
                    QH = _QM2_w1;
                }

                // now get P/10^extra_digits: shift Q_high right by M[extra_digits]-128
                amount = bid_recip_scale[extra_digits];

                _C64 = QH >>> amount;

                if (rmode == 0) //BID_ROUNDING_TO_NEAREST
                    if ((_C64 & 1) != 0) {
                        // check whether fractional part of initial_P/10^extra_digits is exactly .5

                        // get remainder
                        amount2 = 64 - amount;
                        remainder_h = 0;
                        remainder_h--;
                        remainder_h >>>= amount2;
                        remainder_h = remainder_h & QH;

                        if (remainder_h == 0
                            && ((UnsignedLong.isLess(Q_low_w1, bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1]))
                            || (Q_low_w1 == bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1]
                            && (UnsignedLong.isLess(Q_low_w0, bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) /*+ 0*/]))))) {
                            _C64--;
                        }
                    }


                if (is_inexact(fpsc))
                    __set_status_flags(fpsc, BID_UNDERFLOW_EXCEPTION);
                else {
                    status = BID_INEXACT_EXCEPTION;
                    // get remainder
                    remainder_h = QH << (64 - amount);

                    switch (rmode) {
                        case BID_ROUNDING_TO_NEAREST:
                        case BID_ROUNDING_TIES_AWAY:
                            // test whether fractional part is 0
                            if (remainder_h == 0x8000000000000000L
                                && ((UnsignedLong.isLess(Q_low_w1, bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1]))
                                || (Q_low_w1 == bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1]
                                && (UnsignedLong.isLess(Q_low_w0, bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) /*+ 0*/])))))
                                status = BID_EXACT_STATUS;
                            break;
                        case BID_ROUNDING_DOWN:
                        case BID_ROUNDING_TO_ZERO:
                            if (remainder_h == 0
                                && ((UnsignedLong.isLess(Q_low_w1, bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1])))
                                || (Q_low_w1 == bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1]
                                && (UnsignedLong.isLess(Q_low_w0, bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) /*+ 0*/]))))
                                status = BID_EXACT_STATUS;
                            break;
                        default:
                            // round up
                            long CY;
                            //__add_carry_out(out Stemp_w0, out CY, Q_low_w0, bid_reciprocals10_128_flat[(extra_digits << 1) + 0]);
                        {
                            final long __Y = bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) /*+ 0*/];

                            Stemp_w0 = Q_low_w0 + __Y;
                            CY = (UnsignedLong.isLess(Stemp_w0, Q_low_w0)) ? 1L : 0;
                        }
                        //__add_carry_in_out(out Stemp_w1, out carry, Q_low_w1, bid_reciprocals10_128_flat[(extra_digits << 1) + 1], CY);
                        {
                            final long __Y = bid_reciprocals10_128_BID_UINT128[(extra_digits << 1) + 1];

                            final long __X1;
                            __X1 = Q_low_w1 + CY;
                            Stemp_w1 = __X1 + __Y;
                            carry = ((UnsignedLong.isLess(Stemp_w1, __X1)) || (UnsignedLong.isLess(__X1, CY))) ? 1L : 0;
                        }

                        if ((UnsignedLong.isGreaterOrEqual((remainder_h >>> (64 - amount)) + carry, 1L << amount)))
                            status = BID_EXACT_STATUS;
                        break;
                    }

                    if (status != BID_EXACT_STATUS)
                        __set_status_flags(fpsc, BID_UNDERFLOW_EXCEPTION | status);
                }


                return sgn | _C64;
            }
            if (coeff == 0) {
                if (expon > DECIMAL_MAX_EXPON_64) expon = DECIMAL_MAX_EXPON_64;
            }
            while ((UnsignedLong.isLess(coeff, 1000000000000000L)) && expon >= 3 * 256) {
                expon--;
                coeff = (coeff << 3) + (coeff << 1);
            }
            if (expon > DECIMAL_MAX_EXPON_64) {
                __set_status_flags(fpsc, BID_OVERFLOW_EXCEPTION | BID_INEXACT_EXCEPTION);
                // overflow
                r = sgn | INFINITY_MASK64;
                switch (rmode) {
                    case BID_ROUNDING_DOWN:
                        if (sgn == 0)
                            r = LARGEST_BID64;
                        break;
                    case BID_ROUNDING_TO_ZERO:
                        r = sgn | LARGEST_BID64;
                        break;
                    case BID_ROUNDING_UP:
                        // round up
                        if (sgn != 0)
                            r = SMALLEST_BID64;
                        break;
                }
                return r;
            }
        }

        mask = 1;
        mask <<= EXPONENT_SHIFT_SMALL64;

        // check whether coefficient fits in 10*5+3 bits
        if ((UnsignedLong.isLess(coeff, mask))) {
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
}
