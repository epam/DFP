package com.epam.deltix.dfp;

public class Decimal64Math {
    private Decimal64Math() {
    }

    /**
     * Returns the base-e exponential function of x, which is e raised to the power x.
     *
     * @param x Value of the exponent.
     * @return Exponential value of x.
     */
    public static Decimal64 exp(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.exp(x.value));
    }

    /**
     * Returns the base-2 exponential function of x, which is 2 raised to the power x.
     *
     * @param x Value of the exponent.
     * @return 2 raised to the power of x.
     */
    public static Decimal64 exp2(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.exp2(x.value));
    }

    /**
     * Returns the base-10 exponential function of x, which is 10 raised to the power x.
     *
     * @param x Value of the exponent.
     * @return 10 raised to the power of x.
     */
    public static Decimal64 exp10(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.exp10(x.value));
    }

    /**
     * Returns e raised to the power x minus one.
     *
     * @param x Value of the exponent.
     * @return e raised to the power of x, minus one.
     */
    public static Decimal64 expm1(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.expm1(x.value));
    }

    /**
     * Returns the natural logarithm of x.
     *
     * @param x Value whose logarithm is calculated.
     * @return Natural logarithm of x.
     */
    public static Decimal64 log(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.log(x.value));
    }

    /**
     * Returns the binary (base-2) logarithm of x.
     *
     * @param x Value whose logarithm is calculated.
     * @return The binary logarithm of x.
     */
    public static Decimal64 log2(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.log2(x.value));
    }

    /**
     * Returns the common (base-10) logarithm of x.
     *
     * @param x Value whose logarithm is calculated.
     * @return Common logarithm of x.
     */
    public static Decimal64 log10(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.log10(x.value));
    }

    /**
     * Returns the natural logarithm of one plus x: log(1+x).
     *
     * @param x Value whose logarithm is calculated.
     * @return The natural logarithm of (1+x).
     */
    public static Decimal64 log1p(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.log1p(x.value));
    }

    /**
     * Returns base raised to the power exponent.
     *
     * @param x Base value.
     * @param y Exponent value.
     * @return The result of raising base to the power exponent.
     */
    public static Decimal64 pow(final Decimal64 x, final Decimal64 y) {
        return new Decimal64(Decimal64Utils.pow(x.value, y.value));
    }

    /**
     * Returns the floating-point remainder of numer/denom.
     *
     * @param x Value of the quotient numerator.
     * @param y Value of the quotient denominator.
     * @return The remainder of dividing the arguments.
     */
    public static Decimal64 fmod(final Decimal64 x, final Decimal64 y) {
        return new Decimal64(Decimal64Utils.fmod(x.value, y.value));
    }

    //OPN(bid64_modf, bid64_modf(x, iptr), BID_UINT64 x, BID_UINT64 *iptr)

    /**
     * Returns the hypotenuse of a right-angled triangle whose legs are x and y.
     *
     * @param x The first leg.
     * @param y The second leg.
     * @return The square root of (x*x+y*y).
     */
    public static Decimal64 hypot(final Decimal64 x, final Decimal64 y) {
        return new Decimal64(Decimal64Utils.hypot(x.value, y.value));
    }

    /**
     * Returns the sine of an angle of x radians.
     *
     * @param x Value representing an angle expressed in radians.
     * @return Sine of x radians.
     */
    public static Decimal64 sin(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.sin(x.value));
    }

    /**
     * Returns the cosine of an angle of x radians.
     *
     * @param x Value representing an angle expressed in radians.
     * @return Cosine of x radians.
     */
    public static Decimal64 cos(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.cos(x.value));
    }

    /**
     * Returns the tangent of an angle of x radians.
     *
     * @param x Value representing an angle, expressed in radians.
     * @return Tangent of x radians.
     */
    public static Decimal64 tan(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.tan(x.value));
    }

    /**
     * Returns the principal value of the arc sine of x, expressed in radians.
     *
     * @param x Value whose arc sine is computed, in the interval [-1,+1].
     * @return Principal arc sine of x, in the interval [-pi/2,+pi/2] radians.
     */
    public static Decimal64 asin(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.asin(x.value));
    }

    /**
     * Returns the principal value of the arc cosine of x, expressed in radians.
     *
     * @param x Value whose arc cosine is computed, in the interval [-1,+1].
     * @return Principal arc cosine of x, in the interval [0,pi] radians.
     */
    public static Decimal64 acos(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.acos(x.value));
    }

    /**
     * Returns the principal value of the arc tangent of x, expressed in radians.
     *
     * @param x Value whose arc tangent is computed.
     * @return Principal arc tangent of x, in the interval [-pi/2,+pi/2] radians.
     */
    public static Decimal64 atan(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.atan(x.value));
    }

    /**
     * Returns the principal value of the arc tangent of y/x, expressed in radians.
     *
     * @param y Value representing the proportion of the y-coordinate.
     * @param x Value representing the proportion of the x-coordinate.
     * @return Principal arc tangent of y/x, in the interval [-pi,+pi] radians.
     */
    public static Decimal64 atan2(final Decimal64 y, final Decimal64 x) {
        return new Decimal64(Decimal64Utils.atan2(y.value, x.value));
    }

    /**
     * Returns the hyperbolic sine of x.
     *
     * @param x Value representing a hyperbolic angle.
     * @return Hyperbolic sine of x.
     */
    public static Decimal64 sinh(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.sinh(x.value));
    }

    /**
     * Returns the hyperbolic cosine of x.
     *
     * @param x Value representing a hyperbolic angle.
     * @return Hyperbolic cosine of x.
     */
    public static Decimal64 cosh(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.cosh(x.value));
    }

    /**
     * Returns the hyperbolic tangent of x.
     *
     * @param x Value representing a hyperbolic angle.
     * @return Hyperbolic tangent of x.
     */
    public static Decimal64 tanh(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.tanh(x.value));
    }

    /**
     * Returns the area hyperbolic sine of x.
     *
     * @param x Value whose area hyperbolic sine is computed.
     * @return Area hyperbolic sine of x.
     */
    public static Decimal64 asinh(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.asinh(x.value));
    }

    /**
     * Returns the nonnegative area hyperbolic cosine of x.
     *
     * @param x Value whose area hyperbolic cosine is computed.
     * @return Nonnegative area hyperbolic cosine of x, in the interval [0,+INFINITY].
     */
    public static Decimal64 acosh(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.acosh(x.value));
    }

    /**
     * Returns the area hyperbolic tangent of x.
     *
     * @param x Value whose area hyperbolic tangent is computed, in the interval [-1,+1].
     * @return Area hyperbolic tangent of x.
     */
    public static Decimal64 atanh(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.atanh(x.value));
    }

    /**
     * Returns the error function value for x.
     *
     * @param x Parameter for the error function.
     * @return Error function value for x.
     */
    public static Decimal64 erf(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.erf(x.value));
    }

    /**
     * Returns the complementary error function value for x.
     *
     * @param x Parameter for the complementary error function.
     * @return Complementary error function value for x.
     */
    public static Decimal64 erfc(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.erfc(x.value));
    }

    /**
     * Returns the gamma function of x.
     *
     * @param x Parameter for the gamma function.
     * @return Gamma function of x.
     */
    public static Decimal64 tgamma(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.tgamma(x.value));
    }

    /**
     * Returns the natural logarithm of the absolute value of the gamma function of x.
     *
     * @param x Parameter for the log-gamma function.
     * @return Log-gamma function of x.
     */
    public static Decimal64 lgamma(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.lgamma(x.value));
    }

