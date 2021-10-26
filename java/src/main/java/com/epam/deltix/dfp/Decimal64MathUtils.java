package com.epam.deltix.dfp;

public class Decimal64MathUtils {
    private Decimal64MathUtils() {
    }

    /// region Math functions

    /**
     * Returns the base-e exponential function of x, which is e raised to the power x.
     *
     * @param x Value of the exponent.
     * @return Exponential value of x.
     */
    @Decimal
    public static long exp(@Decimal final long x) {
        return NativeImpl.bid64Exp(x);
    }

    /**
     * Returns the base-2 exponential function of x, which is 2 raised to the power x.
     *
     * @param x Value of the exponent.
     * @return 2 raised to the power of x.
     */
    @Decimal
    public static long exp2(@Decimal final long x) {
        return NativeImpl.bid64Exp2(x);
    }

    /**
     * Returns the base-10 exponential function of x, which is 10 raised to the power x.
     *
     * @param x Value of the exponent.
     * @return 10 raised to the power of x.
     */
    @Decimal
    public static long exp10(@Decimal final long x) {
        return NativeImpl.bid64Exp10(x);
    }

    /**
     * Returns e raised to the power x minus one.
     *
     * @param x Value of the exponent.
     * @return e raised to the power of x, minus one.
     */
    @Decimal
    public static long expm1(@Decimal final long x) {
        return NativeImpl.bid64Expm1(x);
    }

    /**
     * Returns the natural logarithm of x.
     *
     * @param x Value whose logarithm is calculated.
     * @return Natural logarithm of x.
     */
    @Decimal
    public static long log(@Decimal final long x) {
        return NativeImpl.bid64Log(x);
    }

    /**
     * Returns the binary (base-2) logarithm of x.
     *
     * @param x Value whose logarithm is calculated.
     * @return The binary logarithm of x.
     */
    @Decimal
    public static long log2(@Decimal final long x) {
        return NativeImpl.bid64Log2(x);
    }

    /**
     * Returns the common (base-10) logarithm of x.
     *
     * @param x Value whose logarithm is calculated.
     * @return Common logarithm of x.
     */
    @Decimal
    public static long log10(@Decimal final long x) {
        return NativeImpl.bid64Log10(x);
    }

    /**
     * Returns the natural logarithm of one plus x: log(1+x).
     *
     * @param x Value whose logarithm is calculated.
     * @return The natural logarithm of (1+x).
     */
    @Decimal
    public static long log1p(@Decimal final long x) {
        return NativeImpl.bid64Log1p(x);
    }

    /**
     * Returns base raised to the power exponent.
     *
     * @param x Base value.
     * @param y Exponent value.
     * @return The result of raising base to the power exponent.
     */
    @Decimal
    public static long pow(@Decimal final long x, @Decimal final long y) {
        return NativeImpl.bid64Pow(x, y);
    }

    /**
     * Returns the floating-point remainder of numer/denom.
     *
     * @param x Value of the quotient numerator.
     * @param y Value of the quotient denominator.
     * @return The remainder of dividing the arguments.
     */
    @Decimal
    public static long fmod(@Decimal final long x, @Decimal final long y) {
        return NativeImpl.bid64Fmod(x, y);
    }

    //OPN(bid64_modf, bid64_modf(x, iptr), BID_UINT64 x, BID_UINT64 *iptr)

    /**
     * Returns the hypotenuse of a right-angled triangle whose legs are x and y.
     *
     * @param x The first leg.
     * @param y The second leg.
     * @return The square root of (x*x+y*y).
     */
    @Decimal
    public static long hypot(@Decimal final long x, @Decimal final long y) {
        return NativeImpl.bid64Hypot(x, y);
    }

    /**
     * Returns the sine of an angle of x radians.
     *
     * @param x Value representing an angle expressed in radians.
     * @return Sine of x radians.
     */
    @Decimal
    public static long sin(@Decimal final long x) {
        return NativeImpl.bid64Sin(x);
    }

    /**
     * Returns the cosine of an angle of x radians.
     *
     * @param x Value representing an angle expressed in radians.
     * @return Cosine of x radians.
     */
    @Decimal
    public static long cos(@Decimal final long x) {
        return NativeImpl.bid64Cos(x);
    }

    /**
     * Returns the tangent of an angle of x radians.
     *
     * @param x Value representing an angle, expressed in radians.
     * @return Tangent of x radians.
     */
    @Decimal
    public static long tan(@Decimal final long x) {
        return NativeImpl.bid64Tan(x);
    }

