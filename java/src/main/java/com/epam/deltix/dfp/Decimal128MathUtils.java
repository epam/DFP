package com.epam.deltix.dfp;

public class Decimal128MathUtils {
    private Decimal128MathUtils() {
    }

    /// region Math functions

    /**
     * Returns the base-e exponential function of x, which is e raised to the power x.
     *
     * @param x Value of the exponent.
     * @param r Exponential value of x.
     */
    public static void exp(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Exp(x.low, x.high, r);
    }

    /**
     * Returns the base-2 exponential function of x, which is 2 raised to the power x.
     *
     * @param x Value of the exponent.
     * @param r 2 raised to the power of x.
     */
    public static void exp2(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Exp2(x.low, x.high, r);
    }

    /**
     * Returns the base-10 exponential function of x, which is 10 raised to the power x.
     *
     * @param x Value of the exponent.
     * @param r 10 raised to the power of x.
     */
    public static void exp10(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Exp10(x.low, x.high, r);
    }

    /**
     * Returns e raised to the power x minus one.
     *
     * @param x Value of the exponent.
     * @param r e raised to the power of x, minus one.
     */
    public static void expm1(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Expm1(x.low, x.high, r);
    }

    /**
     * Returns the natural logarithm of x.
     *
     * @param x Value whose logarithm is calculated.
     * @param r Natural logarithm of x.
     */
    public static void log(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Log(x.low, x.high, r);
    }

    /**
     * Returns the binary (base-2) logarithm of x.
     *
     * @param x Value whose logarithm is calculated.
     * @param r The binary logarithm of x.
     */
    public static void log2(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Log2(x.low, x.high, r);
    }

    /**
     * Returns the common (base-10) logarithm of x.
     *
     * @param x Value whose logarithm is calculated.
     * @param r Common logarithm of x.
     */
    public static void log10(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Log10(x.low, x.high, r);
    }

    /**
     * Returns the natural logarithm of one plus x: log(1+x).
     *
     * @param x Value whose logarithm is calculated.
     * @param r The natural logarithm of (1+x).
     */
    public static void log1p(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Log1p(x.low, x.high, r);
    }

    /**
     * Returns base raised to the power exponent.
     *
     * @param x Base value.
     * @param y Exponent value.
     * @param r The result of raising base to the power exponent.
     */
    public static void pow(final Decimal128Fields x, final Decimal128Fields y, final Decimal128Fields r) {
        NativeImpl.bid128Pow(x.low, x.high, y.low, y.high, r);
    }

    /**
     * Returns the floating-point remainder of numer/denom.
     *
     * @param x Value of the quotient numerator.
     * @param y Value of the quotient denominator.
     * @param r The remainder of dividing the arguments.
     */
    public static void fmod(final Decimal128Fields x, final Decimal128Fields y, final Decimal128Fields r) {
        NativeImpl.bid128Fmod(x.low, x.high, y.low, y.high, r);
    }

    //OPN(bid64_modf, bid64_modf(x, iptr), BID_UINT64 x, BID_UINT64 *iptr)

    /**
     * Returns the hypotenuse of a right-angled triangle whose legs are x and y.
     *
     * @param x The first leg.
     * @param y The second leg.
     * @param r The square root of (x*x+y*y).
     */
    public static void hypot(final Decimal128Fields x, final Decimal128Fields y, final Decimal128Fields r) {
        NativeImpl.bid128Hypot(x.low, x.high, y.low, y.high, r);
    }

    /**
     * Returns the sine of an angle of x radians.
     *
     * @param x Value representing an angle expressed in radians.
     * @param r Sine of x radians.
     */
    public static void sin(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Sin(x.low, x.high, r);
    }

    /**
     * Returns the cosine of an angle of x radians.
     *
     * @param x Value representing an angle expressed in radians.
     * @param r Cosine of x radians.
     */
    public static void cos(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Cos(x.low, x.high, r);
    }

    /**
     * Returns the tangent of an angle of x radians.
     *
     * @param x Value representing an angle, expressed in radians.
     * @param r Tangent of x radians.
     */
    public static void tan(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Tan(x.low, x.high, r);
    }

