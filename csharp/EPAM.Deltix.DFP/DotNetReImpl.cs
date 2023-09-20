using System;
using System.Collections.Generic;
using System.Runtime.CompilerServices;
using System.Text;

using BID_UINT64 = System.UInt64;
using BID_UINT32 = System.UInt32;
using _IDEC_flags = System.UInt32;

[assembly: InternalsVisibleToAttribute("EPAM.Deltix.DFP.Test")]
[assembly: InternalsVisibleToAttribute("EPAM.Deltix.DFP.Benchmark")]
namespace EPAM.Deltix.DFP
{
	internal static class DotNetReImpl
	{
		public const int BID_ROUNDING_TO_NEAREST = 0x00000;
		public const int BID_ROUNDING_DOWN = 0x00001;
		public const int BID_ROUNDING_UP = 0x00002;
		public const int BID_ROUNDING_TO_ZERO = 0x00003;
		public const int BID_ROUNDING_TIES_AWAY = 0x00004;
		public const int BID_ROUNDING_EXCEPTION = 0x00005;

		public const uint BID_EXACT_STATUS = 0x00000000;

		public const uint DEC_FE_INVALID = 0x01;
		public const uint DEC_FE_UNNORMAL = 0x02;
		public const uint DEC_FE_DIVBYZERO = 0x04;
		public const uint DEC_FE_OVERFLOW = 0x08;
		public const uint DEC_FE_UNDERFLOW = 0x10;
		public const uint DEC_FE_INEXACT = 0x20;

		public const uint BID_INEXACT_EXCEPTION = DEC_FE_INEXACT;
		public const uint BID_UNDERFLOW_EXCEPTION = DEC_FE_UNDERFLOW;
		public const uint BID_OVERFLOW_EXCEPTION = DEC_FE_OVERFLOW;
		public const uint BID_ZERO_DIVIDE_EXCEPTION = DEC_FE_DIVBYZERO;
		public const uint BID_DENORMAL_EXCEPTION = DEC_FE_UNNORMAL;
		public const uint BID_INVALID_EXCEPTION = DEC_FE_INVALID;
		public const uint BID_UNDERFLOW_INEXACT_EXCEPTION = (DEC_FE_UNDERFLOW | DEC_FE_INEXACT);
		public const uint BID_OVERFLOW_INEXACT_EXCEPTION = (DEC_FE_OVERFLOW | DEC_FE_INEXACT);
		public const uint BID_INVALID_FORMAT = 0x10000;

		public const int MAX_FORMAT_DIGITS = 16;
		public const int DECIMAL_EXPONENT_BIAS = 398;
		public const int MAX_DECIMAL_EXPONENT = 767;

		private static char tolower_macro(char x)
		{
			return ((x - 'A') <= ('Z' - 'A')) ? (char)(x - 'A' + 'a') : x;
		}

		public static void __set_status_flags(ref _IDEC_flags fpsc, uint status)
		{
			fpsc |= status;
		}

		public static bool is_inexact(_IDEC_flags fpsc)
		{
			return (fpsc & BID_INEXACT_EXCEPTION) != 0;
		}

		private unsafe static bool IsStrEq(char* ptr, string str)
		{
			fixed (char* fixedStr = str)
			{
				char* pStr = fixedStr;

				while (*ptr != '\0' && *pStr != '\0')
				{
					if (*ptr != *pStr)
						return false;
					ptr++;
					pStr++;
				}

				return *ptr == *pStr;
			}
		}

