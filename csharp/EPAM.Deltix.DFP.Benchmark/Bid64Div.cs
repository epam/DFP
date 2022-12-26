using static EPAM.Deltix.DFP.BidDecimalData;
using static EPAM.Deltix.DFP.BidInternal;

using BID_UINT64 = System.UInt64;
using BID_SINT64 = System.Int64;
using BID_UINT32 = System.UInt32;
using _IDEC_flags = System.UInt32;
using int_double = System.UInt64;
using int_float = System.UInt32;
using unsigned = System.UInt32;
using System.Runtime.CompilerServices;

namespace EPAM.Deltix.DFP
{
	internal static class Bid64Div
	{
		/// <summary>
		///  Algorithm description:
		///
		///  if(coefficient_x<coefficient_y)
		///    p = number_digits(coefficient_y) - number_digits(coefficient_x)
		///    A = coefficient_x*10^p
		///    B = coefficient_y
		///    CA= A*10^(15+j), j=0 for A>=B, 1 otherwise
		///    Q = 0
		///  else
		///    get Q=(int)(coefficient_x/coefficient_y)
		///        (based on double precision divide)
		///    check for exact divide case
		///    Let R = coefficient_x - Q*coefficient_y
		///    Let m=16-number_digits(Q)
		///    CA=R*10^m, Q=Q*10^m
		///    B = coefficient_y
		///  endif
		///    if (CA<2^64)
		///      Q += CA/B  (64-bit unsigned divide)
		///    else
		///      get final Q using double precision divide, followed by 3 integer
		///          iterations
		///    if exact result, eliminate trailing zeros
		///    check for underflow
		///    round coefficient to nearest
		/// </summary>
#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe BID_UINT64 bid64_div(BID_UINT64 x, BID_UINT64 y
#if !IEEE_ROUND_NEAREST
			, int rnd_mode
#endif
#if BID_SET_STATUS_FLAGS
			, ref _IDEC_flags pfpsf
#endif
			)
		{
			BID_UINT128 CA, CT;
			BID_UINT64 sign_x, sign_y, coefficient_x, coefficient_y, A, B, QX, PD;
			BID_UINT64 A2, Q, Q2, B2, B4, B5, R, T, DU, res;
			BID_UINT64 valid_x, valid_y;
			BID_SINT64 D;
			double t_scale_d;
			int_double tempq_i, temp_b_i;
			int_float tempx_i, tempy_i;
			double da, db, dq, da_h, da_l;
			int exponent_x, exponent_y, bin_expon_cx;
			int diff_expon, ed1, ed2, bin_index;
#if !IEEE_ROUND_NEAREST
			int rmode;
#endif
			int amount;
			int nzeros, i, j, k, d5;
			BID_UINT32 QX32, digit, digit_h, digit_low;
			BID_UINT32* tdigit = stackalloc BID_UINT32[3];

			//valid_x = unpack_BID64(out sign_x, out exponent_x, out coefficient_x, x);
			{
				sign_x = x & 0x8000000000000000UL;

				if ((x & SPECIAL_ENCODING_MASK64) != SPECIAL_ENCODING_MASK64)
				{
					// exponent
					exponent_x = (int)((x >> EXPONENT_SHIFT_SMALL64) & EXPONENT_MASK64);
					// coefficient
					coefficient_x = (x & SMALL_COEFF_MASK64);

					valid_x = coefficient_x;
				}
				else
				{
					// special encodings
					if ((x & INFINITY_MASK64) == INFINITY_MASK64)
					{
						exponent_x = 0;
						coefficient_x = x & 0xfe03ffffffffffffUL;
						if ((x & 0x0003ffffffffffffUL) >= 1000000000000000UL)
							coefficient_x = x & 0xfe00000000000000UL;
						if ((x & NAN_MASK64) == INFINITY_MASK64)
							coefficient_x = x & SINFINITY_MASK64;
						valid_x = 0; // NaN or Infinity
					}
					else
					{
						// coefficient
						BID_UINT64 coeff = (x & LARGE_COEFF_MASK64) | LARGE_COEFF_HIGH_BIT64;
						// check for non-canonical values
						if (coeff >= 10000000000000000UL)
							coeff = 0;
						coefficient_x = coeff;
						// get exponent
						exponent_x = (int)((x >> EXPONENT_SHIFT_LARGE64) & EXPONENT_MASK64);
						valid_x = coeff;
					}
				}
			}

			//valid_y = unpack_BID64(out sign_y, out exponent_y, out coefficient_y, y);
			{
				sign_y = y & 0x8000000000000000UL;

				if ((y & SPECIAL_ENCODING_MASK64) != SPECIAL_ENCODING_MASK64)
				{
					// exponent
					exponent_y = (int)((y >> EXPONENT_SHIFT_SMALL64) & EXPONENT_MASK64);
					// coefficient
					coefficient_y = (y & SMALL_COEFF_MASK64);

					valid_y = coefficient_y;
				}
				else
				{
					// special encodings
					if ((y & INFINITY_MASK64) == INFINITY_MASK64)
					{
						exponent_y = 0;
						coefficient_y = y & 0xfe03ffffffffffffUL;
						if ((y & 0x0003ffffffffffffUL) >= 1000000000000000UL)
							coefficient_y = y & 0xfe00000000000000UL;
						if ((y & NAN_MASK64) == INFINITY_MASK64)
							coefficient_y = y & SINFINITY_MASK64;
						valid_y = 0; // NaN or Infinity
					}
					else
					{
						// coefficient
						BID_UINT64 coeff = (y & LARGE_COEFF_MASK64) | LARGE_COEFF_HIGH_BIT64;
						// check for non-canonical values
						if (coeff >= 10000000000000000UL)
							coeff = 0;
						coefficient_y = coeff;
						// get exponent
						exponent_y = (int)((y >> EXPONENT_SHIFT_LARGE64) & EXPONENT_MASK64);
						valid_y = coeff;
					}
				}
			}

			// unpack arguments, check for NaN or Infinity
			if (valid_x == 0)
			{
				// x is Inf. or NaN
#if BID_SET_STATUS_FLAGS
				if ((y & SNAN_MASK64) == SNAN_MASK64)   // y is sNaN
					__set_status_flags(ref pfpsf, BID_INVALID_EXCEPTION);
#endif

				// test if x is NaN
				if ((x & NAN_MASK64) == NAN_MASK64)
				{
#if BID_SET_STATUS_FLAGS
					if ((x & SNAN_MASK64) == SNAN_MASK64)   // sNaN
						__set_status_flags(ref pfpsf, BID_INVALID_EXCEPTION);
#endif
					return coefficient_x & QUIET_MASK64;
				}
				// x is Infinity?
				if ((x & INFINITY_MASK64) == INFINITY_MASK64)
				{
					// check if y is Inf or NaN
					if ((y & INFINITY_MASK64) == INFINITY_MASK64)
					{
						// y==Inf, return NaN
						if ((y & NAN_MASK64) == INFINITY_MASK64)
						{
							// Inf/Inf
#if BID_SET_STATUS_FLAGS
							__set_status_flags(ref pfpsf, BID_INVALID_EXCEPTION);
#endif
							return NAN_MASK64;
						}
					}
					else
					{
						// otherwise return +/-Inf
						return ((x ^ y) & 0x8000000000000000UL) | INFINITY_MASK64;
					}
				}
				// x==0
				if (((y & INFINITY_MASK64) != INFINITY_MASK64) && coefficient_y == 0)
				{
					// y==0 , return NaN
#if BID_SET_STATUS_FLAGS
					__set_status_flags(ref pfpsf, BID_INVALID_EXCEPTION);
#endif
					return NAN_MASK64;
				}
				if (((y & INFINITY_MASK64) != INFINITY_MASK64))
				{
					if ((y & SPECIAL_ENCODING_MASK64) == SPECIAL_ENCODING_MASK64)
						exponent_y = (int)(((BID_UINT32)(y >> 51)) & 0x3ff);
					else
						exponent_y = (int)(((BID_UINT32)(y >> 53)) & 0x3ff);
					sign_y = y & 0x8000000000000000UL;

					exponent_x = exponent_x - exponent_y + DECIMAL_EXPONENT_BIAS;
					if (exponent_x > DECIMAL_MAX_EXPON_64)
						exponent_x = DECIMAL_MAX_EXPON_64;
					else if (exponent_x < 0)
						exponent_x = 0;
					return (sign_x ^ sign_y) | (((BID_UINT64)exponent_x) << 53);
				}
			}

			if (valid_y == 0)
			{
				// y is Inf. or NaN

				// test if y is NaN
				if ((y & NAN_MASK64) == NAN_MASK64)
				{
#if BID_SET_STATUS_FLAGS
					if ((y & SNAN_MASK64) == SNAN_MASK64)   // sNaN
						__set_status_flags(ref pfpsf, BID_INVALID_EXCEPTION);
#endif
					return coefficient_y & QUIET_MASK64;
				}
				// y is Infinity?
				if ((y & INFINITY_MASK64) == INFINITY_MASK64)
				{
					// return +/-0
					return (x ^ y) & 0x8000000000000000UL;
				}
				// y is 0
#if BID_SET_STATUS_FLAGS
				__set_status_flags(ref pfpsf, BID_ZERO_DIVIDE_EXCEPTION);
#endif
				return (sign_x ^ sign_y) | INFINITY_MASK64;
			}
#if UNCHANGED_BINARY_STATUS_FLAGS
  // (void) fegetexceptflag (&binaryflags, BID_FE_ALL_FLAGS);
#endif
			diff_expon = exponent_x - exponent_y + DECIMAL_EXPONENT_BIAS;

			if (coefficient_x < coefficient_y)
			{
				// get number of decimal digits for c_x, c_y

				//--- get number of bits in the coefficients of x and y ---
				tempx_i = floatToBits((float)coefficient_x);
				tempy_i = floatToBits((float)coefficient_y);
				bin_index = (int)((tempy_i - tempx_i) >> 23);

				A = coefficient_x * bid_power10_index_binexp[bin_index];
				B = coefficient_y;

				temp_b_i = doubleToBits((double)B);

				// compare A, B
				DU = (A - B) >> 63;
				ed1 = 15 + (int)DU;
				ed2 = bid_estimate_decimal_digits[bin_index] + ed1;
				T = bid_power10_table_128[ed1].w0;
				//__mul_64x64_to_128(out CA, A, T);
				{
					BID_UINT64 CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
					CXH = A >> 32;
					CXL = (BID_UINT32)A;
					CYH = T >> 32;
					CYL = (BID_UINT32)T;

					PM = CXH * CYL;
					PH = CXH * CYH;
					PL = CXL * CYL;
					PM2 = CXL * CYH;
					PH += (PM >> 32);
					PM = (BID_UINT64)((BID_UINT32)PM) + PM2 + (PL >> 32);

					CA.w1 = PH + (PM >> 32);
					CA.w0 = (PM << 32) + (BID_UINT32)PL;
				}

				Q = 0;
				diff_expon = diff_expon - ed2;

				// adjust double precision db, to ensure that later A/B - (int)(da/db) > -1
				if (coefficient_y < 0x0020000000000000UL)
				{
					temp_b_i += 1;
					db = bitsToDouble(temp_b_i);
				}
				else
					db = (double)(B + 2 + (B & 1));
			}
			else
			{
				// get c_x/c_y

				//  set last bit before conversion to DP
				A2 = coefficient_x | 1;
				da = (double)A2;

				db = (double)coefficient_y;

				double tempq_d = da / db;
				Q = (BID_UINT64)tempq_d;
				tempq_i = doubleToBits(tempq_d);

				R = coefficient_x - coefficient_y * Q;

				// will use to get number of dec. digits of Q
				bin_expon_cx = (int)((tempq_i >> 52) - 0x3ff);

				// R<0 ?
				D = ((BID_SINT64)R) >> 63;
				Q += (BID_UINT64)D;
				R += coefficient_y & (BID_UINT64)D;

				// exact result ?
				if (((BID_SINT64)R) <= 0)
				{
					// can have R==-1 for coeff_y==1
					res = get_BID64(sign_x ^ sign_y, diff_expon, (Q + R)
#if !IEEE_ROUND_NEAREST
						, rnd_mode
#endif
#if BID_SET_STATUS_FLAGS
						, ref pfpsf
#endif
						);
#if UNCHANGED_BINARY_STATUS_FLAGS
      // (void) fesetexceptflag (&binaryflags, BID_FE_ALL_FLAGS);
#endif
					return res;
				}
				// get decimal digits of Q
				DU = bid_power10_index_binexp[bin_expon_cx] - Q - 1;
				DU >>= 63;

				ed2 = 16 - bid_estimate_decimal_digits[bin_expon_cx] - (int)DU;

				T = bid_power10_table_128[ed2].w0;

				//__mul_64x64_to_128(out CA, R, T);
				{
					BID_UINT64 CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
					CXH = R >> 32;
					CXL = (BID_UINT32)R;
					CYH = T >> 32;
					CYL = (BID_UINT32)T;

					PM = CXH * CYL;
					PH = CXH * CYH;
					PL = CXL * CYL;
					PM2 = CXL * CYH;
					PH += (PM >> 32);
					PM = (BID_UINT64)((BID_UINT32)PM) + PM2 + (PL >> 32);

					CA.w1 = PH + (PM >> 32);
					CA.w0 = (PM << 32) + (BID_UINT32)PL;
				}

				B = coefficient_y;

				Q *= bid_power10_table_128[ed2].w0;
				diff_expon -= ed2;
			}

			if (CA.w1 == 0)
			{
				Q2 = CA.w0 / B;
				B2 = B + B;
				B4 = B2 + B2;
				R = CA.w0 - Q2 * B;
				Q += Q2;
			}
			else
			{
				// 2^64
				t_scale_d = bitsToDouble(0x43f0000000000000UL);
				// convert CA to DP
				da_h = CA.w1;
				da_l = CA.w0;
				da = da_h * t_scale_d + da_l;

				// quotient
				dq = da / db;
				Q2 = (BID_UINT64)dq;

				// get w[0] remainder
				R = CA.w0 - Q2 * B;

				// R<0 ?
				D = ((BID_SINT64)R) >> 63;
				Q2 += (BID_UINT64)D;
				R += B & (BID_UINT64)D;

				// now R<6*B

				// quick divide

				// 4*B
				B2 = B + B;
				B4 = B2 + B2;

				R = R - B4;
				// R<0 ?
				D = ((BID_SINT64)R) >> 63;
				// restore R if negative
				R += B4 & (BID_UINT64)D;
				Q2 += (BID_UINT64)(~D) & 4;

				R = R - B2;
				// R<0 ?
				D = ((BID_SINT64)R) >> 63;
				// restore R if negative
				R += B2 & (BID_UINT64)D;
				Q2 += (BID_UINT64)(~D) & 2;

				R = R - B;
				// R<0 ?
				D = ((BID_SINT64)R) >> 63;
				// restore R if negative
				R += B & (BID_UINT64)D;
				Q2 += (BID_UINT64)(~D) & 1;

				Q += Q2;
			}

#if BID_SET_STATUS_FLAGS
			if (R != 0)
			{
				// set status flags
				__set_status_flags(ref pfpsf, BID_INEXACT_EXCEPTION);
			}
#if !LEAVE_TRAILING_ZEROS
			else
#endif
#else
#if !LEAVE_TRAILING_ZEROS
			if (R == 0)
#endif
#endif
#if !LEAVE_TRAILING_ZEROS
			{
				// eliminate trailing zeros

				// check whether CX, CY are short
				if ((coefficient_x <= 1024) && (coefficient_y <= 1024))
				{
					i = (int)coefficient_y - 1;
					j = (int)coefficient_x - 1;
					// difference in powers of 2 bid_factors for Y and X
					nzeros = ed2 - bid_factors[i, 0] + bid_factors[j, 0];
					// difference in powers of 5 bid_factors
					d5 = ed2 - bid_factors[i, 1] + bid_factors[j, 1];
					if (d5 < nzeros)
						nzeros = d5;

					//__mul_64x64_to_128(out CT, Q, bid_reciprocals10_64[nzeros]);
					{
						BID_UINT64 CY = bid_reciprocals10_64[nzeros];
						BID_UINT64 CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
						CXH = Q >> 32;
						CXL = (BID_UINT32)Q;
						CYH = CY >> 32;
						CYL = (BID_UINT32)CY;

						PM = CXH * CYL;
						PH = CXH * CYH;
						PL = CXL * CYL;
						PM2 = CXL * CYH;
						PH += (PM >> 32);
						PM = (BID_UINT64)((BID_UINT32)PM) + PM2 + (PL >> 32);

						CT.w1 = PH + (PM >> 32);
						CT.w0 = (PM << 32) + (BID_UINT32)PL;
					}

					// now get P/10^extra_digits: shift C64 right by M[extra_digits]-128
					amount = bid_short_recip_scale[nzeros];
					Q = CT.w1 >> amount;

					diff_expon += nzeros;
				}
				else
				{
					tdigit[0] = (BID_UINT32)(Q & 0x3ffffff);
					tdigit[1] = 0;
					QX = Q >> 26;
					QX32 = (BID_UINT32)QX;
					nzeros = 0;

					for (j = 0; QX32 != 0; j++, QX32 >>= 7)
					{
						k = (int)(QX32 & 127);
						tdigit[0] += bid_convert_table[j, k, 0];
						tdigit[1] += bid_convert_table[j, k, 1];
						if (tdigit[0] >= 100000000)
						{
							tdigit[0] -= 100000000;
							tdigit[1]++;
						}
					}

					digit = tdigit[0];
					if (digit == 0 && tdigit[1] == 0)
						nzeros += 16;
					else
					{
						if (digit == 0)
						{
							nzeros += 8;
							digit = tdigit[1];
						}
						// decompose digit
						PD = (BID_UINT64)digit * 0x068DB8BBUL;
						digit_h = (BID_UINT32)(PD >> 40);
						digit_low = digit - digit_h * 10000;

						if (digit_low == 0)
							nzeros += 4;
						else
							digit_h = digit_low;

						if ((digit_h & 1) == 0)
							nzeros += (int)(3 & (BID_UINT32)(bid_packed_10000_zeros[digit_h >> 3] >> (int)(digit_h & 7)));
					}

					if (nzeros != 0)
					{
						//__mul_64x64_to_128(out CT, Q, bid_reciprocals10_64[nzeros]);
						{
							BID_UINT64 CY = bid_reciprocals10_64[nzeros];
							BID_UINT64 CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
							CXH = Q >> 32;
							CXL = (BID_UINT32)Q;
							CYH = CY >> 32;
							CYL = (BID_UINT32)CY;

							PM = CXH * CYL;
							PH = CXH * CYH;
							PL = CXL * CYL;
							PM2 = CXL * CYH;
							PH += (PM >> 32);
							PM = (BID_UINT64)((BID_UINT32)PM) + PM2 + (PL >> 32);

							CT.w1 = PH + (PM >> 32);
							CT.w0 = (PM << 32) + (BID_UINT32)PL;
						}

						// now get P/10^extra_digits: shift C64 right by M[extra_digits]-128
						amount = bid_short_recip_scale[nzeros];
						Q = CT.w1 >> amount;
					}

					diff_expon += nzeros;
				}

				if (diff_expon >= 0)
				{
					res = fast_get_BID64_check_OF(sign_x ^ sign_y, diff_expon, Q
#if !IEEE_ROUND_NEAREST
						, rnd_mode
#endif
#if BID_SET_STATUS_FLAGS
						, ref pfpsf
#endif
						);
#if UNCHANGED_BINARY_STATUS_FLAGS
      // (void) fesetexceptflag (&binaryflags, BID_FE_ALL_FLAGS);
#endif
					return res;
				}
			}
#endif

			if (diff_expon >= 0)
			{
#if IEEE_ROUND_NEAREST
				// round to nearest code
				// R*10
				R += R;
				R = (R << 2) + R;
				B5 = B4 + B;

				// compare 10*R to 5*B
				R = B5 - R;
				// correction for (R==0 && (Q&1))
				R -= (Q & 1);
				// R<0 ?
				D = (BID_SINT64)(((BID_UINT64)R) >> 63);
				Q += (BID_UINT64)D;
#else
#if IEEE_ROUND_NEAREST_TIES_AWAY
				// round to nearest code
				// R*10
				R += R;
				R = (R << 2) + R;
				B5 = B4 + B;

				// compare 10*R to 5*B
				R = B5 - R;
				// correction for (R==0 && (Q&1))
				R -= (Q & 1);
				// R<0 ?
				D = (BID_SINT64)(((BID_UINT64) R) >> 63);
				Q += (BID_UINT64)D;
#else
				rmode = rnd_mode;
				if ((sign_x ^ sign_y) != 0 && (unsigned)(rmode - 1) < 2)
					rmode = 3 - rmode;
				switch (rmode)
				{
					case 0: // round to nearest code
					case BID_ROUNDING_TIES_AWAY:
						// R*10
						R += R;
						R = (R << 2) + R;
						B5 = B4 + B;
						// compare 10*R to 5*B
						R = B5 - R;
						// correction for (R==0 && (Q&1))
						R -= (Q | (BID_UINT64)(rmode >> 2)) & 1;
						// R<0 ?
						D = (BID_SINT64)(((BID_UINT64)R) >> 63);
						Q += (BID_UINT64)D;
						break;
					case BID_ROUNDING_DOWN:
					case BID_ROUNDING_TO_ZERO:
						break;
					default:    // rounding up
						Q++;
						break;
				}
#endif
#endif

				res = fast_get_BID64_check_OF(sign_x ^ sign_y, diff_expon, Q
#if !IEEE_ROUND_NEAREST
						, rnd_mode
#endif
#if BID_SET_STATUS_FLAGS
						, ref pfpsf
#endif
					);
#if UNCHANGED_BINARY_STATUS_FLAGS
    // (void) fesetexceptflag (&binaryflags, BID_FE_ALL_FLAGS);
#endif
				return res;
			}
			else
			{
				// UF occurs

#if BID_SET_STATUS_FLAGS
				if ((diff_expon + 16 < 0))
				{
					// set status flags
					__set_status_flags(ref pfpsf, BID_INEXACT_EXCEPTION);
				}
#endif
#if !IEEE_ROUND_NEAREST
				rmode = rnd_mode;
#endif
				res = get_BID64_UF(sign_x ^ sign_y, diff_expon, Q, R
#if !IEEE_ROUND_NEAREST
						, rnd_mode
#endif
#if BID_SET_STATUS_FLAGS
						, ref pfpsf
#endif
					);
#if UNCHANGED_BINARY_STATUS_FLAGS
    // (void) fesetexceptflag (&binaryflags, BID_FE_ALL_FLAGS);
#endif
				return res;
			}
		}
	}
}
