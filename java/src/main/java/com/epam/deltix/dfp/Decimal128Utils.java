package com.epam.deltix.dfp;

public class Decimal128Utils {
    private Decimal128Utils() {
    }

//    /// region Constants
//
//    /**
//     * A constant holding canonical representation of Not-a-Number DFP value (non-signaling NaN)
//     */
//    @Decimal
//    public static final long NaN = JavaImpl.NaN;
//
//    /**
//     * A constant holding canonical representation of Positive Infinity value
//     */
//    @Decimal
//    public static final long POSITIVE_INFINITY = JavaImpl.POSITIVE_INFINITY;
//
//    /**
//     * A constant holding canonical representation of Negative Infinity value
//     */
//    @Decimal
//    public static final long NEGATIVE_INFINITY = JavaImpl.NEGATIVE_INFINITY;
//
//    /**
//     * Maximum number of significant decimal digits that 64-bit DFP value can store.
//     */
//    public static final int MAX_SIGNIFICAND_DIGITS = 16;
//
//    /**
//     * Number of bits used to represent a 64-bit DFP value ({@code 64}).
//     */
//    public static final int SIZE = Long.SIZE;
//
//    /**
//     * A constant holding the maximum possible DFP64 exponent for normalized values: {@code 384}
//     */
//    public static final int MAX_EXPONENT = JavaImpl.MAX_EXPONENT;
//
//    /**
//     * A constant holding the minimum possible DFP64 exponent for normalized values: {@code -383}
//     */
//    public static final int MIN_EXPONENT = JavaImpl.MIN_EXPONENT;
//
//    /**
//     * A constant holding the largest representable number: {@code 9999999999999999E+369}
//     */
//    @Decimal
//    public static final long MAX_VALUE = JavaImpl.MAX_VALUE;
//
//    /**
//     * A constant holding the smallest representable number: {@code -9999999999999999E+369}
//     */
//    @Decimal
//    public static final long MIN_VALUE = JavaImpl.MIN_VALUE;
//
//    /**
//     * A constant holding the smallest representable positive number: {@code 1E-398}
//     */
//    @Decimal
//    public static final long MIN_POSITIVE_VALUE = JavaImpl.MIN_POSITIVE_VALUE;
//
//    /**
//     * A constant holding the largest representable negative number: {@code -1E-398}
//     */
//    @Decimal
//    public static final long MAX_NEGATIVE_VALUE = JavaImpl.MAX_NEGATIVE_VALUE;
//
//    /**
//     * Zero: {@code 0}
//     */
//    @Decimal
//    public static final long ZERO = JavaImpl.ZERO;
//
//    /**
//     * One: {@code 1}
//     */
//    @Decimal
//    public static final long ONE = Decimal128Utils.fromInt(1);
//
//    /**
//     * Two: {@code 2}
//     */
//    @Decimal
//    public static final long TWO = Decimal128Utils.fromInt(2);
//
//    /**
//     * Ten: {@code 10}
//     */
//    @Decimal
//    public static final long TEN = Decimal128Utils.fromInt(10);
//
//    /**
//     * One Hundred: {@code 100}
//     */
//    @Decimal
//    public static final long HUNDRED = Decimal128Utils.fromInt(100);
//
//    /**
//     * One Thousand: {@code 1000}
//     */
//    @Decimal
//    public static final long THOUSAND = Decimal128Utils.fromInt(1000);
//
//    /**
//     * One million: {@code 1000_000}
//     */
//    @Decimal
//    public static final long MILLION = Decimal128Utils.fromInt(1000_000);
//
//    /**
//     * One tenth: {@code 0.1}
//     */
//    @Decimal
//    public static final long ONE_TENTH = JavaImpl.fromFixedPointFastUnchecked(1, 1);
//
//    /**
//     * One hundredth: {@code 0.01}
//     */
//    @Decimal
//    public static final long ONE_HUNDREDTH = JavaImpl.fromFixedPointFastUnchecked(1, 2);
//
//    /**
//     * The value corresponding to {@code Decimal64.NULL} / Java {@code null} constant
//     *
//     * @see Decimal64#NULL
//     */
//    @Decimal
//    public static final long NULL = JavaImpl.NULL;

    public static final long NULL_LOW = 0xFFFF_FFFF_FFFF_FF80L;
    public static final long NULL_HIGH = 0xFFFF_FFFF_FFFF_FFFFL;

    public static boolean isNull(final long low, final long high) {
        return low == NULL_LOW && high == NULL_HIGH;
    }

//
//    /// endregion
//
//    /// region Private constants
//
//    private static final int NaN_HASH_CODE = 1;
//    private static final int POSITIVE_INF_HASH_CODE = 2;
//    private static final int NEGATIVE_INF_HASH_CODE = 3;
//
//    /// endregion
//
//    /// region Object Implementation
//
//    /**
//     * Returns a hash code for the binary representation of the supplied DFP value.
//     *
//     * @param value Given decimal.
//     * @return HashCode of given decimal.
//     */
//    public static int identityHashCode(final long value) {
//        return (int) (value ^ (value >>> 32));
//    }
//
//    /**
//     * Returns a hash code for the arithmetical value of the supplied {@code DFP} value.
//     * Does not distinguish between different {@code DFP} representations of the same arithmetical values.
//     * <p>
//     * We consider that all POSITIVE_INFINITYs have equal hashCode,
//     * all NEGATIVE_INFINITYs have equal hashCode,
//     * all NaNs have equal hashCode.
//     *
//     * @param value the DFP value whose hash code is being calculated
//     * @return HashCode for the gfiven DFP.
//     */
//    public static int hashCode(final Decimal128Fields value) {
//        if (JavaImpl.isNonFinite(value)) {
//            return JavaImpl.isNaN(value) ? NaN_HASH_CODE : value >= 0 ? POSITIVE_INF_HASH_CODE : NEGATIVE_INF_HASH_CODE;
//        }
//
//        final long canonizeValue = JavaImpl.canonizeFinite(value);
//        return (int) (canonizeValue ^ (canonizeValue >>> 32));
//    }
//
//
//    public static String toString(final Decimal128Fields value) {
//        return NULL == value ? "null" : appendTo(value, new StringBuilder()).toString();
//    }
//
//    static String toDebugString(final Decimal128Fields value) {
//        return JavaImpl.toDebugString(value);
//    }
//
//    /**
//     * Returns {@code true} if two {@code DFP} values represent same arithmetical value.
//     * <p>
//     * We consider that all possible encodings of {@link #POSITIVE_INFINITY} are equal,
//     * all possible encodings of {@link #NEGATIVE_INFINITY} are equal,
//     * all possible encodings of {@code NaN} and {@code SNaN} are equal,
//     * all invalid encodings that aren't NaN or Infinity are equal to {@link #ZERO}.
//     * Negative and Positive(default) {@link #ZERO} are equal.
//     *
//     * @param a the first {@code DFP} value.
//     * @param b the second {@code DFP} value.
//     * @return {@code true} if both DFP values represent the same arithmetical value
//     * {@code false} otherwise
//     * @see #equals(long, Object)
//     * @see #equals(Object)
//     * @see Decimal64#equals(Decimal64, Decimal64)
//     */
//    public static boolean equals(final Decimal128Fields a, final Decimal128Fields b) {
//        final long canonizedA = canonize(a);
//        final long canonizedB = canonize(b);
//        return canonizedA == canonizedB;
//    }
//
//    /**
//     * Return {@code true} if two decimals have exactly the same binary representation.
//     *
//     * @param a the first {@code DFP} value.
//     * @param b the second {@code DFP} value.
//     * @return {@code true} if two decimals have the same binary representation (same underlying value)
//     * {@code false} otherwise
//     */
//    public static boolean isIdentical(final Decimal128Fields a, final Decimal128Fields b) {
//        return a == b;
//    }
//
//    /**
//     * Returns {@code true} if the second value is {@link Decimal64} and its value if the same as the the first one
//     *
//     * @param a the first {@code DFP} value.
//     * @param b the second value, will be compared with the first argument.
//     * @return {@code true} if the second argument is of type {@link Decimal64} that wraps value that is is the same as
//     * the first argument.
//     * {@code false} otherwise.
//     */
//    public static boolean isIdentical(final Decimal128Fields a, final Object b) {
//        return (b == null && NULL == a) || (b instanceof Decimal64 && (a == ((Decimal64) b).value));
//    }
//
//    /**
//     * Returns {@code true} if two {@code DFP} values represent the same arithmetical value.
//     * <p>
//     * We consider that all possible encodings of {@link #POSITIVE_INFINITY} are equal,
//     * all possible encodings of {@link #NEGATIVE_INFINITY} are equal,
//     * all possible encodings of NaN and SNaN are equal,
//     * all invalid encodings of finite values equal {@link #ZERO}
//     *
//     * @param a the first 64-bit DFP value.
//     * @param b the second 64-bit DFP value.
//     * @return {@code true} if two decimal values represent the same arithmetical value.
//     */
//    public static boolean equals(final Decimal128Fields a, final Object b) {
//        return (b == null && NULL == a)
//            || (b instanceof Decimal64 && equals(a, ((Decimal64) b).value));
//    }
//
//    /**
//     * Returns {@code true} if the supplied DFP64 value equals special {@code NULL} constant
//     * that corresponds to null reference of type {@link Decimal64}.
//     *
//     * @param value the DFP value being checked
//     * @return {@code true}, if {@code NULL}
//     * {@code false} otherwise
//     */
//    public static boolean isNull(final Decimal128Fields value) {
//        return JavaImpl.isNull(value);
//    }
//
//    /// endregion