    /**
     * Returns the principal value of the arc sine of x, expressed in radians.
     *
     * @param x Value whose arc sine is computed, in the interval [-1,+1].
     * @param r Principal arc sine of x, in the interval [-pi/2,+pi/2] radians.
     */
    public static void asin(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Asin(x.low, x.high, r);
    }

    /**
     * Returns the principal value of the arc cosine of x, expressed in radians.
     *
     * @param x Value whose arc cosine is computed, in the interval [-1,+1].
     * @param r Principal arc cosine of x, in the interval [0,pi] radians.
     */
    public static void acos(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Acos(x.low, x.high, r);
    }

    /**
     * Returns the principal value of the arc tangent of x, expressed in radians.
     *
     * @param x Value whose arc tangent is computed.
     * @param r Principal arc tangent of x, in the interval [-pi/2,+pi/2] radians.
     */
    public static void atan(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Atan(x.low, x.high, r);
    }

    /**
     * Returns the principal value of the arc tangent of y/x, expressed in radians.
     *
     * @param y Value representing the proportion of the y-coordinate.
     * @param x Value representing the proportion of the x-coordinate.
     * @param r Principal arc tangent of y/x, in the interval [-pi,+pi] radians.
     */
    public static void atan2(final Decimal128Fields y, final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Atan2(y.low, y.high, x.low, x.high, r);
    }

    /**
     * Returns the hyperbolic sine of x.
     *
     * @param x Value representing a hyperbolic angle.
     * @param r Hyperbolic sine of x.
     */
    public static void sinh(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Sinh(x.low, x.high, r);
    }

    /**
     * Returns the hyperbolic cosine of x.
     *
     * @param x Value representing a hyperbolic angle.
     * @param r Hyperbolic cosine of x.
     */
    public static void cosh(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Cosh(x.low, x.high, r);
    }

    /**
     * Returns the hyperbolic tangent of x.
     *
     * @param x Value representing a hyperbolic angle.
     * @param r Hyperbolic tangent of x.
     */
    public static void tanh(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Tanh(x.low, x.high, r);
    }

    /**
     * Returns the area hyperbolic sine of x.
     *
     * @param x Value whose area hyperbolic sine is computed.
     * @param r Area hyperbolic sine of x.
     */
    public static void asinh(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Asinh(x.low, x.high, r);
    }

    /**
     * Returns the nonnegative area hyperbolic cosine of x.
     *
     * @param x Value whose area hyperbolic cosine is computed.
     * @param r Nonnegative area hyperbolic cosine of x, in the interval [0,+INFINITY].
     */
    public static void acosh(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Acosh(x.low, x.high, r);
    }

    /**
     * Returns the area hyperbolic tangent of x.
     *
     * @param x Value whose area hyperbolic tangent is computed, in the interval [-1,+1].
     * @param r Area hyperbolic tangent of x.
     */
    public static void atanh(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Atanh(x.low, x.high, r);
    }

    /**
     * Returns the error function value for x.
     *
     * @param x Parameter for the error function.
     * @param r Error function value for x.
     */
    public static void erf(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Erf(x.low, x.high, r);
    }

    /**
     * Returns the complementary error function value for x.
     *
     * @param x Parameter for the complementary error function.
     * @param r Complementary error function value for x.
     */
    public static void erfc(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Erfc(x.low, x.high, r);
    }

    /**
     * Returns the gamma function of x.
     *
     * @param x Parameter for the gamma function.
     * @param r Gamma function of x.
     */
    public static void tgamma(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Tgamma(x.low, x.high, r);
    }

    /**
     * Returns the natural logarithm of the absolute value of the gamma function of x.
     *
     * @param x Parameter for the log-gamma function.
     * @param r Log-gamma function of x.
     */
    public static void lgamma(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Lgamma(x.low, x.high, r);
    }

