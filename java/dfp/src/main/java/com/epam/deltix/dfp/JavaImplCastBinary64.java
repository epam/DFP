package com.epam.deltix.dfp;

import java.io.InputStream;
import java.util.Arrays;

import static com.epam.deltix.dfp.JavaImplAdd.LONG_LOW_PART;
import static com.epam.deltix.dfp.JavaImplParse.*;

public class JavaImplCastBinary64 {
    private JavaImplCastBinary64() {
    }

    public static long binary64_to_bid64(double x, final int rnd_mode/*, final JavaImplParse.FloatingPointStatusFlag pfpsf*/) {
        long /*BID_UINT128*/ c_w0, c_w1;
        long /*BID_UINT64*/ c_prov;
        long /*BID_UINT128*/ m_min_w0, m_min_w1;
        long /*BID_UINT256*/ r_w0, r_w1, r_w2, r_w3;
        long /*BID_UINT384*/ z_w0, z_w1, z_w2, z_w3, z_w4, z_w5;

        int e, s, t, e_out;

        // Unpack the input

        //unpack_binary64 (x, s, e, c.w[1], t, return_bid64_zero (s), return_bid64_inf (s), return_bid64_nan);
        {
            //union { BID_UINT64 i; double f; } x_in;
            //x_in.f = x;
            //c = x_in.i;
            c_w1 = Double.doubleToRawLongBits(x);
            e = (int) ((c_w1 >>> 52) & ((1L << 11) - 1));
            s = (int) (c_w1 >>> 63);
            c_w1 = c_w1 & ((1L << 52) - 1);
            if (e == 0) {
                int l;
                if (c_w1 == 0) return return_bid64_zero(s);
                l = clz64(c_w1) - (64 - 53);
                c_w1 = c_w1 << l;
                e = -(l + 1074);
                t = 0;
                //__set_status_flags(pfpsf, BID_DENORMAL_EXCEPTION);
            } else if (e == ((1L << 11) - 1)) {
                if (c_w1 == 0) return return_bid64_inf(s);
                //if ((c_w1 & (1L << 51)) == 0) __set_status_flags(pfpsf, BID_INVALID_EXCEPTION);
                return return_bid64_nan(s, c_w1 << 13, 0L);
            } else {
                c_w1 += 1L << 52;
                t = ctz64(c_w1);
                e -= 1075;
            }
        }


        // Now -1126<=e<=971 (971 for max normal, -1074 for min normal, -1126 for min denormal)

        // Treat like a quad input for uniformity, so (2^{113-53} * c * r) >> 312
        // (312 is the shift value for these tables) which can be written as
        // (2^68 c * r) >> 320, lopping off exactly 320 bits = 5 words. Thus we put
        // input coefficient as the high part of c (<<64) shifted by 4 bits (<<68)
        //
        // Remember to compensate for the fact that exponents are integer for quad

        c_w1 = c_w1 << 4;
        c_w0 = 0;
        t += (113 - 53);
        e -= (113 - 53); // Now e belongs [-1186;911].

        // Check for "trivial" overflow, when 2^e * 2^112 > 10^emax * 10^d.
        // We actually check if e >= ceil((emax + d) * log_2(10) - 112)
        // This could be intercepted later, but it's convenient to keep tables smaller

        // Now filter out all the exact cases where we need to specially force
        // the exponent to 0. We can let through inexact cases and those where the
        // main path will do the right thing anyway, e.g. integers outside coeff range.
        //
        // First check that e <= 0, because if e > 0, the input must be >= 2^113,
        // which is too large for the coefficient of any target decimal format.
        // We write a = -(e + t)
        //
        // (1) If e + t >= 0 <=> a <= 0 the input is an integer; treat it specially
        //     iff it fits in the coefficient range. Shift c' = c >> -e, and
        //     compare with the coefficient range; if it's in range then c' is
        //     our coefficient, exponent is 0. Otherwise we pass through.
        //
        // (2) If a > 0 then we have a non-integer input. The special case would
        //     arise as c' / 2^a where c' = c >> t, i.e. 10^-a * (5^a c'). Now
        //     if a > 48 we can immediately forget this, since 5^49 > 10^34.
        //     Otherwise we determine whether we're in range by a table based on
        //     a, and if so get the multiplier also from a table based on a.
        //
        // Note that when we shift, we need to take into account the fact that
        // c is already 8 places to the left in preparation for the reciprocal
        // multiplication; thus we add 8 to all the shift counts

        if (e <= 0) {
            long /*BID_UINT128*/ cint_w1 = c_w1, cint_w0 = c_w0;
            int a = -(e + t);
            if (a <= 0) {
                //srl128 (cint_w1, cint_w0, 8 - e);
                {
                    final int __c = 8 - e;
                    // if (__c != 0) // No need for condition check: it is always true
                    {
                        if (__c >= 64) {
                            cint_w0 = cint_w1 >>> (__c - 64);
                            cint_w1 = 0;
                        } else {
                            //srl128_short(cint_w1, cint_w0, __c);
                            cint_w0 = (cint_w1 << (64 - __c)) + (cint_w0 >>> __c);
                            cint_w1 = cint_w1 >>> __c;
                        }
                    }
                }

                if ((cint_w1 == 0) && UnsignedLong.isLess(cint_w0, 10000000000000000L))
                    return return_bid64(s, 398, cint_w0);
            } else if (a <= 48) {
                final int bid_coefflimits_bid64_index = a << 1;
                long /*BID_UINT128*/ pow5_w0 = bid_coefflimits_bid64_flat[bid_coefflimits_bid64_index],
                    pow5_w1 = bid_coefflimits_bid64_flat[bid_coefflimits_bid64_index + 1];
                //srl128 (cint_w1, cint_w0, 8 + t);
                {
                    final int __c = 8 + t;
                    if (__c != 0) {
                        if (__c >= 64) {
                            cint_w0 = cint_w1 >>> (__c - 64);
                            cint_w1 = 0;
                        } else {
                            //srl128_short(cint_w1, cint_w0, __c);
                            cint_w0 = (cint_w1 << (64 - __c)) + (cint_w0 >>> __c);
                            cint_w1 = cint_w1 >>> __c;
                        }
                    }
                }

                if (le128(cint_w1, cint_w0, pow5_w1, pow5_w0)) {
                    long /*BID_UINT128*/ cc_w1 = cint_w1, cc_w0 = cint_w0;
                    final int bid_power_five_index = a << 1;
                    pow5_w0 = bid_power_five_flat[bid_power_five_index];
                    pow5_w1 = bid_power_five_flat[bid_power_five_index + 1];

                    //__mul_128x128_low (cc, cc, pow5);
                    {
                        long /*BID_UINT128*/ ALBL_w0, ALBL_w1;
                        long /*BID_UINT64*/ QM64;

                        //__mul_64x64_to_128(ALBL, cc_w0, pow5_w0);
                        {
                            long __CX = cc_w0;
                            long __CY = pow5_w0;
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

                            ALBL_w1 = __PH + (__PM >>> 32);
                            ALBL_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
                        }


                        QM64 = pow5_w0 * cc_w1 + cc_w0 * pow5_w1;

                        cc_w0 = ALBL_w0;
                        cc_w1 = QM64 + ALBL_w1;
                    }

                    return return_bid64(s, 398 - a, cc_w0);
                }
            }
        }

        // Check for "trivial" underflow, when 2^e * 2^113 <= 10^emin * 1/4,
        // so test e <= floor(emin * log_2(10) - 115)
        // In this case just fix ourselves at that value for uniformity.
        //
        // This is important not only to keep the tables small but to maintain the
        // testing of the round/sticky words as a correct rounding method

        // Now look up our exponent e, and the breakpoint between e and e+1

        final int bid_breakpoints_bid64_index = (1437 + e) << 1;
        m_min_w0 = bid_breakpoints_bid64_flat[bid_breakpoints_bid64_index];
        m_min_w1 = bid_breakpoints_bid64_flat[bid_breakpoints_bid64_index + 1];
        e_out = bid_exponents_bid64[1437 + e];

        // Choose exponent and reciprocal multiplier based on breakpoint

        final long[] bid_multipliers_bid64_flat;
        if (le128(c_w1, c_w0, m_min_w1, m_min_w0)) {
            bid_multipliers_bid64_flat = bid_multipliers1_bid64_flat;
        } else {
            bid_multipliers_bid64_flat = bid_multipliers2_bid64_flat;
            e_out = e_out + 1;
        }
        final int bid_multipliers_bid64_index = (1437 + e) << 2;
        r_w0 = bid_multipliers_bid64_flat[bid_multipliers_bid64_index];
        r_w1 = bid_multipliers_bid64_flat[bid_multipliers_bid64_index + 1];
        r_w2 = bid_multipliers_bid64_flat[bid_multipliers_bid64_index + 2];
        r_w3 = bid_multipliers_bid64_flat[bid_multipliers_bid64_index + 3];

        // Do the reciprocal multiplication

        //__mul_128x256_to_384 (z, c, r)
        {
            long /*BID_UINT512*/ P0_w0, P0_w1, P0_w2, P0_w3, P0_w4, P0_w5, P0_w6, P0_w7,
                P1_w0, P1_w1, P1_w2, P1_w3, P1_w4, P1_w5, P1_w6, P1_w7;
            long /*BID_UINT64*/ CY;
            {
                long /*BID_UINT128*/ lP0_w0, lP0_w1, lP1_w0, lP1_w1, lP2_w0, lP2_w1, lP3_w0, lP3_w1;
                long /*BID_UINT64*/ lC;
                {
                    long /*BID_UINT64*/ CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
                    CXH = c_w0 >>> 32;
                    CXL = LONG_LOW_PART & c_w0;
                    CYH = r_w0 >>> 32;
                    CYL = LONG_LOW_PART & r_w0;
                    PM = CXH * CYL;
                    PH = CXH * CYH;
                    PL = CXL * CYL;
                    PM2 = CXL * CYH;
                    PH += PM >>> 32;
                    PM = (LONG_LOW_PART & PM) + PM2 + (PL >>> 32);
                    lP0_w1 = PH + (PM >>> 32);
                    lP0_w0 = (PM << 32) + (LONG_LOW_PART & PL);
                }
                {
                    long /*BID_UINT64*/ CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
                    CXH = c_w0 >>> 32;
                    CXL = LONG_LOW_PART & c_w0;
                    CYH = r_w1 >>> 32;
                    CYL = LONG_LOW_PART & r_w1;
                    PM = CXH * CYL;
                    PH = CXH * CYH;
                    PL = CXL * CYL;
                    PM2 = CXL * CYH;
                    PH += PM >>> 32;
                    PM = (LONG_LOW_PART & PM) + PM2 + (PL >>> 32);
                    lP1_w1 = PH + (PM >>> 32);
                    lP1_w0 = (PM << 32) + (LONG_LOW_PART & PL);
                }
                {
                    long /*BID_UINT64*/ CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
                    CXH = c_w0 >>> 32;
                    CXL = LONG_LOW_PART & c_w0;
                    CYH = r_w2 >>> 32;
                    CYL = LONG_LOW_PART & r_w2;
                    PM = CXH * CYL;
                    PH = CXH * CYH;
                    PL = CXL * CYL;
                    PM2 = CXL * CYH;
                    PH += PM >>> 32;
                    PM = (LONG_LOW_PART & PM) + PM2 + (PL >>> 32);
                    lP2_w1 = PH + (PM >>> 32);
                    lP2_w0 = (PM << 32) + (LONG_LOW_PART & PL);
                }
                {
                    long /*BID_UINT64*/ CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
                    CXH = c_w0 >>> 32;
                    CXL = LONG_LOW_PART & c_w0;
                    CYH = r_w3 >>> 32;
                    CYL = LONG_LOW_PART & r_w3;
                    PM = CXH * CYL;
                    PH = CXH * CYH;
                    PL = CXL * CYL;
                    PM2 = CXL * CYH;
                    PH += PM >>> 32;
                    PM = (LONG_LOW_PART & PM) + PM2 + (PL >>> 32);
                    lP3_w1 = PH + (PM >>> 32);
                    lP3_w0 = (PM << 32) + (LONG_LOW_PART & PL);
                }
                P0_w0 = lP0_w0;
                {
                    long /*BID_UINT64*/ X1 = lP1_w0;
                    P0_w1 = lP1_w0 + lP0_w1;
                    lC = UnsignedLong.isLess(P0_w1, X1) ? 1 : 0;
                }
                {
                    long /*BID_UINT64*/ X1 = lP2_w0 + lC;
                    P0_w2 = X1 + lP1_w1;
                    lC = (UnsignedLong.isLess(P0_w2, X1) || UnsignedLong.isLess(X1, lC)) ? 1 : 0;
                }
                {
                    long /*BID_UINT64*/ X1 = lP3_w0 + lC;
                    P0_w3 = X1 + lP2_w1;
                    lC = (UnsignedLong.isLess(P0_w3, X1) || UnsignedLong.isLess(X1, lC)) ? 1 : 0;
                }
                P0_w4 = lP3_w1 + lC;
            }
            {
                long /*BID_UINT128*/ lP0_w0, lP0_w1, lP1_w0, lP1_w1, lP2_w0, lP2_w1, lP3_w0, lP3_w1;
                long /*BID_UINT64*/ lC;
                {
                    long /*BID_UINT64*/ CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
                    CXH = c_w1 >>> 32;
                    CXL = LONG_LOW_PART & c_w1;
                    CYH = r_w0 >>> 32;
                    CYL = LONG_LOW_PART & r_w0;
                    PM = CXH * CYL;
                    PH = CXH * CYH;
                    PL = CXL * CYL;
                    PM2 = CXL * CYH;
                    PH += PM >>> 32;
                    PM = (LONG_LOW_PART & PM) + PM2 + (PL >>> 32);
                    lP0_w1 = PH + (PM >>> 32);
                    lP0_w0 = (PM << 32) + (LONG_LOW_PART & PL);
                }
                {
                    long /*BID_UINT64*/ CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
                    CXH = c_w1 >>> 32;
                    CXL = LONG_LOW_PART & c_w1;
                    CYH = r_w1 >>> 32;
                    CYL = LONG_LOW_PART & r_w1;
                    PM = CXH * CYL;
                    PH = CXH * CYH;
                    PL = CXL * CYL;
                    PM2 = CXL * CYH;
                    PH += PM >>> 32;
                    PM = (LONG_LOW_PART & PM) + PM2 + (PL >>> 32);
                    lP1_w1 = PH + (PM >>> 32);
                    lP1_w0 = (PM << 32) + (LONG_LOW_PART & PL);
                }
                {
                    long /*BID_UINT64*/ CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
                    CXH = c_w1 >>> 32;
                    CXL = LONG_LOW_PART & c_w1;
                    CYH = r_w2 >>> 32;
                    CYL = LONG_LOW_PART & r_w2;
                    PM = CXH * CYL;
                    PH = CXH * CYH;
                    PL = CXL * CYL;
                    PM2 = CXL * CYH;
                    PH += PM >>> 32;
                    PM = (LONG_LOW_PART & PM) + PM2 + (PL >>> 32);
                    lP2_w1 = PH + (PM >>> 32);
                    lP2_w0 = (PM << 32) + (LONG_LOW_PART & PL);
                }
                {
                    long /*BID_UINT64*/ CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
                    CXH = c_w1 >>> 32;
                    CXL = LONG_LOW_PART & c_w1;
                    CYH = r_w3 >>> 32;
                    CYL = LONG_LOW_PART & r_w3;
                    PM = CXH * CYL;
                    PH = CXH * CYH;
                    PL = CXL * CYL;
                    PM2 = CXL * CYH;
                    PH += PM >>> 32;
                    PM = (LONG_LOW_PART & PM) + PM2 + (PL >>> 32);
                    lP3_w1 = PH + (PM >>> 32);
                    lP3_w0 = (PM << 32) + (LONG_LOW_PART & PL);
                }
                P1_w0 = lP0_w0;
                {
                    long /*BID_UINT64*/ X1 = lP1_w0;
                    P1_w1 = lP1_w0 + lP0_w1;
                    lC = UnsignedLong.isLess(P1_w1, X1) ? 1 : 0;
                }
                {
                    long /*BID_UINT64*/ X1 = lP2_w0 + lC;
                    P1_w2 = X1 + lP1_w1;
                    lC = (UnsignedLong.isLess(P1_w2, X1) || UnsignedLong.isLess(X1, lC)) ? 1 : 0;
                }
                {
                    long /*BID_UINT64*/ X1 = lP3_w0 + lC;
                    P1_w3 = X1 + lP2_w1;
                    lC = (UnsignedLong.isLess(P1_w3, X1) || UnsignedLong.isLess(X1, lC)) ? 1 : 0;
                }
                P1_w4 = lP3_w1 + lC;
            }
            z_w0 = P0_w0;
            {
                long /*BID_UINT64*/ X1 = P1_w0;
                z_w1 = P1_w0 + P0_w1;
                CY = UnsignedLong.isLess(z_w1, X1) ? 1 : 0;
            }
            {
                long /*BID_UINT64*/ X1 = P1_w1 + CY;
                z_w2 = X1 + P0_w2;
                CY = (UnsignedLong.isLess(z_w2, X1) || UnsignedLong.isLess(X1, CY)) ? 1 : 0;
            }
            {
                long /*BID_UINT64*/ X1 = P1_w2 + CY;
                z_w3 = X1 + P0_w3;
                CY = (UnsignedLong.isLess(z_w3, X1) || UnsignedLong.isLess(X1, CY)) ? 1 : 0;
            }
            {
                long /*BID_UINT64*/ X1 = P1_w3 + CY;
                z_w4 = X1 + P0_w4;
                CY = (UnsignedLong.isLess(z_w4, X1) || UnsignedLong.isLess(X1, CY)) ? 1 : 0;
            }
            z_w5 = P1_w4 + CY;
        }

        c_prov = z_w5;

        // Round using round-sticky words
        // If we spill over into the next decade, correct

        final int bid_roundbound_128_index = ((rnd_mode << 2) + ((s & 1) << 1) + (int) (c_prov & 1)) << 1;
        if (lt128
            (bid_roundbound_128_flat[bid_roundbound_128_index + 1],
                bid_roundbound_128_flat[bid_roundbound_128_index], z_w4, z_w3)) {
            c_prov = c_prov + 1;
            if (c_prov == 10000000000000000L) {
                c_prov = 1000000000000000L;
                e_out = e_out + 1;
            }
        }
        // Check for overflow

        // Set the inexact flag as appropriate and check underflow
        // It's no doubt superfluous to check inexactness, but anyway...

//        if ((z_w4 != 0) || (z_w3 != 0)) {
//            __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);
//        }
        // Package up the result

        return return_bid64(s, e_out, c_prov);
    }


//	return NativeImpl.toFloat64(value);     //OPNR(toFloat64, double, bid64_to_binary64(x), BID_UINT64 x)
//
//// **********************************************************************
//
//#if DECIMAL_CALL_BY_REFERENCE
//void
//bid64_to_binary64 (double *pres, BID_UINT64 * px
//                   _RND_MODE_PARAM _EXC_FLAGS_PARAM _EXC_MASKS_PARAM
//                   _EXC_INFO_PARAM) {
//  BID_UINT64 x = *px;
//#if !DECIMAL_GLOBAL_ROUNDING
//  _IDEC_round rnd_mode = *prnd_mode;
//#endif
//#else
//RES_WRAPFN_DFP(double, bid64_to_binary64, 64)
//double
//bid64_to_binary64 (BID_UINT64 x
//                   _RND_MODE_PARAM _EXC_FLAGS_PARAM _EXC_MASKS_PARAM
//                   _EXC_INFO_PARAM) {
//#endif
//
//  BID_UINT64 c_prov;
//  BID_UINT128 c;
//  BID_UINT128 m_min;
//  int s, e, k, e_out;
//  BID_UINT256 r;
//  BID_UINT384 z;
//
//  unpack_bid64 (x, s, e, k, (c_w1), return_binary64_zero (s),
//                return_binary64_inf (s), return_binary64_nan);
//
//// Correct to 2^112 <= c < 2^113 with corresponding exponent adding 113-54=59
//// In fact shift a further 6 places ready for reciprocal multiplication
//// Thus (113-54)+6=65, a shift of 1 given that we've already upacked in c.w[1]
//
//  c_w1 = c_w1 << 1;
//  c_w0 = 0;
//  k = k + 59;
//
//// Check for "trivial" overflow, when 10^e * 1 > 2^{sci_emax+1}, just to
//// keep tables smaller (it would be intercepted later otherwise).
////
//// (Note that we may have normalized the coefficient, but we have a
////  corresponding exponent postcorrection to account for; this can
////  afford to be conservative anyway.)
////
//// We actually check if e >= ceil((sci_emax + 1) * log_10(2))
//// which in this case is 2 >= ceil(1024 * log_10(2)) = ceil(308.25) = 309
//
//  if (e >= 309) {
//    __set_status_flags(pfpsf, BID_OVERFLOW_INEXACT_EXCEPTION);
//    return_binary64_ovf (s);
//  }
//// Also check for "trivial" underflow, when 10^e * 2^113 <= 2^emin * 1/4,
//// so test e <= floor((emin - 115) * log_10(2))
//// In this case just fix ourselves at that value for uniformity.
////
//// This is important not only to keep the tables small but to maintain the
//// testing of the round/sticky words as a correct rounding method
//
//  if (e <= -358)
//    e = -358;
//
//// Look up the breakpoint and approximate exponent
//
//  m_min = (bid_breakpoints_binary64 + 358)[e];
//  e_out = (bid_exponents_binary64 + 358)[e] - k;
//
//// Choose provisional exponent and reciprocal multiplier based on breakpoint
//
//  if (le128 (c_w1, c_w0, m_min_w1, m_min_w0)) {
//    r = (bid_multipliers1_binary64 + 358)[e];
//  } else {
//    r = (bid_multipliers2_binary64 + 358)[e];
//    e_out = e_out + 1;
//  }
//
//// Do the reciprocal multiplication
//
//    __mul_64x256_to_320(z, c_w1, r);
//    z_w5=z_w4; z_w4=z_w3; z_w3=z_w2; z_w2=z_w1; z_w1=z_w0; z_w0=0;
//
//// Check for exponent underflow and compensate by shifting the product
//// Cut off the process at precision+2, since we can't really shift further
//    if (e_out < 1) {
//    int d;
//    d = 1 - e_out;
//    if (d > 55)
//      d = 55;
//    e_out = 1;
//    srl256_short (z_w5, z_w4, z_w3, z_w2, d);
//  }
//  c_prov = z_w5;
//
//// Round using round-sticky words
//// If we spill into the next binade, correct
//// Flag underflow where it may be needed even for |result| = SNN
//
//  if (lt128
//      (bid_roundbound_128[(rnd_mode << 2) + ((s & 1) << 1) + (c_prov & 1)].
//       w[1],
//       bid_roundbound_128[(rnd_mode << 2) + ((s & 1) << 1) +
//                      (c_prov & 1)].w[0], z_w4, z_w3)) {
//    c_prov = c_prov + 1;
//    if (c_prov == (1ull << 53)) {
//      c_prov = 1ull << 52;
//      e_out = e_out + 1;
//    }
//#if BINARY_TINY_DETECTION_AFTER_ROUNDING
//    else if ((c_prov == (1ull << 52)) && (e_out == 1))
//    {
//      if (rnd_mode + (s & 1) == 2)
//        __set_status_flags(pfpsf,BID_UNDERFLOW_EXCEPTION);
//    }
//#endif
//  }
//// Check for overflow
//
//  if (e_out >= 2047) {
//    __set_status_flags(pfpsf, BID_OVERFLOW_INEXACT_EXCEPTION);
//    return_binary64_ovf (s);
//  }
//// Modify exponent for a tiny result, otherwise lop the implicit bit
//
//  if (UnsignedLong.isLess(c_prov , (1ull << 52)))
//    e_out = 0;
//  else
//    c_prov = c_prov & ((1ull << 52) - 1);
//
//// Set the inexact and underflow flag as appropriate
//
//  if ((z_w4 != 0) || (z_w3 != 0)) {
//    __set_status_flags(pfpsf,BID_INEXACT_EXCEPTION);
//    if (e_out == 0)
//      __set_status_flags(pfpsf,BID_UNDERFLOW_EXCEPTION);
//  }
//// Package up the result as a binary floating-point number
//
//  return_binary64 (s, e_out, c_prov);
//}

