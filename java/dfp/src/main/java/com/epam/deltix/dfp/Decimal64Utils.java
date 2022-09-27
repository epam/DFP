package com.epam.deltix.dfp;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.epam.deltix.dfp.JavaImpl.isSpecial;
import static com.epam.deltix.dfp.JavaImplAdd.*;
import static com.epam.deltix.dfp.JavaImpl.BID_ROUNDING_TO_NEAREST;

/**
 * Contains common arithmetical routines for 64-bit Decimal Floating Point numbers as defined by IEEE-754 2008.
 * <p>
 * Decimal floating-point (DFP) arithmetic refers to both a representation and operations on dfp floating-point
 * numbers. Working directly with decimal (base-10) fractions can avoid the rounding errors that otherwise typically
 * occur when converting between decimal fractions (common in human-entered data, such as measurements or financial
 * information) and binary (base-2) fractions.
 */
public class Decimal64Utils {
    /// region Constants

    /**
     * A constant holding canonical representation of Not-a-Number DFP value (non-signaling NaN)
     */
    @Decimal
    public static final long NaN = JavaImpl.NaN;

    /**
     * A constant holding canonical representation of Positive Infinity value
     */
    @Decimal
    public static final long POSITIVE_INFINITY = JavaImpl.POSITIVE_INFINITY;

    /**
     * A constant holding canonical representation of Negative Infinity value
     */
    @Decimal
    public static final long NEGATIVE_INFINITY = JavaImpl.NEGATIVE_INFINITY;

    /**
     * Maximum number of significant decimal digits that 64-bit DFP value can store.
     */
    public static final int MAX_SIGNIFICAND_DIGITS = 16;

    /**
     * Number of bits used to represent a 64-bit DFP value ({@code 64}).
     */
    public static final int SIZE = Long.SIZE;

    /**
     * A constant holding the maximum possible DFP64 exponent for normalized values: {@code 384}
     */
    public static final int MAX_EXPONENT = JavaImpl.MAX_EXPONENT;

    /**
     * A constant holding the minimum possible DFP64 exponent for normalized values: {@code -383}
     */
    public static final int MIN_EXPONENT = JavaImpl.MIN_EXPONENT;

    /**
     * A constant holding the largest representable number: {@code 9999999999999999E+369}
     */
    @Decimal
    public static final long MAX_VALUE = JavaImpl.MAX_VALUE;

    /**
     * A constant holding the smallest representable number: {@code -9999999999999999E+369}
     */
    @Decimal
    public static final long MIN_VALUE = JavaImpl.MIN_VALUE;

    /**
     * A constant holding the smallest representable positive number: {@code 1E-398}
     */
    @Decimal
    public static final long MIN_POSITIVE_VALUE = JavaImpl.MIN_POSITIVE_VALUE;

    /**
     * A constant holding the largest representable negative number: {@code -1E-398}
     */
    @Decimal
    public static final long MAX_NEGATIVE_VALUE = JavaImpl.MAX_NEGATIVE_VALUE;

    /**
     * Zero: {@code 0}
     */
    @Decimal
    public static final long ZERO = JavaImpl.ZERO;

    /**
     * One: {@code 1}
     */
    @Decimal
    public static final long ONE = Decimal64Utils.fromInt(1);

    /**
     * Two: {@code 2}
     */
    @Decimal
    public static final long TWO = Decimal64Utils.fromInt(2);

    /**
     * Ten: {@code 10}
     */
    @Decimal
    public static final long TEN = Decimal64Utils.fromInt(10);

    /**
     * One Hundred: {@code 100}
     */
    @Decimal
    public static final long HUNDRED = Decimal64Utils.fromInt(100);

    /**
     * One Thousand: {@code 1000}
     */
    @Decimal
    public static final long THOUSAND = Decimal64Utils.fromInt(1000);

    /**
     * One million: {@code 1000_000}
     */
    @Decimal
    public static final long MILLION = Decimal64Utils.fromInt(1000_000);

    /**
     * One tenth: {@code 0.1}
     */
    @Decimal
    public static final long ONE_TENTH = JavaImpl.fromFixedPointFastUnchecked(1, 1);

    /**
     * One hundredth: {@code 0.01}
     */
    @Decimal
    public static final long ONE_HUNDREDTH = JavaImpl.fromFixedPointFastUnchecked(1, 2);

    /**
     * The value corresponding to {@code Decimal64.NULL} / Java {@code null} constant
     *
     * @see Decimal64#NULL
     */
    @Decimal
    public static final long NULL = JavaImpl.NULL;

    /// endregion

    /// region Private constants

    private static final int NaN_HASH_CODE = 1;
    private static final int POSITIVE_INF_HASH_CODE = 2;
    private static final int NEGATIVE_INF_HASH_CODE = 3;

    /// endregion

    /// region Object Implementation

    /**
     * Returns a hash code for the binary representation of the supplied DFP value.
     *
     * @param value Given decimal.
     * @return HashCode of given decimal.
     */
    public static int identityHashCode(final long value) {
        return (int) (value ^ (value >>> 32));
    }

    /**
     * Returns a hash code for the arithmetical value of the supplied {@code DFP} value.
     * Does not distinguish between different {@code DFP} representations of the same arithmetical values.
     * <p>
     * We consider that all POSITIVE_INFINITYs have equal hashCode,
     * all NEGATIVE_INFINITYs have equal hashCode,
     * all NaNs have equal hashCode.
     *
     * @param value the DFP value whose hash code is being calculated
     * @return HashCode for the gfiven DFP.
     */
    public static int hashCode(@Decimal final long value) {
        if (JavaImpl.isNonFinite(value)) {
            return JavaImpl.isNaN(value) ? NaN_HASH_CODE : value >= 0 ? POSITIVE_INF_HASH_CODE : NEGATIVE_INF_HASH_CODE;
        }

        final long canonizeValue = JavaImpl.canonizeFinite(value);
        return (int) (canonizeValue ^ (canonizeValue >>> 32));
    }


    public static String toString(@Decimal final long value) {
        return JavaImpl.fastToString(value);
    }

    public static String toScientificString(@Decimal final long value) {
        return JavaImpl.fastToScientificString(value);
    }

    static String toDebugString(@Decimal final long value) {
        return JavaImpl.toDebugString(value);
    }

    /**
     * Returns {@code true} if two {@code DFP} values represent same arithmetical value.
     * <p>
     * We consider that all possible encodings of {@link #POSITIVE_INFINITY} are equal,
     * all possible encodings of {@link #NEGATIVE_INFINITY} are equal,
     * all possible encodings of {@code NaN} and {@code SNaN} are equal,
     * all invalid encodings that aren't NaN or Infinity are equal to {@link #ZERO}.
     * Negative and Positive(default) {@link #ZERO} are equal.
     *
     * @param a the first {@code DFP} value.
     * @param b the second {@code DFP} value.
     * @return {@code true} if both DFP values represent the same arithmetical value
     * {@code false} otherwise
     * @see #equals(long, Object)
     * @see #equals(Object)
     * @see Decimal64#equals(Decimal64, Decimal64)
     */
    public static boolean equals(@Decimal final long a, @Decimal final long b) {
        final long canonizedA = canonize(a);
        final long canonizedB = canonize(b);
        return canonizedA == canonizedB;
    }

    /**
     * Return {@code true} if two decimals have exactly the same binary representation.
     *
     * @param a the first {@code DFP} value.
     * @param b the second {@code DFP} value.
     * @return {@code true} if two decimals have the same binary representation (same underlying value)
     * {@code false} otherwise
     */
    public static boolean isIdentical(@Decimal final long a, @Decimal final long b) {
        return a == b;
    }

    /**
     * Returns {@code true} if the second value is {@link Decimal64} and its value if the same as the the first one
     *
     * @param a the first {@code DFP} value.
     * @param b the second value, will be compared with the first argument.
     * @return {@code true} if the second argument is of type {@link Decimal64} that wraps value that is is the same as
     * the first argument.
     * {@code false} otherwise.
     */
    public static boolean isIdentical(@Decimal final long a, final Object b) {
        return (b == null && NULL == a) || (b instanceof Decimal64 && (a == ((Decimal64) b).value));
    }

    /**
     * Returns {@code true} if two {@code DFP} values represent the same arithmetical value.
     * <p>
     * We consider that all possible encodings of {@link #POSITIVE_INFINITY} are equal,
     * all possible encodings of {@link #NEGATIVE_INFINITY} are equal,
     * all possible encodings of NaN and SNaN are equal,
     * all invalid encodings of finite values equal {@link #ZERO}
     *
     * @param a the first 64-bit DFP value.
     * @param b the second 64-bit DFP value.
     * @return {@code true} if two decimal values represent the same arithmetical value.
     */
    public static boolean equals(@Decimal final long a, final Object b) {
        return (b == null && NULL == a)
            || (b instanceof Decimal64 && equals(a, ((Decimal64) b).value));
    }

    /**
     * Returns {@code true} if the supplied DFP64 value equals special {@code NULL} constant
     * that corresponds to null reference of type {@link Decimal64}.
     *
     * @param value the DFP value being checked
     * @return {@code true}, if {@code NULL}
     * {@code false} otherwise
     */
    public static boolean isNull(@Decimal final long value) {
        return JavaImpl.isNull(value);
    }

    /// endregion

    /// region Conversion

    @Decimal
    public static long fromUnderlying(@Decimal final long value) {
        return value;
    }

    @Decimal
    public static long toUnderlying(@Decimal final long value) {
        return value;
    }

    /**
     * Create {@code @Decimal long} value from {@code BigDecimal} binary floating point value.
     * <p>Note that not all binary FP values can be exactly represented as decimal FP values.
     *
     * @param value source {@code BigDecimal} binary floating point value
     * @return new {@code Decimal long} value
     */
    @Decimal
    public static long fromBigDecimal(final BigDecimal value) {
        return JavaImpl.fromBigDecimal(value, JavaImpl.BID_ROUNDING_TO_NEAREST);
    }

