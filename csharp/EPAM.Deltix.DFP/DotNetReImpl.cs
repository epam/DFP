using System.Runtime.CompilerServices;
using static EPAM.Deltix.DFP.BidDecimalData;
using static EPAM.Deltix.DFP.BidInternal;

using BID_UINT64 = System.UInt64;
using BID_UINT32 = System.UInt32;
using _IDEC_flags = System.UInt32;

[assembly: InternalsVisibleToAttribute("EPAM.Deltix.DFP.Test")]
[assembly: InternalsVisibleToAttribute("EPAM.Deltix.DFP.Benchmark")]

namespace EPAM.Deltix.DFP
{
	internal static class DotNetReImpl
	{
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

		public unsafe static BID_UINT64 bid64_from_string(string s, out _IDEC_flags pfpsf, int rnd_mode = BID_ROUNDING_TO_NEAREST/*, _EXC_MASKS_PARAM _EXC_INFO_PARAM*/)
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

				// detect special cases (INF or NaN)
				if (*ps != '.' && (*ps < '0' || *ps > '9'))
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
				if (*ps == '0' || *ps == '.')
				{

					if (*ps == '.')
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
						if (*ps == '.')
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
				while ((c >= '0' && c <= '9') || c == '.')
				{
					if (c == '.')
					{
						if (rdx_pt_enc != 0)
						{
							pfpsf = BID_INVALID_FORMAT;
							return DotNetImpl.NaN; // 0x7c00000000000000UL | sign_x; // return NaN
						}
						rdx_pt_enc = 1;
						ps++;
						c = *ps;
						continue;
					}
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
	}
}