    /// region Conversion

    public static void fromUnderlying(final long low, final long high, final Decimal128Fields r) {
        r.set(low, high);
    }

//    @Decimal
//    public static long toUnderlying(final Decimal128Fields value) {
//        return value;
//    }
//
//    /**
//     * Create {@code @Decimal long} value from {@code BigDecimal} binary floating point value.
//     * <p>Note that not all binary FP values can be exactly represented as decimal FP values.
//     *
//     * @param value source {@code BigDecimal} binary floating point value
//     * @return new {@code Decimal long} value
//     */
//    @Decimal
//    public static long fromBigDecimal(final BigDecimal value) {
//        return JavaImpl.fromBigDecimal(value, JavaImpl.BID_ROUNDING_TO_NEAREST);
//    }
//
//    /**
//     * Create {@code @Decimal long} from {@code BigDecimal} binary floating point value.
//     *
//     * @param value source {@code BigDecimal} binary floating point value
//     * @return new {@code @Decimal long} value
//     * @throws IllegalArgumentException if the value can't be converted to {@code @Decimal long} without precision loss
//     */
//    @Decimal
//    public static long fromBigDecimalExact(final BigDecimal value) {
//        return JavaImpl.fromBigDecimal(value, JavaImpl.BID_ROUNDING_EXCEPTION);
//    }
//
//    /**
//     * Convert {@code @Decimal long} value to {@code BigDecimal} binary floating point value.
//     * <p>Note that not all decimal FP values can be exactly represented as binary FP values.
//     *
//     * @param value 64-bit floating point dfp value.
//     * @return {@code BigDecimal} value
//     */
//    public static BigDecimal toBigDecimal(final Decimal128Fields value) {
//        return JavaImpl.toBigDecimal(value);
//    }

    /**
     * Converts fixed-point dfp to dfp floating-point value.
     * <p>
     * Essentially, the value behind the fixed-point dfp representation can be
     * computed as {@code mantissa} divided by 10 to the power of {@code numberOfDigits}.
     * <p>
     * E.g., 1.23 can be represented as pair (1230, 3), where 1230 is a mantissa, and 3 is a number of digits after
     * the dot. Thus, {@code Decimal128Utils.fromFixedPoint(1230, 3)} will return a dfp floating-point representation of
     * value {@code 1.23}.
     *
     * @param mantissa       Integer part of fixed-point dfp.
     * @param numberOfDigits Number of digits after the dot.
     * @param r              New 64-bit floating point dfp value.
     */
    public static void fromFixedPoint(final long mantissa, final int numberOfDigits, final Decimal128Fields r) {
        // TODO: Can also create java version for this one
        NativeImpl.bid128FromFixedPoint64(mantissa, numberOfDigits, r);
    }

//    /**
//     * Overload of {@link #fromFixedPoint(long, int)} for mantissa representable by {@code int}.
//     * Faster than the full-range version.
//     *
//     * @param mantissa       source fixed point value represented as {@code int}
//     * @param numberOfDigits number of decimal digits representing fractional part
//     * @return New 64-bit floating point dfp value.
//     */
//    @Decimal
//    public static long fromFixedPoint(final int mantissa, final int numberOfDigits) {
//        return JavaImpl.fromFixedPoint32(mantissa, numberOfDigits);
//    }
//
//    @Decimal
//    public static long fromDecimalDouble(final double value) {
//        return JavaImpl.fromDecimalDouble(value);
//    }
//

    /**
     * Converts from floating-point dfp representation to fixed-point with given number of digits after the dot.
     * <p>
     * For example, dfp value of 1.23 can be represented as pair (1230, 3), where 1230 is a mantissa, and 3 is
     * a number of digits after the dot. Essentially, the value behind the fixed-point dfp representation can be
     * computed as {@code mantissa} divided by 10 to the power of {@code numberOfDigits}.
     *
     * @param x              64-bit floating point dfp value.
     * @param numberOfDigits Number of digits after the dot.
     * @return fixed-point decimal value represented as @{code long}
     */
    public static long toFixedPoint(final Decimal128Fields x, final int numberOfDigits) {
        return NativeImpl.bid128ToFixedPoint(x.low, x.high, numberOfDigits);
    }

    /**
     * Create {@code DFP} value from 64-bit binary floating point ({@code double}) value.
     *
     * @param value source 64-bit binary floating point value
     * @param r     New {@code DFP} value.
     */
    public static void fromDouble(final double value, final Decimal128Fields r) {
        NativeImpl.bid128FromFloat64(value, r);
    }

    /**
     * Convert {@code DFP} value to 64-bit binary floating point ({@code double}) value.
     * <p>Note that not all decimal FP values can be exactly represented as binary FP values.
     *
     * @param x source {@code DFP} value
     * @return {@code double} value
     */
    public static double toDouble(final Decimal128Fields x) {
        return NativeImpl.bid128ToFloat64(x.low, x.high);
    }

    /**
     * Create {@code DFP} value from {@code long} integer.
     *
     * @param value source {@code long} integer value
     * @param r     New {@code DFP} value.
     */
    public static void fromLong(final long value, final Decimal128Fields r) {
        NativeImpl.bid128FromInt64(value, r);
    }

    /**
     * Convert {@code DFP} value to {@code long} integer value by truncating fractional part towards zero.
     * <p>Does not throw exceptions on overflow or invalid data
     *
     * @param x {@code DFP} value
     * @return {@code long} integer value
     */
    public static long toLong(final Decimal128Fields x) {
        return NativeImpl.bid128ToInt64(x.low, x.high);
    }

    /**
     * Create {@code DFP} value from 32-bit integer ({@code int}).
     * <p>faster than creating from ({@code long})
     *
     * @param value source integer value
     * @param r     new {@code DFP} value
     */
    @Decimal
    public static void fromInt(final int value, final Decimal128Fields r) {
        NativeImpl.bid128FromInt32(value, r);
    }

    /**
     * Convert {@code DFP} value to {@code int} value by truncating fractional part towards zero.
     * <p>Does not throw exceptions on overflow or invalid data
     *
     * @param x {@code DFP} value
     * @return {@code int} value
     */
    public static int toInt(final Decimal128Fields x) {
        return (int) NativeImpl.bid128ToInt64(x.low, x.high);
    }

    /// endregion

    /// region Classification

    public static boolean isNaN(final Decimal128Fields x) {
        return NativeImpl.bid128IsNaN(x.low, x.high);
    }

