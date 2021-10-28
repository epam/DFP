package com.epam.deltix.dfp;

import java.io.IOException;
import java.math.BigDecimal;

public class Decimal128 extends Decimal128Fields {
    @Override
    public void set(long low, long high) {
        throw new UnsupportedOperationException("The Decimal128 class must be considered as immutable. For mutable operations please use Decimal128Fields class.");
    }

    public Decimal128() {
        super();
    }

    public Decimal128(final long low, final long high) {
        super(low, high);
    }

//    /// region Constants
//
//    /**
//     * Special null constant to be used with {@code Decimal128} instances.
//     * Use this constant instead of Java 'null' to initialize or nullify Decimal128 scalar variables, or pass null as a
//     * Decimal128 scalar argument.
//     * If a variable is assigned with normal java null reference or left uninitialized, ValueType Agent may generate
//     * less efficient code, in which case it will print a warning.
//     * This limitation may be removed in the future versions of ValueType Agent.
//     * Of course, you may use another constant to initialize Decimal128 variables, just don't leave them uninitialized.
//     * You don't need to use this constant in equality comparisons. 'if (a == null)' is ok.
//     * Also, you are not expected to use Decimal128Utils.NULL constant directly anywhere, if you work with Decimal128 class.
//     *
//     * @see Decimal128Utils#NULL
//     */
//    public static final Decimal128 NULL = null;
//
//    /**
//     * A constant holding canonical representation of Not-a-Number DFP value(not signaling NaN)
//     */
//    public static final Decimal128 NaN = new Decimal128(Decimal128Utils.NaN);
//
//    /**
//     * A constant holding canonical representation of Positive Infinity value
//     */
//    public static final Decimal128 POSITIVE_INFINITY = new Decimal128(Decimal128Utils.POSITIVE_INFINITY);
//
//    /**
//     * A constant holding canonical representation of Negative Infinity value
//     */
//    public static final Decimal128 NEGATIVE_INFINITY = new Decimal128(Decimal128Utils.NEGATIVE_INFINITY);
//
//    /**
//     * A constant holding the smallest representable number: {@code -9999999999999999E+369}
//     */
//    public static final Decimal128 MIN_VALUE = new Decimal128(Decimal128Utils.MIN_VALUE);
//
//    /**
//     * A constant holding the largest representable number: {@code 9999999999999999E+369}
//     */
//    public static final Decimal128 MAX_VALUE = new Decimal128(Decimal128Utils.MAX_VALUE);
//
//    /**
//     * A constant holding the smallest representable positive number: {@code 1E-398}
//     */
//    public static final Decimal128 MIN_POSITIVE_VALUE = new Decimal128(Decimal128Utils.MIN_POSITIVE_VALUE);
//
//    /**
//     * A constant holding the largest representable negative number: {@code -1E-398}
//     */
//    public static final Decimal128 MAX_NEGATIVE_VALUE = new Decimal128(Decimal128Utils.MAX_NEGATIVE_VALUE);
//
//    /**
//     * Zero: {@code 0}
//     */
//    public static final Decimal128 ZERO = new Decimal128(Decimal128Utils.ZERO);
//
//    /**
//     * One: {@code 1}
//     */
//    public static final Decimal128 ONE = new Decimal128(Decimal128Utils.ONE);
//
//    /**
//     * Two: {@code 2}
//     */
//    public static final Decimal128 TWO = new Decimal128(Decimal128Utils.TWO);
//
//    /**
//     * Ten: {@code 10}
//     */
//    public static final Decimal128 TEN = new Decimal128(Decimal128Utils.TEN);
//
//    /**
//     * One Hundred: {@code 100}
//     */
//    public static final Decimal128 HUNDRED = new Decimal128(Decimal128Utils.HUNDRED);
//
//    /**
//     * One Thousand: {@code 1000}
//     */
//    public static final Decimal128 THOUSAND = new Decimal128(Decimal128Utils.THOUSAND);
//
//    /**
//     * One million: {@code 1000_000}
//     */
//    public static final Decimal128 MILLION = new Decimal128(Decimal128Utils.MILLION);
//
//    /**
//     * One tenth: {@code 0.1}
//     */
//    public static final Decimal128 ONE_TENTH = new Decimal128(Decimal128Utils.ONE_TENTH);
//
//    /**
//     * One hundredth: {@code 0.01}
//     */
//    public static final Decimal128 ONE_HUNDREDTH = new Decimal128(Decimal128Utils.ONE_HUNDREDTH);
//
//    /// endregion
//
//    final long value;
//
//    Decimal128(final long value) {
//        this.value = value;
//    }