    //@Decimal public static void add(final Decimal128Fields x, final Decimal128Fields y, final Decimal128Fields r) { NativeImpl.bid128Add(x.low, x.high, y.low, y.high, r); }

    /**
     * Decimal floating-point subtraction.
     *
     * @param x Minuend value.
     * @param y Subtrahend value.
     * @param r Difference of values.
     */
    public static void sub(final Decimal128Fields x, final Decimal128Fields y, final Decimal128Fields r) {
        NativeImpl.bid128Sub(x.low, x.high, y.low, y.high, r);
    }

    /**
     * Decimal floating-point multiplication.
     *
     * @param x Values to be multiplied.
     * @param y Values to be multiplied.
     * @param r Product of values.
     */
    public static void mul(final Decimal128Fields x, final Decimal128Fields y, final Decimal128Fields r) {
        NativeImpl.bid128Mul(x.low, x.high, y.low, y.high, r);
    }

    /**
     * Decimal floating-point division.
     *
     * @param x Dividend value.
     * @param y Divider value.
     * @param r Ratio of values.
     */
    public static void div(final Decimal128Fields x, final Decimal128Fields y, final Decimal128Fields r) {
        NativeImpl.bid128Div(x.low, x.high, y.low, y.high, r);
    }

    /**
     * Decimal floating-point fused multiply-add: x*y+z
     *
     * @param x Values to be multiplied.
     * @param y Values to be multiplied.
     * @param z Value to be added.
     * @param r The result of x*y+z
     */
    public static void fma(final Decimal128Fields x, final Decimal128Fields y,
                           final Decimal128Fields z, final Decimal128Fields r) {
        NativeImpl.bid128Fma(x.low, x.high, y.low, y.high, z.low, z.high, r);
    }

    /**
     * Decimal floating-point square root.
     *
     * @param x Value whose square root is computed.
     * @param r Square root of x.
     */
    public static void sqrt(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Sqrt(x.low, x.high, r);
    }

    /**
     * Returns the cubic root of x.
     *
     * @param x Value whose cubic root is computed.
     * @param r Cubic root of x.
     */
    public static void cbrt(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Cbrt(x.low, x.high, r);
    }

    //public static boolean isEqual(final Decimal128Fields x, final Decimal128Fields y, final Decimal128Fields r) { return NativeImpl.bid128QuietEqual(x, y) ; }

    //public static boolean isGreater(final Decimal128Fields x, final Decimal128Fields y, final Decimal128Fields r) { return NativeImpl.bid128QuietGreater(x, y) ; }

    //public static boolean isGreaterEqual(final Decimal128Fields x, final Decimal128Fields y, final Decimal128Fields r) { NativeImpl.bid128QuietGreaterEqual(x.low, x.high, y.low, y.high, r); }

    /**
     * Compare 128-bit decimal floating-point numbers for specified relation.
     *
     * @param x First decimal number.
     * @param y Second decimal number.
     * @return The comparison sign.
     */
    public static boolean isGreaterUnordered(final Decimal128Fields x, final Decimal128Fields y) {
        return NativeImpl.bid128QuietGreaterUnordered(x.low, x.high, y.low, y.high);
    }

    //public static boolean isLess(final Decimal128Fields x, final Decimal128Fields y, final Decimal128Fields r) { return NativeImpl.bid128QuietLess(x, y) ; }

    //public static boolean isLessEqual(final Decimal128Fields x, final Decimal128Fields y, final Decimal128Fields r) { NativeImpl.bid128QuietLessEqual(x.low, x.high, y.low, y.high, r); }

    /**
     * Compare 128-bit decimal floating-point numbers for specified relation.
     *
     * @param x First decimal number.
     * @param y Second decimal number.
     * @return The comparison sign.
     */
    public static boolean isLessUnordered(final Decimal128Fields x, final Decimal128Fields y) {
        return NativeImpl.bid128QuietLessUnordered(x.low, x.high, y.low, y.high);
    }

    //public static boolean isNotEqual(final Decimal128Fields x, final Decimal128Fields y, final Decimal128Fields r) { return NativeImpl.bid128QuietNotEqual(x, y) ; }