    /**
     * Returns the principal value of the arc sine of x, expressed in radians.
     *
     * @param x Value whose arc sine is computed, in the interval [-1,+1].
     * @return Principal arc sine of x, in the interval [-pi/2,+pi/2] radians.
     */
    @Decimal
    public static long asin(@Decimal final long x) {
        return NativeImpl.bid64Asin(x);
    }

    /**
     * Returns the principal value of the arc cosine of x, expressed in radians.
     *
     * @param x Value whose arc cosine is computed, in the interval [-1,+1].
     * @return Principal arc cosine of x, in the interval [0,pi] radians.
     */
    @Decimal
    public static long acos(@Decimal final long x) {
        return NativeImpl.bid64Acos(x);
    }

    /**
     * Returns the principal value of the arc tangent of x, expressed in radians.
     *
     * @param x Value whose arc tangent is computed.
     * @return Principal arc tangent of x, in the interval [-pi/2,+pi/2] radians.
     */
    @Decimal
    public static long atan(@Decimal final long x) {
        return NativeImpl.bid64Atan(x);
    }

    /**
     * Returns the principal value of the arc tangent of y/x, expressed in radians.
     *
     * @param y Value representing the proportion of the y-coordinate.
     * @param x Value representing the proportion of the x-coordinate.
     * @return Principal arc tangent of y/x, in the interval [-pi,+pi] radians.
     */
    @Decimal
    public static long atan2(@Decimal final long y, @Decimal final long x) {
        return NativeImpl.bid64Atan2(y, x);
    }

    /**
     * Returns the hyperbolic sine of x.
     *
     * @param x Value representing a hyperbolic angle.
     * @return Hyperbolic sine of x.
     */
    @Decimal
    public static long sinh(@Decimal final long x) {
        return NativeImpl.bid64Sinh(x);
    }

    /**
     * Returns the hyperbolic cosine of x.
     *
     * @param x Value representing a hyperbolic angle.
     * @return Hyperbolic cosine of x.
     */
    @Decimal
    public static long cosh(@Decimal final long x) {
        return NativeImpl.bid64Cosh(x);
    }

    /**
     * Returns the hyperbolic tangent of x.
     *
     * @param x Value representing a hyperbolic angle.
     * @return Hyperbolic tangent of x.
     */
    @Decimal
    public static long tanh(@Decimal final long x) {
        return NativeImpl.bid64Tanh(x);
    }

    /**
     * Returns the area hyperbolic sine of x.
     *
     * @param x Value whose area hyperbolic sine is computed.
     * @return Area hyperbolic sine of x.
     */
    @Decimal
    public static long asinh(@Decimal final long x) {
        return NativeImpl.bid64Asinh(x);
    }

    /**
     * Returns the nonnegative area hyperbolic cosine of x.
     *
     * @param x Value whose area hyperbolic cosine is computed.
     * @return Nonnegative area hyperbolic cosine of x, in the interval [0,+INFINITY].
     */
    @Decimal
    public static long acosh(@Decimal final long x) {
        return NativeImpl.bid64Acosh(x);
    }

    /**
     * Returns the area hyperbolic tangent of x.
     *
     * @param x Value whose area hyperbolic tangent is computed, in the interval [-1,+1].
     * @return Area hyperbolic tangent of x.
     */
    @Decimal
    public static long atanh(@Decimal final long x) {
        return NativeImpl.bid64Atanh(x);
    }

    /**
     * Returns the error function value for x.
     *
     * @param x Parameter for the error function.
     * @return Error function value for x.
     */
    @Decimal
    public static long erf(@Decimal final long x) {
        return NativeImpl.bid64Erf(x);
    }

    /**
     * Returns the complementary error function value for x.
     *
     * @param x Parameter for the complementary error function.
     * @return Complementary error function value for x.
     */
    @Decimal
    public static long erfc(@Decimal final long x) {
        return NativeImpl.bid64Erfc(x);
    }

    /**
     * Returns the gamma function of x.
     *
     * @param x Parameter for the gamma function.
     * @return Gamma function of x.
     */
    @Decimal
    public static long tgamma(@Decimal final long x) {
        return NativeImpl.bid64Tgamma(x);
    }

