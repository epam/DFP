package com.epam.deltix.dfp;

import static com.epam.deltix.dfp.JavaImplAdd.*;
import static com.epam.deltix.dfp.JavaImplCmp.*;

class JavaImplCast {
    private JavaImplCast() {
    }

    public static long /*BID_SINT64*/ bid64_to_int64_xint(final long /*BID_UINT64*/ x /*, final JavaImplParse.FloatingPointStatusFlag pfpsf*/) {
        long /*BID_SINT64*/ res;
        long /*BID_UINT64*/ x_sign;
        long /*BID_UINT64*/ x_exp;
        int exp;            // unbiased exponent
        // Note: C1 represents x_significand (BID_UINT64)
        //BID_UI64DOUBLE tmp1;
        /*unsigned*/
        int x_nr_bits;
        int q, ind, shift;
        long /*BID_UINT64*/ C1;
        long /*BID_UINT128*/ C_w0, C_w1;
        long /*BID_UINT64*/ Cstar;            // C* represents up to 16 decimal digits ~ 54 bits
//        long /*BID_UINT128*/ fstar_w0, fstar_w1;                                            //@XINT
        long /*BID_UINT128*/ P128_w0, P128_w1;

        // check for NaN or Infinity
        if ((x & MASK_NAN) == MASK_NAN || (x & MASK_INF) == MASK_INF) {
            // set invalid flag
//            __set_status_flags(pfpsf, BID_INVALID_EXCEPTION);
            // return Integer Indefinite
            return 0x8000000000000000L;
        }
        // unpack x
        x_sign = x & MASK_SIGN;    // 0 for positive, MASK_SIGN for negative
        // if steering bits are 11 (condition will be 0), then exponent is G[0:w+1] =>
        if ((x & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            x_exp = (x & MASK_BINARY_EXPONENT2) >>> 51;    // biased
            C1 = (x & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
            if (C1 > 9999999999999999L) {    // non-canonical
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
            return 0x00000000;
        }
        // x is not special and is not zero

        // q = nr. of decimal digits in x (1 <= q <= 54)
        //  determine first the nr. of bits in x
        if (UnsignedLong.isGreaterOrEqual(C1, 0x0020000000000000L)) {    // x >= 2^53
            // split the 64-bit value in two 32-bit halves to avoid rounding errors
            final long tmp1_ui64 = Double.doubleToRawLongBits((double) (C1 >>> 32));    // exact conversion

            x_nr_bits = 33 + ((((/*unsigned*/ int) (tmp1_ui64 >>> 52)) & 0x7ff) - 0x3ff);
        } else {    // if x < 2^53
            final long tmp1_ui64 = Double.doubleToRawLongBits((double) C1);    // exact conversion
            x_nr_bits = 1 + ((((/*unsigned*/ int) (tmp1_ui64 >>> 52)) & 0x7ff) - 0x3ff);
        }
        q = (int) bid_nr_digits_flat[((x_nr_bits - 1) << 2) /*+ 0 .digits*/];
        if (q == 0) {
            q = (int) bid_nr_digits_flat[((x_nr_bits - 1) << 2) + 3 /*.digits1*/];
            if (UnsignedLong.isGreaterOrEqual(C1, bid_nr_digits_flat[((x_nr_bits - 1) << 2) + 2/*.threshold_lo*/]))
                q++;
        }
        exp = (int) (x_exp - 398);    // unbiased exponent

        if ((q + exp) > 19) {    // x >= 10^19 ~= 2^63.11... (cannot fit in BID_SINT64)
            // set invalid flag
//            __set_status_flags(pfpsf, BID_INVALID_EXCEPTION);
            // return Integer Indefinite
            return 0x8000000000000000L;
        } else if ((q + exp) == 19) {    // x = c(0)c(1)...c(18).c(19)...c(q-1)
            // in this case 2^63.11... ~= 10^19 <= x < 10^20 ~= 2^66.43...
            // so x rounded to an integer may or may not fit in a signed 64-bit int
            // the cases that do not fit are identified here; the ones that fit
            // fall through and will be handled with other cases further,
            // under '1 <= q + exp <= 19'
            if (x_sign != 0) {    // if n < 0 and q + exp = 19
                // if n <= -2^63 - 1 then n is too large
                // too large if c(0)c(1)...c(18).c(19)...c(q-1) >= 2^63+1
                // <=> 0.c(0)c(1)...c(q-1) * 10^20 >= 5*(2^64+2), 1<=q<=16
                // <=> 0.c(0)c(1)...c(q-1) * 10^20 >= 0x5000000000000000a, 1<=q<=16
                // <=> C * 10^(20-q) >= 0x5000000000000000a, 1<=q<=16
                // 1 <= q <= 16 => 4 <= 20-q <= 19 => 10^(20-q) is 64-bit, and so is C1

                //__mul_64x64_to_128MACH(C, C1, bid_ten2k64[20 - q]);
                {
                    final long __CY = bid_ten2k64[20 - q];
                    C_w1 = Mul64Impl.unsignedMultiplyHigh(C1, __CY);
                    C_w0 = C1 * __CY;
                }

                // Note: C1 * 10^(11-q) has 19 or 20 digits; 0x5000000000000000a, has 20
                if (UnsignedLong.isGreater(C_w1, 0x05L) || (C_w1 == 0x05L && UnsignedLong.isGreaterOrEqual(C_w0, 0x0aL))) {
                    // set invalid flag
//                    __set_status_flags(pfpsf, BID_INVALID_EXCEPTION);
                    // return Integer Indefinite
                    return 0x8000000000000000L;
                }
                // else cases that can be rounded to a 64-bit int fall through
                // to '1 <= q + exp <= 19'
            } else {    // if n > 0 and q + exp = 19
                // if n >= 2^63 then n is too large
                // too large if c(0)c(1)...c(18).c(19)...c(q-1) >= 2^63
                // <=> if 0.c(0)c(1)...c(q-1) * 10^20 >= 5*2^64, 1<=q<=16
                // <=> if 0.c(0)c(1)...c(q-1) * 10^20 >= 0x50000000000000000, 1<=q<=16
                // <=> if C * 10^(20-q) >= 0x50000000000000000, 1<=q<=16
                C_w1 = 0x0000000000000005L;
                C_w0 = 0x0000000000000000L;
                // 1 <= q <= 16 => 4 <= 20-q <= 19 => 10^(20-q) is 64-bit, and so is C1

                //__mul_64x64_to_128MACH(C, C1, bid_ten2k64[20 - q]);
                {
                    final long __CY = bid_ten2k64[20 - q];
                    C_w1 = Mul64Impl.unsignedMultiplyHigh(C1, __CY);
                    C_w0 = C1 * __CY;
                }

                if (UnsignedLong.isGreaterOrEqual(C_w1, 0x05L)) {
                    // actually C.w[1] == 0x05ull && C.w[0] >= 0x0000000000000000ull) {
                    // set invalid flag
//                    __set_status_flags(pfpsf, BID_INVALID_EXCEPTION);
                    // return Integer Indefinite
                    return 0x8000000000000000L;
                }
                // else cases that can be rounded to a 64-bit int fall through
                // to '1 <= q + exp <= 19'
            }    // end else if n > 0 and q + exp = 19
        }    // end else if ((q + exp) == 19)

        // n is not too large to be converted to int64: -2^63-1 < n < 2^63
        // Note: some of the cases tested for above fall through to this point
        if ((q + exp) <= 0) {    // n = +/-0.0...c(0)c(1)...c(q-1)
            // set inexact flag                                                             //@XINT
//            __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);                               //@XINT
            // return 0
            return 0x0000000000000000L;
        } else {    // if (1 <= q + exp <= 19, 1 <= q <= 16, -15 <= exp <= 18)
            // -2^63-1 < x <= -1 or 1 <= x < 2^63 so x can be rounded
            // to nearest to a 64-bit signed integer
            if (exp < 0) {    // 2 <= q <= 16, -15 <= exp <= -1, 1 <= q + exp <= 19
                ind = -exp;    // 1 <= ind <= 15; ind is a synonym for 'x'
                // chop off ind digits from the lower part of C1
                // C1 fits in 64 bits
                // calculate C* and f*
                // C* is actually floor(C*) in this case
                // C* and f* need shifting and masking, as shown by
                // bid_shiftright128[] and bid_maskhigh128[]
                // 1 <= x <= 15
                // kx = 10^(-x) = bid_ten2mk64[ind - 1]
                // C* = C1 * 10^(-x)
                // the approximation of 10^(-x) was rounded up to 54 bits

                //__mul_64x64_to_128MACH(P128, C1, bid_ten2mk64[ind - 1]);
                {
                    final long __CY = bid_ten2mk64[ind - 1];
                    P128_w1 = Mul64Impl.unsignedMultiplyHigh(C1, __CY);
                    P128_w0 = C1 * __CY;
                }

                Cstar = P128_w1;
//                fstar_w1 = P128_w1 & bid_maskhigh128[ind - 1];                              //@XINT
//                fstar_w0 = P128_w0;                                                         //@XINT
                // the top Ex bits of 10^(-x) are T* = bid_ten2mk128trunc[ind].w[0], e.g.
                // if x=1, T*=bid_ten2mk128trunc[0].w[0]=0x1999999999999999
                // C* = floor(C*) (logical right shift; C has p decimal digits,
                //     correct by Property 1)
                // n = C* * 10^(e+x)

                // shift right C* by Ex-64 = bid_shiftright128[ind]
                shift = bid_shiftright128[ind - 1];    // 0 <= shift <= 39
                Cstar = Cstar >>> shift;

                // determine inexactness of the rounding of C*                              //@XINT
                // if (0 < f* < 10^(-x)) then                                               //@XINT
                //   the result is exact                                                    //@XINT
                // else // if (f* > T*) then                                                //@XINT
                //   the result is inexact                                                  //@XINT
//                if (ind - 1 <= 2) {    // fstar.w[1] is 0                                   //@XINT
//                    if (UnsignedLong.isGreater(fstar_w0, bid_ten2mk128trunc_flat[((ind - 1) << 1) + 1])) { //@XINT
//                        // bid_ten2mk128trunc[ind -1].w[1] is identical to                  //@XINT
//                        // bid_ten2mk128[ind -1].w[1]                                       //@XINT
//                        // set the inexact flag                                             //@XINT
//                        __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);                   //@XINT
//                    }    // else the result is exact                                        //@XINT
//                } else {    // if 3 <= ind - 1 <= 14                                        //@XINT
//                    if (fstar_w1 != 0 || UnsignedLong.isGreater(fstar_w0, bid_ten2mk128trunc_flat[((ind - 1) << 1) + 1])) { //@XINT
//                        // bid_ten2mk128trunc[ind -1].w[1] is identical to                  //@XINT
//                        // bid_ten2mk128[ind -1].w[1]                                       //@XINT
//                        // set the inexact flag                                             //@XINT
//                        __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);                   //@XINT
//                    }    // else the result is exact                                        //@XINT
//                }                                                                           //@XINT

                if (x_sign != 0)
                    res = -Cstar;
                else
                    res = Cstar;
            } else if (exp == 0) {
                // 1 <= q <= 16
                // res = +/-C (exact)
                if (x_sign != 0)
                    res = -C1;
                else
                    res = C1;
            } else {    // if (exp > 0) => 1 <= exp <= 18, 1 <= q <= 16, 2 <= q + exp <= 20
                // (the upper limit of 20 on q + exp is due to the fact that
                // +/-C * 10^exp is guaranteed to fit in 64 bits)
                // res = +/-C * 10^exp (exact)
                if (x_sign != 0)
                    res = -C1 * bid_ten2k64[exp];
                else
                    res = C1 * bid_ten2k64[exp];
            }
        }
        return res;
    }

    // the first entry of bid_nr_digits[i - 1] (where 1 <= i <= 113), indicates
    // the number of decimal digits needed to represent a binary number with i bits;
    // however, if a binary number of i bits may require either k or k + 1 decimal
    // digits, then the first entry of bid_nr_digits[i - 1] is 0; in this case if the
    // number is less than the value represented by the second and third entries
    // concatenated, then the number of decimal digits k is the fourth entry, else
    // the number of decimal digits is the fourth entry plus 1
    //     typedef struct _DEC_DIGITS {
    //       unsigned int digits;
    //       BID_UINT64 threshold_hi;
    //       BID_UINT64 threshold_lo;
    //       unsigned int digits1;
    //     } DEC_DIGITS;
    final static long[] /*DEC_DIGITS*/ bid_nr_digits_flat = {    // only the first entry is used if it is not 0
        1, 0x0000000000000000L, 0x000000000000000aL, 1,    //   1-bit n < 10^1
        1, 0x0000000000000000L, 0x000000000000000aL, 1,    //   2-bit n < 10^1
        1, 0x0000000000000000L, 0x000000000000000aL, 1,    //   3-bit n < 10^1
        0, 0x0000000000000000L, 0x000000000000000aL, 1,    //   4-bit n ? 10^1
        2, 0x0000000000000000L, 0x0000000000000064L, 2,    //   5-bit n < 10^2
        2, 0x0000000000000000L, 0x0000000000000064L, 2,    //   6-bit n < 10^2
        0, 0x0000000000000000L, 0x0000000000000064L, 2,    //   7-bit n ? 10^2
        3, 0x0000000000000000L, 0x00000000000003e8L, 3,    //   8-bit n < 10^3
        3, 0x0000000000000000L, 0x00000000000003e8L, 3,    //   9-bit n < 10^3
        0, 0x0000000000000000L, 0x00000000000003e8L, 3,    //  10-bit n ? 10^3
        4, 0x0000000000000000L, 0x0000000000002710L, 4,    //  11-bit n < 10^4
        4, 0x0000000000000000L, 0x0000000000002710L, 4,    //  12-bit n < 10^4
        4, 0x0000000000000000L, 0x0000000000002710L, 4,    //  13-bit n < 10^4
        0, 0x0000000000000000L, 0x0000000000002710L, 4,    //  14-bit n ? 10^4
        5, 0x0000000000000000L, 0x00000000000186a0L, 5,    //  15-bit n < 10^5
        5, 0x0000000000000000L, 0x00000000000186a0L, 5,    //  16-bit n < 10^5
        0, 0x0000000000000000L, 0x00000000000186a0L, 5,    //  17-bit n ? 10^5
        6, 0x0000000000000000L, 0x00000000000f4240L, 6,    //  18-bit n < 10^6
        6, 0x0000000000000000L, 0x00000000000f4240L, 6,    //  19-bit n < 10^6
        0, 0x0000000000000000L, 0x00000000000f4240L, 6,    //  20-bit n ? 10^6
        7, 0x0000000000000000L, 0x0000000000989680L, 7,    //  21-bit n < 10^7
        7, 0x0000000000000000L, 0x0000000000989680L, 7,    //  22-bit n < 10^7
        7, 0x0000000000000000L, 0x0000000000989680L, 7,    //  23-bit n < 10^7
        0, 0x0000000000000000L, 0x0000000000989680L, 7,    //  24-bit n ? 10^7
        8, 0x0000000000000000L, 0x0000000005f5e100L, 8,    //  25-bit n < 10^8
        8, 0x0000000000000000L, 0x0000000005f5e100L, 8,    //  26-bit n < 10^8
        0, 0x0000000000000000L, 0x0000000005f5e100L, 8,    //  27-bit n ? 10^8
        9, 0x0000000000000000L, 0x000000003b9aca00L, 9,    //  28-bit n < 10^9
        9, 0x0000000000000000L, 0x000000003b9aca00L, 9,    //  29-bit n < 10^9
        0, 0x0000000000000000L, 0x000000003b9aca00L, 9,    //  30-bit n ? 10^9
        10, 0x0000000000000000L, 0x00000002540be400L, 10,  //  31-bit n < 10^10
        10, 0x0000000000000000L, 0x00000002540be400L, 10,  //  32-bit n < 10^10
        10, 0x0000000000000000L, 0x00000002540be400L, 10,  //  33-bit n < 10^10
        0, 0x0000000000000000L, 0x00000002540be400L, 10,   //  34-bit n ? 10^10
        11, 0x0000000000000000L, 0x000000174876e800L, 11,  //  35-bit n < 10^11
        11, 0x0000000000000000L, 0x000000174876e800L, 11,  //  36-bit n < 10^11
        0, 0x0000000000000000L, 0x000000174876e800L, 11,   //  37-bit n ? 10^11
        12, 0x0000000000000000L, 0x000000e8d4a51000L, 12,  //  38-bit n < 10^12
        12, 0x0000000000000000L, 0x000000e8d4a51000L, 12,  //  39-bit n < 10^12
        0, 0x0000000000000000L, 0x000000e8d4a51000L, 12,   //  40-bit n ? 10^12
        13, 0x0000000000000000L, 0x000009184e72a000L, 13,  //  41-bit n < 10^13
        13, 0x0000000000000000L, 0x000009184e72a000L, 13,  //  42-bit n < 10^13
        13, 0x0000000000000000L, 0x000009184e72a000L, 13,  //  43-bit n < 10^13
        0, 0x0000000000000000L, 0x000009184e72a000L, 13,   //  44-bit n ? 10^13
        14, 0x0000000000000000L, 0x00005af3107a4000L, 14,  //  45-bit n < 10^14
        14, 0x0000000000000000L, 0x00005af3107a4000L, 14,  //  46-bit n < 10^14
        0, 0x0000000000000000L, 0x00005af3107a4000L, 14,   //  47-bit n ? 10^14
        15, 0x0000000000000000L, 0x00038d7ea4c68000L, 15,  //  48-bit n < 10^15
        15, 0x0000000000000000L, 0x00038d7ea4c68000L, 15,  //  49-bit n < 10^15
        0, 0x0000000000000000L, 0x00038d7ea4c68000L, 15,   //  50-bit n ? 10^15
        16, 0x0000000000000000L, 0x002386f26fc10000L, 16,  //  51-bit n < 10^16
        16, 0x0000000000000000L, 0x002386f26fc10000L, 16,  //  52-bit n < 10^16
        16, 0x0000000000000000L, 0x002386f26fc10000L, 16,  //  53-bit n < 10^16
        0, 0x0000000000000000L, 0x002386f26fc10000L, 16,   //  54-bit n ? 10^16
        17, 0x0000000000000000L, 0x016345785d8a0000L, 17,  //  55-bit n < 10^17
        17, 0x0000000000000000L, 0x016345785d8a0000L, 17,  //  56-bit n < 10^17
        0, 0x0000000000000000L, 0x016345785d8a0000L, 17,   //  57-bit n ? 10^17
        18, 0x0000000000000000L, 0x0de0b6b3a7640000L, 18,  //  58-bit n < 10^18
        18, 0x0000000000000000L, 0x0de0b6b3a7640000L, 18,  //  59-bit n < 10^18
        0, 0x0000000000000000L, 0x0de0b6b3a7640000L, 18,   //  60-bit n ? 10^18
        19, 0x0000000000000000L, 0x8ac7230489e80000L, 19,  //  61-bit n < 10^19
        19, 0x0000000000000000L, 0x8ac7230489e80000L, 19,  //  62-bit n < 10^19
        19, 0x0000000000000000L, 0x8ac7230489e80000L, 19,  //  63-bit n < 10^19
        0, 0x0000000000000000L, 0x8ac7230489e80000L, 19,   //  64-bit n ? 10^19
        20, 0x0000000000000005L, 0x6bc75e2d63100000L, 20,  //  65-bit n < 10^20
        20, 0x0000000000000005L, 0x6bc75e2d63100000L, 20,  //  66-bit n < 10^20
        0, 0x0000000000000005L, 0x6bc75e2d63100000L, 20,   //  67-bit n ? 10^20
        21, 0x0000000000000036L, 0x35c9adc5dea00000L, 21,  //  68-bit n < 10^21
        21, 0x0000000000000036L, 0x35c9adc5dea00000L, 21,  //  69-bit n < 10^21
        0, 0x0000000000000036L, 0x35c9adc5dea00000L, 21,   //  70-bit n ? 10^21
        22, 0x000000000000021eL, 0x19e0c9bab2400000L, 22,  //  71-bit n < 10^22
        22, 0x000000000000021eL, 0x19e0c9bab2400000L, 22,  //  72-bit n < 10^22
        22, 0x000000000000021eL, 0x19e0c9bab2400000L, 22,  //  73-bit n < 10^22
        0, 0x000000000000021eL, 0x19e0c9bab2400000L, 22,   //  74-bit n ? 10^22
        23, 0x000000000000152dL, 0x02c7e14af6800000L, 23,  //  75-bit n < 10^23
        23, 0x000000000000152dL, 0x02c7e14af6800000L, 23,  //  76-bit n < 10^23
        0, 0x000000000000152dL, 0x02c7e14af6800000L, 23,   //  77-bit n ? 10^23
        24, 0x000000000000d3c2L, 0x1bcecceda1000000L, 24,  //  78-bit n < 10^24
        24, 0x000000000000d3c2L, 0x1bcecceda1000000L, 24,  //  79-bit n < 10^24
        0, 0x000000000000d3c2L, 0x1bcecceda1000000L, 24,   //  80-bit n ? 10^24
        25, 0x0000000000084595L, 0x161401484a000000L, 25,  //  81-bit n < 10^25
        25, 0x0000000000084595L, 0x161401484a000000L, 25,  //  82-bit n < 10^25
        25, 0x0000000000084595L, 0x161401484a000000L, 25,  //  83-bit n < 10^25
        0, 0x0000000000084595L, 0x161401484a000000L, 25,   //  84-bit n ? 10^25
        26, 0x000000000052b7d2L, 0xdcc80cd2e4000000L, 26,  //  85-bit n < 10^26
        26, 0x000000000052b7d2L, 0xdcc80cd2e4000000L, 26,  //  86-bit n < 10^26
        0, 0x000000000052b7d2L, 0xdcc80cd2e4000000L, 26,   //  87-bit n ? 10^26
        27, 0x00000000033b2e3cL, 0x9fd0803ce8000000L, 27,  //  88-bit n < 10^27
        27, 0x00000000033b2e3cL, 0x9fd0803ce8000000L, 27,  //  89-bit n < 10^27
        0, 0x00000000033b2e3cL, 0x9fd0803ce8000000L, 27,   //  90-bit n ? 10^27
        28, 0x00000000204fce5eL, 0x3e25026110000000L, 28,  //  91-bit n < 10^28
        28, 0x00000000204fce5eL, 0x3e25026110000000L, 28,  //  92-bit n < 10^28
        28, 0x00000000204fce5eL, 0x3e25026110000000L, 28,  //  93-bit n < 10^28
        0, 0x00000000204fce5eL, 0x3e25026110000000L, 28,   //  94-bit n ? 10^28
        29, 0x00000001431e0faeL, 0x6d7217caa0000000L, 29,  //  95-bit n < 10^29
        29, 0x00000001431e0faeL, 0x6d7217caa0000000L, 29,  //  96-bit n < 10^29
        0, 0x00000001431e0faeL, 0x6d7217caa0000000L, 29,   //  97-bit n ? 10^29
        30, 0x0000000c9f2c9cd0L, 0x4674edea40000000L, 30,  //  98-bit n < 10^30
        30, 0x0000000c9f2c9cd0L, 0x4674edea40000000L, 30,  //  99-bit n < 10^30
        0, 0x0000000c9f2c9cd0L, 0x4674edea40000000L, 30,   // 100-bit n ? 10^30
        31, 0x0000007e37be2022L, 0xc0914b2680000000L, 31,  // 101-bit n < 10^31
        31, 0x0000007e37be2022L, 0xc0914b2680000000L, 31,  // 102-bit n < 10^31
        0, 0x0000007e37be2022L, 0xc0914b2680000000L, 31,   // 103-bit n ? 10^31
        32, 0x000004ee2d6d415bL, 0x85acef8100000000L, 32,  // 104-bit n < 10^32
        32, 0x000004ee2d6d415bL, 0x85acef8100000000L, 32,  // 105-bit n < 10^32
        32, 0x000004ee2d6d415bL, 0x85acef8100000000L, 32,  // 106-bit n < 10^32
        0, 0x000004ee2d6d415bL, 0x85acef8100000000L, 32,   // 107-bit n ? 10^32
        33, 0x0000314dc6448d93L, 0x38c15b0a00000000L, 33,  // 108-bit n < 10^33
        33, 0x0000314dc6448d93L, 0x38c15b0a00000000L, 33,  // 109-bit n < 10^33
        0, 0x0000314dc6448d93L, 0x38c15b0a00000000L, 33,   // 100-bit n ? 10^33
        34, 0x0001ed09bead87c0L, 0x378d8e6400000000L, 34,  // 111-bit n < 10^34
        34, 0x0001ed09bead87c0L, 0x378d8e6400000000L, 34,  // 112-bit n < 10^34
        0, 0x0001ed09bead87c0L, 0x378d8e6400000000L, 34    // 113-bit n ? 10^34
        // 35, 0x0013426172c74d82L, 0x2b878fe800000000L, 35   // 114-bit n < 10^35
    };

    // bid_ten2k64[i] = 10^i, 0 <= i <= 19
    static final long[] /*BID_UINT64*/ bid_ten2k64 = {
        0x0000000000000001L,    // 10^0
        0x000000000000000aL,    // 10^1
        0x0000000000000064L,    // 10^2
        0x00000000000003e8L,    // 10^3
        0x0000000000002710L,    // 10^4
        0x00000000000186a0L,    // 10^5
        0x00000000000f4240L,    // 10^6
        0x0000000000989680L,    // 10^7
        0x0000000005f5e100L,    // 10^8
        0x000000003b9aca00L,    // 10^9
        0x00000002540be400L,    // 10^10
        0x000000174876e800L,    // 10^11
        0x000000e8d4a51000L,    // 10^12
        0x000009184e72a000L,    // 10^13
        0x00005af3107a4000L,    // 10^14
        0x00038d7ea4c68000L,    // 10^15
        0x002386f26fc10000L,    // 10^16
        0x016345785d8a0000L,    // 10^17
        0x0de0b6b3a7640000L,    // 10^18
        0x8ac7230489e80000L     // 10^19 (20 digits)
    };

    static final long[] /*BID_UINT64*/ bid_ten2mk64 = {
        0x199999999999999aL,    //  10^(-1) * 2^ 64
        0x028f5c28f5c28f5dL,    //  10^(-2) * 2^ 64
        0x004189374bc6a7f0L,    //  10^(-3) * 2^ 64
        0x00346dc5d638865aL,    //  10^(-4) * 2^ 67
        0x0029f16b11c6d1e2L,    //  10^(-5) * 2^ 70
        0x00218def416bdb1bL,    //  10^(-6) * 2^ 73
        0x0035afe535795e91L,    //  10^(-7) * 2^ 77
        0x002af31dc4611874L,    //  10^(-8) * 2^ 80
        0x00225c17d04dad2aL,    //  10^(-9) * 2^ 83
        0x0036f9bfb3af7b76L,    // 10^(-10) * 2^ 87
        0x002bfaffc2f2c92bL,    // 10^(-11) * 2^ 90
        0x00232f33025bd423L,    // 10^(-12) * 2^ 93
        0x00384b84d092ed04L,    // 10^(-13) * 2^ 97
        0x002d09370d425737L,    // 10^(-14) * 2^100
        0x0024075f3dceac2cL,    // 10^(-15) * 2^103
        0x0039a5652fb11379L,    // 10^(-16) * 2^107
    };

    // bid_shiftright128[] contains the right shift count to obtain C2* from the top
    // 128 bits of the 128x128-bit product C2 * Kx
    static int[] bid_shiftright128 = {
        0,    // 128 - 128
        0,    // 128 - 128
        0,    // 128 - 128

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
        102    // 230 - 128
    };

    // bid_maskhigh128[] contains the mask to apply to the top 128 bits of the
    // 128x128-bit product in order to obtain the high bits of f2*
    // the 64-bit word order is L, H
    static long[] /*BID_UINT64*/ bid_maskhigh128 = {
        0x0000000000000000L,    //  0 = 128 - 128 bits
        0x0000000000000000L,    //  0 = 128 - 128 bits
        0x0000000000000000L,    //  0 = 128 - 128 bits
        0x0000000000000007L,    //  3 = 131 - 128 bits
        0x000000000000003fL,    //  6 = 134 - 128 bits
        0x00000000000001ffL,    //  9 = 137 - 128 bits
        0x0000000000001fffL,    // 13 = 141 - 128 bits
        0x000000000000ffffL,    // 16 = 144 - 128 bits
        0x000000000007ffffL,    // 19 = 147 - 128 bits
        0x00000000007fffffL,    // 23 = 151 - 128 bits
        0x0000000003ffffffL,    // 26 = 154 - 128 bits
        0x000000001fffffffL,    // 29 = 157 - 128 bits
        0x00000001ffffffffL,    // 33 = 161 - 128 bits
        0x0000000fffffffffL,    // 36 = 164 - 128 bits
        0x0000007fffffffffL,    // 39 = 167 - 128 bits
        0x000007ffffffffffL,    // 43 = 171 - 128 bits
        0x00003fffffffffffL,    // 46 = 174 - 128 bits
        0x0001ffffffffffffL,    // 49 = 177 - 128 bits
        0x001fffffffffffffL,    // 53 = 181 - 128 bits
        0x00ffffffffffffffL,    // 56 = 184 - 128 bits
        0x07ffffffffffffffL,    // 59 = 187 - 128 bits
        0x7fffffffffffffffL,    // 63 = 191 - 128 bits
        0x0000000000000003L,    //  2 = 194 - 192 bits
        0x000000000000001fL,    //  5 = 197 - 192 bits
        0x00000000000001ffL,    //  9 = 201 - 192 bits
        0x0000000000000fffL,    // 12 = 204 - 192 bits
        0x0000000000007fffL,    // 15 = 207 - 192 bits
        0x000000000007ffffL,    // 21 = 211 - 192 bits
        0x00000000003fffffL,    // 22 = 214 - 192 bits
        0x0000000001ffffffL,    // 25 = 217 - 192 bits
        0x000000000fffffffL,    // 28 = 220 - 192 bits
        0x00000000ffffffffL,    // 32 = 224 - 192 bits
        0x00000007ffffffffL,    // 35 = 227 - 192 bits
        0x0000003fffffffffL     // 38 = 230 - 192 bits
    };

    // bid_ten2mk128trunc[] contains T*, the top Ex >= 128 bits of 10^(-k),
    // for 1 <= k <= 34
    // the 64-bit word order is L, H
    static long[] /*BID_UINT128*/ bid_ten2mk128trunc_BID_UINT128 = {
        0x9999999999999999L, 0x1999999999999999L,    //  10^(-1) * 2^128
        0x28f5c28f5c28f5c2L, 0x028f5c28f5c28f5cL,    //  10^(-2) * 2^128
        0x9db22d0e56041893L, 0x004189374bc6a7efL,    //  10^(-3) * 2^128
        0x4af4f0d844d013a9L, 0x00346dc5d6388659L,    //  10^(-4) * 2^131
        0x08c3f3e0370cdc87L, 0x0029f16b11c6d1e1L,    //  10^(-5) * 2^134
        0x6d698fe69270b06cL, 0x00218def416bdb1aL,    //  10^(-6) * 2^137
        0xaf0f4ca41d811a46L, 0x0035afe535795e90L,    //  10^(-7) * 2^141
        0xbf3f70834acdae9fL, 0x002af31dc4611873L,    //  10^(-8) * 2^144
        0x65cc5a02a23e254cL, 0x00225c17d04dad29L,    //  10^(-9) * 2^147
        0x6fad5cd10396a213L, 0x0036f9bfb3af7b75L,    // 10^(-10) * 2^151
        0xbfbde3da69454e75L, 0x002bfaffc2f2c92aL,    // 10^(-11) * 2^154
        0x32fe4fe1edd10b91L, 0x00232f33025bd422L,    // 10^(-12) * 2^157
        0x84ca19697c81ac1bL, 0x00384b84d092ed03L,    // 10^(-13) * 2^161
        0x03d4e1213067bce3L, 0x002d09370d425736L,    // 10^(-14) * 2^164
        0x3643e74dc052fd82L, 0x0024075f3dceac2bL,    // 10^(-15) * 2^167
        0x56d30baf9a1e626aL, 0x0039a5652fb11378L,    // 10^(-16) * 2^171
        0x12426fbfae7eb521L, 0x002e1dea8c8da92dL,    // 10^(-17) * 2^174
        0x41cebfcc8b9890e7L, 0x0024e4bba3a48757L,    // 10^(-18) * 2^177
        0x694acc7a78f41b0cL, 0x003b07929f6da558L,    // 10^(-19) * 2^181
        0xbaa23d2ec729af3dL, 0x002f394219248446L,    // 10^(-20) * 2^184
        0xfbb4fdbf05baf297L, 0x0025c768141d369eL,    // 10^(-21) * 2^187
        0x2c54c931a2c4b758L, 0x003c7240202ebdcbL,    // 10^(-22) * 2^191
        0x89dd6dc14f03c5e0L, 0x00305b66802564a2L,    // 10^(-23) * 2^194
        0xd4b1249aa59c9e4dL, 0x0026af8533511d4eL,    // 10^(-24) * 2^197
        0x544ea0f76f60fd48L, 0x003de5a1ebb4fbb1L,    // 10^(-25) * 2^201
        0x76a54d92bf80caa0L, 0x00318481895d9627L,    // 10^(-26) * 2^204
        0x921dd7a89933d54dL, 0x00279d346de4781fL,    // 10^(-27) * 2^207
        0x8362f2a75b862214L, 0x003f61ed7ca0c032L,    // 10^(-28) * 2^211
        0xcf825bb91604e810L, 0x0032b4bdfd4d668eL,    // 10^(-29) * 2^214
        0x0c684960de6a5340L, 0x00289097fdd7853fL,    // 10^(-30) * 2^217
        0x3d203ab3e521dc33L, 0x002073accb12d0ffL,    // 10^(-31) * 2^220
        0x2e99f7863b696052L, 0x0033ec47ab514e65L,    // 10^(-32) * 2^224
        0x587b2c6b62bab375L, 0x002989d2ef743eb7L,    // 10^(-33) * 2^227
        0xad2f56bc4efbc2c4L, 0x00213b0f25f69892L,    // 10^(-34) * 2^230
    };

    static final long BID64_SIG_MAX = 0x002386F26FC0ffffL;

    public static long bid64_from_int64(final long /*BID_SINT64*/ x, final int rnd_mode/*, final JavaImplParse.FloatingPointStatusFlag pfpsf*/) {
        long /*BID_UINT64*/ res;
        long /*BID_UINT64*/ x_sign, C;
        /*unsigned*/
        int q, ind;
        int incr_exp = 0;
        int is_midpoint_lt_even = 0, is_midpoint_gt_even = 0;
        int is_inexact_lt_midpoint = 0, is_inexact_gt_midpoint = 0;

        x_sign = x & 0x8000000000000000L;
        // if the integer is negative, use the absolute value
        if (x_sign != 0)
            C = ~x + 1;
        else
            C = x;
        if (UnsignedLong.isLessOrEqual(C, BID64_SIG_MAX)) {    // |C| <= 10^16-1 and the result is exact
            if (UnsignedLong.isLess(C, 0x0020000000000000L)) {    // C < 2^53
                res = x_sign | 0x31c0000000000000L | C;
            } else {    // C >= 2^53
                res = x_sign | 0x6c70000000000000L | (C & 0x0007ffffffffffffL);
            }
        } else {    // |C| >= 10^16 and the result may be inexact
            // the smallest |C| is 10^16 which has 17 decimal digits
            // the largest |C| is 0x8000000000000000 = 9223372036854775808 w/ 19 digits
            if (UnsignedLong.isLess(C, 0x16345785d8a0000L)) {    // x < 10^17
                q = 17;
                ind = 1;    // number of digits to remove for q = 17
            } else if (UnsignedLong.isLess(C, 0xde0b6b3a7640000L)) {    // C < 10^18
                q = 18;
                ind = 2;    // number of digits to remove for q = 18
            } else {    // C < 10^19
                q = 19;
                ind = 3;    // number of digits to remove for q = 19
            }
            // overflow and underflow are not possible
            // Note: performance can be improved by inlining this call


            //bid_round64_2_18(    // will work for 19 digits too if C fits in 64 bits
            //    q, ind, C, &res, &incr_exp,
            //    &is_midpoint_lt_even, &is_midpoint_gt_even, &is_inexact_lt_midpoint, &is_inexact_gt_midpoint);
            {

                long /*BID_UINT128*/ __P128_w0, __P128_w1;
                long /*BID_UINT128*/ __fstar_w0, __fstar_w1;
                long /*BID_UINT64*/ __Cstar;
                long /*BID_UINT64*/ __tmp64;
                int __shift;
                int __ind;

                // Note:
                //    In round128_2_18() positive numbers with 2 <= q <= 18 will be
                //    rounded to nearest only for 1 <= __x <= 3:
                //     __x = 1 or __x = 2 when q = 17
                //     __x = 2 or __x = 3 when q = 18
                // However, for generality and possible uses outside the frame of IEEE 754
                // this implementation works for 1 <= __x <= q - 1

                // assume *is_midpoint_lt_even, *is_midpoint_gt_even,
                // *is_inexact_lt_midpoint, and *is_inexact_gt_midpoint are
                // initialized to 0 by the caller

                // round a number C with q decimal digits, 2 <= q <= 18
                // to q - __x digits, 1 <= __x <= 17
                // C = C + 1/2 * 10^__x where the result C fits in 64 bits
                // (because the largest value is 999999999999999999 + 50000000000000000 =
                // 0x0e92596fd628ffff, which fits in 60 bits)
                __ind = ind - 1;    // 0 <= __ind <= 16
                C = C + bid_midpoint64[__ind];
                // kx ~= 10^(-__x), kx = bid_Kx64[__ind] * 2^(-Ex), 0 <= __ind <= 16
                // __P128 = (C + 1/2 * 10^__x) * kx * 2^Ex = (C + 1/2 * 10^__x) * Kx
                // the approximation kx of 10^(-__x) was rounded up to 64 bits

                //__mul_64x64_to_128MACH(__P128, C, bid_Kx64[__ind]);
                {
                    final long __CY = bid_Kx64[__ind];
                    __P128_w1 = Mul64Impl.unsignedMultiplyHigh(C, __CY);
                    __P128_w0 = C * __CY;
                }

                // calculate C* = floor (__P128) and f*
                // __Cstar = __P128 >> Ex
                // __fstar = low Ex bits of __P128
                __shift = bid_Ex64m64[__ind];    // in [3, 56]
                __Cstar = __P128_w1 >>> __shift;
                __fstar_w1 = __P128_w1 & bid_mask64[__ind];
                __fstar_w0 = __P128_w0;
                // the top Ex bits of 10^(-__x) are T* = bid_ten2mxtrunc64[__ind], e.g.
                // if __x=1, T*=bid_ten2mxtrunc64[0]=0xcccccccccccccccc
                // if (0 < f* < 10^(-__x)) then the result is a midpoint
                //   if floor(C*) is even then C* = floor(C*) - logical right
                //       __shift; C* has q - __x decimal digits, correct by Prop. 1)
                //   else if floor(C*) is odd C* = floor(C*)-1 (logical right
                //       __shift; C* has q - __x decimal digits, correct by Pr. 1)
                // else
                //   C* = floor(C*) (logical right __shift; C has q - __x decimal digits,
                //       correct by Property 1)
                // in the caling function n = C* * 10^(e+__x)

                // determine inexactness of the rounding of C*
                // if (0 < f* - 1/2 < 10^(-__x)) then
                //   the result is exact
                // else // if (f* - 1/2 > T*) then
                //   the result is inexact
                if (UnsignedLong.isGreater(__fstar_w1, bid_half64[__ind]) ||
                    (__fstar_w1 == bid_half64[__ind] && __fstar_w0 != 0)) {
                    // f* > 1/2 and the result may be exact
                    // Calculate f* - 1/2
                    __tmp64 = __fstar_w1 - bid_half64[__ind];
                    if (__tmp64 != 0 || UnsignedLong.isGreater(__fstar_w0, bid_ten2mxtrunc64[__ind])) {    // f* - 1/2 > 10^(-__x)
                        is_inexact_lt_midpoint = 1;
                    }    // else the result is exact
                } else {    // the result is inexact; f2* <= 1/2
                    is_inexact_gt_midpoint = 1;
                }
                // check for midpoints (could do this before determining inexactness)
                if (__fstar_w1 == 0 && UnsignedLong.isLessOrEqual(__fstar_w0, bid_ten2mxtrunc64[__ind])) {
                    // the result is a midpoint
                    if ((__Cstar & 0x01) != 0) {    // __Cstar is odd; MP in [EVEN, ODD]
                        // if floor(C*) is odd C = floor(C*) - 1; the result may be 0
                        __Cstar--;    // __Cstar is now even
                        is_midpoint_gt_even = 1;
                        is_inexact_lt_midpoint = 0;
                        is_inexact_gt_midpoint = 0;
                    } else {    // else MP in [ODD, EVEN]
                        is_midpoint_lt_even = 1;
                        is_inexact_lt_midpoint = 0;
                        is_inexact_gt_midpoint = 0;
                    }
                }
                // check for rounding overflow, which occurs if __Cstar = 10^(q-__x)
                __ind = q - ind;    // 1 <= __ind <= q - 1
                if (__Cstar == bid_ten2k64[__ind]) {    // if  __Cstar = 10^(q-__x)
                    __Cstar = bid_ten2k64[__ind - 1];    // __Cstar = 10^(q-__x-1)
                    incr_exp = 1;
                } else {    // 10^33 <= __Cstar <= 10^34 - 1
                    incr_exp = 0;
                }
                res = __Cstar;
            }


            if (incr_exp != 0)
                ind++;
            // set the inexact flag
//            if (is_inexact_lt_midpoint!=0 || is_inexact_gt_midpoint!=0 ||
//                is_midpoint_lt_even!=0 || is_midpoint_gt_even!=0)
//                __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);
            // general correction from RN to RA, RM, RP, RZ; result uses ind for exp
            if (rnd_mode != BID_ROUNDING_TO_NEAREST) {
                if ((x_sign == 0
                    && ((rnd_mode == BID_ROUNDING_UP && is_inexact_lt_midpoint != 0)
                    ||
                    ((rnd_mode == BID_ROUNDING_TIES_AWAY
                        || rnd_mode == BID_ROUNDING_UP) && is_midpoint_gt_even != 0)))
                    || (x_sign != 0
                    && ((rnd_mode == BID_ROUNDING_DOWN && is_inexact_lt_midpoint != 0)
                    ||
                    ((rnd_mode == BID_ROUNDING_TIES_AWAY
                        || rnd_mode == BID_ROUNDING_DOWN)
                        && is_midpoint_gt_even != 0)))) {
                    res = res + 1;
                    if (res == 0x002386f26fc10000L) {    // res = 10^16 => rounding overflow
                        res = 0x00038d7ea4c68000L;    // 10^15
                        ind = ind + 1;
                    }
                } else if ((is_midpoint_lt_even != 0 || is_inexact_gt_midpoint != 0) &&
                    ((x_sign != 0 && (rnd_mode == BID_ROUNDING_UP ||
                        rnd_mode == BID_ROUNDING_TO_ZERO)) ||
                        (x_sign == 0 && (rnd_mode == BID_ROUNDING_DOWN ||
                            rnd_mode == BID_ROUNDING_TO_ZERO)))) {
                    res = res - 1;
                    // check if we crossed into the lower decade
                    if (res == 0x00038d7ea4c67fffL) {    // 10^15 - 1
                        res = 0x002386f26fc0ffffL;    // 10^16 - 1
                        ind = ind - 1;
                    }
                } else {
                    ;    // exact, the result is already correct
                }
            }
            if (UnsignedLong.isLess(res, 0x0020000000000000L)) {    // res < 2^53
                res = x_sign | (((long) ind + 398) << 53) | res;
            } else {    // res >= 2^53
                res =
                    x_sign | 0x6000000000000000L | (((long) ind + 398) << 51) |
                        (res & 0x0007ffffffffffffL);
            }
        }
        return res;
    }

    // bid_midpoint64[i - 1] = 1/2 * 10^i = 5 * 10^(i-1), 1 <= i <= 19
    static long[] /*BID_UINT64*/ bid_midpoint64 = {
        0x0000000000000005L,    // 1/2 * 10^1 = 5 * 10^0
        0x0000000000000032L,    // 1/2 * 10^2 = 5 * 10^1
        0x00000000000001f4L,    // 1/2 * 10^3 = 5 * 10^2
        0x0000000000001388L,    // 1/2 * 10^4 = 5 * 10^3
        0x000000000000c350L,    // 1/2 * 10^5 = 5 * 10^4
        0x000000000007a120L,    // 1/2 * 10^6 = 5 * 10^5
        0x00000000004c4b40L,    // 1/2 * 10^7 = 5 * 10^6
        0x0000000002faf080L,    // 1/2 * 10^8 = 5 * 10^7
        0x000000001dcd6500L,    // 1/2 * 10^9 = 5 * 10^8
        0x000000012a05f200L,    // 1/2 * 10^10 = 5 * 10^9
        0x0000000ba43b7400L,    // 1/2 * 10^11 = 5 * 10^10
        0x000000746a528800L,    // 1/2 * 10^12 = 5 * 10^11
        0x0000048c27395000L,    // 1/2 * 10^13 = 5 * 10^12
        0x00002d79883d2000L,    // 1/2 * 10^14 = 5 * 10^13
        0x0001c6bf52634000L,    // 1/2 * 10^15 = 5 * 10^14
        0x0011c37937e08000L,    // 1/2 * 10^16 = 5 * 10^15
        0x00b1a2bc2ec50000L,    // 1/2 * 10^17 = 5 * 10^16
        0x06f05b59d3b20000L,    // 1/2 * 10^18 = 5 * 10^17
        0x4563918244f40000L     // 1/2 * 10^19 = 5 * 10^18
    };

    /***************************************************************************
     *************** TABLES FOR GENERAL ROUNDING FUNCTIONS *********************
     ***************************************************************************/
    // Note: not all entries in these tables will be used with IEEE 754 decimal
    // floating-point arithmetic
    // a) In round128_2_18() numbers with 2 <= q <= 18 will be rounded only
    //    for 1 <= x <= 3:
    //     x = 1 or x = 2 when q = 17
    //     x = 2 or x = 3 when q = 18
    // b) In bid_round128_19_38() numbers with 19 <= q <= 38 will be rounded only
    //    for 1 <= x <= 23:
    //     x = 3 or x = 4 when q = 19
    //     x = 4 or x = 5 when q = 20
    //     ...
    //     x = 18 or x = 19 when q = 34
    //     x = 1 or x = 2 or x = 19 or x = 20 when q = 35
    //     x = 2 or x = 3 or x = 20 or x = 21 when q = 36
    //     x = 3 or x = 4 or x = 21 or x = 22 when q = 37
    //     x = 4 or x = 5 or x = 22 or x = 23 when q = 38
    // c) ...
    // However, for generality and possible uses outside the frame of IEEE 754
    // this implementation includes table values for all x in [1, q - 1]

    // Note: 64-bit tables generated with ten2mx64.ma; output in ten2mx64.out

    // Kx from 10^(-x) ~= Kx * 2^(-Ex); Kx rounded up to 64 bits, 1 <= x <= 17
    static long[] /*BID_UINT64*/ bid_Kx64 = {
        0xcccccccccccccccdL,    // 10^-1 ~= cccccccccccccccd * 2^-67
        0xa3d70a3d70a3d70bL,    // 10^-2 ~= a3d70a3d70a3d70b * 2^-70
        0x83126e978d4fdf3cL,    // 10^-3 ~= 83126e978d4fdf3c * 2^-73
        0xd1b71758e219652cL,    // 10^-4 ~= d1b71758e219652c * 2^-77
        0xa7c5ac471b478424L,    // 10^-5 ~= a7c5ac471b478424 * 2^-80
        0x8637bd05af6c69b6L,    // 10^-6 ~= 8637bd05af6c69b6 * 2^-83
        0xd6bf94d5e57a42bdL,    // 10^-7 ~= d6bf94d5e57a42bd * 2^-87
        0xabcc77118461cefdL,    // 10^-8 ~= abcc77118461cefd * 2^-90
        0x89705f4136b4a598L,    // 10^-9 ~= 89705f4136b4a598 * 2^-93
        0xdbe6fecebdedd5bfL,    // 10^-10 ~= dbe6fecebdedd5bf * 2^-97
        0xafebff0bcb24aaffL,    // 10^-11 ~= afebff0bcb24aaff * 2^-100
        0x8cbccc096f5088ccL,    // 10^-12 ~= 8cbccc096f5088cc * 2^-103
        0xe12e13424bb40e14L,    // 10^-13 ~= e12e13424bb40e14 * 2^-107
        0xb424dc35095cd810L,    // 10^-14 ~= b424dc35095cd810 * 2^-110
        0x901d7cf73ab0acdaL,    // 10^-15 ~= 901d7cf73ab0acda * 2^-113
        0xe69594bec44de15cL,    // 10^-16 ~= e69594bec44de15c * 2^-117
        0xb877aa3236a4b44aL     // 10^-17 ~= b877aa3236a4b44a * 2^-120
    };


    // Ex-64 from 10^(-x) ~= Kx * 2^(-Ex); Kx rounded up to 64 bits, 1 <= x <= 17
    static /*unsigned*/ int[] bid_Ex64m64 = {
        3,     // 67 - 64, Ex = 67
        6,     // 70 - 64, Ex = 70
        9,     // 73 - 64, Ex = 73
        13,    // 77 - 64, Ex = 77
        16,    // 80 - 64, Ex = 80
        19,    // 83 - 64, Ex = 83
        23,    // 87 - 64, Ex = 87
        26,    // 90 - 64, Ex = 90
        29,    // 93 - 64, Ex = 93
        33,    // 97 - 64, Ex = 97
        36,    // 100 - 64, Ex = 100
        39,    // 103 - 64, Ex = 103
        43,    // 107 - 64, Ex = 107
        46,    // 110 - 64, Ex = 110
        49,    // 113 - 64, Ex = 113
        53,    // 117 - 64, Ex = 117
        56     // 120 - 64, Ex = 120
    };

    // Values of 1/2 in the right position to be compared with the fraction from
    // C * kx, 1 <= x <= 17; the fraction consists of the low Ex bits in C * kx
    // (these values are aligned with the high 64 bits of the fraction)
    static long[] /*BID_UINT64*/ bid_half64 = {
        0x0000000000000004L,    // half / 2^64 = 4
        0x0000000000000020L,    // half / 2^64 = 20
        0x0000000000000100L,    // half / 2^64 = 100
        0x0000000000001000L,    // half / 2^64 = 1000
        0x0000000000008000L,    // half / 2^64 = 8000
        0x0000000000040000L,    // half / 2^64 = 40000
        0x0000000000400000L,    // half / 2^64 = 400000
        0x0000000002000000L,    // half / 2^64 = 2000000
        0x0000000010000000L,    // half / 2^64 = 10000000
        0x0000000100000000L,    // half / 2^64 = 100000000
        0x0000000800000000L,    // half / 2^64 = 800000000
        0x0000004000000000L,    // half / 2^64 = 4000000000
        0x0000040000000000L,    // half / 2^64 = 40000000000
        0x0000200000000000L,    // half / 2^64 = 200000000000
        0x0001000000000000L,    // half / 2^64 = 1000000000000
        0x0010000000000000L,    // half / 2^64 = 10000000000000
        0x0080000000000000L     // half / 2^64 = 80000000000000
    };

    // Values of mask in the right position to obtain the high Ex - 64 bits
    // of the fraction from C * kx, 1 <= x <= 17; the fraction consists of
    // the low Ex bits in C * kx
    static long[] /*BID_UINT64*/ bid_mask64 = {
        0x0000000000000007L,    // mask / 2^64
        0x000000000000003fL,    // mask / 2^64
        0x00000000000001ffL,    // mask / 2^64
        0x0000000000001fffL,    // mask / 2^64
        0x000000000000ffffL,    // mask / 2^64
        0x000000000007ffffL,    // mask / 2^64
        0x00000000007fffffL,    // mask / 2^64
        0x0000000003ffffffL,    // mask / 2^64
        0x000000001fffffffL,    // mask / 2^64
        0x00000001ffffffffL,    // mask / 2^64
        0x0000000fffffffffL,    // mask / 2^64
        0x0000007fffffffffL,    // mask / 2^64
        0x000007ffffffffffL,    // mask / 2^64
        0x00003fffffffffffL,    // mask / 2^64
        0x0001ffffffffffffL,    // mask / 2^64
        0x001fffffffffffffL,    // mask / 2^64
        0x00ffffffffffffffL     // mask / 2^64
    };

    // Values of 10^(-x) trancated to Ex bits beyond the binary point, and
    // in the right position to be compared with the fraction from C * kx,
    // 1 <= x <= 17; the fraction consists of the low Ex bits in C * kx
    // (these values are aligned with the low 64 bits of the fraction)
    static long[] /*BID_UINT64*/ bid_ten2mxtrunc64 = {
        0xccccccccccccccccL,    // (ten2mx >> 64) = cccccccccccccccc
        0xa3d70a3d70a3d70aL,    // (ten2mx >> 64) = a3d70a3d70a3d70a
        0x83126e978d4fdf3bL,    // (ten2mx >> 64) = 83126e978d4fdf3b
        0xd1b71758e219652bL,    // (ten2mx >> 64) = d1b71758e219652b
        0xa7c5ac471b478423L,    // (ten2mx >> 64) = a7c5ac471b478423
        0x8637bd05af6c69b5L,    // (ten2mx >> 64) = 8637bd05af6c69b5
        0xd6bf94d5e57a42bcL,    // (ten2mx >> 64) = d6bf94d5e57a42bc
        0xabcc77118461cefcL,    // (ten2mx >> 64) = abcc77118461cefc
        0x89705f4136b4a597L,    // (ten2mx >> 64) = 89705f4136b4a597
        0xdbe6fecebdedd5beL,    // (ten2mx >> 64) = dbe6fecebdedd5be
        0xafebff0bcb24aafeL,    // (ten2mx >> 64) = afebff0bcb24aafe
        0x8cbccc096f5088cbL,    // (ten2mx >> 64) = 8cbccc096f5088cb
        0xe12e13424bb40e13L,    // (ten2mx >> 64) = e12e13424bb40e13
        0xb424dc35095cd80fL,    // (ten2mx >> 64) = b424dc35095cd80f
        0x901d7cf73ab0acd9L,    // (ten2mx >> 64) = 901d7cf73ab0acd9
        0xe69594bec44de15bL,    // (ten2mx >> 64) = e69594bec44de15b
        0xb877aa3236a4b449L     // (ten2mx >> 64) = b877aa3236a4b449
    };
}