    /// region Conversion

    /**
     * Create {@code Decimal128} instance from underlying binary value (boxing operation).
     *
     * @param low The low part if the 128-bit DFP value
     * @param high The high part if the 128-bit DFP value
     * @return new {@code Decimal128} instance
     */
    public static Decimal128 fromUnderlying(final long low, final long high) {
        return Decimal128Utils.isNull(low, high) ? null : new Decimal128(low, high);
    }

//    /**
//     * Get binary representation as {@code long} (unboxing)
//     *
//     * @param obj {@code Decimal128} instance, {@code null} can be passed too
//     * @return underlying binary representation as {@code long}
//     */
//    public static long toUnderlying(final Decimal128 obj) {
//        return null == obj ? Decimal128Utils.NULL : obj.value;
//    }
//
//    /**
//     * Create {@code Decimal128} instance from fixed point decimal value: (12345, 2) -&gt; 123.45
//     *
//     * @param mantissa       source fixed point value represented as {@code long}
//     * @param numberOfDigits number of decimal digits representing fractional part
//     * @return new {@code Decimal128} instance
//     * @see Decimal128Utils#fromFixedPoint(long, int)
//     */
//    public static Decimal128 fromFixedPoint(final long mantissa, final int numberOfDigits) {
//        return new Decimal128(Decimal128Utils.fromFixedPoint(mantissa, numberOfDigits));
//    }
//
//    /**
//     * Create {@code Decimal128} instance from fixed point decimal value: (12345, 2) -&gt; 123.45
//     * Overload of {@link #fromFixedPoint(long, int)} for mantissa representable by {@code int}.
//     * Faster than the full-range version.
//     *
//     * @param mantissa       source fixed point value represented as {@code int}
//     * @param numberOfDigits number of decimal digits representing fractional part
//     * @return new {@code Decimal128} instance
//     * @see Decimal128Utils#fromFixedPoint(int, int)
//     */
//    public static Decimal128 fromFixedPoint(final int mantissa, final int numberOfDigits) {
//        return new Decimal128(Decimal128Utils.fromFixedPoint(mantissa, numberOfDigits));
//    }
//
//    public static Decimal128 fromDecimalDouble(final double value) {
//        return new Decimal128(Decimal128Utils.fromDecimalDouble(value));
//    }
//
//    /**
//     * Convert to fixed-point representation: (123.4567, 2) -&gt; 12346
//     *
//     * @param numberOfDigits number of decimal digits representing fractional part
//     * @return fixed-point decimal value represented as @{code long}
//     */
//    public long toFixedPoint(final int numberOfDigits) {
//        return Decimal128Utils.toFixedPoint(value, numberOfDigits);
//    }
//
//    /**
//     * Create {@code Decimal128} instance from {@code long} integer
//     * Faster than the full-range version.
//     *
//     * @param value source {@code long} integer value
//     * @return new {@code Decimal128} instance
//     */
//    public static Decimal128 fromLong(final long value) {
//        return new Decimal128(Decimal128Utils.fromLong(value));
//    }
//
//    /**
//     * Convert {@code Decimal128} instance to {@code long} integer value by truncating fractional part towards zero
//     *
//     * @return {@code long} integer value
//     */
//    public long toLong() {
//        return Decimal128Utils.toLong(value);
//    }
//
//    /**
//     * Create {@code Decimal128} instance from {@code int}
//     * <p>
//     * Faster than the version that takes {@code long}.
//     *
//     * @param value source {@code int} value
//     * @return new {@code Decimal128} instance
//     */
//    public static Decimal128 fromInt(final int value) {
//        return new Decimal128(Decimal128Utils.fromInt(value));
//    }
//
//    /**
//     * Convert {@code Decimal128} instance to {@code int} value by truncating fractional part towards zero
//     *
//     * @return {@code int} value
//     */
//    public int toInt() {
//        return Decimal128Utils.toInt(value);
//    }

