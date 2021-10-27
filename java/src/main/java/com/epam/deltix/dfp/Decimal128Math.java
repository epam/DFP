package com.epam.deltix.dfp;

public class Decimal128Math {
    private Decimal128Math() {
    }

    /**
     * Returns the base-e exponential function of x, which is e raised to the power x.
     *
     * @param x Value of the exponent.
     * @return Exponential value of x.
     */
    public static Decimal128 exp(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.exp(x, r);
        return r;
    }

    /**
     * Returns the base-2 exponential function of x, which is 2 raised to the power x.
     *
     * @param x Value of the exponent.
     * @return 2 raised to the power of x.
     */
    public static Decimal128 exp2(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.exp2(x, r);
        return r;
    }

    /**
     * Returns the base-10 exponential function of x, which is 10 raised to the power x.
     *
     * @param x Value of the exponent.
     * @return 10 raised to the power of x.
     */
    public static Decimal128 exp10(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.exp10(x, r);
        return r;
    }

    /**
     * Returns e raised to the power x minus one.
     *
     * @param x Value of the exponent.
     * @return e raised to the power of x, minus one.
     */
    public static Decimal128 expm1(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.expm1(x, r);
        return r;
    }

    /**
     * Returns the natural logarithm of x.
     *
     * @param x Value whose logarithm is calculated.
     * @return Natural logarithm of x.
     */
    public static Decimal128 log(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.log(x, r);
        return r;
    }

    /**
     * Returns the binary (base-2) logarithm of x.
     *
     * @param x Value whose logarithm is calculated.
     * @return The binary logarithm of x.
     */
    public static Decimal128 log2(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.log2(x, r);
        return r;
    }

    /**
     * Returns the common (base-10) logarithm of x.
     *
     * @param x Value whose logarithm is calculated.
     * @return Common logarithm of x.
     */
    public static Decimal128 log10(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.log10(x, r);
        return r;
    }

    /**
     * Returns the natural logarithm of one plus x: log(1+x).
     *
     * @param x Value whose logarithm is calculated.
     * @return The natural logarithm of (1+x).
     */
    public static Decimal128 log1p(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.log1p(x, r);
        return r;
    }