    static long return_bid64_zero(final int s) {
        return return_bid64(s, 398, 0);
    }

    static long return_bid64_inf(final int s) {
        return return_bid64(s, (0xF << 6), 0);
    }

    static long return_bid64_nan(final int s, final long c_hi, final long c_lo) {
        return return_bid64(s, (0x1F << 5), (UnsignedLong.isGreater(c_hi >>> 14, 999999999999999L) ? 0 : (c_hi >>> 14)));
    }

    static long return_bid64(final int s, final int e, final long c) {
        if (UnsignedLong.isLess(c, 1L << 53))
            return (((long) (s) << 63) + ((long) (e) << 53) + (c));
        else
            return (((long) (s) << 63) + ((0x3L << 61) - (1L << 53)) + ((long) (e) << 51) + (c));
    }

    static boolean lt128(final long /*BID_UINT64*/ x_hi, final long /*BID_UINT64*/ x_lo,
                         final long /*BID_UINT64*/ y_hi, final long /*BID_UINT64*/ y_lo) {
        return UnsignedLong.isLess(x_hi, y_hi) || ((x_hi == y_hi) && UnsignedLong.isLess(x_lo, y_lo));
    }

    static boolean le128(final long /*BID_UINT64*/ x_hi, final long /*BID_UINT64*/x_lo,
                         final long /*BID_UINT64*/y_hi, final long /*BID_UINT64*/y_lo) {
        return UnsignedLong.isLess(x_hi, y_hi) || ((x_hi == y_hi) && UnsignedLong.isLessOrEqual(x_lo, y_lo));
    }

// Counting leading zeros in an unsigned 64-bit word
// The "_nz" version will return the wrong answer (63) for zero inputs