		public unsafe static BID_UINT64 bid64_from_string(string s, string decimalMarks, out _IDEC_flags pfpsf, int rnd_mode = BID_ROUNDING_TO_NEAREST/*, _EXC_MASKS_PARAM _EXC_INFO_PARAM*/)
		{

			BID_UINT64 coefficient_x = 0, rounded = 0;
			int expon_x = 0, sgn_expon, ndigits, add_expon = 0, midpoint = 0, rounded_up = 0;
			int dec_expon_scale = 0;

			fixed (char* fixedS = s.Trim().ToLowerInvariant())
			{
				char* ps = fixedS;

				if (*ps == '\0')
				{
					pfpsf = BID_INVALID_FORMAT;
					return 0x7c00000000000000UL;                    // return qNaN
				}


				// determine sign
				BID_UINT64 sign_x = *ps == '-' ? 0x8000000000000000UL : 0;
				// get next character if leading +/- sign
				if (*ps == '-' || *ps == '+')
				{
					ps++;
					if (*ps == '\0')
					{
						pfpsf = BID_INVALID_FORMAT;
						return 0x7c00000000000000UL;                    // return qNaN
					}
				}

				bool cEqDot = decimalMarks.IndexOf(*ps) >= 0;

				// detect special cases (INF or NaN)
				if (cEqDot && (*ps < '0' || *ps > '9'))
				{
					if (IsStrEq(ps, "inf") || IsStrEq(ps, "infinity")) // Infinity?
					{
						pfpsf = BID_EXACT_STATUS;
						return 0x7800000000000000UL | sign_x;
					}
					// return sNaN
					if (IsStrEq(ps, "snan")) // case insensitive check for snan
					{
						pfpsf = BID_EXACT_STATUS;
						return 0x7e00000000000000UL | sign_x;
					}
					if (IsStrEq(ps, "nan")) // return qNaN
					{
						pfpsf = BID_EXACT_STATUS;
						return 0x7c00000000000000UL | sign_x;
					}
					else // if c isn't a decimal point or a decimal digit, return NaN
					{
						pfpsf = BID_INVALID_FORMAT;
						return 0x7c00000000000000UL;                    // return qNaN
					}
				}

				int rdx_pt_enc = 0;
				int right_radix_leading_zeros = 0;

				// detect zero (and eliminate/ignore leading zeros)
				if (*ps == '0' || cEqDot)
				{

					if (cEqDot)
					{
						rdx_pt_enc = 1;
						ps++;
					}
					// if all numbers are zeros (with possibly 1 radix point, the number is zero
					// should catch cases such as: 000.0
					while (*ps == '0')
					{
						ps++;
						// for numbers such as 0.0000000000000000000000000000000000001001, 
						// we want to count the leading zeros
						if (rdx_pt_enc != 0)
						{
							right_radix_leading_zeros++;
						}
						// if this character is a radix point, make sure we haven't already 
						// encountered one
						if (decimalMarks.IndexOf(*ps) >= 0)
						{
							if (rdx_pt_enc == 0)
							{
								rdx_pt_enc = 1;
								// if this is the first radix point, and the next character is NULL, 
								// we have a zero
								if (*(ps + 1) == '\0')
								{
									pfpsf = BID_EXACT_STATUS;
									return DotNetImpl.Zero | sign_x; // ((BID_UINT64)(398 - right_radix_leading_zeros) << 53) | sign_x;
								}
								ps++;
							}
							else
							{
								pfpsf = BID_INVALID_FORMAT;
								return DotNetImpl.NaN; // 0x7c00000000000000UL | sign_x; // if 2 radix points, return NaN
							}
						}
						else if (*ps == '\0')
						{
							pfpsf = BID_EXACT_STATUS;
							return DotNetImpl.Zero | sign_x; // ((BID_UINT64)(398 - right_radix_leading_zeros) << 53) | sign_x; //pres->w[1] = 0x3040000000000000UL | sign_x;
						}
					}
				}

				char c = *ps;

				ndigits = 0;
				while ((c >= '0' && c <= '9') || (decimalMarks.IndexOf(c) >= 0))
				{
					if (c >= '0' && c <= '9')
					{
						dec_expon_scale += rdx_pt_enc;

						ndigits++;
						if (ndigits <= 16)
						{
							coefficient_x = (coefficient_x << 1) + (coefficient_x << 3);
							coefficient_x += (BID_UINT64)(c - '0');
						}
						else if (ndigits == 17)
						{
							// coefficient rounding
							switch (rnd_mode)
							{
								case BID_ROUNDING_TO_NEAREST:
									midpoint = (c == '5' && (coefficient_x & 1) == 0) ? 1 : 0;
									// if coefficient is even and c is 5, prepare to round up if 
									// subsequent digit is nonzero
									// if str[MAXDIG+1] > 5, we MUST round up
									// if str[MAXDIG+1] == 5 and coefficient is ODD, ROUND UP!
									if (c > '5' || (c == '5' && (coefficient_x & 1) != 0))
									{
										coefficient_x++;
										rounded_up = 1;
									}
									break;

								case BID_ROUNDING_DOWN:
									if (sign_x != 0) { coefficient_x++; rounded_up = 1; }
									break;
								case BID_ROUNDING_UP:
									if (sign_x == 0) { coefficient_x++; rounded_up = 1; }
									break;
								case BID_ROUNDING_TIES_AWAY:
									if (c >= '5') { coefficient_x++; rounded_up = 1; }
									break;
							}
							if (coefficient_x == 10000000000000000UL)
							{
								coefficient_x = 1000000000000000UL;
								add_expon = 1;
							}
							if (c > '0')
								rounded = 1;
							add_expon += 1;
						}
						else
						{ // ndigits > 17
							add_expon++;
							if (midpoint != 0 && c > '0')
							{
								coefficient_x++;
								midpoint = 0;
								rounded_up = 1;
							}
							if (c > '0')
								rounded = 1;
						}
						ps++;
						c = *ps;
					}
					else
					{
						if (rdx_pt_enc != 0)
						{
							pfpsf = BID_INVALID_FORMAT;
							return DotNetImpl.NaN; // 0x7c00000000000000UL | sign_x; // return NaN
						}
						rdx_pt_enc = 1;
						ps++;
						c = *ps;
					}
				}

				add_expon -= (dec_expon_scale + right_radix_leading_zeros);

				if (c == '\0')
				{
					pfpsf = BID_EXACT_STATUS;
					if (rounded != 0)
						__set_status_flags(ref pfpsf, BID_INEXACT_EXCEPTION);
					return /*fast_get_BID64_check_OF*/get_BID64(sign_x,
								   add_expon + DECIMAL_EXPONENT_BIAS,
								   coefficient_x, 0, ref pfpsf);
				}

				if (c != 'E' && c != 'e')
				{
					pfpsf = BID_INVALID_FORMAT;
					return DotNetImpl.NaN; // 0x7c00000000000000UL | sign_x; // return NaN
				}
				ps++;
				c = *ps;
				sgn_expon = (c == '-') ? 1 : 0;
				if (c == '-' || c == '+')
				{
					ps++;
					c = *ps;
				}
				if (c == '\0' || c < '0' || c > '9')
				{
					pfpsf = BID_INVALID_FORMAT;
					return DotNetImpl.NaN; // 0x7c00000000000000UL | sign_x; // return NaN
				}

				while ((c >= '0') && (c <= '9'))
				{
					if (expon_x < (1 << 20))
					{
						expon_x = (expon_x << 1) + (expon_x << 3);
						expon_x += (int)(c - '0');
					}

					ps++;
					c = *ps;
				}

				if (c != '\0')
				{
					pfpsf = BID_INVALID_FORMAT;
					return DotNetImpl.NaN; // 0x7c00000000000000UL | sign_x; // return NaN
				}

				if (rounded != 0)
				{
					pfpsf = BID_EXACT_STATUS;
					__set_status_flags(ref pfpsf, BID_INEXACT_EXCEPTION);
				}

				if (sgn_expon != 0)
					expon_x = -expon_x;

				expon_x += add_expon + DECIMAL_EXPONENT_BIAS;

				if (expon_x < 0)
				{
					if (rounded_up != 0)
						coefficient_x--;
					rnd_mode = 0;
					pfpsf = BID_EXACT_STATUS;
					return get_BID64_UF(sign_x, expon_x, coefficient_x, rounded, rnd_mode, ref pfpsf);
				}
				pfpsf = BID_EXACT_STATUS;
				return get_BID64(sign_x, expon_x, coefficient_x, rnd_mode, ref pfpsf);
			}
		}