    /**
     * Compare 128-bit decimal floating-point numbers for specified relation.
     *
     * @param x First decimal number.
     * @param y Second decimal number.
     * @return The comparison sign.
     */
    public static boolean isNotGreater(final Decimal128Fields x, final Decimal128Fields y) {
        return NativeImpl.bid128QuietNotGreater(x.low, x.high, y.low, y.high);
    }

    /**
     * Compare 128-bit decimal floating-point numbers for specified relation.
     *
     * @param x First decimal number.
     * @param y Second decimal number.
     * @return The comparison sign.
     */
    public static boolean isNotLess(final Decimal128Fields x, final Decimal128Fields y) {
        return NativeImpl.bid128QuietNotLess(x.low, x.high, y.low, y.high);
    }

    /**
     * These function return a {@code true} value if both arguments are not NaN, otherwise  {@code false}.
     *
     * @param x First decimal number.
     * @param y Second decimal number.
     * @return {@code true} if both arguments are not NaN.
     */
    public static boolean isOrdered(final Decimal128Fields x, final Decimal128Fields y) {
        return NativeImpl.bid128QuietOrdered(x.low, x.high, y.low, y.high);
    }

    /**
     * These function return a {@code true} value if either argument is NaN, otherwise {@code false}.
     *
     * @param x First decimal number.
     * @param y Second decimal number.
     * @return {@code true} if either argument is NaN.
     */
    public static boolean isUnordered(final Decimal128Fields x, final Decimal128Fields y) {
        return NativeImpl.bid128QuietUnordered(x.low, x.high, y.low, y.high);
    }

    /**
     * Round 128-bit decimal floating-point value to integral-valued decimal
     * floating-point value in the same format, using the current rounding mode;
     *
     * @param x Rounding number.
     * @param r The rounded value.
     */
    public static void roundIntegralExact(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128RoundIntegralExact(x.low, x.high, r);
    }

    /**
     * Round 128-bit decimal floating-point value to integral-valued decimal
     * floating-point value in the same format, using the rounding-to-nearest-even mode;
     *
     * @param x Rounding number.
     * @param r The rounded value.
     */
    public static void roundIntegralNearestEven(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128RoundIntegralNearestEven(x.low, x.high, r);
    }

    /**
     * Round 128-bit decimal floating-point value to integral-valued decimal
     * floating-point value in the same format, using the rounding-down mode;
     *
     * @param x Rounding number.
     * @param r The rounded value.
     */
    public static void roundIntegralNegative(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128RoundIntegralNegative(x.low, x.high, r);
    }

    /**
     * Round 128-bit decimal floating-point value to integral-valued decimal
     * floating-point value in the same format, using the rounding-up mode;
     *
     * @param x Rounding number.
     * @param r The rounded value.
     */
    public static void roundIntegralPositive(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128RoundIntegralPositive(x.low, x.high, r);
    }

    /**
     * Round 128-bit decimal floating-point value to integral-valued decimal
     * floating-point value in the same format, using the rounding-to-zero  mode;
     *
     * @param x Rounding number.
     * @param r The rounded value.
     */
    public static void roundIntegralZero(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128RoundIntegralZero(x.low, x.high, r);
    }

    /**
     * Round 128-bit decimal floating-point value to integral-valued decimal
     * floating-point value in the same format, using the rounding-to-nearest-away mode;
     *
     * @param x Rounding number.
     * @param r The rounded value.
     */
    public static void roundIntegralNearestAway(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128RoundIntegralNearestAway(x.low, x.high, r);
    }

    //@Decimal public static void nextUp(final Decimal128Fields x, final Decimal128Fields r) { NativeImpl.bid128Nextup(x.low, x.high, r); }

    //@Decimal public static void nextDown(final Decimal128Fields x, final Decimal128Fields r) { NativeImpl.bid128Nextdown(x.low, x.high, r); }