    /**
     * Returns the natural logarithm of the absolute value of the gamma function of x.
     *
     * @param x Parameter for the log-gamma function.
     * @return Log-gamma function of x.
     */
    @Decimal
    public static long lgamma(@Decimal final long x) {
        return NativeImpl.bid64Lgamma(x);
    }

    //@Decimal public static long add(@Decimal final long x, @Decimal final long y) { return NativeImpl.bid64Add(x, y); }

    /**
     * Decimal floating-point subtraction.
     *
     * @param x Minuend value.
     * @param y Subtrahend value.
     * @return Difference of values.
     */
    @Decimal
    public static long sub(@Decimal final long x, @Decimal final long y) {
        return NativeImpl.bid64Sub(x, y);
    }

    /**
     * Decimal floating-point multiplication.
     *
     * @param x Values to be multiplied.
     * @param y Values to be multiplied.
     * @return Product of values.
     */
    @Decimal
    public static long mul(@Decimal final long x, @Decimal final long y) {
        return NativeImpl.bid64Mul(x, y);
    }

    /**
     * Decimal floating-point division.
     *
     * @param x Dividend value.
     * @param y Divider value.
     * @return Ratio of values.
     */
    @Decimal
    public static long div(@Decimal final long x, @Decimal final long y) {
        return NativeImpl.bid64Div(x, y);
    }

    /**
     * Decimal floating-point fused multiply-add: x*y+z
     *
     * @param x Values to be multiplied.
     * @param y Values to be multiplied.
     * @param z Value to be added.
     * @return The result of x*y+z
     */
    @Decimal
    public static long fma(@Decimal final long x, @Decimal final long y, @Decimal final long z) {
        return NativeImpl.bid64Fma(x, y, z);
    }

    /**
     * Decimal floating-point square root.
     *
     * @param x Value whose square root is computed.
     * @return Square root of x.
     */
    @Decimal
    public static long sqrt(@Decimal final long x) {
        return NativeImpl.bid64Sqrt(x);
    }

    /**
     * Returns the cubic root of x.
     *
     * @param x Value whose cubic root is computed.
     * @return Cubic root of x.
     */
    @Decimal
    public static long cbrt(@Decimal final long x) {
        return NativeImpl.bid64Cbrt(x);
    }

    //public static boolean isEqual(@Decimal final long x, @Decimal final long y) { return NativeImpl.bid64QuietEqual(x, y) ; }

    //public static boolean isGreater(@Decimal final long x, @Decimal final long y) { return NativeImpl.bid64QuietGreater(x, y) ; }

    //public static boolean isGreaterEqual(@Decimal final long x, @Decimal final long y) { return NativeImpl.bid64QuietGreaterEqual(x, y); }

    /**
     * Compare 64-bit decimal floating-point numbers for specified relation.
     *
     * @param x First decimal number.
     * @param y Second decimal number.
     * @return The comparison sign.
     */
    public static boolean isGreaterUnordered(@Decimal final long x, @Decimal final long y) {
        return NativeImpl.bid64QuietGreaterUnordered(x, y);
    }

    //public static boolean isLess(@Decimal final long x, @Decimal final long y) { return NativeImpl.bid64QuietLess(x, y) ; }

    //public static boolean isLessEqual(@Decimal final long x, @Decimal final long y) { return NativeImpl.bid64QuietLessEqual(x, y); }

    /**
     * Compare 64-bit decimal floating-point numbers for specified relation.
     *
     * @param x First decimal number.
     * @param y Second decimal number.
     * @return The comparison sign.
     */
    public static boolean isLessUnordered(@Decimal final long x, @Decimal final long y) {
        return NativeImpl.bid64QuietLessUnordered(x, y);
    }

    //public static boolean isNotEqual(@Decimal final long x, @Decimal final long y) { return NativeImpl.bid64QuietNotEqual(x, y) ; }

    /**
     * Compare 64-bit decimal floating-point numbers for specified relation.
     *
     * @param x First decimal number.
     * @param y Second decimal number.
     * @return The comparison sign.
     */
    public static boolean isNotGreater(@Decimal final long x, @Decimal final long y) {
        return NativeImpl.bid64QuietNotGreater(x, y);
    }

    /**
     * Compare 64-bit decimal floating-point numbers for specified relation.
     *
     * @param x First decimal number.
     * @param y Second decimal number.
     * @return The comparison sign.
     */
    public static boolean isNotLess(@Decimal final long x, @Decimal final long y) {
        return NativeImpl.bid64QuietNotLess(x, y);
    }