		public const int DECIMAL_MAX_EXPON_64 = 767;
		//public const int DECIMAL_EXPONENT_BIAS = 398;
		//public const int MAX_FORMAT_DIGITS = 16;

		public const BID_UINT64 SPECIAL_ENCODING_MASK64 = 0x6000000000000000UL;
		public const BID_UINT64 INFINITY_MASK64 = 0x7800000000000000UL;
		public const BID_UINT64 SINFINITY_MASK64 = 0xf800000000000000UL;
		//public const BID_UINT64 SSNAN_MASK64 = 0xfc00000000000000UL;
		public const BID_UINT64 NAN_MASK64 = 0x7c00000000000000UL;
		//public const BID_UINT64 SNAN_MASK64 = 0x7e00000000000000UL;
		//public const BID_UINT64 QUIET_MASK64 = 0xfdffffffffffffffUL;
		public const BID_UINT64 LARGE_COEFF_MASK64 = 0x0007ffffffffffffUL;
		public const BID_UINT64 LARGE_COEFF_HIGH_BIT64 = 0x0020000000000000UL;
		public const BID_UINT64 SMALL_COEFF_MASK64 = 0x001fffffffffffffUL;
		public const uint EXPONENT_MASK64 = 0x3ff;
		public const int EXPONENT_SHIFT_LARGE64 = 51;
		public const int EXPONENT_SHIFT_SMALL64 = 53;
		public const BID_UINT64 LARGEST_BID64 = 0x77fb86f26fc0ffffUL;
		public const BID_UINT64 SMALLEST_BID64 = 0xf7fb86f26fc0ffffUL;
		//public const BID_UINT64 SMALL_COEFF_MASK128 = 0x0001ffffffffffffUL;
		//public const BID_UINT64 LARGE_COEFF_MASK128 = 0x00007fffffffffffUL;
		//public const uint EXPONENT_MASK128 = 0x3fff;
		//public const BID_UINT64 LARGEST_BID128_HIGH = 0x5fffed09bead87c0UL;
		//public const BID_UINT64 LARGEST_BID128_LOW = 0x378d8e63ffffffffUL;
		//public const uint SPECIAL_ENCODING_MASK32 = 0x60000000;
		//public const uint SINFINITY_MASK32 = 0xf8000000;
		//public const uint INFINITY_MASK32 = 0x78000000;
		//public const uint LARGE_COEFF_MASK32 = 0x007fffff;
		//public const uint LARGE_COEFF_HIGH_BIT32 = 0x00800000;
		//public const uint SMALL_COEFF_MASK32 = 0x001fffff;
		//public const uint EXPONENT_MASK32 = 0xff;
		//public const int LARGEST_BID32 = 0x77f8967f;
		//public const uint NAN_MASK32 = 0x7c000000;
		//public const uint SNAN_MASK32 = 0x7e000000;
		//public const uint SSNAN_MASK32 = 0xfc000000;
		//public const uint QUIET_MASK32 = 0xfdffffff;
		//public const BID_UINT64 MASK_BINARY_EXPONENT = 0x7ff0000000000000UL;
		//public const int BINARY_EXPONENT_BIAS = 0x3ff;
		//public const int UPPER_EXPON_LIMIT = 51;