    /**
     * Create {@code @Decimal long} from {@code BigDecimal} binary floating point value.
     *
     * @param value source {@code BigDecimal} binary floating point value
     * @return new {@code @Decimal long} value
     * @throws IllegalArgumentException if the value can't be converted to {@code @Decimal long} without precision loss
     */
    @Decimal
    public static long fromBigDecimalExact(final BigDecimal value) {
        return JavaImpl.fromBigDecimal(value, JavaImpl.BID_ROUNDING_EXCEPTION);
    }

    /**
     * Convert {@code @Decimal long} value to {@code BigDecimal} binary floating point value.
     * <p>Note that not all decimal FP values can be exactly represented as binary FP values.
     *
     * @param value 64-bit floating point dfp value.
     * @return {@code BigDecimal} value
     */
    public static BigDecimal toBigDecimal(@Decimal final long value) {
        return JavaImpl.toBigDecimal(value);
    }

    /**
     * Converts fixed-point dfp to dfp floating-point value.
     * <p>
     * Essentially, the value behind the fixed-point dfp representation can be
     * computed as {@code mantissa} divided by 10 to the power of {@code numberOfDigits}.
     * <p>
     * E.g., 1.23 can be represented as pair (1230, 3), where 1230 is a mantissa, and 3 is a number of digits after
     * the dot. Thus, {@code Decimal64Utils.fromFixedPoint(1230, 3)} will return a dfp floating-point representation of
     * value {@code 1.23}.
     *
     * @param mantissa       Integer part of fixed-point dfp.
     * @param numberOfDigits Number of digits after the dot.
     * @return New 64-bit floating point dfp value.
     */
    @Decimal
    public static long fromFixedPoint(final long mantissa, final int numberOfDigits) {
        return JavaImplEtc.bid64_scalbn(JavaImplCast.bid64_from_int64(mantissa, BID_ROUNDING_TO_NEAREST), -numberOfDigits);
    }

    /**
     * Overload of {@link #fromFixedPoint(long, int)} for mantissa representable by {@code int}.
     * Faster than the full-range version.
     *
     * @param mantissa       source fixed point value represented as {@code int}
     * @param numberOfDigits number of decimal digits representing fractional part
     * @return New 64-bit floating point dfp value.
     */
    @Decimal
    public static long fromFixedPoint(final int mantissa, final int numberOfDigits) {
        return JavaImpl.fromFixedPoint32(mantissa, numberOfDigits);
    }


    @Decimal
    public static long fromDecimalDouble(final double value) {
        return JavaImpl.fromDecimalDouble(value);
    }

    /**
     * Converts from floating-point dfp representation to fixed-point with given number of digits after the dot.
     * <p>
     * For example, dfp value of 1.23 can be represented as pair (1230, 3), where 1230 is a mantissa, and 3 is
     * a number of digits after the dot. Essentially, the value behind the fixed-point dfp representation can be
     * computed as {@code mantissa} divided by 10 to the power of {@code numberOfDigits}.
     *
     * @param value          64-bit floating point dfp value.
     * @param numberOfDigits Number of digits after the dot.
     * @return fixed-point decimal value represented as @{code long}
     */
    public static long toFixedPoint(@Decimal final long value, final int numberOfDigits) {
        return JavaImplCast.bid64_to_int64_xint(JavaImplEtc.bid64_scalbn(value, numberOfDigits));
    }

    /**
     * Create {@code DFP} value from 64-bit binary floating point ({@code double}) value.
     *
     * @param value source 64-bit binary floating point value
     * @return New {@code DFP} value.
     */
    @Decimal
    public static long fromDouble(final double value) {
        return JavaImplCastBinary64.binary64_to_bid64(value, BID_ROUNDING_TO_NEAREST);
    }

    /**
     * Convert {@code DFP} value to 64-bit binary floating point ({@code double}) value.
     * <p>Note that not all decimal FP values can be exactly represented as binary FP values.
     *
     * @param value source {@code DFP} value
     * @return {@code double} value
     */
    public static double toDouble(@Decimal final long value) {
        return JavaImplCastBinary64.bid64_to_binary64(value, BID_ROUNDING_TO_NEAREST);
    }

    /**
     * Create {@code DFP} value from {@code long} integer.
     *
     * @param value source {@code long} integer value
     * @return New {@code DFP} value.
     */
    @Decimal
    public static long fromLong(final long value) {
        return JavaImplCast.bid64_from_int64(value, BID_ROUNDING_TO_NEAREST);
    }

    /**
     * Convert {@code DFP} value to {@code long} integer value by truncating fractional part towards zero.
     * <p>Does not throw exceptions on overflow or invalid data
     *
     * @param value {@code DFP} value
     * @return {@code long} integer value
     */
    public static long toLong(@Decimal final long value) {
        return JavaImplCast.bid64_to_int64_xint(value);
    }

    /**
     * Create {@code DFP} value from 32-bit integer ({@code int}).
     * <p>faster than creating from ({@code long})
     *
     * @param value source integer value
     * @return new {@code DFP} value
     */
    @Decimal
    public static long fromInt(final int value) {
        return JavaImpl.fromInt32(value);
    }

    /**
     * Convert {@code DFP} value to {@code int} value by truncating fractional part towards zero.
     * <p>Does not throw exceptions on overflow or invalid data
     *
     * @param value {@code DFP} value
     * @return {@code int} value
     */
    public static int toInt(@Decimal final long value) {
        return (int) JavaImplCast.bid64_to_int64_xint(value);
    }

    /// endregion

    /// region Classification

    public static boolean isNaN(@Decimal final long value) {
        return JavaImpl.isNaN(value);
    }

    public static boolean isInfinity(@Decimal final long value) {
        return JavaImpl.isInfinity(value);
    }

    public static boolean isPositiveInfinity(@Decimal final long value) {
        return JavaImpl.isPositiveInfinity(value);
    }

    public static boolean isNegativeInfinity(@Decimal final long value) {
        return JavaImpl.isNegativeInfinity(value);
    }

    public static boolean isFinite(@Decimal final long value) {
        return JavaImpl.isFinite(value);
    }

    public static boolean isNonFinite(@Decimal final long value) {
        return JavaImpl.isNonFinite(value);
    }

    public static boolean isNormal(@Decimal final long value) {
        return JavaImplCmp.bid64_isNormal(value);
    }

    /// endregion

    /// region Comparison

    public static int compareTo(@Decimal final long a, @Decimal final long b) {
        return JavaImplCmp.compare(a, b);
    }

    public static boolean isEqual(@Decimal final long a, @Decimal final long b) {
        return JavaImplCmp.bid64_quiet_equal(a, b);
    }

    public static boolean isNotEqual(@Decimal final long a, @Decimal final long b) {
        return JavaImplCmp.bid64_quiet_not_equal(a, b);
    }

    public static boolean isLess(@Decimal final long a, @Decimal final long b) {
        return JavaImplCmp.bid64_quiet_less(a, b);
    }

    public static boolean isLessOrEqual(@Decimal final long a, @Decimal final long b) {
        return JavaImplCmp.bid64_quiet_less_equal(a, b);
    }

    public static boolean isGreater(@Decimal final long a, @Decimal final long b) {
        return JavaImplCmp.bid64_quiet_greater(a, b);
    }

    public static boolean isGreaterOrEqual(@Decimal final long a, @Decimal final long b) {
        return JavaImplCmp.bid64_quiet_greater_equal(a, b);
    }

    public static boolean isZero(@Decimal final long value) {
        return JavaImplCmp.bid64_isZero(value);
    }

    public static boolean isNonZero(@Decimal final long value) {
        return !isZero(value);
    }

    public static boolean isPositive(@Decimal final long value) {
        return JavaImplCmp.isPositive(value);
    }

    public static boolean isNegative(@Decimal final long value) {
        return JavaImplCmp.isNegative(value);
    }

    public static boolean isNonPositive(@Decimal final long value) {
        return JavaImplCmp.isNonPositive(value);
    }

    public static boolean isNonNegative(@Decimal final long value) {
        return JavaImplCmp.isNonNegative(value);
    }

    /// endregion

    /// region Minimum & Maximum

    @Decimal
    public static long max(@Decimal final long a, @Decimal final long b) {
        return JavaImplMinMax.bid64_max_fix_nan(a, b);
    }

    @Decimal
    public static long max(@Decimal final long a, @Decimal final long b, @Decimal final long c) {
        return JavaImplMinMax.bid64_max_fix_nan(JavaImplMinMax.bid64_max_fix_nan(a, b), c);
    }

    @Decimal
    public static long max(@Decimal final long a, @Decimal final long b, @Decimal final long c, @Decimal final long d) {
        return JavaImplMinMax.bid64_max_fix_nan(
            JavaImplMinMax.bid64_max_fix_nan(a, b),
            JavaImplMinMax.bid64_max_fix_nan(c, d));
    }

    @Decimal
    public static long min(@Decimal final long a, @Decimal final long b) {
        return JavaImplMinMax.bid64_min_fix_nan(a, b);
    }

    @Decimal
    public static long min(@Decimal final long a, @Decimal final long b, @Decimal final long c) {
        return JavaImplMinMax.bid64_min_fix_nan(JavaImplMinMax.bid64_min_fix_nan(a, b), c);
    }

    @Decimal
    public static long min(@Decimal final long a, @Decimal final long b, @Decimal final long c, @Decimal final long d) {
        return JavaImplMinMax.bid64_min_fix_nan(
            JavaImplMinMax.bid64_min_fix_nan(a, b),
            JavaImplMinMax.bid64_min_fix_nan(c, d));
    }

