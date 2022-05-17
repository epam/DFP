using System;
using static EPAM.Deltix.DFP.DotNetReImpl;
using static EPAM.Deltix.DFP.BidDecimalData;

using BID_UINT64 = System.UInt64;
using BID_SINT64 = System.Int64;
using BID_UINT32 = System.UInt32;
using _IDEC_flags = System.UInt32;
using unsigned = System.UInt32;
using System.Runtime.CompilerServices;

namespace EPAM.Deltix.DFP
{
	internal static class BidInternal
	{
		public const int BID_ROUNDING_TO_NEAREST = 0x00000;
		public const int BID_ROUNDING_DOWN = 0x00001;
		public const int BID_ROUNDING_UP = 0x00002;
		public const int BID_ROUNDING_TO_ZERO = 0x00003;
		public const int BID_ROUNDING_TIES_AWAY = 0x00004;

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

		public const int DECIMAL_MAX_EXPON_64 = 767;
		//public const int DECIMAL_EXPONENT_BIAS = 398;
		//public const int MAX_FORMAT_DIGITS = 16;

		public const BID_UINT64 SPECIAL_ENCODING_MASK64 = 0x6000000000000000UL;
		public const BID_UINT64 INFINITY_MASK64 = 0x7800000000000000UL;
		public const BID_UINT64 SINFINITY_MASK64 = 0xf800000000000000UL;
		public const BID_UINT64 SSNAN_MASK64 = 0xfc00000000000000UL;
		public const BID_UINT64 NAN_MASK64 = 0x7c00000000000000UL;
		public const BID_UINT64 SNAN_MASK64 = 0x7e00000000000000UL;
		public const BID_UINT64 QUIET_MASK64 = 0xfdffffffffffffffUL;
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
		public const BID_UINT64 MASK_BINARY_EXPONENT = 0x7ff0000000000000UL;
		public const int BINARY_EXPONENT_BIAS = 0x3ff;
		public const int UPPER_EXPON_LIMIT = 51;

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe void __set_status_flags(ref _IDEC_flags fpsc, _IDEC_flags status)
		{
			fpsc |= status;
		}

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe BID_UINT64 doubleToBits(double value)
		{
			return *(BID_UINT64*)(&value);
			//return (BID_UINT64)BitConverter.DoubleToInt64Bits(value);
		}

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe double bitsToDouble(BID_UINT64 value)
		{
			return *(double*)(&value);
			//return BitConverter.Int64BitsToDouble((long)value);
		}

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe BID_UINT32 floatToBits(float value)
		{
			return *(BID_UINT32*)(&value);
		}

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe float bitsToFloat(BID_UINT32 value)
		{
			return *(float*)(&value);
		}

		/// <summary>
		/// No overflow/underflow checking
		/// or checking for coefficients equal to 10^16 (after rounding)
		/// </summary>
#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe BID_UINT64 very_fast_get_BID64(BID_UINT64 sgn, int expon, BID_UINT64 coeff)
		{
			BID_UINT64 r, mask;

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
			r = (BID_UINT64)expon;
			r <<= EXPONENT_SHIFT_LARGE64;
			r |= (sgn | SPECIAL_ENCODING_MASK64);
			// add coeff, without leading bits
			mask = (mask >> 2) - 1;
			coeff &= mask;
			r |= coeff;

			return r;
		}