    /**
     * These function return a {@code true} value if both arguments are not NaN, otherwise  {@code false}.
     *
     * @param x First decimal number.
     * @param y Second decimal number.
     * @return {@code true} if both arguments are not NaN.
     */
    public static boolean isOrdered(@Decimal final long x, @Decimal final long y) {
        return NativeImpl.bid64QuietOrdered(x, y);
    }

    /**
     * These function return a {@code true} value if either argument is NaN, otherwise {@code false}.
     *
     * @param x First decimal number.
     * @param y Second decimal number.
     * @return {@code true} if either argument is NaN.
     */
    public static boolean isUnordered(@Decimal final long x, @Decimal final long y) {
        return NativeImpl.bid64QuietUnordered(x, y);
    }

    /**
     * Round 64-bit decimal floating-point value to integral-valued decimal
     * floating-point value in the same format, using the current rounding mode;
     *
     * @param x Rounding number.
     * @return The rounded value.
     */
    @Decimal
    public static long roundIntegralExact(@Decimal final long x) {
        return NativeImpl.bid64RoundIntegralExact(x);
    }

    /**
     * Round 64-bit decimal floating-point value to integral-valued decimal
     * floating-point value in the same format, using the rounding-to-nearest-even mode;
     *
     * @param x Rounding number.
     * @return The rounded value.
     */
    @Decimal
    public static long roundIntegralNearestEven(@Decimal final long x) {
        return NativeImpl.bid64RoundIntegralNearestEven(x);
    }

    /**
     * Round 64-bit decimal floating-point value to integral-valued decimal
     * floating-point value in the same format, using the rounding-down mode;
     *
     * @param x Rounding number.
     * @return The rounded value.
     */
    @Decimal
    public static long roundIntegralNegative(@Decimal final long x) {
        return NativeImpl.bid64RoundIntegralNegative(x);
    }

    /**
     * Round 64-bit decimal floating-point value to integral-valued decimal
     * floating-point value in the same format, using the rounding-up mode;
     *
     * @param x Rounding number.
     * @return The rounded value.
     */
    @Decimal
    public static long roundIntegralPositive(@Decimal final long x) {
        return NativeImpl.bid64RoundIntegralPositive(x);
    }

    /**
     * Round 64-bit decimal floating-point value to integral-valued decimal
     * floating-point value in the same format, using the rounding-to-zero  mode;
     *
     * @param x Rounding number.
     * @return The rounded value.
     */
    @Decimal
    public static long roundIntegralZero(@Decimal final long x) {
        return NativeImpl.bid64RoundIntegralZero(x);
    }

    /**
     * Round 64-bit decimal floating-point value to integral-valued decimal
     * floating-point value in the same format, using the rounding-to-nearest-away mode;
     *
     * @param x Rounding number.
     * @return The rounded value.
     */
    @Decimal
    public static long roundIntegralNearestAway(@Decimal final long x) {
        return NativeImpl.bid64RoundIntegralNearestAway(x);
    }

    //@Decimal public static long nextUp(@Decimal final long x) { return NativeImpl.bid64Nextup(x); }

    //@Decimal public static long nextDown(@Decimal final long x) { return NativeImpl.bid64Nextdown(x); }