    static final long CLZ64_MASK32 = 0xFFFFFFFF00000000L;
    static final long CLZ64_MASK16 = 0xFFFF0000FFFF0000L;
    static final long CLZ64_MASK8 = 0xFF00FF00FF00FF00L;
    static final long CLZ64_MASK4 = 0xF0F0F0F0F0F0F0F0L;
    static final long CLZ64_MASK2 = 0xCCCCCCCCCCCCCCCCL;
    static final long CLZ64_MASK1 = 0xAAAAAAAAAAAAAAAAL;

    static int clz64_nz(final long /*BID_UINT64*/ n) {
        return (UnsignedLong.isLessOrEqual(n & CLZ64_MASK32, n & ~CLZ64_MASK32) ? 32 : 0) +
            (UnsignedLong.isLessOrEqual(n & CLZ64_MASK16, n & ~CLZ64_MASK16) ? 16 : 0) +
            (UnsignedLong.isLessOrEqual(n & CLZ64_MASK8, n & ~CLZ64_MASK8) ? 8 : 0) +
            (UnsignedLong.isLessOrEqual(n & CLZ64_MASK4, n & ~CLZ64_MASK4) ? 4 : 0) +
            (UnsignedLong.isLessOrEqual(n & CLZ64_MASK2, n & ~CLZ64_MASK2) ? 2 : 0) +
            (UnsignedLong.isLessOrEqual(n & CLZ64_MASK1, n & ~CLZ64_MASK1) ? 1 : 0);
    }