    /**
     * Create {@code Decimal128} instance from 64-bit binary floating point value({@code double})
     * <p>Note that not all binary FP values can be exactly represented as decimal FP values.
     *
     * @param value source 64-bit binary floating point value
     * @return new {@code Decimal128} instance
     */
    public static Decimal128 fromDouble(final double value) {
        Decimal128 r = new Decimal128();
        Decimal128Utils.fromDouble(value, r);
        return r;
    }

    /**
     * Convert {@code Decimal128} instance to 64-bit binary floating point ({@code double}) value.
     * <p>Note that not all decimal FP values can be exactly represented as binary FP values.
     *
     * @return {@code double} value
     */
    public double toDouble() {
        return Decimal128Utils.toDouble(this);
    }

//    /**
//     * Create {@code Decimal128} instance from {@code BigDecimal} binary floating point value.
//     * <p>Note that not all binary FP values can be exactly represented as decimal FP values.
//     *
//     * @param value source {@code BigDecimal} binary floating point value
//     * @return new {@code Decimal128} instance
//     */
//    public static Decimal128 fromBigDecimal(final BigDecimal value) {
//        return new Decimal128(Decimal128Utils.fromBigDecimal(value));
//    }
//
//    /**
//     * Create {@code Decimal128} instance from {@code BigDecimal} binary floating point value.
//     *
//     * @param value source {@code BigDecimal} binary floating point value
//     * @return new {@code Decimal128} instance
//     * @throws IllegalArgumentException if the value can't be converted to {@code Decimal128} without precision loss
//     */
//    public static Decimal128 fromBigDecimalExact(final BigDecimal value) throws IllegalArgumentException {
//        return new Decimal128(Decimal128Utils.fromBigDecimalExact(value));
//    }
//
//    /**
//     * Convert {@code Decimal128} instance to {@code BigDecimal} binary floating point value.
//     * <p>Note that not all decimal FP values can be exactly represented as binary FP values.
//     *
//     * @return {@code BigDecimal} value
//     */
//    public BigDecimal toBigDecimal() {
//        return Decimal128Utils.toBigDecimal(value);
//    }
//
//    /// endregion

    /// region Classification

    /**
     * Check, if this {@code Decimal128} instance holds a Not-a-Number value.
     *
     * @return {@code true}, if the value is NaN
     */
    public boolean isNaN() {
        return Decimal128Utils.isNaN(this);
    }

    /**
     * Check, if this {@code Decimal128} instance holds is a positive or negative infinity.
     *
     * @return {@code true}, if the value is an infinity
     */
    public boolean isInfinity() {
        return Decimal128Utils.isInfinity(this);
    }

    /**
     * Check, if this {@code Decimal128} instance holds a Positive Infinity value.
     *
     * @return {@code true}, if Positive Infinity
     */
    public boolean isPositiveInfinity() {
        return Decimal128Utils.isPositiveInfinity(this);
    }

    /**
     * Check, if this {@code Decimal128} instance holds a Negative Infinity value.
     *
     * @return {@code true}, if Negative Infinity
     */
    public boolean isNegativeInfinity() {
        return Decimal128Utils.isNegativeInfinity(this);
    }

    /**
     * Check, if this {@code Decimal128} instance holds a finite value(Not infinity or NaN).
     *
     * @return {@code true}, if finite. {@code false} if Infinity or NaN.
     */
    public boolean isFinite() {
        return Decimal128Utils.isFinite(this);
    }

    /**
     * Check, if the value held by this {@code Decimal128} instance is normalized.
     *
     * @return {@code true}, if normalized.
     */
    public boolean isNormal() {
        return Decimal128Utils.isNormal(this);
    }