    /// endregion

    /// region Arithmetic

    @Decimal
    public static long negate(@Decimal final long value) {
        return JavaImpl.negate(value);
    }

    @Decimal
    public static long abs(@Decimal final long value) {
        return JavaImpl.abs(value);
    }

    @Decimal
    public static long add(@Decimal final long a, @Decimal final long b) {
        return JavaImplAdd.bid64_add(a, b);
    }

    @Decimal
    public static long add(@Decimal final long a, @Decimal final long b, @Decimal final long c) {
        return JavaImplAdd.bid64_add(JavaImplAdd.bid64_add(a, b), c);
    }

    @Decimal
    public static long add(@Decimal final long a, @Decimal final long b, @Decimal final long c, @Decimal final long d) {
        return JavaImplAdd.bid64_add(JavaImplAdd.bid64_add(a, b), JavaImplAdd.bid64_add(c, d));
    }

    @Decimal
    public static long subtract(@Decimal final long a, @Decimal final long b) {
        return JavaImplAdd.bid64_sub(a, b);
    }

    @Decimal
    public static long multiply(@Decimal final long a, @Decimal final long b) {
        return JavaImplMul.bid64_mul(a, b);
    }

    @Decimal
    public static long multiply(@Decimal final long a, @Decimal final long b, @Decimal final long c) {
        return JavaImplMul.bid64_mul(JavaImplMul.bid64_mul(a, b), c);
    }

    @Decimal
    public static long multiply(@Decimal final long a, @Decimal final long b, @Decimal final long c, @Decimal final long d) {
        return JavaImplMul.bid64_mul(JavaImplMul.bid64_mul(a, b), JavaImplMul.bid64_mul(c, d));
    }

    @Decimal
    public static long multiplyByInteger(@Decimal final long a, final int b) {
        return JavaImplMul.bid64_mul(a, JavaImpl.fromInt32(b));
    }

    @Decimal
    public static long multiplyByInteger(@Decimal final long a, final long b) {
        return JavaImplMul.bid64_mul(a, JavaImplCast.bid64_from_int64(b, BID_ROUNDING_TO_NEAREST));
    }

    @Decimal
    public static long divide(@Decimal final long a, @Decimal final long b) {
        return JavaImplDiv.bid64_div(a, b);
    }

    @Decimal
    public static long divideByInteger(@Decimal final long a, final int b) {
        return JavaImplDiv.bid64_div(a, JavaImpl.fromInt32(b));
    }