    public static boolean isInfinity(final Decimal128Fields x) {
        return NativeImpl.bid128IsInfinity(x.low, x.high);
    }

    public static boolean isPositiveInfinity(final Decimal128Fields x) {
        return NativeImpl.bid128IsPositiveInfinity(x.low, x.high);
    }

    public static boolean isNegativeInfinity(final Decimal128Fields x) {
        return NativeImpl.bid128IsNegativeInfinity(x.low, x.high);
    }

    public static boolean isFinite(final Decimal128Fields x) {
        return NativeImpl.bid128IsFinite(x.low, x.high);
    }

    public static boolean isNormal(final Decimal128Fields x) {
        return NativeImpl.bid128IsNormal(x.low, x.high);
    }

    /// endregion

    /// region Comparison

    public static int compareTo(final Decimal128Fields a, final Decimal128Fields b) {
        return NativeImpl.bid128Compare(a.low, a.high, b.low, b.high);
    }

    public static boolean isEqual(final Decimal128Fields a, final Decimal128Fields b) {
        return NativeImpl.bid128IsEqual(a.low, a.high, b.low, b.high);
    }

    public static boolean isNotEqual(final Decimal128Fields a, final Decimal128Fields b) {
        return NativeImpl.bid128IsNotEqual(a.low, a.high, b.low, b.high);
    }

    public static boolean isLess(final Decimal128Fields a, final Decimal128Fields b) {
        return NativeImpl.bid128IsLess(a.low, a.high, b.low, b.high);
    }

    public static boolean isLessOrEqual(final Decimal128Fields a, final Decimal128Fields b) {
        return NativeImpl.bid128IsLessOrEqual(a.low, a.high, b.low, b.high);
    }

    public static boolean isGreater(final Decimal128Fields a, final Decimal128Fields b) {
        return NativeImpl.bid128IsGreater(a.low, a.high, b.low, b.high);
    }

    public static boolean isGreaterOrEqual(final Decimal128Fields a, final Decimal128Fields b) {
        return NativeImpl.bid128IsGreaterOrEqual(a.low, a.high, b.low, b.high);
    }

    public static boolean isZero(final Decimal128Fields x) {
        return NativeImpl.bid128IsZero(x.low, x.high);
    }

    public static boolean isNonZero(final Decimal128Fields x) {
        return NativeImpl.bid128IsNonZero(x.low, x.high);
    }

    public static boolean isPositive(final Decimal128Fields x) {
        return NativeImpl.bid128IsPositive(x.low, x.high);
    }

    public static boolean isNegative(final Decimal128Fields x) {
        return NativeImpl.bid128IsNegative(x.low, x.high);
    }

    public static boolean isNonPositive(final Decimal128Fields x) {
        return NativeImpl.bid128IsNonPositive(x.low, x.high);
    }

    public static boolean isNonNegative(final Decimal128Fields x) {
        return NativeImpl.bid128IsNonNegative(x.low, x.high);
    }

    /// endregion

    /// region Minimum & Maximum

    public static void max(final Decimal128Fields a, final Decimal128Fields b, final Decimal128Fields r) {
        NativeImpl.bid128Max2(a.low, a.high, b.low, b.high, r);
    }

    public static void max(final Decimal128Fields a, final Decimal128Fields b, final Decimal128Fields c, final Decimal128Fields r) {
        NativeImpl.bid128Max3(a.low, a.high, b.low, b.high, c.low, c.high, r);
    }

    public static void max(final Decimal128Fields a, final Decimal128Fields b, final Decimal128Fields c, final Decimal128Fields d, final Decimal128Fields r) {
        NativeImpl.bid128Max4(a.low, a.high, b.low, b.high, c.low, c.high, d.low, d.high, r);
    }

    public static void min(final Decimal128Fields a, final Decimal128Fields b, final Decimal128Fields r) {
        NativeImpl.bid128Min2(a.low, a.high, b.low, b.high, r);
    }

    public static void min(final Decimal128Fields a, final Decimal128Fields b, final Decimal128Fields c, final Decimal128Fields r) {
        NativeImpl.bid128Min3(a.low, a.high, b.low, b.high, c.low, c.high, r);
    }

    public static void min(final Decimal128Fields a, final Decimal128Fields b, final Decimal128Fields c, final Decimal128Fields d, final Decimal128Fields r) {
        NativeImpl.bid128Min4(a.low, a.high, b.low, b.high, c.low, c.high, d.low, d.high, r);
    }

    /// endregion

    /// region Arithmetic

    public static void negate(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Negate(x.low, x.high, r);
    }

    public static void abs(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128Abs(x.low, x.high, r);
    }

    public static void add(final Decimal128Fields a, final Decimal128Fields b, final Decimal128Fields r) {
        NativeImpl.bid128Add2(a.low, a.high, b.low, b.high, r);
    }

    public static void add(final Decimal128Fields a, final Decimal128Fields b, final Decimal128Fields c, final Decimal128Fields r) {
        NativeImpl.bid128Add3(a.low, a.high, b.low, b.high, c.low, c.high, r);
    }

    public static void add(final Decimal128Fields a, final Decimal128Fields b, final Decimal128Fields c, final Decimal128Fields d, final Decimal128Fields r) {
        NativeImpl.bid128Add4(a.low, a.high, b.low, b.high, c.low, c.high, d.low, d.high, r);
    }

    public static void subtract(final Decimal128Fields a, final Decimal128Fields b, final Decimal128Fields r) {
        NativeImpl.bid128Subtract(a.low, a.high, b.low, b.high, r);
    }

    public static void multiply(final Decimal128Fields a, final Decimal128Fields b, final Decimal128Fields r) {
        NativeImpl.bid128Multiply2(a.low, a.high, b.low, b.high, r);
    }

    public static void multiply(final Decimal128Fields a, final Decimal128Fields b, final Decimal128Fields c, final Decimal128Fields r) {
        NativeImpl.bid128Multiply3(a.low, a.high, b.low, b.high, c.low, c.high, r);
    }

    public static void multiply(final Decimal128Fields a, final Decimal128Fields b, final Decimal128Fields c, final Decimal128Fields d, final Decimal128Fields r) {
        NativeImpl.bid128Multiply4(a.low, a.high, b.low, b.high, c.low, c.high, d.low, d.high, r);
    }

    public static void multiplyByInteger(final Decimal128Fields a, final int b, final Decimal128Fields r) {
        NativeImpl.bid128MultiplyByInt32(a.low, a.high, b, r);
    }

    public static void multiplyByInteger(final Decimal128Fields a, final long b, final Decimal128Fields r) {
        NativeImpl.bid128MultiplyByInt64(a.low, a.high, b, r);
    }

    public static void divide(final Decimal128Fields a, final Decimal128Fields b, final Decimal128Fields r) {
        NativeImpl.bid128Divide(a.low, a.high, b.low, b.high, r);
    }

    public static void divideByInteger(final Decimal128Fields a, final int b, final Decimal128Fields r) {
        NativeImpl.bid128DivideByInt32(a.low, a.high, b, r);
    }

    public static void divideByInteger(final Decimal128Fields a, final long b, final Decimal128Fields r) {
        NativeImpl.bid128DivideByInt64(a.low, a.high, b, r);
    }

    /**
     * Compute {@code (a * b) + c} rounded as one ternary operation. The value is calculated to infinite precision
     * and rounded once to 64-bit {@code DFP} format according to the default rounding mode (nearest, ties away from zero).
     * <p>
     * Corresponds to {@code fmad64()} operation.
     *
     * @param a multiplicand represented as {@code DFP}
     * @param b multiplier represented as {@code DFP}
     * @param c summand represented as {@code DFP}
     * @param r The value of {@code (a * b) + c} rounded once to {@code DFP} format.
     */
    public static void multiplyAndAdd(final Decimal128Fields a, final Decimal128Fields b, final Decimal128Fields c, final Decimal128Fields r) {
        NativeImpl.bid128MultiplyAndAdd(a.low, a.high, b.low, b.high, c.low, c.high, r);
    }

