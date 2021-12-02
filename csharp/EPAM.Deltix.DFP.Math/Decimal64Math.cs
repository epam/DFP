using System;
using System.Collections.Generic;
using System.Text;

namespace EPAM.Deltix.DFP
{
	public static class Decimal64Math
	{
		/// <summary>
		/// Returns the base-e exponential function of x, which is e raised to the power x.
		/// </summary>
		/// <param name="x">Value of the exponent.</param>
		/// <returns>Exponential value of x.</returns>
		public static Decimal64 Exp(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Exp(x.Bits));
		}

		/// <summary>
		/// Returns the base-2 exponential function of x, which is 2 raised to the power x.
		/// </summary>
		/// <param name="x">Value of the exponent.</param>
		/// <returns>2 raised to the power of x.</returns>
		public static Decimal64 Exp2(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Exp2(x.Bits));
		}

		/// <summary>
		/// Returns the base-10 exponential function of x, which is 10 raised to the power x.
		/// </summary>
		/// <param name="x">Value of the exponent.</param>
		/// <returns>10 raised to the power of x.</returns>
		public static Decimal64 Exp10(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Exp10(x.Bits));
		}

		/// <summary>
		/// Returns e raised to the power x minus one.
		/// </summary>
		/// <param name="x">Value of the exponent.</param>
		/// <returns>e raised to the power of x, minus one.</returns>
		public static Decimal64 Expm1(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Expm1(x.Bits));
		}

		/// <summary>
		/// Returns the natural logarithm of x.
		/// </summary>
		/// <param name="x">Value whose logarithm is calculated.</param>
		/// <returns>Natural logarithm of x.</returns>
		public static Decimal64 Log(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Log(x.Bits));
		}

		/// <summary>
		/// Returns the binary (base-2) logarithm of x.
		/// </summary>
		/// <param name="x">Value whose logarithm is calculated.</param>
		/// <returns>The binary logarithm of x.</returns>
		public static Decimal64 Log2(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Log2(x.Bits));
		}

		/// <summary>
		/// Returns the common (base-10) logarithm of x.
		/// </summary>
		/// <param name="x">Value whose logarithm is calculated.</param>
		/// <returns>Common logarithm of x.</returns>
		public static Decimal64 Log10(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Log10(x.Bits));
		}

		/// <summary>
		/// Returns the natural logarithm of one plus x: log(1+x).
		/// </summary>
		/// <param name="x">Value whose logarithm is calculated.</param>
		/// <returns>The natural logarithm of (1+x).</returns>
		public static Decimal64 Log1p(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Log1p(x.Bits));
		}

		/// <summary>
		/// Returns base raised to the power exponent.
		/// </summary>
		/// <param name="x">Base value.</param>
		/// <param name="y">Exponent value.</param>
		/// <returns>The result of raising base to the power exponent.</returns>
		public static Decimal64 Pow(Decimal64 x, Decimal64 y)
		{
			return new Decimal64(NativeImpl.bid64Pow(x.Bits, y.Bits));
		}

		/// <summary>
		/// Returns the floating-point remainder of numer/denom.
		/// </summary>
		/// <param name="x">Value of the quotient numerator.</param>
		/// <param name="y">Value of the quotient denominator.</param>
		/// <returns>The remainder of dividing the arguments.</returns>
		public static Decimal64 Fmod(Decimal64 x, Decimal64 y)
		{
			return new Decimal64(NativeImpl.bid64Fmod(x.Bits, y.Bits));
		}

		//OPN(bid64_modf, bid64_modf(x, iptr), BID_UINT64 x, BID_UINT64 *iptr)

		/// <summary>
		/// Returns the hypotenuse of a right-angled triangle whose legs are x and y.
		/// </summary>
		/// <param name="x">The first leg.</param>
		/// <param name="y">The second leg.</param>
		/// <returns>The square root of (x*x+y*y).</returns>
		public static Decimal64 Hypot(Decimal64 x, Decimal64 y)
		{
			return new Decimal64(NativeImpl.bid64Hypot(x.Bits, y.Bits));
		}

		/// <summary>
		/// Returns the sine of an angle of x radians.
		/// </summary>
		/// <param name="x">Value representing an angle expressed in radians.</param>
		/// <returns>Sine of x radians.</returns>
		public static Decimal64 Sin(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Sin(x.Bits));
		}

		/// <summary>
		/// Returns the cosine of an angle of x radians.
		/// </summary>
		/// <param name="x">Value representing an angle expressed in radians.</param>
		/// <returns>Cosine of x radians.</returns>
		public static Decimal64 Cos(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Cos(x.Bits));
		}

		/// <summary>
		/// Returns the tangent of an angle of x radians.
		/// </summary>
		/// <param name="x">Value representing an angle, expressed in radians.</param>
		/// <returns>Tangent of x radians.</returns>
		public static Decimal64 Tan(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Tan(x.Bits));
		}

		/// <summary>
		/// Returns the principal value of the arc sine of x, expressed in radians.
		/// </summary>
		/// <param name="x">Value whose arc sine is computed, in the interval [-1,+1].</param>
		/// <returns>Principal arc sine of x, in the interval [-pi/2,+pi/2] radians.</returns>
		public static Decimal64 Asin(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Asin(x.Bits));
		}

		/// <summary>
		/// Returns the principal value of the arc cosine of x, expressed in radians.
		/// </summary>
		/// <param name="x">Value whose arc cosine is computed, in the interval [-1,+1].</param>
		/// <returns>Principal arc cosine of x, in the interval [0,pi] radians.</returns>
		public static Decimal64 Acos(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Acos(x.Bits));
		}

		/// <summary>
		/// Returns the principal value of the arc tangent of x, expressed in radians.
		/// </summary>
		/// <param name="x">Value whose arc tangent is computed.</param>
		/// <returns>Principal arc tangent of x, in the interval [-pi/2,+pi/2] radians.</returns>
		public static Decimal64 Atan(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Atan(x.Bits));
		}

		/// <summary>
		/// Returns the principal value of the arc tangent of y/x, expressed in radians.
		/// </summary>
		/// <param name="y">Value representing the proportion of the y-coordinate.</param>
		/// <param name="x">Value representing the proportion of the x-coordinate.</param>
		/// <returns>Principal arc tangent of y/x, in the interval [-pi,+pi] radians.</returns>
		public static Decimal64 Atan2(Decimal64 y, Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Atan2(y.Bits, x.Bits));
		}

		/// <summary>
		/// Returns the hyperbolic sine of x.
		/// </summary>
		/// <param name="x">Value representing a hyperbolic angle.</param>
		/// <returns>Hyperbolic sine of x.</returns>
		public static Decimal64 Sinh(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Sinh(x.Bits));
		}

		/// <summary>
		/// Returns the hyperbolic cosine of x.
		/// </summary>
		/// <param name="x">Value representing a hyperbolic angle.</param>
		/// <returns>Hyperbolic cosine of x.</returns>
		public static Decimal64 Cosh(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Cosh(x.Bits));
		}

		/// <summary>
		/// Returns the hyperbolic tangent of x.
		/// </summary>
		/// <param name="x">Value representing a hyperbolic angle.</param>
		/// <returns>Hyperbolic tangent of x.</returns>
		public static Decimal64 Tanh(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Tanh(x.Bits));
		}

		/// <summary>
		/// Returns the area hyperbolic sine of x.
		/// </summary>
		/// <param name="x">Value whose area hyperbolic sine is computed.</param>
		/// <returns>Area hyperbolic sine of x.</returns>
		public static Decimal64 Asinh(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Asinh(x.Bits));
		}

		/// <summary>
		/// Returns the nonnegative area hyperbolic cosine of x.
		/// </summary>
		/// <param name="x">Value whose area hyperbolic cosine is computed.</param>
		/// <returns>Nonnegative area hyperbolic cosine of x, in the interval [0,+INFINITY].</returns>
		public static Decimal64 Acosh(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Acosh(x.Bits));
		}

		/// <summary>
		/// Returns the area hyperbolic tangent of x.
		/// </summary>
		/// <param name="x">Value whose area hyperbolic tangent is computed, in the interval [-1,+1].</param>
		/// <returns>Area hyperbolic tangent of x.</returns>
		public static Decimal64 Atanh(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Atanh(x.Bits));
		}

		/// <summary>
		/// Returns the error function value for x.
		/// </summary>
		/// <param name="x">Parameter for the error function.</param>
		/// <returns>Error function value for x.</returns>
		public static Decimal64 Erf(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Erf(x.Bits));
		}

		/// <summary>
		/// Returns the complementary error function value for x.
		/// </summary>
		/// <param name="x">Parameter for the complementary error function.</param>
		/// <returns>Complementary error function value for x.</returns>
		public static Decimal64 Erfc(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Erfc(x.Bits));
		}

		/// <summary>
		/// Returns the gamma function of x.
		/// </summary>
		/// <param name="x">Parameter for the gamma function.</param>
		/// <returns>Gamma function of x.</returns>
		public static Decimal64 Tgamma(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Tgamma(x.Bits));
		}

		/// <summary>
		/// Returns the natural logarithm of the absolute value of the gamma function of x.
		/// </summary>
		/// <param name="x">Parameter for the log-gamma function.</param>
		/// <returns>Log-gamma function of x.</returns>
		public static Decimal64 Lgamma(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Lgamma(x.Bits));
		}

		//public static Decimal64 Add(Decimal64 x, Decimal64 y) { return new Decimal64(NativeImpl.bid64Add(x.Bits, y.Bits)); }

		//public static Decimal64 Sub(Decimal64 x, Decimal64 y) { return new Decimal64(NativeImpl.bid64Sub(x.Bits, y.Bits)); }

		//public static Decimal64 Mul(Decimal64 x, Decimal64 y) { return new Decimal64(NativeImpl.bid64Mul(x.Bits, y.Bits)); }

		//public static Decimal64 Div(Decimal64 x, Decimal64 y) { return new Decimal64(NativeImpl.bid64Div(x.Bits, y.Bits)); }

		/// <summary>
		/// Decimal floating-point fused multiply-add: x*y+z
		/// </summary>
		/// <param name="x">Values to be multiplied.</param>
		/// <param name="y">Values to be multiplied.</param>
		/// <param name="z">Value to be added.</param>
		/// <returns>The result of x*y+z</returns>
		public static Decimal64 Fma(Decimal64 x, Decimal64 y, Decimal64 z)
		{
			return new Decimal64(NativeImpl.bid64Fma(x.Bits, y.Bits, z.Bits));
		}

		/// <summary>
		/// Decimal floating-point square root.
		/// </summary>
		/// <param name="x">Value whose square root is computed.</param>
		/// <returns>Square root of x.</returns>
		public static Decimal64 Sqrt(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Sqrt(x.Bits));
		}

		/// <summary>
		/// Returns the cubic root of x.
		/// </summary>
		/// <param name="x">Value whose cubic root is computed.</param>
		/// <returns>Cubic root of x.</returns>
		public static Decimal64 Cbrt(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Cbrt(x.Bits));
		}

		//public bool IsEqual(Decimal64 y) { return NativeImpl.bid64IsEqual(Bits, y.Bits); }

		//public bool IsGreater(Decimal64 y) { return NativeImpl.bid64IsGreater(Bits, y.Bits); }

		//public bool IsGreaterEqual(Decimal64 y) { return NativeImpl.bid64IsGreaterEqual(Bits, y.Bits); }

		/// <summary>
		/// Compare 64-bit decimal floating-point numbers for specified relation.
		/// </summary>
		/// <param name="y">Second decimal number.</param>
		/// <returns>The comparison sign.</returns>
		public static bool IsGreaterUnordered(this Decimal64 x, Decimal64 y)
		{
			return NativeImpl.bid64QuietGreaterUnordered(x.Bits, y.Bits);
		}

		//public bool IsLess(Decimal64 y) { return NativeImpl.bid64IsLess(Bits, y.Bits); }

		//public bool IsLessEqual(Decimal64 y) { return NativeImpl.bid64IsLessEqual(Bits, y.Bits); }

		/// <summary>
		/// Compare 64-bit decimal floating-point numbers for specified relation.
		/// </summary>
		/// <param name="y">Second decimal number.</param>
		/// <returns>The comparison sign.</returns>
		public static bool IsLessUnordered(this Decimal64 x, Decimal64 y)
		{
			return NativeImpl.bid64QuietLessUnordered(x.Bits, y.Bits);
		}

		//public bool IsNotEqual(Decimal64 y) { return NativeImpl.bid64IsNotEqual(Bits, y.Bits); }

		/// <summary>
		/// Compare 64-bit decimal floating-point numbers for specified relation.
		/// </summary>
		/// <param name="y">Second decimal number.</param>
		/// <returns>The comparison sign.</returns>
		public static bool IsNotGreater(this Decimal64 x, Decimal64 y)
		{
			return NativeImpl.bid64QuietNotGreater(x.Bits, y.Bits);
		}

		/// <summary>
		/// Compare 64-bit decimal floating-point numbers for specified relation.
		/// </summary>
		/// <param name="y">Second decimal number.</param>
		/// <returns>The comparison sign.</returns>
		public static bool IsNotLess(this Decimal64 x, Decimal64 y)
		{
			return NativeImpl.bid64QuietNotLess(x.Bits, y.Bits);
		}

		/// <summary>
		/// These function return a <c>true</c> value if both arguments are not NaN, otherwise  <c>false</c>.
		/// </summary>
		/// <param name="y">Second decimal number.</param>
		/// <returns><c>true</c> if both arguments are not NaN.</returns>
		public static bool IsOrdered(this Decimal64 x, Decimal64 y)
		{
			return NativeImpl.bid64QuietOrdered(x.Bits, y.Bits);
		}

		/// <summary>
		/// These function return a <c>true</c> value if either argument is NaN, otherwise <c>false</c>.
		/// </summary>
		/// <param name="y">Second decimal number.</param>
		/// <returns><c>true</c> if either argument is NaN.</returns>
		public static bool IsUnordered(this Decimal64 x, Decimal64 y)
		{
			return NativeImpl.bid64QuietUnordered(x.Bits, y.Bits);
		}

		/// <summary>
		/// Round 64-bit decimal floating-point value to integral-valued decimal
		/// floating-point value in the same format, using the current rounding mode;
		/// </summary>
		/// <param name="x">Rounding number.</param>
		/// <returns>The rounded value.</returns>
		public static Decimal64 RoundIntegralExact(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64RoundIntegralExact(x.Bits));
		}

		/// <summary>
		/// Round 64-bit decimal floating-point value to integral-valued decimal
		/// floating-point value in the same format, using the rounding-to-nearest-even mode;
		/// </summary>
		/// <param name="x">Rounding number.</param>
		/// <returns>The rounded value.</returns>
		public static Decimal64 RoundIntegralNearestEven(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64RoundIntegralNearestEven(x.Bits));
		}

		/// <summary>
		/// Round 64-bit decimal floating-point value to integral-valued decimal
		/// floating-point value in the same format, using the rounding-down mode;
		/// </summary>
		/// <param name="x">Rounding number.</param>
		/// <returns>The rounded value.</returns>
		public static Decimal64 RoundIntegralNegative(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64RoundIntegralNegative(x.Bits));
		}

		/// <summary>
		/// Round 64-bit decimal floating-point value to integral-valued decimal
		/// floating-point value in the same format, using the rounding-up mode;
		/// </summary>
		/// <param name="x">Rounding number.</param>
		/// <returns>The rounded value.</returns>
		public static Decimal64 RoundIntegralPositive(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64RoundIntegralPositive(x.Bits));
		}

		/// <summary>
		/// Round 64-bit decimal floating-point value to integral-valued decimal
		/// floating-point value in the same format, using the rounding-to-zero  mode;
		/// </summary>
		/// <param name="x">Rounding number.</param>
		/// <returns>The rounded value.</returns>
		public static Decimal64 RoundIntegralZero(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64RoundIntegralZero(x.Bits));
		}

		/// <summary>
		/// Round 64-bit decimal floating-point value to integral-valued decimal
		/// floating-point value in the same format, using the rounding-to-nearest-away mode;
		/// </summary>
		/// <param name="x">Rounding number.</param>
		/// <returns>The rounded value.</returns>
		public static Decimal64 RoundIntegralNearestAway(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64RoundIntegralNearestAway(x.Bits));
		}

		public static Decimal64 NextUp(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Nextup(x.Bits));
		}

		public static Decimal64 NextDown(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Nextdown(x.Bits));
		}

		/// <summary>
		/// Returns the next 64-bit decimal floating-point number that neighbors
		/// the first operand in the direction toward the second operand.
		/// </summary>
		/// <param name="x">Starting point.</param>
		/// <param name="y">Direction.</param>
		/// <returns>Starting point value adjusted in Direction way.</returns>
		public static Decimal64 NextAfter(Decimal64 x, Decimal64 y)
		{
			return new Decimal64(NativeImpl.bid64Nextafter(x.Bits, y.Bits));
		}

		/// <summary>
		/// Returns the canonicalized floating-point number x if x < y,
		/// y if y < x, the canonicalized floating-point number if one operand is
		/// a floating-point number and the other a quiet NaN.
		/// </summary>
		/// <param name="x">First decimal number.</param>
		/// <param name="y">Second decimal number.</param>
		/// <returns>The minimal value.</returns>
		public static Decimal64 MinNum(Decimal64 x, Decimal64 y)
		{
			return new Decimal64(NativeImpl.bid64Minnum(x.Bits, y.Bits));
		}

		/// <summary>
		/// Returns the canonicalized floating-point number x if |x| < |y|,
		/// y if |y| < |x|, otherwise this function is identical to <see cref="MinNum(Decimal64, Decimal64)"/>.
		/// </summary>
		/// <param name="x">First decimal number.</param>
		/// <param name="y">Second decimal number.</param>
		/// <returns>The value with minimal magnitude.</returns>
		public static Decimal64 MinNumMag(Decimal64 x, Decimal64 y)
		{
			return new Decimal64(NativeImpl.bid64MinnumMag(x.Bits, y.Bits));
		}

		/// <summary>
		/// Returns the canonicalized floating-point number y if x < y,
		/// x if y < x, the canonicalized floating-point number if one operand is a
		/// floating-point number and the other a quiet NaN. Otherwise it is either x
		/// or y, canonicalized.
		/// </summary>
		/// <param name="x">First decimal number.</param>
		/// <param name="y">Second decimal number.</param>
		/// <returns>The maximal value.</returns>
		public static Decimal64 MaxNum(Decimal64 x, Decimal64 y)
		{
			return new Decimal64(NativeImpl.bid64Maxnum(x.Bits, y.Bits));
		}

		/// <summary>
		/// Returns the canonicalized floating-point number x if |x| > |y|,
		/// y if |y| > |x|, otherwise this function is identical to <see cref="MaxNum(Decimal64, Decimal64)"/>.
		/// </summary>
		/// <param name="x">First decimal number.</param>
		/// <param name="y">Second decimal number.</param>
		/// <returns>The value with maximal magnitude.</returns>
		public static Decimal64 MaxNumMag(Decimal64 x, Decimal64 y)
		{
			return new Decimal64(NativeImpl.bid64MaxnumMag(x.Bits, y.Bits));
		}

		/// <summary>
		/// Convert 32-bit signed integer to 64-bit decimal floating-point number.
		/// </summary>
		/// <param name="x">Value to convert.</param>
		/// <returns>The converted value.</returns>
		public static Decimal64 FromInt32(int x)
		{
			return new Decimal64(NativeImpl.bid64FromInt32(x));
		}

		/// <summary>
		/// Convert 32-bit unsigned integer to 64-bit decimal floating-point.
		/// </summary>
		/// <param name="x">Value to convert.</param>
		/// <returns>The converted value.</returns>
		public static Decimal64 FromUInt32(uint x)
		{
			return new Decimal64(NativeImpl.bid64FromUint32(x));
		}

		/// <summary>
		/// Convert 64-bit signed integer to 64-bit decimal floating-point number.
		/// </summary>
		/// <param name="x">Value to convert.</param>
		/// <returns>The converted value.</returns>
		public static Decimal64 FromInt64(long x)
		{
			return new Decimal64(NativeImpl.bid64FromInt64(x));
		}

		/// <summary>
		/// Convert 64-bit unsigned integer to 64-bit decimal floating-point.
		/// </summary>
		/// <param name="x">Value to convert.</param>
		/// <returns>The converted value.</returns>
		public static Decimal64 FromUInt64(ulong x)
		{
			return new Decimal64(NativeImpl.bid64FromUint64(x));
		}

		/// <summary>
		/// Return <c>true</c> if and only if x is subnormal.
		/// </summary>
		/// <returns>The check flag.</returns>
		public static bool IsSubnormal(this Decimal64 x)
		{
			return NativeImpl.bid64IsSubnormal(x.Bits);
		}

		/// <summary>
		/// Return <c>true</c> if and only if x is a signaling NaN.
		/// </summary>
		/// <returns>The check flag.</returns>
		public static bool IsSignaling(this Decimal64 x)
		{
			return NativeImpl.bid64IsSignaling(x.Bits);
		}

		/// <summary>
		/// Return <c>true</c> if and only if x is a finite number, infinity, or
		/// NaN that is canonical.
		/// </summary>
		/// <returns>The check flag.</returns>
		public static bool IsCanonical(this Decimal64 x)
		{
			return NativeImpl.bid64IsCanonical(x.Bits);
		}

		public static bool IsNaN(Decimal64 x)
		{
			return NativeImpl.bid64IsNaN(x.Bits);
		}

		public static bool IsInf(Decimal64 x)
		{
			return NativeImpl.bid64IsInf(x.Bits);
		}

		public static Decimal64 Abs(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Abs(x.Bits));
		}

		/// <summary>
		/// Copies a 64-bit decimal floating-point operand x to a destination
		/// in the same format as x, but with the sign of y.
		/// </summary>
		/// <param name="x">Magnitude value.</param>
		/// <param name="y">Sign value.</param>
		/// <returns>Combined value.</returns>
		public static Decimal64 CopySign(Decimal64 x, Decimal64 y)
		{
			return new Decimal64(NativeImpl.bid64CopySign(x.Bits, y.Bits));
		}

		/// <summary>
		/// Tells which of the following ten classes x falls into (details in
		/// the IEEE Standard 754-2008): signalingNaN, quietNaN, negativeInfinity,
		/// negativeNormal, negativeSubnormal, negativeZero, positiveZero,
		/// positiveSubnormal, positiveNormal, positiveInfinity.
		/// </summary>
		/// <param name="x">Test value.</param>
		/// <returns>The value class.</returns>
		public static int ClassOfValue(Decimal64 x)
		{
			return NativeImpl.bid64Class(x.Bits);
		}

		/// <summary>
		/// sameQuantum(x, y) is <c>true</c> if the exponents of x and y are the same,
		/// and <c>false</c> otherwise; sameQuantum(NaN, NaN) and sameQuantum(inf, inf) are
		/// <c>true</c>; if exactly one operand is infinite or exactly one operand is NaN,
		/// sameQuantum is <c>false</c>.
		/// </summary>
		/// <param name="x">First decimal value.</param>
		/// <param name="y">Second decimal value.</param>
		/// <returns>Comparison flag.</returns>
		public static bool IsSameQuantum(Decimal64 x, Decimal64 y)
		{
			return NativeImpl.bid64SameQuantum(x.Bits, y.Bits);
		}

		/// <summary>
		/// Return <c>true</c> if x and y are ordered (see the IEEE Standard 754-2008).
		/// </summary>
		/// <param name="x">First decimal value.</param>
		/// <param name="y">Second decimal value.</param>
		/// <returns>Comparison flag.</returns>
		public static bool IsTotalOrder(Decimal64 x, Decimal64 y)
		{
			return NativeImpl.bid64TotalOrder(x.Bits, y.Bits);
		}

		/// <summary>
		/// Return <c>true</c> if the absolute values of x and y are ordered
		/// (see the IEEE Standard 754-2008)
		/// </summary>
		/// <param name="x">First decimal value.</param>
		/// <param name="y">Second decimal value.</param>
		/// <returns>Comparison flag.</returns>
		public static bool IsTotalOrderMag(Decimal64 x, Decimal64 y)
		{
			return NativeImpl.bid64TotalOrderMag(x.Bits, y.Bits);
		}

		/// <summary>
		/// Return the radix b of the format of x, 2 or 10.
		/// </summary>
		/// <param name="x">The test value.</param>
		/// <returns>The value radix.</returns>
		public static int Radix(Decimal64 x)
		{
			return NativeImpl.bid64Radix(x.Bits);
		}

		/// <summary>
		/// Decimal floating-point remainder.
		/// </summary>
		/// <param name="x">Value of the quotient numerator.</param>
		/// <param name="y">Value of the quotient denominator.</param>
		/// <returns>The remainder of dividing the arguments.</returns>
		public static Decimal64 Rem(Decimal64 x, Decimal64 y)
		{
			return new Decimal64(NativeImpl.bid64Rem(x.Bits, y.Bits));
		}

		/// <summary>
		/// Returns the exponent e of x, a signed integral value, determined
		/// as though x were represented with infinite range and minimum exponent.
		/// </summary>
		/// <param name="x">Value whose ilogb is returned.</param>
		/// <returns>The integral part of the logarithm of |x|.</returns>
		public static int Ilogb(Decimal64 x)
		{
			return NativeImpl.bid64Ilogb(x.Bits);
		}

		/// <summary>
		/// Returns x * 10^N.
		/// </summary>
		/// <param name="x">The mantissa part.</param>
		/// <param name="n">The exponent part.</param>
		/// <returns>The combined value.</returns>
		public static Decimal64 Scalbn(Decimal64 x, int n)
		{
			return new Decimal64(NativeImpl.bid64Scalbn(x.Bits, n));
		}

		/// <summary>
		/// Returns the result of multiplying x (the significand) by 10 raised to the power of exp (the exponent).
		/// </summary>
		/// <param name="x">Floating point value representing the significand.</param>
		/// <param name="n">Value of the exponent.</param>
		/// <returns>The x*10^exp value.</returns>
		public static Decimal64 Ldexp(Decimal64 x, int n)
		{
			return new Decimal64(NativeImpl.bid64Ldexp(x.Bits, n));
		}

		/// <summary>
		/// Quantize(x, y) is a floating-point number in the same format that
		/// has, if possible, the same numerical value as x and the same quantum
		/// (unit-in-the-last-place) as y. If the exponent is being increased, rounding
		/// according to the prevailing rounding-direction mode might occur: the result
		/// is a different floating-point representation and inexact is signaled if the
		/// result does not have the same numerical value as x. If the exponent is being
		/// decreased and the significand of the result would have more than 16 digits,
		/// invalid is signaled and the result is NaN. If one or both operands are NaN
		/// the rules for NaNs are followed. Otherwise if only one operand is
		/// infinite then invalid is signaled and the result is NaN. If both operands
		/// are infinite then the result is canonical infinity with the sign of x.
		/// </summary>
		/// <param name="x">The value for quantization.</param>
		/// <param name="y">The value for quantum.</param>
		/// <returns>The quantized value.</returns>
		public static Decimal64 Quantize(Decimal64 x, Decimal64 y)
		{
			return new Decimal64(NativeImpl.bid64Quantize(x.Bits, y.Bits));
		}

		/// <summary>
		/// Convert 64-bit decimal floating-point value (binary encoding)
		/// to 32-bit binary floating-point format.
		/// </summary>
		/// <returns>The converted value.</returns>
		public static float ToBinary32(this Decimal64 x)
		{
			return NativeImpl.bid64ToBinary32(x.Bits);
		}

		/// <summary>
		/// Convert 64-bit decimal floating-point value (binary encoding)
		/// to 64-bit binary floating-point format.
		/// </summary>
		/// <returns>The converted value.</returns>
		public static double ToBinary64(this Decimal64 x)
		{
			return NativeImpl.bid64ToBinary64(x.Bits);
		}

		/// <summary>
		/// Returns the adjusted exponent of the absolute value.
		/// </summary>
		/// <param name="x">Value whose logarithm is calculated.</param>
		/// <returns>The adjusted logarithm of |x|.</returns>
		public static Decimal64 Logb(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Logb(x.Bits));
		}

		/// <summary>
		/// Rounds the floating-point argument arg to an integer value in floating-point format, using the current rounding mode.
		/// </summary>
		/// <param name="x">Value to round.</param>
		/// <returns>The rounded value.</returns>
		public static Decimal64 NearByInt(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Nearbyint(x.Bits));
		}

		/// <summary>
		/// Returns the positive difference between x and y, that is, if x > y, returns x-y, otherwise (if x <= y), returns +0.
		/// </summary>
		/// <param name="x">Minuend value.</param>
		/// <param name="y">Subtrahend value.</param>
		/// <returns>The positive difference.</returns>
		public static Decimal64 Fdim(Decimal64 x, Decimal64 y)
		{
			return new Decimal64(NativeImpl.bid64Fdim(x.Bits, y.Bits));
		}

		/// <summary>
		/// The function compute the quantum exponent of a finite argument. The numerical value of a finite number
		/// is given by: (-1)^sign x coefficient x 10^exponent. The quantum of a finite number is given by
		/// 1 x 10^exponent and represents the value of a unit in the least significant position of the coefficient
		/// of a finite number. The quantum exponent is the exponent of the quantum (represented by exponent above).
		/// </summary>
		/// <param name="x">The value for operation.</param>
		/// <returns>The quantum exponent.</returns>
		public static int QuantExp(Decimal64 x)
		{
			return NativeImpl.bid64Quantexp(x.Bits);
		}

		/// <summary>
		/// The function compute the quantum exponent of a finite argument. The numerical value of a finite number
		/// is given by: (-1)^sign x coefficient x 10^exponent. The quantum of a finite number is given by
		/// 1 x 10^exponent and represents the value of a unit in the least significant position of the coefficient
		/// of a finite number. The quantum exponent is the exponent of the quantum (represented by exponent above).
		/// </summary>
		/// <param name="x">The value for operation.</param>
		/// <returns>The quantum.</returns>
		public static Decimal64 Quantum(Decimal64 x)
		{
			return new Decimal64(NativeImpl.bid64Quantum(x.Bits));
		}
	}
}