    @Decimal
    public static long divideByInteger(@Decimal final long a, final long b) {
        return JavaImplDiv.bid64_div(a, JavaImplCast.bid64_from_int64(b, BID_ROUNDING_TO_NEAREST));
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
     * @return The value of {@code (a * b) + c} rounded once to {@code DFP} format.
     */
    @Decimal
    public static long multiplyAndAdd(@Decimal final long a, @Decimal final long b, @Decimal final long c) {
        return JavaImplFma.bid64_fma(a, b, c);
    }

    @Decimal
    public static long scaleByPowerOfTen(@Decimal final long a, final int n) {
        return JavaImplEtc.bid64_scalbn(a, n);
    }

    @Decimal
    public static long average(@Decimal final long a, @Decimal final long b) {
        return JavaImplDiv.mean2(a, b);
    }

    @Decimal
    public static long mean(@Decimal final long a, @Decimal final long b) {
        return JavaImplDiv.mean2(a, b);
    }

    /// endregion

    /// region Rounding

    /**
     * Returns the {@code DFP} value that is rounded according the selected rounding type.
     *
     * @param value     {@code DFP} argument to round
     * @param n         the number of decimals to use when rounding the number
     * @param roundType {@code RoundingMode} type of rounding
     * @return {@code DFP} the rounded value
     */
    @Decimal
    public static long round(@Decimal final long value, final int n, final RoundingMode roundType) {
        return JavaImpl.round(value, n, roundType);
    }

    /**
     * Returns the smallest (closest to negative infinity) {@code DFP} value that is greater than or equal to the
     * argument and is equal to a mathematical integer. Same as {@link Decimal64Utils#roundTowardsPositiveInfinity(long)}
     * If the argument is not finite, its value is not changed
     *
     * @param value {@code DFP} argument
     * @return If {@code DFP value} is finite, returns {@code value} rounded up to a mathematical integer,
     * otherwise {@code value} is returned unchanged.
     * @see Decimal64Utils#roundTowardsPositiveInfinity(long)
     */
    @Decimal
    @Deprecated
    public static long ceil(@Decimal final long value) {
        return JavaImplRound.bid64_round_integral_positive(value, BID_ROUNDING_TO_NEAREST);
    }

    /**
     * Returns the smallest (closest to negative infinity) {@code DFP} value that is greater than or equal to the
     * argument and is equal to a mathematical integer. Same as {@link Decimal64Utils#roundTowardsPositiveInfinity(long)}
     * If the argument is not finite, its value is not changed
     *
     * @param value {@code DFP} argument
     * @return If {@code DFP value} is finite, returns {@code value} rounded up to a mathematical integer,
     * otherwise {@code value} is returned unchanged.
     * @see Decimal64Utils#roundTowardsPositiveInfinity(long)
     * otherwise {@code value} is returned unchanged.
     */
    @Decimal
    public static long ceiling(@Decimal final long value) {
        return JavaImplRound.bid64_round_integral_positive(value, BID_ROUNDING_TO_NEAREST);
    }

    /**
     * Returns the smallest (closest to negative infinity) {@code DFP} value that is greater than or equal to the
     * argument and is equal to a mathematical integer.
     * If the argument is not finite, its value is not changed
     *
     * @param value {@code DFP} argument
     * @return If {@code DFP value} is finite, returns {@code value} rounded up to a mathematical integer,
     * otherwise {@code value} is returned unchanged.
     */
    @Decimal
    public static long roundTowardsPositiveInfinity(@Decimal final long value) {
        return JavaImplRound.bid64_round_integral_positive(value, BID_ROUNDING_TO_NEAREST);
    }

    /**
     * Returns the largest (closest to positive infinity) {@code DFP} value that is less than or equal to the
     * argument and is equal to a mathematical integer. Same as {@link Decimal64Utils#roundTowardsNegativeInfinity(long)}
     *
     * @param value {@code DFP} argument
     * @return If {@code DFP value} is finite, returns {@code value} rounded down to a mathematical integer,
     * otherwise {@code value} is returned unchanged.
     * @see Decimal64Utils#roundTowardsNegativeInfinity(long)
     */
    @Decimal
    public static long floor(@Decimal final long value) {
        return JavaImplRound.bid64_round_integral_negative(value, BID_ROUNDING_TO_NEAREST);
    }

    /**
     * Returns the largest (closest to positive infinity) {@code DFP} value that is less than or equal to the
     * argument and is equal to a mathematical integer.
     *
     * @param value {@code DFP} argument
     * @return If {@code DFP value} is finite, returns {@code value} rounded down to an integer,
     * otherwise {@code value} is returned unchanged.
     */
    @Decimal
    public static long roundTowardsNegativeInfinity(@Decimal final long value) {
        return JavaImplRound.bid64_round_integral_negative(value, BID_ROUNDING_TO_NEAREST);
    }

    /**
     * Returns nearest {@code DFP} value whose absolute value is the same or smaller than the value of the
     * argument and is equal to a mathematical integer. Same as {@link Decimal64Utils#roundTowardsZero(long)}
     *
     * @param value {@code DFP} argument
     * @return If {@code DFP value} is finite, returns {@code value} rounded towards zero to an integer,
     * otherwise {@code value} is returned unchanged.
     * @see Decimal64Utils#roundTowardsZero(long)
     */
    @Decimal
    public static long truncate(@Decimal final long value) {
        return JavaImplRound.bid64_round_integral_zero(value, BID_ROUNDING_TO_NEAREST);
    }


    /**
     * Returns nearest {@code DFP} value whose absolute value is the same or smaller than the value of the
     * argument and is equal to a mathematical integer.
     *
     * @param value {@code DFP} argument
     * @return If {@code DFP value} is finite, returns {@code value} rounded towards zero to an integer,
     * otherwise {@code value} is returned unchanged.
     */
    @Decimal
    public static long roundTowardsZero(@Decimal final long value) {
        return JavaImplRound.bid64_round_integral_zero(value, BID_ROUNDING_TO_NEAREST);
    }

    /**
     * Returns {@code DFP} value that is nearest to the first argument and a multiple of the second {@code DFP} argument,
     * with ties rounding away from zero. Same as {@link Decimal64Utils#roundToNearestTiesAwayFromZero(long, long)}
     * <p>
     * Example: {@code roundToNearestTiesAwayFromZero(Decimal64Utils.parse("1.234"), Decimal64Utils.parse("0.05"))} returns {@code 1.25}
     *
     * @param value    {@code DFP} argument
     * @param multiple rounding precision expressed as {@code DFP} number (e.g. 0.001 will round to 3 digits after decimal point)
     * @return If {@code DFP value} is finite, returns {@code value} rounded to a multiple of {@code multiple},
     * otherwise {@code value} is returned unchanged.
     * @see Decimal64Utils#roundToNearestTiesAwayFromZero(long, long)
     */
    @Decimal
    public static long round(@Decimal final long value, @Decimal final long multiple) {
        return roundToNearestTiesAwayFromZero(value, multiple);
    }

    /**
     * Returns the nearest {@code DFP} value that is equal to a mathematical integer,
     * with ties rounding away from zero. Same as {@link Decimal64Utils#roundToNearestTiesAwayFromZero(long)}
     *
     * @param value {@code DFP} argument
     * @return the value of the argument rounded to the nearest mathematical integer
     * @see Decimal64Utils#roundToNearestTiesAwayFromZero(long)
     */
    @Decimal
    public static long round(@Decimal final long value) {
        return JavaImplRound.bid64_round_integral_nearest_away(value, BID_ROUNDING_TO_NEAREST);
    }

    /**
     * Returns the nearest {@code DFP} value that is equal to a mathematical integer,
     * with ties rounding away from zero
     *
     * @param value {@code DFP} argument
     * @return the value of the argument rounded to the nearest mathematical integer
     */
    @Decimal
    public static long roundToNearestTiesAwayFromZero(@Decimal final long value) {
        return JavaImplRound.bid64_round_integral_nearest_away(value, BID_ROUNDING_TO_NEAREST);
    }

    /**
     * Returns the nearest {@code DFP} value that is equal to a mathematical integer,
     * with ties rounding to even
     *
     * @param value {@code DFP} argument
     * @return the value of the argument rounded to the nearest mathematical integer
     */
    @Decimal
    public static long roundToNearestTiesToEven(@Decimal final long value) {
        return JavaImplRound.bid64_round_integral_nearest_even(value, BID_ROUNDING_TO_NEAREST);
    }

    /**
     * Returns the smallest (closest to negative infinity) {@code DFP} value that is greater than or equal to the
     * argument and is equal to a mathematical integer.
     * <p>
     * Example: {@code roundTowardsPositiveInfinity(Decimal64Utils.parse("3.14"), Decimal64Utils.parse("0.5"))} returns {@code 3.5}
     * Example:
     * <pre>
     *  private static @Decimal long roundOrderPrice(final @Decimal long price, @Decimal final long tickSize, final Side side) {
     *      if (Decimal64Utils.isPositive(tickSize)) {
     *          return (side == Side.BUY) ?
     *              Decimal64Utils.roundTowardsNegativeInfinity(price, tickSize):
     *              Decimal64Utils.roundTowardsPositiveInfinity(price, tickSize);
     *      } else {
     *          return price;
     *      }
     *  }
     * </pre>
     *
     * @param value    {@code DFP} argument
     * @param multiple rounding precision expressed as {@code DFP} number (e.g. 0.001 will round to 3 digits after decimal point)
     * @return If {@code DFP value} is finite, returns {@code value} rounded up to a multiple of {@code multiple},
     * otherwise {@code value} is returned unchanged.
     */
    @Decimal
    public static long roundTowardsPositiveInfinity(@Decimal final long value, @Decimal final long multiple) {
        if (!isFinite(multiple) || isNonPositive(multiple))
            throw new IllegalArgumentException("Multiple must be a positive finite number.");
        if (isNaN(value))
            return value;

        @Decimal final long ratio = ceiling(divide(value, multiple));
        return multiply(ratio, multiple);
    }

    /**
     * Returns the largest (closest to positive infinity) {@code DFP} value that is less than or equal to the
     * argument and is equal to a mathematical integer.
     * <p>
     * Example: {@code roundTowardsNegativeInfinity(Decimal64Utils.parse("1.234"), Decimal64Utils.parse("0.05"))} returns {@code 1.20}
     * Example:
     * <pre>
     *  private static @Decimal long roundOrderPrice(final @Decimal long price, @Decimal final long tickSize, final Side side) {
     *      if (Decimal64Utils.isPositive(tickSize)) {
     *          return (side == Side.BUY) ?
     *              Decimal64Utils.roundTowardsNegativeInfinity(price, tickSize):
     *              Decimal64Utils.roundTowardsPositiveInfinity(price, tickSize);
     *      } else {
     *          return price;
     *      }
     *  }
     * </pre>
     *
     * @param value    {@code DFP} argument
     * @param multiple rounding precision expressed as {@code DFP} number (e.g. 0.001 will round to 3 digits after decimal point)
     * @return If {@code DFP value} is finite, returns {@code value} rounded down to a multiple of {@code multiple},
     * otherwise {@code value} is returned unchanged.
     */
    @Decimal
    public static long roundTowardsNegativeInfinity(@Decimal final long value, @Decimal final long multiple) {
        if (!isFinite(multiple) || isNonPositive(multiple))
            throw new IllegalArgumentException("Multiple must be a positive finite number.");
        if (isNaN(value))
            return value;

        @Decimal final long ratio = floor(divide(value, multiple));
        return multiply(ratio, multiple);
    }

    /**
     * Returns {@code DFP} value that is nearest to the first argument and a multiple of the second {@code DFP} argument,
     * with ties rounding away from zero.
     * <p>
     * Example: {@code roundToNearestTiesAwayFromZero(Decimal64Utils.parse("1.234"), Decimal64Utils.parse("0.05"))} returns {@code 1.25}
     *
     * @param value    {@code DFP} argument
     * @param multiple rounding precision expressed as {@code DFP} number (e.g. 0.001 will round to 3 digits after decimal point)
     * @return If {@code DFP value} is finite, returns {@code value} rounded to a multiple of {@code multiple},
     * otherwise {@code value} is returned unchanged.
     */
    @Decimal
    public static long roundToNearestTiesAwayFromZero(@Decimal final long value, @Decimal final long multiple) {
        if (!isFinite(multiple) || isNonPositive(multiple))
            throw new IllegalArgumentException("Multiple must be a positive finite number.");
        if (isNaN(value))
            return value;

        @Decimal final long ratio = JavaImplRound.bid64_round_integral_nearest_away(divide(value, multiple), BID_ROUNDING_TO_NEAREST);
        return multiply(ratio, multiple);
    }

    /**
     * Returns {@code DFP} value that is nearest to the first argument and a multiple of the second {@code DFP} argument,
     * with ties rounding to even.
     *
     * @param value    {@code DFP} argument
     * @param multiple rounding precision expressed as {@code DFP} number (e.g. 0.001 will round to 3 digits after decimal point)
     * @return If {@code DFP value} is finite, returns {@code value} rounded to a multiple of {@code multiple},
     * otherwise {@code value} is returned unchanged.
     */
    @Decimal
    public static long roundToNearestTiesToEven(@Decimal final long value, @Decimal final long multiple) {
        if (!isFinite(multiple) || isNonPositive(multiple))
            throw new IllegalArgumentException("Multiple must be a positive finite number.");
        if (isNaN(value))
            return value;

        @Decimal final long ratio = JavaImplRound.bid64_round_integral_nearest_even(divide(value, multiple), BID_ROUNDING_TO_NEAREST);
        return multiply(ratio, multiple);
    }

    /// endregion

    /// region Parts processing

    /**
     * Returns the unscaled value of the {@code DFP} in the same way as {@link BigDecimal#unscaledValue()} do.
     * For abnormal values return {@code Long.MIN_VALUE}.
     *
     * @param value {@code DFP} argument as long
     * @return the unscaled value of {@code DFP} value.
     */
    public static long getUnscaledValue(@Decimal final long value) {
        return getUnscaledValue(value, Long.MIN_VALUE);
    }

    /**
     * Returns the unscaled value of the {@code DFP} in the same way as {@link BigDecimal#unscaledValue()} do.
     *
     * @param value          {@code DFP} argument as long
     * @param abnormalReturn The value returned for abnormal input values (NaN, +Inf, -Inf).
     * @return the unscaled value of {@code DFP} value.
     */
    public static long getUnscaledValue(@Decimal final long value, final long abnormalReturn) {
        final boolean sign = JavaImpl.signBit(value);

        if (!JavaImpl.isSpecial(value)) {
            final long coefficient = (value & SMALL_COEFF_MASK64);
            return sign ? -coefficient : coefficient;
        } else {
            // special encodings
            if ((value & INFINITY_MASK64) == INFINITY_MASK64) {
                return abnormalReturn;    // NaN or Infinity
            } else {
                long coeff = (value & LARGE_COEFF_MASK64) | LARGE_COEFF_HIGH_BIT64;
                if (UnsignedLong.isGreaterOrEqual(coeff, 10000000000000000L))
                    coeff = 0;
                return sign ? -coeff : coeff;
            }
        }
    }

    /**
     * Returns the scale of the {@code DFP} in the same way as {@link BigDecimal#scale()} do.
     * For abnormal values return {@code Integer.MIN_VALUE}.
     *
     * @param value {@code DFP} argument as long
     * @return the scale of {@code DFP} value.
     */
    public static int getScale(@Decimal final long value) {
        return getScale(value, Integer.MIN_VALUE);
    }

    /**
     * Returns the scale of the {@code DFP} in the same way as {@link BigDecimal#scale()} do.
     *
     * @param value          {@code DFP} argument as long
     * @param abnormalReturn The value returned for abnormal input values (NaN, +Inf, -Inf).
     * @return the scale of {@code DFP} value.
     */
    public static int getScale(@Decimal final long value, final int abnormalReturn) {
        if (!isSpecial(value)) {
            return -((int) ((value >>> EXPONENT_SHIFT_SMALL64) & EXPONENT_MASK64) - JavaImpl.EXPONENT_BIAS);
        } else {
            // special encodings
            if ((value & INFINITY_MASK64) == INFINITY_MASK64)
                return abnormalReturn;
            else
                return -((int) ((value >>> EXPONENT_SHIFT_LARGE64) & EXPONENT_MASK64) - JavaImpl.EXPONENT_BIAS);
        }
    }

    /// endregion

    /// region Special

    /**
     * Returns smallest DFP value that is bigger than {@code value} (adjacent in the direction of positive infinity)
     *
     * @param value {@code DFP} argument as long
     * @return adjacent DFP value in the direction of positive infinity
     */
    @Decimal
    public static long nextUp(@Decimal final long value) {
        return JavaImplEtc.bid64_nextup(value);
    }

    /**
     * Returns greatest DFP value that is smaller than {@code value} (adjacent in the direction of negative infinity)
     *
     * @param value {@code Decimal64} argument
     * @return adjacent DFP value in the direction of negative infinity
     */
    @Decimal
    public static long nextDown(@Decimal final long value) {
        return JavaImplEtc.bid64_nextdown(value);
    }

    /**
     * Returns canonical representation of a DFP value.
     * <p>
     * e.g. {@code 12300(12300E100) -> 12300(123E102); 12.30000(1230000E-5) -> 12.3(123E-1); 0.00 -> 0}
     * <p>
     * We consider that all binary representations of one arithmetical value to have the same canonical binary representation.
     * Canonical representation of zeros = {@link #ZERO}
     * Canonical representation of NaNs/SNaN = {@link #NaN}
     * Canonical representation of POSITIVE_INFINITY = {@link #POSITIVE_INFINITY}
     * Canonical representation of NEGATIVE_INFINITY = {@link #NEGATIVE_INFINITY}
     *
     * @param value {@code DFP} argument.
     * @return Canonical representation of the {@code DFP} argument.
     */
    @Decimal
    public static long canonize(@Decimal final long value) {
        if (JavaImpl.isNonFinite(value)) {
            return JavaImpl.isNaN(value) ? NaN : value & NEGATIVE_INFINITY;
        }

        return JavaImpl.canonizeFinite(value);
    }

    /// endregion

    /// region Parsing & Formatting

    /**
     * Append string representation of {@code DFP} {@code value} to {@link Appendable} {@code appendable}
     * <p>
     * Same as {@code appendable.append(value.toString())}, but more efficient.
     *
     * @param value      {@code DFP64} argument
     * @param appendable {@link Appendable} instance to which the string representation of the {@code value} will be appended
     * @return the 2nd argument ({@link Appendable} {@code appendable})
     * @throws IOException from {@link Appendable#append(char)}
     */
    public static Appendable appendTo(@Decimal final long value, final Appendable appendable) throws IOException {
        return JavaImpl.fastAppendToAppendable(value, appendable);
    }

    /**
     * Append string representation of {@code DFP} {@code value} to {@link Appendable} {@code appendable}
     * <p>
     * Same as {@code appendable.append(value.toString())}, but more efficient.
     *
     * @param value      {@code DFP64} argument
     * @param appendable {@link Appendable} instance to which the string representation of the {@code value} will be appended
     * @return the 2nd argument ({@link Appendable} {@code appendable})
     * @throws IOException from {@link Appendable#append(char)}
     */
    public static Appendable scientificAppendTo(@Decimal final long value, final Appendable appendable) throws IOException {
        return JavaImpl.fastScientificAppendToAppendable(value, appendable);
    }

    /**
     * Implements {@link Decimal64#scientificAppendTo(Appendable)}, adds null check; do not use directly.
     *
     * @param value      DFP argument
     * @param appendable an object, implementing Appendable interface
     * @return ..
     * @throws IOException from {@link Appendable#append(char)}
     */
    public static Appendable scientificAppendToChecked(@Decimal final long value, final Appendable appendable) throws IOException {
        checkNull(value);
        return scientificAppendTo(value, appendable);
    }

    /**
     * Append string representation of {@code DFP} value to {@link StringBuilder} {@code sb}
     * <p>
     * Same as {@code sb.append(value.toString());}, but more efficient.
     *
     * @param value {@code DFP64} argument
     * @param sb    {@link StringBuilder} instance to which the string representation of the {@code value} will be appended
     * @return the value of 2nd argument ({@link StringBuilder} {@code sb})
     */
    public static StringBuilder appendTo(@Decimal final long value, final StringBuilder sb) {
        return JavaImpl.fastAppendToStringBuilder(value, sb);
    }

    /**
     * Append string representation of {@code DFP} value to {@link StringBuilder} {@code sb}
     * <p>
     * Same as {@code sb.append(value.toString());}, but more efficient.
     *
     * @param value {@code DFP64} argument
     * @param sb    {@link StringBuilder} instance to which the string representation of the {@code value} will be appended
     * @return the value of 2nd argument ({@link StringBuilder} {@code sb})
     */
    public static StringBuilder scientificAppendTo(@Decimal final long value, final StringBuilder sb) {
        return JavaImpl.fastScientificAppendToStringBuilder(value, sb);
    }

    /**
     * Implements {@link Decimal64#scientificAppendTo(StringBuilder)}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @param sb    {@link StringBuilder} instance to which the string representation of the {@code value} will be appended
     * @return ..
     */
    public static StringBuilder scientificAppendToChecked(@Decimal final long value, final StringBuilder sb) {
        checkNull(value);
        return scientificAppendTo(value, sb);
    }

    /**
     * Try parse a dfp floating-point value from the given textual representation.
     * <p>
     * Besides regular floating-point values (possibly in scientific notation) the following special cases are accepted:
     * <ul>
     * <li>{@code +Inf}, {@code Inf}, {@code +Infinity}, {@code Infinity} in any character case result in
     * {@code Decimal64Utils.POSITIVE_INFINITY}</li>
     * <li>{@code -Inf}, {@code -Infinity} in any character case result in
     * {@code Decimal64Utils.NEGATIVE_INFINITY}</li>
     * <li>{@code +NaN}, {@code -NaN}, {@code NaN} in any character case result in
     * {@code Decimal64Utils.NaN}</li>
     * </ul>
     *
     * @param text       Textual representation of dfp floating-point value.
     * @param startIndex Index of character to start parsing at.
     * @param endIndex   Index of character to stop parsing at, non-inclusive.
     * @param outStatus  The parsing output status and value.
     * @return Return {@code true} if value was parsed exactly without rounding.
     * @throws NumberFormatException if {@code text} does not contain valid dfp value.
     */
    @Decimal
    public static boolean tryParse(final CharSequence text, final int startIndex, final int endIndex,
                                   final Decimal64Status outStatus) {
        outStatus.underlying = JavaImplParse.bid64_from_string(text, startIndex, endIndex, outStatus, JavaImpl.BID_ROUNDING_TO_NEAREST);
        return outStatus.isExact();
    }

    /**
     * Parses a dfp floating-point value from the given textual representation.
     * <p>
     * Besides regular floating-point values (possibly in scientific notation) the following special cases are accepted:
     * <ul>
     * <li>{@code +Inf}, {@code Inf}, {@code +Infinity}, {@code Infinity} in any character case result in
     * {@code Decimal64Utils.POSITIVE_INFINITY}</li>
     * <li>{@code -Inf}, {@code -Infinity} in any character case result in
     * {@code Decimal64Utils.NEGATIVE_INFINITY}</li>
     * <li>{@code +NaN}, {@code -NaN}, {@code NaN} in any character case result in
     * {@code Decimal64Utils.NaN}</li>
     * </ul>
     *
     * @param text       Textual representation of dfp floating-point value.
     * @param startIndex Index of character to start parsing at.
     * @param endIndex   Index of character to stop parsing at, non-inclusive.
     * @return parsed 64-bit decimal floating point value.
     * @throws NumberFormatException if {@code text} does not contain valid dfp value.
     */
    @Decimal
    public static long parse(final CharSequence text, final int startIndex, final int endIndex) {
        JavaImplParse.FloatingPointStatusFlag fpsf = tlsFpst.get();
        final long ret = JavaImplParse.bid64_from_string(text, startIndex, endIndex, fpsf, JavaImpl.BID_ROUNDING_TO_NEAREST);
        if ((fpsf.status & JavaImplParse.BID_INVALID_FORMAT) != 0)
            throw new NumberFormatException("Input string is not in a correct format.");
//        else if ((fpsf.value & JavaImplParse.BID_INEXACT_EXCEPTION) != 0)
//        	throw new NumberFormatException("Can't convert input string to value without precision loss.");
        return ret;
    }

    private static final ThreadLocal<JavaImplParse.FloatingPointStatusFlag> tlsFpst =
        new ThreadLocal<JavaImplParse.FloatingPointStatusFlag>() {
            @Override
            protected JavaImplParse.FloatingPointStatusFlag initialValue() {
                return new JavaImplParse.FloatingPointStatusFlag();
            }
        };

    /**
     * Parses a dfp floating-point value from the given textual representation.
     * <p>
     * Besides regular floating-point values (possibly in scientific notation) the following special cases are accepted:
     * <ul>
     * <li>{@code +Inf}, {@code Inf}, {@code +Infinity}, {@code Infinity} in any character case result in
     * {@code Decimal64Utils.POSITIVE_INFINITY}</li>
     * <li>{@code -Inf}, {@code -Infinity} in any character case result in
     * {@code Decimal64Utils.NEGATIVE_INFINITY}</li>
     * <li>{@code +NaN}, {@code -NaN}, {@code NaN} in any character case result in
     * {@code Decimal64Utils.NaN}</li>
     * </ul>
     *
     * @param text       Textual representation of dfp floating-point value.
     * @param startIndex Index of character to start parsing at.
     * @return parsed 64-bit decimal floating point value.
     * @throws NumberFormatException if {@code text} does not contain valid dfp value.
     */
    @Decimal
    public static long parse(final CharSequence text, final int startIndex) {
        return parse(text, startIndex, text.length());
    }

    /**
     * Parses a dfp floating-point value from the given textual representation.
     * <p>
     * Besides regular floating-point values (possibly in scientific notation) the following special cases are accepted:
     * <ul>
     * <li>{@code +Inf}, {@code Inf}, {@code +Infinity}, {@code Infinity} in any character case result in
     * {@code Decimal64Utils.POSITIVE_INFINITY}</li>
     * <li>{@code -Inf}, {@code -Infinity} in any character case result in
     * {@code Decimal64Utils.NEGATIVE_INFINITY}</li>
     * <li>{@code +NaN}, {@code -NaN}, {@code NaN} in any character case result in
     * {@code Decimal64Utils.NaN}</li>
     * </ul>
     *
     * @param text Textual representation of dfp floating-point value.
     * @return parsed 64-bit decimal floating point value.
     * @throws NumberFormatException if {@code text} does not contain a valid DFP value.
     */
    @Decimal
    public static long parse(final CharSequence text) {
        return parse(text, 0, text.length());
    }

    /**
     * Tries to parse a dfp floating-point value from the given textual representation.
     * Returns the default value in case of fail.
     *
     * @param text         Textual representation of dfp floating-point value.
     * @param startIndex   Index of character to start parsing at.
     * @param endIndex     Index of character to stop parsing at, non-inclusive.
     * @param defaultValue Default value in case of fail.
     * @return parsed 64-bit decimal floating point value.
     */
    @Decimal
    public static long tryParse(final CharSequence text, final int startIndex, final int endIndex, @Decimal final long defaultValue) {
        JavaImplParse.FloatingPointStatusFlag fpsf = tlsFpst.get();
        final long ret = JavaImplParse.bid64_from_string(text, startIndex, endIndex, fpsf, JavaImpl.BID_ROUNDING_TO_NEAREST);
        if ((fpsf.status & JavaImplParse.BID_INVALID_FORMAT) != 0)
            return defaultValue;
//        else if ((fpsf.value & JavaImplParse.BID_INEXACT_EXCEPTION) != 0)
//        	throw new NumberFormatException("Can't convert input string to value without precision loss.");
        return ret;
    }

    /**
     * Tries to parse a dfp floating-point value from the given textual representation.
     * Returns the default value in case of fail.
     *
     * @param text         Textual representation of dfp floating-point value.
     * @param startIndex   Index of character to start parsing at.
     * @param defaultValue Default value in case of fail.
     * @return parsed 64-bit decimal floating point value.
     */
    @Decimal
    public static long tryParse(final CharSequence text, final int startIndex, @Decimal final long defaultValue) {
        return tryParse(text, startIndex, text.length(), defaultValue);
    }

    /**
     * Tries to parse a dfp floating-point value from the given textual representation.
     * Returns the default value in case of fail.
     *
     * @param text         Textual representation of dfp floating-point value.
     * @param defaultValue Default value in case of fail.
     * @return parsed 64-bit decimal floating point value.
     */
    @Decimal
    public static long tryParse(final CharSequence text, @Decimal final long defaultValue) {
        return tryParse(text, 0, text.length(), defaultValue);
    }

    /// endregion

    /// region Null-checking wrappers for non-static methods

    static protected void checkNull(@Decimal final long value) {
        if (isNull(value)) {
            throw new NullPointerException();
        }
    }

    static protected void checkNull(@Decimal final long a, @Decimal final long b) {
        if (isNull(a) || isNull(b)) {
            throw new NullPointerException();
        }
    }

    /**
     * Implements {@link Decimal64#toFixedPoint(int)}, adds null check; do not use directly.
     *
     * @param value          DFP argument
     * @param numberOfDigits number of significant digits after decimal point
     * @return ..
     */
    public static long toFixedPointChecked(@Decimal final long value, final int numberOfDigits) {
        checkNull(value);
        return toFixedPoint(value, numberOfDigits);
    }

    /**
     * Implements {@link Decimal64#toDouble()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    public static double toDoubleChecked(@Decimal final long value) {
        checkNull(value);
        return toDouble(value);
    }

    /**
     * Implements {@link Decimal64#toBigDecimal()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    public static BigDecimal toBigDecimalChecked(@Decimal final long value) {
        checkNull(value);
        return toBigDecimal(value);
    }

    @Decimal
    public static long fromLongChecked(final long value) {
        return fromLong(value);
    }

    /**
     * Implements {@link Decimal64#toLong()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    public static long toLongChecked(@Decimal final long value) {
        checkNull(value);
        return toLong(value);
    }

    @Decimal
    public static long fromIntChecked(final int value) {
        return fromInt(value);
    }

    /**
     * Implements {@link Decimal64#toInt()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    public static int toIntChecked(@Decimal final long value) {
        checkNull(value);
        return toInt(value);
    }

    /**
     * Implements {@link Decimal64#isNaN()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    public static boolean isNaNChecked(@Decimal final long value) {
        checkNull(value);
        return isNaN(value);
    }

    /**
     * Implements {@link Decimal64#isInfinity()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    public static boolean isInfinityChecked(@Decimal final long value) {
        checkNull(value);
        return isInfinity(value);
    }

    /**
     * Implements {@link Decimal64#isPositiveInfinity()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    public static boolean isPositiveInfinityChecked(@Decimal final long value) {
        checkNull(value);
        return isPositiveInfinity(value);
    }

    /**
     * Implements {@link Decimal64#isNegativeInfinity()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    public static boolean isNegativeInfinityChecked(@Decimal final long value) {
        checkNull(value);
        return isNegativeInfinity(value);
    }

    /**
     * Implements {@link Decimal64#isFinite()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    public static boolean isFiniteChecked(@Decimal final long value) {
        checkNull(value);
        return isFinite(value);
    }

    /**
     * Implements {@link Decimal64#isNormal()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    public static boolean isNormalChecked(@Decimal final long value) {
        checkNull(value);
        return isNormal(value);
    }

    /**
     * Implements {@link Decimal64#isIdentical(Decimal64)}, adds null checks; do not use directly.
     *
     * @param a 1st DFP argument
     * @param b 2nd DFP argument
     * @return ..
     */
    public static boolean isIdenticalChecked(@Decimal final long a, @Decimal final long b) {
        checkNull(a, b);
        return a == b;
    }

    /**
     * Implements {@link Decimal64#isIdentical(Decimal64)}, adds null checks; do not use directly.
     *
     * @param value DFP argument
     * @param obj   {@link Object} argument
     * @return ..
     */
    public static boolean isIdenticalChecked(@Decimal final long value, final Object obj) {
        checkNull(value);
        return obj instanceof Decimal64 && value == ((Decimal64) obj).value;
    }

    /**
     * Implements {@link Decimal64#isEqual(Decimal64)}, adds null checks; do not use directly.
     *
     * @param a 1st DFP argument
     * @param b 2nd DFP argument
     * @return ..
     */
    public static boolean isEqualChecked(@Decimal final long a, @Decimal final long b) {
        checkNull(a, b);
        return isEqual(a, b);
    }

    /**
     * Implements {@link Decimal64#isNotEqual(Decimal64)}, adds null checks; do not use directly.
     *
     * @param a 1st DFP argument
     * @param b 2nd DFP argument
     * @return ..
     */
    public static boolean isNotEqualChecked(@Decimal final long a, @Decimal final long b) {
        checkNull(a, b);
        return isNotEqual(a, b);
    }

    /**
     * Implements {@link Decimal64#isLess(Decimal64)}, adds null checks; do not use directly.
     *
     * @param a 1st DFP argument
     * @param b 2nd DFP argument
     * @return ..
     */
    public static boolean isLessChecked(@Decimal final long a, @Decimal final long b) {
        checkNull(a, b);
        return isLess(a, b);
    }

    /**
     * Implements {@link Decimal64#isLessOrEqual(Decimal64)}, adds null checks; do not use directly.
     *
     * @param a 1st DFP argument
     * @param b 2nd DFP argument
     * @return ..
     */
    public static boolean isLessOrEqualChecked(@Decimal final long a, @Decimal final long b) {
        checkNull(a, b);
        return isLessOrEqual(a, b);
    }

    /**
     * Implements {@link Decimal64#isGreater(Decimal64)}, adds null checks; do not use directly.
     *
     * @param a 1st DFP argument
     * @param b 2nd DFP argument
     * @return ..
     */
    public static boolean isGreaterChecked(@Decimal final long a, @Decimal final long b) {
        checkNull(a, b);
        return isGreater(a, b);
    }

    /**
     * Implements {@link Decimal64#isGreaterOrEqual(Decimal64)}, adds null checks; do not use directly.
     *
     * @param a 1st DFP argument
     * @param b 2nd DFP argument
     * @return ..
     */
    public static boolean isGreaterOrEqualChecked(@Decimal final long a, @Decimal final long b) {
        checkNull(a, b);
        return isGreaterOrEqual(a, b);
    }

    /**
     * Implements {@link Decimal64#isZero()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    public static boolean isZeroChecked(@Decimal final long value) {
        checkNull(value);
        return isZero(value);
    }

    /**
     * Implements {@link Decimal64#isNonZero()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    public static boolean isNonZeroChecked(@Decimal final long value) {
        checkNull(value);
        return isNonZero(value);
    }

    /**
     * Implements {@link Decimal64#isPositive()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    public static boolean isPositiveChecked(@Decimal final long value) {
        checkNull(value);
        return isPositive(value);
    }

    /**
     * Implements {@link Decimal64#isNegative()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    public static boolean isNegativeChecked(@Decimal final long value) {
        checkNull(value);
        return isNegative(value);
    }

    /**
     * Implements {@link Decimal64#isNonPositive()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    public static boolean isNonPositiveChecked(@Decimal final long value) {
        checkNull(value);
        return isNonPositive(value);
    }

    /**
     * Implements {@link Decimal64#isNonNegative()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    public static boolean isNonNegativeChecked(@Decimal final long value) {
        checkNull(value);
        return isNonNegative(value);
    }

    /**
     * Implements {@link Decimal64#negate()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    @Decimal
    public static long negateChecked(@Decimal final long value) {
        checkNull(value);
        return negate(value);
    }

    /**
     * Implements {@link Decimal64#abs()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    @Decimal
    public static long absChecked(@Decimal final long value) {
        checkNull(value);
        return abs(value);
    }

    /**
     * Implements {@link Decimal64#add(Decimal64)}, adds null checks; do not use directly.
     *
     * @param a DFP argument
     * @param b DFP argument
     * @return ..
     */
    @Decimal
    public static long addChecked(@Decimal final long a, @Decimal final long b) {
        checkNull(a, b);
        return add(a, b);
    }

    /**
     * Implements {@link Decimal64#add(Decimal64, Decimal64)}, adds null checks; do not use directly.
     *
     * @param a DFP argument
     * @param b DFP argument
     * @param c DFP argument
     * @return ..
     */
    @Decimal
    public static long addChecked(@Decimal final long a, @Decimal final long b, @Decimal final long c) {
        checkNull(a, b);
        checkNull(c);
        return add(a, b, c);
    }

    /**
     * Implements {@link Decimal64#add(Decimal64, Decimal64, Decimal64)}, adds null checks; do not use directly.
     *
     * @param a DFP argument
     * @param b DFP argument
     * @param c DFP argument
     * @param d DFP argument
     * @return ..
     */
    @Decimal
    public static long addChecked(@Decimal final long a, @Decimal final long b,
                                  @Decimal final long c, @Decimal final long d) {
        checkNull(a, b);
        checkNull(c, d);
        return add(a, b, c, d);
    }

    /**
     * Implements {@link Decimal64#subtract(Decimal64)}, adds null checks; do not use directly.
     *
     * @param a DFP argument
     * @param b DFP argument
     * @return ..
     */
    @Decimal
    public static long subtractChecked(@Decimal final long a, @Decimal final long b) {
        checkNull(a, b);
        return subtract(a, b);
    }

    /**
     * Implements {@link Decimal64#multiply(Decimal64)}, adds null checks; do not use directly.
     *
     * @param a DFP argument
     * @param b DFP argument
     * @return ..
     */
    @Decimal
    public static long multiplyChecked(@Decimal final long a, @Decimal final long b) {
        checkNull(a, b);
        return multiply(a, b);
    }

    /**
     * Implements {@link Decimal64#multiply(Decimal64, Decimal64)}, adds null checks; do not use directly.
     *
     * @param a DFP argument
     * @param b DFP argument
     * @param c DFP argument
     * @return ..
     */
    @Decimal
    public static long multiplyChecked(@Decimal final long a, @Decimal final long b, @Decimal final long c) {
        checkNull(a, b);
        checkNull(c);
        return multiply(a, b, c);
    }

    /**
     * Implements {@link Decimal64#multiply(Decimal64, Decimal64, Decimal64)}, adds null checks; do not use directly.
     *
     * @param a DFP argument
     * @param b DFP argument
     * @param c DFP argument
     * @param d DFP argument
     * @return ..
     */
    @Decimal
    public static long multiplyChecked(@Decimal final long a, @Decimal final long b,
                                       @Decimal final long c, @Decimal final long d) {
        checkNull(a, b);
        checkNull(c, d);
        return multiply(a, b, c, d);
    }

    /**
     * Implements {@link Decimal64#divide(Decimal64)}, adds null checks; do not use directly.
     *
     * @param a DFP argument
     * @param b DFP argument
     * @return ..
     */
    @Decimal
    public static long divideChecked(@Decimal final long a, @Decimal final long b) {
        checkNull(a, b);
        return divide(a, b);
    }

    /**
     * Implements {@link Decimal64#multiplyByInteger(int)}, adds null check; do not use directly.
     *
     * @param a DFP argument
     * @param b DFP argument
     * @return ..
     */
    @Decimal
    public static long multiplyByIntegerChecked(@Decimal final long a, final int b) {
        checkNull(a);
        return multiplyByInteger(a, b);
    }

    /**
     * Implements {@link Decimal64#multiplyByInteger(long)}, adds null check; do not use directly.
     *
     * @param a DFP argument
     * @param b DFP argument
     * @return ..
     */
    @Decimal
    public static long multiplyByIntegerChecked(@Decimal final long a, final long b) {
        checkNull(a);
        return multiplyByInteger(a, b);
    }

    /**
     * Implements {@link Decimal64#divideByInteger(int)}, adds null check; do not use directly.
     *
     * @param a DFP argument
     * @param b DFP argument
     * @return ..
     */
    @Decimal
    public static long divideByIntegerChecked(@Decimal final long a, final int b) {
        checkNull(a);
        return divideByInteger(a, b);
    }

    /**
     * Implements {@link Decimal64#divideByInteger(long)}, adds null check; do not use directly.
     *
     * @param a DFP argument
     * @param b DFP argument
     * @return ..
     */
    @Decimal
    public static long divideByIntegerChecked(@Decimal final long a, final long b) {
        checkNull(a);
        return divideByInteger(a, b);
    }

    /**
     * Implements {@link Decimal64#multiplyAndAdd(Decimal64, Decimal64)}, adds null checks; do not use directly.
     *
     * @param a DFP argument
     * @param b DFP argument
     * @param c DFP argument
     * @return ..
     */
    @Decimal
    public static long multiplyAndAddChecked(@Decimal final long a, @Decimal final long b, @Decimal final long c) {
        checkNull(a);
        checkNull(b, c);
        return multiplyAndAdd(a, b, c);
    }

    /**
     * Implements {@link Decimal64#scaleByPowerOfTen(int)}, adds null checks; do not use directly.
     *
     * @param a DFP argument
     * @param n Scale argument
     * @return ..
     */
    @Decimal
    public static long scaleByPowerOfTenChecked(@Decimal final long a, final int n) {
        checkNull(a);
        return scaleByPowerOfTen(a, n);
    }

    /**
     * Implements {@link Decimal64#average(Decimal64)}, adds null checks; do not use directly.
     *
     * @param a DFP argument
     * @param b DFP argument
     * @return ..
     */
    @Decimal
    public static long averageChecked(@Decimal final long a, @Decimal final long b) {
        checkNull(a, b);
        return average(a, b);
    }

    /**
     * Implements {@link Decimal64#max(Decimal64)}, adds null checks; do not use directly.
     *
     * @param a DFP argument
     * @param b DFP argument
     * @return ..
     */
    @Decimal
    public static long maxChecked(@Decimal final long a, @Decimal final long b) {
        checkNull(a, b);
        return max(a, b);
    }

    /**
     * Implements {@link Decimal64#min(Decimal64)}, adds null checks; do not use directly.
     *
     * @param a DFP argument
     * @param b DFP argument
     * @return ..
     */
    @Decimal
    public static long minChecked(@Decimal final long a, @Decimal final long b) {
        checkNull(a, b);
        return min(a, b);
    }

    /**
     * Implements {@link Decimal64#round()}, adds null check; do not use directly.
     *
     * @param value     {@code DFP} argument to round
     * @param n         the number of decimals to use when rounding the number
     * @param roundType {@code RoundingMode} type of rounding
     * @return {@code DFP} the rounded value
     */
    @Decimal
    public static long roundChecked(@Decimal final long value, final int n, final RoundingMode roundType) {
        checkNull(value);
        return round(value, n, roundType);
    }

    /**
     * Implements {@link Decimal64#ceil()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    @Decimal
    public static long ceilChecked(@Decimal final long value) {
        checkNull(value);
        return ceil(value);
    }

    /**
     * Implements {@link Decimal64#floor()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    @Decimal
    public static long floorChecked(@Decimal final long value) {
        checkNull(value);
        return floor(value);
    }

    /**
     * Implements {@link Decimal64#round()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    @Decimal
    public static long roundChecked(@Decimal final long value) {
        checkNull(value);
        return round(value);
    }

    /**
     * Implements {@link Decimal64#round(Decimal64)}, adds null check; do not use directly.
     *
     * @param value    DFP argument
     * @param multiple DFP argument
     * @return ..
     */
    @Decimal
    public static long roundChecked(@Decimal final long value, final long multiple) {
        checkNull(value, multiple);
        return round(value, multiple);
    }

    /**
     * Implements {@link Decimal64#ceiling()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    @Decimal
    public static long ceilingChecked(@Decimal final long value) {
        checkNull(value);
        return ceiling(value);
    }

    /**
     * Implements {@link Decimal64#roundTowardsPositiveInfinity()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    @Decimal
    public static long roundTowardsPositiveInfinityChecked(@Decimal final long value) {
        checkNull(value);
        return roundTowardsPositiveInfinity(value);
    }

    /**
     * Implements {@link Decimal64#roundTowardsNegativeInfinity()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    @Decimal
    public static long roundTowardsNegativeInfinityChecked(@Decimal final long value) {
        checkNull(value);
        return roundTowardsNegativeInfinity(value);
    }

    /**
     * Implements {@link Decimal64#truncate()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    @Decimal
    public static long truncateChecked(@Decimal final long value) {
        checkNull(value);
        return truncate(value);
    }

    /**
     * Implements {@link Decimal64#roundTowardsZero()}, adds null checks; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    @Decimal
    public static long roundTowardsZeroChecked(@Decimal final long value) {
        checkNull(value);
        return roundTowardsZeroChecked(value);
    }

    /**
     * Implements {@link Decimal64#roundToNearestTiesAwayFromZero()}, adds null checks; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    @Decimal
    public static long roundToNearestTiesAwayFromZeroChecked(@Decimal final long value) {
        checkNull(value);
        return roundToNearestTiesAwayFromZero(value);
    }

    /**
     * Implements {@link Decimal64#roundToNearestTiesToEven()}, adds null checks; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    @Decimal
    public static long roundToNearestTiesToEvenChecked(@Decimal final long value) {
        checkNull(value);
        return roundToNearestTiesToEven(value);
    }

    /**
     * Implements {@link Decimal64#roundTowardsPositiveInfinity()}, adds null checks; do not use directly.
     *
     * @param value    DFP argument
     * @param multiple DFP argument
     * @return ..
     */
    @Decimal
    public static long roundTowardsPositiveInfinityChecked(@Decimal final long value, @Decimal final long multiple) {
        checkNull(value, multiple);
        return roundTowardsPositiveInfinity(value, multiple);
    }

    /**
     * Implements {@link Decimal64#roundTowardsNegativeInfinity()}, adds null checks; do not use directly.
     *
     * @param value    DFP argument
     * @param multiple DFP argument
     * @return ..
     */
    @Decimal
    public static long roundTowardsNegativeInfinityChecked(@Decimal final long value, @Decimal final long multiple) {
        checkNull(value, multiple);
        return roundTowardsNegativeInfinity(value, multiple);
    }

    /**
     * Implements {@link Decimal64#roundToNearestTiesAwayFromZero()}, adds null checks; do not use directly.
     *
     * @param value    DFP argument
     * @param multiple DFP argument
     * @return ..
     */
    @Decimal
    public static long roundToNearestTiesAwayFromZeroChecked(@Decimal final long value, @Decimal final long multiple) {
        checkNull(value, multiple);
        return roundToNearestTiesAwayFromZero(value, multiple);
    }

    /**
     * Implements {@link Decimal64#roundToNearestTiesToEven()}, adds null checks; do not use directly.
     *
     * @param value    DFP argument
     * @param multiple DFP argument
     * @return ..
     */
    @Decimal
    public static long roundToNearestTiesToEvenChecked(@Decimal final long value, @Decimal final long multiple) {
        checkNull(value, multiple);
        return roundToNearestTiesToEven(value, multiple);
    }

    /**
     * Implements {@link Decimal64#identityHashCode()}, adds null checks; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    public static int identityHashCodeChecked(@Decimal final long value) {
        checkNull(value);
        return identityHashCode(value);
    }

    /**
     * Implements {@link Decimal64#hashCode()}, adds null checks; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    public static int hashCodeChecked(@Decimal final long value) {
        checkNull(value);
        return hashCode(value);
    }

    /**
     * Implements {@link Decimal64#appendTo(Appendable)}, adds null check; do not use directly.
     *
     * @param value      DFP argument
     * @param appendable an object, implementing Appendable interface
     * @return ..
     * @throws IOException from {@link Appendable#append(char)}
     */
    public static Appendable appendToChecked(@Decimal final long value, final Appendable appendable) throws IOException {
        checkNull(value);
        return appendTo(value, appendable);
    }

    /**
     * Implements {@link Decimal64#appendTo(StringBuilder)}, adds null check; do not use directly.
     *
     * @param value         DFP argument
     * @param stringBuilder StringBuilder argument
     * @return ..
     */
    public static StringBuilder appendToChecked(@Decimal final long value, final StringBuilder stringBuilder) {
        checkNull(value);
        return appendTo(value, stringBuilder);
    }

    /**
     * Implements {@link Decimal64#toString()}, adds null checks; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    public static String toStringChecked(@Decimal final long value) {
        checkNull(value);
        return toString(value);
    }

    /**
     * Implements {@link Decimal64#equals(Decimal64)}, adds null checks; do not use directly.
     *
     * @param a DFP argument
     * @param b DFP argument
     * @return ..
     */
    public static boolean equalsChecked(@Decimal final long a, @Decimal final long b) {
        checkNull(a, b);
        return equals(a, b);
    }

    /**
     * Implements {@link Decimal64#equals(Object)}, adds null checks; do not use directly.
     *
     * @param a DFP argument
     * @param b {@code Object}
     * @return ..
     */
    public static boolean equalsChecked(@Decimal final long a, final Object b) {
        checkNull(a);
        return equals(a, ((Decimal64) b).value);
    }

    /**
     * Implements {@link Decimal64#getUnscaledValue()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    public static long getUnscaledValueChecked(@Decimal final long value) {
        checkNull(value);
        return getUnscaledValue(value);
    }

    /**
     * Implements {@link Decimal64#getUnscaledValue(long)}, adds null check; do not use directly.
     *
     * @param value          DFP argument
     * @param abnormalReturn The value returned for abnormal input values (NaN, +Inf, -Inf).
     * @return ..
     */
    public static long getUnscaledValueChecked(@Decimal final long value, final long abnormalReturn) {
        checkNull(value);
        return getUnscaledValue(value, abnormalReturn);
    }

    /**
     * Implements {@link Decimal64#getScale()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    public static int getScaleChecked(@Decimal final long value) {
        checkNull(value);
        return getScale(value);
    }

    /**
     * Implements {@link Decimal64#getScale(int)}, adds null check; do not use directly.
     *
     * @param value          DFP argument
     * @param abnormalReturn The value returned for abnormal input values (NaN, +Inf, -Inf).
     * @return ..
     */
    public static int getScaleChecked(@Decimal final long value, final int abnormalReturn) {
        checkNull(value);
        return getScale(value, abnormalReturn);
    }

    /**
     * Implements {@link Decimal64#nextUp()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    @Decimal
    public static long nextUpChecked(@Decimal final long value) {
        checkNull(value);
        return nextUp(value);
    }

    /**
     * Implements {@link Decimal64#nextDown()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    @Decimal
    public static long nextDownChecked(@Decimal final long value) {
        checkNull(value);
        return nextDown(value);
    }

    /**
     * Implements {@link Decimal64#canonize()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    @Decimal
    public static long canonizeChecked(@Decimal final long value) {
        checkNull(value);
        return canonize(value);
    }

    /**
     * Implements {@link Decimal64#intValue()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    public static int intValueChecked(@Decimal final long value) {
        checkNull(value);
        return toInt(value);
    }

    /**
     * Implements {@link Decimal64#longValue()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    public static long longValueChecked(@Decimal final long value) {
        checkNull(value);
        return toLong(value);
    }

    /**
     * Implements {@link Decimal64#floatValue()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    public static float floatValueChecked(@Decimal final long value) {
        checkNull(value);
        return (float) toDouble(value);
    }

    /**
     * Implements {@link Decimal64#doubleValue()}, adds null check; do not use directly.
     *
     * @param value DFP argument
     * @return ..
     */
    public static double doubleValueChecked(@Decimal final long value) {
        checkNull(value);
        return toDouble(value);
    }

    /**
     * Implements {@link Decimal64#compareTo(Decimal64)}, adds null checks; do not use directly.
     *
     * @param a DFP argument
     * @param b DFP argument
     * @return ..
     */
    public static int compareToChecked(@Decimal final long a, @Decimal final long b) {
        checkNull(a, b);
        return compareTo(a, b);
    }

    /**
     * Implements {@link Comparable#compareTo(Object)} for {@link Decimal64} (type erasure for {@code Comparable<Decimal64>})
     * Compares the value of {@code a} to an {@link Object}, while checking {@code a} for {@code null} constant.
     * Do not use directly.
     *
     * @param a DFP argument
     * @param b Object argument
     * @return comparison result
     * @throws NullPointerException if a or b are null
     * @throws ClassCastException   if the second argument is not {@link Decimal64}
     * @see Decimal64#compareTo(Decimal64)
     * @see Decimal64Utils#compareTo(long, long)
     */
    public static int compareToChecked(@Decimal final long a, final Object b) {
        checkNull(a);
        return compareTo(a, ((Decimal64) b).value);
    }

    /// endregion

    /// region Array boxing/unboxing (array conversions from long[] / to long[])

    /**
     * Converts an array of DFP64 values represented as {@code long} into array of {@link Decimal64} instances (performs boxing).
     *
     * @param src       source array
     * @param srcOffset src offset
     * @param dst       destination array
     * @param dstOffset dst offset
     * @param length    length
     * @return The destination array ({@code dst})
     */
    public static Decimal64[] fromUnderlyingLongArray(@Decimal final long[] src, final int srcOffset, final Decimal64[] dst, final int dstOffset, final int length) {

        final int srcLength = src.length;
        final int srcEndOffset = srcOffset + length;

        // NOTE: no bounds checks
        for (int i = 0; i < length; ++i) {
            dst[dstOffset + i] = Decimal64.fromUnderlying(src[srcOffset + i]);
        }

        return dst;
    }

    /**
     * Converts an array of {@link Decimal64} instances into array of underlying {@code long} DFP values (performs unboxing).
     *
     * @param src       source array
     * @param srcOffset src offset
     * @param dst       destination array
     * @param dstOffset dst offset
     * @param length    length
     * @return The destination array {@link Decimal64} {@code dst}
     */
    public static long[] toUnderlyingLongArray(final Decimal64[] src, final int srcOffset, @Decimal final long[] dst, final int dstOffset, final int length) {

        final int srcLength = src.length;
        final int srcEndOffset = srcOffset + length;

        // NOTE: no bounds checks
        for (int i = 0; i < length; ++i) {
            dst[dstOffset + i] = Decimal64.toUnderlying(src[srcOffset + i]);
        }

        return dst;
    }

    /**
     * Converts an array of DFP64 values represented as {@code long} into array of {@link Decimal64} instances (performs unboxing).
     *
     * @param src source array
     * @return The destination array ({@code dst})
     */
    public static @Decimal
    long[] toUnderlyingLongArray(final Decimal64[] src) {
        return null == src ? null : toUnderlyingLongArray(src, 0, new long[src.length], 0, src.length);
    }

    /**
     * Converts an array of {@link Decimal64} instances into array of underlying {@code long} DFP64 values (performs boxing).
     *
     * @param src source array
     * @return The destination array {@link Decimal64} {@code dst}
     */
    public static Decimal64[] fromUnderlyingLongArray(@Decimal final long[] src) {
        return null == src ? null : fromUnderlyingLongArray(src, 0, new Decimal64[src.length], 0, src.length);
    }

    /// endregion
}