//public static Decimal64 add(final Decimal64 x, final Decimal64 y) { return new Decimal64(Decimal64Utils.add(x.value, y.value)); }

//public static Decimal64 sub(final Decimal64 x, final Decimal64 y) { return new Decimal64(Decimal64Utils.sub(x.value, y.value)); }

//public static Decimal64 mul(final Decimal64 x, final Decimal64 y) { return new Decimal64(Decimal64Utils.mul(x.value, y.value)); }

//public static Decimal64 div(final Decimal64 x, final Decimal64 y) { return new Decimal64(Decimal64Utils.div(x.value, y.value)); }

    /**
     * Decimal floating-point fused multiply-add: x*y+z
     *
     * @param x Values to be multiplied.
     * @param y Values to be multiplied.
     * @param z Value to be added.
     * @return The result of x*y+z
     */
    public static Decimal64 fma(final Decimal64 x, final Decimal64 y, final Decimal64 z) {
        return new Decimal64(Decimal64Utils.fma(x.value, y.value, z.value));
    }

    /**
     * Decimal floating-point square root.
     *
     * @param x Value whose square root is computed.
     * @return Square root of x.
     */
    public static Decimal64 sqrt(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.sqrt(x.value));
    }

    /**
     * Returns the cubic root of x.
     *
     * @param x Value whose cubic root is computed.
     * @return Cubic root of x.
     */
    public static Decimal64 cbrt(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.cbrt(x.value));
    }

    /**
     * Round 64-bit decimal floating-point value to integral-valued decimal
     * floating-point value in the same format, using the current rounding mode;
     *
     * @param x Rounding number.
     * @return The rounded value.
     */
    public static Decimal64 roundIntegralExact(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.roundIntegralExact(x.value));
    }

    /**
     * Round 64-bit decimal floating-point value to integral-valued decimal
     * floating-point value in the same format, using the rounding-to-nearest-even mode;
     *
     * @param x Rounding number.
     * @return The rounded value.
     */
    public static Decimal64 roundIntegralNearestEven(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.roundIntegralNearestEven(x.value));
    }

    /**
     * Round 64-bit decimal floating-point value to integral-valued decimal
     * floating-point value in the same format, using the rounding-down mode;
     *
     * @param x Rounding number.
     * @return The rounded value.
     */
    public static Decimal64 roundIntegralNegative(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.roundIntegralNegative(x.value));
    }

    /**
     * Round 64-bit decimal floating-point value to integral-valued decimal
     * floating-point value in the same format, using the rounding-up mode;
     *
     * @param x Rounding number.
     * @return The rounded value.
     */
    public static Decimal64 roundIntegralPositive(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.roundIntegralPositive(x.value));
    }

    /**
     * Round 64-bit decimal floating-point value to integral-valued decimal
     * floating-point value in the same format, using the rounding-to-zero  mode;
     *
     * @param x Rounding number.
     * @return The rounded value.
     */
    public static Decimal64 roundIntegralZero(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.roundIntegralZero(x.value));
    }

    /**
     * Round 64-bit decimal floating-point value to integral-valued decimal
     * floating-point value in the same format, using the rounding-to-nearest-away mode;
     *
     * @param x Rounding number.
     * @return The rounded value.
     */
    public static Decimal64 roundIntegralNearestAway(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.roundIntegralNearestAway(x.value));
    }

    public static Decimal64 nextUp(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.nextUp(x.value));
    }

    public static Decimal64 nextDown(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.nextDown(x.value));
    }

    /**
     * Returns the next 64-bit decimal floating-point number that neighbors
     * the first operand in the direction toward the second operand.
     *
     * @param x Starting point.
     * @param y Direction.
     * @return Starting point value adjusted in Direction way.
     */
    public static Decimal64 nextAfter(final Decimal64 x, final Decimal64 y) {
        return new Decimal64(Decimal64Utils.nextAfter(x.value, y.value));
    }

    /**
     * Returns the canonicalized floating-point number x if x &lt; y,
     * y if y &lt; x, the canonicalized floating-point number if one operand is
     * a floating-point number and the other a quiet NaN.
     *
     * @param x First decimal number.
     * @param y Second decimal number.
     * @return The minimal value.
     */
    public static Decimal64 minNum(final Decimal64 x, final Decimal64 y) {
        return new Decimal64(Decimal64Utils.minNum(x.value, y.value));
    }

    /**
     * Returns the canonicalized floating-point number x if |x| &lt; |y|,
     * y if |y| &lt; |x|, otherwise this function is identical to {@link #minNum(Decimal64, Decimal64)}.
     *
     * @param x First decimal number.
     * @param y Second decimal number.
     * @return The value with minimal magnitude.
     */
    public static Decimal64 minNumMag(final Decimal64 x, final Decimal64 y) {
        return new Decimal64(Decimal64Utils.minNumMag(x.value, y.value));
    }

    /**
     * Returns the canonicalized floating-point number y if x &lt; y,
     * x if y &lt; x, the canonicalized floating-point number if one operand is a
     * floating-point number and the other a quiet NaN. Otherwise it is either x
     * or y, canonicalized.
     *
     * @param x First decimal number.
     * @param y Second decimal number.
     * @return The maximal value.
     */
    public static Decimal64 maxNum(final Decimal64 x, final Decimal64 y) {
        return new Decimal64(Decimal64Utils.maxNum(x.value, y.value));
    }

    /**
     * Returns the canonicalized floating-point number x if |x| &gt; |y|,
     * y if |y| &gt; |x|, otherwise this function is identical to {@link #maxNum(Decimal64, Decimal64)}.
     *
     * @param x First decimal number.
     * @param y Second decimal number.
     * @return The value with maximal magnitude.
     */
    public static Decimal64 maxNumMag(final Decimal64 x, final Decimal64 y) {
        return new Decimal64(Decimal64Utils.maxNumMag(x.value, y.value));
    }

    /**
     * Convert 32-bit signed integer to 64-bit decimal floating-point number.
     *
     * @param x Value to convert.
     * @return The converted value.
     */
    public static Decimal64 fromInt32(final int x) {
        return new Decimal64(Decimal64Utils.fromInt32(x));
    }

    /**
     * Convert 32-bit unsigned integer to 64-bit decimal floating-point.
     *
     * @param x Value to convert.
     * @return The converted value.
     */
    public static Decimal64 fromUInt32(final int x) {
        return new Decimal64(Decimal64Utils.fromUInt32(x));
    }

    /**
     * Convert 64-bit signed integer to 64-bit decimal floating-point number.
     *
     * @param x Value to convert.
     * @return The converted value.
     */
    public static Decimal64 fromInt64(final long x) {
        return new Decimal64(Decimal64Utils.fromInt64(x));
    }

    /**
     * Convert 64-bit unsigned integer to 64-bit decimal floating-point.
     *
     * @param x Value to convert.
     * @return The converted value.
     */
    public static Decimal64 fromUInt64(final long x) {
        return new Decimal64(Decimal64Utils.fromUInt64(x));
    }

    public static boolean isNaN(final Decimal64 x) {
        return Decimal64Utils.isNaN(x.value);
    }

    public static boolean isInf(final Decimal64 x) {
        return Decimal64Utils.isInf(x.value);
    }

    public static Decimal64 abs(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.abs(x.value));
    }

    /**
     * Copies a 64-bit decimal floating-point operand x to a destination
     * in the same format as x, but with the sign of y.
     *
     * @param x Magnitude value.
     * @param y Sign value.
     * @return Combined value.
     */
    public static Decimal64 copySign(final Decimal64 x, final Decimal64 y) {
        return new Decimal64(Decimal64Utils.copySign(x.value, y.value));
    }

    /**
     * Tells which of the following ten classes x falls into (details in
     * the IEEE Standard 754-2008): signalingNaN, quietNaN, negativeInfinity,
     * negativeNormal, negativeSubnormal, negativeZero, positiveZero,
     * positiveSubnormal, positiveNormal, positiveInfinity.
     *
     * @param x Test value.
     * @return The value class.
     */
    public static int classOfValue(final Decimal64 x) {
        return Decimal64Utils.classOfValue(x.value);
    }

    /**
     * sameQuantum(x, y) is {@code true} if the exponents of x and y are the same,
     * and {@code false} otherwise; sameQuantum(NaN, NaN) and sameQuantum(inf, inf) are
     * {@code true}; if exactly one operand is infinite or exactly one operand is NaN,
     * sameQuantum is {@code false}.
     *
     * @param x First decimal value.
     * @param y Second decimal value.
     * @return Comparison flag.
     */
    public static boolean isSameQuantum(final Decimal64 x, final Decimal64 y) {
        return Decimal64Utils.isSameQuantum(x.value, y.value);
    }

    /**
     * Return {@code true} if x and y are ordered (see the IEEE Standard 754-2008).
     *
     * @param x First decimal value.
     * @param y Second decimal value.
     * @return Comparison flag.
     */
    public static boolean isTotalOrder(final Decimal64 x, final Decimal64 y) {
        return Decimal64Utils.isTotalOrder(x.value, y.value);
    }

    /**
     * Return {@code true} if the absolute values of x and y are ordered
     * (see the IEEE Standard 754-2008)
     *
     * @param x First decimal value.
     * @param y Second decimal value.
     * @return Comparison flag.
     */
    public static boolean isTotalOrderMag(final Decimal64 x, final Decimal64 y) {
        return Decimal64Utils.isTotalOrderMag(x.value, y.value);
    }

    /**
     * Return the radix b of the format of x, 2 or 10.
     *
     * @param x The test value.
     * @return The value radix.
     */
    public static int radix(final Decimal64 x) {
        return Decimal64Utils.radix(x.value);
    }

    /**
     * Decimal floating-point remainder.
     *
     * @param x Value of the quotient numerator.
     * @param y Value of the quotient denominator.
     * @return The remainder of dividing the arguments.
     */
    public static Decimal64 rem(final Decimal64 x, final Decimal64 y) {
        return new Decimal64(Decimal64Utils.rem(x.value, y.value));
    }

    /**
     * Returns the exponent e of x, a signed integral value, determined
     * as though x were represented with infinite range and minimum exponent.
     *
     * @param x Value whose ilogb is returned.
     * @return The integral part of the logarithm of |x|.
     */
    public static int ilogb(final Decimal64 x) {
        return Decimal64Utils.ilogb(x.value);
    }

    /**
     * Returns x * 10^N.
     *
     * @param x The mantissa part.
     * @param n The exponent part.
     * @return The combined value.
     */
    public static Decimal64 scalbn(final Decimal64 x, final int n) {
        return new Decimal64(Decimal64Utils.scalbn(x.value, n));
    }

    /**
     * Returns the result of multiplying x (the significand) by 2 raised to the power of exp (the exponent).
     *
     * @param x Floating point value representing the significand.
     * @param n Value of the exponent.
     * @return The x*2^exp value.
     */
    public static Decimal64 ldexp(final Decimal64 x, final int n) {
        return new Decimal64(Decimal64Utils.ldexp(x.value, n));
    }

    /**
     * Quantize(x, y) is a floating-point number in the same format that
     * has, if possible, the same numerical value as x and the same quantum
     * (unit-in-the-last-place) as y. If the exponent is being increased, rounding
     * according to the prevailing rounding-direction mode might occur: the result
     * is a different floating-point representation and inexact is signaled if the
     * result does not have the same numerical value as x. If the exponent is being
     * decreased and the significand of the result would have more than 16 digits,
     * invalid is signaled and the result is NaN. If one or both operands are NaN
     * the rules for NaNs are followed. Otherwise if only one operand is
     * infinite then invalid is signaled and the result is NaN. If both operands
     * are infinite then the result is canonical infinity with the sign of x.
     *
     * @param x The value for quantization.
     * @param y The value for quantum.
     * @return The quantized value.
     */
    public static Decimal64 quantize(final Decimal64 x, final Decimal64 y) {
        return new Decimal64(Decimal64Utils.quantize(x.value, y.value));
    }

    /**
     * Returns the logarithm of |x|.
     *
     * @param x Value whose logarithm is calculated.
     * @return The logarithm of x.
     */
    public static Decimal64 logb(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.logb(x.value));
    }

    /**
     * Rounds the floating-point argument arg to an integer value in floating-point format, using the current rounding mode.
     *
     * @param x Value to round.
     * @return The rounded value.
     */
    public static Decimal64 nearByInt(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.nearByInt(x.value));
    }

    /**
     * Returns the positive difference between x and y, that is, if x &gt; y, returns x-y, otherwise (if x &le; y), returns +0.
     *
     * @param x Minuend value.
     * @param y Subtrahend value.
     * @return The positive difference.
     */
    public static Decimal64 fdim(final Decimal64 x, final Decimal64 y) {
        return new Decimal64(Decimal64Utils.fdim(x.value, y.value));
    }

    /**
     * The function compute the quantum exponent of a finite argument. The numerical value of a finite number
     * is given by: (-1)^sign x coefficient x 10^exponent. The quantum of a finite number is given by
     * 1 x 10^exponent and represents the value of a unit in the least significant position of the coefficient
     * of a finite number. The quantum exponent is the exponent of the quantum (represented by exponent above).
     *
     * @param x The value for operation.
     * @return The quantum exponent.
     */
    public static int quantExp(final Decimal64 x) {
        return Decimal64Utils.quantExp(x.value);
    }

    /**
     * The function compute the quantum exponent of a finite argument. The numerical value of a finite number
     * is given by: (-1)^sign x coefficient x 10^exponent. The quantum of a finite number is given by
     * 1 x 10^exponent and represents the value of a unit in the least significant position of the coefficient
     * of a finite number. The quantum exponent is the exponent of the quantum (represented by exponent above).
     *
     * @param x The value for operation.
     * @return The quantum.
     */
    public static Decimal64 quantum(final Decimal64 x) {
        return new Decimal64(Decimal64Utils.quantum(x.value));
    }
}