    /**
     * Returns the next 128-bit decimal floating-point number that neighbors
     * the first operand in the direction toward the second operand.
     *
     * @param x Starting point.
     * @param y Direction.
     * @param r Starting point value adjusted in Direction way.
     */
    public static void nextAfter(final Decimal128Fields x, final Decimal128Fields y, final Decimal128Fields r) {
        NativeImpl.bid128Nextafter(x.low, x.high, y.low, y.high, r);
    }

    /**
     * Returns the canonicalized floating-point number x if x &lt; y,
     * y if y &lt; x, the canonicalized floating-point number if one operand is
     * a floating-point number and the other a quiet NaN.
     *
     * @param x First decimal number.
     * @param y Second decimal number.
     * @param r The minimal value.
     */
    public static void minNum(final Decimal128Fields x, final Decimal128Fields y, final Decimal128Fields r) {
        NativeImpl.bid128Minnum(x.low, x.high, y.low, y.high, r);
    }

    /**
     * Returns the canonicalized floating-point number x if |x| &lt; |y|,
     * y if |y| &lt; |x|, otherwise this function is identical to {@link #minNum}.
     *
     * @param x First decimal number.
     * @param y Second decimal number.
     * @param r The value with minimal magnitude.
     */
    public static void minNumMag(final Decimal128Fields x, final Decimal128Fields y, final Decimal128Fields r) {
        NativeImpl.bid128MinnumMag(x.low, x.high, y.low, y.high, r);
    }

    /**
     * Returns the canonicalized floating-point number y if x &lt; y,
     * x if y &lt; x, the canonicalized floating-point number if one operand is a
     * floating-point number and the other a quiet NaN. Otherwise it is either x
     * or y, canonicalized.
     *
     * @param x First decimal number.
     * @param y Second decimal number.
     * @param r The maximal value.
     */
    public static void maxNum(final Decimal128Fields x, final Decimal128Fields y, final Decimal128Fields r) {
        NativeImpl.bid128Maxnum(x.low, x.high, y.low, y.high, r);
    }

    /**
     * Returns the canonicalized floating-point number x if |x| &gt; |y|,
     * y if |y| &gt; |x|, otherwise this function is identical to {@link #maxNum}.
     *
     * @param x First decimal number.
     * @param y Second decimal number.
     * @param r The value with maximal magnitude.
     */
    public static void maxNumMag(final Decimal128Fields x, final Decimal128Fields y, final Decimal128Fields r) {
        NativeImpl.bid128MaxnumMag(x.low, x.high, y.low, y.high, r);
    }

    /**
     * Convert 32-bit signed integer to 128-bit decimal floating-point number.
     *
     * @param x Value to convert.
     * @param r The converted value.
     */
    public static void fromInt32(final int x, final Decimal128Fields r) {
        NativeImpl.bid128FromInt32(x, r);
    }

    /**
     * Convert 32-bit unsigned integer to 128-bit decimal floating-point.
     *
     * @param x Value to convert.
     * @param r The converted value.
     */
    public static void fromUInt32(final int x, final Decimal128Fields r) {
        NativeImpl.bid128FromUint32(x, r);
    }

    /**
     * Convert 64-bit signed integer to 128-bit decimal floating-point number.
     *
     * @param x Value to convert.
     * @param r The converted value.
     */
    public static void fromInt64(final long x, final Decimal128Fields r) {
        NativeImpl.bid128FromInt64(x, r);
    }

    /**
     * Convert 64-bit unsigned integer to 128-bit decimal floating-point.
     *
     * @param x Value to convert.
     * @param r The converted value.
     */
    public static void fromUInt64(final long x, final Decimal128Fields r) {
        NativeImpl.bid128FromUint64(x, r);
    }

    /**
     * Return {@code true} if and only if x has negative sign.
     *
     * @param x Test value.
     * @return The sign.
     */
    public static boolean isSigned(final Decimal128Fields x) {
        return NativeImpl.bid128IsSigned(x.low, x.high);
    }

    /**
     * Return {@code true} if and only if x is subnormal.
     *
     * @param x Test value.
     * @return The check flag.
     */
    public static boolean isSubnormal(final Decimal128Fields x) {
        return NativeImpl.bid128IsSubnormal(x.low, x.high);
    }

