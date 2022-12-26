using static EPAM.Deltix.DFP.BidDecimalData;
using static EPAM.Deltix.DFP.BidInternal;
using BID_UINT64 = System.UInt64;
using BID_SINT64 = System.Int64;
using BID_UINT32 = System.UInt32;
using _IDEC_flags = System.UInt32;
using int_double = System.UInt64;
using unsigned = System.UInt32;
using System.Runtime.CompilerServices;

namespace EPAM.Deltix.DFP
{
	internal static class Bid64Mul
	{
		/// <summary>
		///  Algorithm description:
		///
		///  if(number_digits(coefficient_x)+number_digits(coefficient_y) guaranteed
		///       below 16)
		///      return get_BID64(sign_x^sign_y, exponent_x + exponent_y - dec_bias,
		///                     coefficient_x*coefficient_y)
		///  else
		///      get long product: coefficient_x*coefficient_y
		///      determine number of digits to round off (extra_digits)
		///      rounding is performed as a 128x128-bit multiplication by
		///         2^M[extra_digits]/10^extra_digits, followed by a shift
		///         M[extra_digits] is sufficiently large for required accuracy
		/// </summary>
#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe BID_UINT64 bid64_mul(BID_UINT64 x, BID_UINT64 y
#if !IEEE_ROUND_NEAREST
			, int rnd_mode
#endif
#if BID_SET_STATUS_FLAGS
			, ref _IDEC_flags pfpsf
#endif
			)
		{
			BID_UINT128 P, C128, Q_high, Q_low;
#if BID_SET_STATUS_FLAGS
			BID_UINT128 Stemp;
#endif
#if DECIMAL_TINY_DETECTION_AFTER_ROUNDING
			BID_UINT128 PU;
#endif
			BID_UINT64 sign_x, sign_y, coefficient_x, coefficient_y;
			BID_UINT64 C64, remainder_h, res;
#if BID_SET_STATUS_FLAGS
			BID_UINT64 carry, CY;
#endif
			BID_UINT64 valid_x, valid_y;
			int_double tempx_i, tempy_i;
			int extra_digits, exponent_x, exponent_y, bin_expon_cx, bin_expon_cy, bin_expon_product;
			int rmode, digits_p, bp, amount, amount2, final_exponent, round_up;
#if BID_SET_STATUS_FLAGS
			unsigned status, uf_status;
#endif

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
#if BID_SET_STATUS_FLAGS
				if ((y & SNAN_MASK64) == SNAN_MASK64)   // y is sNaN
					__set_status_flags(ref pfpsf, BID_INVALID_EXCEPTION);
#endif
				// x is Inf. or NaN

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
					// check if y is 0
					if (((y & INFINITY_MASK64) != INFINITY_MASK64) && coefficient_y == 0)
					{
#if BID_SET_STATUS_FLAGS
						__set_status_flags(ref pfpsf, BID_INVALID_EXCEPTION);
#endif
						// y==0 , return NaN
						return NAN_MASK64;
					}
					// check if y is NaN
					if ((y & NAN_MASK64) == NAN_MASK64)
						// y==NaN , return NaN
						return coefficient_y & QUIET_MASK64;
					// otherwise return +/-Inf
					return ((x ^ y) & 0x8000000000000000UL) | INFINITY_MASK64;
				}
				// x is 0
				if (((y & INFINITY_MASK64) != INFINITY_MASK64))
				{
					if ((y & SPECIAL_ENCODING_MASK64) == SPECIAL_ENCODING_MASK64)
						exponent_y = (int)(((BID_UINT32)(y >> 51)) & 0x3ff);
					else
						exponent_y = (int)(((BID_UINT32)(y >> 53)) & 0x3ff);
					sign_y = y & 0x8000000000000000UL;

					exponent_x += exponent_y - DECIMAL_EXPONENT_BIAS;
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
					// check if x is 0
					if (coefficient_x == 0)
					{
#if BID_SET_STATUS_FLAGS
						__set_status_flags(ref pfpsf, BID_INVALID_EXCEPTION);
#endif
						// x==0, return NaN
						return NAN_MASK64;
					}
					// otherwise return +/-Inf
					return ((x ^ y) & 0x8000000000000000UL) | INFINITY_MASK64;
				}
				// y is 0
				exponent_x += exponent_y - DECIMAL_EXPONENT_BIAS;
				if (exponent_x > DECIMAL_MAX_EXPON_64)
					exponent_x = DECIMAL_MAX_EXPON_64;
				else if (exponent_x < 0)
					exponent_x = 0;
				return (sign_x ^ sign_y) | (((BID_UINT64)exponent_x) << 53);
			}
			//--- get number of bits in the coefficients of x and y ---
			// version 2 (original)
			tempx_i = doubleToBits((double)coefficient_x);
			bin_expon_cx = (int)((tempx_i & MASK_BINARY_EXPONENT) >> 52);
			tempy_i = doubleToBits((double)coefficient_y);
			bin_expon_cy = (int)((tempy_i & MASK_BINARY_EXPONENT) >> 52);

			// magnitude estimate for coefficient_x*coefficient_y is
			//        2^(unbiased_bin_expon_cx + unbiased_bin_expon_cx)
			bin_expon_product = bin_expon_cx + bin_expon_cy;

			// check if coefficient_x*coefficient_y<2^(10*k+3)
			// equivalent to unbiased_bin_expon_cx + unbiased_bin_expon_cx < 10*k+1
			if (bin_expon_product < UPPER_EXPON_LIMIT + 2 * BINARY_EXPONENT_BIAS)
			{
				//  easy multiply
				C64 = coefficient_x * coefficient_y;

				return get_BID64_small_mantissa(sign_x ^ sign_y, exponent_x + exponent_y - DECIMAL_EXPONENT_BIAS, C64
#if !IEEE_ROUND_NEAREST
					, rnd_mode
#endif
#if BID_SET_STATUS_FLAGS
					, ref pfpsf
#endif
					);
			}
			else
			{
#if BID_SET_STATUS_FLAGS
				uf_status = 0;
#endif
				// get 128-bit product: coefficient_x*coefficient_y
				//__mul_64x64_to_128(out P, coefficient_x, coefficient_y);
				{
					BID_UINT64 CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
					CXH = coefficient_x >> 32;
					CXL = (BID_UINT32)coefficient_x;
					CYH = coefficient_y >> 32;
					CYL = (BID_UINT32)coefficient_y;

					PM = CXH * CYL;
					PH = CXH * CYH;
					PL = CXL * CYL;
					PM2 = CXL * CYH;
					PH += (PM >> 32);
					PM = (BID_UINT64)((BID_UINT32)PM) + PM2 + (PL >> 32);

					P.w1 = PH + (PM >> 32);
					P.w0 = (PM << 32) + (BID_UINT32)PL;
				}

				// tighten binary range of P:  leading bit is 2^bp
				// unbiased_bin_expon_product <= bp <= unbiased_bin_expon_product+1
				bin_expon_product -= 2 * BINARY_EXPONENT_BIAS;

				//__tight_bin_range_128(out bp, P, bin_expon_product);
				{
					BID_UINT64 M = 1;
					bp = bin_expon_product;
					if (bp < 63)
					{
						M <<= bp + 1;
						if (P.w0 >= M) bp++;
					}
					else if (bp > 64)
					{
						M <<= bp + 1 - 64;
						if ((P.w1 > M) || (P.w1 == M && P.w0 != 0))
							bp++;
					}
					else if (P.w1 != 0)
						bp++;
				}

				// get number of decimal digits in the product
				digits_p = bid_estimate_decimal_digits[bp];
				if (!(__unsigned_compare_gt_128(bid_power10_table_128[digits_p], P)))
					digits_p++; // if bid_power10_table_128[digits_p] <= P

				// determine number of decimal digits to be rounded out
				extra_digits = digits_p - MAX_FORMAT_DIGITS;
				final_exponent = exponent_x + exponent_y + extra_digits - DECIMAL_EXPONENT_BIAS;

#if !IEEE_ROUND_NEAREST_TIES_AWAY
#if !IEEE_ROUND_NEAREST
				rmode = (int)rnd_mode;
				if ((sign_x ^ sign_y) != 0 && (unsigned)(rmode - 1) < 2)
					rmode = 3 - rmode;
#else
				rmode = 0;
#endif
#else
	rmode = 0;
#endif

				round_up = 0;
				if (((unsigned)final_exponent) >= 3 * 256)
				{
					if (final_exponent < 0)
					{
						// underflow
						if (final_exponent + 16 < 0)
						{
							res = sign_x ^ sign_y;
#if BID_SET_STATUS_FLAGS
							__set_status_flags(ref pfpsf, BID_UNDERFLOW_EXCEPTION | BID_INEXACT_EXCEPTION);
#endif
							if (rmode == BID_ROUNDING_UP)
								res |= 1;
							return res;
						}

#if BID_SET_STATUS_FLAGS
						uf_status = BID_UNDERFLOW_EXCEPTION;
#endif
#if DECIMAL_TINY_DETECTION_AFTER_ROUNDING
						if (final_exponent == -1)
						{
							//__add_128_64(out PU, P, bid_round_const_table[rmode, extra_digits]);
							{
								BID_UINT64 B64 = bid_round_const_table[rmode, extra_digits];
								BID_UINT64 R64H = P.w1;
								PU.w0 = B64 + P.w0;
								if (PU.w0 < B64)
									R64H++;
								PU.w1 = R64H;
							}
							if (__unsigned_compare_ge_128
								(PU, bid_power10_table_128[extra_digits + 16]))
								uf_status = 0;
						}
#endif
						extra_digits -= final_exponent;
						final_exponent = 0;

						if (extra_digits > 17)
						{
							//__mul_128x128_full(out Q_high, out Q_low, P, bid_reciprocals10_128[16]);
							{
								BID_UINT128 B = bid_reciprocals10_128[16];

								BID_UINT128 ALBL, ALBH, AHBL, AHBH, QM, QM2;

								//__mul_64x64_to_128(out ALBH, P.w0, B.w1);
								{
									BID_UINT64 CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
									CXH = P.w0 >> 32;
									CXL = (BID_UINT32)P.w0;
									CYH = B.w1 >> 32;
									CYL = (BID_UINT32)B.w1;

									PM = CXH * CYL;
									PH = CXH * CYH;
									PL = CXL * CYL;
									PM2 = CXL * CYH;
									PH += (PM >> 32);
									PM = (BID_UINT64)((BID_UINT32)PM) + PM2 + (PL >> 32);

									ALBH.w1 = PH + (PM >> 32);
									ALBH.w0 = (PM << 32) + (BID_UINT32)PL;
								}

								//__mul_64x64_to_128(out AHBL, B.w0, P.w1);
								{
									BID_UINT64 CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
									CXH = B.w0 >> 32;
									CXL = (BID_UINT32)B.w0;
									CYH = P.w1 >> 32;
									CYL = (BID_UINT32)P.w1;

									PM = CXH * CYL;
									PH = CXH * CYH;
									PL = CXL * CYL;
									PM2 = CXL * CYH;
									PH += (PM >> 32);
									PM = (BID_UINT64)((BID_UINT32)PM) + PM2 + (PL >> 32);

									AHBL.w1 = PH + (PM >> 32);
									AHBL.w0 = (PM << 32) + (BID_UINT32)PL;
								}

								//__mul_64x64_to_128(out ALBL, P.w0, B.w0);
								{
									BID_UINT64 CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
									CXH = P.w0 >> 32;
									CXL = (BID_UINT32)P.w0;
									CYH = B.w0 >> 32;
									CYL = (BID_UINT32)B.w0;

									PM = CXH * CYL;
									PH = CXH * CYH;
									PL = CXL * CYL;
									PM2 = CXL * CYH;
									PH += (PM >> 32);
									PM = (BID_UINT64)((BID_UINT32)PM) + PM2 + (PL >> 32);

									ALBL.w1 = PH + (PM >> 32);
									ALBL.w0 = (PM << 32) + (BID_UINT32)PL;
								}

								//__mul_64x64_to_128(out AHBH, P.w1, B.w1);
								{
									BID_UINT64 CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
									CXH = P.w1 >> 32;
									CXL = (BID_UINT32)P.w1;
									CYH = B.w1 >> 32;
									CYL = (BID_UINT32)B.w1;

									PM = CXH * CYL;
									PH = CXH * CYH;
									PL = CXL * CYL;
									PM2 = CXL * CYH;
									PH += (PM >> 32);
									PM = (BID_UINT64)((BID_UINT32)PM) + PM2 + (PL >> 32);

									AHBH.w1 = PH + (PM >> 32);
									AHBH.w0 = (PM << 32) + (BID_UINT32)PL;
								}


								//__add_128_128(out QM, ALBH, AHBL);
								{
									BID_UINT128 Q128;
									Q128.w1 = ALBH.w1 + AHBL.w1;
									Q128.w0 = AHBL.w0 + ALBH.w0;
									if (Q128.w0 < AHBL.w0)
										Q128.w1++;
									QM.w1 = Q128.w1;
									QM.w0 = Q128.w0;
								}

								Q_low.w0 = ALBL.w0;

								//__add_128_64(out QM2, QM, ALBL.w1);
								{
									BID_UINT64 R64H = QM.w1;
									QM2.w0 = ALBL.w1 + QM.w0;
									if (QM2.w0 < ALBL.w1)
										R64H++;
									QM2.w1 = R64H;
								}

								//__add_128_64(out Q_high, AHBH, QM2.w1);
								{
									BID_UINT64 R64H = AHBH.w1;
									Q_high.w0 = QM2.w1 + AHBH.w0;
									if (Q_high.w0 < QM2.w1)
										R64H++;
									Q_high.w1 = R64H;
								}

								Q_low.w1 = QM2.w0;
							}

							amount = bid_recip_scale[16];
							//__shr_128(out P, Q_high, amount);
							{
								P.w0 = Q_high.w0 >> amount;
								P.w0 |= Q_high.w1 << (64 - amount);
								P.w1 = Q_high.w1 >> amount;
							}


							// get sticky bits
							amount2 = 64 - amount;
							remainder_h = 0;
							remainder_h--;
							remainder_h >>= amount2;
							remainder_h = remainder_h & Q_high.w0;

							extra_digits -= 16;
							if (remainder_h != 0 || (Q_low.w1 > bid_reciprocals10_128[16].w1
										|| (Q_low.w1 == bid_reciprocals10_128[16].w1
										&& Q_low.w0 >= bid_reciprocals10_128[16].w0)))
							{
								round_up = 1;
#if BID_SET_STATUS_FLAGS
								__set_status_flags(ref pfpsf, BID_UNDERFLOW_EXCEPTION | BID_INEXACT_EXCEPTION);
#endif
								P.w0 = (P.w0 << 3) + (P.w0 << 1);
								P.w0 |= 1;
								extra_digits++;
							}
						}
					}
					else
					{
						return fast_get_BID64_check_OF(sign_x ^ sign_y, final_exponent, 1000000000000000UL
#if !IEEE_ROUND_NEAREST
							, rnd_mode
#endif
#if BID_SET_STATUS_FLAGS
							, ref pfpsf
#endif
							);
					}
				}


				if (extra_digits > 0)
				{
					// will divide by 10^(digits_p - 16)

					// add a constant to P, depending on rounding mode
					// 0.5*10^(digits_p - 16) for round-to-nearest
					//__add_128_64(out P, P, bid_round_const_table[rmode, extra_digits]);
					{
						BID_UINT64 B64 = bid_round_const_table[rmode, extra_digits];
						BID_UINT64 R64H = P.w1;
						P.w0 = B64 + P.w0;
						if (P.w0 < B64)
							R64H++;
						P.w1 = R64H;
					}

					// get P*(2^M[extra_digits])/10^extra_digits
					//__mul_128x128_full(out Q_high, out Q_low, P, bid_reciprocals10_128[extra_digits]);
					{
						BID_UINT128 B = bid_reciprocals10_128[extra_digits];
						BID_UINT128 ALBL, ALBH, AHBL, AHBH, QM, QM2;

						//__mul_64x64_to_128(out ALBH, P.w0, B.w1);
						{
							BID_UINT64 CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
							CXH = P.w0 >> 32;
							CXL = (BID_UINT32)P.w0;
							CYH = B.w1 >> 32;
							CYL = (BID_UINT32)B.w1;

							PM = CXH * CYL;
							PH = CXH * CYH;
							PL = CXL * CYL;
							PM2 = CXL * CYH;
							PH += (PM >> 32);
							PM = (BID_UINT64)((BID_UINT32)PM) + PM2 + (PL >> 32);

							ALBH.w1 = PH + (PM >> 32);
							ALBH.w0 = (PM << 32) + (BID_UINT32)PL;
						}

						//__mul_64x64_to_128(out AHBL, B.w0, P.w1);
						{
							BID_UINT64 CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
							CXH = B.w0 >> 32;
							CXL = (BID_UINT32)B.w0;
							CYH = P.w1 >> 32;
							CYL = (BID_UINT32)P.w1;

							PM = CXH * CYL;
							PH = CXH * CYH;
							PL = CXL * CYL;
							PM2 = CXL * CYH;
							PH += (PM >> 32);
							PM = (BID_UINT64)((BID_UINT32)PM) + PM2 + (PL >> 32);

							AHBL.w1 = PH + (PM >> 32);
							AHBL.w0 = (PM << 32) + (BID_UINT32)PL;
						}

						//__mul_64x64_to_128(out ALBL, P.w0, B.w0);
						{
							BID_UINT64 CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
							CXH = P.w0 >> 32;
							CXL = (BID_UINT32)P.w0;
							CYH = B.w0 >> 32;
							CYL = (BID_UINT32)B.w0;

							PM = CXH * CYL;
							PH = CXH * CYH;
							PL = CXL * CYL;
							PM2 = CXL * CYH;
							PH += (PM >> 32);
							PM = (BID_UINT64)((BID_UINT32)PM) + PM2 + (PL >> 32);

							ALBL.w1 = PH + (PM >> 32);
							ALBL.w0 = (PM << 32) + (BID_UINT32)PL;
						}

						//__mul_64x64_to_128(out AHBH, P.w1, B.w1);
						{
							BID_UINT64 CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
							CXH = P.w1 >> 32;
							CXL = (BID_UINT32)P.w1;
							CYH = B.w1 >> 32;
							CYL = (BID_UINT32)B.w1;

							PM = CXH * CYL;
							PH = CXH * CYH;
							PL = CXL * CYL;
							PM2 = CXL * CYH;
							PH += (PM >> 32);
							PM = (BID_UINT64)((BID_UINT32)PM) + PM2 + (PL >> 32);

							AHBH.w1 = PH + (PM >> 32);
							AHBH.w0 = (PM << 32) + (BID_UINT32)PL;
						}

						//__add_128_128(out QM, ALBH, AHBL);
						{
							BID_UINT128 Q128;
							Q128.w1 = ALBH.w1 + AHBL.w1;
							Q128.w0 = AHBL.w0 + ALBH.w0;
							if (Q128.w0 < AHBL.w0)
								Q128.w1++;
							QM.w1 = Q128.w1;
							QM.w0 = Q128.w0;
						}

						Q_low.w0 = ALBL.w0;

						//__add_128_64(out QM2, QM, ALBL.w1);
						{
							BID_UINT64 R64H = QM.w1;
							QM2.w0 = ALBL.w1 + QM.w0;
							if (QM2.w0 < ALBL.w1)
								R64H++;
							QM2.w1 = R64H;
						}

						//__add_128_64(out Q_high, AHBH, QM2.w1);
						{
							BID_UINT64 R64H = AHBH.w1;
							Q_high.w0 = QM2.w1 + AHBH.w0;
							if (Q_high.w0 < QM2.w1)
								R64H++;
							Q_high.w1 = R64H;
						}

						Q_low.w1 = QM2.w0;
					}

					// now get P/10^extra_digits: shift Q_high right by M[extra_digits]-128
					amount = bid_recip_scale[extra_digits];

					//__shr_128(out C128, Q_high, amount);
					{
						C128.w0 = Q_high.w0 >> amount;
						C128.w0 |= Q_high.w1 << (64 - amount);
						C128.w1 = Q_high.w1 >> amount;
					}

					C64 = C128.w0;

#if !IEEE_ROUND_NEAREST_TIES_AWAY
#if !IEEE_ROUND_NEAREST
					if (rmode == 0) //BID_ROUNDING_TO_NEAREST
#endif
					if ((C64 & 1) != 0 && round_up == 0)
					{
						// check whether fractional part of initial_P/10^extra_digits
						// is exactly .5
						// this is the same as fractional part of
						// (initial_P + 0.5*10^extra_digits)/10^extra_digits is exactly zero

						// get remainder
						remainder_h = Q_high.w0 << (64 - amount);

						// test whether fractional part is 0
						if (remainder_h == 0
							&& (Q_low.w1 < bid_reciprocals10_128[extra_digits].w1
							|| (Q_low.w1 == bid_reciprocals10_128[extra_digits].w1
								&& Q_low.w0 < bid_reciprocals10_128[extra_digits].w0)))
						{
							C64--;
						}
					}
#endif

#if BID_SET_STATUS_FLAGS
					status = BID_INEXACT_EXCEPTION | uf_status;

					// get remainder
					remainder_h = Q_high.w0 << (64 - amount);

					switch (rmode)
					{
						case BID_ROUNDING_TO_NEAREST:
						case BID_ROUNDING_TIES_AWAY:
							// test whether fractional part is 0
							if (remainder_h == 0x8000000000000000UL
								&& (Q_low.w1 < bid_reciprocals10_128[extra_digits].w1
								|| (Q_low.w1 == bid_reciprocals10_128[extra_digits].w1
									&& Q_low.w0 < bid_reciprocals10_128[extra_digits].w0)))
								status = BID_EXACT_STATUS;
							break;
						case BID_ROUNDING_DOWN:
						case BID_ROUNDING_TO_ZERO:
							if (remainder_h == 0
								&& (Q_low.w1 < bid_reciprocals10_128[extra_digits].w1
								|| (Q_low.w1 == bid_reciprocals10_128[extra_digits].w1
									&& Q_low.w0 < bid_reciprocals10_128[extra_digits].w0)))
								status = BID_EXACT_STATUS;
							break;
						default:
							// round up
							//__add_carry_out(out Stemp.w0, out CY, Q_low.w0, bid_reciprocals10_128[extra_digits].w0);
							{
								Stemp.w0 = Q_low.w0 + bid_reciprocals10_128[extra_digits].w0;
								CY = (Stemp.w0 < Q_low.w0) ? 1UL : 0;
							}

							//__add_carry_in_out(out Stemp.w1, out carry, Q_low.w1, bid_reciprocals10_128[extra_digits].w1, CY);
							{
								BID_UINT64 X1 = Q_low.w1 + CY;
								Stemp.w1 = X1 + bid_reciprocals10_128[extra_digits].w1;
								carry = ((Stemp.w1 < X1) || (X1 < CY)) ? 1UL : 0;
							}

							if ((remainder_h >> (64 - amount)) + carry >= (((BID_UINT64)1) << amount))
								status = BID_EXACT_STATUS;
							break;
					}

					__set_status_flags(ref pfpsf, status);
#endif

					// convert to BID and return
					return fast_get_BID64_check_OF(sign_x ^ sign_y, final_exponent, C64
#if !IEEE_ROUND_NEAREST
						, rnd_mode
#endif
#if BID_SET_STATUS_FLAGS
						, ref pfpsf
#endif
						);
				}

				// go to convert_format and exit
				C64 = P.w0;
				return get_BID64(sign_x ^ sign_y, exponent_x + exponent_y - DECIMAL_EXPONENT_BIAS, C64
#if !IEEE_ROUND_NEAREST
					, rnd_mode
#endif
#if BID_SET_STATUS_FLAGS
					, ref pfpsf
#endif
					);
			}
		}
	}
}