    /**
     * Returns base raised to the power exponent.
     *
     * @param x Base value.
     * @param y Exponent value.
     * @return The result of raising base to the power exponent.
     */
    public static Decimal128 pow(final Decimal128 x, final Decimal128 y) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.pow(x, y, r);
        return r;
    }

    /**
     * Returns the floating-point remainder of numer/denom.
     *
     * @param x Value of the quotient numerator.
     * @param y Value of the quotient denominator.
     * @return The remainder of dividing the arguments.
     */
    public static Decimal128 fmod(final Decimal128 x, final Decimal128 y) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.fmod(x, y, r);
        return r;
    }

    //OPN(bid64_modf, bid64_modf(x, iptr), BID_UINT64 x, BID_UINT64 *iptr)

    /**
     * Returns the hypotenuse of a right-angled triangle whose legs are x and y.
     *
     * @param x The first leg.
     * @param y The second leg.
     * @return The square root of (x*x+y*y).
     */
    public static Decimal128 hypot(final Decimal128 x, final Decimal128 y) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.hypot(x, y, r);
        return r;
    }

    /**
     * Returns the sine of an angle of x radians.
     *
     * @param x Value representing an angle expressed in radians.
     * @return Sine of x radians.
     */
    public static Decimal128 sin(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.sin(x, r);
        return r;
    }

    /**
     * Returns the cosine of an angle of x radians.
     *
     * @param x Value representing an angle expressed in radians.
     * @return Cosine of x radians.
     */
    public static Decimal128 cos(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.cos(x, r);
        return r;
    }

    /**
     * Returns the tangent of an angle of x radians.
     *
     * @param x Value representing an angle, expressed in radians.
     * @return Tangent of x radians.
     */
    public static Decimal128 tan(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.tan(x, r);
        return r;
    }

    /**
     * Returns the principal value of the arc sine of x, expressed in radians.
     *
     * @param x Value whose arc sine is computed, in the interval [-1,+1].
     * @return Principal arc sine of x, in the interval [-pi/2,+pi/2] radians.
     */
    public static Decimal128 asin(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.asin(x, r);
        return r;
    }

    /**
     * Returns the principal value of the arc cosine of x, expressed in radians.
     *
     * @param x Value whose arc cosine is computed, in the interval [-1,+1].
     * @return Principal arc cosine of x, in the interval [0,pi] radians.
     */
    public static Decimal128 acos(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.acos(x, r);
        return r;
    }

    /**
     * Returns the principal value of the arc tangent of x, expressed in radians.
     *
     * @param x Value whose arc tangent is computed.
     * @return Principal arc tangent of x, in the interval [-pi/2,+pi/2] radians.
     */
    public static Decimal128 atan(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.atan(x, r);
        return r;
    }

    /**
     * Returns the principal value of the arc tangent of y/x, expressed in radians.
     *
     * @param y Value representing the proportion of the y-coordinate.
     * @param x Value representing the proportion of the x-coordinate.
     * @return Principal arc tangent of y/x, in the interval [-pi,+pi] radians.
     */
    public static Decimal128 atan2(final Decimal128 y, final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.atan2(y, x, r);
        return r;
    }

    /**
     * Returns the hyperbolic sine of x.
     *
     * @param x Value representing a hyperbolic angle.
     * @return Hyperbolic sine of x.
     */
    public static Decimal128 sinh(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.sinh(x, r);
        return r;
    }

    /**
     * Returns the hyperbolic cosine of x.
     *
     * @param x Value representing a hyperbolic angle.
     * @return Hyperbolic cosine of x.
     */
    public static Decimal128 cosh(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.cosh(x, r);
        return r;
    }

    /**
     * Returns the hyperbolic tangent of x.
     *
     * @param x Value representing a hyperbolic angle.
     * @return Hyperbolic tangent of x.
     */
    public static Decimal128 tanh(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.tanh(x, r);
        return r;
    }

    /**
     * Returns the area hyperbolic sine of x.
     *
     * @param x Value whose area hyperbolic sine is computed.
     * @return Area hyperbolic sine of x.
     */
    public static Decimal128 asinh(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.asinh(x, r);
        return r;
    }

    /**
     * Returns the nonnegative area hyperbolic cosine of x.
     *
     * @param x Value whose area hyperbolic cosine is computed.
     * @return Nonnegative area hyperbolic cosine of x, in the interval [0,+INFINITY].
     */
    public static Decimal128 acosh(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.acosh(x, r);
        return r;
    }

    /**
     * Returns the area hyperbolic tangent of x.
     *
     * @param x Value whose area hyperbolic tangent is computed, in the interval [-1,+1].
     * @return Area hyperbolic tangent of x.
     */
    public static Decimal128 atanh(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.atanh(x, r);
        return r;
    }

    /**
     * Returns the error function value for x.
     *
     * @param x Parameter for the error function.
     * @return Error function value for x.
     */
    public static Decimal128 erf(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.erf(x, r);
        return r;
    }

    /**
     * Returns the complementary error function value for x.
     *
     * @param x Parameter for the complementary error function.
     * @return Complementary error function value for x.
     */
    public static Decimal128 erfc(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.erfc(x, r);
        return r;
    }

    /**
     * Returns the gamma function of x.
     *
     * @param x Parameter for the gamma function.
     * @return Gamma function of x.
     */
    public static Decimal128 tgamma(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.tgamma(x, r);
        return r;
    }

    /**
     * Returns the natural logarithm of the absolute value of the gamma function of x.
     *
     * @param x Parameter for the log-gamma function.
     * @return Log-gamma function of x.
     */
    public static Decimal128 lgamma(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.lgamma(x, r);
        return r;
    }

    //public static Decimal128 add(final Decimal128 x, final Decimal128 y) { return new Decimal128(Decimal128Utils.add(x.value, y.value)); }

//public static Decimal128 sub(final Decimal128 x, final Decimal128 y) { return new Decimal128(Decimal128Utils.sub(x.value, y.value)); }

//public static Decimal128 mul(final Decimal128 x, final Decimal128 y) { return new Decimal128(Decimal128Utils.mul(x.value, y.value)); }