    //    /**
//     * Return {@code true} if and only if x is zero, subnormal or normal (not infinite or NaN).
//     *
//     * @param x Test value.
//     * @return The check flag.
//     */
//    public static boolean isFinite(final Decimal128Fields x, final Decimal128Fields r) { return NativeImpl.bid128IsFinite(x) ; }

//    /**
//     * Return {@code true} if and only if x is +0 or -0.
//     *
//     * @param x Test value.
//     * @return The check flag.
//     */
//    public static boolean isZero(final Decimal128Fields x, final Decimal128Fields r) { return NativeImpl.bid128IsZero(x) ; }

    /**
     * Return {@code true} if and only if x is infinite.
     *
     * @param x Test value.
     * @return The check flag.
     */
    public static boolean isInf(final Decimal128Fields x) {
        return NativeImpl.bid128IsInf(x.low, x.high);
    }

    /**
     * Return {@code true} if and only if x is a signaling NaN.
     *
     * @param x Test value.
     * @return The check flag.
     */
    public static boolean isSignaling(final Decimal128Fields x) {
        return NativeImpl.bid128IsSignaling(x.low, x.high);
    }

    /**
     * Return {@code true} if and only if x is a finite number, infinity, or
     * NaN that is canonical.
     *
     * @param x Test value.
     * @return The check flag.
     */
    public static boolean isCanonical(final Decimal128Fields x) {
        return NativeImpl.bid128IsCanonical(x.low, x.high);
    }

    //    /**
//     * Return true if and only if x is a NaN.
//     *
//     * @param x Test value.
//     * @return The check flag.
//     */
//    public static boolean isNaN(final Decimal128Fields x, final Decimal128Fields r) { return NativeImpl.bid128IsNaN(x) ; }

    //@Decimal public static void copy(final Decimal128Fields x, final Decimal128Fields r) { NativeImpl.bid128Copy(x.low, x.high, r); }
    //@Decimal public static void negate(final Decimal128Fields x, final Decimal128Fields r) { NativeImpl.bid128Negate(x.low, x.high, r); }

    public static void abs(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Abs(x.low, x.high, r);
    }