    static int clz64(final long /*BID_UINT64*/ n) {
        return n == 0 ? 64 : clz64_nz(n);
    }

// Counting trailing zeros in an unsigned 64-bit word
// The ctz64_1bit version is for a single bit

    static int ctz64_1bit(final long /*BID_UINT64*/ n) {
        return ((n & ~CLZ64_MASK32) != 0 ? 0 : 32) +
            ((n & ~CLZ64_MASK16) != 0 ? 0 : 16) +
            ((n & ~CLZ64_MASK8) != 0 ? 0 : 8) +
            ((n & ~CLZ64_MASK4) != 0 ? 0 : 4) +
            ((n & ~CLZ64_MASK2) != 0 ? 0 : 2) +
            ((n & ~CLZ64_MASK1) != 0 ? 0 : 1);
    }

    static int ctz64(final long /*BID_UINT64*/n) {
        return (n == 0 ? 64 : ctz64_1bit(n & -n));
    }

    static final long[] /*BID_UINT128*/ bid_coefflimits_bid64_flat = {
        0x002386F26FC10000L, 0x0000000000000000L,
        0x00071AFD498D0000L, 0x0000000000000000L,
        0x00016BCC41E90000L, 0x0000000000000000L,
        0x000048C273950000L, 0x0000000000000000L,
        0x00000E8D4A510000L, 0x0000000000000000L,
        0x000002E90EDD0000L, 0x0000000000000000L,
        0x0000009502F90000L, 0x0000000000000000L,
        0x0000001DCD650000L, 0x0000000000000000L,
        0x00000005F5E10000L, 0x0000000000000000L,
        0x00000001312D0000L, 0x0000000000000000L,
        0x000000003D090000L, 0x0000000000000000L,
        0x000000000C350000L, 0x0000000000000000L,
        0x0000000002710000L, 0x0000000000000000L,
        0x00000000007D0000L, 0x0000000000000000L,
        0x0000000000190000L, 0x0000000000000000L,
        0x0000000000050000L, 0x0000000000000000L,
        0x0000000000010000L, 0x0000000000000000L,
        0x0000000000003333L, 0x0000000000000000L,
        0x0000000000000A3DL, 0x0000000000000000L,
        0x000000000000020CL, 0x0000000000000000L,
        0x0000000000000068L, 0x0000000000000000L,
        0x0000000000000014L, 0x0000000000000000L,
        0x0000000000000004L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
    };