		//
		//   no underflow checking
		//
		public static BID_UINT64 fast_get_BID64_check_OF(BID_UINT64 sgn, int expon, BID_UINT64 coeff, int rmode, ref uint fpsc)
		{
			BID_UINT64 r, mask;

			if (((uint)expon) >= 3 * 256 - 1)
			{
				if ((expon == 3 * 256 - 1) && coeff == 10000000000000000UL)
				{
					expon = 3 * 256;
					coeff = 1000000000000000UL;
				}

				if (((uint)expon) >= 3 * 256)
				{
					while (coeff < 1000000000000000UL && expon >= 3 * 256)
					{
						expon--;
						coeff = (coeff << 3) + (coeff << 1);
					}
					if (expon > DECIMAL_MAX_EXPON_64)
					{
						__set_status_flags(ref fpsc,
									BID_OVERFLOW_EXCEPTION | BID_INEXACT_EXCEPTION);
						// overflow
						r = sgn | INFINITY_MASK64;
						switch (rmode)
						{
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
			if (coeff < mask)
			{
				r = (BID_UINT64)expon;
				r <<= EXPONENT_SHIFT_SMALL64;
				r |= (coeff | sgn);
				return r;
			}
			// special format

			// eliminate the case coeff==10^16 after rounding
			if (coeff == 10000000000000000UL)
			{
				r = (BID_UINT64)(expon + 1);
				r <<= EXPONENT_SHIFT_SMALL64;
				r |= (1000000000000000UL | sgn);
				return r;
			}

			r = (BID_UINT64)expon;
			r <<= EXPONENT_SHIFT_LARGE64;
			r |= (sgn | SPECIAL_ENCODING_MASK64);
			// add coeff, without leading bits
			mask = (mask >> 2) - 1;
			coeff &= mask;
			r |= coeff;

			return r;
		}

		public struct BID_UINT128
		{
			public BID_UINT128(BID_UINT64 w0, BID_UINT64 w1)
			{
				this.w0 = w0;
				this.w1 = w1;
			}

			public BID_UINT64 w0, w1;
		}

		public static readonly BID_UINT64[,] bid_round_const_table = {
			{	// RN
				0UL,	// 0 extra digits
				5UL,	// 1 extra digits
				50UL,	// 2 extra digits
				500UL,	// 3 extra digits
				5000UL,	// 4 extra digits
				50000UL,	// 5 extra digits
				500000UL,	// 6 extra digits
				5000000UL,	// 7 extra digits
				50000000UL,	// 8 extra digits
				500000000UL,	// 9 extra digits
				5000000000UL,	// 10 extra digits
				50000000000UL,	// 11 extra digits
				500000000000UL,	// 12 extra digits
				5000000000000UL,	// 13 extra digits
				50000000000000UL,	// 14 extra digits
				500000000000000UL,	// 15 extra digits
				5000000000000000UL,	// 16 extra digits
				50000000000000000UL,	// 17 extra digits
				500000000000000000UL	// 18 extra digits
			}
			,
			{	// RD
				0UL,	// 0 extra digits
				0UL,	// 1 extra digits
				0UL,	// 2 extra digits
				00UL,	// 3 extra digits
				000UL,	// 4 extra digits
				0000UL,	// 5 extra digits
				00000UL,	// 6 extra digits
				000000UL,	// 7 extra digits
				0000000UL,	// 8 extra digits
				00000000UL,	// 9 extra digits
				000000000UL,	// 10 extra digits
				0000000000UL,	// 11 extra digits
				00000000000UL,	// 12 extra digits
				000000000000UL,	// 13 extra digits
				0000000000000UL,	// 14 extra digits
				00000000000000UL,	// 15 extra digits
				000000000000000UL,	// 16 extra digits
				0000000000000000UL,	// 17 extra digits
				00000000000000000UL	// 18 extra digits
			}
			,
			{	// round to Inf
				0UL,	// 0 extra digits
				9UL,	// 1 extra digits
				99UL,	// 2 extra digits
				999UL,	// 3 extra digits
				9999UL,	// 4 extra digits
				99999UL,	// 5 extra digits
				999999UL,	// 6 extra digits
				9999999UL,	// 7 extra digits
				99999999UL,	// 8 extra digits
				999999999UL,	// 9 extra digits
				9999999999UL,	// 10 extra digits
				99999999999UL,	// 11 extra digits
				999999999999UL,	// 12 extra digits
				9999999999999UL,	// 13 extra digits
				99999999999999UL,	// 14 extra digits
				999999999999999UL,	// 15 extra digits
				9999999999999999UL,	// 16 extra digits
				99999999999999999UL,	// 17 extra digits
				999999999999999999UL	// 18 extra digits
			}
			,
			{	// RZ
				0UL,	// 0 extra digits
				0UL,	// 1 extra digits
				0UL,	// 2 extra digits
				00UL,	// 3 extra digits
				000UL,	// 4 extra digits
				0000UL,	// 5 extra digits
				00000UL,	// 6 extra digits
				000000UL,	// 7 extra digits
				0000000UL,	// 8 extra digits
				00000000UL,	// 9 extra digits
				000000000UL,	// 10 extra digits
				0000000000UL,	// 11 extra digits
				00000000000UL,	// 12 extra digits
				000000000000UL,	// 13 extra digits
				0000000000000UL,	// 14 extra digits
				00000000000000UL,	// 15 extra digits
				000000000000000UL,	// 16 extra digits
				0000000000000000UL,	// 17 extra digits
				00000000000000000UL	// 18 extra digits
			}
			,
			{	// round ties away from 0
				0UL,	// 0 extra digits
				5UL,	// 1 extra digits
				50UL,	// 2 extra digits
				500UL,	// 3 extra digits
				5000UL,	// 4 extra digits
				50000UL,	// 5 extra digits
				500000UL,	// 6 extra digits
				5000000UL,	// 7 extra digits
				50000000UL,	// 8 extra digits
				500000000UL,	// 9 extra digits
				5000000000UL,	// 10 extra digits
				50000000000UL,	// 11 extra digits
				500000000000UL,	// 12 extra digits
				5000000000000UL,	// 13 extra digits
				50000000000000UL,	// 14 extra digits
				500000000000000UL,	// 15 extra digits
				5000000000000000UL,	// 16 extra digits
				50000000000000000UL,	// 17 extra digits
				500000000000000000UL	// 18 extra digits
			}
			,
		};

		public static BID_UINT128[] bid_reciprocals10_128 =  {
			new BID_UINT128(0UL, 0UL)  ,	// 0 extra digits
			new BID_UINT128(0x3333333333333334UL, 0x3333333333333333UL),	// 1 extra digit
			new BID_UINT128(0x51eb851eb851eb86UL, 0x051eb851eb851eb8UL),	// 2 extra digits
			new BID_UINT128(0x3b645a1cac083127UL, 0x0083126e978d4fdfUL),	// 3 extra digits
			new BID_UINT128(0x4af4f0d844d013aaUL, 0x00346dc5d6388659UL),	//  10^(-4) * 2^131
			new BID_UINT128(0x08c3f3e0370cdc88UL, 0x0029f16b11c6d1e1UL),	//  10^(-5) * 2^134
			new BID_UINT128(0x6d698fe69270b06dUL, 0x00218def416bdb1aUL),	//  10^(-6) * 2^137
			new BID_UINT128(0xaf0f4ca41d811a47UL, 0x0035afe535795e90UL),	//  10^(-7) * 2^141
			new BID_UINT128(0xbf3f70834acdaea0UL, 0x002af31dc4611873UL),	//  10^(-8) * 2^144
			new BID_UINT128(0x65cc5a02a23e254dUL, 0x00225c17d04dad29UL),	//  10^(-9) * 2^147
			new BID_UINT128(0x6fad5cd10396a214UL, 0x0036f9bfb3af7b75UL),	// 10^(-10) * 2^151
			new BID_UINT128(0xbfbde3da69454e76UL, 0x002bfaffc2f2c92aUL),	// 10^(-11) * 2^154
			new BID_UINT128(0x32fe4fe1edd10b92UL, 0x00232f33025bd422UL),	// 10^(-12) * 2^157
			new BID_UINT128(0x84ca19697c81ac1cUL, 0x00384b84d092ed03UL),	// 10^(-13) * 2^161
			new BID_UINT128(0x03d4e1213067bce4UL, 0x002d09370d425736UL),	// 10^(-14) * 2^164
			new BID_UINT128(0x3643e74dc052fd83UL, 0x0024075f3dceac2bUL),	// 10^(-15) * 2^167
			new BID_UINT128(0x56d30baf9a1e626bUL, 0x0039a5652fb11378UL),	// 10^(-16) * 2^171
			new BID_UINT128(0x12426fbfae7eb522UL, 0x002e1dea8c8da92dUL),	// 10^(-17) * 2^174
			new BID_UINT128(0x41cebfcc8b9890e8UL, 0x0024e4bba3a48757UL),	// 10^(-18) * 2^177
			new BID_UINT128(0x694acc7a78f41b0dUL, 0x003b07929f6da558UL),	// 10^(-19) * 2^181
			new BID_UINT128(0xbaa23d2ec729af3eUL, 0x002f394219248446UL),	// 10^(-20) * 2^184
			new BID_UINT128(0xfbb4fdbf05baf298UL, 0x0025c768141d369eUL),	// 10^(-21) * 2^187
			new BID_UINT128(0x2c54c931a2c4b759UL, 0x003c7240202ebdcbUL),	// 10^(-22) * 2^191
			new BID_UINT128(0x89dd6dc14f03c5e1UL, 0x00305b66802564a2UL),	// 10^(-23) * 2^194
			new BID_UINT128(0xd4b1249aa59c9e4eUL, 0x0026af8533511d4eUL),	// 10^(-24) * 2^197
			new BID_UINT128(0x544ea0f76f60fd49UL, 0x003de5a1ebb4fbb1UL),	// 10^(-25) * 2^201
			new BID_UINT128(0x76a54d92bf80caa1UL, 0x00318481895d9627UL),	// 10^(-26) * 2^204
			new BID_UINT128(0x921dd7a89933d54eUL, 0x00279d346de4781fUL),	// 10^(-27) * 2^207
			new BID_UINT128(0x8362f2a75b862215UL, 0x003f61ed7ca0c032UL),	// 10^(-28) * 2^211
			new BID_UINT128(0xcf825bb91604e811UL, 0x0032b4bdfd4d668eUL),	// 10^(-29) * 2^214
			new BID_UINT128(0x0c684960de6a5341UL, 0x00289097fdd7853fUL),	// 10^(-30) * 2^217
			new BID_UINT128(0x3d203ab3e521dc34UL, 0x002073accb12d0ffUL),	// 10^(-31) * 2^220
			new BID_UINT128(0x2e99f7863b696053UL, 0x0033ec47ab514e65UL),	// 10^(-32) * 2^224
			new BID_UINT128(0x587b2c6b62bab376UL, 0x002989d2ef743eb7UL),	// 10^(-33) * 2^227
			new BID_UINT128(0xad2f56bc4efbc2c5UL, 0x00213b0f25f69892UL),	// 10^(-34) * 2^230
			new BID_UINT128(0x0f2abc9d8c9689d1UL, 0x01a95a5b7f87a0efUL),	// 35 extra digits
		};

		public static int[] bid_recip_scale = {
			129 - 128,	// 1
			129 - 128,	// 1/10
			129 - 128,	// 1/10^2
			129 - 128,	// 1/10^3
			3,	// 131 - 128
			6,	// 134 - 128
			9,	// 137 - 128
			13,	// 141 - 128
			16,	// 144 - 128
			19,	// 147 - 128
			23,	// 151 - 128
			26,	// 154 - 128
			29,	// 157 - 128
			33,	// 161 - 128
			36,	// 164 - 128
			39,	// 167 - 128
			43,	// 171 - 128
			46,	// 174 - 128
			49,	// 177 - 128
			53,	// 181 - 128
			56,	// 184 - 128
			59,	// 187 - 128
			63,	// 191 - 128

			66,	// 194 - 128
			69,	// 197 - 128
			73,	// 201 - 128
			76,	// 204 - 128
			79,	// 207 - 128
			83,	// 211 - 128
			86,	// 214 - 128
			89,	// 217 - 128
			92,	// 220 - 128
			96,	// 224 - 128
			99,	// 227 - 128
			102,	// 230 - 128
			109,	// 237 - 128, 1/10^35
		};


		public static void __mul_64x128_full(out BID_UINT64 Ph, out BID_UINT128 Ql, BID_UINT64 A, BID_UINT128 B)
		{
			BID_UINT128 ALBL, ALBH, QM2;

			__mul_64x64_to_128(out ALBH, A, B.w1);
			__mul_64x64_to_128(out ALBL, A, B.w0);

			Ql.w0 = ALBL.w0;
			__add_128_64(out QM2, ALBH, ALBL.w1);
			Ql.w1 = QM2.w0;
			Ph = QM2.w1;
		}

		public static void __mul_64x64_to_128(out BID_UINT128 P, BID_UINT64 CX, BID_UINT64 CY)
		{
			BID_UINT64 CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
			CXH = (CX) >> 32;
			CXL = (BID_UINT32)(CX);
			CYH = (CY) >> 32;
			CYL = (BID_UINT32)(CY);

			PM = CXH * CYL;
			PH = CXH * CYH;
			PL = CXL * CYL;
			PM2 = CXL * CYH;
			PH += (PM >> 32);
			PM = (BID_UINT64)((BID_UINT32)PM) + PM2 + (PL >> 32);

			P.w1 = PH + (PM >> 32);
			P.w0 = (PM << 32) + (BID_UINT32)PL;
		}

		public static void __add_128_64(out BID_UINT128 R128, BID_UINT128 A128, BID_UINT64 B64)
		{
			BID_UINT64 R64H;
			R64H = A128.w1;
			(R128).w0 = (B64) + (A128).w0;
			if ((R128).w0 < (B64))
				R64H++;
			(R128).w1 = R64H;
		}

		public static void __add_carry_out(out BID_UINT64 S, out BID_UINT64 CY, BID_UINT64 X, BID_UINT64 Y)
		{
			BID_UINT64 X1 = X;
			S = X + Y;
			CY = (S < X1) ? 1UL : 0;
		}

		public static void __add_carry_in_out(out BID_UINT64 S, out BID_UINT64 CY, BID_UINT64 X, BID_UINT64 Y, BID_UINT64 CI)
		{
			BID_UINT64 X1;
			X1 = X + CI;
			S = X1 + Y;
			CY = ((S < X1) || (X1 < CI)) ? 1UL : 0;
		}



		//
		// This pack macro is used when underflow is known to occur
		//
		public static BID_UINT64 get_BID64_UF(BID_UINT64 sgn, int expon, BID_UINT64 coeff, BID_UINT64 R, int rmode, ref uint fpsc)
		{
			BID_UINT128 C128, Q_low, Stemp;
			BID_UINT64 _C64, remainder_h, QH, carry, CY;
			int extra_digits, amount, amount2;
			uint status;

			// underflow
			if (expon + MAX_FORMAT_DIGITS < 0)
			{
				__set_status_flags(ref fpsc, BID_UNDERFLOW_EXCEPTION | BID_INEXACT_EXCEPTION);
				if (rmode == BID_ROUNDING_DOWN && sgn != 0)
					return 0x8000000000000001UL;
				if (rmode == BID_ROUNDING_UP && sgn == 0)
					return 1UL;
				// result is 0
				return sgn;
			}
			// 10*coeff
			coeff = (coeff << 3) + (coeff << 1);
			if (sgn != 0 && (uint)(rmode - 1) < 2)
				rmode = 3 - rmode;
			if (R != 0)
				coeff |= 1;
			// get digits to be shifted out
			extra_digits = 1 - expon;
			C128.w0 = coeff + bid_round_const_table[rmode, extra_digits];

			// get coeff*(2^M[extra_digits])/10^extra_digits
			__mul_64x128_full(out QH, out Q_low, C128.w0, bid_reciprocals10_128[extra_digits]);

			// now get P/10^extra_digits: shift Q_high right by M[extra_digits]-128
			amount = bid_recip_scale[extra_digits];

			_C64 = QH >> amount;
			//__shr_128(C128, Q_high, amount); 

			if (rmode == 0) //BID_ROUNDING_TO_NEAREST
				if ((_C64 & 1) != 0)
				{
					// check whether fractional part of initial_P/10^extra_digits is exactly .5

					// get remainder
					amount2 = 64 - amount;
					remainder_h = 0;
					remainder_h--;
					remainder_h >>= amount2;
					remainder_h = remainder_h & QH;

					if (remainder_h == 0
					&& (Q_low.w1 < bid_reciprocals10_128[extra_digits].w1
						|| (Q_low.w1 == bid_reciprocals10_128[extra_digits].w1
						&& Q_low.w0 < bid_reciprocals10_128[extra_digits].w0)))
					{
						_C64--;
					}
				}


			if (is_inexact(fpsc))
				__set_status_flags(ref fpsc, BID_UNDERFLOW_EXCEPTION);
			else
			{
				status = BID_INEXACT_EXCEPTION;
				// get remainder
				remainder_h = QH << (64 - amount);

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
						__add_carry_out(out Stemp.w0, out CY, Q_low.w0, bid_reciprocals10_128[extra_digits].w0);
						__add_carry_in_out(out Stemp.w1, out carry, Q_low.w1, bid_reciprocals10_128[extra_digits].w1, CY);
						if ((remainder_h >> (64 - amount)) + carry >= (((BID_UINT64)1) << amount))
							status = BID_EXACT_STATUS;
						break;
				}

				if (status != BID_EXACT_STATUS)
					__set_status_flags(ref fpsc, BID_UNDERFLOW_EXCEPTION | status);
			}


			return sgn | _C64;

		}

		static void ThrowPackException(BID_UINT64 signMask, int exponentIn, BID_UINT64 coefficientIn)
		{
			throw new FormatException("The unbiasedExponent(=" + (exponentIn - DECIMAL_EXPONENT_BIAS) + ") of the value (=" + coefficientIn + ") with sign(=" + (signMask != 0 ? '-' : '+') + ") can't be saved to Decimal64 without precision loss.");
		}


		//
		//   BID64 pack macro (general form)
		//
		public static BID_UINT64 get_BID64(BID_UINT64 sgn, int exponIn, BID_UINT64 coeffIn, int rmode, ref uint fpsc)
		{
			BID_UINT128 Stemp, Q_low;
			BID_UINT64 QH, r, mask, _C64, remainder_h, CY, carry;
			int extra_digits, amount, amount2;
			uint status;

			int expon = exponIn;
			BID_UINT64 coeff = coeffIn;

			BID_UINT64 tenDivRem = 0;
			bool isAnyNonZeroRem = false;
			while (coeff > 9999999999999999UL)
			{
				tenDivRem = coeff % 10;
				if (tenDivRem != 0)
				{
					if (rmode == BID_ROUNDING_EXCEPTION)
						ThrowPackException(sgn, exponIn, coeffIn);
					isAnyNonZeroRem = true;
				}
				coeff /= 10;
				expon++;
			}

			if (isAnyNonZeroRem)
			{
				switch (rmode)
				{
					case BID_ROUNDING_TO_NEAREST:
						if (tenDivRem >= 5) // Rounding away from zero
							coeff++;
						break;

					case BID_ROUNDING_DOWN:
						if (sgn != 0 /*&& isAnyNonZeroRem - already checked*/)
							coeff++;
						break;

					case BID_ROUNDING_UP:
						if (sgn == 0 /*&& isAnyNonZeroRem - already checked*/)
							coeff++;
						break;

					case BID_ROUNDING_TO_ZERO:
						break;

					case BID_ROUNDING_TIES_AWAY:
						//if (isAnyNonZeroRem)  - already checked
						coeff++;
						break;

					case BID_ROUNDING_EXCEPTION:
						ThrowPackException(sgn, exponIn, coeffIn);
						break;

					default:
						throw new ArgumentException("Unsupported roundingMode(=" + rmode + ") value.");

				}
			}

			if (coeff > 9999999999999999UL)
			{
				if (rmode == BID_ROUNDING_EXCEPTION)
					ThrowPackException(sgn, exponIn, coeffIn);
				expon++;
				coeff = 1000000000000000UL;
			}
			// check for possible underflow/overflow
			if (((uint)expon) >= 3 * 256)
			{
				if (expon < 0)
				{
					// underflow
					if (expon + MAX_FORMAT_DIGITS < 0)
					{
						__set_status_flags(ref fpsc, BID_UNDERFLOW_EXCEPTION | BID_INEXACT_EXCEPTION);
						if (rmode == BID_ROUNDING_EXCEPTION)
							ThrowPackException(sgn, exponIn, coeffIn);
						if (rmode == BID_ROUNDING_DOWN && sgn != 0)
							return 0x8000000000000001UL;
						if (rmode == BID_ROUNDING_UP && sgn == 0)
							return 1UL;
						// result is 0
						return sgn;
					}
					if (sgn != 0 && (uint)(rmode - 1) < 2)
						rmode = 3 - rmode;
					// get digits to be shifted out
					extra_digits = -expon;
					coeff += bid_round_const_table[rmode, extra_digits];

					// get coeff*(2^M[extra_digits])/10^extra_digits
					__mul_64x128_full(out QH, out Q_low, coeff, bid_reciprocals10_128[extra_digits]);

					// now get P/10^extra_digits: shift Q_high right by M[extra_digits]-128
					amount = bid_recip_scale[extra_digits];

					_C64 = QH >> amount;

					if (rmode == 0) //BID_ROUNDING_TO_NEAREST
						if ((_C64 & 1) != 0)
						{
							// check whether fractional part of initial_P/10^extra_digits is exactly .5

							// get remainder
							amount2 = 64 - amount;
							remainder_h = 0;
							remainder_h--;
							remainder_h >>= amount2;
							remainder_h = remainder_h & QH;

							if (remainder_h == 0
								&& (Q_low.w1 < bid_reciprocals10_128[extra_digits].w1
								|| (Q_low.w1 == bid_reciprocals10_128[extra_digits].w1
									&& Q_low.w0 < bid_reciprocals10_128[extra_digits].w0)))
							{
								_C64--;
							}
						}


					if (is_inexact(fpsc))
						__set_status_flags(ref fpsc, BID_UNDERFLOW_EXCEPTION);
					else
					{
						status = BID_INEXACT_EXCEPTION;
						// get remainder
						remainder_h = QH << (64 - amount);

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
								__add_carry_out(out Stemp.w0, out CY, Q_low.w0, bid_reciprocals10_128[extra_digits].w0);
								__add_carry_in_out(out Stemp.w1, out carry, Q_low.w1, bid_reciprocals10_128[extra_digits].w1, CY);
								if ((remainder_h >> (64 - amount)) + carry >= (((BID_UINT64)1) << amount))
									status = BID_EXACT_STATUS;
								break;
						}

						if (status != BID_EXACT_STATUS)
							__set_status_flags(ref fpsc, BID_UNDERFLOW_EXCEPTION | status);
					}


					return sgn | _C64;
				}
				if (coeff == 0) { if (expon > DECIMAL_MAX_EXPON_64) expon = DECIMAL_MAX_EXPON_64; }
				while (coeff < 1000000000000000UL && expon >= 3 * 256)
				{
					expon--;
					coeff = (coeff << 3) + (coeff << 1);
				}
				if (expon > DECIMAL_MAX_EXPON_64)
				{
					__set_status_flags(ref fpsc, BID_OVERFLOW_EXCEPTION | BID_INEXACT_EXCEPTION);
					// overflow
					r = sgn | INFINITY_MASK64;
					switch (rmode)
					{
						case BID_ROUNDING_EXCEPTION:
							ThrowPackException(sgn, exponIn, coeffIn);
							break;

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
			if (coeff < mask)
			{
				r = (BID_UINT64)expon;
				r <<= EXPONENT_SHIFT_SMALL64;
				r |= (coeff | sgn);
				return r;
			}
			// special format

			// eliminate the case coeff==10^16 after rounding
			if (coeff == 10000000000000000UL)
			{
				r = (BID_UINT64)(expon + 1);
				r <<= EXPONENT_SHIFT_SMALL64;
				r |= (1000000000000000UL | sgn);
				return r;
			}

			r = (BID_UINT64)expon;
			r <<= EXPONENT_SHIFT_LARGE64;
			r |= (sgn | SPECIAL_ENCODING_MASK64);
			// add coeff, without leading bits
			mask = (mask >> 2) - 1;
			coeff &= mask;
			r |= coeff;

			return r;
		}

		public static BID_UINT64 unpack_BID64(out BID_UINT64 psign_x, out int pexponent_x, out BID_UINT64 pcoefficient_x, BID_UINT64 x)
		{
			BID_UINT64 tmp, coeff;

			psign_x = x & 0x8000000000000000UL;

			if ((x & SPECIAL_ENCODING_MASK64) == SPECIAL_ENCODING_MASK64)
			{
				// special encodings
				// coefficient
				coeff = (x & LARGE_COEFF_MASK64) | LARGE_COEFF_HIGH_BIT64;

				if ((x & INFINITY_MASK64) == INFINITY_MASK64)
				{
					pexponent_x = 0;
					pcoefficient_x = x & 0xfe03ffffffffffffUL;
					if ((x & 0x0003ffffffffffffUL) >= 1000000000000000UL)
						pcoefficient_x = x & 0xfe00000000000000UL;
					if ((x & NAN_MASK64) == INFINITY_MASK64)
						pcoefficient_x = x & SINFINITY_MASK64;
					return 0;   // NaN or Infinity
				}
				// check for non-canonical values
				if (coeff >= 10000000000000000UL)
					coeff = 0;
				pcoefficient_x = coeff;
				// get exponent
				tmp = x >> EXPONENT_SHIFT_LARGE64;
				pexponent_x = (int)(tmp & EXPONENT_MASK64);
				return coeff;
			}
			// exponent
			tmp = x >> EXPONENT_SHIFT_SMALL64;
			pexponent_x = (int)(tmp & EXPONENT_MASK64);
			// coefficient
			pcoefficient_x = (x & SMALL_COEFF_MASK64);

			return pcoefficient_x;
		}
	}
}