    /// endregion

//    /// region Comparison
//
//    /**
//     * Check, if this instance and the specified {@code Decimal128} instance represent the same mathematical value.
//     * <p>
//     * We consider that all possible encodings of {@link #POSITIVE_INFINITY} are equal,
//     * all possible encodings of {@link #NEGATIVE_INFINITY} are equal,
//     * all possible encodings of {@code NaN} and {@code SNaN} are equal,
//     * all invalid encodings that aren't NaN or Infinity are equal to {@link #ZERO}.
//     * Negative and Positive(default) {@link #ZERO} are equal.
//     *
//     * @param other {@code Decimal128} instance being compared to this instance.
//     * @return {@code true} if this instance and the other instance represent the same mathematical value;
//     * {@code false} otherwise.
//     * @see #equals(Decimal128, Decimal128)
//     * @see #equals(Object)
//     */
//    public boolean equals(final Decimal128 other) {
//        return this == other || other != null && Decimal128Utils.equals(this.value, other.value);
//    }
//
//    /**
//     * Check, if this instance and the specified {@code Decimal128} instance have exactly the same underlying representation.
//     * <p>
//     * This method returns {@code true} if and only if the other {@code Decimal128} is exactly the same
//     * (underlying values are equal). This means that {@code Decimal128.NaN.isIdentical(Decimal128.NaN)} evaluates to
//     * {@code true}, while at the same time comparing two different representations of equal real values will cause this
//     * method to return {@code false}.
//     * E.g. various representations of 0 are not considered same.
//     * <p>
//     * {@code isIdentical(x, y) => equals(x, y)}
//     *
//     * @param other {@code Decimal128} instance being compared to this instance.
//     * @return {@code true} if the binary representation held by this instance is the same as the underlying value
//     * held by the other instance;
//     * {@code false} otherwise.
//     */
//    public boolean isIdentical(final Decimal128 other) {
//        return this == other || other != null && value == other.value;
//    }
//
//    /**
//     * Check, if the specified object is a {@code Decimal128} instance that holds the same underlying value.
//     * <p>
//     * This method returns {@code true} if and only if the argument is of type {@code Decimal128} and their
//     * underlying binary values are equal. This means that {@code Decimal128.NaN.isIdentical(Decimal128.NaN)} evaluates to
//     * {@code true}, while at the same time comparing two different representations of equal real values will cause this
//     * method to return {@code false}.
//     * E.g. various representations of 0 are not considered same.
//     * <p>
//     * {@code isIdentical(x, y) => equals(x, y)}
//     *
//     * @param other Object being compared to this instance.
//     * @return {@code true} if the binary representation of this value is the same as the underlying value of the other;
//     * {@code false} otherwise.
//     */
//    public boolean isIdentical(final Object other) {
//        return this == other || other instanceof Decimal128 && value == ((Decimal128) other).value;
//    }
//
//    /**
//     * Check, if two {@code Decimal128} instances represent the same mathematical value.
//     * <p>
//     * We consider that all possible encodings of {@link #POSITIVE_INFINITY} are equal,
//     * all possible encodings of {@link #NEGATIVE_INFINITY} are equal,
//     * all possible encodings of {@code NaN} and {@code SNaN} are equal,
//     * all invalid encodings that aren't NaN or Infinity are equal to {@link #ZERO}.
//     * Negative and Positive(default) {@link #ZERO} are equal.
//     *
//     * @param a the first {@code Decimal128} instance.
//     * @param b the second {@code Decimal128} instance.
//     * @return {@code true} if both {@code Decimal128} instances represent the same arithmetical value;
//     * {@code false} otherwise.
//     * @see #equals(Decimal128)
//     * @see #equals(Object)
//     */
//    public static boolean equals(final Decimal128 a, final Decimal128 b) {
//        return a == b || a != null && b != null && Decimal128Utils.equals(a.value, b.value);
//    }
//
//    /**
//     * Check, if two {@code Decimal128} instances hold the same underlying value.
//     * <p>
//     * This method returns {@code true} if and only if the underlying values of both {@code Decimal128} instances are the same.
//     * This means that {@code Decimal128.NaN.isIdentical(Decimal128.NaN)} evaluates to {@code true},
//     * while at the same time comparing two different representations of equal real values will cause this
//     * method to return {@code false}.
//     * E.g. various representations of 0 are not considered same.
//     * <p>
//     * {@code isIdentical(x, y) => equals(x, y)}
//     *
//     * @param a the first {@code Decimal128} instance.
//     * @param b the second {@code Decimal128} instance.
//     * @return {@code true} if the binary representations of a and b are equal;
//     * {@code false} otherwise.
//     */
//    public static boolean isIdentical(final Decimal128 a, final Decimal128 b) {
//        return a == b || a != null && b != null && a.value == b.value;
//    }
//
//    /**
//     * Check, if the second argument is an instance of {@code Decimal128} that represents the same mathematical value as the first argument.
//     * <p>
//     * We consider that all possible encodings of {@link #POSITIVE_INFINITY} are equal,
//     * all possible encodings of {@link #NEGATIVE_INFINITY} are equal,
//     * all possible encodings of {@code NaN} and {@code SNaN} are equal,
//     * all invalid encodings that aren't NaN or Infinity are equal to {@link #ZERO}.
//     * Negative and Positive(default) {@link #ZERO} are equal.
//     *
//     * @param a {@code Decimal128} instance.
//     * @param b Object, to which the first argument is compared.
//     * @return {@code true} if the second argument is an instance of {@code Decimal128} instances and both arguments represent the same arithmetical value;
//     * {@code false} otherwise.
//     */
//    public static boolean equals(final Decimal128 a, final Object b) {
//        return a == b || a != null && b instanceof Decimal128 && Decimal128Utils.equals(a.value, ((Decimal128) b).value);
//    }

