using static EPAM.Deltix.DFP.BidDecimalData;
using static EPAM.Deltix.DFP.BidInternal;

using BID_UINT64 = System.UInt64;
using BID_SINT64 = System.Int64;
using BID_UINT32 = System.UInt32;
using _IDEC_flags = System.UInt32;
using int_double = System.UInt64;

namespace EPAM.Deltix.DFP
{
	internal static class Bid64Add
	{
		/// <summary>
		///  Algorithm description:
		///
		///   if(exponent_a < exponent_b)
		///       switch a, b
		///   diff_expon = exponent_a - exponent_b
		///   if(diff_expon > 16)
		///      return normalize(a)
		///   if(coefficient_a*10^diff_expon guaranteed below 2^62)
		///       S = sign_a*coefficient_a*10^diff_expon + sign_b*coefficient_b
		///       if(|S|<10^16)
		///           return get_BID64(sign(S),exponent_b,|S|)
		///       else
		///          determine number of extra digits in S (1, 2, or 3)
		///            return rounded result
		///   else // large exponent difference
		///       if(number_digits(coefficient_a*10^diff_expon) +/- 10^16)
		///          guaranteed the same as
		///          number_digits(coefficient_a*10^diff_expon) )
		///           S = normalize(coefficient_a + (sign_a^sign_b)*10^(16-diff_expon))
		///           corr = 10^16 + (sign_a^sign_b)*coefficient_b
		///           corr*10^exponent_b is rounded so it aligns with S*10^exponent_S
		///           return get_BID64(sign_a,exponent(S),S+rounded(corr))
		///       else
		///         add sign_a*coefficient_a*10^diff_expon, sign_b*coefficient_b
		///             in 128-bit integer arithmetic, then round to 16 decimal digits
		/// </summary>
		public static BID_UINT64 bid64_add(BID_UINT64 x, BID_UINT64 y
#if !IEEE_ROUND_NEAREST
			, int rnd_mode
#endif
#if BID_SET_STATUS_FLAGS
			, ref _IDEC_flags pfpsf
#endif
			)
		{
			BID_UINT128 CA, CT, CT_new;
			BID_UINT64 sign_x, sign_y, coefficient_x, coefficient_y, C64_new;
			BID_UINT64 valid_x, valid_y;
			BID_UINT64 res;
			BID_UINT64 sign_a, sign_b, coefficient_a, coefficient_b, sign_s, sign_ab, rem_a;
			BID_UINT64 saved_ca, saved_cb, C0_64, C64, remainder_h, T1, carry, tmp;
			int_double tempx_i;
			int exponent_x, exponent_y, exponent_a, exponent_b, diff_dec_expon;
			int bin_expon_ca, extra_digits, amount, scale_k, scale_ca;
			int rmode;
#if BID_SET_STATUS_FLAGS
			_IDEC_flags status;
#endif

			valid_x = unpack_BID64(out sign_x, out exponent_x, out coefficient_x, x);
			valid_y = unpack_BID64(out sign_y, out exponent_y, out coefficient_y, y);

			// unpack arguments, check for NaN or Infinity
			if (valid_x == 0)
			{
				// x is Inf. or NaN

				// test if x is NaN
				if ((x & NAN_MASK64) == NAN_MASK64)
				{
#if BID_SET_STATUS_FLAGS
					if (((x & SNAN_MASK64) == SNAN_MASK64)  // sNaN
						|| ((y & SNAN_MASK64) == SNAN_MASK64))
						__set_status_flags(ref pfpsf, BID_INVALID_EXCEPTION);
#endif
					return coefficient_x & QUIET_MASK64;
				}
				// x is Infinity?
				if ((x & INFINITY_MASK64) == INFINITY_MASK64)
				{
					// check if y is Inf
					if (((y & NAN_MASK64) == INFINITY_MASK64))
					{
						if (sign_x == (y & 0x8000000000000000UL))
						{
							return coefficient_x;
						}
						// return NaN
						{
#if BID_SET_STATUS_FLAGS
							__set_status_flags(ref pfpsf, BID_INVALID_EXCEPTION);
#endif
							return NAN_MASK64;
						}
					}
					// check if y is NaN
					if (((y & NAN_MASK64) == NAN_MASK64))
					{
						res = coefficient_y & QUIET_MASK64;
#if BID_SET_STATUS_FLAGS
						if (((y & SNAN_MASK64) == SNAN_MASK64))
							__set_status_flags(ref pfpsf, BID_INVALID_EXCEPTION);
#endif
						return res;
					}
					// otherwise return +/-Inf
					{
						return coefficient_x;
					}
				}
				// x is 0
				{
					if (((y & INFINITY_MASK64) != INFINITY_MASK64) && coefficient_y != 0)
					{
						if (exponent_y <= exponent_x)
						{
							return y;
						}
					}
				}
			}

			if (valid_y == 0)
			{
				// y is Inf. or NaN?
				if (((y & INFINITY_MASK64) == INFINITY_MASK64))
				{
#if BID_SET_STATUS_FLAGS
					if ((y & SNAN_MASK64) == SNAN_MASK64)   // sNaN
						__set_status_flags(ref pfpsf, BID_INVALID_EXCEPTION);
#endif
					return coefficient_y & QUIET_MASK64;
				}
				// y is 0
				if (coefficient_x == 0)
				{
					// x==0
					if (exponent_x <= exponent_y)
						res = ((BID_UINT64)exponent_x) << 53;
					else
						res = ((BID_UINT64)exponent_y) << 53;
					if (sign_x == sign_y)
						res |= sign_x;
#if !IEEE_ROUND_NEAREST_TIES_AWAY
#if !IEEE_ROUND_NEAREST
					if (rnd_mode == BID_ROUNDING_DOWN && sign_x != sign_y)
						res |= 0x8000000000000000UL;
#endif
#endif
					return res;
				}
				else if (exponent_y >= exponent_x)
				{
					return x;
				}
			}

			// sort arguments by exponent
			if (exponent_x < exponent_y)
			{
				sign_a = sign_y;
				exponent_a = exponent_y;
				coefficient_a = coefficient_y;
				sign_b = sign_x;
				exponent_b = exponent_x;
				coefficient_b = coefficient_x;
			}
			else
			{
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
			tempx_i = doubleToBits((double)coefficient_a);
			bin_expon_ca = (int)(((tempx_i & MASK_BINARY_EXPONENT) >> 52) - 0x3ff);

			if (diff_dec_expon > MAX_FORMAT_DIGITS)
			{
				// normalize a to a 16-digit coefficient

				scale_ca = bid_estimate_decimal_digits[bin_expon_ca];
				if (coefficient_a >= bid_power10_table_128[scale_ca].w0)
					scale_ca++;

				scale_k = 16 - scale_ca;

				coefficient_a *= bid_power10_table_128[scale_k].w0;

				diff_dec_expon -= scale_k;
				exponent_a -= scale_k;

				/* get binary coefficients of x and y */

				//--- get number of bits in the coefficients of x and y ---
				tempx_i = doubleToBits((double)coefficient_a);
				bin_expon_ca = (int)(((tempx_i & MASK_BINARY_EXPONENT) >> 52) - 0x3ff);

				if (diff_dec_expon > MAX_FORMAT_DIGITS)
				{
#if BID_SET_STATUS_FLAGS
					if (coefficient_b != 0)
					{
						__set_status_flags(ref pfpsf, BID_INEXACT_EXCEPTION);
					}
#endif

#if !IEEE_ROUND_NEAREST_TIES_AWAY
#if !IEEE_ROUND_NEAREST
					if (((rnd_mode) & 3) != 0 && coefficient_b != 0)  // not BID_ROUNDING_TO_NEAREST
					{
						switch (rnd_mode)
						{
							case BID_ROUNDING_DOWN:
								if (sign_b != 0)
								{
									coefficient_a -= (BID_UINT64)((((BID_SINT64)sign_a) >> 63) | 1);
									if (coefficient_a < 1000000000000000UL)
									{
										exponent_a--;
										coefficient_a = 9999999999999999UL;
									}
									else if (coefficient_a >= 10000000000000000UL)
									{
										exponent_a++;
										coefficient_a = 1000000000000000UL;
									}
								}
								break;
							case BID_ROUNDING_UP:
								if (sign_b == 0)
								{
									coefficient_a += (BID_UINT64)((((BID_SINT64)sign_a) >> 63) | 1);
									if (coefficient_a < 1000000000000000UL)
									{
										exponent_a--;
										coefficient_a = 9999999999999999UL;
									}
									else if (coefficient_a >= 10000000000000000UL)
									{
										exponent_a++;
										coefficient_a = 1000000000000000UL;
									}
								}
								break;
							default:    // RZ
								if (sign_a != sign_b)
								{
									coefficient_a--;
									if (coefficient_a < 1000000000000000UL)
									{
										exponent_a--;
										coefficient_a = 9999999999999999UL;
									}
								}
								break;
						}
					}
					else
#endif
#endif
					// check special case here
					if ((coefficient_a == 1000000000000000UL) && (diff_dec_expon == MAX_FORMAT_DIGITS + 1)
						&& (sign_a ^ sign_b) != 0 && (coefficient_b > 5000000000000000UL))
					{
						coefficient_a = 9999999999999999UL;
						exponent_a--;
					}

					return fast_get_BID64_check_OF(sign_a, exponent_a, coefficient_a
#if !IEEE_ROUND_NEAREST
						, rnd_mode
#endif
#if BID_SET_STATUS_FLAGS
						, ref pfpsf
#endif
						);
				}
			}
			// test whether coefficient_a*10^(exponent_a-exponent_b)  may exceed 2^62
			if (bin_expon_ca + bid_estimate_bin_expon[diff_dec_expon] < 60)
			{
				// coefficient_a*10^(exponent_a-exponent_b)<2^63

				// multiply by 10^(exponent_a-exponent_b)
				coefficient_a *= bid_power10_table_128[diff_dec_expon].w0;

				// sign mask
				sign_b = (BID_UINT64)(((BID_SINT64)sign_b) >> 63);
				// apply sign to coeff. of b
				coefficient_b = (coefficient_b + sign_b) ^ sign_b;

				// apply sign to coefficient a
				sign_a = (BID_UINT64)(((BID_SINT64)sign_a) >> 63);
				coefficient_a = (coefficient_a + sign_a) ^ sign_a;

				coefficient_a += coefficient_b;
				// get sign
				sign_s = (BID_UINT64)(((BID_SINT64)coefficient_a) >> 63);
				coefficient_a = (coefficient_a + sign_s) ^ sign_s;
				sign_s &= 0x8000000000000000UL;

				// coefficient_a < 10^16 ?
				if (coefficient_a < bid_power10_table_128[MAX_FORMAT_DIGITS].w0)
				{
#if !IEEE_ROUND_NEAREST_TIES_AWAY
#if !IEEE_ROUND_NEAREST
					if (rnd_mode == BID_ROUNDING_DOWN && (coefficient_a == 0)
					&& sign_a != sign_b)
						sign_s = 0x8000000000000000UL;
#endif
#endif
					return very_fast_get_BID64(sign_s, exponent_b, coefficient_a);
				}
				// otherwise rounding is necessary

				// already know coefficient_a<10^19
				// coefficient_a < 10^17 ?
				if (coefficient_a < bid_power10_table_128[17].w0)
					extra_digits = 1;
				else if (coefficient_a < bid_power10_table_128[18].w0)
					extra_digits = 2;
				else
					extra_digits = 3;

#if !IEEE_ROUND_NEAREST_TIES_AWAY
#if !IEEE_ROUND_NEAREST
				rmode = rnd_mode;
				if (sign_s != 0 && (uint)(rmode - 1) < 2)
					rmode = 3 - rmode;
#else
				rmode = 0;
#endif
#else
				rmode = 0;
#endif
				coefficient_a += bid_round_const_table[rmode, extra_digits];

				// get P*(2^M[extra_digits])/10^extra_digits
				__mul_64x64_to_128(out CT, coefficient_a, bid_reciprocals10_64[extra_digits]);

				// now get P/10^extra_digits: shift C64 right by M[extra_digits]-128
				amount = bid_short_recip_scale[extra_digits];
				C64 = CT.w1 >> amount;
			}
			else
			{
				// coefficient_a*10^(exponent_a-exponent_b) is large
				sign_s = sign_a;

#if !IEEE_ROUND_NEAREST_TIES_AWAY
#if !IEEE_ROUND_NEAREST
				rmode = rnd_mode;
				if (sign_s != 0 && (uint)(rmode - 1) < 2)
					rmode = 3 - rmode;
#else
				rmode = 0;
#endif
#else
				rmode = 0;
#endif

				// check whether we can take faster path
				scale_ca = bid_estimate_decimal_digits[bin_expon_ca];

				sign_ab = sign_a ^ sign_b;
				sign_ab = (BID_UINT64)(((BID_SINT64)sign_ab) >> 63);

				// T1 = 10^(16-diff_dec_expon)
				T1 = bid_power10_table_128[16 - diff_dec_expon].w0;

				// get number of digits in coefficient_a
				if (coefficient_a >= bid_power10_table_128[scale_ca].w0)
				{
					scale_ca++;
				}

				scale_k = 16 - scale_ca;

				// addition
				saved_ca = coefficient_a - T1;
				coefficient_a = (BID_UINT64)((BID_SINT64)saved_ca * (BID_SINT64)bid_power10_table_128[scale_k].w0);
				extra_digits = diff_dec_expon - scale_k;

				// apply sign
				saved_cb = (coefficient_b + sign_ab) ^ sign_ab;
				// add 10^16 and rounding constant
				coefficient_b = saved_cb + 10000000000000000UL + bid_round_const_table[rmode, extra_digits];

				// get P*(2^M[extra_digits])/10^extra_digits
				__mul_64x64_to_128(out CT, coefficient_b, bid_reciprocals10_64[extra_digits]);

				// now get P/10^extra_digits: shift C64 right by M[extra_digits]-128
				amount = bid_short_recip_scale[extra_digits];
				C0_64 = CT.w1 >> amount;

				// result coefficient
				C64 = C0_64 + coefficient_a;
				// filter out difficult (corner) cases
				// this test ensures the number of digits in coefficient_a does not change
				// after adding (the appropriately scaled and rounded) coefficient_b
				if ((BID_UINT64)(C64 - 1000000000000000UL - 1) > 9000000000000000UL - 2)
				{
					if (C64 >= 10000000000000000UL)
					{
						// result has more than 16 digits
						if (scale_k == 0)
						{
							// must divide coeff_a by 10
							saved_ca = saved_ca + T1;
							__mul_64x64_to_128(out CA, saved_ca, 0x3333333333333334UL);
							//reciprocals10_64[1]);
							coefficient_a = CA.w1 >> 1;
							rem_a = saved_ca - (coefficient_a << 3) - (coefficient_a << 1);
							coefficient_a = coefficient_a - T1;

							saved_cb += rem_a * bid_power10_table_128[diff_dec_expon].w0;
						}
						else
							coefficient_a = (BID_UINT64)((BID_SINT64)(saved_ca - T1 - (T1 << 3)) * (BID_SINT64)bid_power10_table_128[scale_k - 1].w0);

						extra_digits++;
						coefficient_b = saved_cb + 100000000000000000UL + bid_round_const_table[rmode, extra_digits];

						// get P*(2^M[extra_digits])/10^extra_digits
						__mul_64x64_to_128(out CT, coefficient_b, bid_reciprocals10_64[extra_digits]);

						// now get P/10^extra_digits: shift C64 right by M[extra_digits]-128
						amount = bid_short_recip_scale[extra_digits];
						C0_64 = CT.w1 >> amount;

						// result coefficient
						C64 = C0_64 + coefficient_a;
					}
					else if (C64 <= 1000000000000000UL)
					{
						// less than 16 digits in result
						coefficient_a = (BID_UINT64)((BID_SINT64)saved_ca * (BID_SINT64)bid_power10_table_128[scale_k + 1].w0);
						//extra_digits --;
						exponent_b--;
						coefficient_b = (saved_cb << 3) + (saved_cb << 1) + 100000000000000000UL + bid_round_const_table[rmode, extra_digits];

						// get P*(2^M[extra_digits])/10^extra_digits
						__mul_64x64_to_128(out CT_new, coefficient_b, bid_reciprocals10_64[extra_digits]);

						// now get P/10^extra_digits: shift C64 right by M[extra_digits]-128
						amount = bid_short_recip_scale[extra_digits];
						C0_64 = CT_new.w1 >> amount;

						// result coefficient
						C64_new = C0_64 + coefficient_a;
						if (C64_new < 10000000000000000UL)
						{
							C64 = C64_new;
#if BID_SET_STATUS_FLAGS
							CT = CT_new;
#endif
						}
						else
							exponent_b++;
					}
				}
			}

#if !IEEE_ROUND_NEAREST_TIES_AWAY
#if !IEEE_ROUND_NEAREST
			if (rmode == 0) //BID_ROUNDING_TO_NEAREST
#endif
			if ((C64 & 1) != 0)
			{
				// check whether fractional part of initial_P/10^extra_digits is
				// exactly .5
				// this is the same as fractional part of
				//      (initial_P + 0.5*10^extra_digits)/10^extra_digits is exactly zero

				// get remainder
				remainder_h = CT.w1 << (64 - amount);

				// test whether fractional part is 0
				if (remainder_h == 0 && (CT.w0 < bid_reciprocals10_64[extra_digits]))
				{
					C64--;
				}
			}
#endif

#if BID_SET_STATUS_FLAGS
			status = BID_INEXACT_EXCEPTION;

			// get remainder
			remainder_h = CT.w1 << (64 - amount);

			switch (rmode)
			{
				case BID_ROUNDING_TO_NEAREST:
				case BID_ROUNDING_TIES_AWAY:
					// test whether fractional part is 0
					if ((remainder_h == 0x8000000000000000UL) && (CT.w0 < bid_reciprocals10_64[extra_digits]))
						status = BID_EXACT_STATUS;
					break;
				case BID_ROUNDING_DOWN:
				case BID_ROUNDING_TO_ZERO:
					if (remainder_h == 0 && (CT.w0 < bid_reciprocals10_64[extra_digits]))
						status = BID_EXACT_STATUS;
					//if(!C64 && rmode==BID_ROUNDING_DOWN) sign_s=sign_y;
					break;
				default:
					// round up
					__add_carry_out(out tmp, out carry, CT.w0, bid_reciprocals10_64[extra_digits]);
					if ((remainder_h >> (64 - amount)) + carry >= (((BID_UINT64)1) << amount))
						status = BID_EXACT_STATUS;
					break;
			}
			__set_status_flags(ref pfpsf, status);

#endif

			return fast_get_BID64_check_OF(sign_s, exponent_b + extra_digits, C64
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