		//
		//   no underflow checking
		//
#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe BID_UINT64 fast_get_BID64_check_OF(BID_UINT64 sgn, int expon, BID_UINT64 coeff, int rmode, ref uint fpsc)
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
						__set_status_flags(ref fpsc, BID_OVERFLOW_EXCEPTION | BID_INEXACT_EXCEPTION);
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

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe BID_UINT64 fast_get_BID64_check_OF(BID_UINT64 sgn, int expon, BID_UINT64 coeff)
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
						// overflow
						return sgn | INFINITY_MASK64;
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

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe bool __unsigned_compare_gt_128(BID_UINT128 A, BID_UINT128 B)
		{
			return (A.w1 > B.w1) || ((A.w1 == B.w1) && (A.w0 > B.w0));
		}

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe bool __unsigned_compare_ge_128(BID_UINT128 A, BID_UINT128 B)
		{
			return (A.w1 > B.w1) || ((A.w1 == B.w1) && (A.w0 >= B.w0));
		}

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe bool __test_equal_128(BID_UINT128 A, BID_UINT128 B)
		{
			return (A.w1 == B.w1) && (A.w0 == B.w0);
		}

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe void __tight_bin_range_128(out int bp, BID_UINT128 P, int bin_expon)
		{
			BID_UINT64 M;
			M = 1;
			bp = bin_expon;
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

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe void __mul_64x64_to_64_high_fast(out BID_UINT64 P, BID_UINT64 CX, BID_UINT64 CY)
		{
			BID_UINT64 CXH, CXL, CYH, CYL, PL, PH, PM;
			CXH = (CX) >> 32;
			CXL = (BID_UINT32)(CX);
			CYH = (CY) >> 32;
			CYL = (BID_UINT32)(CY);

			PM = CXH * CYL;
			PL = CXL * CYL;
			PH = CXH * CYH;
			PM += CXL * CYH;
			PM += (PL >> 32);

			P = PH + (PM >> 32);
		}

		// get full 64x64bit product
		//
#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe void __mul_64x64_to_128(out BID_UINT128 P, BID_UINT64 CX_, BID_UINT64 CY_)
		{
			BID_UINT64 CX = CX_;
			BID_UINT64 CY = CY_;
			BID_UINT64 CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
			CXH = CX >> 32;
			CXL = (BID_UINT32)CX;
			CYH = CY >> 32;
			CYL = (BID_UINT32)CY;

			PM = CXH * CYL;
			PH = CXH * CYH;
			PL = CXL * CYL;
			PM2 = CXL * CYH;
			PH += (PM >> 32);
			PM = (BID_UINT64)((BID_UINT32)PM) + PM2 + (PL >> 32);

			P.w1 = PH + (PM >> 32);
			P.w0 = (PM << 32) + (BID_UINT32)PL;
		}

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe void __mul_128x128_high(out BID_UINT128 Q, BID_UINT128 A, BID_UINT128 B)
		{
			BID_UINT128 ALBL, ALBH, AHBL, AHBH, QM, QM2;

			__mul_64x64_to_128(out ALBH, A.w0, B.w1);
			__mul_64x64_to_128(out AHBL, B.w0, A.w1);
			__mul_64x64_to_128(out ALBL, A.w0, B.w0);
			__mul_64x64_to_128(out AHBH, A.w1, B.w1);

			__add_128_128(out QM, ALBH, AHBL);
			__add_128_64(out QM2, QM, ALBL.w1);
			__add_128_64(out Q, AHBH, QM2.w1);
		}

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe void __mul_128x128_full(out BID_UINT128 Qh, out BID_UINT128 Ql, BID_UINT128 A, BID_UINT128 B)
		{
			BID_UINT128 ALBL, ALBH, AHBL, AHBH, QM, QM2;

			__mul_64x64_to_128(out ALBH, A.w0, B.w1);
			__mul_64x64_to_128(out AHBL, B.w0, A.w1);
			__mul_64x64_to_128(out ALBL, A.w0, B.w0);
			__mul_64x64_to_128(out AHBH, A.w1, B.w1);

			__add_128_128(out QM, ALBH, AHBL);
			Ql.w0 = ALBL.w0;
			__add_128_64(out QM2, QM, ALBL.w1);
			__add_128_64(out Qh, AHBH, QM2.w1);
			Ql.w1 = QM2.w0;
		}

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe void __mul_128x128_low(out BID_UINT128 Ql, BID_UINT128 A, BID_UINT128 B)
		{
			BID_UINT128 ALBL;
			BID_UINT64 QM64;

			__mul_64x64_to_128(out ALBL, A.w0, B.w0);
			QM64 = B.w0 * A.w1 + A.w0 * B.w1;

			Ql.w0 = ALBL.w0;
			Ql.w1 = QM64 + ALBL.w1;
		}

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe void __mul_64x128_low(out BID_UINT128 Ql, BID_UINT64 A, BID_UINT128 B)
		{
			BID_UINT128 ALBL, ALBH, QM2;
			__mul_64x64_to_128(out ALBH, A, B.w1);
			__mul_64x64_to_128(out ALBL, A, B.w0);
			Ql.w0 = ALBL.w0;
			__add_128_64(out QM2, ALBH, ALBL.w1);
			Ql.w1 = QM2.w0;
		}

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe void __mul_64x128_full(out BID_UINT64 Ph, out BID_UINT128 Ql, BID_UINT64 A, BID_UINT128 B)
		{
			BID_UINT128 ALBL, ALBH, QM2;

			__mul_64x64_to_128(out ALBH, A, B.w1);
			__mul_64x64_to_128(out ALBL, A, B.w0);

			Ql.w0 = ALBL.w0;
			__add_128_64(out QM2, ALBH, ALBL.w1);
			Ql.w1 = QM2.w0;
			Ph = QM2.w1;
		}

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe void __mul_64x128_to_192(out BID_UINT192 Q, BID_UINT64 A, BID_UINT128 B)
		{
			BID_UINT128 ALBL, ALBH, QM2;

			__mul_64x64_to_128(out ALBH, A, B.w1);
			__mul_64x64_to_128(out ALBL, A, B.w0);

			Q.w0 = ALBL.w0;
			__add_128_64(out QM2, ALBH, ALBL.w1);
			Q.w1 = QM2.w0;
			Q.w2 = QM2.w1;
		}

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe void __mul_128x128_to_256(out BID_UINT256 P256, BID_UINT128 A, BID_UINT128 B)
		{
			BID_UINT128 Qll, Qlh;
			BID_UINT64 Phl, Phh, CY1, CY2;

			__mul_64x128_full(out Phl, out Qll, A.w0, B);
			__mul_64x128_full(out Phh, out Qlh, A.w1, B);
			P256.w0 = Qll.w0;
			__add_carry_out(out P256.w1, out CY1, Qlh.w0, Qll.w1);
			__add_carry_in_out(out P256.w2, out CY2, Qlh.w1, Phl, CY1);
			P256.w3 = Phh + CY2;
		}


		// add 64-bit value to 128-bit
#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe void __add_128_64(out BID_UINT128 R128, BID_UINT128 A128_, BID_UINT64 B64_)
		{
			BID_UINT128 A128 = A128_;
			BID_UINT64 B64 = B64_;
			BID_UINT64 R64H = A128.w1;
			R128.w0 = B64 + A128.w0;
			if (R128.w0 < B64)
				R64H++;
			R128.w1 = R64H;
		}

		// subtract 64-bit value from 128-bit
#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe void __sub_128_64(out BID_UINT128 R128, BID_UINT128 A128, BID_UINT64 B64)
		{
			BID_UINT64 R64H;
			R64H = A128.w1;
			if (A128.w0 < B64)
				R64H--;
			R128.w1 = R64H;
			R128.w0 = A128.w0 - B64;
		}

		// add 128-bit value to 128-bit
		// assume no carry-out
#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe void __add_128_128(out BID_UINT128 R128, BID_UINT128 A128_, BID_UINT128 B128_)
		{
			BID_UINT128 A128 = A128_;
			BID_UINT128 B128 = B128_;
			BID_UINT128 Q128;
			Q128.w1 = A128.w1 + B128.w1;
			Q128.w0 = B128.w0 + A128.w0;
			if (Q128.w0 < B128.w0)
				Q128.w1++;
			R128.w1 = Q128.w1;
			R128.w0 = Q128.w0;
		}

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe void __sub_128_128(out BID_UINT128 R128, BID_UINT128 A128, BID_UINT128 B128)
		{
			BID_UINT128 Q128;
			Q128.w1 = A128.w1 - B128.w1;
			Q128.w0 = A128.w0 - B128.w0;
			if (A128.w0 < B128.w0)
				Q128.w1--;
			R128.w1 = Q128.w1;
			R128.w0 = Q128.w0;
		}

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe void __add_carry_out(out BID_UINT64 S, out BID_UINT64 CY, BID_UINT64 X, BID_UINT64 Y)
		{
			S = X + Y;
			CY = (S < X) ? 1UL : 0;
		}

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe void __add_carry_in_out(out BID_UINT64 S, out BID_UINT64 CY, BID_UINT64 X, BID_UINT64 Y, BID_UINT64 CI)
		{
			BID_UINT64 X1 = X + CI;
			S = X1 + Y;
			CY = ((S < X1) || (X1 < CI)) ? 1UL : 0;
		}

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe void __sub_borrow_out(out BID_UINT64 S, out BID_UINT64 CY, BID_UINT64 X, BID_UINT64 Y)
		{
			BID_UINT64 X1 = X;
			S = X - Y;
			CY = (S > X1) ? 1UL : 0;
		}

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe void __sub_borrow_in_out(out BID_UINT64 S, out BID_UINT64 CY, BID_UINT64 X, BID_UINT64 Y, BID_UINT64 CI)
		{
			BID_UINT64 X1, X0 = X;
			X1 = X - CI;
			S = X1 - Y;
			CY = ((S > X1) || (X1 > X0)) ? 1UL : 0;
		}

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe void __shr_128(out BID_UINT128 Q, BID_UINT128 A, int k)
		{
			Q.w0 = A.w0 >> k;
			Q.w0 |= A.w1 << (64 - k);
			Q.w1 = A.w1 >> k;
		}

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe void __shr_128_long(out BID_UINT128 Q, BID_UINT128 A, int k)
		{
			if (k < 64)
			{
				Q.w0 = A.w0 >> k;
				Q.w0 |= A.w1 << (64 - k);
				Q.w1 = A.w1 >> k;
			}
			else
			{
				Q.w0 = A.w1 >> (k - 64);
				Q.w1 = 0;
			}
		}

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe void __shl_128_long(out BID_UINT128 Q, BID_UINT128 A, int k)
		{
			if (k < 64)
			{
				Q.w1 = A.w1 << k;
				Q.w1 |= A.w0 >> (64 - k);
				Q.w0 = A.w0 << k;
			}
			else
			{
				Q.w1 = A.w0 << (k - 64);
				Q.w0 = 0;
			}
		}

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe BID_UINT64 __low_64(BID_UINT128 Q)
		{
			return Q.w0;
		}

		//
		// This pack macro is used when underflow is known to occur
		//
#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe BID_UINT64 get_BID64_UF(BID_UINT64 sgn, int expon, BID_UINT64 coeff, BID_UINT64 R, int rmode, ref _IDEC_flags fpsc)
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

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe BID_UINT64 get_BID64_UF(BID_UINT64 sgn, int expon, BID_UINT64 coeff, BID_UINT64 R)
		{
			_IDEC_flags fpsc = BID_EXACT_STATUS;
			return get_BID64_UF(sgn, expon, coeff, R, BID_ROUNDING_TO_NEAREST, ref fpsc);
		}

		//
		//   This pack macro doesnot check for coefficients above 2^53
		//
#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe BID_UINT64 get_BID64_small_mantissa(BID_UINT64 sgn, int expon, BID_UINT64 coeff, int rmode, ref _IDEC_flags fpsc)
		{
			BID_UINT128 C128, Q_low;
#if BID_SET_STATUS_FLAGS
			BID_UINT128 Stemp;
			BID_UINT64 carry, CY;
#endif
			BID_UINT64 r, mask, _C64, remainder_h, QH;
			int extra_digits, amount, amount2;
#if BID_SET_STATUS_FLAGS
			unsigned status;
#endif

			// check for possible underflow/overflow
			if (((unsigned)expon) >= 3 * 256)
			{
				if (expon < 0)
				{
					// underflow
					if (expon + MAX_FORMAT_DIGITS < 0)
					{
#if BID_SET_STATUS_FLAGS
						__set_status_flags(ref fpsc, BID_UNDERFLOW_EXCEPTION | BID_INEXACT_EXCEPTION);
#endif
#if !IEEE_ROUND_NEAREST_TIES_AWAY
#if !IEEE_ROUND_NEAREST
						if (rmode == BID_ROUNDING_DOWN && sgn != 0)
							return 0x8000000000000001UL;
						if (rmode == BID_ROUNDING_UP && sgn == 0)
							return 1UL;
#endif
#endif
						// result is 0
						return sgn;
					}
#if !IEEE_ROUND_NEAREST_TIES_AWAY
#if !IEEE_ROUND_NEAREST
					if (sgn != 0 && (unsigned)(rmode - 1) < 2)
						rmode = 3 - rmode;
#endif
#endif
					// get digits to be shifted out
					extra_digits = -expon;
					C128.w0 = coeff + bid_round_const_table[rmode, extra_digits];

					// get coeff*(2^M[extra_digits])/10^extra_digits
					__mul_64x128_full(out QH, out Q_low, C128.w0, bid_reciprocals10_128[extra_digits]);

					// now get P/10^extra_digits: shift Q_high right by M[extra_digits]-128
					amount = bid_recip_scale[extra_digits];

					_C64 = QH >> amount;

#if !IEEE_ROUND_NEAREST_TIES_AWAY
#if !IEEE_ROUND_NEAREST
					if (rmode == 0) //BID_ROUNDING_TO_NEAREST
#endif
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
#endif

#if BID_SET_STATUS_FLAGS
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
								if ((remainder_h >> (64 - amount)) + carry >=
									(((BID_UINT64)1) << amount))
									status = BID_EXACT_STATUS;
								break;
						}

						if (status != BID_EXACT_STATUS)
							__set_status_flags(ref fpsc, BID_UNDERFLOW_EXCEPTION | status);
					}

#endif

					return sgn | _C64;
				}

				while (coeff < 1000000000000000UL && expon >= 3 * 256)
				{
					expon--;
					coeff = (coeff << 3) + (coeff << 1);
				}

				if (expon > DECIMAL_MAX_EXPON_64)
				{
#if BID_SET_STATUS_FLAGS
					__set_status_flags(ref fpsc, BID_OVERFLOW_EXCEPTION | BID_INEXACT_EXCEPTION);
#endif
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
				else
				{
					mask = 1;
					mask <<= EXPONENT_SHIFT_SMALL64;
					if (coeff >= mask)
					{
						r = (BID_UINT64)expon;
						r <<= EXPONENT_SHIFT_LARGE64;
						r |= (sgn | SPECIAL_ENCODING_MASK64);
						// add coeff, without leading bits
						mask = (mask >> 2) - 1;
						coeff &= mask;
						r |= coeff;
						return r;
					}
				}
			}

			r = (BID_UINT64)expon;
			r <<= EXPONENT_SHIFT_SMALL64;
			r |= (coeff | sgn);

			return r;
		}

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe BID_UINT64 get_BID64_small_mantissa(BID_UINT64 sgn, int expon, BID_UINT64 coeff)
		{
			_IDEC_flags fpsc = BID_EXACT_STATUS;
			return get_BID64_small_mantissa(sgn, expon, coeff, BID_ROUNDING_TO_NEAREST, ref fpsc);
		}