    /**
     * Returns the next 64-bit decimal floating-point number that neighbors
     * the first operand in the direction toward the second operand.
     *
     * @param x Starting point.
     * @param y Direction.
     * @return Starting point value adjusted in Direction way.
     */
    @Decimal
    public static long nextAfter(@Decimal final long x, @Decimal final long y) {
        return NativeImpl.bid64Nextafter(x, y);
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
    @Decimal
    public static long minNum(@Decimal final long x, @Decimal final long y) {
        return NativeImpl.bid64Minnum(x, y);
    }

    /**
     * Returns the canonicalized floating-point number x if |x| &lt; |y|,
     * y if |y| &lt; |x|, otherwise this function is identical to {@link #minNum(long, long)}.
     *
     * @param x First decimal number.
     * @param y Second decimal number.
     * @return The value with minimal magnitude.
     */
    @Decimal
    public static long minNumMag(@Decimal final long x, @Decimal final long y) {
        return NativeImpl.bid64MinnumMag(x, y);
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
    @Decimal
    public static long maxNum(@Decimal final long x, @Decimal final long y) {
        return NativeImpl.bid64Maxnum(x, y);
    }

    /**
     * Returns the canonicalized floating-point number x if |x| &gt; |y|,
     * y if |y| &gt; |x|, otherwise this function is identical to {@link #maxNum(long, long)}.
     *
     * @param x First decimal number.
     * @param y Second decimal number.
     * @return The value with maximal magnitude.
     */
    @Decimal
    public static long maxNumMag(@Decimal final long x, @Decimal final long y) {
        return NativeImpl.bid64MaxnumMag(x, y);
    }

    /**
     * Convert 32-bit signed integer to 64-bit decimal floating-point number.
     *
     * @param x Value to convert.
     * @return The converted value.
     */
    @Decimal
    public static long fromInt32(final int x) {
        return NativeImpl.bid64FromInt32(x);
    }

    /**
     * Convert 32-bit unsigned integer to 64-bit decimal floating-point.
     *
     * @param x Value to convert.
     * @return The converted value.
     */
    @Decimal
    public static long fromUInt32(final int x) {
        return NativeImpl.bid64FromUint32(x);
    }

    /**
     * Convert 64-bit signed integer to 64-bit decimal floating-point number.
     *
     * @param x Value to convert.
     * @return The converted value.
     */
    @Decimal
    public static long fromInt64(final long x) {
        return NativeImpl.bid64FromInt64(x);
    }

    /**
     * Convert 64-bit unsigned integer to 64-bit decimal floating-point.
     *
     * @param x Value to convert.
     * @return The converted value.
     */
    @Decimal
    public static long fromUInt64(final long x) {
        return NativeImpl.bid64FromUint64(x);
    }

    /**
     * Return {@code true} if and only if x has negative sign.
     *
     * @param x Test value.
     * @return The sign.
     */
    public static boolean isSigned(@Decimal final long x) {
        return NativeImpl.bid64IsSigned(x);
    }

    /**
     * Return {@code true} if and only if x is subnormal.
     *
     * @param x Test value.
     * @return The check flag.
     */
    public static boolean isSubnormal(@Decimal final long x) {
        return NativeImpl.bid64IsSubnormal(x);
    }

//    /**
//     * Return {@code true} if and only if x is zero, subnormal or normal (not infinite or NaN).
//     *
//     * @param x Test value.
//     * @return The check flag.
//     */
//    public static boolean isFinite(@Decimal final long x) { return NativeImpl.bid64IsFinite(x) ; }

//    /**
//     * Return {@code true} if and only if x is +0 or -0.
//     *
//     * @param x Test value.
//     * @return The check flag.
//     */
//    public static boolean isZero(@Decimal final long x) { return NativeImpl.bid64IsZero(x) ; }

    /**
     * Return {@code true} if and only if x is infinite.
     *
     * @param x Test value.
     * @return The check flag.
     */
    public static boolean isInf(@Decimal final long x) {
        return NativeImpl.bid64IsInf(x);
    }

    /**
     * Return {@code true} if and only if x is a signaling NaN.
     *
     * @param x Test value.
     * @return The check flag.
     */
    public static boolean isSignaling(@Decimal final long x) {
        return NativeImpl.bid64IsSignaling(x);
    }

    /**
     * Return {@code true} if and only if x is a finite number, infinity, or
     * NaN that is canonical.
     *
     * @param x Test value.
     * @return The check flag.
     */
    public static boolean isCanonical(@Decimal final long x) {
        return NativeImpl.bid64IsCanonical(x);
    }

//    /**
//     * Return true if and only if x is a NaN.
//     *
//     * @param x Test value.
//     * @return The check flag.
//     */
//    public static boolean isNaN(@Decimal final long x) { return NativeImpl.bid64IsNaN(x) ; }

    //@Decimal public static long copy(@Decimal final long x) { return NativeImpl.bid64Copy(x); }
    //@Decimal public static long negate(@Decimal final long x) { return NativeImpl.bid64Negate(x); }
    //@Decimal public static long abs(@Decimal final long x) { return NativeImpl.bid64Abs(x); }

    /**
     * Copies a 64-bit decimal floating-point operand x to a destination
     * in the same format as x, but with the sign of y.
     *
     * @param x Magnitude value.
     * @param y Sign value.
     * @return Combined value.
     */
    @Decimal
    public static long copySign(@Decimal final long x, @Decimal final long y) {
        return NativeImpl.bid64CopySign(x, y);
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
    public static int classOfValue(@Decimal final long x) {
        return NativeImpl.bid64Class(x);
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
    public static boolean isSameQuantum(@Decimal final long x, @Decimal final long y) {
        return NativeImpl.bid64SameQuantum(x, y);
    }

    /**
     * Return {@code true} if x and y are ordered (see the IEEE Standard 754-2008).
     *
     * @param x First decimal value.
     * @param y Second decimal value.
     * @return Comparison flag.
     */
    public static boolean isTotalOrder(@Decimal final long x, @Decimal final long y) {
        return NativeImpl.bid64TotalOrder(x, y);
    }

    /**
     * Return {@code true} if the absolute values of x and y are ordered
     * (see the IEEE Standard 754-2008)
     *
     * @param x First decimal value.
     * @param y Second decimal value.
     * @return Comparison flag.
     */
    public static boolean isTotalOrderMag(@Decimal final long x, @Decimal final long y) {
        return NativeImpl.bid64TotalOrderMag(x, y);
    }

    /**
     * Return the radix b of the format of x, 2 or 10.
     *
     * @param x The test value.
     * @return The value radix.
     */
    public static int radix(@Decimal final long x) {
        return NativeImpl.bid64Radix(x);
    }

    /**
     * Decimal floating-point remainder.
     *
     * @param x Value of the quotient numerator.
     * @param y Value of the quotient denominator.
     * @return The remainder of dividing the arguments.
     */
    @Decimal
    public static long rem(@Decimal final long x, @Decimal final long y) {
        return NativeImpl.bid64Rem(x, y);
    }

    /**
     * Returns the exponent e of x, a signed integral value, determined
     * as though x were represented with infinite range and minimum exponent.
     *
     * @param x Value whose ilogb is returned.
     * @return The integral part of the logarithm of |x|.
     */
    public static int ilogb(@Decimal final long x) {
        return NativeImpl.bid64Ilogb(x);
    }

    /**
     * Returns x * 10^N.
     *
     * @param x The mantissa part.
     * @param n The exponent part.
     * @return The combined value.
     */
    @Decimal
    public static long scalbn(@Decimal final long x, final int n) {
        return NativeImpl.bid64Scalbn(x, n);
    }

    /**
     * Returns the result of multiplying x (the significand) by 10 raised to the power of exp (the exponent).
     *
     * @param x Floating point value representing the significand.
     * @param n Value of the exponent.
     * @return The x*10^exp value.
     */
    @Decimal
    public static long ldexp(@Decimal final long x, final int n) {
        return NativeImpl.bid64Ldexp(x, n);
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
    @Decimal
    public static long quantize(@Decimal final long x, @Decimal final long y) {
        return NativeImpl.bid64Quantize(x, y);
    }

    /**
     * Convert 64-bit decimal floating-point value (binary encoding)
     * to 32-bit binary floating-point format.
     *
     * @param x The input decimal value.
     * @return The converted value.
     */
    public static float toBinary32(@Decimal final long x) {
        return NativeImpl.bid64ToBinary32(x);
    }

    /**
     * Convert 64-bit decimal floating-point value (binary encoding)
     * to 64-bit binary floating-point format.
     *
     * @param x The input decimal value.
     * @return The converted value.
     */
    public static double toBinary64(@Decimal final long x) {
        return NativeImpl.bid64ToBinary64(x);
    }

    /**
     * Returns the adjusted exponent of the absolute value.
     *
     * @param x Value whose logarithm is calculated.
     * @return The adjusted logarithm of |x|.
     */
    @Decimal
    public static long logb(@Decimal final long x) {
        return NativeImpl.bid64Logb(x);
    }

    /**
     * Rounds the floating-point argument arg to an integer value in floating-point format, using the current rounding mode.
     *
     * @param x Value to round.
     * @return The rounded value.
     */
    @Decimal
    public static long nearByInt(@Decimal final long x) {
        return NativeImpl.bid64Nearbyint(x);
    }

    /**
     * Returns the positive difference between x and y, that is, if x &gt; y, returns x-y, otherwise (if x &le; y), returns +0.
     *
     * @param x Minuend value.
     * @param y Subtrahend value.
     * @return The positive difference.
     */
    @Decimal
    public static long fdim(@Decimal final long x, @Decimal final long y) {
        return NativeImpl.bid64Fdim(x, y);
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
    public static int quantExp(@Decimal final long x) {
        return NativeImpl.bid64Quantexp(x);
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
    @Decimal
    public static long quantum(@Decimal final long x) {
        return NativeImpl.bid64Quantum(x);
    }

    /// endregion
}
