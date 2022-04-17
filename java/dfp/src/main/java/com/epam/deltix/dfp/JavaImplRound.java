package com.epam.deltix.dfp;

import static com.epam.deltix.dfp.JavaImplAdd.BID_ROUNDING_DOWN;
import static com.epam.deltix.dfp.JavaImplAdd.BID_ROUNDING_TIES_AWAY;
import static com.epam.deltix.dfp.JavaImplAdd.BID_ROUNDING_TO_NEAREST;
import static com.epam.deltix.dfp.JavaImplAdd.BID_ROUNDING_TO_ZERO;
import static com.epam.deltix.dfp.JavaImplAdd.BID_ROUNDING_UP;
import static com.epam.deltix.dfp.JavaImplAdd.LONG_LOW_PART;
import static com.epam.deltix.dfp.JavaImplCast.*;
import static com.epam.deltix.dfp.JavaImplCmp.*;
import static com.epam.deltix.dfp.JavaImplParse.*;

class JavaImplRound {
    private JavaImplRound() {
    }

    public static long bid64_round_integral_exact(long /*BID_UINT64*/ x, final int rnd_mode, final JavaImplParse.FloatingPointStatusFlag pfpsf) {
        long /*BID_UINT64*/ res = 0xbaddbaddbaddbaddL;
        long /*BID_UINT64*/ x_sign;
        int exp;            // unbiased exponent
        // Note: C1 represents the significand (BID_UINT64)
        //BID_UI64DOUBLE tmp1;
        int x_nr_bits;
        int q, ind, shift;
        long /*BID_UINT64*/ C1;
        // BID_UINT64 res is C* at first - represents up to 16 decimal digits <= 54 bits
        long /*BID_UINT128*/ fstar_w0 = 0x0L, fstar_w1 = 0x0L;
        long /*BID_UINT128*/ P128_w0, P128_w1;

        x_sign = x & MASK_SIGN;    // 0 for positive, MASK_SIGN for negative

        // check for NaNs and infinities
        if ((x & MASK_NAN) == MASK_NAN) {    // check for NaN
            if (UnsignedLong.isGreater(x & 0x0003ffffffffffffL, 999999999999999L))
                x = x & 0xfe00000000000000L;    // clear G6-G12 and the payload bits
            else
                x = x & 0xfe03ffffffffffffL;    // clear G6-G12
            if ((x & MASK_SNAN) == MASK_SNAN) {    // SNaN
                // set invalid flag
                __set_status_flags(pfpsf, BID_INVALID_EXCEPTION);
                // return quiet (SNaN)
                res = x & 0xfdffffffffffffffL;
            } else {    // QNaN
                res = x;
            }
            return res;
        } else if ((x & MASK_INF) == MASK_INF) {    // check for Infinity
            return x_sign | 0x7800000000000000L;
        }
        // unpack x
        if ((x & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            // if the steering bits are 11 (condition will be 0), then
            // the exponent is G[0:w+1]
            exp = (int) (((x & MASK_BINARY_EXPONENT2) >>> 51) - 398);
            C1 = (x & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
            if (UnsignedLong.isGreater(C1, 9999999999999999L)) {    // non-canonical
                C1 = 0;
            }
        } else {    // if ((x & MASK_STEERING_BITS) != MASK_STEERING_BITS)
            exp = (int) (((x & MASK_BINARY_EXPONENT1) >>> 53) - 398);
            C1 = (x & MASK_BINARY_SIG1);
        }

        // if x is 0 or non-canonical return 0 preserving the sign bit and
        // the preferred exponent of MAX(Q(x), 0)
        if (C1 == 0) {
            if (exp < 0)
                exp = 0;
            return x_sign | (((long) exp + 398) << 53);
        }
        // x is a finite non-zero number (not 0, non-canonical, or special)

        switch (rnd_mode) {
            case BID_ROUNDING_TO_NEAREST:
            case BID_ROUNDING_TIES_AWAY:
                // return 0 if (exp <= -(p+1))
                if (exp <= -17) {
                    __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);
                    return x_sign | 0x31c0000000000000L;
                }
                break;
            case BID_ROUNDING_DOWN:
                // return 0 if (exp <= -p)
                if (exp <= -16) {
                    __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);
                    return x_sign != 0 ? 0xb1c0000000000001L : 0x31c0000000000000L;
                }
                break;
            case BID_ROUNDING_UP:
                // return 0 if (exp <= -p)
                if (exp <= -16) {
                    __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);
                    return x_sign != 0 ? 0xb1c0000000000000L : 0x31c0000000000001L;
                }
                break;
            case BID_ROUNDING_TO_ZERO:
                // return 0 if (exp <= -p)
                if (exp <= -16) {
                    __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);
                    return x_sign | 0x31c0000000000000L;
                }
                break;
        }    // end switch ()

        // q = nr. of decimal digits in x (1 <= q <= 54)
        //  determine first the nr. of bits in x
        if (UnsignedLong.isGreaterOrEqual(C1, 0x0020000000000000L)) {    // x >= 2^53
            q = 16;
        } else {    // if x < 2^53
            final long tmp1_ui64 = Double.doubleToRawLongBits((double) C1);    // exact conversion
            x_nr_bits = 1 + ((((/*unsigned*/ int) (tmp1_ui64 >>> 52)) & 0x7ff) - 0x3ff);
            q = (int) bid_nr_digits_flat[((x_nr_bits - 1) << 2) /*+ 0 .digits*/];
            if (q == 0) {
                q = (int) bid_nr_digits_flat[((x_nr_bits - 1) << 2) + 3 /*.digits1*/];
                if (UnsignedLong.isGreaterOrEqual(C1, bid_nr_digits_flat[((x_nr_bits - 1) << 2) + 2 /*.threshold_lo*/]))
                    q++;
            }
        }

        if (exp >= 0) {    // -exp <= 0
            // the argument is an integer already
            return x;
        }

        switch (rnd_mode) {
            case BID_ROUNDING_TO_NEAREST:
                if ((q + exp) >= 0) {    // exp < 0 and 1 <= -exp <= q
                    // need to shift right -exp digits from the coefficient; exp will be 0
                    ind = -exp;    // 1 <= ind <= 16; ind is a synonym for 'x'
                    // chop off ind digits from the lower part of C1
                    // C1 = C1 + 1/2 * 10^x where the result C1 fits in 64 bits
                    // FOR ROUND_TO_NEAREST, WE ADD 1/2 ULP(y) then truncate
                    C1 = C1 + bid_midpoint64[ind - 1];
                    // calculate C* and f*
                    // C* is actually floor(C*) in this case
                    // C* and f* need shifting and masking, as shown by
                    // bid_shiftright128[] and bid_maskhigh128[]
                    // 1 <= x <= 16
                    // kx = 10^(-x) = bid_ten2mk64[ind - 1]
                    // C* = (C1 + 1/2 * 10^x) * 10^(-x)
                    // the approximation of 10^(-x) was rounded up to 64 bits

                    //__mul_64x64_to_128(P128, C1, bid_ten2mk64[ind - 1]);
                    {
                        long __CY = bid_ten2mk64[ind - 1];
                        long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
                        __CXH = C1 >>> 32;
                        __CXL = LONG_LOW_PART & C1;
                        __CYH = __CY >>> 32;
                        __CYL = LONG_LOW_PART & __CY;

                        __PM = __CXH * __CYL;
                        __PH = __CXH * __CYH;
                        __PL = __CXL * __CYL;
                        __PM2 = __CXL * __CYH;
                        __PH += (__PM >>> 32);
                        __PM = (LONG_LOW_PART & __PM) + __PM2 + (__PL >>> 32);

                        P128_w1 = __PH + (__PM >>> 32);
                        P128_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
                    }

                    // if (0 < f* < 10^(-x)) then the result is a midpoint
                    //   if floor(C*) is even then C* = floor(C*) - logical right
                    //       shift; C* has p decimal digits, correct by Prop. 1)
                    //   else if floor(C*) is odd C* = floor(C*)-1 (logical right
                    //       shift; C* has p decimal digits, correct by Pr. 1)
                    // else
                    //   C* = floor(C*) (logical right shift; C has p decimal digits,
                    //       correct by Property 1)
                    // n = C* * 10^(e+x)

                    if (ind - 1 <= 2) {    // 0 <= ind - 1 <= 2 => shift = 0
                        res = P128_w1;
                        fstar_w1 = 0;
                        fstar_w0 = P128_w0;
                    } else if (ind - 1 <= 21) {    // 3 <= ind - 1 <= 21 => 3 <= shift <= 63
                        shift = bid_shiftright128[ind - 1];    // 3 <= shift <= 63
                        res = (P128_w1 >>> shift);
                        fstar_w1 = P128_w1 & bid_maskhigh128[ind - 1];
                        fstar_w0 = P128_w0;
                    }
                    // if (0 < f* < 10^(-x)) then the result is a midpoint
                    // since round_to_even, subtract 1 if current result is odd
                    if ((res & 0x0000000000000001L) != 0 && (fstar_w1 == 0) && UnsignedLong.isLess(fstar_w0, bid_ten2mk64[ind - 1])) {
                        res--;
                    }
                    // determine inexactness of the rounding of C*
                    // if (0 < f* - 1/2 < 10^(-x)) then
                    //   the result is exact
                    // else // if (f* - 1/2 > T*) then
                    //   the result is inexact
                    if (ind - 1 <= 2) {
                        if (UnsignedLong.isGreater(fstar_w0, 0x8000000000000000L)) {
                            // f* > 1/2 and the result may be exact
                            // fstar.w[0] - 0x8000000000000000L is f* - 1/2
                            if (UnsignedLong.isGreater(fstar_w0 - 0x8000000000000000L, bid_ten2mk64[ind - 1])) {
                                // set the inexact flag
                                __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);
                            }    // else the result is exact
                        } else {    // the result is inexact; f2* <= 1/2
                            // set the inexact flag
                            __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);
                        }
                    } else {    // if 3 <= ind - 1 <= 21
                        if (UnsignedLong.isGreater(fstar_w1, bid_onehalf128[ind - 1]) || (fstar_w1 == bid_onehalf128[ind - 1] && fstar_w0 != 0)) {
                            // f2* > 1/2 and the result may be exact
                            // Calculate f2* - 1/2
                            if (UnsignedLong.isGreater(fstar_w1, bid_onehalf128[ind - 1]) || UnsignedLong.isGreater(fstar_w0, bid_ten2mk64[ind - 1])) {
                                // set the inexact flag
                                __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);
                            }    // else the result is exact
                        } else {    // the result is inexact; f2* <= 1/2
                            // set the inexact flag
                            __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);
                        }
                    }
                    // set exponent to zero as it was negative before.
                    return x_sign | 0x31c0000000000000L | res;
                } else {    // if exp < 0 and q + exp < 0
                    // the result is +0 or -0
                    __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);
                    return x_sign | 0x31c0000000000000L;
                }
                //break;
            case BID_ROUNDING_TIES_AWAY:
                if ((q + exp) >= 0) {    // exp < 0 and 1 <= -exp <= q
                    // need to shift right -exp digits from the coefficient; exp will be 0
                    ind = -exp;    // 1 <= ind <= 16; ind is a synonym for 'x'
                    // chop off ind digits from the lower part of C1
                    // C1 = C1 + 1/2 * 10^x where the result C1 fits in 64 bits
                    // FOR ROUND_TO_NEAREST, WE ADD 1/2 ULP(y) then truncate
                    C1 = C1 + bid_midpoint64[ind - 1];
                    // calculate C* and f*
                    // C* is actually floor(C*) in this case
                    // C* and f* need shifting and masking, as shown by
                    // bid_shiftright128[] and bid_maskhigh128[]
                    // 1 <= x <= 16
                    // kx = 10^(-x) = bid_ten2mk64[ind - 1]
                    // C* = (C1 + 1/2 * 10^x) * 10^(-x)
                    // the approximation of 10^(-x) was rounded up to 64 bits

                    //__mul_64x64_to_128(P128, C1, bid_ten2mk64[ind - 1]);
                    {
                        long __CY = bid_ten2mk64[ind - 1];
                        long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
                        __CXH = C1 >>> 32;
                        __CXL = LONG_LOW_PART & C1;
                        __CYH = __CY >>> 32;
                        __CYL = LONG_LOW_PART & __CY;

                        __PM = __CXH * __CYL;
                        __PH = __CXH * __CYH;
                        __PL = __CXL * __CYL;
                        __PM2 = __CXL * __CYH;
                        __PH += (__PM >>> 32);
                        __PM = (LONG_LOW_PART & __PM) + __PM2 + (__PL >>> 32);

                        P128_w1 = __PH + (__PM >>> 32);
                        P128_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
                    }

                    // if (0 < f* < 10^(-x)) then the result is a midpoint
                    //   C* = floor(C*) - logical right shift; C* has p decimal digits,
                    //       correct by Prop. 1)
                    // else
                    //   C* = floor(C*) (logical right shift; C has p decimal digits,
                    //       correct by Property 1)
                    // n = C* * 10^(e+x)

                    if (ind - 1 <= 2) {    // 0 <= ind - 1 <= 2 => shift = 0
                        res = P128_w1;
                        fstar_w1 = 0;
                        fstar_w0 = P128_w0;
                    } else if (ind - 1 <= 21) {    // 3 <= ind - 1 <= 21 => 3 <= shift <= 63
                        shift = bid_shiftright128[ind - 1];    // 3 <= shift <= 63
                        res = (P128_w1 >>> shift);
                        fstar_w1 = P128_w1 & bid_maskhigh128[ind - 1];
                        fstar_w0 = P128_w0;
                    }
                    // midpoints are already rounded correctly
                    // determine inexactness of the rounding of C*
                    // if (0 < f* - 1/2 < 10^(-x)) then
                    //   the result is exact
                    // else // if (f* - 1/2 > T*) then
                    //   the result is inexact
                    if (ind - 1 <= 2) {
                        if (UnsignedLong.isGreater(fstar_w0, 0x8000000000000000L)) {
                            // f* > 1/2 and the result may be exact
                            // fstar.w[0] - 0x8000000000000000L is f* - 1/2
                            if (UnsignedLong.isGreater(fstar_w0 - 0x8000000000000000L, bid_ten2mk64[ind - 1])) {
                                // set the inexact flag
                                __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);
                            }    // else the result is exact
                        } else {    // the result is inexact; f2* <= 1/2
                            // set the inexact flag
                            __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);
                        }
                    } else {    // if 3 <= ind - 1 <= 21
                        if (UnsignedLong.isGreater(fstar_w1, bid_onehalf128[ind - 1]) || (fstar_w1 == bid_onehalf128[ind - 1] && fstar_w0 != 0)) {
                            // f2* > 1/2 and the result may be exact
                            // Calculate f2* - 1/2
                            if (UnsignedLong.isGreater(fstar_w1, bid_onehalf128[ind - 1]) || UnsignedLong.isGreater(fstar_w0, bid_ten2mk64[ind - 1])) {
                                // set the inexact flag
                                __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);
                            }    // else the result is exact
                        } else {    // the result is inexact; f2* <= 1/2
                            // set the inexact flag
                            __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);
                        }
                    }
                    // set exponent to zero as it was negative before.
                    return x_sign | 0x31c0000000000000L | res;
                } else {    // if exp < 0 and q + exp < 0
                    // the result is +0 or -0
                    __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);
                    return x_sign | 0x31c0000000000000L;
                }
                //break;
            case BID_ROUNDING_DOWN:
                if ((q + exp) > 0) {    // exp < 0 and 1 <= -exp < q
                    // need to shift right -exp digits from the coefficient; exp will be 0
                    ind = -exp;    // 1 <= ind <= 16; ind is a synonym for 'x'
                    // chop off ind digits from the lower part of C1
                    // C1 fits in 64 bits
                    // calculate C* and f*
                    // C* is actually floor(C*) in this case
                    // C* and f* need shifting and masking, as shown by
                    // bid_shiftright128[] and bid_maskhigh128[]
                    // 1 <= x <= 16
                    // kx = 10^(-x) = bid_ten2mk64[ind - 1]
                    // C* = C1 * 10^(-x)
                    // the approximation of 10^(-x) was rounded up to 64 bits

                    //__mul_64x64_to_128(P128, C1, bid_ten2mk64[ind - 1]);
                    {
                        long __CY = bid_ten2mk64[ind - 1];
                        long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
                        __CXH = C1 >>> 32;
                        __CXL = LONG_LOW_PART & C1;
                        __CYH = __CY >>> 32;
                        __CYL = LONG_LOW_PART & __CY;

                        __PM = __CXH * __CYL;
                        __PH = __CXH * __CYH;
                        __PL = __CXL * __CYL;
                        __PM2 = __CXL * __CYH;
                        __PH += (__PM >>> 32);
                        __PM = (LONG_LOW_PART & __PM) + __PM2 + (__PL >>> 32);

                        P128_w1 = __PH + (__PM >>> 32);
                        P128_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
                    }

                    // C* = floor(C*) (logical right shift; C has p decimal digits,
                    //       correct by Property 1)
                    // if (0 < f* < 10^(-x)) then the result is exact
                    // n = C* * 10^(e+x)

                    if (ind - 1 <= 2) {    // 0 <= ind - 1 <= 2 => shift = 0
                        res = P128_w1;
                        fstar_w1 = 0;
                        fstar_w0 = P128_w0;
                    } else if (ind - 1 <= 21) {    // 3 <= ind - 1 <= 21 => 3 <= shift <= 63
                        shift = bid_shiftright128[ind - 1];    // 3 <= shift <= 63
                        res = (P128_w1 >>> shift);
                        fstar_w1 = P128_w1 & bid_maskhigh128[ind - 1];
                        fstar_w0 = P128_w0;
                    }
                    // if (f* > 10^(-x)) then the result is inexact
                    if ((fstar_w1 != 0) || UnsignedLong.isGreaterOrEqual(fstar_w0, bid_ten2mk64[ind - 1])) {
                        if (x_sign != 0) {
                            // if negative and not exact, increment magnitude
                            res++;
                        }
                        __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);
                    }
                    // set exponent to zero as it was negative before.
                    return x_sign | 0x31c0000000000000L | res;
                } else {    // if exp < 0 and q + exp <= 0
                    // the result is +0 or -1
                    __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);
                    return x_sign != 0 ? 0xb1c0000000000001L : 0x31c0000000000000L;
                }
                //break;
            case BID_ROUNDING_UP:
                if ((q + exp) > 0) {    // exp < 0 and 1 <= -exp < q
                    // need to shift right -exp digits from the coefficient; exp will be 0
                    ind = -exp;    // 1 <= ind <= 16; ind is a synonym for 'x'
                    // chop off ind digits from the lower part of C1
                    // C1 fits in 64 bits
                    // calculate C* and f*
                    // C* is actually floor(C*) in this case
                    // C* and f* need shifting and masking, as shown by
                    // bid_shiftright128[] and bid_maskhigh128[]
                    // 1 <= x <= 16
                    // kx = 10^(-x) = bid_ten2mk64[ind - 1]
                    // C* = C1 * 10^(-x)
                    // the approximation of 10^(-x) was rounded up to 64 bits

                    //__mul_64x64_to_128(P128, C1, bid_ten2mk64[ind - 1]);
                    {
                        long __CY = bid_ten2mk64[ind - 1];
                        long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
                        __CXH = C1 >>> 32;
                        __CXL = LONG_LOW_PART & C1;
                        __CYH = __CY >>> 32;
                        __CYL = LONG_LOW_PART & __CY;

                        __PM = __CXH * __CYL;
                        __PH = __CXH * __CYH;
                        __PL = __CXL * __CYL;
                        __PM2 = __CXL * __CYH;
                        __PH += (__PM >>> 32);
                        __PM = (LONG_LOW_PART & __PM) + __PM2 + (__PL >>> 32);

                        P128_w1 = __PH + (__PM >>> 32);
                        P128_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
                    }

                    // C* = floor(C*) (logical right shift; C has p decimal digits,
                    //       correct by Property 1)
                    // if (0 < f* < 10^(-x)) then the result is exact
                    // n = C* * 10^(e+x)

                    if (ind - 1 <= 2) {    // 0 <= ind - 1 <= 2 => shift = 0
                        res = P128_w1;
                        fstar_w1 = 0;
                        fstar_w0 = P128_w0;
                    } else if (ind - 1 <= 21) {    // 3 <= ind - 1 <= 21 => 3 <= shift <= 63
                        shift = bid_shiftright128[ind - 1];    // 3 <= shift <= 63
                        res = (P128_w1 >>> shift);
                        fstar_w1 = P128_w1 & bid_maskhigh128[ind - 1];
                        fstar_w0 = P128_w0;
                    }
                    // if (f* > 10^(-x)) then the result is inexact
                    if ((fstar_w1 != 0) || UnsignedLong.isGreaterOrEqual(fstar_w0, bid_ten2mk64[ind - 1])) {
                        if (x_sign == 0) {
                            // if positive and not exact, increment magnitude
                            res++;
                        }
                        __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);
                    }
                    // set exponent to zero as it was negative before.
                    return x_sign | 0x31c0000000000000L | res;
                } else {    // if exp < 0 and q + exp <= 0
                    // the result is -0 or +1
                    __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);
                    return x_sign != 0 ? 0xb1c0000000000000L : 0x31c0000000000001L;
                }
                //break;
            case BID_ROUNDING_TO_ZERO:
                if ((q + exp) >= 0) {    // exp < 0 and 1 <= -exp <= q
                    // need to shift right -exp digits from the coefficient; exp will be 0
                    ind = -exp;    // 1 <= ind <= 16; ind is a synonym for 'x'
                    // chop off ind digits from the lower part of C1
                    // C1 fits in 127 bits
                    // calculate C* and f*
                    // C* is actually floor(C*) in this case
                    // C* and f* need shifting and masking, as shown by
                    // bid_shiftright128[] and bid_maskhigh128[]
                    // 1 <= x <= 16
                    // kx = 10^(-x) = bid_ten2mk64[ind - 1]
                    // C* = C1 * 10^(-x)
                    // the approximation of 10^(-x) was rounded up to 64 bits

                    //__mul_64x64_to_128(P128, C1, bid_ten2mk64[ind - 1]);
                    {
                        long __CY = bid_ten2mk64[ind - 1];
                        long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
                        __CXH = C1 >>> 32;
                        __CXL = LONG_LOW_PART & C1;
                        __CYH = __CY >>> 32;
                        __CYL = LONG_LOW_PART & __CY;

                        __PM = __CXH * __CYL;
                        __PH = __CXH * __CYH;
                        __PL = __CXL * __CYL;
                        __PM2 = __CXL * __CYH;
                        __PH += (__PM >>> 32);
                        __PM = (LONG_LOW_PART & __PM) + __PM2 + (__PL >>> 32);

                        P128_w1 = __PH + (__PM >>> 32);
                        P128_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
                    }

                    // C* = floor(C*) (logical right shift; C has p decimal digits,
                    //       correct by Property 1)
                    // if (0 < f* < 10^(-x)) then the result is exact
                    // n = C* * 10^(e+x)

                    if (ind - 1 <= 2) {    // 0 <= ind - 1 <= 2 => shift = 0
                        res = P128_w1;
                        fstar_w1 = 0;
                        fstar_w0 = P128_w0;
                    } else if (ind - 1 <= 21) {    // 3 <= ind - 1 <= 21 => 3 <= shift <= 63
                        shift = bid_shiftright128[ind - 1];    // 3 <= shift <= 63
                        res = (P128_w1 >>> shift);
                        fstar_w1 = P128_w1 & bid_maskhigh128[ind - 1];
                        fstar_w0 = P128_w0;
                    }
                    // if (f* > 10^(-x)) then the result is inexact
                    if ((fstar_w1 != 0) || UnsignedLong.isGreaterOrEqual(fstar_w0, bid_ten2mk64[ind - 1])) {
                        __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);
                    }
                    // set exponent to zero as it was negative before.
                    return x_sign | 0x31c0000000000000L | res;
                } else {    // if exp < 0 and q + exp < 0
                    // the result is +0 or -0
                    __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);
                    return x_sign | 0x31c0000000000000L;
                }
                //break;
        }    // end switch ()
        return res;
    }

    public static long bid64_round_integral_nearest_even(long /*BID_UINT64*/ x, final int rnd_mode, final JavaImplParse.FloatingPointStatusFlag pfpsf) {
        long /*BID_UINT64*/ res = 0xbaddbaddbaddbaddL;
        long /*BID_UINT64*/ x_sign;
        int exp;            // unbiased exponent
        // Note: C1.w[1], C1.w[0] represent x_signif_hi, x_signif_lo (all are BID_UINT64)
        //BID_UI64DOUBLE tmp1;
        int x_nr_bits;
        int q, ind, shift;
        long /*BID_UINT64*/ C1;
        long /*BID_UINT128*/ fstar_w0 = 0x0L, fstar_w1 = 0x0L;
        long /*BID_UINT128*/ P128_w0, P128_w1;

        x_sign = x & MASK_SIGN;    // 0 for positive, MASK_SIGN for negative

        // check for NaNs and infinities
        if ((x & MASK_NAN) == MASK_NAN) {    // check for NaN
            if (UnsignedLong.isGreater(x & 0x0003ffffffffffffL, 999999999999999L))
                x = x & 0xfe00000000000000L;    // clear G6-G12 and the payload bits
            else
                x = x & 0xfe03ffffffffffffL;    // clear G6-G12
            if ((x & MASK_SNAN) == MASK_SNAN) {    // SNaN
                // set invalid flag
                __set_status_flags(pfpsf, BID_INVALID_EXCEPTION);
                // return quiet (SNaN)
                res = x & 0xfdffffffffffffffL;
            } else {    // QNaN
                res = x;
            }
            return res;
        } else if ((x & MASK_INF) == MASK_INF) {    // check for Infinity
            return x_sign | 0x7800000000000000L;
        }
        // unpack x
        if ((x & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            // if the steering bits are 11 (condition will be 0), then
            // the exponent is G[0:w+1]
            exp = (int) (((x & MASK_BINARY_EXPONENT2) >>> 51) - 398);
            C1 = (x & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
            if (UnsignedLong.isGreater(C1, 9999999999999999L)) {    // non-canonical
                C1 = 0;
            }
        } else {    // if ((x & MASK_STEERING_BITS) != MASK_STEERING_BITS)
            exp = (int) (((x & MASK_BINARY_EXPONENT1) >>> 53) - 398);
            C1 = (x & MASK_BINARY_SIG1);
        }

        // if x is 0 or non-canonical
        if (C1 == 0) {
            if (exp < 0)
                exp = 0;
            return x_sign | (((long) exp + 398) << 53);
        }
        // x is a finite non-zero number (not 0, non-canonical, or special)

        // return 0 if (exp <= -(p+1))
        if (exp <= -17) {
            return x_sign | 0x31c0000000000000L;
        }
        // q = nr. of decimal digits in x (1 <= q <= 54)
        //  determine first the nr. of bits in x
        if (UnsignedLong.isGreaterOrEqual(C1, 0x0020000000000000L)) {    // x >= 2^53
            q = 16;
        } else {    // if x < 2^53
            final long tmp1_ui64 = Double.doubleToRawLongBits((double) C1);    // exact conversion
            x_nr_bits = 1 + ((((/*unsigned*/ int) (tmp1_ui64 >>> 52)) & 0x7ff) - 0x3ff);
            q = (int) bid_nr_digits_flat[((x_nr_bits - 1) << 2) /*+ 0 .digits*/];
            if (q == 0) {
                q = (int) bid_nr_digits_flat[((x_nr_bits - 1) << 2) + 3 /*.digits1*/];
                if (UnsignedLong.isGreaterOrEqual(C1, bid_nr_digits_flat[((x_nr_bits - 1) << 2) + 2 /*.threshold_lo*/]))
                    q++;
            }
        }

        if (exp >= 0) {    // -exp <= 0
            // the argument is an integer already
            return x;
        } else if ((q + exp) >= 0) {    // exp < 0 and 1 <= -exp <= q
            // need to shift right -exp digits from the coefficient; the exp will be 0
            ind = -exp;    // 1 <= ind <= 16; ind is a synonym for 'x'
            // chop off ind digits from the lower part of C1
            // C1 = C1 + 1/2 * 10^x where the result C1 fits in 64 bits
            // FOR ROUND_TO_NEAREST, WE ADD 1/2 ULP(y) then truncate
            C1 = C1 + bid_midpoint64[ind - 1];
            // calculate C* and f*
            // C* is actually floor(C*) in this case
            // C* and f* need shifting and masking, as shown by
            // bid_shiftright128[] and bid_maskhigh128[]
            // 1 <= x <= 16
            // kx = 10^(-x) = bid_ten2mk64[ind - 1]
            // C* = (C1 + 1/2 * 10^x) * 10^(-x)
            // the approximation of 10^(-x) was rounded up to 64 bits

            //__mul_64x64_to_128(P128, C1, bid_ten2mk64[ind - 1]);
            {
                long __CY = bid_ten2mk64[ind - 1];
                long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
                __CXH = C1 >>> 32;
                __CXL = LONG_LOW_PART & C1;
                __CYH = __CY >>> 32;
                __CYL = LONG_LOW_PART & __CY;

                __PM = __CXH * __CYL;
                __PH = __CXH * __CYH;
                __PL = __CXL * __CYL;
                __PM2 = __CXL * __CYH;
                __PH += (__PM >>> 32);
                __PM = (LONG_LOW_PART & __PM) + __PM2 + (__PL >>> 32);

                P128_w1 = __PH + (__PM >>> 32);
                P128_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
            }

            // if (0 < f* < 10^(-x)) then the result is a midpoint
            //   if floor(C*) is even then C* = floor(C*) - logical right
            //       shift; C* has p decimal digits, correct by Prop. 1)
            //   else if floor(C*) is odd C* = floor(C*)-1 (logical right
            //       shift; C* has p decimal digits, correct by Pr. 1)
            // else
            //   C* = floor(C*) (logical right shift; C has p decimal digits,
            //       correct by Property 1)
            // n = C* * 10^(e+x)

            if (ind - 1 <= 2) {    // 0 <= ind - 1 <= 2 => shift = 0
                res = P128_w1;
                fstar_w1 = 0;
                fstar_w0 = P128_w0;
            } else if (ind - 1 <= 21) {    // 3 <= ind - 1 <= 21 => 3 <= shift <= 63
                shift = bid_shiftright128[ind - 1];    // 3 <= shift <= 63
                res = (P128_w1 >>> shift);
                fstar_w1 = P128_w1 & bid_maskhigh128[ind - 1];
                fstar_w0 = P128_w0;
            }
            // if (0 < f* < 10^(-x)) then the result is a midpoint
            // since round_to_even, subtract 1 if current result is odd
            if ((res & 0x0000000000000001L) != 0 && (fstar_w1 == 0) && UnsignedLong.isLess(fstar_w0, bid_ten2mk64[ind - 1])) {
                res--;
            }
            // set exponent to zero as it was negative before.
            return x_sign | 0x31c0000000000000L | res;
        } else {    // if exp < 0 and q + exp < 0
            // the result is +0 or -0
            return x_sign | 0x31c0000000000000L;
        }
    }

    public static long bid64_round_integral_negative(long /*BID_UINT64*/ x, final int rnd_mode, final JavaImplParse.FloatingPointStatusFlag pfpsf) {
        long /*BID_UINT64*/ res = 0xbaddbaddbaddbaddL;
        long /*BID_UINT64*/ x_sign;
        int exp;            // unbiased exponent
        // Note: C1.w[1], C1.w[0] represent x_signif_hi, x_signif_lo (all are BID_UINT64)
        //BID_UI64DOUBLE tmp1;
        int x_nr_bits;
        int q, ind, shift;
        long /*BID_UINT64*/ C1;
        // BID_UINT64 res is C* at first - represents up to 34 decimal digits ~ 113 bits
        long /*BID_UINT128*/ fstar_w0 = 0x0L, fstar_w1 = 0x0L;
        long /*BID_UINT128*/ P128_w0, P128_w1;

        x_sign = x & MASK_SIGN;    // 0 for positive, MASK_SIGN for negative

        // check for NaNs and infinities
        if ((x & MASK_NAN) == MASK_NAN) {    // check for NaN
            if (UnsignedLong.isGreater(x & 0x0003ffffffffffffL, 999999999999999L))
                x = x & 0xfe00000000000000L;    // clear G6-G12 and the payload bits
            else
                x = x & 0xfe03ffffffffffffL;    // clear G6-G12
            if ((x & MASK_SNAN) == MASK_SNAN) {    // SNaN
                // set invalid flag
                __set_status_flags(pfpsf, BID_INVALID_EXCEPTION);
                // return quiet (SNaN)
                res = x & 0xfdffffffffffffffL;
            } else {    // QNaN
                res = x;
            }
            return res;
        } else if ((x & MASK_INF) == MASK_INF) {    // check for Infinity
            return x_sign | 0x7800000000000000L;
        }
        // unpack x
        if ((x & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            // if the steering bits are 11 (condition will be 0), then
            // the exponent is G[0:w+1]
            exp = (int) (((x & MASK_BINARY_EXPONENT2) >>> 51) - 398);
            C1 = (x & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
            if (UnsignedLong.isGreater(C1, 9999999999999999L)) {    // non-canonical
                C1 = 0;
            }
        } else {    // if ((x & MASK_STEERING_BITS) != MASK_STEERING_BITS)
            exp = (int) (((x & MASK_BINARY_EXPONENT1) >>> 53) - 398);
            C1 = (x & MASK_BINARY_SIG1);
        }

        // if x is 0 or non-canonical
        if (C1 == 0) {
            if (exp < 0)
                exp = 0;
            return x_sign | (((long) exp + 398) << 53);
        }
        // x is a finite non-zero number (not 0, non-canonical, or special)

        // return 0 if (exp <= -p)
        if (exp <= -16) {
            if (x_sign != 0) {
                res = 0xb1c0000000000001L;
            } else {
                res = 0x31c0000000000000L;
            }
            return res;
        }
        // q = nr. of decimal digits in x (1 <= q <= 54)
        //  determine first the nr. of bits in x
        if (UnsignedLong.isGreaterOrEqual(C1, 0x0020000000000000L)) {    // x >= 2^53
            q = 16;
        } else {    // if x < 2^53
            final long tmp1_ui64 = Double.doubleToRawLongBits((double) C1);    // exact conversion
            x_nr_bits = 1 + ((((/*unsigned*/ int) (tmp1_ui64 >>> 52)) & 0x7ff) - 0x3ff);
            q = (int) bid_nr_digits_flat[((x_nr_bits - 1) << 2) /*+ 0 .digits*/];
            if (q == 0) {
                q = (int) bid_nr_digits_flat[((x_nr_bits - 1) << 2) + 3 /*.digits1*/];
                if (UnsignedLong.isGreaterOrEqual(C1, bid_nr_digits_flat[((x_nr_bits - 1) << 2) + 2 /*.threshold_lo*/]))
                    q++;
            }
        }

        if (exp >= 0) {    // -exp <= 0
            // the argument is an integer already
            return x;
        } else if ((q + exp) > 0) {    // exp < 0 and 1 <= -exp < q
            // need to shift right -exp digits from the coefficient; the exp will be 0
            ind = -exp;    // 1 <= ind <= 16; ind is a synonym for 'x'
            // chop off ind digits from the lower part of C1
            // C1 fits in 64 bits
            // calculate C* and f*
            // C* is actually floor(C*) in this case
            // C* and f* need shifting and masking, as shown by
            // bid_shiftright128[] and bid_maskhigh128[]
            // 1 <= x <= 16
            // kx = 10^(-x) = bid_ten2mk64[ind - 1]
            // C* = C1 * 10^(-x)
            // the approximation of 10^(-x) was rounded up to 64 bits

            //__mul_64x64_to_128(P128, C1, bid_ten2mk64[ind - 1]);
            {
                long __CY = bid_ten2mk64[ind - 1];
                long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
                __CXH = C1 >>> 32;
                __CXL = LONG_LOW_PART & C1;
                __CYH = __CY >>> 32;
                __CYL = LONG_LOW_PART & __CY;

                __PM = __CXH * __CYL;
                __PH = __CXH * __CYH;
                __PL = __CXL * __CYL;
                __PM2 = __CXL * __CYH;
                __PH += (__PM >>> 32);
                __PM = (LONG_LOW_PART & __PM) + __PM2 + (__PL >>> 32);

                P128_w1 = __PH + (__PM >>> 32);
                P128_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
            }

            // C* = floor(C*) (logical right shift; C has p decimal digits,
            //       correct by Property 1)
            // if (0 < f* < 10^(-x)) then the result is exact
            // n = C* * 10^(e+x)

            if (ind - 1 <= 2) {    // 0 <= ind - 1 <= 2 => shift = 0
                res = P128_w1;
                fstar_w1 = 0;
                fstar_w0 = P128_w0;
            } else if (ind - 1 <= 21) {    // 3 <= ind - 1 <= 21 => 3 <= shift <= 63
                shift = bid_shiftright128[ind - 1];    // 3 <= shift <= 63
                res = (P128_w1 >>> shift);
                fstar_w1 = P128_w1 & bid_maskhigh128[ind - 1];
                fstar_w0 = P128_w0;
            }
            // if (f* > 10^(-x)) then the result is inexact
            if (x_sign != 0 && ((fstar_w1 != 0) || UnsignedLong.isGreaterOrEqual(fstar_w0, bid_ten2mk64[ind - 1]))) {
                // if negative and not exact, increment magnitude
                res++;
            }
            // set exponent to zero as it was negative before.
            return x_sign | 0x31c0000000000000L | res;
        } else {    // if exp < 0 and q + exp <= 0
            // the result is +0 or -1
            if (x_sign != 0) {
                res = 0xb1c0000000000001L;
            } else {
                res = 0x31c0000000000000L;
            }
            return res;
        }
    }

    public static long bid64_round_integral_positive(long /*BID_UINT64*/ x, final int rnd_mode, final JavaImplParse.FloatingPointStatusFlag pfpsf) {
        long /*BID_UINT64*/ res = 0xbaddbaddbaddbaddL;
        long /*BID_UINT64*/ x_sign;
        int exp;            // unbiased exponent
        // Note: C1.w[1], C1.w[0] represent x_signif_hi, x_signif_lo (all are BID_UINT64)
        //BID_UI64DOUBLE tmp1;
        int x_nr_bits;
        int q, ind, shift;
        long /*BID_UINT64*/ C1;
        // BID_UINT64 res is C* at first - represents up to 34 decimal digits ~ 113 bits
        long /*BID_UINT128*/ fstar_w0 = 0x0L, fstar_w1 = 0x0L;
        long /*BID_UINT128*/ P128_w0, P128_w1;

        x_sign = x & MASK_SIGN;    // 0 for positive, MASK_SIGN for negative

        // check for NaNs and infinities
        if ((x & MASK_NAN) == MASK_NAN) {    // check for NaN
            if (UnsignedLong.isGreater(x & 0x0003ffffffffffffL, 999999999999999L))
                x = x & 0xfe00000000000000L;    // clear G6-G12 and the payload bits
            else
                x = x & 0xfe03ffffffffffffL;    // clear G6-G12
            if ((x & MASK_SNAN) == MASK_SNAN) {    // SNaN
                // set invalid flag
                __set_status_flags(pfpsf, BID_INVALID_EXCEPTION);
                // return quiet (SNaN)
                res = x & 0xfdffffffffffffffL;
            } else {    // QNaN
                res = x;
            }
            return res;
        } else if ((x & MASK_INF) == MASK_INF) {    // check for Infinity
            return x_sign | 0x7800000000000000L;
        }
        // unpack x
        if ((x & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            // if the steering bits are 11 (condition will be 0), then
            // the exponent is G[0:w+1]
            exp = (int) (((x & MASK_BINARY_EXPONENT2) >>> 51) - 398);
            C1 = (x & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
            if (UnsignedLong.isGreater(C1, 9999999999999999L)) {    // non-canonical
                C1 = 0;
            }
        } else {    // if ((x & MASK_STEERING_BITS) != MASK_STEERING_BITS)
            exp = (int) (((x & MASK_BINARY_EXPONENT1) >>> 53) - 398);
            C1 = (x & MASK_BINARY_SIG1);
        }

        // if x is 0 or non-canonical
        if (C1 == 0) {
            if (exp < 0)
                exp = 0;
            return x_sign | (((long) exp + 398) << 53);
        }
        // x is a finite non-zero number (not 0, non-canonical, or special)

        // return 0 if (exp <= -p)
        if (exp <= -16) {
            if (x_sign != 0) {
                res = 0xb1c0000000000000L;
            } else {
                res = 0x31c0000000000001L;
            }
            return res;
        }
        // q = nr. of decimal digits in x (1 <= q <= 54)
        //  determine first the nr. of bits in x
        if (UnsignedLong.isGreaterOrEqual(C1, 0x0020000000000000L)) {    // x >= 2^53
            q = 16;
        } else {    // if x < 2^53
            final long tmp1_ui64 = Double.doubleToRawLongBits((double) C1);    // exact conversion
            x_nr_bits = 1 + ((((/*unsigned*/ int) (tmp1_ui64 >>> 52)) & 0x7ff) - 0x3ff);
            q = (int) bid_nr_digits_flat[((x_nr_bits - 1) << 2) /*+ 0 .digits*/];
            if (q == 0) {
                q = (int) bid_nr_digits_flat[((x_nr_bits - 1) << 2) + 3 /*.digits1*/];
                if (UnsignedLong.isGreaterOrEqual(C1, bid_nr_digits_flat[((x_nr_bits - 1) << 2) + 2 /*.threshold_lo*/]))
                    q++;
            }
        }

        if (exp >= 0) {    // -exp <= 0
            // the argument is an integer already
            return x;
        } else if ((q + exp) > 0) {    // exp < 0 and 1 <= -exp < q
            // need to shift right -exp digits from the coefficient; the exp will be 0
            ind = -exp;    // 1 <= ind <= 16; ind is a synonym for 'x'
            // chop off ind digits from the lower part of C1
            // C1 fits in 64 bits
            // calculate C* and f*
            // C* is actually floor(C*) in this case
            // C* and f* need shifting and masking, as shown by
            // bid_shiftright128[] and bid_maskhigh128[]
            // 1 <= x <= 16
            // kx = 10^(-x) = bid_ten2mk64[ind - 1]
            // C* = C1 * 10^(-x)
            // the approximation of 10^(-x) was rounded up to 64 bits

            //__mul_64x64_to_128(P128, C1, bid_ten2mk64[ind - 1]);
            {
                long __CY = bid_ten2mk64[ind - 1];
                long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
                __CXH = C1 >>> 32;
                __CXL = LONG_LOW_PART & C1;
                __CYH = __CY >>> 32;
                __CYL = LONG_LOW_PART & __CY;

                __PM = __CXH * __CYL;
                __PH = __CXH * __CYH;
                __PL = __CXL * __CYL;
                __PM2 = __CXL * __CYH;
                __PH += (__PM >>> 32);
                __PM = (LONG_LOW_PART & __PM) + __PM2 + (__PL >>> 32);

                P128_w1 = __PH + (__PM >>> 32);
                P128_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
            }

            // C* = floor(C*) (logical right shift; C has p decimal digits,
            //       correct by Property 1)
            // if (0 < f* < 10^(-x)) then the result is exact
            // n = C* * 10^(e+x)

            if (ind - 1 <= 2) {    // 0 <= ind - 1 <= 2 => shift = 0
                res = P128_w1;
                fstar_w1 = 0;
                fstar_w0 = P128_w0;
            } else if (ind - 1 <= 21) {    // 3 <= ind - 1 <= 21 => 3 <= shift <= 63
                shift = bid_shiftright128[ind - 1];    // 3 <= shift <= 63
                res = (P128_w1 >>> shift);
                fstar_w1 = P128_w1 & bid_maskhigh128[ind - 1];
                fstar_w0 = P128_w0;
            }
            // if (f* > 10^(-x)) then the result is inexact
            if (x_sign == 0 && ((fstar_w1 != 0) || UnsignedLong.isGreaterOrEqual(fstar_w0, bid_ten2mk64[ind - 1]))) {
                // if positive and not exact, increment magnitude
                res++;
            }
            // set exponent to zero as it was negative before.
            return x_sign | 0x31c0000000000000L | res;
        } else {    // if exp < 0 and q + exp <= 0
            // the result is -0 or +1
            if (x_sign != 0) {
                res = 0xb1c0000000000000L;
            } else {
                res = 0x31c0000000000001L;
            }
            return res;
        }
    }

    public static long bid64_round_integral_zero(long /*BID_UINT64*/ x, final int rnd_mode, final JavaImplParse.FloatingPointStatusFlag pfpsf) {
        long /*BID_UINT64*/ res = 0xbaddbaddbaddbaddL;
        long /*BID_UINT64*/ x_sign;
        int exp;            // unbiased exponent
        // Note: C1.w[1], C1.w[0] represent x_signif_hi, x_signif_lo (all are BID_UINT64)
        //BID_UI64DOUBLE tmp1;
        int x_nr_bits;
        int q, ind, shift;
        long /*BID_UINT64*/ C1;
        // BID_UINT64 res is C* at first - represents up to 34 decimal digits ~ 113 bits
        long /*BID_UINT128*/ P128_w0, P128_w1;

        x_sign = x & MASK_SIGN;    // 0 for positive, MASK_SIGN for negative

        // check for NaNs and infinities
        if ((x & MASK_NAN) == MASK_NAN) {    // check for NaN
            if (UnsignedLong.isGreater(x & 0x0003ffffffffffffL, 999999999999999L))
                x = x & 0xfe00000000000000L;    // clear G6-G12 and the payload bits
            else
                x = x & 0xfe03ffffffffffffL;    // clear G6-G12
            if ((x & MASK_SNAN) == MASK_SNAN) {    // SNaN
                // set invalid flag
                __set_status_flags(pfpsf, BID_INVALID_EXCEPTION);
                // return quiet (SNaN)
                res = x & 0xfdffffffffffffffL;
            } else {    // QNaN
                res = x;
            }
            return res;
        } else if ((x & MASK_INF) == MASK_INF) {    // check for Infinity
            return x_sign | 0x7800000000000000L;
        }
        // unpack x
        if ((x & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            // if the steering bits are 11 (condition will be 0), then
            // the exponent is G[0:w+1]
            exp = (int) (((x & MASK_BINARY_EXPONENT2) >>> 51) - 398);
            C1 = (x & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
            if (UnsignedLong.isGreater(C1, 9999999999999999L)) {    // non-canonical
                C1 = 0;
            }
        } else {    // if ((x & MASK_STEERING_BITS) != MASK_STEERING_BITS)
            exp = (int) (((x & MASK_BINARY_EXPONENT1) >>> 53) - 398);
            C1 = (x & MASK_BINARY_SIG1);
        }

        // if x is 0 or non-canonical
        if (C1 == 0) {
            if (exp < 0)
                exp = 0;
            return x_sign | (((long) exp + 398) << 53);
        }
        // x is a finite non-zero number (not 0, non-canonical, or special)

        // return 0 if (exp <= -p)
        if (exp <= -16) {
            return x_sign | 0x31c0000000000000L;
        }
        // q = nr. of decimal digits in x (1 <= q <= 54)
        //  determine first the nr. of bits in x
        if (UnsignedLong.isGreaterOrEqual(C1, 0x0020000000000000L)) {    // x >= 2^53
            q = 16;
        } else {    // if x < 2^53
            final long tmp1_ui64 = Double.doubleToRawLongBits((double) C1);    // exact conversion
            x_nr_bits = 1 + ((((/*unsigned*/ int) (tmp1_ui64 >>> 52)) & 0x7ff) - 0x3ff);
            q = (int) bid_nr_digits_flat[((x_nr_bits - 1) << 2) /*+ 0 .digits*/];
            if (q == 0) {
                q = (int) bid_nr_digits_flat[((x_nr_bits - 1) << 2) + 3 /*.digits1*/];
                if (UnsignedLong.isGreaterOrEqual(C1, bid_nr_digits_flat[((x_nr_bits - 1) << 2) + 2 /*.threshold_lo*/]))
                    q++;
            }
        }

        if (exp >= 0) {    // -exp <= 0
            // the argument is an integer already
            return x;
        } else if ((q + exp) >= 0) {    // exp < 0 and 1 <= -exp <= q
            // need to shift right -exp digits from the coefficient; the exp will be 0
            ind = -exp;    // 1 <= ind <= 16; ind is a synonym for 'x'
            // chop off ind digits from the lower part of C1
            // C1 fits in 127 bits
            // calculate C* and f*
            // C* is actually floor(C*) in this case
            // C* and f* need shifting and masking, as shown by
            // bid_shiftright128[] and bid_maskhigh128[]
            // 1 <= x <= 16
            // kx = 10^(-x) = bid_ten2mk64[ind - 1]
            // C* = C1 * 10^(-x)
            // the approximation of 10^(-x) was rounded up to 64 bits

            //__mul_64x64_to_128(P128, C1, bid_ten2mk64[ind - 1]);
            {
                long __CY = bid_ten2mk64[ind - 1];
                long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
                __CXH = C1 >>> 32;
                __CXL = LONG_LOW_PART & C1;
                __CYH = __CY >>> 32;
                __CYL = LONG_LOW_PART & __CY;

                __PM = __CXH * __CYL;
                __PH = __CXH * __CYH;
                __PL = __CXL * __CYL;
                __PM2 = __CXL * __CYH;
                __PH += (__PM >>> 32);
                __PM = (LONG_LOW_PART & __PM) + __PM2 + (__PL >>> 32);

                P128_w1 = __PH + (__PM >>> 32);
                P128_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
            }

            // C* = floor(C*) (logical right shift; C has p decimal digits,
            //       correct by Property 1)
            // if (0 < f* < 10^(-x)) then the result is exact
            // n = C* * 10^(e+x)

            if (ind - 1 <= 2) {    // 0 <= ind - 1 <= 2 => shift = 0
                res = P128_w1;
                // redundant fstar.w[1] = 0;
                // redundant fstar.w[0] = P128.w[0];
            } else if (ind - 1 <= 21) {    // 3 <= ind - 1 <= 21 => 3 <= shift <= 63
                shift = bid_shiftright128[ind - 1];    // 3 <= shift <= 63
                res = (P128_w1 >>> shift);
                // redundant fstar.w[1] = P128.w[1] & bid_maskhigh128[ind - 1];
                // redundant fstar.w[0] = P128.w[0];
            }
            // if (f* > 10^(-x)) then the result is inexact
            // if ((fstar.w[1] != 0) || (fstar.w[0] >= bid_ten2mk64[ind-1])){
            //   // redundant
            // }
            // set exponent to zero as it was negative before.
            return x_sign | 0x31c0000000000000L | res;
        } else {    // if exp < 0 and q + exp < 0
            // the result is +0 or -0
            return x_sign | 0x31c0000000000000L;
        }
    }

    public static long bid64_round_integral_nearest_away(long /*BID_UINT64*/ x, final int rnd_mode, final JavaImplParse.FloatingPointStatusFlag pfpsf) {
        long /*BID_UINT64*/ res = 0xbaddbaddbaddbaddL;
        long /*BID_UINT64*/ x_sign;
        int exp;            // unbiased exponent
        // Note: C1.w[1], C1.w[0] represent x_signif_hi, x_signif_lo (all are BID_UINT64)
        //BID_UI64DOUBLE tmp1;
        int x_nr_bits;
        int q, ind, shift;
        long /*BID_UINT64*/ C1;
        long /*BID_UINT128*/ P128_w0, P128_w1;

        x_sign = x & MASK_SIGN;    // 0 for positive, MASK_SIGN for negative

        // check for NaNs and infinities
        if ((x & MASK_NAN) == MASK_NAN) {    // check for NaN
            if (UnsignedLong.isGreater(x & 0x0003ffffffffffffL, 999999999999999L))
                x = x & 0xfe00000000000000L;    // clear G6-G12 and the payload bits
            else
                x = x & 0xfe03ffffffffffffL;    // clear G6-G12
            if ((x & MASK_SNAN) == MASK_SNAN) {    // SNaN
                // set invalid flag
                __set_status_flags(pfpsf, BID_INVALID_EXCEPTION);
                // return quiet (SNaN)
                res = x & 0xfdffffffffffffffL;
            } else {    // QNaN
                res = x;
            }
            return res;
        } else if ((x & MASK_INF) == MASK_INF) {    // check for Infinity
            return x_sign | 0x7800000000000000L;
        }
        // unpack x
        if ((x & MASK_STEERING_BITS) == MASK_STEERING_BITS) {
            // if the steering bits are 11 (condition will be 0), then
            // the exponent is G[0:w+1]
            exp = (int) (((x & MASK_BINARY_EXPONENT2) >>> 51) - 398);
            C1 = (x & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
            if (UnsignedLong.isGreater(C1, 9999999999999999L)) {    // non-canonical
                C1 = 0;
            }
        } else {    // if ((x & MASK_STEERING_BITS) != MASK_STEERING_BITS)
            exp = (int) (((x & MASK_BINARY_EXPONENT1) >>> 53) - 398);
            C1 = (x & MASK_BINARY_SIG1);
        }

        // if x is 0 or non-canonical
        if (C1 == 0) {
            if (exp < 0)
                exp = 0;
            return x_sign | (((long) exp + 398) << 53);
        }
        // x is a finite non-zero number (not 0, non-canonical, or special)

        // return 0 if (exp <= -(p+1))
        if (exp <= -17) {
            return x_sign | 0x31c0000000000000L;
        }
        // q = nr. of decimal digits in x (1 <= q <= 54)
        //  determine first the nr. of bits in x
        if (UnsignedLong.isGreaterOrEqual(C1, 0x0020000000000000L)) {    // x >= 2^53
            q = 16;
        } else {    // if x < 2^53
            final long tmp1_ui64 = Double.doubleToRawLongBits((double) C1);    // exact conversion
            x_nr_bits = 1 + ((((/*unsigned*/ int) (tmp1_ui64 >>> 52)) & 0x7ff) - 0x3ff);
            q = (int) bid_nr_digits_flat[((x_nr_bits - 1) << 2) /*+ 0 .digits*/];
            if (q == 0) {
                q = (int) bid_nr_digits_flat[((x_nr_bits - 1) << 2) + 3 /*.digits1*/];
                if (UnsignedLong.isGreaterOrEqual(C1, bid_nr_digits_flat[((x_nr_bits - 1) << 2) + 2 /*.threshold_lo*/]))
                    q++;
            }
        }

        if (exp >= 0) {    // -exp <= 0
            // the argument is an integer already
            return x;
        } else if ((q + exp) >= 0) {    // exp < 0 and 1 <= -exp <= q
            // need to shift right -exp digits from the coefficient; the exp will be 0
            ind = -exp;    // 1 <= ind <= 16; ind is a synonym for 'x'
            // chop off ind digits from the lower part of C1
            // C1 = C1 + 1/2 * 10^x where the result C1 fits in 64 bits
            // FOR ROUND_TO_NEAREST, WE ADD 1/2 ULP(y) then truncate
            C1 = C1 + bid_midpoint64[ind - 1];
            // calculate C* and f*
            // C* is actually floor(C*) in this case
            // C* and f* need shifting and masking, as shown by
            // bid_shiftright128[] and bid_maskhigh128[]
            // 1 <= x <= 16
            // kx = 10^(-x) = bid_ten2mk64[ind - 1]
            // C* = (C1 + 1/2 * 10^x) * 10^(-x)
            // the approximation of 10^(-x) was rounded up to 64 bits

            //__mul_64x64_to_128(P128, C1, bid_ten2mk64[ind - 1]);
            {
                long __CY = bid_ten2mk64[ind - 1];
                long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
                __CXH = C1 >>> 32;
                __CXL = LONG_LOW_PART & C1;
                __CYH = __CY >>> 32;
                __CYL = LONG_LOW_PART & __CY;

                __PM = __CXH * __CYL;
                __PH = __CXH * __CYH;
                __PL = __CXL * __CYL;
                __PM2 = __CXL * __CYH;
                __PH += (__PM >>> 32);
                __PM = (LONG_LOW_PART & __PM) + __PM2 + (__PL >>> 32);

                P128_w1 = __PH + (__PM >>> 32);
                P128_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
            }

            // if (0 < f* < 10^(-x)) then the result is a midpoint
            //   C* = floor(C*) - logical right shift; C* has p decimal digits,
            //       correct by Prop. 1)
            // else
            //   C* = floor(C*) (logical right shift; C has p decimal digits,
            //       correct by Property 1)
            // n = C* * 10^(e+x)

            if (ind - 1 <= 2) {    // 0 <= ind - 1 <= 2 => shift = 0
                res = P128_w1;
            } else if (ind - 1 <= 21) {    // 3 <= ind - 1 <= 21 => 3 <= shift <= 63
                shift = bid_shiftright128[ind - 1];    // 3 <= shift <= 63
                res = (P128_w1 >>> shift);
            }
            // midpoints are already rounded correctly
            // set exponent to zero as it was negative before.
            return x_sign | 0x31c0000000000000L | res;
        } else {    // if exp < 0 and q + exp < 0
            // the result is +0 or -0
            return x_sign | 0x31c0000000000000L;
        }
    }

    // bid_onehalf128[] contains the high bits of 1/2 positioned correctly for
    // comparison with the high bits of f2*
    // the 64-bit word order is L, H
    public static final long[] /*BID_UINT64*/ bid_onehalf128 = {
        0x0000000000000000L,    //  0 bits
        0x0000000000000000L,    //  0 bits
        0x0000000000000000L,    //  0 bits
        0x0000000000000004L,    //  3 bits
        0x0000000000000020L,    //  6 bits
        0x0000000000000100L,    //  9 bits
        0x0000000000001000L,    // 13 bits
        0x0000000000008000L,    // 16 bits
        0x0000000000040000L,    // 19 bits
        0x0000000000400000L,    // 23 bits
        0x0000000002000000L,    // 26 bits
        0x0000000010000000L,    // 29 bits
        0x0000000100000000L,    // 33 bits
        0x0000000800000000L,    // 36 bits
        0x0000004000000000L,    // 39 bits
        0x0000040000000000L,    // 43 bits
        0x0000200000000000L,    // 46 bits
        0x0001000000000000L,    // 49 bits
        0x0010000000000000L,    // 53 bits
        0x0080000000000000L,    // 56 bits
        0x0400000000000000L,    // 59 bits
        0x4000000000000000L,    // 63 bits
        0x0000000000000002L,    // 66 bits
        0x0000000000000010L,    // 69 bits
        0x0000000000000100L,    // 73 bits
        0x0000000000000800L,    // 76 bits
        0x0000000000004000L,    // 79 bits
        0x0000000000040000L,    // 83 bits
        0x0000000000200000L,    // 86 bits
        0x0000000001000000L,    // 89 bits
        0x0000000008000000L,    // 92 bits
        0x0000000080000000L,    // 96 bits
        0x0000000400000000L,    // 99 bits
        0x0000002000000000L     // 102 bits
    };
}
