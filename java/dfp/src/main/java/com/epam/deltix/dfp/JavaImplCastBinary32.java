package com.epam.deltix.dfp;

import static com.epam.deltix.dfp.JavaImplAdd.*;
import static com.epam.deltix.dfp.JavaImplCastBinary64.*;

public class JavaImplCastBinary32 {
    private JavaImplCastBinary32() {
    }

    public static long binary32_to_bid64(float x, final int rnd_mode/*, final JavaImplParse.FloatingPointStatusFlag pfpsf*/) {
        long /*BID_UINT128*/ c_w0, c_w1;
        long /*BID_UINT64*/ c_prov;
        long /*BID_UINT128*/ m_min_w0, m_min_w1;
        long /*BID_UINT256*/ r_w0, r_w1, r_w2, r_w3;
        long /*BID_UINT384*/ z_w0, z_w1, z_w2, z_w3, z_w4, z_w5;

        int e, s, t, e_out;

        // Unpack the input

        //unpack_binary32 (x, s, e, c.w[1], t, return_bid64_zero(s), return_bid64_inf(s), return_bid64_nan);
        {
            //union { BID_UINT32 i; float f; } x_in;
            //x_in.f = x;
            //c.w[1] = x_in.i;
            c_w1 = Float.floatToRawIntBits(x);
            e = (int) (c_w1 >>> 23) & ((1 << 8) - 1);
            s = (int) (c_w1 >>> 31);
            c_w1 = c_w1 & ((1 << 23) - 1);
            if (e == 0) {
                int l;
                if (c_w1 == 0)
                    return return_bid64_zero(s);
                l = clz32(c_w1) - (32 - 24);
                c_w1 = c_w1 << l;
                e = -(l + 149);
                t = 0;
                //__set_status_flags(pfpsf,BID_DENORMAL_EXCEPTION);
            } else if (e == ((1 << 8) - 1)) {
                if (c_w1 == 0)
                    return return_bid64_inf(s);
                //if ((c_w1 & (1 << 22)) == 0) __set_status_flags(pfpsf, BID_INVALID_EXCEPTION);
                return return_bid64_nan(s, c_w1 << 42, 0);
            } else {
                c_w1 += 1 << 23;
                t = ctz32(c_w1);
                e -= 150;
            }
        }

        // Now -172<=e<=104 (104 for max normal, -149 for min normal, -172 for min denormal)

        // Treat like a quad input for uniformity, so (2^{113-24} * c * r) >> 312
        // (312 is the shift value for these tables) which can be written as
        // (2^97 c * r) >> 320, lopping off exactly 320 bits = 5 words. Thus we put
        // input coefficient as the high part of c (<<64) shifted by 33 bits (<<97)
        //
        // Remember to compensate for the fact that exponents are integer for quad

        final int float_vs_double_mantissa_size = -(52 - 23);
        c_w1 = c_w1 << (33 + float_vs_double_mantissa_size);
        c_w0 = 0;
        t += (113 - 24 + float_vs_double_mantissa_size);
        e -= (113 - 24 + float_vs_double_mantissa_size); // Now e belongs [-238..15]

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
                long /*BID_UINT128*/ pow5_w0 = bid_coefflimits_bid64_BID_UINT128[bid_coefflimits_bid64_index],
                    pow5_w1 = bid_coefflimits_bid64_BID_UINT128[bid_coefflimits_bid64_index + 1];
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
                    pow5_w0 = bid_power_five_BID_UINT128[bid_power_five_index];
                    pow5_w1 = bid_power_five_BID_UINT128[bid_power_five_index + 1];

                    //__mul_128x128_low (cc, cc, pow5);
                    {
                        long /*BID_UINT128*/ ALBL_w0, ALBL_w1;
                        long /*BID_UINT64*/ QM64;

                        //__mul_64x64_to_128(ALBL, cc_w0, pow5_w0);
                        ALBL_w1 = Mul64Impl.unsignedMultiplyHigh(cc_w0, pow5_w0);
                        ALBL_w0 = cc_w0 * pow5_w0;


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
        m_min_w0 = bid_breakpoints_bid64_BID_UINT128[bid_breakpoints_bid64_index];
        m_min_w1 = bid_breakpoints_bid64_BID_UINT128[bid_breakpoints_bid64_index + 1];
        e_out = bid_exponents_bid64[1437 + e];

        // Choose exponent and reciprocal multiplier based on breakpoint

        final long[] bid_multipliers_bid64_BID_UINT256;
        if (le128(c_w1, c_w0, m_min_w1, m_min_w0)) {
            bid_multipliers_bid64_BID_UINT256 = bid_multipliers1_bid64_BID_UINT256;
        } else {
            bid_multipliers_bid64_BID_UINT256 = bid_multipliers2_bid64_BID_UINT256;
            e_out = e_out + 1;
        }
        final int bid_multipliers_bid64_index = (1437 + e) << 2;
        r_w0 = bid_multipliers_bid64_BID_UINT256[bid_multipliers_bid64_index];
        r_w1 = bid_multipliers_bid64_BID_UINT256[bid_multipliers_bid64_index + 1];
        r_w2 = bid_multipliers_bid64_BID_UINT256[bid_multipliers_bid64_index + 2];
        r_w3 = bid_multipliers_bid64_BID_UINT256[bid_multipliers_bid64_index + 3];

        // Do the reciprocal multiplication

        //__mul_128x256_to_384 (z, c, r)
        {
            long /*BID_UINT512*/ P0_w0, P0_w1, P0_w2, P0_w3, P0_w4, P0_w5, P0_w6, P0_w7,
                P1_w0, P1_w1, P1_w2, P1_w3, P1_w4, P1_w5, P1_w6, P1_w7;
            long /*BID_UINT64*/ CY;
            {
                long /*BID_UINT128*/ lP0_w0, lP0_w1, lP1_w0, lP1_w1, lP2_w0, lP2_w1, lP3_w0, lP3_w1;
                long /*BID_UINT64*/ lC;
                lP0_w1 = Mul64Impl.unsignedMultiplyHigh(c_w0, r_w0);
                lP0_w0 = c_w0 * r_w0;
                lP1_w1 = Mul64Impl.unsignedMultiplyHigh(c_w0, r_w1);
                lP1_w0 = c_w0 * r_w1;
                lP2_w1 = Mul64Impl.unsignedMultiplyHigh(c_w0, r_w2);
                lP2_w0 = c_w0 * r_w2;
                lP3_w1 = Mul64Impl.unsignedMultiplyHigh(c_w0, r_w3);
                lP3_w0 = c_w0 * r_w3;
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
                lP0_w1 = Mul64Impl.unsignedMultiplyHigh(c_w1, r_w0);
                lP0_w0 = c_w1 * r_w0;
                lP1_w1 = Mul64Impl.unsignedMultiplyHigh(c_w1, r_w1);
                lP1_w0 = c_w1 * r_w1;
                lP2_w1 = Mul64Impl.unsignedMultiplyHigh(c_w1, r_w2);
                lP2_w0 = c_w1 * r_w2;
                lP3_w1 = Mul64Impl.unsignedMultiplyHigh(c_w1, r_w3);
                lP3_w0 = c_w1 * r_w3;
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
            (bid_roundbound_128_BID_UINT128[bid_roundbound_128_index + 1],
                bid_roundbound_128_BID_UINT128[bid_roundbound_128_index], z_w4, z_w3)) {
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

    public static float bid64_to_binary32(final long /*BID_UINT64*/ x, final int rnd_mode/*, final JavaImplParse.FloatingPointStatusFlag pfpsf*/) {
        long /*BID_UINT64*/ c_prov;
        long /*BID_UINT128*/ c_w0, c_w1;
        long /*BID_UINT128*/ m_min_w0, m_min_w1;
        int s, e, k, e_out;
        long /*BID_UINT256*/ r_w0, r_w1, r_w2, r_w3;
        long /*BID_UINT384*/ z_w0, z_w1, z_w2, z_w3, z_w4, z_w5;

        //unpack_bid64 (x, s, e, k, (c_w0), return_binary32_zero (s), return_binary32_inf (s), return_binary32_nan);
        {
            s = (int) (x >>> 63);
            if ((x & (3L << 61)) == (3L << 61)) {
                if ((x & (0xFL << 59)) == (0xFL << 59)) {
                    if ((x & (0x1FL << 58)) != (0x1FL << 58))
                        return return_binary32_inf(s);
//                    if ((x & (1L << 57)) != 0)
//                        __set_status_flags(pfpsf, BID_INVALID_EXCEPTION);
                    return return_binary32_nan(s, (x & 0x3FFFFFFFFFFFFL) > 999999999999999L ? 0 : (x << 14), 0L);
                }
                e = (int) (((x >>> 51) & ((1L << 10) - 1)) - 398);
                c_w0 = (1L << 53) + (x & ((1L << 51) - 1));
                if (UnsignedLong.isGreater(c_w0, 9999999999999999L))
                    return return_binary32_zero(s);
                k = 0;
            } else {
                e = (int) (((x >>> 53) & ((1L << 10) - 1)) - 398);
                c_w0 = x & ((1L << 53) - 1);
                if (c_w0 == 0)
                    return return_binary32_zero(s);
                k = clz64_nz(c_w0) - 10;
                c_w0 = c_w0 << k;
            }
        }
        c_w1 = 0;

        // Correct to 2^112 <= c < 2^113 with corresponding exponent adding 113-54=59

        // sll128_short (c_w1, c_w0, 59);
        c_w1 = /*(c_w1 << 59) @optimization: always zero +*/ (c_w0 >>> (64 - 59));
        c_w0 = c_w0 << 59;

        k = k + 59;

        // Check for "trivial" overflow, when 10^e * 1 > 2^{sci_emax+1}, just to
        // keep tables smaller (it would be intercepted later otherwise).
        //
        // (Note that we may have normalized the coefficient, but we have a
        //  corresponding exponent postcorrection to account for; this can
        //  afford to be conservative anyway.)
        //
        // We actually check if e >= ceil((sci_emax + 1) * log_10(2))
        // which in this case is e >= ceil(128 * log_10(2)) = 39

        if (e >= 39) {
            //__set_status_flags(pfpsf, BID_OVERFLOW_INEXACT_EXCEPTION);
            return return_binary32_ovf(s, rnd_mode);
        }

        // Also check for "trivial" underflow, when 10^e * 2^113 <= 2^emin * 1/4,
        // so test e <= floor((emin - 115) * log_10(2))
        // In this case just fix ourselves at that value for uniformity.
        //
        // This is important not only to keep the tables small but to maintain the
        // testing of the round/sticky words as a correct rounding method

        if (e <= -80)
            e = -80;

        // Look up the breakpoint and approximate exponent

        final int bid_breakpoints_binary32_index = (80 + e) << 1;
        m_min_w0 = bid_breakpoints_binary32_BID_UINT128[bid_breakpoints_binary32_index];
        m_min_w1 = bid_breakpoints_binary32_BID_UINT128[bid_breakpoints_binary32_index + 1];
        e_out = bid_exponents_binary32[80 + e] - k;

        // Choose provisional exponent and reciprocal multiplier based on breakpoint

        final long[] bid_multipliers_binary32_BID_UINT256;
        if (le128(c_w1, c_w0, m_min_w1, m_min_w0)) {
            bid_multipliers_binary32_BID_UINT256 = bid_multipliers1_binary32_BID_UINT256;
        } else {
            bid_multipliers_binary32_BID_UINT256 = bid_multipliers2_binary32_BID_UINT256;
            e_out = e_out + 1;
        }
        final int bid_multipliers_binary32_index = (80 + e) << 2;
        r_w0 = bid_multipliers_binary32_BID_UINT256[bid_multipliers_binary32_index];
        r_w1 = bid_multipliers_binary32_BID_UINT256[bid_multipliers_binary32_index + 1];
        r_w2 = bid_multipliers_binary32_BID_UINT256[bid_multipliers_binary32_index + 2];
        r_w3 = bid_multipliers_binary32_BID_UINT256[bid_multipliers_binary32_index + 3];

        // Do the reciprocal multiplication

        //__mul_128x256_to_384 (z, c, r)
        {
            long /*BID_UINT512*/ P0_w0, P0_w1, P0_w2, P0_w3, P0_w4, P0_w5, P0_w6, P0_w7,
                P1_w0, P1_w1, P1_w2, P1_w3, P1_w4, P1_w5, P1_w6, P1_w7;
            long /*BID_UINT64*/ CY;
            {
                long /*BID_UINT128*/ lP0_w0, lP0_w1, lP1_w0, lP1_w1, lP2_w0, lP2_w1, lP3_w0, lP3_w1;
                long /*BID_UINT64*/ lC;
                lP0_w1 = Mul64Impl.unsignedMultiplyHigh(c_w0, r_w0);
                lP0_w0 = c_w0 * r_w0;
                lP1_w1 = Mul64Impl.unsignedMultiplyHigh(c_w0, r_w1);
                lP1_w0 = c_w0 * r_w1;
                lP2_w1 = Mul64Impl.unsignedMultiplyHigh(c_w0, r_w2);
                lP2_w0 = c_w0 * r_w2;
                lP3_w1 = Mul64Impl.unsignedMultiplyHigh(c_w0, r_w3);
                lP3_w0 = c_w0 * r_w3;
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
                lP0_w1 = Mul64Impl.unsignedMultiplyHigh(c_w1, r_w0);
                lP0_w0 = c_w1 * r_w0;
                lP1_w1 = Mul64Impl.unsignedMultiplyHigh(c_w1, r_w1);
                lP1_w0 = c_w1 * r_w1;
                lP2_w1 = Mul64Impl.unsignedMultiplyHigh(c_w1, r_w2);
                lP2_w0 = c_w1 * r_w2;
                lP3_w1 = Mul64Impl.unsignedMultiplyHigh(c_w1, r_w3);
                lP3_w0 = c_w1 * r_w3;
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

        // Check for exponent underflow and compensate by shifting the product
        // Cut off the process at precision+2, since we can't really shift further
        if (e_out < 1) {
            int d;
            d = 1 - e_out;
            if (d > 26)
                d = 26;
            e_out = 1;
            //srl256_short(z_w5, z_w4, z_w3, z_w2, d);
            {
                z_w2 = (z_w3 << (64 - d)) + (z_w2 >>> d);
                z_w3 = (z_w4 << (64 - d)) + (z_w3 >>> d);
                z_w4 = (z_w5 << (64 - d)) + (z_w4 >>> d);
                z_w5 = z_w5 >>> d;
            }
        }
        c_prov = z_w5;

        // Round using round-sticky words
        // If we spill into the next binade, correct
        // Flag underflow where it may be needed even for |result| = SNN

        final int bid_roundbound_128_index = ((rnd_mode << 2) + ((s & 1) << 1) + (int) (c_prov & 1)) << 1;
        if (lt128
            (bid_roundbound_128_BID_UINT128[bid_roundbound_128_index + 1],
                bid_roundbound_128_BID_UINT128[bid_roundbound_128_index], z_w4, z_w3)) {
            c_prov = c_prov + 1;
            if (c_prov == (1L << 24)) {
                c_prov = 1L << 23;
                e_out = e_out + 1;
            }
//        #if BINARY_TINY_DETECTION_AFTER_ROUNDING
//            else if ((c_prov == (1L << 23)) && (e_out == 1)) {
//                if ((((rnd_mode & 3) == 0) && UnsignedLong.isLess(z_w4, (3L << 62))) ||
//                    ((rnd_mode + (s & 1) == 2) && UnsignedLong.isLess(z_w4, (1L << 63))))
//                    __set_status_flags(pfpsf, BID_UNDERFLOW_EXCEPTION);
//            }
//        #endif
        }

        // Check for overflow

        if (e_out >= 255) {
//            __set_status_flags(pfpsf, BID_OVERFLOW_INEXACT_EXCEPTION);
            return_binary32_ovf(s, rnd_mode);
        }

        // Modify exponent for a tiny result, otherwise lop the implicit bit

        if (UnsignedLong.isLess(c_prov, (1L << 23)))
            e_out = 0;
        else
            c_prov = c_prov & ((1L << 23) - 1);

        // Set the inexact and underflow flag as appropriate

//        if ((z_w4 != 0) || (z_w3 != 0)) {
//            __set_status_flags(pfpsf, BID_INEXACT_EXCEPTION);
//            if (e_out == 0)
//                __set_status_flags(pfpsf, BID_UNDERFLOW_EXCEPTION);
//        }

        // Package up the result as a binary floating-point number

        return return_binary32(s, e_out, (int) c_prov);
    }

    static float return_binary32_zero(final int s) {
        return return_binary32(s, 0, 0);
    }

    static float return_binary32_inf(final int s) {
        return return_binary32(s, 255, 0);
    }

    static float return_binary32_nan(final int s, final long /*BID_UINT64*/ c_hi, final long /*BID_UINT64*/ c_lo) {
        return return_binary32(s, 255, (int) ((c_hi >>> 42) + (1 << 22)));
    }

    static float return_binary32_max(final int s) {
        return return_binary32(s, 254, (1 << 23) - 1);
    }

    static float return_binary32_ovf(final int s, final int rnd_mode) {
        if ((rnd_mode == BID_ROUNDING_TO_ZERO) ||
            (rnd_mode == ((s != 0) ? BID_ROUNDING_UP : BID_ROUNDING_DOWN)))
            return return_binary32_max(s);
        else
            return return_binary32_inf(s);
    }

    static float return_binary32(final int /*BID_UINT32*/ s, final int /*BID_UINT32*/ e, final int c) {
        final int x_out_i = (s << 31) + (e << 23) + (c);
        return Float.intBitsToFloat(x_out_i);
    }

    static final int CLZ32_MASK16 = 0xFFFF0000;
    static final int CLZ32_MASK8 = 0xFF00FF00;
    static final int CLZ32_MASK4 = 0xF0F0F0F0;
    static final int CLZ32_MASK2 = 0xCCCCCCCC;
    static final int CLZ32_MASK1 = 0xAAAAAAAA;

    static int clz32_nz(final int n) {
        return ((UnsignedInteger.isLessOrEqual(n & CLZ32_MASK16, n & ~CLZ32_MASK16) ? 16 : 0) +
            (UnsignedInteger.isLessOrEqual(n & CLZ32_MASK8, n & ~CLZ32_MASK8) ? 8 : 0) +
            (UnsignedInteger.isLessOrEqual(n & CLZ32_MASK4, n & ~CLZ32_MASK4) ? 4 : 0) +
            (UnsignedInteger.isLessOrEqual(n & CLZ32_MASK2, n & ~CLZ32_MASK2) ? 2 : 0) +
            (UnsignedInteger.isLessOrEqual(n & CLZ32_MASK1, n & ~CLZ32_MASK1) ? 1 : 0));
    }

    static int clz32(final long /*BID_UINT64*/ n) {
        return n == 0 ? 32 : clz32_nz((int) n);
    }

// Counting trailing zeros in an unsigned 32-bit word
// The ctz32_1bit version is for a single bit

    static int ctz32_1bit(final int n) {
        return ((n & ~CLZ32_MASK16) != 0 ? 0 : 16) +
            ((n & ~CLZ32_MASK8) != 0 ? 0 : 8) +
            ((n & ~CLZ32_MASK4) != 0 ? 0 : 4) +
            ((n & ~CLZ32_MASK2) != 0 ? 0 : 2) +
            ((n & ~CLZ32_MASK1) != 0 ? 0 : 1);
    }

    static int ctz32(final long /*BID_UINT64*/ n) {
        return n == 0 ? 32 : ctz32_1bit((int) (n & -n));
    }

// Tables of values for the various conversions:
//
// exponents: table of output exponents
// breakpoints: test values to decide between two possible exponents
// multipliers1/multipliers2: corresponding reciprocal multipliers
// coefflimits: used in exactness checks
//

    static final long[] bid_breakpoints_binary32_BID_UINT128 = new long[]{
        0xeff7b866e8bd92f7L, 0x0001afcef51f0fb5L,
        0xf32c938586fe0f2cL, 0x000159725db272f7L,
        0x8f56dc6ad264d8f0L, 0x0001145b7e285bf9L,
        0xb22493de1d6e27e7L, 0x0001ba2bfd0d5ff5L,
        0x5b50764b4abe8652L, 0x000161bcca711991L,
        0x15d9f83c3bcb9ea8L, 0x00011afd6ec0e141L,
        0x56298d2d2c78fddaL, 0x0001c4c8b1349b9bL,
        0x11bad75756c7317bL, 0x00016a3a275d4949L,
        0x74957912abd28dfcL, 0x000121c81f7dd43aL,
        0xba88c1b77950e32dL, 0x0001cfa698c95390L,
        0xc86d67c5faa71c24L, 0x000172ebad6ddc73L,
        0x39f11fd195527ce9L, 0x000128bc8abe49f6L,
        0xf64e994f5550c7dcL, 0x0001dac74463a989L,
        0x91d87aa5ddda397dL, 0x00017bd29d1c87a1L,
        0x74ad2eeb17e1c797L, 0x00012fdbb0e39fb4L,
        0x211517de8c9c728bL, 0x0001e62c4e38ff87L,
        0x4daa797ed6e38ed6L, 0x000184f03e93ff9fL,
        0x0aeec798abe93f11L, 0x0001372698766619L,
        0xab17a5c1130ecb4fL, 0x0001f1d75a5709c1L,
        0x55ac849a75a56f72L, 0x00018e45e1df3b01L,
        0x448a03aec4845928L, 0x00013e9e4e4c2f34L,
        0xd41005e46da08ea7L, 0x0001fdca16e04b86L,
        0x767337e9f14d3eecL, 0x000197d4df19d605L,
        0x2b8f5fee5aa43256L, 0x00014643e5ae44d1L,
        0x893f7ff1e21cf512L, 0x000105031e2503daL,
        0x0ecbffe969c7ee83L, 0x0001a19e96a19fc4L,
        0xd8a33321216cbecfL, 0x00014e1878814c9cL,
        0xe0828f4db456ff0cL, 0x00010b46c6cdd6e3L,
        0x00d0e549208b31adL, 0x0001aba4714957d3L,
        0x00a71dd41a08f48aL, 0x0001561d276ddfdcL,
        0x9a1f4b1014d3f6d5L, 0x000111b0ec57e649L,
        0xf6987819baecbe22L, 0x0001b5e7e08ca3a8L,
        0x2bad2ce16256fe81L, 0x00015e531a0a1c87L,
        0xbc8a8a4de8459867L, 0x000118427b3b4a05L,
        0x60ddaa16406f5a3fL, 0x0001c06a5ec5433cL,
        0xe717bb45005914ffL, 0x000166bb7f0435c9L,
        0xb8dfc904004743ffL, 0x00011efc659cf7d4L,
        0x8e32db399a0b9fffL, 0x0001cb2d6f618c87L,
        0x0b5be2947b3c7fffL, 0x00016f578c4e0a06L,
        0x6f7cb54395c9ffffL, 0x000125dfa371a19eL,
        0x4bfabb9f560fffffL, 0x0001d6329f1c35caL,
        0xd6622fb2ab3fffffL, 0x000178287f49c4a1L,
        0x11e8262888ffffffL, 0x00012ced32a16a1bL,
        0xb6403d0da7ffffffL, 0x0001e17b84357691L,
        0x2b66973e1fffffffL, 0x0001812f9cf7920eL,
        0x22b878fe7fffffffL, 0x00013426172c74d8L,
        0x378d8e63ffffffffL, 0x0001ed09bead87c0L,
        0xc60ad84fffffffffL, 0x00018a6e32246c99L,
        0x6b3be03fffffffffL, 0x00013b8b5b5056e1L,
        0x452c99ffffffffffL, 0x0001f8def8808b02L,
        0x9dbd47ffffffffffL, 0x000193e5939a08ceL,
        0x17ca9fffffffffffL, 0x0001431e0fae6d72L,
        0x13087fffffffffffL, 0x0001027e72f1f128L,
        0x1e73ffffffffffffL, 0x00019d971e4fe840L,
        0x4b8fffffffffffffL, 0x00014adf4b732033L,
        0x093fffffffffffffL, 0x000108b2a2c28029L,
        0x41ffffffffffffffL, 0x0001a784379d99dbL,
        0x67ffffffffffffffL, 0x000152d02c7e14afL,
        0x1fffffffffffffffL, 0x00010f0cf064dd59L,
        0xffffffffffffffffL, 0x0001b1ae4d6e2ef4L,
        0xffffffffffffffffL, 0x00015af1d78b58c3L,
        0xffffffffffffffffL, 0x0001158e460913cfL,
        0xffffffffffffffffL, 0x0001bc16d674ec7fL,
        0xffffffffffffffffL, 0x00016345785d89ffL,
        0xffffffffffffffffL, 0x00011c37937e07ffL,
        0xffffffffffffffffL, 0x0001c6bf52633fffL,
        0xffffffffffffffffL, 0x00016bcc41e8ffffL,
        0xffffffffffffffffL, 0x00012309ce53ffffL,
        0xffffffffffffffffL, 0x0001d1a94a1fffffL,
        0xffffffffffffffffL, 0x000174876e7fffffL,
        0xffffffffffffffffL, 0x00012a05f1ffffffL,
        0xffffffffffffffffL, 0x0001dcd64fffffffL,
        0xffffffffffffffffL, 0x00017d783fffffffL,
        0xffffffffffffffffL, 0x0001312cffffffffL,
        0xffffffffffffffffL, 0x0001e847ffffffffL,
        0xffffffffffffffffL, 0x0001869fffffffffL,
        0xffffffffffffffffL, 0x0001387fffffffffL,
        0xffffffffffffffffL, 0x0001f3ffffffffffL,
        0xffffffffffffffffL, 0x00018fffffffffffL,
        0xffffffffffffffffL, 0x00013fffffffffffL,
        0xffffffffffffffffL, 0x0000ffffffffffffL,
        0x9999999999999999L, 0x0001999999999999L,
        0xae147ae147ae147aL, 0x000147ae147ae147L,
        0xbe76c8b439581062L, 0x00010624dd2f1a9fL,
        0xca57a786c226809dL, 0x0001a36e2eb1c432L,
        0x08461f9f01b866e4L, 0x00014f8b588e368fL,
        0xd36b4c7f34938583L, 0x00010c6f7a0b5ed8L,
        0x85787a6520ec08d2L, 0x0001ad7f29abcaf4L,
        0x9df9fb841a566d74L, 0x00015798ee2308c3L,
        0x4b2e62d01511f12aL, 0x000112e0be826d69L,
        0xab7d6ae6881cb510L, 0x0001b7cdfd9d7bdbL,
        0x55fdef1ed34a2a73L, 0x00015fd7fe179649L,
        0x1197f27f0f6e885cL, 0x000119799812dea1L,
        0x1c2650cb4be40d60L, 0x0001c25c26849768L,
        0xb01ea70909833de7L, 0x00016849b86a12b9L,
        0x59b21f3a6e0297ecL, 0x0001203af9ee7561L,
        0xc2b6985d7cd0f313L, 0x0001cd2b297d889bL,
        0x6892137dfd73f5a9L, 0x000170ef54646d49L,
        0xba0e75fe645cc487L, 0x00012725dd1d243aL,
        0xc34a5663d3c7a0d8L, 0x0001d83c94fb6d2aL,
        0x35d511e976394d79L, 0x000179ca10c92422L,
        0xf7dda7edf82dd794L, 0x00012e3b40a0e9b4L,
        0x5962a6498d1625baL, 0x0001e392010175eeL,
        0x144eeb6e0a781e2fL, 0x000182db34012b25L,
        0x76a58924d52ce4f2L, 0x0001357c299a88eaL,
        0x8aa27507bb7b07eaL, 0x0001ef2d0f5da7ddL,
        0x3bb52a6c95fc0655L, 0x00018c240c4aecb1L,
        0xfc90eebd44c99eaaL, 0x00013ce9a36f23c0L,
        0x941b17953adc3110L, 0x0001fb0f6be50601L,
        0x767c12ddc8b02740L, 0x000195a5efea6b34L,
        0xf863424b06f3529aL, 0x00014484bfeebc29L,
        0xf9e901d59f290ee1L, 0x0001039d66589687L,
        0x2974cfbc31db4b02L, 0x00019f623d5a8a73L,
        0xbac3d9635b15d59bL, 0x00014c4e977ba1f5L,
        0x95697ab5e277de16L, 0x000109d8792fb4c4L,
        0xef0f2abc9d8c9689L, 0x0001a95a5b7f87a0L,
        0x25a5bbca17a3aba1L, 0x000154484932d2e7L,
        0xeaeafca1ac82efb4L, 0x00011039d428a8b8L,
        0x44ab2dcf7a6b1920L, 0x0001b38fb9daa78eL,
        0x36ef57d92ebc141aL, 0x00015c72fb1552d8L,
    };

    static final int[] bid_exponents_binary32 = {
        -27, -24, -21, -17, -14, -11, -7, -4, -1, 3,
        6, 9, 13, 16, 19, 23, 26, 29, 33, 36,
        39, 43, 46, 49, 52, 56, 59, 62, 66, 69,
        72, 76, 79, 82, 86, 89, 92, 96, 99, 102,
        106, 109, 112, 116, 119, 122, 126, 129, 132, 136,
        139, 142, 145, 149, 152, 155, 159, 162, 165, 169,
        172, 175, 179, 182, 185, 189, 192, 195, 199, 202,
        205, 209, 212, 215, 219, 222, 225, 229, 232, 235,
        238, 242, 245, 248, 252, 255, 258, 262, 265, 268,
        272, 275, 278, 282, 285, 288, 292, 295, 298, 302,
        305, 308, 312, 315, 318, 322, 325, 328, 332, 335,
        338, 341, 345, 348, 351, 355, 358, 361, 365, 368,
    };

    static final long[] bid_multipliers1_binary32_BID_UINT256 = new long[]{
        0x5375a13ad57881e8L, 0x67d41a021da8c6f1L, 0x0919a5dccd879fc9L, 0x00000097c560ba6bL,
        0xa85309898ad6a262L, 0xc1c92082a512f8adL, 0xcb600f5400e987bbL, 0x000000bdb6b8e905L,
        0x1267cbebed8c4afaL, 0xb23b68a34e57b6d9L, 0x3e3813290123e9aaL, 0x000000ed24672347L,
        0xab80df737477aedcL, 0xaf65216610f6d247L, 0x86e30bf9a0b6720aL, 0x0000009436c0760cL,
        0x9661175051959a93L, 0x5b3e69bf953486d9L, 0xa89bcef808e40e8dL, 0x000000b94470938fL,
        0xfbf95d2465fb0138L, 0xb20e042f7a81a88fL, 0x92c2c2b60b1d1230L, 0x000000e7958cb873L,
        0xfd7bda36bfbce0c3L, 0x6f48c29dac910959L, 0x3bb9b9b1c6f22b5eL, 0x00000090bd77f348L,
        0x7cdad0c46fac18f4L, 0x0b1af34517b54bb0L, 0x4aa8281e38aeb636L, 0x000000b4ecd5f01aL,
        0x9c1184f58b971f31L, 0x8de1b0165da29e9cL, 0xdd523225c6da63c3L, 0x000000e2280b6c20L,
        0xe18af319773e737fL, 0x38ad0e0dfa85a321L, 0x8a535f579c487e5aL, 0x0000008d59072394L,
        0x59edafdfd50e105eL, 0xc6d8519179270beaL, 0xace8372d835a9df0L, 0x000000b0af48ec79L,
        0xf0691bd7ca519476L, 0xf88e65f5d770cee4L, 0x182244f8e431456cL, 0x000000dcdb1b2798L,
        0x1641b166de72fccaL, 0x1b58ffb9a6a6814fL, 0x0f156b1b8e9ecb64L, 0x0000008a08f0f8bfL,
        0xdbd21dc0960fbbfcL, 0x222f3fa8105021a2L, 0xd2dac5e272467e3dL, 0x000000ac8b2d36eeL,
        0x92c6a530bb93aafbL, 0x6abb0f9214642a0bL, 0x8791775b0ed81dccL, 0x000000d7adf884aaL,
        0x3bbc273e753c4addL, 0xc2b4e9bb4cbe9a47L, 0x94baea98e947129fL, 0x00000086ccbb52eaL,
        0x0aab310e128b5d94L, 0xb362242a1fee40d9L, 0x39e9a53f2398d747L, 0x000000a87fea27a5L,
        0x4d55fd51972e34f9L, 0xa03aad34a7e9d10fL, 0x88640e8eec7f0d19L, 0x000000d29fe4b18eL,
        0x9055be52fe7ce11cL, 0x0424ac40e8f222a9L, 0x153e891953cf6830L, 0x00000083a3eeeef9L,
        0xf46b2de7be1c1963L, 0x052dd751232eab53L, 0x5a8e2b5fa8c3423cL, 0x000000a48ceaaab7L,
        0xf185f961ada31fbbL, 0x06794d256bfa5628L, 0x3131b63792f412cbL, 0x000000cdb0255565L,
        0x96f3bbdd0c85f3d5L, 0xe40bd037637c75d9L, 0x3ebf11e2bbd88bbeL, 0x000000808e17555fL,
        0xfcb0aad44fa770caL, 0x9d0ec4453c5b934fL, 0x0e6ed65b6aceaeaeL, 0x000000a0b19d2ab7L,
        0xfbdcd58963914cfdL, 0x445275568b727823L, 0xd20a8bf245825a5aL, 0x000000c8de047564L,
        0xfad40aebbc75a03cL, 0xd56712ac2e4f162cL, 0x068d2eeed6e2f0f0L, 0x000000fb158592beL,
        0x1cc486d355c98426L, 0x85606bab9cf16ddcL, 0xc4183d55464dd696L, 0x0000009ced737bb6L,
        0x23f5a8882b3be52fL, 0x26b88696842dc953L, 0x751e4caa97e14c3cL, 0x000000c428d05aa4L,
        0xecf312aa360ade7aL, 0x3066a83c25393ba7L, 0x9265dfd53dd99f4bL, 0x000000f53304714dL,
        0xf417ebaa61c6cb0dL, 0xfe4029259743c548L, 0x7b7fabe546a8038eL, 0x000000993fe2c6d0L,
        0x311de694fa387dd0L, 0xbdd0336efd14b69bL, 0x9a5f96de98520472L, 0x000000bf8fdb7884L,
        0xfd65603a38c69d44L, 0x6d44404abc59e441L, 0xc0f77c963e66858fL, 0x000000ef73d256a5L,
        0x3e5f5c24637c224aL, 0xa44aa82eb5b82ea9L, 0x989aaddde7001379L, 0x00000095a8637627L,
        0x8df7332d7c5b2addL, 0x0d5d523a63263a53L, 0x7ec1595560c01858L, 0x000000bb127c53b1L,
        0x7174fff8db71f594L, 0x10b4a6c8fbefc8e8L, 0xde71afaab8f01e6eL, 0x000000e9d71b689dL,
        0x46e91ffb8927397dL, 0xca70e83d9d75dd91L, 0xab070dcab3961304L, 0x0000009226712162L,
        0x98a367fa6b7107dcL, 0xfd0d224d04d354f5L, 0x55c8d13d607b97c5L, 0x000000b6b00d69bbL,
        0xfecc41f9064d49d3L, 0x7c506ae046082a32L, 0x2b3b058cb89a7db7L, 0x000000e45c10c42aL,
        0xdf3fa93ba3f04e24L, 0xadb242cc2bc51a5fL, 0x5b04e377f3608e92L, 0x0000008eb98a7a9aL,
        0xd70f938a8cec61adL, 0x591ed37f36b660f7L, 0xf1c61c55f038b237L, 0x000000b267ed1940L,
        0xccd3786d30277a18L, 0x2f66885f0463f935L, 0x2e37a36b6c46dec5L, 0x000000df01e85f91L,
        0xa0042b443e18ac4fL, 0x3da0153b62be7bc1L, 0xbce2c62323ac4b3bL, 0x0000008b61313bbaL,
        0x080536154d9ed763L, 0x0d081a8a3b6e1ab2L, 0x6c1b77abec975e0aL, 0x000000ae397d8aa9L,
        0x8a06839aa1068d3bL, 0x904a212cca49a15eL, 0xc7225596e7bd358cL, 0x000000d9c7dced53L,
        0x16441240a4a41845L, 0xda2e54bbfe6e04dbL, 0x5c75757e50d64177L, 0x000000881cea1454L,
        0xdbd516d0cdcd1e56L, 0xd0b9e9eafe098611L, 0x7392d2dde50bd1d5L, 0x000000aa24249969L,
        0x52ca5c85014065ecL, 0x44e86465bd8be796L, 0xd07787955e4ec64bL, 0x000000d4ad2dbfc3L,
        0xf3be79d320c83fb3L, 0x0b113ebf967770bdL, 0x624ab4bd5af13befL, 0x00000084ec3c97daL,
        0x70ae1847e8fa4fa0L, 0xcdd58e6f7c154cedL, 0xfadd61ecb1ad8aeaL, 0x000000a6274bbdd0L,
        0xccd99e59e338e388L, 0x814af20b5b1aa028L, 0x3994ba67de18eda5L, 0x000000cfb11ead45L,
        0x800802f82e038e35L, 0x70ced74718f0a419L, 0x43fcf480eacf9487L, 0x00000081ceb32c4bL,
        0xe00a03b6398471c2L, 0x4d028d18df2ccd1fL, 0x14fc31a1258379a9L, 0x000000a2425ff75eL,
        0xd80c84a3c7e58e33L, 0xa043305f16f80067L, 0x9a3b3e096ee45813L, 0x000000cad2f7f535L,
        0xce0fa5ccb9def1c0L, 0x8853fc76dcb60081L, 0x00ca0d8bca9d6e18L, 0x000000fd87b5f283L,
        0x20c9c79ff42b5718L, 0x55347dca49f1c051L, 0xe07e48775ea264cfL, 0x0000009e74d1b791L,
        0x68fc3987f1362cdeL, 0x2a819d3cdc6e3065L, 0x589dda95364afe03L, 0x000000c612062576L,
        0xc33b47e9ed83b815L, 0xf522048c1389bc7eL, 0xeec5513a83ddbd83L, 0x000000f79687aed3L,
        0x3a050cf23472530dL, 0x793542d78c3615cfL, 0x753b52c4926a9672L, 0x0000009abe14cd44L,
        0x0886502ec18ee7d1L, 0x1782938d6f439b43L, 0x928a2775b7053c0fL, 0x000000c16d9a0095L,
        0xcaa7e43a71f2a1c5L, 0xdd633870cb148213L, 0xf72cb15324c68b12L, 0x000000f1c90080baL,
        0x5ea8eea48737a51bL, 0xca5e03467eecd14cL, 0xda7beed3f6fc16ebL, 0x000000971da05074L,
        0x76532a4da9058e62L, 0xbcf584181ea8059fL, 0x111aea88f4bb1ca6L, 0x000000bce5086492L,
        0x53e7f4e11346f1faL, 0x6c32e51e26520707L, 0x9561a52b31e9e3d0L, 0x000000ec1e4a7db6L,
        0x9470f90cac0c573cL, 0x439fcf32d7f34464L, 0x1d5d073aff322e62L, 0x0000009392ee8e92L,
        0xb98d374fd70f6d0bL, 0xd487c2ff8df0157dL, 0xa4b44909befeb9faL, 0x000000b877aa3236L,
        0x27f08523ccd3484eL, 0x89a9b3bf716c1addL, 0x4de15b4c2ebe6879L, 0x000000e69594bec4L,
        0x38f6533660040d31L, 0xf60a1057a6e390caL, 0xb0acd90f9d37014bL, 0x000000901d7cf73aL,
        0xc733e803f805107dL, 0xf38c946d909c74fcL, 0x5cd80f538484c19eL, 0x000000b424dc3509L,
        0xf900e204f606549cL, 0xb06fb988f4c3923bL, 0xb40e132865a5f206L, 0x000000e12e13424bL,
        0x7ba08d4319c3f4e2L, 0x2e45d3f598fa3b65L, 0x5088cbf93f87b744L, 0x0000008cbccc096fL,
        0xda88b093e034f21aL, 0x39d748f2ff38ca3eL, 0x24aafef78f69a515L, 0x000000afebff0bcbL,
        0x912adcb8d8422ea1L, 0x884d1b2fbf06fcceL, 0xedd5beb573440e5aL, 0x000000dbe6fecebdL,
        0x1abac9f387295d25L, 0x953030fdd7645e01L, 0xb4a59731680a88f8L, 0x00000089705f4136L,
        0x61697c7068f3b46eL, 0xba7c3d3d4d3d7581L, 0x61cefcfdc20d2b36L, 0x000000abcc771184L,
        0xb9c3db8c8330a189L, 0x691b4c8ca08cd2e1L, 0x7a42bc3d32907604L, 0x000000d6bf94d5e5L,
        0x141a6937d1fe64f6L, 0xc1b10fd7e45803cdL, 0x6c69b5a63f9a49c2L, 0x0000008637bd05afL,
        0x59210385c67dfe33L, 0x721d53cddd6e04c0L, 0x4784230fcf80dc33L, 0x000000a7c5ac471bL,
        0x6f694467381d7dc0L, 0x4ea4a8c154c985f0L, 0x19652bd3c3611340L, 0x000000d1b71758e2L,
        0x45a1cac083126e98L, 0x3126e978d4fdf3b6L, 0x4fdf3b645a1cac08L, 0x00000083126e978dL,
        0xd70a3d70a3d70a3eL, 0x3d70a3d70a3d70a3L, 0xa3d70a3d70a3d70aL, 0x000000a3d70a3d70L,
        0xcccccccccccccccdL, 0xccccccccccccccccL, 0xccccccccccccccccL, 0x000000ccccccccccL,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000010000000000L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x000000a000000000L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x000000c800000000L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x000000fa00000000L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000009c40000000L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x000000c350000000L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x000000f424000000L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000009896800000L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x000000bebc200000L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x000000ee6b280000L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000009502f90000L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x000000ba43b74000L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x000000e8d4a51000L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000009184e72a00L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x000000b5e620f480L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x000000e35fa931a0L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000008e1bc9bf04L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x000000b1a2bc2ec5L,
        0x0000000000000000L, 0x0000000000000000L, 0x4000000000000000L, 0x000000de0b6b3a76L,
        0x0000000000000000L, 0x0000000000000000L, 0xe800000000000000L, 0x0000008ac7230489L,
        0x0000000000000000L, 0x0000000000000000L, 0x6200000000000000L, 0x000000ad78ebc5acL,
        0x0000000000000000L, 0x0000000000000000L, 0x7a80000000000000L, 0x000000d8d726b717L,
        0x0000000000000000L, 0x0000000000000000L, 0xac90000000000000L, 0x000000878678326eL,
        0x0000000000000000L, 0x0000000000000000L, 0x57b4000000000000L, 0x000000a968163f0aL,
        0x0000000000000000L, 0x0000000000000000L, 0xeda1000000000000L, 0x000000d3c21bceccL,
        0x0000000000000000L, 0x0000000000000000L, 0x1484a00000000000L, 0x0000008459516140L,
        0x0000000000000000L, 0x0000000000000000L, 0x19a5c80000000000L, 0x000000a56fa5b990L,
        0x0000000000000000L, 0x0000000000000000L, 0x200f3a0000000000L, 0x000000cecb8f27f4L,
        0x0000000000000000L, 0x0000000000000000L, 0x9409844000000000L, 0x000000813f3978f8L,
        0x0000000000000000L, 0x0000000000000000L, 0xb90be55000000000L, 0x000000a18f07d736L,
        0x0000000000000000L, 0x0000000000000000L, 0x674edea400000000L, 0x000000c9f2c9cd04L,
        0x0000000000000000L, 0x0000000000000000L, 0x8122964d00000000L, 0x000000fc6f7c4045L,
        0x0000000000000000L, 0x0000000000000000L, 0x70b59df020000000L, 0x0000009dc5ada82bL,
        0x0000000000000000L, 0x0000000000000000L, 0x4ce3056c28000000L, 0x000000c537191236L,
        0x0000000000000000L, 0x0000000000000000L, 0xe01bc6c732000000L, 0x000000f684df56c3L,
        0x0000000000000000L, 0x0000000000000000L, 0x6c115c3c7f400000L, 0x0000009a130b963aL,
        0x0000000000000000L, 0x0000000000000000L, 0x0715b34b9f100000L, 0x000000c097ce7bc9L,
        0x0000000000000000L, 0x0000000000000000L, 0x48db201e86d40000L, 0x000000f0bdc21abbL,
        0x0000000000000000L, 0x0000000000000000L, 0x0d88f41314448000L, 0x00000096769950b5L,
        0x0000000000000000L, 0x0000000000000000L, 0x50eb3117d955a000L, 0x000000bc143fa4e2L,
    };
    static final long[] bid_multipliers2_binary32_BID_UINT256 = new long[]{
        0xa9bad09d6abc40f4L, 0xb3ea0d010ed46378L, 0x848cd2ee66c3cfe4L, 0x0000004be2b05d35L,
        0xd42984c4c56b5131L, 0xe0e4904152897c56L, 0xe5b007aa0074c3ddL, 0x0000005edb5c7482L,
        0x8933e5f5f6c6257dL, 0x591db451a72bdb6cL, 0x9f1c09948091f4d5L, 0x00000076923391a3L,
        0xd5c06fb9ba3bd76eL, 0x57b290b3087b6923L, 0x437185fcd05b3905L, 0x0000004a1b603b06L,
        0xcb308ba828cacd4aL, 0xad9f34dfca9a436cL, 0xd44de77c04720746L, 0x0000005ca23849c7L,
        0xfdfcae9232fd809cL, 0x59070217bd40d447L, 0xc961615b058e8918L, 0x00000073cac65c39L,
        0xfebded1b5fde7062L, 0x37a4614ed64884acL, 0x1ddcdcd8e37915afL, 0x000000485ebbf9a4L,
        0x3e6d686237d60c7aL, 0x058d79a28bdaa5d8L, 0x2554140f1c575b1bL, 0x0000005a766af80dL,
        0x4e08c27ac5cb8f99L, 0xc6f0d80b2ed14f4eL, 0x6ea91912e36d31e1L, 0x000000711405b610L,
        0xf0c5798cbb9f39c0L, 0x1c568706fd42d190L, 0x4529afabce243f2dL, 0x00000046ac8391caL,
        0x2cf6d7efea87082fL, 0x636c28c8bc9385f5L, 0xd6741b96c1ad4ef8L, 0x0000005857a4763cL,
        0x78348debe528ca3bL, 0x7c4732faebb86772L, 0x0c11227c7218a2b6L, 0x0000006e6d8d93ccL,
        0x8b20d8b36f397e65L, 0x0dac7fdcd35340a7L, 0x878ab58dc74f65b2L, 0x0000004504787c5fL,
        0x6de90ee04b07ddfeL, 0x91179fd4082810d1L, 0x696d62f139233f1eL, 0x0000005645969b77L,
        0xc96352985dc9d57eL, 0x355d87c90a321505L, 0x43c8bbad876c0ee6L, 0x0000006bd6fc4255L,
        0x9dde139f3a9e256fL, 0xe15a74dda65f4d23L, 0x4a5d754c74a3894fL, 0x00000043665da975L,
        0x855598870945aecaL, 0xd9b112150ff7206cL, 0x9cf4d29f91cc6ba3L, 0x000000543ff513d2L,
        0xa6aafea8cb971a7dL, 0xd01d569a53f4e887L, 0x44320747763f868cL, 0x000000694ff258c7L,
        0xc82adf297f3e708eL, 0x0212562074791154L, 0x8a9f448ca9e7b418L, 0x00000041d1f7777cL,
        0xfa3596f3df0e0cb2L, 0x0296eba8919755a9L, 0xad4715afd461a11eL, 0x000000524675555bL,
        0x78c2fcb0d6d18fdeL, 0x833ca692b5fd2b14L, 0x9898db1bc97a0965L, 0x00000066d812aab2L,
        0xcb79ddee8642f9ebL, 0x7205e81bb1be3aecL, 0x9f5f88f15dec45dfL, 0x00000040470baaafL,
        0xfe58556a27d3b865L, 0x4e8762229e2dc9a7L, 0x87376b2db5675757L, 0x0000005058ce955bL,
        0xfdee6ac4b1c8a67fL, 0x22293aab45b93c11L, 0x690545f922c12d2dL, 0x000000646f023ab2L,
        0x7d6a0575de3ad01eL, 0x6ab3895617278b16L, 0x034697776b717878L, 0x0000007d8ac2c95fL,
        0x0e624369aae4c213L, 0x42b035d5ce78b6eeL, 0x620c1eaaa326eb4bL, 0x0000004e76b9bddbL,
        0x91fad444159df298L, 0x135c434b4216e4a9L, 0x3a8f26554bf0a61eL, 0x0000006214682d52L,
        0xf67989551b056f3dL, 0x9833541e129c9dd3L, 0xc932efea9eeccfa5L, 0x0000007a998238a6L,
        0x7a0bf5d530e36587L, 0x7f201492cba1e2a4L, 0x3dbfd5f2a35401c7L, 0x0000004c9ff16368L,
        0x988ef34a7d1c3ee8L, 0x5ee819b77e8a5b4dL, 0x4d2fcb6f4c290239L, 0x0000005fc7edbc42L,
        0xfeb2b01d1c634ea2L, 0xb6a220255e2cf220L, 0xe07bbe4b1f3342c7L, 0x00000077b9e92b52L,
        0x9f2fae1231be1125L, 0xd22554175adc1754L, 0xcc4d56eef38009bcL, 0x0000004ad431bb13L,
        0xc6fb9996be2d956fL, 0x06aea91d31931d29L, 0xbf60acaab0600c2cL, 0x0000005d893e29d8L,
        0x38ba7ffc6db8facaL, 0x085a53647df7e474L, 0xef38d7d55c780f37L, 0x00000074eb8db44eL,
        0xa3748ffdc4939cbfL, 0x6538741ecebaeec8L, 0x558386e559cb0982L, 0x00000049133890b1L,
        0xcc51b3fd35b883eeL, 0xfe8691268269aa7aL, 0xaae4689eb03dcbe2L, 0x0000005b5806b4ddL,
        0x7f6620fc8326a4eaL, 0xbe28357023041519L, 0x159d82c65c4d3edbL, 0x000000722e086215L,
        0xef9fd49dd1f82712L, 0x56d9216615e28d2fL, 0x2d8271bbf9b04749L, 0x000000475cc53d4dL,
        0xeb87c9c5467630d7L, 0xac8f69bf9b5b307bL, 0x78e30e2af81c591bL, 0x0000005933f68ca0L,
        0xe669bc369813bd0cL, 0x97b3442f8231fc9aL, 0x971bd1b5b6236f62L, 0x0000006f80f42fc8L,
        0xd00215a21f0c5628L, 0x9ed00a9db15f3de0L, 0x5e71631191d6259dL, 0x00000045b0989dddL,
        0x04029b0aa6cf6bb2L, 0x06840d451db70d59L, 0xb60dbbd5f64baf05L, 0x000000571cbec554L,
        0x450341cd5083469eL, 0x482510966524d0afL, 0xe3912acb73de9ac6L, 0x0000006ce3ee76a9L,
        0x8b22092052520c23L, 0xed172a5dff37026dL, 0x2e3ababf286b20bbL, 0x000000440e750a2aL,
        0xedea8b6866e68f2bL, 0xe85cf4f57f04c308L, 0xb9c9696ef285e8eaL, 0x0000005512124cb4L,
        0x29652e4280a032f6L, 0xa2743232dec5f3cbL, 0xe83bc3caaf276325L, 0x0000006a5696dfe1L,
        0xf9df3ce990641fdaL, 0x85889f5fcb3bb85eL, 0x31255a5ead789df7L, 0x00000042761e4bedL,
        0xb8570c23f47d27d0L, 0x66eac737be0aa676L, 0x7d6eb0f658d6c575L, 0x0000005313a5dee8L,
        0x666ccf2cf19c71c4L, 0xc0a57905ad8d5014L, 0x9cca5d33ef0c76d2L, 0x00000067d88f56a2L,
        0xc004017c1701c71bL, 0xb8676ba38c78520cL, 0xa1fe7a407567ca43L, 0x00000040e7599625L,
        0xf00501db1cc238e1L, 0xa681468c6f96668fL, 0x0a7e18d092c1bcd4L, 0x00000051212ffbafL,
        0xec064251e3f2c71aL, 0xd021982f8b7c0033L, 0xcd1d9f04b7722c09L, 0x00000065697bfa9aL,
        0xe707d2e65cef78e0L, 0x4429fe3b6e5b0040L, 0x806506c5e54eb70cL, 0x0000007ec3daf941L,
        0x9064e3cffa15ab8cL, 0xaa9a3ee524f8e028L, 0xf03f243baf513267L, 0x0000004f3a68dbc8L,
        0xb47e1cc3f89b166fL, 0x9540ce9e6e371832L, 0x2c4eed4a9b257f01L, 0x00000063090312bbL,
        0x619da3f4f6c1dc0bL, 0xfa91024609c4de3fL, 0xf762a89d41eedec1L, 0x0000007bcb43d769L,
        0x9d0286791a392987L, 0x3c9aa16bc61b0ae7L, 0x3a9da96249354b39L, 0x0000004d5f0a66a2L,
        0x8443281760c773e9L, 0x8bc149c6b7a1cda1L, 0xc94513badb829e07L, 0x00000060b6cd004aL,
        0xe553f21d38f950e3L, 0x6eb19c38658a4109L, 0x7b9658a992634589L, 0x00000078e480405dL,
        0x2f547752439bd28eL, 0xe52f01a33f7668a6L, 0x6d3df769fb7e0b75L, 0x0000004b8ed0283aL,
        0xbb299526d482c731L, 0x5e7ac20c0f5402cfL, 0x088d75447a5d8e53L, 0x0000005e72843249L,
        0xa9f3fa7089a378fdL, 0x3619728f13290383L, 0x4ab0d29598f4f1e8L, 0x000000760f253edbL,
        0x4a387c8656062b9eL, 0x21cfe7996bf9a232L, 0x0eae839d7f991731L, 0x00000049c9774749L,
        0xdcc69ba7eb87b686L, 0x6a43e17fc6f80abeL, 0x525a2484df7f5cfdL, 0x0000005c3bd5191bL,
        0x93f84291e669a427L, 0xc4d4d9dfb8b60d6eL, 0x26f0ada6175f343cL, 0x000000734aca5f62L,
        0x1c7b299b30020699L, 0xfb05082bd371c865L, 0x58566c87ce9b80a5L, 0x000000480ebe7b9dL,
        0x6399f401fc02883fL, 0x79c64a36c84e3a7eL, 0xae6c07a9c24260cfL, 0x0000005a126e1a84L,
        0xfc8071027b032a4eL, 0x5837dcc47a61c91dL, 0xda07099432d2f903L, 0x000000709709a125L,
        0xbdd046a18ce1fa71L, 0x1722e9facc7d1db2L, 0xa84465fc9fc3dba2L, 0x000000465e6604b7L,
        0x6d445849f01a790dL, 0x9ceba4797f9c651fL, 0x92557f7bc7b4d28aL, 0x00000057f5ff85e5L,
        0x48956e5c6c211751L, 0x44268d97df837e67L, 0xf6eadf5ab9a2072dL, 0x0000006df37f675eL,
        0x8d5d64f9c394ae93L, 0x4a98187eebb22f00L, 0x5a52cb98b405447cL, 0x00000044b82fa09bL,
        0xb0b4be383479da37L, 0x5d3e1e9ea69ebac0L, 0x30e77e7ee106959bL, 0x00000055e63b88c2L,
        0xdce1edc6419850c5L, 0x348da64650466970L, 0xbd215e1e99483b02L, 0x0000006b5fca6af2L,
        0x8a0d349be8ff327bL, 0x60d887ebf22c01e6L, 0xb634dad31fcd24e1L, 0x000000431bde82d7L,
        0x2c9081c2e33eff1aL, 0xb90ea9e6eeb70260L, 0xa3c21187e7c06e19L, 0x00000053e2d6238dL,
        0x37b4a2339c0ebee0L, 0x27525460aa64c2f8L, 0x0cb295e9e1b089a0L, 0x00000068db8bac71L,
        0x22d0e5604189374cL, 0x189374bc6a7ef9dbL, 0xa7ef9db22d0e5604L, 0x0000004189374bc6L,
        0xeb851eb851eb851fL, 0x1eb851eb851eb851L, 0x51eb851eb851eb85L, 0x00000051eb851eb8L,
        0x6666666666666667L, 0x6666666666666666L, 0x6666666666666666L, 0x0000006666666666L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000008000000000L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000005000000000L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000006400000000L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000007d00000000L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000004e20000000L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x00000061a8000000L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000007a12000000L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000004c4b400000L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000005f5e100000L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000007735940000L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000004a817c8000L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000005d21dba000L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x000000746a528800L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x00000048c2739500L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000005af3107a40L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x00000071afd498d0L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x000000470de4df82L,
        0x0000000000000000L, 0x0000000000000000L, 0x8000000000000000L, 0x00000058d15e1762L,
        0x0000000000000000L, 0x0000000000000000L, 0x2000000000000000L, 0x0000006f05b59d3bL,
        0x0000000000000000L, 0x0000000000000000L, 0xf400000000000000L, 0x0000004563918244L,
        0x0000000000000000L, 0x0000000000000000L, 0x3100000000000000L, 0x00000056bc75e2d6L,
        0x0000000000000000L, 0x0000000000000000L, 0xbd40000000000000L, 0x0000006c6b935b8bL,
        0x0000000000000000L, 0x0000000000000000L, 0x5648000000000000L, 0x00000043c33c1937L,
        0x0000000000000000L, 0x0000000000000000L, 0x2bda000000000000L, 0x00000054b40b1f85L,
        0x0000000000000000L, 0x0000000000000000L, 0x76d0800000000000L, 0x00000069e10de766L,
        0x0000000000000000L, 0x0000000000000000L, 0x0a42500000000000L, 0x000000422ca8b0a0L,
        0x0000000000000000L, 0x0000000000000000L, 0x0cd2e40000000000L, 0x00000052b7d2dcc8L,
        0x0000000000000000L, 0x0000000000000000L, 0x10079d0000000000L, 0x0000006765c793faL,
        0x0000000000000000L, 0x0000000000000000L, 0x4a04c22000000000L, 0x000000409f9cbc7cL,
        0x0000000000000000L, 0x0000000000000000L, 0x5c85f2a800000000L, 0x00000050c783eb9bL,
        0x0000000000000000L, 0x0000000000000000L, 0x33a76f5200000000L, 0x00000064f964e682L,
        0x0000000000000000L, 0x0000000000000000L, 0xc0914b2680000000L, 0x0000007e37be2022L,
        0x0000000000000000L, 0x0000000000000000L, 0xb85acef810000000L, 0x0000004ee2d6d415L,
        0x0000000000000000L, 0x0000000000000000L, 0x267182b614000000L, 0x000000629b8c891bL,
        0x0000000000000000L, 0x0000000000000000L, 0xf00de36399000000L, 0x0000007b426fab61L,
        0x0000000000000000L, 0x0000000000000000L, 0x3608ae1e3fa00000L, 0x0000004d0985cb1dL,
        0x0000000000000000L, 0x0000000000000000L, 0x838ad9a5cf880000L, 0x000000604be73de4L,
        0x0000000000000000L, 0x0000000000000000L, 0xa46d900f436a0000L, 0x000000785ee10d5dL,
        0x0000000000000000L, 0x0000000000000000L, 0x86c47a098a224000L, 0x0000004b3b4ca85aL,
        0x0000000000000000L, 0x0000000000000000L, 0x2875988becaad000L, 0x0000005e0a1fd271L,
    };
}
