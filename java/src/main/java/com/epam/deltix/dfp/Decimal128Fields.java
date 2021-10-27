package com.epam.deltix.dfp;

public class Decimal128Fields implements Comparable<Decimal128Fields> {
    long low;
    long high;

    public void set(final long low, final long high) {
        this.low = low;
        this.high = high;
    }

    public void copyFrom(final Decimal128Fields src) {
        set(src.low, src.high);
    }

    public void copyTo(final Decimal128Fields dst) {
        dst.copyFrom(this);
    }

    /// region Comparable<T> Interface Implementation

    @Override
    public int compareTo(final Decimal128Fields o) {
        return Decimal128Utils.compareTo(this, o);
    }

    /// endregion

    /// region Math functions

    /**
     * Compare 128-bit decimal floating-point numbers for specified relation.
     *
     * @param y Second decimal number.
     * @return The comparison sign.
     */
    public boolean isGreaterUnordered(final Decimal128Fields y) {
        return Decimal128MathUtils.isGreaterUnordered(this, y);
    }

    /**
     * Compare 128-bit decimal floating-point numbers for specified relation.
     *
     * @param y Second decimal number.
     * @return The comparison sign.
     */
    public boolean isLessUnordered(final Decimal128Fields y) {
        return Decimal128MathUtils.isLessUnordered(this, y);
    }

    /**
     * Compare 128-bit decimal floating-point numbers for specified relation.
     *
     * @param y Second decimal number.
     * @return The comparison sign.
     */
    public boolean isNotGreater(final Decimal128Fields y) {
        return Decimal128MathUtils.isNotGreater(this, y);
    }

    /**
     * Compare 128-bit decimal floating-point numbers for specified relation.
     *
     * @param y Second decimal number.
     * @return The comparison sign.
     */
    public boolean isNotLess(final Decimal128Fields y) {
        return Decimal128MathUtils.isNotLess(this, y);
    }

    /**
     * These function return a {@code true} value if both arguments are not NaN, otherwise  {@code false}.
     *
     * @param y Second decimal number.
     * @return {@code true} if both arguments are not NaN.
     */
    public boolean isOrdered(final Decimal128Fields y) {
        return Decimal128MathUtils.isOrdered(this, y);
    }

    /**
     * These function return a {@code true} value if either argument is NaN, otherwise {@code false}.
     *
     * @param y Second decimal number.
     * @return {@code true} if either argument is NaN.
     */
    public boolean isUnordered(final Decimal128Fields y) {
        return Decimal128MathUtils.isUnordered(this, y);
    }

    /**
     * Return {@code true} if and only if x has negative sign.
     *
     * @return The sign.
     */
    public boolean isSigned() {
        return Decimal128MathUtils.isSigned(this);
    }

    /**
     * Return {@code true} if and only if x is subnormal.
     *
     * @return The check flag.
     */
    public boolean isSubnormal() {
        return Decimal128MathUtils.isSubnormal(this);
    }

    /**
     * Return {@code true} if and only if x is infinite.
     *
     * @return The check flag.
     */
    public boolean isInf() {
        return Decimal128MathUtils.isInf(this);
    }

    /**
     * Return true if and only if x is a signaling NaN.
     *
     * @return The check flag.
     */
    public boolean isSignaling() {
        return Decimal128MathUtils.isSignaling(this);
    }

    /**
     * Return true if and only if x is a finite number, infinity, or
     * NaN that is canonical.
     *
     * @return The check flag.
     */
    public boolean isCanonical() {
        return Decimal128MathUtils.isCanonical(this);
    }

    /**
     * Convert 128-bit decimal floating-point value (binary encoding)
     * to 32-bit binary floating-point format.
     *
     * @return The converted value.
     */
    public float toBinary32() {
        return Decimal128MathUtils.toBinary32(this);
    }

    /**
     * Convert 128-bit decimal floating-point value (binary encoding)
     * to 64-bit binary floating-point format.
     *
     * @return The converted value.
     */
    public double toBinary64() {
        return Decimal128MathUtils.toBinary64(this);
    }

    /// endregion
}