//public static Decimal128 div(final Decimal128 x, final Decimal128 y) { return new Decimal128(Decimal128Utils.div(x.value, y.value)); }

    /**
     * Decimal floating-point fused multiply-add: x*y+z
     *
     * @param x Values to be multiplied.
     * @param y Values to be multiplied.
     * @param z Value to be added.
     * @return The result of x*y+z
     */
    public static Decimal128 fma(final Decimal128 x, final Decimal128 y, final Decimal128 z) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.fma(x, y, z, r);
        return r;
    }

    /**
     * Decimal floating-point square root.
     *
     * @param x Value whose square root is computed.
     * @return Square root of x.
     */
    public static Decimal128 sqrt(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.sqrt(x, r);
        return r;
    }

    /**
     * Returns the cubic root of x.
     *
     * @param x Value whose cubic root is computed.
     * @return Cubic root of x.
     */
    public static Decimal128 cbrt(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.cbrt(x, r);
        return r;
    }

    /**
     * Round 128-bit decimal floating-point value to integral-valued decimal
     * floating-point value in the same format, using the current rounding mode;
     *
     * @param x Rounding number.
     * @return The rounded value.
     */
    public static Decimal128 roundIntegralExact(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.roundIntegralExact(x, r);
        return r;
    }

    /**
     * Round 128-bit decimal floating-point value to integral-valued decimal
     * floating-point value in the same format, using the rounding-to-nearest-even mode;
     *
     * @param x Rounding number.
     * @return The rounded value.
     */
    public static Decimal128 roundIntegralNearestEven(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.roundIntegralNearestEven(x, r);
        return r;
    }

    /**
     * Round 128-bit decimal floating-point value to integral-valued decimal
     * floating-point value in the same format, using the rounding-down mode;
     *
     * @param x Rounding number.
     * @return The rounded value.
     */
    public static Decimal128 roundIntegralNegative(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.roundIntegralNegative(x, r);
        return r;
    }

    /**
     * Round 128-bit decimal floating-point value to integral-valued decimal
     * floating-point value in the same format, using the rounding-up mode;
     *
     * @param x Rounding number.
     * @return The rounded value.
     */
    public static Decimal128 roundIntegralPositive(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.roundIntegralPositive(x, r);
        return r;
    }

    /**
     * Round 128-bit decimal floating-point value to integral-valued decimal
     * floating-point value in the same format, using the rounding-to-zero  mode;
     *
     * @param x Rounding number.
     * @return The rounded value.
     */
    public static Decimal128 roundIntegralZero(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.roundIntegralZero(x, r);
        return r;
    }

    /**
     * Round 128-bit decimal floating-point value to integral-valued decimal
     * floating-point value in the same format, using the rounding-to-nearest-away mode;
     *
     * @param x Rounding number.
     * @return The rounded value.
     */
    public static Decimal128 roundIntegralNearestAway(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.roundIntegralNearestAway(x, r);
        return r;
    }

//    public static Decimal128 nextUp(final Decimal128 x) {
//        Decimal128 r = new Decimal128();
//        Decimal128Utils.nextUp(x, r);
//        return r;
//    }