    public static void scaleByPowerOfTen(final Decimal128Fields a, final int n, final Decimal128Fields r) {
        NativeImpl.bid128ScaleByPowerOfTen(a.low, a.high, n, r);
    }

    public static void average(final Decimal128Fields a, final Decimal128Fields b, final Decimal128Fields r) {
        NativeImpl.bid128Mean2(a.low, a.high, b.low, b.high, r);
    }

    public static void mean(final Decimal128Fields a, final Decimal128Fields b, final Decimal128Fields r) {
        NativeImpl.bid128Mean2(a.low, a.high, b.low, b.high, r);
    }

    /// endregion

    /// region Rounding

//    /**
//     * Returns the {@code DFP} value that is rounded according the selected rounding type.
//     *
//     * @param value     {@code DFP} argument to round
//     * @param n         the number of decimals to use when rounding the number
//     * @param roundType {@code RoundType} type of rounding
//     * @return {@code DFP} the rounded value
//     */
//    public static long round(final Decimal128Fields value, final int n, final RoundType roundType) {
//        return JavaImpl.round(value, n, roundType);
//    }

    /**
     * Returns the smallest (closest to negative infinity) {@code DFP} value that is greater than or equal to the
     * argument and is equal to a mathematical integer. Same as {@link Decimal128Utils#roundTowardsPositiveInfinity}
     * If the argument is not finite, its value is not changed
     *
     * @param x {@code DFP} argument
     * @param r If {@code DFP value} is finite, returns {@code value} rounded up to a mathematical integer,
     *          otherwise {@code value} is returned unchanged.
     * @see Decimal128Utils#roundTowardsPositiveInfinity
     */
    @Deprecated
    public static void ceil(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128RoundTowardsPositiveInfinity(x.low, x.high, r);
    }

    /**
     * Returns the smallest (closest to negative infinity) {@code DFP} value that is greater than or equal to the
     * argument and is equal to a mathematical integer. Same as {@link Decimal128Utils#roundTowardsPositiveInfinity}
     * If the argument is not finite, its value is not changed
     *
     * @param x {@code DFP} argument
     * @param r If {@code DFP value} is finite, returns {@code value} rounded up to a mathematical integer,
     *          otherwise {@code value} is returned unchanged.
     * @see Decimal128Utils#roundTowardsPositiveInfinity
     * otherwise {@code value} is returned unchanged.
     */
    @Decimal
    public static void ceiling(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128RoundTowardsPositiveInfinity(x.low, x.high, r);
    }

    /**
     * Returns the smallest (closest to negative infinity) {@code DFP} value that is greater than or equal to the
     * argument and is equal to a mathematical integer.
     * If the argument is not finite, its value is not changed
     *
     * @param x {@code DFP} argument
     * @param r If {@code DFP value} is finite, returns {@code value} rounded up to a mathematical integer,
     *          otherwise {@code value} is returned unchanged.
     */
    public static void roundTowardsPositiveInfinity(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128RoundTowardsPositiveInfinity(x.low, x.high, r);
    }

    /**
     * Returns the largest (closest to positive infinity) {@code DFP} value that is less than or equal to the
     * argument and is equal to a mathematical integer. Same as {@link Decimal128Utils#roundTowardsNegativeInfinity}
     *
     * @param x {@code DFP} argument
     * @param r If {@code DFP value} is finite, returns {@code value} rounded down to a mathematical integer,
     *          otherwise {@code value} is returned unchanged.
     * @see Decimal128Utils#roundTowardsNegativeInfinity
     */
    @Decimal
    public static void floor(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128RoundTowardsNegativeInfinity(x.low, x.high, r);
    }

    /**
     * Returns the largest (closest to positive infinity) {@code DFP} value that is less than or equal to the
     * argument and is equal to a mathematical integer.
     *
     * @param x {@code DFP} argument
     * @param r If {@code DFP value} is finite, returns {@code value} rounded down to an integer,
     *          otherwise {@code value} is returned unchanged.
     */
    @Decimal
    public static void roundTowardsNegativeInfinity(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128RoundTowardsNegativeInfinity(x.low, x.high, r);
    }

    /**
     * Returns nearest {@code DFP} value whose absolute value is the same or smaller than the value of the
     * argument and is equal to a mathematical integer. Same as {@link Decimal128Utils#roundTowardsZero}
     *
     * @param x {@code DFP} argument
     * @param r If {@code DFP value} is finite, returns {@code value} rounded towards zero to an integer,
     *          otherwise {@code value} is returned unchanged.
     * @see Decimal128Utils#roundTowardsZero
     */
    public static void truncate(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128RoundTowardsZero(x.low, x.high, r);
    }

    /**
     * Returns nearest {@code DFP} value whose absolute value is the same or smaller than the value of the
     * argument and is equal to a mathematical integer.
     *
     * @param x {@code DFP} argument
     * @param r If {@code DFP value} is finite, returns {@code value} rounded towards zero to an integer,
     *          otherwise {@code value} is returned unchanged.
     */
    public static void roundTowardsZero(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128RoundTowardsZero(x.low, x.high, r);
    }

    /**
     * Returns {@code DFP} value that is nearest to the first argument and a multiple of the second {@code DFP} argument,
     * with ties rounding away from zero. Same as {@link Decimal128Utils#roundToNearestTiesAwayFromZero}
     * <p>
     * Example: {@code roundToNearestTiesAwayFromZero(Decimal128Utils.parse("1.234"), Decimal128Utils.parse("0.05"))} returns {@code 1.25}
     *
     * @param value    {@code DFP} argument
     * @param multiple rounding precision expressed as {@code DFP} number (e.g. 0.001 will round to 3 digits after decimal point)
     * @param r        If {@code DFP value} is finite, returns {@code value} rounded to a multiple of {@code multiple},
     *                 otherwise {@code value} is returned unchanged.
     * @see Decimal128Utils#roundToNearestTiesAwayFromZero
     */
    public static void round(final Decimal128Fields value, final Decimal128Fields multiple, final Decimal128Fields r) {
        roundToNearestTiesAwayFromZero(value, multiple, r);
    }

    /**
     * Returns the nearest {@code DFP} value that is equal to a mathematical integer,
     * with ties rounding away from zero. Same as {@link Decimal128Utils#roundToNearestTiesAwayFromZero}
     *
     * @param x {@code DFP} argument
     * @param r the value of the argument rounded to the nearest mathematical integer
     * @see Decimal128Utils#roundToNearestTiesAwayFromZero
     */
    public static void round(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128RoundToNearestTiesAwayFromZero(x.low, x.high, r);
    }

    /**
     * Returns the nearest {@code DFP} value that is equal to a mathematical integer,
     * with ties rounding away from zero
     *
     * @param x {@code DFP} argument
     * @param r the value of the argument rounded to the nearest mathematical integer
     */
    public static void roundToNearestTiesAwayFromZero(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128RoundToNearestTiesAwayFromZero(x.low, x.high, r);
    }

    /**
     * Returns the smallest (closest to negative infinity) {@code DFP} value that is greater than or equal to the
     * argument and is equal to a mathematical integer.
     * <p>
     * Example: {@code roundTowardsPositiveInfinity(Decimal128Utils.parse("3.14"), Decimal128Utils.parse("0.5"))} returns {@code 3.5}
     * Example:
     * <pre>
     *  private static @Decimal long roundOrderPrice(final @Decimal long price, final Decimal128Fields tickSize, final Side side) {
     *      if (Decimal128Utils.isPositive(tickSize)) {
     *          return (side == Side.BUY) ?
     *              Decimal128Utils.roundTowardsNegativeInfinity(price, tickSize):
     *              Decimal128Utils.roundTowardsPositiveInfinity(price, tickSize);
     *      } else {
     *          return price;
     *      }
     *  }
     * </pre>
     *
     * @param value    {@code DFP} argument
     * @param multiple rounding precision expressed as {@code DFP} number (e.g. 0.001 will round to 3 digits after decimal point)
     * @param r        If {@code DFP value} is finite, returns {@code value} rounded up to a multiple of {@code multiple},
     *                 otherwise {@code value} is returned unchanged.
     */
    public static void roundTowardsPositiveInfinity(final Decimal128Fields value, final Decimal128Fields multiple, final Decimal128Fields r) {
        if (!isFinite(multiple) || isNonPositive(multiple))
            throw new IllegalArgumentException("Multiple must be a positive finite number.");
        if (isNaN(value)) {
            r.copyFrom(value);
            return;
        }

        divide(value, multiple, r);
        ceiling(r, r);
        multiply(r, multiple, r);
    }