    /**
     * Check, if the second argument is an instance of {@code Decimal128} that holds the same underlying value as the first argument.
     * <p>
     * This method returns {@code true} if and only if the 2nd argument is of type {@code Decimal128} and the underlying values
     * of both objects match.  This means that {@code Decimal128.NaN.isIdentical(Decimal128.NaN)} evaluates to {@code true},
     * while at the same time comparing two different representations of equal real values will cause this
     * method to return {@code false}.
     * E.g. various representations of 0 are not considered same.
     * <p>
     * {@code isIdentical(x, y) => equals(x, y)}
     *
     * @param a {@code Decimal128} instance.
     * @param b Object, to which the first argument is compared.
     * @return {@code true} if two instances of {@code Decimal128} hold the same underlying value;
     * {@code false} otherwise.
     */
    public static boolean isIdentical(final Decimal128 a, final Object b) {
        return a == b || a != null && b instanceof Decimal128 && a.low == ((Decimal128) b).low && a.high == ((Decimal128) b).high;
    }

    public boolean isEqual(final Decimal128 other) {
        return Decimal128Utils.isEqual(this, other);
    }

    public boolean isNotEqual(final Decimal128 other) {
        return Decimal128Utils.isNotEqual(this, other);
    }

    public boolean isLess(final Decimal128 other) {
        return Decimal128Utils.isLess(this, other);
    }

    public boolean isLessOrEqual(final Decimal128 other) {
        return Decimal128Utils.isLessOrEqual(this, other);
    }

    public boolean isGreater(final Decimal128 other) {
        return Decimal128Utils.isGreater(this, other);
    }

    public boolean isGreaterOrEqual(final Decimal128 other) {
        return Decimal128Utils.isGreaterOrEqual(this, other);
    }

    public boolean isZero() {
        return Decimal128Utils.isZero(this);
    }

    public boolean isNonZero() {
        return Decimal128Utils.isNonZero(this);
    }

    public boolean isPositive() {
        return Decimal128Utils.isPositive(this);
    }

    public boolean isNegative() {
        return Decimal128Utils.isNegative(this);
    }

    public boolean isNonPositive() {
        return Decimal128Utils.isNonPositive(this);
    }

    public boolean isNonNegative() {
        return Decimal128Utils.isNonNegative(this);
    }

    /// endregion

    /// region Minimum & Maximum

    /**
     * Returns the smallest of two given values.
     *
     * @param a first argument
     * @param b second argument
     * @return The smallest of two values.
     */
    public static Decimal128 min(final Decimal128 a, final Decimal128 b) {
        Decimal128 r = new Decimal128();
        Decimal128Utils.min(a, b, r);
        return r;
    }

    public static Decimal128 min(final Decimal128 a, final Decimal128 b, final Decimal128 c) {
        Decimal128 r = new Decimal128();
        Decimal128Utils.min(a, b, c, r);
        return r;
    }

    public static Decimal128 min(final Decimal128 a, final Decimal128 b, final Decimal128 c, final Decimal128 d) {
        Decimal128 r = new Decimal128();
        Decimal128Utils.min(a, b, c, d, r);
        return r;
    }

    /**
     * Returns the greatest of two given values.
     *
     * @param a first value.
     * @param b second value.
     * @return The greatest of two values.
     */
    public static Decimal128 max(final Decimal128 a, final Decimal128 b) {
        Decimal128 r = new Decimal128();
        Decimal128Utils.max(a, b, r);
        return r;
    }

    public static Decimal128 max(final Decimal128 a, final Decimal128 b, final Decimal128 c) {
        Decimal128 r = new Decimal128();
        Decimal128Utils.max(a, b, c, r);
        return r;
    }