		//
		//   BID64 pack macro (general form)
		//
#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe BID_UINT64 get_BID64(BID_UINT64 sgn, int expon, BID_UINT64 coeff, int rmode, ref _IDEC_flags fpsc)
		{
			BID_UINT128 Stemp, Q_low;
			BID_UINT64 QH, r, mask, _C64, remainder_h, CY, carry;
			int extra_digits, amount, amount2;
			uint status;

			if (coeff > 9999999999999999UL)
			{
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

				if (coeff == 0)
				{
					if (expon > DECIMAL_MAX_EXPON_64) expon = DECIMAL_MAX_EXPON_64;
				}

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

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe BID_UINT64 get_BID64(BID_UINT64 sgn, int expon, BID_UINT64 coeff)
		{
			_IDEC_flags fpsc = BID_EXACT_STATUS;
			return get_BID64(sgn, expon, coeff, BID_ROUNDING_TO_NEAREST, ref fpsc);
		}

#if NET6_0_OR_GREATER
		[MethodImpl(MethodImplOptions.AggressiveOptimization)]
#endif
		public static unsafe BID_UINT64 unpack_BID64(out BID_UINT64 psign_x, out int pexponent_x, out BID_UINT64 pcoefficient_x, BID_UINT64 x)
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
					return 0; // NaN or Infinity
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