    /**
     * Returns the largest (closest to positive infinity) {@code DFP} value that is less than or equal to the
     * argument and is equal to a mathematical integer.
     * <p>
     * Example: {@code roundTowardsNegativeInfinity(Decimal128Utils.parse("1.234"), Decimal128Utils.parse("0.05"))} returns {@code 1.20}
     * Example:
     * <pre>
     *  private static @Decimal long roundOrderPrice(final @Decimal long price, final Decimal128Fields tickSize, final Side side) {
     *      if (Decimal128Utils.isPositive(tickSize)) {
     *          return (side == Side.BUY) ?
     *              Decimal128Utils.roundTowardsNegativeInfinity(price, tickSize):
     *              Decimal128Utils.roundTowardsPositiveInfinity(price, tickSize);
     *      } else {
     *          return price;
     *      }
     *  }
     * </pre>
     *
     * @param value    {@code DFP} argument
     * @param multiple rounding precision expressed as {@code DFP} number (e.g. 0.001 will round to 3 digits after decimal point)
     * @param r        If {@code DFP value} is finite, returns {@code value} rounded down to a multiple of {@code multiple},
     *                 otherwise {@code value} is returned unchanged.
     */
    public static void roundTowardsNegativeInfinity(final Decimal128Fields value, final Decimal128Fields multiple, final Decimal128Fields r) {
        if (!isFinite(multiple) || isNonPositive(multiple))
            throw new IllegalArgumentException("Multiple must be a positive finite number.");
        if (isNaN(value)) {
            r.copyFrom(value);
            return;
        }

        divide(value, multiple, r);
        floor(r, r);
        multiply(r, multiple, r);
    }

    /**
     * Returns {@code DFP} value that is nearest to the first argument and a multiple of the second {@code DFP} argument,
     * with ties rounding away from zero.
     * <p>
     * Example: {@code roundToNearestTiesAwayFromZero(Decimal128Utils.parse("1.234"), Decimal128Utils.parse("0.05"))} returns {@code 1.25}
     *
     * @param value    {@code DFP} argument
     * @param multiple rounding precision expressed as {@code DFP} number (e.g. 0.001 will round to 3 digits after decimal point)
     * @param r        If {@code DFP value} is finite, returns {@code value} rounded to a multiple of {@code multiple},
     *                 otherwise {@code value} is returned unchanged.
     */
    public static void roundToNearestTiesAwayFromZero(final Decimal128Fields value, final Decimal128Fields multiple, final Decimal128Fields r) {
        if (!isFinite(multiple) || isNonPositive(multiple))
            throw new IllegalArgumentException("Multiple must be a positive finite number.");
        if (isNaN(value)) {
            r.copyFrom(value);
            return;
        }

        divide(value, multiple, r);
        round(r, r);
        multiply(r, multiple, r);
    }

    /// endregion

    /// region Special

    /**
     * Returns smallest DFP value that is bigger than {@code value} (adjacent in the direction of positive infinity)
     *
     * @param x {@code DFP} argument as long
     * @param r adjacent DFP value in the direction of positive infinity
     */
    public static void nextUp(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128NextUp(x.low, x.high, r);
    }

    /**
     * Returns greatest DFP value that is smaller than {@code value} (adjacent in the direction of negative infinity)
     *
     * @param x {@code Decimal64} argument
     * @param r adjacent DFP value in the direction of negative infinity
     */
    public static void nextDown(final Decimal128Fields x, final Decimal128Fields r) {
        NativeImpl.bid128NextDown(x.low, x.high, r);
    }

//    /**
//     * Returns canonical representation of a DFP value.
//     * <p>
//     * e.g. {@code 12300(12300E100) -> 12300(123E102); 12.30000(1230000E-5) -> 12.3(123E-1); 0.00 -> 0}
//     * <p>
//     * We consider that all binary representations of one arithmetical value to have the same canonical binary representation.
//     * Canonical representation of zeros = {@link #ZERO}
//     * Canonical representation of NaNs/SNaN = {@link #NaN}
//     * Canonical representation of POSITIVE_INFINITY = {@link #POSITIVE_INFINITY}
//     * Canonical representation of NEGATIVE_INFINITY = {@link #NEGATIVE_INFINITY}
//     *
//     * @param value {@code DFP} argument.
//     * @return Canonical representation of the {@code DFP} argument.
//     */
//    @Decimal
//    public static long canonize(final Decimal128Fields value) {
//        if (JavaImpl.isNonFinite(value)) {
//            return JavaImpl.isNaN(value) ? NaN : value & NEGATIVE_INFINITY;
//        }
//
//        return JavaImpl.canonizeFinite(value);
//    }

    /// endregion