    static final long[] /*BID_UINT128*/ bid_power_five_flat = {
        0x0000000000000001L, 0x0000000000000000L,
        0x0000000000000005L, 0x0000000000000000L,
        0x0000000000000019L, 0x0000000000000000L,
        0x000000000000007DL, 0x0000000000000000L,
        0x0000000000000271L, 0x0000000000000000L,
        0x0000000000000C35L, 0x0000000000000000L,
        0x0000000000003D09L, 0x0000000000000000L,
        0x000000000001312DL, 0x0000000000000000L,
        0x000000000005F5E1L, 0x0000000000000000L,
        0x00000000001DCD65L, 0x0000000000000000L,
        0x00000000009502F9L, 0x0000000000000000L,
        0x0000000002E90EDDL, 0x0000000000000000L,
        0x000000000E8D4A51L, 0x0000000000000000L,
        0x0000000048C27395L, 0x0000000000000000L,
        0x000000016BCC41E9L, 0x0000000000000000L,
        0x000000071AFD498DL, 0x0000000000000000L,
        0x0000002386F26FC1L, 0x0000000000000000L,
        0x000000B1A2BC2EC5L, 0x0000000000000000L,
        0x000003782DACE9D9L, 0x0000000000000000L,
        0x00001158E460913DL, 0x0000000000000000L,
        0x000056BC75E2D631L, 0x0000000000000000L,
        0x0001B1AE4D6E2EF5L, 0x0000000000000000L,
        0x000878678326EAC9L, 0x0000000000000000L,
        0x002A5A058FC295EDL, 0x0000000000000000L,
        0x00D3C21BCECCEDA1L, 0x0000000000000000L,
        0x0422CA8B0A00A425L, 0x0000000000000000L,
        0x14ADF4B7320334B9L, 0x0000000000000000L,
        0x6765C793FA10079DL, 0x0000000000000000L,
        0x04FCE5E3E2502611L, 0x0000000000000002L,
        0x18F07D736B90BE55L, 0x000000000000000AL,
        0x7CB2734119D3B7A9L, 0x0000000000000032L,
        0x6F7C40458122964DL, 0x00000000000000FCL,
        0x2D6D415B85ACEF81L, 0x00000000000004EEL,
        0xE32246C99C60AD85L, 0x00000000000018A6L,
        0x6FAB61F00DE36399L, 0x0000000000007B42L,
        0x2E58E9B04570F1FDL, 0x000000000002684CL,
        0xE7BC90715B34B9F1L, 0x00000000000C097CL,
        0x86AED236C807A1B5L, 0x00000000003C2F70L,
        0xA16A1B11E8262889L, 0x00000000012CED32L,
        0x2712875988BECAADL, 0x0000000005E0A1FDL,
        0xC35CA4BFABB9F561L, 0x000000001D6329F1L,
        0xD0CF37BE5AA1CAE5L, 0x0000000092EFD1B8L,
        0x140C16B7C528F679L, 0x00000002DEAF189CL,
        0x643C7196D9CCD05DL, 0x0000000E596B7B0CL,
        0xF52E37F2410011D1L, 0x00000047BF19673DL,
        0xC9E717BB45005915L, 0x00000166BB7F0435L,
        0xF18376A85901BD69L, 0x00000701A97B150CL,
        0xB7915149BD08B30DL, 0x000023084F676940L,
        0x95D69670B12B7F41L, 0x0000AF298D050E43L,
    };

    static final long[] /*BID_UINT128*/ bid_roundbound_128_flat = {
        0x0000000000000000L, 0x8000000000000000L,
        0xFFFFFFFFFFFFFFFFL, 0x7FFFFFFFFFFFFFFFL,
        0x0000000000000000L, 0x8000000000000000L,
        0xFFFFFFFFFFFFFFFFL, 0x7FFFFFFFFFFFFFFFL,
        0xFFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFFL,
        0xFFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFFL,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000000L, 0x0000000000000000L,
        0xFFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFFL,
        0xFFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFFL,
        0xFFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFFL,
        0xFFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFFL,
        0xFFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFFL,
        0xFFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFFL,
        0xFFFFFFFFFFFFFFFFL, 0x7FFFFFFFFFFFFFFFL,
        0xFFFFFFFFFFFFFFFFL, 0x7FFFFFFFFFFFFFFFL,
        0xFFFFFFFFFFFFFFFFL, 0x7FFFFFFFFFFFFFFFL,
        0xFFFFFFFFFFFFFFFFL, 0x7FFFFFFFFFFFFFFFL,
    };

