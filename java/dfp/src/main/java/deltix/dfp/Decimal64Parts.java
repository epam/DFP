package deltix.dfp;

/**
 * The class that represents the internals of 64-bit Decimal Floating Point value.
 * Supposed to be used with ver performance-sensitive code and therefore does not perform range checks
 * Also, not supposed to represent non-finite DFP64 values
 */
class Decimal64Parts {
    long coefficient;
    long signMask;
    int exponent;

    public Decimal64Parts() {
        this(0L, 0, 0);
    }

    public Decimal64Parts(final long coefficient, final int biasedExponent, final long signMask) {
        assert (0 == (~Long.MIN_VALUE & signMask));
        this.coefficient = coefficient;
        this.exponent = biasedExponent;
        this.signMask = signMask;
    }

    boolean isValid() {
        return 0 == (~Long.MIN_VALUE & signMask) &&
            exponent >= 0 && exponent < JavaImpl.BIASED_EXPONENT_MAX_VALUE + 1 &&
            coefficient >= 0 && coefficient < JavaImpl.MAX_COEFFICIENT + 1;
    }

    public long getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(final long coefficient) {
        this.coefficient = coefficient;
    }

    public int getBiasedExponent() {
        return exponent;
    }

    public void setBiasedExponent(final int exponent) {
        this.exponent = exponent;
    }

    public int getUnbiasedExponent() {
        return this.exponent - JavaImpl.EXPONENT_BIAS;
    }

    public void setUnbiasedExponent(final int exponent) {
        this.exponent = exponent + JavaImpl.EXPONENT_BIAS;
    }

    public long getSignMask() {
        return signMask;
    }

    public void setSignMask(final long signMask) {
        assert (0 == (~Long.MIN_VALUE & signMask));
        this.signMask = signMask;
    }

    public boolean isNegative() {
        return signMask < 0;
    }

    public void setNegative(final boolean negative) {
        signMask = negative ? Long.MIN_VALUE : 0;
    }
}