    /// region Parsing & Formatting

//    /**
//     * Append string representation of {@code DFP} {@code value} to {@link Appendable} {@code appendable}
//     * <p>
//     * Same as {@code appendable.append(value.toString())}, but more efficient.
//     *
//     * @param value      {@code DFP64} argument
//     * @param appendable {@link Appendable} instance to which the string representation of the {@code value} will be appended
//     * @return the 2nd argument ({@link Appendable} {@code appendable})
//     * @throws IOException from {@link Appendable#append(char)}
//     */
//    public static Appendable appendTo(final Decimal128Fields value, final Appendable appendable) throws IOException {
//        return JavaImpl.appendTo(value, appendable);
//    }
//
//    /**
//     * Append string representation of {@code DFP} value to {@link StringBuilder} {@code sb}
//     * <p>
//     * Same as {@code sb.append(value.toString());}, but more efficient.
//     *
//     * @param value {@code DFP64} argument
//     * @param sb    {@link StringBuilder} instance to which the string representation of the {@code value} will be appended
//     * @return the value of 2nd argument ({@link StringBuilder} {@code sb})
//     */
//    public static StringBuilder appendTo(final Decimal128Fields value, final StringBuilder sb) {
//        try {
//            JavaImpl.appendTo(value, sb);
//            return sb;
//        } catch (final IOException exception) {
//            throw new RuntimeException("IO exception was unexpected.", exception);
//        }
//    }
//
//    /**
//     * Parses a dfp floating-point value from the given textual representation.
//     * <p>
//     * Besides regular floating-point values (possibly in scientific notation) the following special cases are accepted:
//     * <ul>
//     * <li>{@code +Inf}, {@code Inf}, {@code +Infinity}, {@code Infinity} in any character case result in
//     * {@code Decimal128Utils.POSITIVE_INFINITY}</li>
//     * <li>{@code -Inf}, {@code -Infinity} in any character case result in
//     * {@code Decimal128Utils.NEGATIVE_INFINITY}</li>
//     * <li>{@code +NaN}, {@code -NaN}, {@code NaN} in any character case result in
//     * {@code Decimal128Utils.NaN}</li>
//     * </ul>
//     *
//     * @param text       Textual representation of dfp floating-point value.
//     * @param startIndex Index of character to start parsing at.
//     * @param endIndex   Index of character to stop parsing at, non-inclusive.
//     * @return parsed 64-bit decimal floating point value.
//     * @throws NumberFormatException if {@code text} does not contain valid dfp value.
//     */
//    @Decimal
//    public static long parse(final CharSequence text, final int startIndex, final int endIndex) {
//        return JavaImpl.parse(text, startIndex, endIndex, JavaImpl.BID_ROUNDING_TO_NEAREST);
//    }
//
//    /**
//     * Parses a dfp floating-point value from the given textual representation.
//     * <p>
//     * Besides regular floating-point values (possibly in scientific notation) the following special cases are accepted:
//     * <ul>
//     * <li>{@code +Inf}, {@code Inf}, {@code +Infinity}, {@code Infinity} in any character case result in
//     * {@code Decimal128Utils.POSITIVE_INFINITY}</li>
//     * <li>{@code -Inf}, {@code -Infinity} in any character case result in
//     * {@code Decimal128Utils.NEGATIVE_INFINITY}</li>
//     * <li>{@code +NaN}, {@code -NaN}, {@code NaN} in any character case result in
//     * {@code Decimal128Utils.NaN}</li>
//     * </ul>
//     *
//     * @param text       Textual representation of dfp floating-point value.
//     * @param startIndex Index of character to start parsing at.
//     * @return parsed 64-bit decimal floating point value.
//     * @throws NumberFormatException if {@code text} does not contain valid dfp value.
//     */
//    @Decimal
//    public static long parse(final CharSequence text, final int startIndex) {
//        return parse(text, startIndex, text.length());
//    }
//
//    /**
//     * Parses a dfp floating-point value from the given textual representation.
//     * <p>
//     * Besides regular floating-point values (possibly in scientific notation) the following special cases are accepted:
//     * <ul>
//     * <li>{@code +Inf}, {@code Inf}, {@code +Infinity}, {@code Infinity} in any character case result in
//     * {@code Decimal128Utils.POSITIVE_INFINITY}</li>
//     * <li>{@code -Inf}, {@code -Infinity} in any character case result in
//     * {@code Decimal128Utils.NEGATIVE_INFINITY}</li>
//     * <li>{@code +NaN}, {@code -NaN}, {@code NaN} in any character case result in
//     * {@code Decimal128Utils.NaN}</li>
//     * </ul>
//     *
//     * @param text Textual representation of dfp floating-point value.
//     * @return parsed 64-bit decimal floating point value.
//     * @throws NumberFormatException if {@code text} does not contain a valid DFP value.
//     */
//    @Decimal
//    public static long parse(final CharSequence text) {
//        return parse(text, 0, text.length());
//    }
//
//    /**
//     * Tries to parse a dfp floating-point value from the given textual representation.
//     * Returns the default value in case of fail.
//     *
//     * @param text         Textual representation of dfp floating-point value.
//     * @param startIndex   Index of character to start parsing at.
//     * @param endIndex     Index of character to stop parsing at, non-inclusive.
//     * @param defaultValue Default value in case of fail.
//     * @return parsed 64-bit decimal floating point value.
//     */
//    @Decimal
//    public static long tryParse(final CharSequence text, final int startIndex, final int endIndex, final Decimal128Fields defaultValue) {
//        @Decimal long value = defaultValue;
//
//        try {
//            value = parse(text, startIndex, endIndex);
//        } catch (final NumberFormatException ignore) {
//            // ignore
//        }
//
//        return value;
//    }
//
//    /**
//     * Tries to parse a dfp floating-point value from the given textual representation.
//     * Returns the default value in case of fail.
//     *
//     * @param text         Textual representation of dfp floating-point value.
//     * @param startIndex   Index of character to start parsing at.
//     * @param defaultValue Default value in case of fail.
//     * @return parsed 64-bit decimal floating point value.
//     */
//    @Decimal
//    public static long tryParse(final CharSequence text, final int startIndex, final Decimal128Fields defaultValue) {
//        return tryParse(text, startIndex, text.length(), defaultValue);
//    }
//
//    /**
//     * Tries to parse a dfp floating-point value from the given textual representation.
//     * Returns the default value in case of fail.
//     *
//     * @param text         Textual representation of dfp floating-point value.
//     * @param defaultValue Default value in case of fail.
//     * @return parsed 64-bit decimal floating point value.
//     */
//    @Decimal
//    public static long tryParse(final CharSequence text, final Decimal128Fields defaultValue) {
//        return tryParse(text, 0, text.length(), defaultValue);
//    }
//
//    /// endregion
//
//    /// region Null-checking wrappers for non-static methods
//
//    static protected void checkNull(final Decimal128Fields value) {
//        if (isNull(value)) {
//            throw new NullPointerException();
//        }
//    }
//
//    static protected void checkNull(final Decimal128Fields a, final Decimal128Fields b) {
//        if (isNull(a) || isNull(b)) {
//            throw new NullPointerException();
//        }
//    }
//
//    /**
//     * Implements {@link Decimal64#toFixedPoint(int)}, adds null check; do not use directly.
//     *
//     * @param value          DFP argument
//     * @param numberOfDigits number of significant digits after decimal point
//     * @return ..
//     */
//    public static long toFixedPointChecked(final Decimal128Fields value, final int numberOfDigits) {
//        checkNull(value);
//        return toFixedPoint(value, numberOfDigits);
//    }
//
//    /**
//     * Implements {@link Decimal64#toDouble()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    public static double toDoubleChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return toDouble(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#toBigDecimal()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    public static BigDecimal toBigDecimalChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return toBigDecimal(value);
//    }
//
//    @Decimal
//    public static long fromLongChecked(final long value) {
//        return fromLong(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#toLong()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    public static long toLongChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return toLong(value);
//    }
//
//    @Decimal
//    public static long fromIntChecked(final int value) {
//        return fromInt(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#toInt()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    public static int toIntChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return toInt(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#isNaN()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    public static boolean isNaNChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return isNaN(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#isInfinity()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    public static boolean isInfinityChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return isInfinity(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#isPositiveInfinity()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    public static boolean isPositiveInfinityChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return isPositiveInfinity(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#isNegativeInfinity()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    public static boolean isNegativeInfinityChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return isNegativeInfinity(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#isFinite()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    public static boolean isFiniteChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return isFinite(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#isNormal()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    public static boolean isNormalChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return isNormal(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#isIdentical(Decimal64)}, adds null checks; do not use directly.
//     *
//     * @param a 1st DFP argument
//     * @param b 2nd DFP argument
//     * @return ..
//     */
//    public static boolean isIdenticalChecked(final Decimal128Fields a, final Decimal128Fields b) {
//        checkNull(a, b);
//        return a == b;
//    }
//
//    /**
//     * Implements {@link Decimal64#isIdentical(Decimal64)}, adds null checks; do not use directly.
//     *
//     * @param value DFP argument
//     * @param obj   {@link Object} argument
//     * @return ..
//     */
//    public static boolean isIdenticalChecked(final Decimal128Fields value, final Object obj) {
//        checkNull(value);
//        return obj instanceof Decimal64 && value == ((Decimal64) obj).value;
//    }
//
//    /**
//     * Implements {@link Decimal64#isEqual(Decimal64)}, adds null checks; do not use directly.
//     *
//     * @param a 1st DFP argument
//     * @param b 2nd DFP argument
//     * @return ..
//     */
//    public static boolean isEqualChecked(final Decimal128Fields a, final Decimal128Fields b) {
//        checkNull(a, b);
//        return isEqual(a, b);
//    }
//
//    /**
//     * Implements {@link Decimal64#isNotEqual(Decimal64)}, adds null checks; do not use directly.
//     *
//     * @param a 1st DFP argument
//     * @param b 2nd DFP argument
//     * @return ..
//     */
//    public static boolean isNotEqualChecked(final Decimal128Fields a, final Decimal128Fields b) {
//        checkNull(a, b);
//        return isNotEqual(a, b);
//    }
//
//    /**
//     * Implements {@link Decimal64#isLess(Decimal64)}, adds null checks; do not use directly.
//     *
//     * @param a 1st DFP argument
//     * @param b 2nd DFP argument
//     * @return ..
//     */
//    public static boolean isLessChecked(final Decimal128Fields a, final Decimal128Fields b) {
//        checkNull(a, b);
//        return isLess(a, b);
//    }
//
//    /**
//     * Implements {@link Decimal64#isLessOrEqual(Decimal64)}, adds null checks; do not use directly.
//     *
//     * @param a 1st DFP argument
//     * @param b 2nd DFP argument
//     * @return ..
//     */
//    public static boolean isLessOrEqualChecked(final Decimal128Fields a, final Decimal128Fields b) {
//        checkNull(a, b);
//        return isLessOrEqual(a, b);
//    }
//
//    /**
//     * Implements {@link Decimal64#isGreater(Decimal64)}, adds null checks; do not use directly.
//     *
//     * @param a 1st DFP argument
//     * @param b 2nd DFP argument
//     * @return ..
//     */
//    public static boolean isGreaterChecked(final Decimal128Fields a, final Decimal128Fields b) {
//        checkNull(a, b);
//        return isGreater(a, b);
//    }
//
//    /**
//     * Implements {@link Decimal64#isGreaterOrEqual(Decimal64)}, adds null checks; do not use directly.
//     *
//     * @param a 1st DFP argument
//     * @param b 2nd DFP argument
//     * @return ..
//     */
//    public static boolean isGreaterOrEqualChecked(final Decimal128Fields a, final Decimal128Fields b) {
//        checkNull(a, b);
//        return isGreaterOrEqual(a, b);
//    }
//
//    /**
//     * Implements {@link Decimal64#isZero()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    public static boolean isZeroChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return isZero(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#isNonZero()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    public static boolean isNonZeroChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return isNonZero(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#isPositive()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    public static boolean isPositiveChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return isPositive(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#isNegative()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    public static boolean isNegativeChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return isNegative(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#isNonPositive()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    public static boolean isNonPositiveChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return isNonPositive(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#isNonNegative()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    public static boolean isNonNegativeChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return isNonNegative(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#negate()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long negateChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return negate(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#abs()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long absChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return abs(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#add(Decimal64)}, adds null checks; do not use directly.
//     *
//     * @param a DFP argument
//     * @param b DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long addChecked(final Decimal128Fields a, final Decimal128Fields b) {
//        checkNull(a, b);
//        return add(a, b);
//    }
//
//    /**
//     * Implements {@link Decimal64#add(Decimal64, Decimal64)}, adds null checks; do not use directly.
//     *
//     * @param a DFP argument
//     * @param b DFP argument
//     * @param c DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long addChecked(final Decimal128Fields a, final Decimal128Fields b, final Decimal128Fields c) {
//        checkNull(a, b);
//        checkNull(c);
//        return add(a, b, c);
//    }
//
//    /**
//     * Implements {@link Decimal64#add(Decimal64, Decimal64, Decimal64)}, adds null checks; do not use directly.
//     *
//     * @param a DFP argument
//     * @param b DFP argument
//     * @param c DFP argument
//     * @param d DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long addChecked(final Decimal128Fields a, final Decimal128Fields b,
//                                  final Decimal128Fields c, final Decimal128Fields d) {
//        checkNull(a, b);
//        checkNull(c, d);
//        return add(a, b, c, d);
//    }
//
//    /**
//     * Implements {@link Decimal64#subtract(Decimal64)}, adds null checks; do not use directly.
//     *
//     * @param a DFP argument
//     * @param b DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long subtractChecked(final Decimal128Fields a, final Decimal128Fields b) {
//        checkNull(a, b);
//        return subtract(a, b);
//    }
//
//    /**
//     * Implements {@link Decimal64#multiply(Decimal64)}, adds null checks; do not use directly.
//     *
//     * @param a DFP argument
//     * @param b DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long multiplyChecked(final Decimal128Fields a, final Decimal128Fields b) {
//        checkNull(a, b);
//        return multiply(a, b);
//    }
//
//    /**
//     * Implements {@link Decimal64#multiply(Decimal64, Decimal64)}, adds null checks; do not use directly.
//     *
//     * @param a DFP argument
//     * @param b DFP argument
//     * @param c DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long multiplyChecked(final Decimal128Fields a, final Decimal128Fields b, final Decimal128Fields c) {
//        checkNull(a, b);
//        checkNull(c);
//        return multiply(a, b, c);
//    }
//
//    /**
//     * Implements {@link Decimal64#multiply(Decimal64, Decimal64, Decimal64)}, adds null checks; do not use directly.
//     *
//     * @param a DFP argument
//     * @param b DFP argument
//     * @param c DFP argument
//     * @param d DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long multiplyChecked(final Decimal128Fields a, final Decimal128Fields b,
//                                       final Decimal128Fields c, final Decimal128Fields d) {
//        checkNull(a, b);
//        checkNull(c, d);
//        return multiply(a, b, c, d);
//    }
//
//    /**
//     * Implements {@link Decimal64#divide(Decimal64)}, adds null checks; do not use directly.
//     *
//     * @param a DFP argument
//     * @param b DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long divideChecked(final Decimal128Fields a, final Decimal128Fields b) {
//        checkNull(a, b);
//        return divide(a, b);
//    }
//
//    /**
//     * Implements {@link Decimal64#multiplyByInteger(int)}, adds null check; do not use directly.
//     *
//     * @param a DFP argument
//     * @param b DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long multiplyByIntegerChecked(final Decimal128Fields a, final int b) {
//        checkNull(a);
//        return multiplyByInteger(a, b);
//    }
//
//    /**
//     * Implements {@link Decimal64#multiplyByInteger(long)}, adds null check; do not use directly.
//     *
//     * @param a DFP argument
//     * @param b DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long multiplyByIntegerChecked(final Decimal128Fields a, final long b) {
//        checkNull(a);
//        return multiplyByInteger(a, b);
//    }
//
//    /**
//     * Implements {@link Decimal64#divideByInteger(int)}, adds null check; do not use directly.
//     *
//     * @param a DFP argument
//     * @param b DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long divideByIntegerChecked(final Decimal128Fields a, final int b) {
//        checkNull(a);
//        return divideByInteger(a, b);
//    }
//
//    /**
//     * Implements {@link Decimal64#divideByInteger(long)}, adds null check; do not use directly.
//     *
//     * @param a DFP argument
//     * @param b DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long divideByIntegerChecked(final Decimal128Fields a, final long b) {
//        checkNull(a);
//        return divideByInteger(a, b);
//    }
//
//    /**
//     * Implements {@link Decimal64#multiplyAndAdd(Decimal64, Decimal64)}, adds null checks; do not use directly.
//     *
//     * @param a DFP argument
//     * @param b DFP argument
//     * @param c DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long multiplyAndAddChecked(final Decimal128Fields a, final Decimal128Fields b, final Decimal128Fields c) {
//        checkNull(a);
//        checkNull(b, c);
//        return multiplyAndAdd(a, b, c);
//    }
//
//    /**
//     * Implements {@link Decimal64#average(Decimal64)}, adds null checks; do not use directly.
//     *
//     * @param a DFP argument
//     * @param b DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long averageChecked(final Decimal128Fields a, final Decimal128Fields b) {
//        checkNull(a, b);
//        return average(a, b);
//    }
//
//    /**
//     * Implements {@link Decimal64#max(Decimal64)}, adds null checks; do not use directly.
//     *
//     * @param a DFP argument
//     * @param b DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long maxChecked(final Decimal128Fields a, final Decimal128Fields b) {
//        checkNull(a, b);
//        return max(a, b);
//    }
//
//    /**
//     * Implements {@link Decimal64#min(Decimal64)}, adds null checks; do not use directly.
//     *
//     * @param a DFP argument
//     * @param b DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long minChecked(final Decimal128Fields a, final Decimal128Fields b) {
//        checkNull(a, b);
//        return min(a, b);
//    }
//
//    /**
//     * Implements {@link Decimal64#ceil()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long ceilChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return ceil(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#floor()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long floorChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return floor(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#round()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long roundChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return round(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#round(Decimal64)}, adds null check; do not use directly.
//     *
//     * @param value    DFP argument
//     * @param multiple DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long roundChecked(final Decimal128Fields value, final long multiple) {
//        checkNull(value, multiple);
//        return round(value, multiple);
//    }
//
//    /**
//     * Implements {@link Decimal64#ceiling()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long ceilingChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return ceiling(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#roundTowardsPositiveInfinity()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long roundTowardsPositiveInfinityChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return roundTowardsPositiveInfinity(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#roundTowardsNegativeInfinity()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long roundTowardsNegativeInfinityChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return roundTowardsNegativeInfinity(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#truncate()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long truncateChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return truncate(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#roundTowardsZero()}, adds null checks; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long roundTowardsZeroChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return roundTowardsZeroChecked(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#roundToNearestTiesAwayFromZero()}, adds null checks; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long roundToNearestTiesAwayFromZeroChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return roundToNearestTiesAwayFromZero(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#roundTowardsPositiveInfinity()}, adds null checks; do not use directly.
//     *
//     * @param value    DFP argument
//     * @param multiple DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long roundTowardsPositiveInfinityChecked(final Decimal128Fields value, final Decimal128Fields multiple) {
//        checkNull(value);
//        return roundTowardsPositiveInfinity(value, multiple);
//    }
//
//    /**
//     * Implements {@link Decimal64#roundTowardsNegativeInfinity()}, adds null checks; do not use directly.
//     *
//     * @param value    DFP argument
//     * @param multiple DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long roundTowardsNegativeInfinityChecked(final Decimal128Fields value, final Decimal128Fields multiple) {
//        checkNull(value);
//        return roundTowardsNegativeInfinity(value, multiple);
//    }
//
//    /**
//     * Implements {@link Decimal64#roundToNearestTiesAwayFromZero()}, adds null checks; do not use directly.
//     *
//     * @param value    DFP argument
//     * @param multiple DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long roundToNearestTiesAwayFromZeroChecked(final Decimal128Fields value, final Decimal128Fields multiple) {
//        checkNull(value);
//        return roundToNearestTiesAwayFromZero(value, multiple);
//    }
//
//    /**
//     * Implements {@link Decimal64#identityHashCode()}, adds null checks; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    public static int identityHashCodeChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return identityHashCode(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#hashCode()}, adds null checks; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    public static int hashCodeChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return hashCode(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#appendTo(Appendable)}, adds null check; do not use directly.
//     *
//     * @param value      DFP argument
//     * @param appendable an object, implementing Appendable interface
//     * @return ..
//     * @throws IOException from {@link Appendable#append(char)}
//     */
//    public static Appendable appendToChecked(final Decimal128Fields value, final Appendable appendable) throws IOException {
//        checkNull(value);
//        return appendTo(value, appendable);
//    }
//
//    /**
//     * Implements {@link Decimal64#appendTo(StringBuilder)}, adds null check; do not use directly.
//     *
//     * @param value         DFP argument
//     * @param stringBuilder StringBuilder argument
//     * @return ..
//     */
//    public static StringBuilder appendToChecked(final Decimal128Fields value, final StringBuilder stringBuilder) {
//        checkNull(value);
//        return appendTo(value, stringBuilder);
//    }
//
//    /**
//     * Implements {@link Decimal64#toString()}, adds null checks; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    public static String toStringChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return toString(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#equals(Decimal64)}, adds null checks; do not use directly.
//     *
//     * @param a DFP argument
//     * @param b DFP argument
//     * @return ..
//     */
//    public static boolean equalsChecked(final Decimal128Fields a, final Decimal128Fields b) {
//        checkNull(a, b);
//        return equals(a, b);
//    }
//
//    /**
//     * Implements {@link Decimal64#equals(Object)}, adds null checks; do not use directly.
//     *
//     * @param a DFP argument
//     * @param b {@code Object}
//     * @return ..
//     */
//    public static boolean equalsChecked(final Decimal128Fields a, final Object b) {
//        checkNull(a);
//        return equals(a, ((Decimal64) b).value);
//    }
//
//    /**
//     * Implements {@link Decimal64#nextUp()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long nextUpChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return nextUp(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#nextDown()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long nextDownChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return nextDown(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#canonize()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    @Decimal
//    public static long canonizeChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return canonize(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#intValue()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    public static int intValueChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return toInt(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#longValue()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    public static long longValueChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return toLong(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#floatValue()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    public static float floatValueChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return (float) toDouble(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#doubleValue()}, adds null check; do not use directly.
//     *
//     * @param value DFP argument
//     * @return ..
//     */
//    public static double doubleValueChecked(final Decimal128Fields value) {
//        checkNull(value);
//        return toDouble(value);
//    }
//
//    /**
//     * Implements {@link Decimal64#compareTo(Decimal64)}, adds null checks; do not use directly.
//     *
//     * @param a DFP argument
//     * @param b DFP argument
//     * @return ..
//     */
//    public static int compareToChecked(final Decimal128Fields a, final Decimal128Fields b) {
//        checkNull(a, b);
//        return compareTo(a, b);
//    }
//
//    /**
//     * Implements {@link Comparable#compareTo(Object)} for {@link Decimal64} (type erasure for {@code Comparable<Decimal64>})
//     * Compares the value of {@code a} to an {@link Object}, while checking {@code a} for {@code null} constant.
//     * Do not use directly.
//     *
//     * @param a DFP argument
//     * @param b Object argument
//     * @return comparison result
//     * @throws NullPointerException if a or b are null
//     * @throws ClassCastException   if the second argument is not {@link Decimal64}
//     * @see Decimal64#compareTo(Decimal64)
//     * @see Decimal128Utils#compareTo(long, long)
//     */
//    public static int compareToChecked(final Decimal128Fields a, final Object b) {
//        checkNull(a);
//        return compareTo(a, ((Decimal64) b).value);
//    }
//
//    /// endregion
//
//    /// region Array boxing/unboxing (array conversions from long[] / to long[])
//
//    /**
//     * Converts an array of DFP64 values represented as {@code long} into array of {@link Decimal64} instances (performs boxing).
//     *
//     * @param src       source array
//     * @param srcOffset src offset
//     * @param dst       destination array
//     * @param dstOffset dst offset
//     * @param length    length
//     * @return The destination array ({@code dst})
//     */
//    public static Decimal64[] fromUnderlyingLongArray(final Decimal128Fields[] src, final int srcOffset, final Decimal64[] dst, final int dstOffset, final int length) {
//
//        final int srcLength = src.length;
//        final int srcEndOffset = srcOffset + length;
//
//        // NOTE: no bounds checks
//        for (int i = 0; i < length; ++i) {
//            dst[dstOffset + i] = Decimal64.fromUnderlying(src[srcOffset + i]);
//        }
//
//        return dst;
//    }
//
//    /**
//     * Converts an array of {@link Decimal64} instances into array of underlying {@code long} DFP values (performs unboxing).
//     *
//     * @param src       source array
//     * @param srcOffset src offset
//     * @param dst       destination array
//     * @param dstOffset dst offset
//     * @param length    length
//     * @return The destination array {@link Decimal64} {@code dst}
//     */
//    public static long[] toUnderlyingLongArray(final Decimal64[] src, final int srcOffset, final Decimal128Fields[] dst, final int dstOffset, final int length) {
//
//        final int srcLength = src.length;
//        final int srcEndOffset = srcOffset + length;
//
//        // NOTE: no bounds checks
//        for (int i = 0; i < length; ++i) {
//            dst[dstOffset + i] = Decimal64.toUnderlying(src[srcOffset + i]);
//        }
//
//        return dst;
//    }
//
//    /**
//     * Converts an array of DFP64 values represented as {@code long} into array of {@link Decimal64} instances (performs unboxing).
//     *
//     * @param src source array
//     * @return The destination array ({@code dst})
//     */
//    public static @Decimal
//    long[] toUnderlyingLongArray(final Decimal64[] src) {
//        return null == src ? null : toUnderlyingLongArray(src, 0, new long[src.length], 0, src.length);
//    }
//
//    /**
//     * Converts an array of {@link Decimal64} instances into array of underlying {@code long} DFP64 values (performs boxing).
//     *
//     * @param src source array
//     * @return The destination array {@link Decimal64} {@code dst}
//     */
//    public static Decimal64[] fromUnderlyingLongArray(final Decimal128Fields[] src) {
//        return null == src ? null : fromUnderlyingLongArray(src, 0, new Decimal64[src.length], 0, src.length);
//    }
//
//    /// endregion
}