//    public static Decimal128 nextDown(final Decimal128 x) {
//        Decimal128 r = new Decimal128();
//        Decimal128Utils.nextDown(x, r);
//        return r;
//    }

    /**
     * Returns the next 128-bit decimal floating-point number that neighbors
     * the first operand in the direction toward the second operand.
     *
     * @param x Starting point.
     * @param y Direction.
     * @return Starting point value adjusted in Direction way.
     */
    public static Decimal128 nextAfter(final Decimal128 x, final Decimal128 y) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.nextAfter(x, y, r);
        return r;
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
    public static Decimal128 minNum(final Decimal128 x, final Decimal128 y) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.minNum(x, y, r);
        return r;
    }

    /**
     * Returns the canonicalized floating-point number x if |x| &lt; |y|,
     * y if |y| &lt; |x|, otherwise this function is identical to {@link #minNum(Decimal128, Decimal128)}.
     *
     * @param x First decimal number.
     * @param y Second decimal number.
     * @return The value with minimal magnitude.
     */
    public static Decimal128 minNumMag(final Decimal128 x, final Decimal128 y) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.minNumMag(x, y, r);
        return r;
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
    public static Decimal128 maxNum(final Decimal128 x, final Decimal128 y) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.maxNum(x, y, r);
        return r;
    }

    /**
     * Returns the canonicalized floating-point number x if |x| &gt; |y|,
     * y if |y| &gt; |x|, otherwise this function is identical to {@link #maxNum(Decimal128, Decimal128)}.
     *
     * @param x First decimal number.
     * @param y Second decimal number.
     * @return The value with maximal magnitude.
     */
    public static Decimal128 maxNumMag(final Decimal128 x, final Decimal128 y) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.maxNumMag(x, y, r);
        return r;
    }

    /**
     * Convert 32-bit signed integer to 128-bit decimal floating-point number.
     *
     * @param x Value to convert.
     * @return The converted value.
     */
    public static Decimal128 fromInt32(final int x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.fromInt32(x, r);
        return r;
    }

    /**
     * Convert 32-bit unsigned integer to 128-bit decimal floating-point.
     *
     * @param x Value to convert.
     * @return The converted value.
     */
    public static Decimal128 fromUInt32(final int x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.fromUInt32(x, r);
        return r;
    }

    /**
     * Convert 64-bit signed integer to 128-bit decimal floating-point number.
     *
     * @param x Value to convert.
     * @return The converted value.
     */
    public static Decimal128 fromInt64(final long x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.fromInt64(x, r);
        return r;
    }

    /**
     * Convert 64-bit unsigned integer to 128-bit decimal floating-point.
     *
     * @param x Value to convert.
     * @return The converted value.
     */
    public static Decimal128 fromUInt64(final long x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.fromUInt64(x, r);
        return r;
    }

    public static boolean isNaN(final Decimal128 x) {
        return Decimal128Utils.isNaN(x);
    }

    public static boolean isInf(final Decimal128 x) {
        return Decimal128MathUtils.isInf(x);
    }

    public static Decimal128 abs(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.abs(x, r);
        return r;
    }

    /**
     * Copies a 128-bit decimal floating-point operand x to a destination
     * in the same format as x, but with the sign of y.
     *
     * @param x Magnitude value.
     * @param y Sign value.
     * @return Combined value.
     */
    public static Decimal128 copySign(final Decimal128 x, final Decimal128 y) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.copySign(x, y, r);
        return r;
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
    public static int classOfValue(final Decimal128 x) {
        return Decimal128MathUtils.classOfValue(x);
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
    public static boolean isSameQuantum(final Decimal128 x, final Decimal128 y) {
        return Decimal128MathUtils.isSameQuantum(x, y);
    }

    /**
     * Return {@code true} if x and y are ordered (see the IEEE Standard 754-2008).
     *
     * @param x First decimal value.
     * @param y Second decimal value.
     * @return Comparison flag.
     */
    public static boolean isTotalOrder(final Decimal128 x, final Decimal128 y) {
        return Decimal128MathUtils.isTotalOrder(x, y);
    }

    /**
     * Return {@code true} if the absolute values of x and y are ordered
     * (see the IEEE Standard 754-2008)
     *
     * @param x First decimal value.
     * @param y Second decimal value.
     * @return Comparison flag.
     */
    public static boolean isTotalOrderMag(final Decimal128 x, final Decimal128 y) {
        return Decimal128MathUtils.isTotalOrderMag(x, y);
    }

    /**
     * Return the radix b of the format of x, 2 or 10.
     *
     * @param x The test value.
     * @return The value radix.
     */
    public static int radix(final Decimal128 x) {
        return Decimal128MathUtils.radix(x);
    }

    /**
     * Decimal floating-point remainder.
     *
     * @param x Value of the quotient numerator.
     * @param y Value of the quotient denominator.
     * @return The remainder of dividing the arguments.
     */
    public static Decimal128 rem(final Decimal128 x, final Decimal128 y) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.rem(x, y, r);
        return r;
    }

    /**
     * Returns the exponent e of x, a signed integral value, determined
     * as though x were represented with infinite range and minimum exponent.
     *
     * @param x Value whose ilogb is returned.
     * @return The integral part of the logarithm of |x|.
     */
    public static int ilogb(final Decimal128 x) {
        return Decimal128MathUtils.ilogb(x);
    }

    /**
     * Returns x * 10^N.
     *
     * @param x The mantissa part.
     * @param n The exponent part.
     * @return The combined value.
     */
    public static Decimal128 scalbn(final Decimal128 x, final int n) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.scalbn(x, n, r);
        return r;
    }

    /**
     * Returns the result of multiplying x (the significand) by 2 raised to the power of exp (the exponent).
     *
     * @param x Floating point value representing the significand.
     * @param n Value of the exponent.
     * @return The x*2^exp value.
     */
    public static Decimal128 ldexp(final Decimal128 x, final int n) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.ldexp(x, n, r);
        return r;
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
    public static Decimal128 quantize(final Decimal128 x, final Decimal128 y) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.quantize(x, y, r);
        return r;
    }

    /**
     * Returns the logarithm of |x|.
     *
     * @param x Value whose logarithm is calculated.
     * @return The logarithm of x.
     */
    public static Decimal128 logb(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.logb(x, r);
        return r;
    }

    /**
     * Rounds the floating-point argument arg to an integer value in floating-point format, using the current rounding mode.
     *
     * @param x Value to round.
     * @return The rounded value.
     */
    public static Decimal128 nearByInt(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.nearByInt(x, r);
        return r;
    }

    /**
     * Returns the positive difference between x and y, that is, if x &gt; y, returns x-y, otherwise (if x &le; y), returns +0.
     *
     * @param x Minuend value.
     * @param y Subtrahend value.
     * @return The positive difference.
     */
    public static Decimal128 fdim(final Decimal128 x, final Decimal128 y) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.fdim(x, y, r);
        return r;
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
    public static int quantExp(final Decimal128 x) {
        return Decimal128MathUtils.quantExp(x);
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
    public static Decimal128 quantum(final Decimal128 x) {
        Decimal128 r = new Decimal128();
        Decimal128MathUtils.quantum(x, r);
        return r;
    }
}