    /**
     * Copies a 128-bit decimal floating-point operand x to a destination
     * in the same format as x, but with the sign of y.
     *
     * @param x Magnitude value.
     * @param y Sign value.
     * @param r Combined value.
     */
    public static void copySign(final Decimal128Fields x, final Decimal128Fields y, final Decimal128Fields r) {
        NativeImpl.bid128CopySign(x.low, x.high, y.low, y.high, r);
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
    public static int classOfValue(final Decimal128Fields x) {
        return NativeImpl.bid128Class(x.low, x.high);
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
    public static boolean isSameQuantum(final Decimal128Fields x, final Decimal128Fields y) {
        return NativeImpl.bid128SameQuantum(x.low, x.high, y.low, y.high);
    }

    /**
     * Return {@code true} if x and y are ordered (see the IEEE Standard 754-2008).
     *
     * @param x First decimal value.
     * @param y Second decimal value.
     * @return Comparison flag.
     */
    public static boolean isTotalOrder(final Decimal128Fields x, final Decimal128Fields y) {
        return NativeImpl.bid128TotalOrder(x.low, x.high, y.low, y.high);
    }

    /**
     * Return {@code true} if the absolute values of x and y are ordered
     * (see the IEEE Standard 754-2008)
     *
     * @param x First decimal value.
     * @param y Second decimal value.
     * @return Comparison flag.
     */
    public static boolean isTotalOrderMag(final Decimal128Fields x, final Decimal128Fields y) {
        return NativeImpl.bid128TotalOrderMag(x.low, x.high, y.low, y.high);
    }

    /**
     * Return the radix b of the format of x, 2 or 10.
     *
     * @param x The test value.
     * @return The value radix.
     */
    public static int radix(final Decimal128Fields x) {
        return NativeImpl.bid128Radix(x.low, x.high);
    }

    /**
     * Decimal floating-point remainder.
     *
     * @param x Value of the quotient numerator.
     * @param y Value of the quotient denominator.
     * @param r The remainder of dividing the arguments.
     */
    public static void rem(final Decimal128Fields x, final Decimal128Fields y, final Decimal128Fields r) {
        NativeImpl.bid128Rem(x.low, x.high, y.low, y.high, r);
    }

    /**
     * Returns the exponent e of x, a signed integral value, determined
     * as though x were represented with infinite range and minimum exponent.
     *
     * @param x Value whose ilogb is returned.
     * @return The integral part of the logarithm of |x|.
     */
    public static int ilogb(final Decimal128Fields x) {
        return NativeImpl.bid128Ilogb(x.low, x.high);
    }

    /**
     * Returns x * 10^N.
     *
     * @param x The mantissa part.
     * @param n The exponent part.
     * @param r The combined value.
     */
    public static void scalbn(final Decimal128Fields x, final int n, final Decimal128Fields r) {
        NativeImpl.bid128Scalbn(x.low, x.high, n, r);
    }

    /**
     * Returns the result of multiplying x (the significand) by 10 raised to the power of exp (the exponent).
     *
     * @param x Floating point value representing the significand.
     * @param n Value of the exponent.
     * @param r The x*10^exp value.
     */
    public static void ldexp(final Decimal128Fields x, final int n, final Decimal128Fields r) {
        NativeImpl.bid128Ldexp(x.low, x.high, n, r);
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
     * @param r The quantized value.
     */
    public static void quantize(final Decimal128Fields x, final Decimal128Fields y, final Decimal128Fields r) {
        NativeImpl.bid128Quantize(x.low, x.high, y.low, y.high, r);
    }

    /**
     * Convert 128-bit decimal floating-point value (binary encoding)
     * to 32-bit binary floating-point format.
     *
     * @param x The input decimal value.
     * @return The converted value.
     */
    public static float toBinary32(final Decimal128Fields x) {
        return NativeImpl.bid128ToBinary32(x.low, x.high);
    }

    /**
     * Convert 128-bit decimal floating-point value (binary encoding)
     * to 64-bit binary floating-point format.
     *
     * @param x The input decimal value.
     * @return The converted value.
     */
    public static double toBinary64(final Decimal128Fields x) {
        return NativeImpl.bid128ToBinary64(x.low, x.high);
    }

    /**
     * Returns the adjusted exponent of the absolute value.
     *
     * @param x Value whose logarithm is calculated.
     * @param r The adjusted logarithm of |x|.
     */
    public static void logb(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Logb(x.low, x.high, r);
    }

    /**
     * Rounds the floating-point argument arg to an integer value in floating-point format, using the current rounding mode.
     *
     * @param x Value to round.
     * @param r The rounded value.
     */
    public static void nearByInt(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Nearbyint(x.low, x.high, r);
    }

    /**
     * Returns the positive difference between x and y, that is, if x &gt; y, returns x-y, otherwise (if x &le; y), returns +0.
     *
     * @param x Minuend value.
     * @param y Subtrahend value.
     * @param r The positive difference.
     */
    public static void fdim(final Decimal128Fields x, final Decimal128Fields y, final Decimal128Fields r) {
        NativeImpl.bid128Fdim(x.low, x.high, y.low, y.high, r);
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
    public static int quantExp(final Decimal128Fields x) {
        return NativeImpl.bid128Quantexp(x.low, x.high);
    }

    /**
     * The function compute the quantum exponent of a finite argument. The numerical value of a finite number
     * is given by: (-1)^sign x coefficient x 10^exponent. The quantum of a finite number is given by
     * 1 x 10^exponent and represents the value of a unit in the least significant position of the coefficient
     * of a finite number. The quantum exponent is the exponent of the quantum (represented by exponent above).
     *
     * @param x The value for operation.
     * @param r The quantum.
     */
    public static void quantum(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Quantum(x.low, x.high, r);
    }

    /// endregion
}