    static final int[] bid_exponents_bid64 = {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 1,
        1, 1, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6, 6, 7,
        7, 7, 8, 8, 8, 9, 9, 9, 9, 10, 10, 10, 11, 11, 11, 12, 12, 12, 12, 13,
        13, 13, 14, 14, 14, 15, 15, 15, 15, 16, 16, 16, 17, 17, 17, 18, 18, 18, 18, 19,
        19, 19, 20, 20, 20, 21, 21, 21, 21, 22, 22, 22, 23, 23, 23, 24, 24, 24, 24, 25,
        25, 25, 26, 26, 26, 27, 27, 27, 27, 28, 28, 28, 29, 29, 29, 30, 30, 30, 30, 31,
        31, 31, 32, 32, 32, 33, 33, 33, 34, 34, 34, 34, 35, 35, 35, 36, 36, 36, 37, 37,
        37, 37, 38, 38, 38, 39, 39, 39, 40, 40, 40, 40, 41, 41, 41, 42, 42, 42, 43, 43,
        43, 43, 44, 44, 44, 45, 45, 45, 46, 46, 46, 46, 47, 47, 47, 48, 48, 48, 49, 49,
        49, 49, 50, 50, 50, 51, 51, 51, 52, 52, 52, 52, 53, 53, 53, 54, 54, 54, 55, 55,
        55, 55, 56, 56, 56, 57, 57, 57, 58, 58, 58, 58, 59, 59, 59, 60, 60, 60, 61, 61,
        61, 62, 62, 62, 62, 63, 63, 63, 64, 64, 64, 65, 65, 65, 65, 66, 66, 66, 67, 67,
        67, 68, 68, 68, 68, 69, 69, 69, 70, 70, 70, 71, 71, 71, 71, 72, 72, 72, 73, 73,
        73, 74, 74, 74, 74, 75, 75, 75, 76, 76, 76, 77, 77, 77, 77, 78, 78, 78, 79, 79,
        79, 80, 80, 80, 80, 81, 81, 81, 82, 82, 82, 83, 83, 83, 83, 84, 84, 84, 85, 85,
        85, 86, 86, 86, 86, 87, 87, 87, 88, 88, 88, 89, 89, 89, 90, 90, 90, 90, 91, 91,
        91, 92, 92, 92, 93, 93, 93, 93, 94, 94, 94, 95, 95, 95, 96, 96, 96, 96, 97, 97,
        97, 98, 98, 98, 99, 99, 99, 99, 100, 100, 100, 101, 101, 101, 102, 102, 102, 102, 103, 103,
        103, 104, 104, 104, 105, 105, 105, 105, 106, 106, 106, 107, 107, 107, 108, 108, 108, 108, 109, 109,
        109, 110, 110, 110, 111, 111, 111, 111, 112, 112, 112, 113, 113, 113, 114, 114, 114, 114, 115, 115,
        115, 116, 116, 116, 117, 117, 117, 117, 118, 118, 118, 119, 119, 119, 120, 120, 120, 121, 121, 121,
        121, 122, 122, 122, 123, 123, 123, 124, 124, 124, 124, 125, 125, 125, 126, 126, 126, 127, 127, 127,
        127, 128, 128, 128, 129, 129, 129, 130, 130, 130, 130, 131, 131, 131, 132, 132, 132, 133, 133, 133,
        133, 134, 134, 134, 135, 135, 135, 136, 136, 136, 136, 137, 137, 137, 138, 138, 138, 139, 139, 139,
        139, 140, 140, 140, 141, 141, 141, 142, 142, 142, 142, 143, 143, 143, 144, 144, 144, 145, 145, 145,
        145, 146, 146, 146, 147, 147, 147, 148, 148, 148, 149, 149, 149, 149, 150, 150, 150, 151, 151, 151,
        152, 152, 152, 152, 153, 153, 153, 154, 154, 154, 155, 155, 155, 155, 156, 156, 156, 157, 157, 157,
        158, 158, 158, 158, 159, 159, 159, 160, 160, 160, 161, 161, 161, 161, 162, 162, 162, 163, 163, 163,
        164, 164, 164, 164, 165, 165, 165, 166, 166, 166, 167, 167, 167, 167, 168, 168, 168, 169, 169, 169,
        170, 170, 170, 170, 171, 171, 171, 172, 172, 172, 173, 173, 173, 173, 174, 174, 174, 175, 175, 175,
        176, 176, 176, 176, 177, 177, 177, 178, 178, 178, 179, 179, 179, 180, 180, 180, 180, 181, 181, 181,
        182, 182, 182, 183, 183, 183, 183, 184, 184, 184, 185, 185, 185, 186, 186, 186, 186, 187, 187, 187,
        188, 188, 188, 189, 189, 189, 189, 190, 190, 190, 191, 191, 191, 192, 192, 192, 192, 193, 193, 193,
        194, 194, 194, 195, 195, 195, 195, 196, 196, 196, 197, 197, 197, 198, 198, 198, 198, 199, 199, 199,
        200, 200, 200, 201, 201, 201, 201, 202, 202, 202, 203, 203, 203, 204, 204, 204, 204, 205, 205, 205,
        206, 206, 206, 207, 207, 207, 208, 208, 208, 208, 209, 209, 209, 210, 210, 210, 211, 211, 211, 211,
        212, 212, 212, 213, 213, 213, 214, 214, 214, 214, 215, 215, 215, 216, 216, 216, 217, 217, 217, 217,
        218, 218, 218, 219, 219, 219, 220, 220, 220, 220, 221, 221, 221, 222, 222, 222, 223, 223, 223, 223,
        224, 224, 224, 225, 225, 225, 226, 226, 226, 226, 227, 227, 227, 228, 228, 228, 229, 229, 229, 229,
        230, 230, 230, 231, 231, 231, 232, 232, 232, 232, 233, 233, 233, 234, 234, 234, 235, 235, 235, 236,
        236, 236, 236, 237, 237, 237, 238, 238, 238, 239, 239, 239, 239, 240, 240, 240, 241, 241, 241, 242,
        242, 242, 242, 243, 243, 243, 244, 244, 244, 245, 245, 245, 245, 246, 246, 246, 247, 247, 247, 248,
        248, 248, 248, 249, 249, 249, 250, 250, 250, 251, 251, 251, 251, 252, 252, 252, 253, 253, 253, 254,
        254, 254, 254, 255, 255, 255, 256, 256, 256, 257, 257, 257, 257, 258, 258, 258, 259, 259, 259, 260,
        260, 260, 260, 261, 261, 261, 262, 262, 262, 263, 263, 263, 263, 264, 264, 264, 265, 265, 265, 266,
        266, 266, 267, 267, 267, 267, 268, 268, 268, 269, 269, 269, 270, 270, 270, 270, 271, 271, 271, 272,
        272, 272, 273, 273, 273, 273, 274, 274, 274, 275, 275, 275, 276, 276, 276, 276, 277, 277, 277, 278,
        278, 278, 279, 279, 279, 279, 280, 280, 280, 281, 281, 281, 282, 282, 282, 282, 283, 283, 283, 284,
        284, 284, 285, 285, 285, 285, 286, 286, 286, 287, 287, 287, 288, 288, 288, 288, 289, 289, 289, 290,
        290, 290, 291, 291, 291, 291, 292, 292, 292, 293, 293, 293, 294, 294, 294, 295, 295, 295, 295, 296,
        296, 296, 297, 297, 297, 298, 298, 298, 298, 299, 299, 299, 300, 300, 300, 301, 301, 301, 301, 302,
        302, 302, 303, 303, 303, 304, 304, 304, 304, 305, 305, 305, 306, 306, 306, 307, 307, 307, 307, 308,
        308, 308, 309, 309, 309, 310, 310, 310, 310, 311, 311, 311, 312, 312, 312, 313, 313, 313, 313, 314,
        314, 314, 315, 315, 315, 316, 316, 316, 316, 317, 317, 317, 318, 318, 318, 319, 319, 319, 319, 320,
        320, 320, 321, 321, 321, 322, 322, 322, 322, 323, 323, 323, 324, 324, 324, 325, 325, 325, 326, 326,
        326, 326, 327, 327, 327, 328, 328, 328, 329, 329, 329, 329, 330, 330, 330, 331, 331, 331, 332, 332,
        332, 332, 333, 333, 333, 334, 334, 334, 335, 335, 335, 335, 336, 336, 336, 337, 337, 337, 338, 338,
        338, 338, 339, 339, 339, 340, 340, 340, 341, 341, 341, 341, 342, 342, 342, 343, 343, 343, 344, 344,
        344, 344, 345, 345, 345, 346, 346, 346, 347, 347, 347, 347, 348, 348, 348, 349, 349, 349, 350, 350,
        350, 350, 351, 351, 351, 352, 352, 352, 353, 353, 353, 354, 354, 354, 354, 355, 355, 355, 356, 356,
        356, 357, 357, 357, 357, 358, 358, 358, 359, 359, 359, 360, 360, 360, 360, 361, 361, 361, 362, 362,
        362, 363, 363, 363, 363, 364, 364, 364, 365, 365, 365, 366, 366, 366, 366, 367, 367, 367, 368, 368,
        368, 369, 369, 369, 369, 370, 370, 370, 371, 371, 371, 372, 372, 372, 372, 373, 373, 373, 374, 374,
        374, 375, 375, 375, 375, 376, 376, 376, 377, 377, 377, 378, 378, 378, 378, 379, 379, 379, 380, 380,
        380, 381, 381, 381, 381, 382, 382, 382, 383, 383, 383, 384, 384, 384, 385, 385, 385, 385, 386, 386,
        386, 387, 387, 387, 388, 388, 388, 388, 389, 389, 389, 390, 390, 390, 391, 391, 391, 391, 392, 392,
        392, 393, 393, 393, 394, 394, 394, 394, 395, 395, 395, 396, 396, 396, 397, 397, 397, 397, 398, 398,
        398, 399, 399, 399, 400, 400, 400, 400, 401, 401, 401, 402, 402, 402, 403, 403, 403, 403, 404, 404,
        404, 405, 405, 405, 406, 406, 406, 406, 407, 407, 407, 408, 408, 408, 409, 409, 409, 409, 410, 410,
        410, 411, 411, 411, 412, 412, 412, 413, 413, 413, 413, 414, 414, 414, 415, 415, 415, 416, 416, 416,
        416, 417, 417, 417, 418, 418, 418, 419, 419, 419, 419, 420, 420, 420, 421, 421, 421, 422, 422, 422,
        422, 423, 423, 423, 424, 424, 424, 425, 425, 425, 425, 426, 426, 426, 427, 427, 427, 428, 428, 428,
        428, 429, 429, 429, 430, 430, 430, 431, 431, 431, 431, 432, 432, 432, 433, 433, 433, 434, 434, 434,
        434, 435, 435, 435, 436, 436, 436, 437, 437, 437, 437, 438, 438, 438, 439, 439, 439, 440, 440, 440,
        441, 441, 441, 441, 442, 442, 442, 443, 443, 443, 444, 444, 444, 444, 445, 445, 445, 446, 446, 446,
        447, 447, 447, 447, 448, 448, 448, 449, 449, 449, 450, 450, 450, 450, 451, 451, 451, 452, 452, 452,
        453, 453, 453, 453, 454, 454, 454, 455, 455, 455, 456, 456, 456, 456, 457, 457, 457, 458, 458, 458,
        459, 459, 459, 459, 460, 460, 460, 461, 461, 461, 462, 462, 462, 462, 463, 463, 463, 464, 464, 464,
        465, 465, 465, 465, 466, 466, 466, 467, 467, 467, 468, 468, 468, 468, 469, 469, 469, 470, 470, 470,
        471, 471, 471, 472, 472, 472, 472, 473, 473, 473, 474, 474, 474, 475, 475, 475, 475, 476, 476, 476,
        477, 477, 477, 478, 478, 478, 478, 479, 479, 479, 480, 480, 480, 481, 481, 481, 481, 482, 482, 482,
        483, 483, 483, 484, 484, 484, 484, 485, 485, 485, 486, 486, 486, 487, 487, 487, 487, 488, 488, 488,
        489, 489, 489, 490, 490, 490, 490, 491, 491, 491, 492, 492, 492, 493, 493, 493, 493, 494, 494, 494,
        495, 495, 495, 496, 496, 496, 496, 497, 497, 497, 498, 498, 498, 499, 499, 499, 500, 500, 500, 500,
        501, 501, 501, 502, 502, 502, 503, 503, 503, 503, 504, 504, 504, 505, 505, 505, 506, 506, 506, 506,
        507, 507, 507, 508, 508, 508, 509, 509, 509, 509, 510, 510, 510, 511, 511, 511, 512, 512, 512, 512,
        513, 513, 513, 514, 514, 514, 515, 515, 515, 515, 516, 516, 516, 517, 517, 517, 518, 518, 518, 518,
        519, 519, 519, 520, 520, 520, 521, 521, 521, 521, 522, 522, 522, 523, 523, 523, 524, 524, 524, 524,
        525, 525, 525, 526, 526, 526, 527, 527, 527, 527, 528, 528, 528, 529, 529, 529, 530, 530, 530, 531,
        531, 531, 531, 532, 532, 532, 533, 533, 533, 534, 534, 534, 534, 535, 535, 535, 536, 536, 536, 537,
        537, 537, 537, 538, 538, 538, 539, 539, 539, 540, 540, 540, 540, 541, 541, 541, 542, 542, 542, 543,
        543, 543, 543, 544, 544, 544, 545, 545, 545, 546, 546, 546, 546, 547, 547, 547, 548, 548, 548, 549,
        549, 549, 549, 550, 550, 550, 551, 551, 551, 552, 552, 552, 552, 553, 553, 553, 554, 554, 554, 555,
        555, 555, 555, 556, 556, 556, 557, 557, 557, 558, 558, 558, 559, 559, 559, 559, 560, 560, 560, 561,
        561, 561, 562, 562, 562, 562, 563, 563, 563, 564, 564, 564, 565, 565, 565, 565, 566, 566, 566, 567,
        567, 567, 568, 568, 568, 568, 569, 569, 569, 570, 570, 570, 571, 571, 571, 571, 572, 572, 572, 573,
        573, 573, 574, 574, 574, 574, 575, 575, 575, 576, 576, 576, 577, 577, 577, 577, 578, 578, 578, 579,
        579, 579, 580, 580, 580, 580, 581, 581, 581, 582, 582, 582, 583, 583, 583, 583, 584, 584, 584, 585,
        585, 585, 586, 586, 586, 587, 587, 587, 587, 588, 588, 588, 589, 589, 589, 590, 590, 590, 590, 591,
        591, 591, 592, 592, 592, 593, 593, 593, 593, 594, 594, 594, 595, 595, 595, 596, 596, 596, 596, 597,
        597, 597, 598, 598, 598, 599, 599, 599, 599, 600, 600, 600, 601, 601, 601, 602, 602, 602, 602, 603,
        603, 603, 604, 604, 604, 605, 605, 605, 605, 606, 606, 606, 607, 607, 607, 608, 608, 608, 608, 609,
        609, 609, 610, 610, 610, 611, 611, 611, 611, 612, 612, 612, 613, 613, 613, 614, 614, 614, 614, 615,
        615, 615, 616, 616, 616, 617, 617, 617, 618, 618, 618, 618, 619, 619, 619, 620, 620, 620, 621, 621,
        621, 621, 622, 622, 622, 623, 623, 623, 624, 624, 624, 624, 625, 625, 625, 626, 626, 626, 627, 627,
        627, 627, 628, 628, 628, 629, 629, 629, 630, 630, 630, 630, 631, 631, 631, 632, 632, 632, 633, 633,
        633, 633, 634, 634, 634, 635, 635, 635, 636, 636, 636, 636, 637, 637, 637, 638, 638, 638, 639, 639,
        639, 639, 640, 640, 640, 641, 641, 641, 642, 642, 642, 642, 643, 643, 643, 644, 644, 644, 645, 645,
        645, 646, 646, 646, 646, 647, 647, 647, 648, 648, 648, 649, 649, 649, 649, 650, 650, 650, 651, 651,
        651, 652, 652, 652, 652, 653, 653, 653, 654, 654, 654, 655, 655, 655, 655, 656, 656, 656, 657, 657,
        657, 658, 658, 658, 658, 659, 659, 659, 660, 660, 660, 661, 661, 661, 661, 662, 662, 662, 663, 663,
        663, 664, 664, 664, 664, 665, 665, 665, 666, 666, 666, 667, 667, 667, 667, 668, 668, 668, 669, 669,
        669, 670, 670, 670, 670, 671, 671, 671, 672, 672, 672, 673, 673, 673, 673, 674, 674, 674, 675, 675,
        675, 676, 676, 676, 677, 677, 677, 677, 678, 678, 678, 679, 679, 679, 680, 680, 680, 680, 681, 681,
        681, 682, 682, 682, 683, 683, 683, 683, 684, 684, 684, 685, 685, 685, 686, 686, 686, 686, 687, 687,
        687, 688, 688, 688, 689, 689, 689, 689, 690, 690, 690, 691, 691, 691, 692, 692, 692, 692, 693, 693,
        693, 694, 694, 694, 695, 695, 695, 695, 696, 696, 696, 697, 697, 697, 698, 698, 698, 698, 699, 699,
        699, 700, 700, 700, 701, 701, 701, 701, 702, 702, 702, 703, 703, 703, 704, 704, 704, 705, 705, 705,
        705, 706, 706, 706, 707, 707, 707, 708, 708, 708, 708, 709, 709, 709, 710, 710, 710, 711, 711, 711,
        711, 712, 712, 712, 713, 713, 713, 714, 714, 714, 714, 715, 715, 715, 716, 716, 716, 717, 717, 717,
        717, 718, 718, 718, 719, 719, 719, 720, 720, 720, 720, 721, 721, 721, 722, 722, 722, 723, 723, 723,
        723, 724, 724, 724, 725, 725, 725, 726, 726, 726, 726, 727, 727, 727, 728, 728, 728, 729, 729, 729,
        729, 730, 730, 730, 731, 731, 731, 732, 732, 732, 733, 733, 733, 733, 734, 734, 734, 735, 735, 735,
        736, 736, 736, 736, 737, 737, 737, 738, 738, 738, 739, 739, 739, 739, 740, 740, 740, 741, 741, 741,
        742, 742, 742, 742, 743, 743, 743, 744, 744, 744, 745, 745, 745, 745, 746, 746, 746, 747, 747, 747,
        748, 748, 748, 748, 749, 749, 749, 750, 750, 750, 751, 751, 751, 751, 752, 752, 752, 753, 753, 753,
        754, 754, 754, 754, 755, 755, 755, 756, 756, 756, 757, 757, 757, 757, 758, 758, 758, 759, 759, 759,
        760, 760, 760, 760, 761, 761, 761, 762, 762, 762, 763, 763, 763, 764, 764, 764, 764, 765, 765, 765,
        766, 766, 766, 767, 767
    };