    public static Decimal128 max(final Decimal128 a, final Decimal128 b, final Decimal128 c, final Decimal128 d) {
        Decimal128 r = new Decimal128();
        Decimal128Utils.max(a, b, c, d, r);
        return r;
    }

    /**
     * Returns the smallest of two values: this value and {@code a}.
     *
     * @param other another value.
     * @return The smallest of two values.
     */
    public Decimal128 min(final Decimal128 other) {
        return Decimal128.min(this, other);
    }

    /**
     * Returns the greatest of two values: this value and {@code a}.
     *
     * @param other another value.
     * @return The greatest of two values.
     */
    public Decimal128 max(final Decimal128 other) {
        return Decimal128.max(this, other);
    }

    /// endregion

    /// region Arithmetic

    public Decimal128 negate() {
        Decimal128 r = new Decimal128();
        Decimal128Utils.negate(this, r);
        return r;
    }

    public Decimal128 abs() {
        Decimal128 r = new Decimal128();
        Decimal128Utils.abs(this, r);
        return r;
    }

    public Decimal128 add(final Decimal128 other) {
        Decimal128 r = new Decimal128();
        Decimal128Utils.add(this, other, r);
        return r;
    }

    public Decimal128 add(final Decimal128 a, final Decimal128 b) {
        Decimal128 r = new Decimal128();
        Decimal128Utils.add(this, a, b, r);
        return r;
    }

    public Decimal128 add(final Decimal128 a, final Decimal128 b, final Decimal128 c) {
        Decimal128 r = new Decimal128();
        Decimal128Utils.add(this, a, b, c, r);
        return r;
    }

    public Decimal128 subtract(final Decimal128 other) {
        Decimal128 r = new Decimal128();
        Decimal128Utils.subtract(this, other, r);
        return r;
    }

    public Decimal128 multiply(final Decimal128 other) {
        Decimal128 r = new Decimal128();
        Decimal128Utils.multiply(this, other, r);
        return r;
    }

    public Decimal128 multiply(final Decimal128 a, final Decimal128 b) {
        Decimal128 r = new Decimal128();
        Decimal128Utils.multiply(this, a, b, r);
        return r;
    }

    public Decimal128 multiply(final Decimal128 a, final Decimal128 b, final Decimal128 c) {
        Decimal128 r = new Decimal128();
        Decimal128Utils.multiply(this, a, b, c, r);
        return r;
    }

    public Decimal128 multiplyByInteger(final int value) {
        Decimal128 r = new Decimal128();
        Decimal128Utils.multiplyByInteger(this, value, r);
        return r;
    }

    public Decimal128 multiplyByInteger(final long value) {
        Decimal128 r = new Decimal128();
        Decimal128Utils.multiplyByInteger(this, value, r);
        return r;
    }

    public Decimal128 divide(final Decimal128 other) {
        Decimal128 r = new Decimal128();
        Decimal128Utils.divide(this, other, r);
        return r;
    }

    public Decimal128 divideByInteger(final int value) {
        Decimal128 r = new Decimal128();
        Decimal128Utils.divideByInteger(this, value, r);
        return r;
    }

    public Decimal128 divideByInteger(final long value) {
        Decimal128 r = new Decimal128();
        Decimal128Utils.divideByInteger(this, value, r);
        return r;
    }

    public Decimal128 multiplyAndAdd(final Decimal128 m, final Decimal128 a) {
        Decimal128 r = new Decimal128();
        Decimal128Utils.multiplyAndAdd(this, m, a, r);
        return r;
    }

    public Decimal128 average(final Decimal128 other) {
        Decimal128 r = new Decimal128();
        Decimal128Utils.average(this, other, r);
        return r;
    }

//    // Same as average
//    public Decimal128 mean(Decimal128 other) {
//        return new Decimal128(Decimal128Utils.average(value, other.value));
//    }

    /// endregion

    /// region Rounding

//    public Decimal128 round(final int n, final RoundType roundType) {
//        Decimal128 r = new Decimal128();
//        Decimal128Utils.round(this, n, roundType, r);
//        return r;
//    }

    public Decimal128 round() {
        Decimal128 r = new Decimal128();
        Decimal128Utils.round(this, r);
        return r;
    }

    public Decimal128 round(final Decimal128 multiple) {
        Decimal128 r = new Decimal128();
        Decimal128Utils.round(this, multiple, r);
        return r;
    }

    @Deprecated
    public Decimal128 ceil() {
        Decimal128 r = new Decimal128();
        Decimal128Utils.ceil(this, r);
        return r;
    }

    public Decimal128 ceiling() {
        Decimal128 r = new Decimal128();
        Decimal128Utils.ceiling(this, r);
        return r;
    }

    public Decimal128 roundTowardsPositiveInfinity() {
        Decimal128 r = new Decimal128();
        Decimal128Utils.roundTowardsPositiveInfinity(this, r);
        return r;
    }

    public Decimal128 floor() {
        Decimal128 r = new Decimal128();
        Decimal128Utils.floor(this, r);
        return r;
    }

    public Decimal128 roundTowardsNegativeInfinity() {
        Decimal128 r = new Decimal128();
        Decimal128Utils.roundTowardsNegativeInfinity(this, r);
        return r;
    }

    public Decimal128 truncate() {
        Decimal128 r = new Decimal128();
        Decimal128Utils.truncate(this, r);
        return r;
    }

    public Decimal128 roundTowardsZero() {
        Decimal128 r = new Decimal128();
        Decimal128Utils.roundTowardsZero(this, r);
        return r;
    }

    public Decimal128 roundToNearestTiesAwayFromZero() {
        Decimal128 r = new Decimal128();
        Decimal128Utils.roundToNearestTiesAwayFromZero(this, r);
        return r;
    }

    public Decimal128 roundTowardsPositiveInfinity(final Decimal128 multiple) {
        Decimal128 r = new Decimal128();
        Decimal128Utils.roundTowardsPositiveInfinity(this, multiple, r);
        return r;
    }

    public Decimal128 roundTowardsNegativeInfinity(final Decimal128 multiple) {
        Decimal128 r = new Decimal128();
        Decimal128Utils.roundTowardsNegativeInfinity(this, multiple, r);
        return r;
    }

    public Decimal128 roundToNearestTiesAwayFromZero(final Decimal128 multiple) {
        Decimal128 r = new Decimal128();
        Decimal128Utils.roundToNearestTiesAwayFromZero(this, multiple, r);
        return r;
    }

    /// endregion

    /// region Special

    public Decimal128 nextUp() {
        Decimal128 r = new Decimal128();
        Decimal128Utils.nextUp(this, r);
        return r;
    }

    public Decimal128 nextDown() {
        Decimal128 r = new Decimal128();
        Decimal128Utils.nextDown(this, r);
        return r;
    }

//    /**
//     * Returns canonical representation of s {@code Decimal128} value.
//     * <p>
//     * We consider that all binary representations of one arithmetical value have the same canonical binary representation.
//     * Canonical representation of zeros = {@link #ZERO ZERO}
//     * Canonical representation of NaNs = {@link #NaN NaN}
//     * Canonical representation of POSITIVE_INFINITYs = {@link #POSITIVE_INFINITY POSITIVE_INFINITY}
//     * Canonical representation of NEGATIVE_INFINITYs = {@link #NEGATIVE_INFINITY NEGATIVE_INFINITY}
//     *
//     * @return Canonical representation of decimal.
//     */
//    public Decimal128 canonize() {
//        return new Decimal128(Decimal128Utils.canonize(value));
//    }