    static {
        final String root = JavaImplCastBinary64.class.getPackage().getName().replace('.', '/') + '/';
        bid_breakpoints_bid64_flat = loadResource(root + "bid_breakpoints_bid64.bin", bid_exponents_bid64.length * 2);
        bid_multipliers1_bid64_flat = loadResource(root + "bid_multipliers1_bid64.bin", bid_exponents_bid64.length * 4);
        bid_multipliers2_bid64_flat = loadResource(root + "bid_multipliers2_bid64.bin", bid_exponents_bid64.length * 4);
    }

    static long[] loadResource(final String resourceName, final int requiredSize) {
        try {
            final InputStream resourceStream = JavaImplCastBinary64.class.getClassLoader().getResourceAsStream(resourceName);
            if (resourceStream == null)
                throw new RuntimeException("Can't get the " + resourceName + " resource.");

            long[] values = new long[requiredSize];
            int valuesCount = 0;

            final byte[] readBuffer = new byte[Long.SIZE / Byte.SIZE];
            while (true) {
                int bufferPosition = 0;
                while (bufferPosition < readBuffer.length) {
                    final int readCount = resourceStream.read(readBuffer, bufferPosition, readBuffer.length - bufferPosition);
                    if (readCount == -1) {
                        bufferPosition = -1;
                        break;
                    }
                    bufferPosition += readCount;
                }
                if (bufferPosition == -1)
                    break;

                final long longValue =
                    (((long) readBuffer[7] << 56) +
                        ((long) (readBuffer[6] & 255) << 48) +
                        ((long) (readBuffer[5] & 255) << 40) +
                        ((long) (readBuffer[4] & 255) << 32) +
                        ((long) (readBuffer[3] & 255) << 24) +
                        ((readBuffer[2] & 255) << 16) +
                        ((readBuffer[1] & 255) << 8) +
                        ((readBuffer[0] & 255)));

                if (valuesCount >= values.length)
                    values = Arrays.copyOf(values, values.length * 3 / 2 + 1);
                values[valuesCount++] = longValue;
            }

            resourceStream.close();

            if (values.length != valuesCount)
                values = Arrays.copyOf(values, valuesCount);

            if (valuesCount != requiredSize)
                throw new RuntimeException("The " + resourceName + " resource actual size(=" + valuesCount +
                    ") != required size(=" + requiredSize + ").");

            return values;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    static final long[] /*BID_UINT128*/ bid_breakpoints_bid64_flat;
    static final long[] /*BID_UINT256*/ bid_multipliers1_bid64_flat;
    static final long[] /*BID_UINT256*/ bid_multipliers2_bid64_flat;
}