    /// endregion

//    /// region Parsing & Formatting
//
//    public static String toString(final Decimal128 decimal64) {
//        return null == decimal64 ? "null" : Decimal128Utils.toString(decimal64.value);
//    }
//
//    public Appendable appendTo(final Appendable appendable) throws IOException {
//        return Decimal128Utils.appendTo(value, appendable);
//    }
//
//    public StringBuilder appendTo(final StringBuilder builder) {
//        return Decimal128Utils.appendTo(value, builder);
//    }
//
//    public static Appendable appendTo(final Decimal128 decimal64, final Appendable appendable) throws IOException {
//        return null == decimal64 ? appendable.append("null") : Decimal128Utils.appendTo(decimal64.value, appendable);
//    }
//
//    public static StringBuilder appendTo(final Decimal128 decimal64, final StringBuilder builder) {
//        return null == decimal64 ? builder.append("null") : Decimal128Utils.appendTo(decimal64.value, builder);
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
//     * @param stopIndex  Index of character to stop parsing at.
//     * @return 64-bit dfp floating-point.
//     * @throws NumberFormatException if {@code text} does not contain valid dfp floating value.
//     */
//    public static Decimal128 parse(final CharSequence text, final int startIndex, final int stopIndex) {
//        return Decimal128.fromUnderlying(Decimal128Utils.parse(text, startIndex, stopIndex));
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
//     * @return 64-bit dfp floating-point.
//     * @throws NumberFormatException if {@code text} does not contain valid dfp floating value.
//     */
//    public static Decimal128 parse(final CharSequence text, final int startIndex) {
//        return Decimal128.fromUnderlying(Decimal128Utils.parse(text, startIndex, text.length()));
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
//     * @return 64-bit dfp floating-point.
//     * @throws NumberFormatException if {@code text} does not contain valid dfp floating value.
//     */
//    public static Decimal128 parse(final CharSequence text) {
//        return Decimal128.fromUnderlying(Decimal128Utils.parse(text, 0, text.length()));
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
//     * @return 64-bit dfp floating-point.
//     */
//    public static Decimal128 tryParse(final CharSequence text, final int startIndex, final int endIndex,
//                                     final Decimal128 defaultValue) {
//        try {
//            return Decimal128.fromUnderlying(Decimal128Utils.parse(text, startIndex, endIndex));
//        } catch (final NumberFormatException ignore) {
//            return defaultValue;
//        }
//    }
//
//    /**
//     * Tries to parse a dfp floating-point value from the given textual representation.
//     * Returns the default value in case of fail.
//     *
//     * @param text         Textual representation of dfp floating-point value.
//     * @param startIndex   Index of character to start parsing at.
//     * @param defaultValue Default value in case of fail.
//     * @return 64-bit dfp floating-point.
//     */
//    public static Decimal128 tryParse(final CharSequence text, final int startIndex, final Decimal128 defaultValue) {
//        return tryParse(text, startIndex, text.length(), defaultValue);
//    }
//
//    /**
//     * Tries to parse a dfp floating-point value from the given textual representation.
//     * Returns the default value in case of fail.
//     *
//     * @param text         Textual representation of dfp floating-point value.
//     * @param defaultValue Default value in case of fail.
//     * @return 64-bit dfp floating-point.
//     */
//    public static Decimal128 tryParse(final CharSequence text, final Decimal128 defaultValue) {
//        return tryParse(text, 0, text.length(), defaultValue);
//    }
//
//    /// endregion
//
//    /// region Object Interface Implementation
//
//    /**
//     * Returns {@code true} if the given Object is an instance of Decimal128 and has the same arithmetical value.
//     * <p>
//     * We consider that all POSITIVE_INFINITYs is equal to another POSITIVE_INFINITY,
//     * all NEGATIVE_INFINITYs is equal to another NEGATIVE_INFINITY,
//     * all NaNs is equal to another NaN.
//     *
//     * @param other value to compare
//     * @return True if two decimals represents the same arithmetical value.
//     * @see #equals(Decimal128, Decimal128)
//     * @see #equals(Decimal128)
//     */
//    @Override
//    public boolean equals(final Object other) {
//        return this == other || other instanceof Decimal128 && Decimal128Utils.equals(value, ((Decimal128) other).value);
//    }
//
//    /**
//     * Hash code of binary representation of given decimal.
//     *
//     * @return HashCode of given decimal.
//     */
//    public int identityHashCode() {
//        return Decimal128Utils.identityHashCode(value);
//    }
//
//    /**
//     * Return hash code of arithmetical value of given decimal.
//     * <p>
//     * We consider that all POSITIVE_INFINITYs have equal hashCode,
//     * all NEGATIVE_INFINITYs have equal hashCode,
//     * all NaNs have equal hashCode.
//     *
//     * @return HashCode of given decimal.
//     */
//    @Override
//    public int hashCode() {
//        return Decimal128Utils.hashCode(value);
//    }
//
//    @Override
//    public String toString() {
//        return Decimal128Utils.toString(value);
//    }
//
//    /// endregion
//
//    /// region Number Interface Implementation
//    @Override
//    public int intValue() {
//        return toInt();
//    }
//
//    @Override
//    public long longValue() {
//        return toLong();
//    }
//
//    @Override
//    public float floatValue() {
//        return (float) toDouble();
//    }
//
//    @Override
//    public double doubleValue() {
//        return toDouble();
//    }
//
//    /// endregion
}
