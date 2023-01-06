package com.epam.deltix.dfp;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;

import static com.epam.deltix.dfp.JavaImplAdd.LONG_LOW_PART;

class JavaImpl {
    private JavaImpl() {
    }

    public static final long POSITIVE_INFINITY = 0x7800_0000_0000_0000L;
    public static final long NEGATIVE_INFINITY = 0xF800_0000_0000_0000L;
    public static final long NaN = 0x7C00_0000_0000_0000L;
    public static final long NULL = 0xFFFF_FFFF_FFFF_FF80L; // = -0x80L;

    public static final long ZERO = 0x31C0_0000_0000_0000L; //e=0,m=0,sign=0
    public static final long MAX_VALUE = 0x77FB_86F2_6FC0_FFFFL;
    public static final long MIN_VALUE = 0xF7FB_86F2_6FC0_FFFFL;
    public static final long MIN_POSITIVE_VALUE = 0x0000_0000_0000_0001L;
    public static final long MAX_NEGATIVE_VALUE = 0x8000_0000_0000_0001L;

    public static final long MAX_COEFFICIENT = 9999_9999_9999_9999L;
    public static final long MIN_COEFFICIENT = 0L;

    public static final int MIN_EXPONENT = -383;
    public static final int MAX_EXPONENT = 384;

    public static long fromInt32(final int value) {
        final long longValue = value; // Fixes -Integer.MIN_VALUE
        return value >= 0 ? (0x31C00000L << 32) | longValue : (0xB1C00000L << 32) | -longValue;
    }

    static long fromInt32V2(final int value) {
        final long sign = value >> 31;
        final long longValue = value;
        return longValue & MASK_SIGN | (longValue + sign ^ sign) | ZERO;
    }

    public static long fromUInt32(final int value) {
        return (0x31C00000L << 32) | value;
    }

    public static long fromFixedPointFastUnchecked(final int intMantissa, final int numDigits) {
        final long sign = intMantissa >> 31;
        final long mantissa = intMantissa; // Just to help the compiler
        return mantissa & MASK_SIGN | (mantissa + sign ^ sign) | ((long) (EXPONENT_BIAS - numDigits) << 53);
    }

    public static long fromFixedPointFastUnchecked(final long mantissa, final int numDigits) {
        final long sign = mantissa >> 63;
        return mantissa & MASK_SIGN | (mantissa + sign ^ sign) | ((long) (EXPONENT_BIAS - numDigits) << 53);
    }

    public static long fromFixedPointFastUnsignedUnchecked(final int mantissaUint, final int numDigits) {
        return mantissaUint + ((long) (EXPONENT_BIAS - numDigits) << 53);
    }

    public static long fromFixedPointFast(final int mantissa, final int numDigits) {
        final long rv = fromFixedPointFastUnchecked(mantissa, numDigits);
        if (numDigits + (Integer.MIN_VALUE + BIASED_EXPONENT_MAX_VALUE - EXPONENT_BIAS)
            > (Integer.MIN_VALUE + BIASED_EXPONENT_MAX_VALUE))
            throw new IllegalArgumentException();
        return rv;
    }

    public static long fromFixedPointFastUnsigned(final int mantissaUint, final int numDigits) {
        final long rv = fromFixedPointFastUnsignedUnchecked(mantissaUint, numDigits);
        if (numDigits + (Integer.MIN_VALUE + BIASED_EXPONENT_MAX_VALUE - EXPONENT_BIAS)
            > (Integer.MIN_VALUE + BIASED_EXPONENT_MAX_VALUE))
            throw new IllegalArgumentException();
        return rv;
    }


    public static long fromFixedPoint32(final int mantissa, final int numDigits) {
        return numDigits + (Integer.MIN_VALUE + BIASED_EXPONENT_MAX_VALUE - EXPONENT_BIAS)
            > (Integer.MIN_VALUE + BIASED_EXPONENT_MAX_VALUE) ?
            Decimal64Utils.fromFixedPoint((long) mantissa, numDigits) : fromFixedPointFastUnchecked(mantissa, numDigits);
    }


    public static boolean isNaN(final long value) {
        return (value & MASK_INFINITY_NAN) == MASK_INFINITY_NAN;
    }

    public static boolean isNull(final long value) {
        return value == NULL;
    }

    static boolean isSpecial(final long value) {
        return (value & MASK_SPECIAL) == MASK_SPECIAL;
    }

    public static boolean isInfinity(final long value) {
        return (value & MASK_INFINITY_NAN) == POSITIVE_INFINITY;
    }

    public static boolean isPositiveInfinity(final long value) {
        return (value & MASK_SIGN_INFINITY_NAN) == POSITIVE_INFINITY;
    }

    public static boolean isNegativeInfinity(final long value) {
        return (value & MASK_SIGN_INFINITY_NAN) == NEGATIVE_INFINITY;
    }

    public static boolean signBit(final long value) {
        return value < 0;
    }

    public static boolean isFinite(final long value) {
        return (value & MASK_INFINITY_AND_NAN) != MASK_INFINITY_AND_NAN;
    }

    public static boolean isNonFinite(final long value) {
        return (value & MASK_INFINITY_AND_NAN) == MASK_INFINITY_AND_NAN;
    }

    public static long negate(final long value) {
        return value ^ MASK_SIGN;
    }

    public static long abs(final long value) {
        return value & ~MASK_SIGN;
    }

    /**
     * Canonize a *finite* value. Infinite/NaN values are unexpected here.
     *
     * @param value value to be "canonized"
     * @return
     */
    public static long canonizeFinite(final long value) {
        final long signMask = value & MASK_SIGN;
        long coefficient;
        int exponent;

        if (isSpecial(value)) {
            assert (isFinite(value));

            // Check for non-canonical values.
            final long x = (value & LARGE_COEFFICIENT_MASK) | LARGE_COEFFICIENT_HIGH_BIT;
            coefficient = UnsignedLong.isGreater(x, MAX_COEFFICIENT) ? 0 : x;

            // Extract exponent.
            final long tmp = value >> EXPONENT_SHIFT_LARGE;
            exponent = (int) (tmp & EXPONENT_MASK);
        } else {
            // Extract coefficient.
            coefficient = (value & SMALL_COEFFICIENT_MASK);

            // Extract exponent. Maximum biased value for "small exponent" is 0x2FF(*2=0x5FE), signed: []
            // upper 1/4 of the mask range is "special", as checked in the code above
            final long tmp = value >> EXPONENT_SHIFT_SMALL;
            exponent = (int) (tmp & EXPONENT_MASK);
        }

        if (coefficient == 0)
            return ZERO;

        long div10 = coefficient / 10;
        if (div10 * 10 != coefficient)
            return value;

        do {
            coefficient = div10;
            div10 /= 10;
            ++exponent;
        } while (div10 * 10 == coefficient);
        return pack(signMask, exponent, coefficient, BID_ROUNDING_TO_NEAREST);
    }

    public static BigDecimal toBigDecimal(final long value) {
        // final Decimal64Parts parts = JavaImpl.toParts(value);
        // return BigDecimal.valueOf(parts.signMask == 0 ? parts.coefficient : -parts.coefficient, -(parts.exponent - EXPONENT_BIAS));

        final long partsSignMask = value & MASK_SIGN;
        int partsExponent = 0;
        long partsCoefficient = 0;

        if (isNull(value))
            return null;
        if (isNonFinite(value))
            throw new IllegalArgumentException("The BigDecimal do not supports Infinity and Nan values.");


        if (isSpecial(value)) {
            // Check for non-canonical values.
            final long coefficient = (value & LARGE_COEFFICIENT_MASK) | LARGE_COEFFICIENT_HIGH_BIT;
            partsCoefficient = UnsignedLong.isGreater(coefficient, MAX_COEFFICIENT) ? 0 : coefficient;

            // Extract exponent.
            final long tmp = value >> EXPONENT_SHIFT_LARGE;
            partsExponent = (int) (tmp & EXPONENT_MASK);
        } else { // Non-special
            // Extract exponent. Maximum biased value for "small exponent" is 0x2FF(*2=0x5FE), signed: []
            // upper 1/4 of the mask range is "special", as checked in the code above
            final long tmp = value >> EXPONENT_SHIFT_SMALL;
            partsExponent = (int) (tmp & EXPONENT_MASK);

            // Extract coefficient.
            partsCoefficient = (value & SMALL_COEFFICIENT_MASK);
        }

        return BigDecimal.valueOf(partsSignMask == 0 ? partsCoefficient : -partsCoefficient, -(partsExponent - EXPONENT_BIAS));
    }

    public static long fromBigDecimal(final BigDecimal value, final int roundingMode) {
        if (value == null)
            return JavaImpl.NULL;

        long signMask = 0;
        int scale = value.scale();

        BigInteger unscaledValue = value.unscaledValue();
        final int unscaledBits = unscaledValue.bitLength();
        if (unscaledBits > 62) {
            if (roundingMode == BID_ROUNDING_EXCEPTION)
                throw new NumberFormatException("The BigDecimal(=" + value + ") can't be converted to Decimal64 without precision loss.");
            final int baseTenShiftSize = (int) Math.ceil((unscaledBits - 62) * log10Tolog2Ratio);
            unscaledValue = unscaledValue.divide(BigInteger.TEN.pow(baseTenShiftSize));
            scale -= baseTenShiftSize;
        }
        long intCompact = unscaledValue.longValue();

        if (intCompact < 0) {
            signMask = MASK_SIGN;
            intCompact = -intCompact;
        }

        return pack(signMask, EXPONENT_BIAS - scale, intCompact, roundingMode);
    }

    private static final double log10Tolog2Ratio = Math.log10(1024) / 10;

    public static Decimal64Parts toParts(final long value) {
        final Decimal64Parts parts = new Decimal64Parts();
        toParts(value, parts);
        return parts;
    }

    public static long toParts(final long value, final Decimal64Parts parts) {
        parts.signMask = value & MASK_SIGN;

        if (isSpecial(value)) {
            if (isNonFinite(value)) {
                parts.exponent = 0;

                parts.coefficient = value & 0xFE03_FFFF_FFFF_FFFFL;
                if (UnsignedLong.isGreater(value & 0x0003_FFFF_FFFF_FFFFL, MAX_COEFFICIENT))
                    parts.coefficient = value & ~MASK_COEFFICIENT;
                if (isInfinity(value))
                    parts.coefficient = value & MASK_SIGN_INFINITY_NAN; // TODO: Why this was done??
                return 0;
            }

            // Check for non-canonical values.
            final long coefficient = (value & LARGE_COEFFICIENT_MASK) | LARGE_COEFFICIENT_HIGH_BIT;
            parts.coefficient = UnsignedLong.isGreater(coefficient, MAX_COEFFICIENT) ? 0 : coefficient;

            // Extract exponent.
            final long tmp = value >> EXPONENT_SHIFT_LARGE;
            parts.exponent = (int) (tmp & EXPONENT_MASK);

            return parts.coefficient;
        }

        // Extract exponent. Maximum biased value for "small exponent" is 0x2FF(*2=0x5FE), signed: []
        // upper 1/4 of the mask range is "special", as checked in the code above
        final long tmp = value >> EXPONENT_SHIFT_SMALL;
        parts.exponent = (int) (tmp & EXPONENT_MASK);

        // Extract coefficient.
        return parts.coefficient = (value & SMALL_COEFFICIENT_MASK);
    }

    public static long fromParts(final Decimal64Parts parts) {
        return pack(parts.signMask, parts.exponent, parts.coefficient, BID_ROUNDING_TO_NEAREST);
    }

    public static long fromDecimalDouble(final double x) {
        final long y = Decimal64Utils.fromDouble(x);
        long m, signAndExp;

        // Odd + special encoding(16 digits)
        final long notY = ~y;
        if ((MASK_SPECIAL & notY) == 0) {
            if ((MASK_INFINITY_AND_NAN & notY) == 0)
                return y;

            m = (y & LARGE_COEFFICIENT_MASK) + LARGE_COEFFICIENT_HIGH_BIT;
            signAndExp = ((y << 2) & EXPONENT_MASK_SMALL) + (y & MASK_SIGN);
        } else {
            m = y & SMALL_COEFFICIENT_MASK;
            // 16 digits + odd
            signAndExp = y & (-1L << EXPONENT_SHIFT_SMALL);
            if (m <= MAX_COEFFICIENT / 10 + 1)
                return y;
        }

        if ((y & 1) == 0)
            return y;
        // NeedAdjustment
        // Check the last digit
        final long m1 = m + 1;
        m = m1 / 10;
        if (m1 - m * 10 > 2)
            return y;

        signAndExp += 1L << EXPONENT_SHIFT_SMALL;
        if (Decimal64Utils.toDouble(signAndExp + m) != x)
            return y;

        for (long n = m; ; ) {
            final long m10 = n / 10;
            if (m10 * 10 != n)
                return signAndExp + n;
            n = m10;
            signAndExp += 1L << EXPONENT_SHIFT_SMALL;
        }
    }

//    private final static ThreadLocal<Decimal64Parts> tlsDecimal64Parts = new ThreadLocal<Decimal64Parts>() {
//        @Override
//        protected Decimal64Parts initialValue() {
//            return new Decimal64Parts();
//        }
//    };

    public static Appendable appendToRefImpl(final long value, final Appendable appendable) throws IOException {
        if (isNull(value)) {
            return appendable.append("null");
        }
        if (isNonFinite(value)) {
            // Value is either Inf or NaN
            // TODO: Do we need SNaN?
            return appendable.append(isNaN(value) ? "NaN" : value < 0 ? "-Infinity" : "Infinity");
        }

        //final Decimal64Parts parts = tlsDecimal64Parts.get();
        //final long coefficient = toParts(value, parts);
        long partsCoefficient;
//        long partsSignMask;
        int partsExponent;
        {
//            partsSignMask = value & MASK_SIGN;

            if (isSpecial(value)) {
//                if (isNonFinite(value)) {
//                    partsExponent = 0;
//
//                    partsCoefficient = value & 0xFE03_FFFF_FFFF_FFFFL;
//                    if (UnsignedLong.isGreater(value & 0x0003_FFFF_FFFF_FFFFL, MAX_COEFFICIENT))
//                        partsCoefficient = value & ~MASK_COEFFICIENT;
//                    if (isInfinity(value))
//                        partsCoefficient = value & MASK_SIGN_INFINITY_NAN; // TODO: Why this was done??
////                return 0;
//                } else
                {
                    // Check for non-canonical values.
                    final long coefficient = (value & LARGE_COEFFICIENT_MASK) | LARGE_COEFFICIENT_HIGH_BIT;
                    partsCoefficient = UnsignedLong.isGreater(coefficient, MAX_COEFFICIENT) ? 0 : coefficient;

                    // Extract exponent.
                    final long tmp = value >> EXPONENT_SHIFT_LARGE;
                    partsExponent = (int) (tmp & EXPONENT_MASK);

//                return parts.coefficient;
                }
            } else {
                // Extract exponent. Maximum biased value for "small exponent" is 0x2FF(*2=0x5FE), signed: []
                // upper 1/4 of the mask range is "special", as checked in the code above
                final long tmp = value >> EXPONENT_SHIFT_SMALL;
                partsExponent = (int) (tmp & EXPONENT_MASK);

                // Extract coefficient.
//            return
                partsCoefficient = (value & SMALL_COEFFICIENT_MASK);
            }
        }

        if (0 == partsCoefficient)
            return appendable.append('0');

        if (value < 0)
            appendable.append('-');

        final int exponent = partsExponent - EXPONENT_BIAS;
        final int digits = numberOfDigits(partsCoefficient);

        if (exponent >= 0) {
            appendLongTo(partsCoefficient, appendable, digits);
            for (int i = 0; i < exponent; i += 1)
                appendable.append('0');
        } else if (digits + exponent > 0) {
            final long integralPart = partsCoefficient / POWERS_OF_TEN[-exponent];
            final long fractionalPart = partsCoefficient % POWERS_OF_TEN[-exponent];
            appendLongTo(integralPart, appendable);
            if (fractionalPart != 0L) {
                appendable.append('.');
                for (int i = numberOfDigits(fractionalPart); i < -exponent; i += 1)
                    appendable.append('0');
                appendLongTo(dropTrailingZeros(fractionalPart), appendable);
            }
        } else {
            appendable.append("0.");
            for (int i = digits + exponent; i < 0; i += 1)
                appendable.append('0');
            appendLongTo(dropTrailingZeros(partsCoefficient), appendable);
        }

        return appendable;
    }

    private static final ThreadLocal<char[]> CHAR_BUFFER =
        new ThreadLocal<char[]>() {
            @Override
            protected char[] initialValue() {
                return new char[512];
            }
        };

    private static final int BCD_TABLE_DIGITS = 3;
    private static final int BCD_DIVIDER = 1000_000_000;
    private static final int BCD_DIVIDER_GROUPS = 3; // log10(BCD_DIVIDER) / BCD_TABLE_DIGITS must be natural value

    private static final char[] BCD_TABLE = makeBcdTable(BCD_TABLE_DIGITS);

    private static char[] makeBcdTable(final int tenPowerMaxIndex) {
        int n = 1;
        for (int i = 0; i < tenPowerMaxIndex; ++i)
            n *= 10;

        final char[] table = new char[n * tenPowerMaxIndex];

        final char[] value = new char[tenPowerMaxIndex];
        Arrays.fill(value, '0');

        for (int i = 0, ib = 0; i < n; ++i) {
            for (int j = 0; j < tenPowerMaxIndex; ++j)
                table[ib++] = value[j];
            value[0] += 1;
            for (int j = 0; j < tenPowerMaxIndex - 1; ++j) {
                if (value[j] <= '9')
                    break;
                else {
                    value[j] -= 10;
                    value[j + 1] += 1;
                }
            }
        }

        return table;
    }

    public static String fastToString(final long value) {
        if (isNull(value))
            return "null";

        if (isNonFinite(value)) {
            // Value is either Inf or NaN
            // TODO: Do we need SNaN?
            return isNaN(value) ? "NaN" : value < 0 ? "-Infinity" : "Infinity";
        }

        //final Decimal64Parts parts = tlsDecimal64Parts.get();
        //final long coefficient = toParts(value, parts);
        long partsCoefficient;
//        long partsSignMask;
        int partsExponent;
        {
//            partsSignMask = value & MASK_SIGN;

            if (isSpecial(value)) {
//                if (isNonFinite(value)) {
//                    partsExponent = 0;
//
//                    partsCoefficient = value & 0xFE03_FFFF_FFFF_FFFFL;
//                    if (UnsignedLong.isGreater(value & 0x0003_FFFF_FFFF_FFFFL, MAX_COEFFICIENT))
//                        partsCoefficient = value & ~MASK_COEFFICIENT;
//                    if (isInfinity(value))
//                        partsCoefficient = value & MASK_SIGN_INFINITY_NAN; // TODO: Why this was done??
////                return 0;
//                } else
                {
                    // Check for non-canonical values.
                    final long coefficient = (value & LARGE_COEFFICIENT_MASK) | LARGE_COEFFICIENT_HIGH_BIT;
                    partsCoefficient = UnsignedLong.isGreater(coefficient, MAX_COEFFICIENT) ? 0 : coefficient;

                    // Extract exponent.
                    final long tmp = value >> EXPONENT_SHIFT_LARGE;
                    partsExponent = (int) (tmp & EXPONENT_MASK);

//                return parts.coefficient;
                }
            } else {
                // Extract exponent. Maximum biased value for "small exponent" is 0x2FF(*2=0x5FE), signed: []
                // upper 1/4 of the mask range is "special", as checked in the code above
                final long tmp = value >> EXPONENT_SHIFT_SMALL;
                partsExponent = (int) (tmp & EXPONENT_MASK);

                // Extract coefficient.
//            return
                partsCoefficient = (value & SMALL_COEFFICIENT_MASK);
            }
        }

        if (partsCoefficient == 0)
            return "0";

        int exponent = partsExponent - EXPONENT_BIAS;

        final char[] buffer = CHAR_BUFFER.get();

        if (exponent >= 0) {
            int bi = buffer.length - exponent;
            for (int i = buffer.length - exponent; i < buffer.length; ++i)
                buffer[i] = '0';

            while (partsCoefficient > 0) {
                bi = formatUIntFromBcdTable((int) (partsCoefficient % BCD_DIVIDER), buffer, bi);
                partsCoefficient /= BCD_DIVIDER;
            }

            while (buffer[bi] == '0')
                ++bi;

            if (value < 0)
                buffer[--bi] = '-';

            return new String(buffer, bi, buffer.length - bi);

        } else { // exponent < 0
            int bi = buffer.length;

            final int digits = numberOfDigits(partsCoefficient);

            if (digits + exponent > 0) {
                long integralPart = partsCoefficient / POWERS_OF_TEN[-exponent];
                long fractionalPart = partsCoefficient % POWERS_OF_TEN[-exponent];

                while (fractionalPart > 0) {
                    bi = formatUIntFromBcdTable((int) (fractionalPart % BCD_DIVIDER), buffer, bi);
                    fractionalPart /= BCD_DIVIDER;
                }

                final int written = buffer.length - bi /* already written */;
                //if (written < -exponent /* must be written */)
                for (int ei = 0, ee = -exponent - written; ei < ee; ++ei)
                    buffer[--bi] = '0';

                bi = buffer.length + exponent; /* buffer.length - (-exponent) */

                buffer[--bi] = '.';

                while (integralPart > 0) {
                    bi = formatUIntFromBcdTable((int) (integralPart % BCD_DIVIDER), buffer, bi);
                    integralPart /= BCD_DIVIDER;
                }

                while (buffer[bi] == '0')
                    ++bi;

                if (value < 0)
                    buffer[--bi] = '-';

                int be = buffer.length;
                while (buffer[be - 1] == '0')
                    --be;

                if (buffer[be - 1] == '.')
                    --be;

                return new String(buffer, bi, be - bi);

            } else {
                while (partsCoefficient > 0) {
                    bi = formatUIntFromBcdTable((int) (partsCoefficient % BCD_DIVIDER), buffer, bi);
                    partsCoefficient /= BCD_DIVIDER;
                }

                final int written = buffer.length - bi /* already written */;
                //if (written < -exponent /* must be written */)
                for (int ei = 0, ee = -exponent - written; ei < ee; ++ei)
                    buffer[--bi] = '0';

                bi = buffer.length + exponent; /* buffer.length - (-exponent) */

                buffer[--bi] = '.';
                buffer[--bi] = '0';

                if (value < 0)
                    buffer[--bi] = '-';

                int be = buffer.length;
                while (buffer[be - 1] == '0')
                    --be;

                return new String(buffer, bi, be - bi);
            }
        }
    }

    public static String fastToScientificString(final long value) {
        if (isNull(value))
            return "null";

        if (isNonFinite(value)) {
            // Value is either Inf or NaN
            // TODO: Do we need SNaN?
            return isNaN(value) ? "NaN" : value < 0 ? "-Infinity" : "Infinity";
        }

        //final Decimal64Parts parts = tlsDecimal64Parts.get();
        //final long coefficient = toParts(value, parts);
        long partsCoefficient;
//        long partsSignMask;
        int partsExponent;
        {
//            partsSignMask = value & MASK_SIGN;

            if (isSpecial(value)) {
//                if (isNonFinite(value)) {
//                    partsExponent = 0;
//
//                    partsCoefficient = value & 0xFE03_FFFF_FFFF_FFFFL;
//                    if (UnsignedLong.isGreater(value & 0x0003_FFFF_FFFF_FFFFL, MAX_COEFFICIENT))
//                        partsCoefficient = value & ~MASK_COEFFICIENT;
//                    if (isInfinity(value))
//                        partsCoefficient = value & MASK_SIGN_INFINITY_NAN; // TODO: Why this was done??
////                return 0;
//                } else
                {
                    // Check for non-canonical values.
                    final long coefficient = (value & LARGE_COEFFICIENT_MASK) | LARGE_COEFFICIENT_HIGH_BIT;
                    partsCoefficient = UnsignedLong.isGreater(coefficient, MAX_COEFFICIENT) ? 0 : coefficient;

                    // Extract exponent.
                    final long tmp = value >> EXPONENT_SHIFT_LARGE;
                    partsExponent = (int) (tmp & EXPONENT_MASK);

//                return parts.coefficient;
                }
            } else {
                // Extract exponent. Maximum biased value for "small exponent" is 0x2FF(*2=0x5FE), signed: []
                // upper 1/4 of the mask range is "special", as checked in the code above
                final long tmp = value >> EXPONENT_SHIFT_SMALL;
                partsExponent = (int) (tmp & EXPONENT_MASK);

                // Extract coefficient.
//            return
                partsCoefficient = (value & SMALL_COEFFICIENT_MASK);
            }
        }

        if (partsCoefficient == 0)
            return "0.000000000000000e+000";

        int exponent = partsExponent - EXPONENT_BIAS;

        final char[] buffer = CHAR_BUFFER.get();

        int bi = MAX_FORMAT_DIGITS * 2 + 2, be = bi;
        while (partsCoefficient > 0) {
            bi = formatUIntFromBcdTable((int) (partsCoefficient % BCD_DIVIDER), buffer, bi);
            partsCoefficient /= BCD_DIVIDER;
        }

        while (buffer[bi] == '0')
            ++bi;

        exponent += be - bi - 1;

        for (int bee = MAX_FORMAT_DIGITS + bi; be < bee; ++be)
            buffer[be] = '0';

        bi--;
        buffer[bi] = buffer[bi + 1];
        buffer[bi + 1] = '.';

        if (value < 0)
            buffer[--bi] = '-';

        buffer[be++] = 'e';
        buffer[be++] = exponent >= 0 ? '+' : '-';
        {
            be += BCD_TABLE_DIGITS;
            for (int j = 0, ti = Math.abs(exponent) * BCD_TABLE_DIGITS /* (remainder << 1) + remainder */; j < BCD_TABLE_DIGITS; ++j, ++ti)
                buffer[be - 1 - j] = BCD_TABLE[ti];
        }

        return new String(buffer, bi, be - bi);
    }

    // Copy-paste of the fastToString
    public static StringBuilder fastAppendToStringBuilder(final long value, final StringBuilder stringBuilder) {
        if (isNull(value))
            return stringBuilder.append("null");

        if (isNonFinite(value)) {
            // Value is either Inf or NaN
            // TODO: Do we need SNaN?
            return stringBuilder.append(isNaN(value) ? "NaN" : value < 0 ? "-Infinity" : "Infinity");
        }

        //final Decimal64Parts parts = tlsDecimal64Parts.get();
        //final long coefficient = toParts(value, parts);
        long partsCoefficient;
//        long partsSignMask;
        int partsExponent;
        {
//            partsSignMask = value & MASK_SIGN;

            if (isSpecial(value)) {
//                if (isNonFinite(value)) {
//                    partsExponent = 0;
//
//                    partsCoefficient = value & 0xFE03_FFFF_FFFF_FFFFL;
//                    if (UnsignedLong.isGreater(value & 0x0003_FFFF_FFFF_FFFFL, MAX_COEFFICIENT))
//                        partsCoefficient = value & ~MASK_COEFFICIENT;
//                    if (isInfinity(value))
//                        partsCoefficient = value & MASK_SIGN_INFINITY_NAN; // TODO: Why this was done??
////                return 0;
//                } else
                {
                    // Check for non-canonical values.
                    final long coefficient = (value & LARGE_COEFFICIENT_MASK) | LARGE_COEFFICIENT_HIGH_BIT;
                    partsCoefficient = UnsignedLong.isGreater(coefficient, MAX_COEFFICIENT) ? 0 : coefficient;

                    // Extract exponent.
                    final long tmp = value >> EXPONENT_SHIFT_LARGE;
                    partsExponent = (int) (tmp & EXPONENT_MASK);

//                return parts.coefficient;
                }
            } else {
                // Extract exponent. Maximum biased value for "small exponent" is 0x2FF(*2=0x5FE), signed: []
                // upper 1/4 of the mask range is "special", as checked in the code above
                final long tmp = value >> EXPONENT_SHIFT_SMALL;
                partsExponent = (int) (tmp & EXPONENT_MASK);

                // Extract coefficient.
//            return
                partsCoefficient = (value & SMALL_COEFFICIENT_MASK);
            }
        }

        if (partsCoefficient == 0)
            return stringBuilder.append('0');

        int exponent = partsExponent - EXPONENT_BIAS;

        final char[] buffer = CHAR_BUFFER.get();

        if (exponent >= 0) {
            int bi = buffer.length - exponent;
            for (int i = buffer.length - exponent; i < buffer.length; ++i)
                buffer[i] = '0';

            while (partsCoefficient > 0) {
                bi = formatUIntFromBcdTable((int) (partsCoefficient % BCD_DIVIDER), buffer, bi);
                partsCoefficient /= BCD_DIVIDER;
            }

            while (buffer[bi] == '0')
                ++bi;

            if (value < 0)
                buffer[--bi] = '-';

            return stringBuilder.append(buffer, bi, buffer.length - bi);

        } else { // exponent < 0
            int bi = buffer.length;

            final int digits = numberOfDigits(partsCoefficient);

            if (digits + exponent > 0) {
                long integralPart = partsCoefficient / POWERS_OF_TEN[-exponent];
                long fractionalPart = partsCoefficient % POWERS_OF_TEN[-exponent];

                while (fractionalPart > 0) {
                    bi = formatUIntFromBcdTable((int) (fractionalPart % BCD_DIVIDER), buffer, bi);
                    fractionalPart /= BCD_DIVIDER;
                }

                final int written = buffer.length - bi /* already written */;
                //if (written < -exponent /* must be written */)
                for (int ei = 0, ee = -exponent - written; ei < ee; ++ei)
                    buffer[--bi] = '0';

                bi = buffer.length + exponent; /* buffer.length - (-exponent) */

                buffer[--bi] = '.';

                while (integralPart > 0) {
                    bi = formatUIntFromBcdTable((int) (integralPart % BCD_DIVIDER), buffer, bi);
                    integralPart /= BCD_DIVIDER;
                }

                while (buffer[bi] == '0')
                    ++bi;

                if (value < 0)
                    buffer[--bi] = '-';

                int be = buffer.length;
                while (buffer[be - 1] == '0')
                    --be;

                if (buffer[be - 1] == '.')
                    --be;

                return stringBuilder.append(buffer, bi, be - bi);

            } else {
                while (partsCoefficient > 0) {
                    bi = formatUIntFromBcdTable((int) (partsCoefficient % BCD_DIVIDER), buffer, bi);
                    partsCoefficient /= BCD_DIVIDER;
                }

                final int written = buffer.length - bi /* already written */;
                //if (written < -exponent /* must be written */)
                for (int ei = 0, ee = -exponent - written; ei < ee; ++ei)
                    buffer[--bi] = '0';

                bi = buffer.length + exponent; /* buffer.length - (-exponent) */

                buffer[--bi] = '.';
                buffer[--bi] = '0';

                if (value < 0)
                    buffer[--bi] = '-';

                int be = buffer.length;
                while (buffer[be - 1] == '0')
                    --be;

                return stringBuilder.append(buffer, bi, be - bi);
            }
        }
    }

    // Copy-paste of the fastToScientificString
    public static StringBuilder fastScientificAppendToStringBuilder(final long value, final StringBuilder stringBuilder) {
        if (isNull(value))
            return stringBuilder.append("null");

        if (isNonFinite(value)) {
            // Value is either Inf or NaN
            // TODO: Do we need SNaN?
            return stringBuilder.append(isNaN(value) ? "NaN" : value < 0 ? "-Infinity" : "Infinity");
        }

        //final Decimal64Parts parts = tlsDecimal64Parts.get();
        //final long coefficient = toParts(value, parts);
        long partsCoefficient;
//        long partsSignMask;
        int partsExponent;
        {
//            partsSignMask = value & MASK_SIGN;

            if (isSpecial(value)) {
//                if (isNonFinite(value)) {
//                    partsExponent = 0;
//
//                    partsCoefficient = value & 0xFE03_FFFF_FFFF_FFFFL;
//                    if (UnsignedLong.isGreater(value & 0x0003_FFFF_FFFF_FFFFL, MAX_COEFFICIENT))
//                        partsCoefficient = value & ~MASK_COEFFICIENT;
//                    if (isInfinity(value))
//                        partsCoefficient = value & MASK_SIGN_INFINITY_NAN; // TODO: Why this was done??
////                return 0;
//                } else
                {
                    // Check for non-canonical values.
                    final long coefficient = (value & LARGE_COEFFICIENT_MASK) | LARGE_COEFFICIENT_HIGH_BIT;
                    partsCoefficient = UnsignedLong.isGreater(coefficient, MAX_COEFFICIENT) ? 0 : coefficient;

                    // Extract exponent.
                    final long tmp = value >> EXPONENT_SHIFT_LARGE;
                    partsExponent = (int) (tmp & EXPONENT_MASK);

//                return parts.coefficient;
                }
            } else {
                // Extract exponent. Maximum biased value for "small exponent" is 0x2FF(*2=0x5FE), signed: []
                // upper 1/4 of the mask range is "special", as checked in the code above
                final long tmp = value >> EXPONENT_SHIFT_SMALL;
                partsExponent = (int) (tmp & EXPONENT_MASK);

                // Extract coefficient.
//            return
                partsCoefficient = (value & SMALL_COEFFICIENT_MASK);
            }
        }

        if (partsCoefficient == 0)
            return stringBuilder.append("0.000000000000000e+000");

        int exponent = partsExponent - EXPONENT_BIAS;

        final char[] buffer = CHAR_BUFFER.get();

        int bi = MAX_FORMAT_DIGITS * 2 + 2, be = bi;
        while (partsCoefficient > 0) {
            bi = formatUIntFromBcdTable((int) (partsCoefficient % BCD_DIVIDER), buffer, bi);
            partsCoefficient /= BCD_DIVIDER;
        }

        while (buffer[bi] == '0')
            ++bi;

        exponent += be - bi - 1;

        for (int bee = MAX_FORMAT_DIGITS + bi; be < bee; ++be)
            buffer[be] = '0';

        bi--;
        buffer[bi] = buffer[bi + 1];
        buffer[bi + 1] = '.';

        if (value < 0)
            buffer[--bi] = '-';

        buffer[be++] = 'e';
        buffer[be++] = exponent >= 0 ? '+' : '-';
        {
            be += BCD_TABLE_DIGITS;
            for (int j = 0, ti = Math.abs(exponent) * BCD_TABLE_DIGITS /* (remainder << 1) + remainder */; j < BCD_TABLE_DIGITS; ++j, ++ti)
                buffer[be - 1 - j] = BCD_TABLE[ti];
        }

        return stringBuilder.append(buffer, bi, be - bi);
    }

    private static class MutableCharBuffer implements CharSequence {
        public final char[] buffer;
        public int offset;
        public int length;

        public MutableCharBuffer() {
            buffer = CHAR_BUFFER.get();
            offset = 0;
            length = 0;
        }

        MutableCharBuffer(final char[] buffer, final int offset, final int length) {
            this.buffer = buffer;
            this.offset = offset;
            this.length = length;
        }

        public MutableCharBuffer setRange(final int offset, final int length) {
            this.offset = offset;
            this.length = length;
            return this;
        }

        @Override
        public int length() {
            return length;
        }

        @Override
        public char charAt(int index) {
            if (index < 0 || index >= length)
                throw new IndexOutOfBoundsException("Wrong argument index(=" + index + ") on the string with length(=" + length + ").");
            return buffer[offset + index];
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            if (start < 0 || end > length || start > end)
                throw new IndexOutOfBoundsException("Wrong arguments start(=" + start + "), end(=" + end + ") on the string with length(=" + length + ").");
            return new MutableCharBuffer(buffer, offset + start, end - start);
        }

        @Override
        public String toString() {
            return new String(buffer, offset, length);
        }
    }

    private static final ThreadLocal<MutableCharBuffer> MUTABLE_CHAR_BUFFER =
        new ThreadLocal<MutableCharBuffer>() {
            @Override
            protected MutableCharBuffer initialValue() {
                return new MutableCharBuffer();
            }
        };

    // Copy-paste of the fastToString
    public static Appendable fastAppendToAppendable(final long value, final Appendable appendable) throws IOException {
        if (isNull(value))
            return appendable.append("null");

        if (isNonFinite(value)) {
            // Value is either Inf or NaN
            // TODO: Do we need SNaN?
            return appendable.append(isNaN(value) ? "NaN" : value < 0 ? "-Infinity" : "Infinity");
        }

        //final Decimal64Parts parts = tlsDecimal64Parts.get();
        //final long coefficient = toParts(value, parts);
        long partsCoefficient;
//        long partsSignMask;
        int partsExponent;
        {
//            partsSignMask = value & MASK_SIGN;

            if (isSpecial(value)) {
//                if (isNonFinite(value)) {
//                    partsExponent = 0;
//
//                    partsCoefficient = value & 0xFE03_FFFF_FFFF_FFFFL;
//                    if (UnsignedLong.isGreater(value & 0x0003_FFFF_FFFF_FFFFL, MAX_COEFFICIENT))
//                        partsCoefficient = value & ~MASK_COEFFICIENT;
//                    if (isInfinity(value))
//                        partsCoefficient = value & MASK_SIGN_INFINITY_NAN; // TODO: Why this was done??
////                return 0;
//                } else
                {
                    // Check for non-canonical values.
                    final long coefficient = (value & LARGE_COEFFICIENT_MASK) | LARGE_COEFFICIENT_HIGH_BIT;
                    partsCoefficient = UnsignedLong.isGreater(coefficient, MAX_COEFFICIENT) ? 0 : coefficient;

                    // Extract exponent.
                    final long tmp = value >> EXPONENT_SHIFT_LARGE;
                    partsExponent = (int) (tmp & EXPONENT_MASK);

//                return parts.coefficient;
                }
            } else {
                // Extract exponent. Maximum biased value for "small exponent" is 0x2FF(*2=0x5FE), signed: []
                // upper 1/4 of the mask range is "special", as checked in the code above
                final long tmp = value >> EXPONENT_SHIFT_SMALL;
                partsExponent = (int) (tmp & EXPONENT_MASK);

                // Extract coefficient.
//            return
                partsCoefficient = (value & SMALL_COEFFICIENT_MASK);
            }
        }

        if (partsCoefficient == 0)
            return appendable.append('0');

        int exponent = partsExponent - EXPONENT_BIAS;

        final MutableCharBuffer charBuffer = MUTABLE_CHAR_BUFFER.get();
        final char[] buffer = charBuffer.buffer;

        if (exponent >= 0) {
            int bi = buffer.length - exponent;
            for (int i = buffer.length - exponent; i < buffer.length; ++i)
                buffer[i] = '0';

            while (partsCoefficient > 0) {
                bi = formatUIntFromBcdTable((int) (partsCoefficient % BCD_DIVIDER), buffer, bi);
                partsCoefficient /= BCD_DIVIDER;
            }

            while (buffer[bi] == '0')
                ++bi;

            if (value < 0)
                buffer[--bi] = '-';

            return appendable.append(charBuffer.setRange(bi, buffer.length - bi));

        } else { // exponent < 0
            int bi = buffer.length;

            final int digits = numberOfDigits(partsCoefficient);

            if (digits + exponent > 0) {
                long integralPart = partsCoefficient / POWERS_OF_TEN[-exponent];
                long fractionalPart = partsCoefficient % POWERS_OF_TEN[-exponent];

                while (fractionalPart > 0) {
                    bi = formatUIntFromBcdTable((int) (fractionalPart % BCD_DIVIDER), buffer, bi);
                    fractionalPart /= BCD_DIVIDER;
                }

                final int written = buffer.length - bi /* already written */;
                //if (written < -exponent /* must be written */)
                for (int ei = 0, ee = -exponent - written; ei < ee; ++ei)
                    buffer[--bi] = '0';

                bi = buffer.length + exponent; /* buffer.length - (-exponent) */

                buffer[--bi] = '.';

                while (integralPart > 0) {
                    bi = formatUIntFromBcdTable((int) (integralPart % BCD_DIVIDER), buffer, bi);
                    integralPart /= BCD_DIVIDER;
                }

                while (buffer[bi] == '0')
                    ++bi;

                if (value < 0)
                    buffer[--bi] = '-';

                int be = buffer.length;
                while (buffer[be - 1] == '0')
                    --be;

                if (buffer[be - 1] == '.')
                    --be;

                return appendable.append(charBuffer.setRange(bi, be - bi));

            } else {
                while (partsCoefficient > 0) {
                    bi = formatUIntFromBcdTable((int) (partsCoefficient % BCD_DIVIDER), buffer, bi);
                    partsCoefficient /= BCD_DIVIDER;
                }

                final int written = buffer.length - bi /* already written */;
                //if (written < -exponent /* must be written */)
                for (int ei = 0, ee = -exponent - written; ei < ee; ++ei)
                    buffer[--bi] = '0';

                bi = buffer.length + exponent; /* buffer.length - (-exponent) */

                buffer[--bi] = '.';
                buffer[--bi] = '0';

                if (value < 0)
                    buffer[--bi] = '-';

                int be = buffer.length;
                while (buffer[be - 1] == '0')
                    --be;

                return appendable.append(charBuffer.setRange(bi, be - bi));
            }
        }
    }

    // Copy-paste of the fastToScientificString
    public static Appendable fastScientificAppendToAppendable(final long value, final Appendable appendable) throws IOException {
        if (isNull(value))
            return appendable.append("null");

        if (isNonFinite(value)) {
            // Value is either Inf or NaN
            // TODO: Do we need SNaN?
            return appendable.append(isNaN(value) ? "NaN" : value < 0 ? "-Infinity" : "Infinity");
        }

        //final Decimal64Parts parts = tlsDecimal64Parts.get();
        //final long coefficient = toParts(value, parts);
        long partsCoefficient;
//        long partsSignMask;
        int partsExponent;
        {
//            partsSignMask = value & MASK_SIGN;

            if (isSpecial(value)) {
//                if (isNonFinite(value)) {
//                    partsExponent = 0;
//
//                    partsCoefficient = value & 0xFE03_FFFF_FFFF_FFFFL;
//                    if (UnsignedLong.isGreater(value & 0x0003_FFFF_FFFF_FFFFL, MAX_COEFFICIENT))
//                        partsCoefficient = value & ~MASK_COEFFICIENT;
//                    if (isInfinity(value))
//                        partsCoefficient = value & MASK_SIGN_INFINITY_NAN; // TODO: Why this was done??
////                return 0;
//                } else
                {
                    // Check for non-canonical values.
                    final long coefficient = (value & LARGE_COEFFICIENT_MASK) | LARGE_COEFFICIENT_HIGH_BIT;
                    partsCoefficient = UnsignedLong.isGreater(coefficient, MAX_COEFFICIENT) ? 0 : coefficient;

                    // Extract exponent.
                    final long tmp = value >> EXPONENT_SHIFT_LARGE;
                    partsExponent = (int) (tmp & EXPONENT_MASK);

//                return parts.coefficient;
                }
            } else {
                // Extract exponent. Maximum biased value for "small exponent" is 0x2FF(*2=0x5FE), signed: []
                // upper 1/4 of the mask range is "special", as checked in the code above
                final long tmp = value >> EXPONENT_SHIFT_SMALL;
                partsExponent = (int) (tmp & EXPONENT_MASK);

                // Extract coefficient.
//            return
                partsCoefficient = (value & SMALL_COEFFICIENT_MASK);
            }
        }

        if (partsCoefficient == 0)
            return appendable.append("0.000000000000000e+000");

        int exponent = partsExponent - EXPONENT_BIAS;

        final MutableCharBuffer charBuffer = MUTABLE_CHAR_BUFFER.get();
        final char[] buffer = charBuffer.buffer;

        int bi = MAX_FORMAT_DIGITS * 2 + 2, be = bi;
        while (partsCoefficient > 0) {
            bi = formatUIntFromBcdTable((int) (partsCoefficient % BCD_DIVIDER), buffer, bi);
            partsCoefficient /= BCD_DIVIDER;
        }

        while (buffer[bi] == '0')
            ++bi;

        exponent += be - bi - 1;

        for (int bee = MAX_FORMAT_DIGITS + bi; be < bee; ++be)
            buffer[be] = '0';

        bi--;
        buffer[bi] = buffer[bi + 1];
        buffer[bi + 1] = '.';

        if (value < 0)
            buffer[--bi] = '-';

        buffer[be++] = 'e';
        buffer[be++] = exponent >= 0 ? '+' : '-';
        {
            be += BCD_TABLE_DIGITS;
            for (int j = 0, ti = Math.abs(exponent) * BCD_TABLE_DIGITS /* (remainder << 1) + remainder */; j < BCD_TABLE_DIGITS; ++j, ++ti)
                buffer[be - 1 - j] = BCD_TABLE[ti];
        }

        return appendable.append(charBuffer.setRange(bi, be - bi));
    }

    private static int formatUIntFromBcdTable(int value, final char[] buffer, int bi) {
        for (int blockIndex = 0; blockIndex < BCD_DIVIDER_GROUPS; ++blockIndex) {
            final int newValue = (int) (2199023256L * value >>> 41);
            final int remainder = value - newValue * 1000;
            //final int remainder = value - ((newValue << 10) - (newValue << 4) - (newValue << 3));
            value = newValue;

            for (int j = 0, ti = remainder * BCD_TABLE_DIGITS /* (remainder << 1) + remainder */; j < BCD_TABLE_DIGITS; ++j, ++ti)
                buffer[--bi] = BCD_TABLE[ti];
        }

        return bi;
    }

    private static long dropTrailingZeros(long value) {
        assert value >= 0;
        while (value != 0) {
            final long quotient = value / 10;
            final long remainder = value - (quotient << 3) - (quotient << 1);
            if (remainder != 0)
                return value;
            value = quotient;
        }
        return value;
    }

    private static Appendable appendLongTo(final long value, final Appendable appendable) throws IOException {
        return appendLongTo(value, appendable, numberOfDigits(value));
    }

    private static Appendable appendLongTo(long value, final Appendable appendable, final int numberOfDigits) throws IOException {
        assert value >= 0;
        for (int i = numberOfDigits - 1; i >= 1; i -= 1) {
            final long powerOfTen = POWERS_OF_TEN[i];
            final int digit = (int) (value / powerOfTen);
            assert 0 <= digit && digit <= 9;
            appendable.append(DECIMAL_DIGITS[digit]);
            value %= powerOfTen;
        }

        assert 0 <= value && value <= 9;
        return appendable.append(DECIMAL_DIGITS[(int) value]);
    }

    static String toDebugString(final long value) {

        final Decimal64Parts parts = new Decimal64Parts();
        toParts(value, parts);

        final StringBuilder sb = new StringBuilder(64).append("0x").append(Long.toHexString(value))
            .append("=");
        if (NULL == value) {
            sb.append("null");
        } else {
            sb.append(value < 0 ? '-' : '+');
            if (!isSpecial(value)) {
                sb.append(parts.coefficient).append('E').append(parts.exponent);

            } else {
                sb.append(isNaN(value) ? "NaN" : "Infinity");
            }
        }

        return sb.toString();
    }

    public static final char[] DECIMAL_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    public static final long[] POWERS_OF_TEN = {
        /*  0 */ 1L,
        /*  1 */ 10L,
        /*  2 */ 100L,
        /*  3 */ 1000L,
        /*  4 */ 10000L,
        /*  5 */ 100000L,
        /*  6 */ 1000000L,
        /*  7 */ 10000000L,
        /*  8 */ 100000000L,
        /*  9 */ 1000000000L,
        /* 10 */ 10000000000L,
        /* 11 */ 100000000000L,
        /* 12 */ 1000000000000L,
        /* 13 */ 10000000000000L,
        /* 14 */ 100000000000000L,
        /* 15 */ 1000000000000000L,
        /* 16 */ 10000000000000000L,
        /* 17 */ 100000000000000000L,
        /* 18 */ 1000000000000000000L
    };

    /**
     * Computes number of dfp digits required to represent the given non-negative value.
     *
     * @param value Non-negative value.
     * @return Number of digits required.
     */
    public static int numberOfDigits(final long value) {
        for (int i = 1; i < POWERS_OF_TEN.length; i += 1)
            if (value < POWERS_OF_TEN[i])
                return i;
        return 19;
    }

    static final long MASK_SIGN = 0x8000_0000_0000_0000L;
    static final long MASK_SPECIAL = 0x6000_0000_0000_0000L;
    static final long MASK_INFINITY_NAN = 0x7C00_0000_0000_0000L;
    static final long MASK_SIGN_INFINITY_NAN = 0xFC00_0000_0000_0000L; // SIGN|INF|NAN

    static final long MASK_INFINITY_AND_NAN = 0x7800_0000_0000_0000L;

    static final int EXPONENT_BIAS = 398;
    static final int BIASED_EXPONENT_MAX_VALUE = 767;
    static final int BIASED_EXPONENT_MIN_VALUE = 0;

    static final long LARGE_COEFFICIENT_MASK = 0x0007_FFFF_FFFF_FFFFL;
    static final long LARGE_COEFFICIENT_HIGH_BIT = 0x0020_0000_0000_0000L;

    static final long SMALL_COEFFICIENT_MASK = 0x001F_FFFF_FFFF_FFFFL;
    static final long MASK_COEFFICIENT = 0x0001_FFFF_FFFF_FFFFL;

    static final int EXPONENT_MASK = 0x03FF;
    static final int EXPONENT_SHIFT_LARGE = 51;
    static final int EXPONENT_SHIFT_SMALL = 53;
    static final long EXPONENT_MASK_SMALL = (long) EXPONENT_MASK << EXPONENT_SHIFT_SMALL;
    static final long EXPONENT_MASK_LARGE = (long) EXPONENT_MASK << EXPONENT_SHIFT_LARGE;

    static final int MAX_FORMAT_DIGITS = 16;

    static final long MASK_STEERING_BITS = 0x6000_0000_0000_0000L;
    private static final long MASK_BINARY_EXPONENT1 = 0x7FE0_0000_0000_0000L;
    private static final long MASK_BINARY_SIG1 = 0x001F_FFFF_FFFF_FFFFL;
    private static final long MASK_BINARY_EXPONENT2 = 0x1FF8_0000_0000_0000L;
    // Used to take G[2:w+3] (sec 3.3)
    private static final long MASK_BINARY_SIG2 = 0x0007_FFFF_FFFF_FFFFL;
    // Used to mask out G4:T0 (sec 3.3)
    private static final long MASK_BINARY_OR2 = 0x0020_0000_0000_0000L;
    // Used to prefix 8+G4 to T (sec 3.3)
    private static final int UPPER_EXPON_LIMIT = 51;
    private static final long MASK_EXP = 0x7FFE_0000_0000_0000L;
    private static final long MASK_EXP2 = 0x1FFF_8000_0000_0000L;

    private static final long MASK_SNAN = 0x7E00_0000_0000_0000L;
    private static final long MASK_ANY_INF = 0x7C00_0000_0000_0000L;

    private static NumberFormatException invalidFormat(final CharSequence s, final int si, final int ei) {
        return new NumberFormatException(s.subSequence(si, ei).toString());
    }

    public static long parse(final CharSequence s, final int si, final int ei, final int roundingMode) {
        char c;
        int p = si;
        boolean sign = false;

        c = s.charAt(p);
        if (c == '+') {
            p += 1;
            c = p < ei ? s.charAt(p) : 0;
        }
        if (c == '-') {
            sign = true;

            p += 1;
            c = p < ei ? s.charAt(p) : 0;
        }

        if (c != '.' && (c < '0' || c > '9')) {
            if (TextUtils.equalsIgnoringCase(s, p, ei, "Infinity") ||
                TextUtils.equalsIgnoringCase(s, p, ei, "Inf"))
                return sign ? JavaImpl.NEGATIVE_INFINITY : JavaImpl.POSITIVE_INFINITY;
            if (TextUtils.equalsIgnoringCase(s, p, ei, "NaN") ||
                TextUtils.equalsIgnoringCase(s, p, ei, "SNaN"))
                return JavaImpl.NaN;
            throw invalidFormat(s, si, ei);
        }

        boolean seenRadixPoint = false;
        int leadingZerosAfterPoint = 0;

        if (c == '0' || c == '.') {
            if (c == '.') {
                seenRadixPoint = true;

                p += 1;
                c = p < ei ? s.charAt(p) : 0;
            }

            while (c == '0') {
                p += 1;
                c = p < ei ? s.charAt(p) : 0;

                if (seenRadixPoint)
                    leadingZerosAfterPoint += 1;

                if (c == '.') {
                    if (seenRadixPoint)
                        throw invalidFormat(s, si, ei);
                    seenRadixPoint = true;

                    p += 1;
                    c = p < ei ? s.charAt(p) : 0;
                    if (c == 0)
                        return makeZero(sign, EXPONENT_BIAS - leadingZerosAfterPoint);
                } else if (c == 0) {
                    return makeZero(sign, EXPONENT_BIAS - leadingZerosAfterPoint);
                }
            }
        }

        int numberOfDigits = 0;
        int decimalExponentScale = 0;
        long coefficient = 0;
        boolean roundedUp = false, rounded = false, midpoint = false;
        int additionalExponent = 0;

        while ((c >= '0' && c <= '9') || c == '.') {
            if (c == '.') {
                if (seenRadixPoint)
                    throw invalidFormat(s, si, ei);

                seenRadixPoint = true;

                p += 1;
                c = p < ei ? s.charAt(p) : 0;

                continue;
            }

            if (seenRadixPoint)
                decimalExponentScale += 1;

            numberOfDigits += 1;
            if (numberOfDigits <= 16) {
                coefficient = (coefficient << 1) + (coefficient << 3) + c - '0';
            } else if (numberOfDigits == 17) {
                switch (roundingMode) {
                    case BID_ROUNDING_EXCEPTION:
                        throw new NumberFormatException("Can't convert string(='" + s.subSequence(si, ei) + "') to Decimal64 without precision loss.");

                    case BID_ROUNDING_TO_NEAREST:
                        midpoint = (c == '5' && (coefficient & 1) == 0);
                        if (c > '5' || (c == '5' && (coefficient & 1) != 0)) {
                            coefficient += 1;
                            roundedUp = true;
                        }
                        break;

                    case BID_ROUNDING_DOWN:
                        if (sign) {
                            coefficient += 1;
                            roundedUp = true;
                        }
                        break;

                    case BID_ROUNDING_UP:
                        if (!sign) {
                            coefficient += 1;
                            roundedUp = true;
                        }
                        break;

                    case BID_ROUNDING_TIES_AWAY:
                        if (c >= '5') {
                            coefficient += 1;
                            roundedUp = true;
                        }
                        break;
                }
                if (coefficient == 10000000000000000L) {
                    coefficient = 1000000000000000L;
                    additionalExponent = 1;
                }
                if (c > '0')
                    rounded = true;
                additionalExponent += 1;
            } else {
                additionalExponent += 1;
                if (midpoint && c >= '0') {
                    coefficient += 1;
                    midpoint = false;
                    roundedUp = true;
                }
                if (c >= '0')
                    rounded = true;
            }

            p += 1;
            c = p < ei ? s.charAt(p) : 0;
        }

        additionalExponent -= (decimalExponentScale + leadingZerosAfterPoint);

        if (c == 0)
            return fastPackCheckOverflow(sign, additionalExponent + EXPONENT_BIAS, coefficient);

        if (c != 'E' && c != 'e')
            throw invalidFormat(s, si, ei);

        p += 1;
        c = p < ei ? s.charAt(p) : 0;

        final boolean isExponentSigned = c == '-';
        if (c == '-' || c == '+') {
            p += 1;
            c = p < ei ? s.charAt(p) : 0;
        }

        if (c == 0 || c < '0' || c > '9')
            throw invalidFormat(s, si, ei);

        int exponent = 0;
        while (c >= '0' && c <= '9') {
            if (exponent < (1 << 20)) {
                exponent = (exponent << 1) + (exponent << 3) + (c - '0');

                p += 1;
                c = p < ei ? s.charAt(p) : 0;
            }
        }

        if (c != 0)
            throw invalidFormat(s, si, ei);

        if (isExponentSigned)
            exponent = -exponent;
        exponent += additionalExponent + EXPONENT_BIAS;

        if (exponent < 0) {
            if (roundedUp)
                coefficient -= 1;
            return packUnderflow(sign, exponent, coefficient, rounded, BID_ROUNDING_TO_NEAREST);
        }

        return pack(sign ? MASK_SIGN : 0, exponent, coefficient, BID_ROUNDING_TO_NEAREST);
    }

    private static long makeZero(final boolean isNegative, final int exponent) {
        final long value = ((long) exponent << EXPONENT_SHIFT_SMALL);
        return isNegative ? negate(value) : value;
    }

    private static long fastPackCheckOverflow(final boolean isSigned, int exponent, long coefficient) {
        long result;
        final long signMask = isSigned ? MASK_SIGN : 0;

        if (UnsignedInteger.isGreaterOrEqual(exponent, 3 * 256 - 1)) {
            if ((exponent == 3 * 256 - 1) && coefficient == 10000000000000000L) {
                exponent = 3 * 256;
                coefficient = 1000000000000000L;
            }

            if (UnsignedInteger.isGreaterOrEqual(exponent, 3 * 256)) {
                while (coefficient < 1000000000000000L && exponent >= 3 * 256) {
                    exponent--;
                    coefficient = (coefficient << 3) + (coefficient << 1);
                }
                if (exponent > BIASED_EXPONENT_MAX_VALUE)
                    return signMask | POSITIVE_INFINITY;
            }
        }

        long mask = 1L << EXPONENT_SHIFT_SMALL;

        // Check whether coefficient fits in 10 * 5 + 3 bits.
        if (coefficient < mask) {
            result = exponent;
            result <<= EXPONENT_SHIFT_SMALL;
            result |= (coefficient | signMask);
            return result;
        }

        // Eliminate the case coefficient == 10^16 after rounding.
        if (coefficient == 10000000000000000L) {
            result = exponent + 1;
            result <<= EXPONENT_SHIFT_SMALL;
            result |= (1000000000000000L | signMask);
            return result;
        }

        result = exponent;
        result <<= EXPONENT_SHIFT_LARGE;
        result |= (signMask | MASK_SPECIAL);

        // Add coefficient, without leading bits.
        mask = (mask >> 2) - 1;
        coefficient &= mask;
        result |= coefficient;

        return result;
    }

    private final static long UINT32_MAX = 0xFFFF_FFFFL;

    private static long packUnderflow(final boolean isSigned, final int exponent, long coefficient,
                                      final boolean rounded, int roundingMode) {
        final long sgn = isSigned ? MASK_SIGN : 0L;
        final long C128_0;
        final long Q_low_0;
        final long Q_low_1;
        long _C64;
        long remainder_h;
        final long QH;
        final int extra_digits;
        final int amount;
        final int amount2;

        // Underflow
        if (exponent + MAX_FORMAT_DIGITS < 0) {
            if (roundingMode == BID_ROUNDING_EXCEPTION)
                throw new NumberFormatException("The exponent(=" + exponent + ") of the value (=" + coefficient + ") with sign(=" + (isSigned ? '-' : '+') + ") can't be saved to Decimal64 without precision loss.");
            if (roundingMode == BID_ROUNDING_DOWN && isSigned)
                return 0x8000000000000001L;
            if (roundingMode == BID_ROUNDING_UP && !isSigned)
                return 1L;
            return sgn;
        }
        coefficient = (coefficient << 3) + (coefficient << 1);

        if (isSigned && (roundingMode == BID_ROUNDING_DOWN || roundingMode == BID_ROUNDING_UP))
            roundingMode = 3 - roundingMode;

        if (rounded)
            coefficient |= 1;

        // get digits to be shifted out
        extra_digits = 1 - exponent;
        C128_0 = coefficient + bid_round_const_table[roundingMode][extra_digits];

        // get coefficient*(2^M[extra_digits])/10^extra_digits
        {
            final long ALBL_0;
            final long ALBL_1;
            final long ALBH_0;
            final long ALBH_1;
            final long QM2_0;
            final long QM2_1;
            {
                final long CXH;
                final long CXL;
                final long CYH;
                final long CYL;
                final long PL;
                long PH;
                long PM;
                final long PM2;
                CXH = (C128_0) >>> 32;
                CXL = C128_0 & UINT32_MAX;
                CYH = (bid_reciprocals10_128[extra_digits][1]) >>> 32;
                CYL = bid_reciprocals10_128[extra_digits][1] & UINT32_MAX;
                PM = CXH * CYL;
                PH = CXH * CYH;
                PL = CXL * CYL;
                PM2 = CXL * CYH;
                PH += (PM >>> 32);
                PM = (PM & UINT32_MAX) + PM2 + (PL >>> 32);
                ALBH_1 = PH + (PM >>> 32);
                ALBH_0 = (PM << 32) + (PL & UINT32_MAX);
            }
            {
                final long CXH;
                final long CXL;
                final long CYH;
                final long CYL;
                final long PL;
                long PH;
                long PM;
                final long PM2;
                CXH = ((C128_0)) >>> 32;
                CXL = C128_0 & UINT32_MAX;
                CYH = (bid_reciprocals10_128[extra_digits][0]) >>> 32;
                CYL = bid_reciprocals10_128[extra_digits][0] & UINT32_MAX;
                PM = CXH * CYL;
                PH = CXH * CYH;
                PL = CXL * CYL;
                PM2 = CXL * CYH;
                PH += (PM >>> 32);
                PM = (PM & UINT32_MAX) + PM2 + (PL >>> 32);
                ALBL_1 = PH + (PM >>> 32);
                ALBL_0 = (PM << 32) + (PL & UINT32_MAX);
            }
            Q_low_0 = ALBL_0;
            {
                long R64H;
                R64H = ALBH_1;
                QM2_0 = ALBL_1 + ALBH_0;
                if (QM2_0 < ALBL_1)
                    R64H++;
                QM2_1 = R64H;
            }
            Q_low_1 = QM2_0;
            QH = QM2_1;
        }

        // now get P/10^extra_digits: shift Q_high right by M[extra_digits]-128
        amount = bid_recip_scale[extra_digits];

        _C64 = QH >>> amount;

        if (roundingMode == BID_ROUNDING_TO_NEAREST)
            if ((_C64 & 1) != 0) {
                // check whether fractional part of initial_P/10^extra_digits is exactly .5

                // get remainder
                amount2 = 64 - amount;
                remainder_h = 0;
                remainder_h--;
                remainder_h >>>= amount2;
                remainder_h = remainder_h & QH;

                if (remainder_h == 0
                    && (Q_low_1 < bid_reciprocals10_128[extra_digits][1]
                    || (Q_low_1 == bid_reciprocals10_128[extra_digits][1]
                    && Q_low_0 < bid_reciprocals10_128[extra_digits][0]))) {
                    _C64--;
                }
            }

        return sgn | _C64;
    }

    private static final int[] bid_recip_scale = {
        129 - 128,    // 1
        129 - 128,    // 1/10
        129 - 128,    // 1/10^2
        129 - 128,    // 1/10^3
        3,    // 131 - 128
        6,    // 134 - 128
        9,    // 137 - 128
        13,    // 141 - 128
        16,    // 144 - 128
        19,    // 147 - 128
        23,    // 151 - 128
        26,    // 154 - 128
        29,    // 157 - 128
        33,    // 161 - 128
        36,    // 164 - 128
        39,    // 167 - 128
        43,    // 171 - 128
        46,    // 174 - 128
        49,    // 177 - 128
        53,    // 181 - 128
        56,    // 184 - 128
        59,    // 187 - 128
        63,    // 191 - 128

        66,    // 194 - 128
        69,    // 197 - 128
        73,    // 201 - 128
        76,    // 204 - 128
        79,    // 207 - 128
        83,    // 211 - 128
        86,    // 214 - 128
        89,    // 217 - 128
        92,    // 220 - 128
        96,    // 224 - 128
        99,    // 227 - 128
        102,    // 230 - 128
        109,    // 237 - 128, 1/10^35
    };

    private static final long[][] bid_round_const_table = new long[][]{
        {    // RN
            0L,    // 0 extra digits
            5L,    // 1 extra digits
            50L,    // 2 extra digits
            500L,    // 3 extra digits
            5000L,    // 4 extra digits
            50000L,    // 5 extra digits
            500000L,    // 6 extra digits
            5000000L,    // 7 extra digits
            50000000L,    // 8 extra digits
            500000000L,    // 9 extra digits
            5000000000L,    // 10 extra digits
            50000000000L,    // 11 extra digits
            500000000000L,    // 12 extra digits
            5000000000000L,    // 13 extra digits
            50000000000000L,    // 14 extra digits
            500000000000000L,    // 15 extra digits
            5000000000000000L,    // 16 extra digits
            50000000000000000L,    // 17 extra digits
            500000000000000000L    // 18 extra digits
        },
        {    // RD
            0L,    // 0 extra digits
            0L,    // 1 extra digits
            0L,    // 2 extra digits
            0L,    // 3 extra digits
            0L,    // 4 extra digits
            0L,    // 5 extra digits
            0L,    // 6 extra digits
            0L,    // 7 extra digits
            0L,    // 8 extra digits
            0L,    // 9 extra digits
            0L,    // 10 extra digits
            0L,    // 11 extra digits
            0L,    // 12 extra digits
            0L,    // 13 extra digits
            0L,    // 14 extra digits
            0L,    // 15 extra digits
            0L,    // 16 extra digits
            0L,    // 17 extra digits
            0L    // 18 extra digits
        },
        {    // round to Inf
            0L,    // 0 extra digits
            9L,    // 1 extra digits
            99L,    // 2 extra digits
            999L,    // 3 extra digits
            9999L,    // 4 extra digits
            99999L,    // 5 extra digits
            999999L,    // 6 extra digits
            9999999L,    // 7 extra digits
            99999999L,    // 8 extra digits
            999999999L,    // 9 extra digits
            9999999999L,    // 10 extra digits
            99999999999L,    // 11 extra digits
            999999999999L,    // 12 extra digits
            9999999999999L,    // 13 extra digits
            99999999999999L,    // 14 extra digits
            999999999999999L,    // 15 extra digits
            9999999999999999L,    // 16 extra digits
            99999999999999999L,    // 17 extra digits
            999999999999999999L    // 18 extra digits
        },
        {    // RZ
            0L,    // 0 extra digits
            0L,    // 1 extra digits
            0L,    // 2 extra digits
            0L,    // 3 extra digits
            0L,    // 4 extra digits
            0L,    // 5 extra digits
            0L,    // 6 extra digits
            0L,    // 7 extra digits
            0L,    // 8 extra digits
            0L,    // 9 extra digits
            0L,    // 10 extra digits
            0L,    // 11 extra digits
            0L,    // 12 extra digits
            0L,    // 13 extra digits
            0L,    // 14 extra digits
            0L,    // 15 extra digits
            0L,    // 16 extra digits
            0L,    // 17 extra digits
            0L    // 18 extra digits
        },
        {    // round ties away from 0
            0L,    // 0 extra digits
            5L,    // 1 extra digits
            50L,    // 2 extra digits
            500L,    // 3 extra digits
            5000L,    // 4 extra digits
            50000L,    // 5 extra digits
            500000L,    // 6 extra digits
            5000000L,    // 7 extra digits
            50000000L,    // 8 extra digits
            500000000L,    // 9 extra digits
            5000000000L,    // 10 extra digits
            50000000000L,    // 11 extra digits
            500000000000L,    // 12 extra digits
            5000000000000L,    // 13 extra digits
            50000000000000L,    // 14 extra digits
            500000000000000L,    // 15 extra digits
            5000000000000000L,    // 16 extra digits
            50000000000000000L,    // 17 extra digits
            500000000000000000L    // 18 extra digits
        }
    };


    private static final long[][] bid_reciprocals10_128 = {
        {0L, 0L},                                      // 0 extra digits
        {0x3333333333333334L, 0x3333333333333333L},    // 1 extra digit
        {0x51eb851eb851eb86L, 0x051eb851eb851eb8L},    // 2 extra digits
        {0x3b645a1cac083127L, 0x0083126e978d4fdfL},    // 3 extra digits
        {0x4af4f0d844d013aaL, 0x00346dc5d6388659L},    //  10^(-4) * 2^131
        {0x08c3f3e0370cdc88L, 0x0029f16b11c6d1e1L},    //  10^(-5) * 2^134
        {0x6d698fe69270b06dL, 0x00218def416bdb1aL},    //  10^(-6) * 2^137
        {0xaf0f4ca41d811a47L, 0x0035afe535795e90L},    //  10^(-7) * 2^141
        {0xbf3f70834acdaea0L, 0x002af31dc4611873L},    //  10^(-8) * 2^144
        {0x65cc5a02a23e254dL, 0x00225c17d04dad29L},    //  10^(-9) * 2^147
        {0x6fad5cd10396a214L, 0x0036f9bfb3af7b75L},    // 10^(-10) * 2^151
        {0xbfbde3da69454e76L, 0x002bfaffc2f2c92aL},    // 10^(-11) * 2^154
        {0x32fe4fe1edd10b92L, 0x00232f33025bd422L},    // 10^(-12) * 2^157
        {0x84ca19697c81ac1cL, 0x00384b84d092ed03L},    // 10^(-13) * 2^161
        {0x03d4e1213067bce4L, 0x002d09370d425736L},    // 10^(-14) * 2^164
        {0x3643e74dc052fd83L, 0x0024075f3dceac2bL},    // 10^(-15) * 2^167
        {0x56d30baf9a1e626bL, 0x0039a5652fb11378L},    // 10^(-16) * 2^171
        {0x12426fbfae7eb522L, 0x002e1dea8c8da92dL},    // 10^(-17) * 2^174
        {0x41cebfcc8b9890e8L, 0x0024e4bba3a48757L},    // 10^(-18) * 2^177
        {0x694acc7a78f41b0dL, 0x003b07929f6da558L},    // 10^(-19) * 2^181
        {0xbaa23d2ec729af3eL, 0x002f394219248446L},    // 10^(-20) * 2^184
        {0xfbb4fdbf05baf298L, 0x0025c768141d369eL},    // 10^(-21) * 2^187
        {0x2c54c931a2c4b759L, 0x003c7240202ebdcbL},    // 10^(-22) * 2^191
        {0x89dd6dc14f03c5e1L, 0x00305b66802564a2L},    // 10^(-23) * 2^194
        {0xd4b1249aa59c9e4eL, 0x0026af8533511d4eL},    // 10^(-24) * 2^197
        {0x544ea0f76f60fd49L, 0x003de5a1ebb4fbb1L},    // 10^(-25) * 2^201
        {0x76a54d92bf80caa1L, 0x00318481895d9627L},    // 10^(-26) * 2^204
        {0x921dd7a89933d54eL, 0x00279d346de4781fL},    // 10^(-27) * 2^207
        {0x8362f2a75b862215L, 0x003f61ed7ca0c032L},    // 10^(-28) * 2^211
        {0xcf825bb91604e811L, 0x0032b4bdfd4d668eL},    // 10^(-29) * 2^214
        {0x0c684960de6a5341L, 0x00289097fdd7853fL},    // 10^(-30) * 2^217
        {0x3d203ab3e521dc34L, 0x002073accb12d0ffL},    // 10^(-31) * 2^220
        {0x2e99f7863b696053L, 0x0033ec47ab514e65L},    // 10^(-32) * 2^224
        {0x587b2c6b62bab376L, 0x002989d2ef743eb7L},    // 10^(-33) * 2^227
        {0xad2f56bc4efbc2c5L, 0x00213b0f25f69892L},    // 10^(-34) * 2^230
        {0x0f2abc9d8c9689d1L, 0x01a95a5b7f87a0efL},    // 35 extra digits
    };

    static final int BID_ROUNDING_TO_NEAREST = 0x00000;
    static final int BID_ROUNDING_DOWN = 0x00001;
    static final int BID_ROUNDING_UP = 0x00002;
    static final int BID_ROUNDING_TO_ZERO = 0x00003;
    static final int BID_ROUNDING_TIES_AWAY = 0x00004;
    static final int BID_ROUNDING_EXCEPTION = 0x00005;

    static void throwPackException(final long signMask, final int exponentIn, final long coefficientIn) {
        throw new NumberFormatException("The unbiasedExponent(=" + (exponentIn - EXPONENT_BIAS) + ") of the value (=" + coefficientIn + ") with sign(=" + (signMask != 0 ? '-' : '+') + ") can't be saved to Decimal64 without precision loss.");
    }

    public static long pack(final long signMask, final int exponentIn, final long coefficientIn, int roundingMode) {
        final long Q_low_0;
        final long Q_low_1;
        final long QH;
        long r;
        long mask;
        long _C64;
        long remainder_h;

        int exponent = exponentIn;
        long coefficient = coefficientIn;

        final int extra_digits;
        final int amount;
        final int amount2;

        // TODO: optimize: (coefficient always positive! & use more efficient comparison)
        long tenDivRem = 0;
        boolean isAnyNonZeroRem = false;
        while (UnsignedLong.isGreater(coefficient, 9999999999999999L)) {
            tenDivRem = coefficient % 10;
            if (tenDivRem != 0) {
                if (roundingMode == BID_ROUNDING_EXCEPTION)
                    throwPackException(signMask, exponentIn, coefficientIn);
                isAnyNonZeroRem = true;
            }
            coefficient /= 10;
            exponent++;
        }

        if (isAnyNonZeroRem) {
            switch (roundingMode) {
                case BID_ROUNDING_TO_NEAREST:
                    if (tenDivRem >= 5) // Rounding away from zero
                        coefficient++;
                    break;

                case BID_ROUNDING_DOWN:
                    if (signMask != 0 /*&& isAnyNonZeroRem - already checked*/)
                        coefficient++;
                    break;

                case BID_ROUNDING_UP:
                    if (signMask == 0 /*&& isAnyNonZeroRem - already checked*/)
                        coefficient++;
                    break;

                case BID_ROUNDING_TO_ZERO:
                    break;

                case BID_ROUNDING_TIES_AWAY:
                    //if (isAnyNonZeroRem)  - already checked
                    coefficient++;
                    break;

                case BID_ROUNDING_EXCEPTION:
                    throwPackException(signMask, exponentIn, coefficientIn);

                default:
                    throw new IllegalArgumentException("Unsupported roundingMode(=" + roundingMode + ") value.");

            }
        }

        if (UnsignedLong.isGreater(coefficient, 9999999999999999L)) {
            if (roundingMode == BID_ROUNDING_EXCEPTION)
                throwPackException(signMask, exponentIn, coefficientIn);
            exponent++;
            coefficient = 1000000000000000L;
        }

        // Check for possible underflow/overflow.
        if (exponent > BIASED_EXPONENT_MAX_VALUE || exponent < 0) {
            if (exponent < 0) {
                // Underflow.
                if (exponent + MAX_FORMAT_DIGITS < 0) {
                    if (roundingMode == BID_ROUNDING_EXCEPTION)
                        throwPackException(signMask, exponentIn, coefficientIn);
                    if (roundingMode == BID_ROUNDING_DOWN && signMask < 0)
                        return 0x8000000000000001L;
                    if (roundingMode == BID_ROUNDING_UP && signMask >= 0)
                        return 1L;
                    return signMask;
                }

                if (signMask < 0 && (roundingMode == BID_ROUNDING_DOWN || roundingMode == BID_ROUNDING_UP))
                    roundingMode = 3 - roundingMode;

                // Get digits to be shifted out
                extra_digits = -exponent;
                coefficient += bid_round_const_table[roundingMode][extra_digits];

                // Get coefficient * (2^M[extra_digits])/10^extra_digits
                {
                    final long ALBL_0;
                    final long ALBL_1;
                    final long ALBH_0;
                    final long ALBH_1;
                    final long QM2_0;
                    final long QM2_1;
                    {
                        final long CXH;
                        final long CXL;
                        final long CYH;
                        final long CYL;
                        final long PL;
                        long PH;
                        long PM;
                        final long PM2;
                        CXH = coefficient >>> 32;
                        CXL = (int) ((coefficient));
                        CYH = bid_reciprocals10_128[extra_digits][1] >>> 32;
                        CYL = (int) bid_reciprocals10_128[extra_digits][1];
                        PM = CXH * CYL;
                        PH = CXH * CYH;
                        PL = CXL * CYL;
                        PM2 = CXL * CYH;
                        PH += (PM >>> 32);
                        PM = (PM & 0xFFFFFFFFL) + PM2 + (PL >> 32);
                        ALBH_1 = PH + (PM >> 32);
                        ALBH_0 = (PM << 32) + (int) PL;
                    }
                    {
                        final long CXH;
                        final long CXL;
                        final long CYH;
                        final long CYL;
                        final long PL;
                        long PH;
                        long PM;
                        final long PM2;
                        CXH = ((coefficient)) >>> 32;
                        CXL = (int) ((coefficient));
                        CYH = bid_reciprocals10_128[extra_digits][0] >>> 32;
                        CYL = (int) bid_reciprocals10_128[extra_digits][0];
                        PM = CXH * CYL;
                        PH = CXH * CYH;
                        PL = CXL * CYL;
                        PM2 = CXL * CYH;
                        PH += (PM >>> 32);
                        PM = (PM & 0xFFFFFFFFL) + PM2 + (PL >>> 32);
                        ALBL_1 = PH + (PM >>> 32);
                        ALBL_0 = (PM << 32) + (PL & 0xFFFFFFFFL);
                    }
                    Q_low_0 = ALBL_0;
                    {
                        long R64H;
                        R64H = ALBH_1;
                        QM2_0 = ALBL_1 + ALBH_0;
                        if (QM2_0 < ALBL_1)
                            R64H++;
                        QM2_1 = R64H;
                    }
                    Q_low_1 = QM2_0;
                    QH = QM2_1;
                }

                // Now get P/10^extra_digits: shift Q_high right by M[extra_digits]-128
                amount = bid_recip_scale[extra_digits];

                _C64 = QH >>> amount;

                if (roundingMode == BID_ROUNDING_TO_NEAREST)
                    if ((_C64 & 1) != 0) {
                        // Check whether fractional part of initial_P/10^extra_digits is exactly .5
                        // Get remainder
                        amount2 = 64 - amount;
                        remainder_h = 0;
                        remainder_h--;
                        remainder_h >>>= amount2;
                        remainder_h = remainder_h & QH;

                        if (remainder_h == 0L
                            && (Q_low_1 < bid_reciprocals10_128[extra_digits][1]
                            || (Q_low_1 == bid_reciprocals10_128[extra_digits][1]
                            && Q_low_0 < bid_reciprocals10_128[extra_digits][0]))) {
                            _C64--;
                        }
                    }
                return signMask | _C64;
            }
            if (coefficient == 0L) {
                if (exponent > BIASED_EXPONENT_MAX_VALUE)
                    exponent = BIASED_EXPONENT_MAX_VALUE;
            }
            while (UnsignedLong.isLess(coefficient, 1000000000000000L) && exponent >= 3 * 256) {
                exponent--;
                coefficient = (coefficient << 3) + (coefficient << 1);
            }
            if (exponent > BIASED_EXPONENT_MAX_VALUE) {
                // Overflow
                r = signMask | MASK_INFINITY_AND_NAN;
                switch (roundingMode) {
                    case BID_ROUNDING_EXCEPTION:
                        throwPackException(signMask, exponentIn, coefficientIn);

                    case BID_ROUNDING_DOWN:
                        if (signMask >= 0)
                            r = MAX_VALUE;
                        break;

                    case BID_ROUNDING_TO_ZERO:
                        r = signMask | MAX_VALUE;
                        break;

                    case BID_ROUNDING_UP:
                        if (signMask < 0)
                            r = MIN_VALUE;
                }
                return r;
            }
        }

        mask = 1L << EXPONENT_SHIFT_SMALL;

        // Check whether coefficient fits in 10 * 5 + 3 bits.
        if (coefficient < mask) {
            r = exponent;
            r <<= EXPONENT_SHIFT_SMALL;
            return r | coefficient | signMask;
        }
        // Special format.

        // Eliminate the case coefficient == 10^16 after rounding.
        if (coefficient == 10000000000000000L) {
            r = exponent + 1L; // TODO: optimize, including the line above!
            r <<= EXPONENT_SHIFT_SMALL;
            return r | 1000000000000000L | signMask;
        }

        r = exponent;
        r <<= EXPONENT_SHIFT_LARGE;
        r |= (signMask | MASK_SPECIAL);

        // Add coefficient, without leading bits.
        mask = (mask >>> 2) - 1;
        coefficient &= mask;
        r |= coefficient;

        return r;
    }

    /**
     * pack value where exponent is within allowable range and coefficient does not require extended format
     *
     * @param signMask
     * @param exponent
     * @param coefficient
     * @return
     */
    public static long packBasic(final long signMask, final int exponent, final long coefficient) {
        assert (0 == (~Long.MIN_VALUE & signMask));
        assert (exponent >= 0);
        assert (exponent <= BIASED_EXPONENT_MAX_VALUE);
        assert (coefficient <= SMALL_COEFFICIENT_MASK);
        assert (coefficient >= 0);

        return signMask + ((long) exponent << EXPONENT_SHIFT_SMALL) + coefficient;
    }

    public static long round(final long value, final int n, final RoundingMode roundType) {
        if (isNonFinite(value))
            return value;
        if (n > JavaImpl.MAX_EXPONENT)
            return value;
        if (n < JavaImpl.MIN_EXPONENT)
            return JavaImpl.ZERO;

//        final Decimal64Parts parts = tlsDecimal64Parts.get();
//        JavaImpl.toParts(value, parts);
        long partsCoefficient;
        long partsSignMask;
        int partsExponent;
        { // Copy-paste the toParts method for speedup
            partsSignMask = value & MASK_SIGN;

            if (isSpecial(value)) {
//                if (isNonFinite(value)) {
//                    partsExponent = 0;
//
//                    partsCoefficient = value & 0xFE03_FFFF_FFFF_FFFFL;
//                    if (UnsignedLong.isGreater(value & 0x0003_FFFF_FFFF_FFFFL, MAX_COEFFICIENT))
//                        partsCoefficient = value & ~MASK_COEFFICIENT;
//                    if (isInfinity(value))
//                        partsCoefficient = value & MASK_SIGN_INFINITY_NAN; // TODO: Why this was done??
//                } else
                {
                    // Check for non-canonical values.
                    final long coefficient = (value & LARGE_COEFFICIENT_MASK) | LARGE_COEFFICIENT_HIGH_BIT;
                    partsCoefficient = UnsignedLong.isGreater(coefficient, MAX_COEFFICIENT) ? 0 : coefficient;

                    // Extract exponent.
                    final long tmp = value >> EXPONENT_SHIFT_LARGE;
                    partsExponent = (int) (tmp & EXPONENT_MASK);
                }
            } else {

                // Extract exponent. Maximum biased value for "small exponent" is 0x2FF(*2=0x5FE), signed: []
                // upper 1/4 of the mask range is "special", as checked in the code above
                final long tmp = value >> EXPONENT_SHIFT_SMALL;
                partsExponent = (int) (tmp & EXPONENT_MASK);

                // Extract coefficient.
                partsCoefficient = (value & SMALL_COEFFICIENT_MASK);
            }
        }

        if (partsCoefficient == 0)
            return Decimal64Utils.ZERO;

        final int exponent = partsExponent - JavaImpl.EXPONENT_BIAS + n;

        if (exponent >= 0) // value is already rounded
            return value;
        // All next - negative exponent case

        final long divFactor;
        int addExponent = 0;
        { // Truncate all digits except last one
            int absPower = -exponent;
            if (absPower >= MAX_FORMAT_DIGITS) {
                divFactor = MAX_COEFFICIENT + 1;
                final int expShift = MAX_FORMAT_DIGITS;
                addExponent = absPower - expShift;

            } else {
                divFactor = POWERS_OF_TEN[absPower];
            }
        }

        // Process last digit
        switch (roundType) {
            case UP:
                partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor - 1) / divFactor) * divFactor : divFactor;
                break;

            case DOWN:
                partsCoefficient = addExponent == 0 ? (partsCoefficient / divFactor) * divFactor : 0;
                break;

            case CEILING:
                if (partsSignMask >= 0/*!parts.isNegative()*/)
                    partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor - 1) / divFactor) * divFactor : divFactor;
                else
                    partsCoefficient = addExponent == 0 ? (partsCoefficient / divFactor) * divFactor : 0;
                break;

            case FLOOR:
                if (partsSignMask >= 0/*!parts.isNegative()*/)
                    partsCoefficient = addExponent == 0 ? (partsCoefficient / divFactor) * divFactor : 0;
                else
                    partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor - 1) / divFactor) * divFactor : divFactor;
                break;

            case HALF_UP:
                partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor / 2) / divFactor) * divFactor : 0;
                break;

            case HALF_DOWN:
                partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor / 2 - 1) / divFactor) * divFactor : 0;
                break;

            case HALF_EVEN:
                partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor / 2 - 1 + ((partsCoefficient / divFactor) & 1L)) / divFactor) * divFactor : 0;
                break;

            case UNNECESSARY:
                if (!isRoundedImpl(addExponent, partsCoefficient, divFactor))
                    throw new ArithmeticException("Rounding necessary");
                return value;

            default:
                throw new IllegalArgumentException("Unsupported roundType(=" + roundType + ") value.");
        }
        partsExponent += addExponent;
        if (partsCoefficient == 0)
            return JavaImpl.ZERO;

        return pack(partsSignMask, partsExponent, partsCoefficient, BID_ROUNDING_TO_NEAREST); // JavaImpl.fromParts(parts)
    }

    public static boolean isRounded(final long value, final int n) {
        if (isNonFinite(value))
            return false;
        if (n > JavaImpl.MAX_EXPONENT)
            return true;
//        if (n < JavaImpl.MIN_EXPONENT)
//            return JavaImpl.ZERO;

//        final Decimal64Parts parts = tlsDecimal64Parts.get();
//        JavaImpl.toParts(value, parts);
        long partsCoefficient;
//        long partsSignMask; // No need sing check
        int partsExponent;
        { // Copy-paste the toParts method for speedup
//            partsSignMask = value & MASK_SIGN;

            if (isSpecial(value)) {
//                if (isNonFinite(value)) {
//                    partsExponent = 0;
//
//                    partsCoefficient = value & 0xFE03_FFFF_FFFF_FFFFL;
//                    if (UnsignedLong.isGreater(value & 0x0003_FFFF_FFFF_FFFFL, MAX_COEFFICIENT))
//                        partsCoefficient = value & ~MASK_COEFFICIENT;
//                    if (isInfinity(value))
//                        partsCoefficient = value & MASK_SIGN_INFINITY_NAN; // TODO: Why this was done??
//                } else
                {
                    // Check for non-canonical values.
                    final long coefficient = (value & LARGE_COEFFICIENT_MASK) | LARGE_COEFFICIENT_HIGH_BIT;
                    if (coefficient > MAX_COEFFICIENT) // So, partsCoefficient=0, so Zero is rounded
                        return true;
                    partsCoefficient = coefficient;

                    // Extract exponent.
                    final long tmp = value >> EXPONENT_SHIFT_LARGE;
                    partsExponent = (int) (tmp & EXPONENT_MASK);
                }
            } else {

                // Extract exponent. Maximum biased value for "small exponent" is 0x2FF(*2=0x5FE), signed: []
                // upper 1/4 of the mask range is "special", as checked in the code above
                final long tmp = value >> EXPONENT_SHIFT_SMALL;
                partsExponent = (int) (tmp & EXPONENT_MASK);

                // Extract coefficient.
                partsCoefficient = (value & SMALL_COEFFICIENT_MASK);
            }
        }

        if (partsCoefficient == 0)
            return true;

        final int exponent = partsExponent - JavaImpl.EXPONENT_BIAS + n;

        if (exponent >= 0) // value is already rounded
            return true;
        // All next - negative exponent case

        { // Truncate all digits except last one
            int absPower = -exponent;
            if (absPower > MAX_FORMAT_DIGITS) {
                return false;

            } else {
                final long divFactor = POWERS_OF_TEN[absPower];
                return partsCoefficient % divFactor == 0;
            }
        }
    }

    public static boolean isRoundedImpl(final int addExponent, final long partsCoefficient, final long divFactor) {
        return addExponent == 0 && partsCoefficient % divFactor == 0;
    }

    public static long roundToReciprocal(final long value, final int r, final RoundingMode roundType) {
        if (r < 1)
            throw new IllegalArgumentException("The r(=" + r + ") argument must be positive.");
        if (isNonFinite(value))
            return value;
//        if (Math.log10(r) > JavaImpl.MAX_EXPONENT) // Never can happens
//            return value;
//        if (Math.log10(r) < JavaImpl.MIN_EXPONENT)
//            return JavaImpl.ZERO;

//        final Decimal64Parts parts = tlsDecimal64Parts.get();
//        JavaImpl.toParts(value, parts);
        long partsCoefficient;
        long partsSignMask;
        int partsExponent;
        { // Copy-paste the toParts method for speedup
            partsSignMask = value & MASK_SIGN;

            if (isSpecial(value)) {
//                if (isNonFinite(value)) {
//                    partsExponent = 0;
//
//                    partsCoefficient = value & 0xFE03_FFFF_FFFF_FFFFL;
//                    if (UnsignedLong.isGreater(value & 0x0003_FFFF_FFFF_FFFFL, MAX_COEFFICIENT))
//                        partsCoefficient = value & ~MASK_COEFFICIENT;
//                    if (isInfinity(value))
//                        partsCoefficient = value & MASK_SIGN_INFINITY_NAN; // TODO: Why this was done??
//                } else
                {
                    // Check for non-canonical values.
                    final long coefficient = (value & LARGE_COEFFICIENT_MASK) | LARGE_COEFFICIENT_HIGH_BIT;
                    partsCoefficient = UnsignedLong.isGreater(coefficient, MAX_COEFFICIENT) ? 0 : coefficient;

                    // Extract exponent.
                    final long tmp = value >> EXPONENT_SHIFT_LARGE;
                    partsExponent = (int) (tmp & EXPONENT_MASK);
                }
            } else {

                // Extract exponent. Maximum biased value for "small exponent" is 0x2FF(*2=0x5FE), signed: []
                // upper 1/4 of the mask range is "special", as checked in the code above
                final long tmp = value >> EXPONENT_SHIFT_SMALL;
                partsExponent = (int) (tmp & EXPONENT_MASK);

                // Extract coefficient.
                partsCoefficient = (value & SMALL_COEFFICIENT_MASK);
            }
        }

        if (partsCoefficient == 0)
            return Decimal64Utils.ZERO;

        int unbiasedExponent = partsExponent - JavaImpl.EXPONENT_BIAS;

        if (unbiasedExponent >= 0) // value is already rounded
            return value;

        // Denormalize partsCoefficient to the maximal value to get the maximal precision after final r division
        {
            final int dn = numberOfDigits(partsCoefficient);
            /*if (dn < POWERS_OF_TEN.length - 1)*/
            {

                final int expShift = (POWERS_OF_TEN.length - 1) - dn;
                partsExponent -= expShift;
                unbiasedExponent -= expShift;
                partsCoefficient *= POWERS_OF_TEN[expShift];
            }
        }


        //Multiply partsCoefficient with r
        long coefficientMulR_w0, coefficientMulR_w21;
        {
            final long l0 = (LONG_LOW_PART & partsCoefficient) * r;
            coefficientMulR_w0 = LONG_LOW_PART & l0;
            coefficientMulR_w21 = (partsCoefficient >>> 32) * r + (l0 >>> 32);
        }

        //final long divFactor;
        final int divFactor01, divFactor02, divFactor03; // divFactor = divFactor1 * divFactor2 * divFactor3
        final int addExponent;
        {
            int absPower = -unbiasedExponent;
            final int maxPower = Math.min(absPower, Math.min(3 * 9, numberOfDigits(coefficientMulR_w21) + 10 /* low part */));
//            divFactor = POWERS_OF_TEN[maxPower];
            final int factor1Power = Math.min(maxPower, 9); // Int can hold max 1_000_000_000
            divFactor01 = (int) POWERS_OF_TEN[factor1Power];
            final int factor2Power = Math.min(maxPower - factor1Power, 9);
            divFactor02 = (int) POWERS_OF_TEN[factor2Power];
            divFactor03 = (int) POWERS_OF_TEN[maxPower - factor1Power - factor2Power];
            addExponent = absPower - maxPower;
        }

        // Process last digit
        switch (roundType) {
            case UP:
                // partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor - 1) / divFactor) * divFactor : divFactor;
                if (addExponent != 0) {
                    {
                        final long divFactor12 = (long) divFactor01 * divFactor02;
                        coefficientMulR_w0 = LONG_LOW_PART & divFactor12;
                        coefficientMulR_w21 = divFactor12 >>> 32;

                        if (divFactor03 > 1) {
                            final long lowMul = coefficientMulR_w0 * divFactor03;
                            coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                            coefficientMulR_w21 = coefficientMulR_w21 * divFactor03 + (lowMul >>> 32);
                        }
                    }

                } else { // addExponent != 0
                    { // + divFactor - 1
                        long divFactor_w0, divFactor_w21;
                        {
                            final long lowPart = (long) divFactor01 * divFactor02;
                            divFactor_w0 = lowPart & LONG_LOW_PART;
                            divFactor_w21 = lowPart >>> 32;
                        }
                        if (divFactor03 > 1) {
                            final long lowMul = divFactor_w0 * divFactor03;
                            divFactor_w0 = lowMul & LONG_LOW_PART;
                            divFactor_w21 = divFactor_w21 * divFactor03 + (lowMul >>> 32);
                        }

                        { // divFactor - 1
                            final long lowPart = divFactor_w0 + 0xFFFFFFFFL;
                            divFactor_w0 = lowPart & LONG_LOW_PART;
                            divFactor_w21 = divFactor_w21 + 0xFFFFFFFFFFFFFFFFL + (lowPart >>> 32);
                        }

                        {
                            final long lowPart = coefficientMulR_w0 + divFactor_w0;
                            coefficientMulR_w0 = lowPart & LONG_LOW_PART;
                            coefficientMulR_w21 += divFactor_w21 + (lowPart >>> 32);
                        }
                    }
                    { // / divFactor
                        {
                            final long r21 = coefficientMulR_w21 % divFactor01;
                            coefficientMulR_w21 /= divFactor01;
                            coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor01;
                        }

                        if (divFactor02 > 1) {
                            final long r21 = coefficientMulR_w21 % divFactor02;
                            coefficientMulR_w21 /= divFactor02;
                            coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor02;
                        }

                        if (divFactor03 > 1) {
                            final long r21 = coefficientMulR_w21 % divFactor03;
                            coefficientMulR_w21 /= divFactor03;
                            coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor03;
                        }
                    }
                    { // * divFactor
                        {
                            final long lowMul = coefficientMulR_w0 * divFactor01;
                            coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                            coefficientMulR_w21 = coefficientMulR_w21 * divFactor01 + (lowMul >>> 32);
                        }

                        if (divFactor02 > 1) {
                            final long lowMul = coefficientMulR_w0 * divFactor02;
                            coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                            coefficientMulR_w21 = coefficientMulR_w21 * divFactor02 + (lowMul >>> 32);
                        }

                        if (divFactor03 > 1) {
                            final long lowMul = coefficientMulR_w0 * divFactor03;
                            coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                            coefficientMulR_w21 = coefficientMulR_w21 * divFactor03 + (lowMul >>> 32);
                        }
                    }
                }
                // partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor - 1) / divFactor) * divFactor : divFactor;
                break;

            case DOWN:
                // partsCoefficient = addExponent == 0 ? (partsCoefficient / divFactor) * divFactor : 0;
                if (addExponent != 0) {
                    {
                        coefficientMulR_w0 = 0;
                        coefficientMulR_w21 = 0;
                    }

                } else { // addExponent != 0
                    { // / divFactor
                        {
                            final long r21 = coefficientMulR_w21 % divFactor01;
                            coefficientMulR_w21 /= divFactor01;
                            coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor01;
                        }

                        if (divFactor02 > 1) {
                            final long r21 = coefficientMulR_w21 % divFactor02;
                            coefficientMulR_w21 /= divFactor02;
                            coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor02;
                        }

                        if (divFactor03 > 1) {
                            final long r21 = coefficientMulR_w21 % divFactor03;
                            coefficientMulR_w21 /= divFactor03;
                            coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor03;
                        }
                    }
                    { // * divFactor
                        {
                            final long lowMul = coefficientMulR_w0 * divFactor01;
                            coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                            coefficientMulR_w21 = coefficientMulR_w21 * divFactor01 + (lowMul >>> 32);
                        }

                        if (divFactor02 > 1) {
                            final long lowMul = coefficientMulR_w0 * divFactor02;
                            coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                            coefficientMulR_w21 = coefficientMulR_w21 * divFactor02 + (lowMul >>> 32);
                        }

                        if (divFactor03 > 1) {
                            final long lowMul = coefficientMulR_w0 * divFactor03;
                            coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                            coefficientMulR_w21 = coefficientMulR_w21 * divFactor03 + (lowMul >>> 32);
                        }
                    }
                }
                // partsCoefficient = addExponent == 0 ? (partsCoefficient / divFactor) * divFactor : 0;
                break;

            case CEILING:
                if (partsSignMask >= 0/*!parts.isNegative()*/) {
                    // partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor - 1) / divFactor) * divFactor : divFactor;
                    if (addExponent != 0) {
                        {
                            final long divFactor12 = (long) divFactor01 * divFactor02;
                            coefficientMulR_w0 = LONG_LOW_PART & divFactor12;
                            coefficientMulR_w21 = divFactor12 >>> 32;

                            if (divFactor03 > 1) {
                                final long lowMul = coefficientMulR_w0 * divFactor03;
                                coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                                coefficientMulR_w21 = coefficientMulR_w21 * divFactor03 + (lowMul >>> 32);
                            }
                        }

                    } else { // addExponent != 0
                        { // + divFactor - 1
                            long divFactor_w0, divFactor_w21;
                            {
                                final long lowPart = (long) divFactor01 * divFactor02;
                                divFactor_w0 = lowPart & LONG_LOW_PART;
                                divFactor_w21 = lowPart >>> 32;
                            }
                            if (divFactor03 > 1) {
                                final long lowMul = divFactor_w0 * divFactor03;
                                divFactor_w0 = lowMul & LONG_LOW_PART;
                                divFactor_w21 = divFactor_w21 * divFactor03 + (lowMul >>> 32);
                            }

                            { // divFactor - 1
                                final long lowPart = divFactor_w0 + 0xFFFFFFFFL;
                                divFactor_w0 = lowPart & LONG_LOW_PART;
                                divFactor_w21 = divFactor_w21 + 0xFFFFFFFFFFFFFFFFL + (lowPart >>> 32);
                            }

                            {
                                final long lowPart = coefficientMulR_w0 + divFactor_w0;
                                coefficientMulR_w0 = lowPart & LONG_LOW_PART;
                                coefficientMulR_w21 += divFactor_w21 + (lowPart >>> 32);
                            }
                        }
                        { // / divFactor
                            {
                                final long r21 = coefficientMulR_w21 % divFactor01;
                                coefficientMulR_w21 /= divFactor01;
                                coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor01;
                            }

                            if (divFactor02 > 1) {
                                final long r21 = coefficientMulR_w21 % divFactor02;
                                coefficientMulR_w21 /= divFactor02;
                                coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor02;
                            }

                            if (divFactor03 > 1) {
                                final long r21 = coefficientMulR_w21 % divFactor03;
                                coefficientMulR_w21 /= divFactor03;
                                coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor03;
                            }
                        }
                        { // * divFactor
                            {
                                final long lowMul = coefficientMulR_w0 * divFactor01;
                                coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                                coefficientMulR_w21 = coefficientMulR_w21 * divFactor01 + (lowMul >>> 32);
                            }

                            if (divFactor02 > 1) {
                                final long lowMul = coefficientMulR_w0 * divFactor02;
                                coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                                coefficientMulR_w21 = coefficientMulR_w21 * divFactor02 + (lowMul >>> 32);
                            }

                            if (divFactor03 > 1) {
                                final long lowMul = coefficientMulR_w0 * divFactor03;
                                coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                                coefficientMulR_w21 = coefficientMulR_w21 * divFactor03 + (lowMul >>> 32);
                            }
                        }
                    }
                    // partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor - 1) / divFactor) * divFactor : divFactor;

                } else { // partsSignMask >= 0
                    // partsCoefficient = addExponent == 0 ? (partsCoefficient / divFactor) * divFactor : 0;
                    if (addExponent != 0) {
                        {
                            coefficientMulR_w0 = 0;
                            coefficientMulR_w21 = 0;
                        }

                    } else { // addExponent != 0
                        { // / divFactor
                            {
                                final long r21 = coefficientMulR_w21 % divFactor01;
                                coefficientMulR_w21 /= divFactor01;
                                coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor01;
                            }

                            if (divFactor02 > 1) {
                                final long r21 = coefficientMulR_w21 % divFactor02;
                                coefficientMulR_w21 /= divFactor02;
                                coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor02;
                            }

                            if (divFactor03 > 1) {
                                final long r21 = coefficientMulR_w21 % divFactor03;
                                coefficientMulR_w21 /= divFactor03;
                                coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor03;
                            }
                        }
                        { // * divFactor
                            {
                                final long lowMul = coefficientMulR_w0 * divFactor01;
                                coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                                coefficientMulR_w21 = coefficientMulR_w21 * divFactor01 + (lowMul >>> 32);
                            }

                            if (divFactor02 > 1) {
                                final long lowMul = coefficientMulR_w0 * divFactor02;
                                coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                                coefficientMulR_w21 = coefficientMulR_w21 * divFactor02 + (lowMul >>> 32);
                            }

                            if (divFactor03 > 1) {
                                final long lowMul = coefficientMulR_w0 * divFactor03;
                                coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                                coefficientMulR_w21 = coefficientMulR_w21 * divFactor03 + (lowMul >>> 32);
                            }
                        }
                    }
                    // partsCoefficient = addExponent == 0 ? (partsCoefficient / divFactor) * divFactor : 0;
                }
                break;

            case FLOOR:
                if (partsSignMask >= 0/*!parts.isNegative()*/) {
                    // partsCoefficient = addExponent == 0 ? (partsCoefficient / divFactor) * divFactor : 0;
                    if (addExponent != 0) {
                        {
                            coefficientMulR_w0 = 0;
                            coefficientMulR_w21 = 0;
                        }

                    } else { // addExponent != 0
                        { // / divFactor
                            {
                                final long r21 = coefficientMulR_w21 % divFactor01;
                                coefficientMulR_w21 /= divFactor01;
                                coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor01;
                            }

                            if (divFactor02 > 1) {
                                final long r21 = coefficientMulR_w21 % divFactor02;
                                coefficientMulR_w21 /= divFactor02;
                                coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor02;
                            }

                            if (divFactor03 > 1) {
                                final long r21 = coefficientMulR_w21 % divFactor03;
                                coefficientMulR_w21 /= divFactor03;
                                coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor03;
                            }
                        }
                        { // * divFactor
                            {
                                final long lowMul = coefficientMulR_w0 * divFactor01;
                                coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                                coefficientMulR_w21 = coefficientMulR_w21 * divFactor01 + (lowMul >>> 32);
                            }

                            if (divFactor02 > 1) {
                                final long lowMul = coefficientMulR_w0 * divFactor02;
                                coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                                coefficientMulR_w21 = coefficientMulR_w21 * divFactor02 + (lowMul >>> 32);
                            }

                            if (divFactor03 > 1) {
                                final long lowMul = coefficientMulR_w0 * divFactor03;
                                coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                                coefficientMulR_w21 = coefficientMulR_w21 * divFactor03 + (lowMul >>> 32);
                            }
                        }
                    }
                    // partsCoefficient = addExponent == 0 ? (partsCoefficient / divFactor) * divFactor : 0;

                } else { // partsSignMask >= 0
                    // partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor - 1) / divFactor) * divFactor : divFactor;
                    if (addExponent != 0) {
                        {
                            final long divFactor12 = (long) divFactor01 * divFactor02;
                            coefficientMulR_w0 = LONG_LOW_PART & divFactor12;
                            coefficientMulR_w21 = divFactor12 >>> 32;

                            if (divFactor03 > 1) {
                                final long lowMul = coefficientMulR_w0 * divFactor03;
                                coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                                coefficientMulR_w21 = coefficientMulR_w21 * divFactor03 + (lowMul >>> 32);
                            }
                        }

                    } else { // addExponent != 0
                        { // + divFactor - 1
                            long divFactor_w0, divFactor_w21;
                            {
                                final long lowPart = (long) divFactor01 * divFactor02;
                                divFactor_w0 = lowPart & LONG_LOW_PART;
                                divFactor_w21 = lowPart >>> 32;
                            }
                            if (divFactor03 > 1) {
                                final long lowMul = divFactor_w0 * divFactor03;
                                divFactor_w0 = lowMul & LONG_LOW_PART;
                                divFactor_w21 = divFactor_w21 * divFactor03 + (lowMul >>> 32);
                            }

                            { // divFactor - 1
                                final long lowPart = divFactor_w0 + 0xFFFFFFFFL;
                                divFactor_w0 = lowPart & LONG_LOW_PART;
                                divFactor_w21 = divFactor_w21 + 0xFFFFFFFFFFFFFFFFL + (lowPart >>> 32);
                            }

                            {
                                final long lowPart = coefficientMulR_w0 + divFactor_w0;
                                coefficientMulR_w0 = lowPart & LONG_LOW_PART;
                                coefficientMulR_w21 += divFactor_w21 + (lowPart >>> 32);
                            }
                        }
                        { // / divFactor
                            {
                                final long r21 = coefficientMulR_w21 % divFactor01;
                                coefficientMulR_w21 /= divFactor01;
                                coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor01;
                            }

                            if (divFactor02 > 1) {
                                final long r21 = coefficientMulR_w21 % divFactor02;
                                coefficientMulR_w21 /= divFactor02;
                                coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor02;
                            }

                            if (divFactor03 > 1) {
                                final long r21 = coefficientMulR_w21 % divFactor03;
                                coefficientMulR_w21 /= divFactor03;
                                coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor03;
                            }
                        }
                        { // * divFactor
                            {
                                final long lowMul = coefficientMulR_w0 * divFactor01;
                                coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                                coefficientMulR_w21 = coefficientMulR_w21 * divFactor01 + (lowMul >>> 32);
                            }

                            if (divFactor02 > 1) {
                                final long lowMul = coefficientMulR_w0 * divFactor02;
                                coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                                coefficientMulR_w21 = coefficientMulR_w21 * divFactor02 + (lowMul >>> 32);
                            }

                            if (divFactor03 > 1) {
                                final long lowMul = coefficientMulR_w0 * divFactor03;
                                coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                                coefficientMulR_w21 = coefficientMulR_w21 * divFactor03 + (lowMul >>> 32);
                            }
                        }
                    }
                    // partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor - 1) / divFactor) * divFactor : divFactor;
                }
                break;

            case HALF_UP:
                // partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor / 2) / divFactor) * divFactor : 0;
                if (addExponent != 0) {
                    {
                        coefficientMulR_w0 = 0;
                        coefficientMulR_w21 = 0;
                    }

                } else { // addExponent != 0
                    { // + divFactor / 2
                        long divFactor_w0, divFactor_w21;
                        {
                            final long lowPart = (long) divFactor01 * divFactor02;
                            divFactor_w0 = lowPart & LONG_LOW_PART;
                            divFactor_w21 = lowPart >>> 32;
                        }
                        if (divFactor03 > 1) {
                            final long lowMul = divFactor_w0 * divFactor03;
                            divFactor_w0 = lowMul & LONG_LOW_PART;
                            divFactor_w21 = divFactor_w21 * divFactor03 + (lowMul >>> 32);
                        }

                        { // divFactor / 2
                            divFactor_w0 = ((divFactor_w21 & 1) << 31) | (divFactor_w0 >>> 1);
                            divFactor_w21 = divFactor_w21 >>> 1;
                        }

                        {
                            final long lowPart = coefficientMulR_w0 + divFactor_w0;
                            coefficientMulR_w0 = lowPart & LONG_LOW_PART;
                            coefficientMulR_w21 += divFactor_w21 + (lowPart >>> 32);
                        }
                    }
                    { // / divFactor
                        {
                            final long r21 = coefficientMulR_w21 % divFactor01;
                            coefficientMulR_w21 /= divFactor01;
                            coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor01;
                        }

                        if (divFactor02 > 1) {
                            final long r21 = coefficientMulR_w21 % divFactor02;
                            coefficientMulR_w21 /= divFactor02;
                            coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor02;
                        }

                        if (divFactor03 > 1) {
                            final long r21 = coefficientMulR_w21 % divFactor03;
                            coefficientMulR_w21 /= divFactor03;
                            coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor03;
                        }
                    }
                    { // * divFactor
                        {
                            final long lowMul = coefficientMulR_w0 * divFactor01;
                            coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                            coefficientMulR_w21 = coefficientMulR_w21 * divFactor01 + (lowMul >>> 32);
                        }

                        if (divFactor02 > 1) {
                            final long lowMul = coefficientMulR_w0 * divFactor02;
                            coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                            coefficientMulR_w21 = coefficientMulR_w21 * divFactor02 + (lowMul >>> 32);
                        }

                        if (divFactor03 > 1) {
                            final long lowMul = coefficientMulR_w0 * divFactor03;
                            coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                            coefficientMulR_w21 = coefficientMulR_w21 * divFactor03 + (lowMul >>> 32);
                        }
                    }
                }
                // partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor / 2) / divFactor) * divFactor : 0;
                break;

            case HALF_DOWN:
                // partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor / 2 - 1) / divFactor) * divFactor : 0;
                if (addExponent != 0) {
                    {
                        coefficientMulR_w0 = 0;
                        coefficientMulR_w21 = 0;
                    }

                } else { // addExponent != 0
                    { // + divFactor / 2
                        long divFactor_w0, divFactor_w21;
                        {
                            final long lowPart = (long) divFactor01 * divFactor02;
                            divFactor_w0 = lowPart & LONG_LOW_PART;
                            divFactor_w21 = lowPart >>> 32;
                        }
                        if (divFactor03 > 1) {
                            final long lowMul = divFactor_w0 * divFactor03;
                            divFactor_w0 = lowMul & LONG_LOW_PART;
                            divFactor_w21 = divFactor_w21 * divFactor03 + (lowMul >>> 32);
                        }

                        { // divFactor / 2
                            divFactor_w0 = ((divFactor_w21 & 1) << 31) | (divFactor_w0 >>> 1);
                            divFactor_w21 = divFactor_w21 >>> 1;
                        }

                        { // divFactor - 1
                            final long lowPart = divFactor_w0 + 0xFFFFFFFFL;
                            divFactor_w0 = lowPart & LONG_LOW_PART;
                            divFactor_w21 = divFactor_w21 + 0xFFFFFFFFFFFFFFFFL + (lowPart >>> 32);
                        }

                        {
                            final long lowPart = coefficientMulR_w0 + divFactor_w0;
                            coefficientMulR_w0 = lowPart & LONG_LOW_PART;
                            coefficientMulR_w21 += divFactor_w21 + (lowPart >>> 32);
                        }
                    }
                    { // / divFactor
                        {
                            final long r21 = coefficientMulR_w21 % divFactor01;
                            coefficientMulR_w21 /= divFactor01;
                            coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor01;
                        }

                        if (divFactor02 > 1) {
                            final long r21 = coefficientMulR_w21 % divFactor02;
                            coefficientMulR_w21 /= divFactor02;
                            coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor02;
                        }

                        if (divFactor03 > 1) {
                            final long r21 = coefficientMulR_w21 % divFactor03;
                            coefficientMulR_w21 /= divFactor03;
                            coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor03;
                        }
                    }
                    { // * divFactor
                        {
                            final long lowMul = coefficientMulR_w0 * divFactor01;
                            coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                            coefficientMulR_w21 = coefficientMulR_w21 * divFactor01 + (lowMul >>> 32);
                        }

                        if (divFactor02 > 1) {
                            final long lowMul = coefficientMulR_w0 * divFactor02;
                            coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                            coefficientMulR_w21 = coefficientMulR_w21 * divFactor02 + (lowMul >>> 32);
                        }

                        if (divFactor03 > 1) {
                            final long lowMul = coefficientMulR_w0 * divFactor03;
                            coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                            coefficientMulR_w21 = coefficientMulR_w21 * divFactor03 + (lowMul >>> 32);
                        }
                    }
                }
                // partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor / 2 - 1) / divFactor) * divFactor : 0;
                break;

            case HALF_EVEN:
                // partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor / 2 - 1 + ((partsCoefficient / divFactor) & 1L)) / divFactor) * divFactor : 0;
                if (addExponent != 0) {
                    {
                        coefficientMulR_w0 = 0;
                        coefficientMulR_w21 = 0;
                    }

                } else { // addExponent != 0
                    { // + divFactor / 2
                        long divFactor_w0, divFactor_w21;
                        {
                            final long lowPart = (long) divFactor01 * divFactor02;
                            divFactor_w0 = lowPart & LONG_LOW_PART;
                            divFactor_w21 = lowPart >>> 32;
                        }
                        if (divFactor03 > 1) {
                            final long lowMul = divFactor_w0 * divFactor03;
                            divFactor_w0 = lowMul & LONG_LOW_PART;
                            divFactor_w21 = divFactor_w21 * divFactor03 + (lowMul >>> 32);
                        }

                        { // divFactor / 2
                            divFactor_w0 = ((divFactor_w21 & 1) << 31) | (divFactor_w0 >>> 1);
                            divFactor_w21 = divFactor_w21 >>> 1;
                        }

                        final boolean divisionLatestBit;
                        { // ((partsCoefficient / divFactor) & 1L)
                            long tmpCoefficientMulR_w0 = coefficientMulR_w0;
                            long tmpCoefficientMulR_w21 = coefficientMulR_w21;
                            { // / divFactor
                                {
                                    final long r21 = tmpCoefficientMulR_w21 % divFactor01;
                                    tmpCoefficientMulR_w21 /= divFactor01;
                                    tmpCoefficientMulR_w0 = ((r21 << 32) | tmpCoefficientMulR_w0) / divFactor01;
                                }

                                if (divFactor02 > 1) {
                                    final long r21 = tmpCoefficientMulR_w21 % divFactor02;
                                    tmpCoefficientMulR_w21 /= divFactor02;
                                    tmpCoefficientMulR_w0 = ((r21 << 32) | tmpCoefficientMulR_w0) / divFactor02;
                                }

                                if (divFactor03 > 1) {
                                    final long r21 = tmpCoefficientMulR_w21 % divFactor03;
                                    // tmpCoefficientMulR_w21 /= divFactor03; // No need high words
                                    tmpCoefficientMulR_w0 = ((r21 << 32) | tmpCoefficientMulR_w0) / divFactor03;
                                }
                            }

                            divisionLatestBit = (tmpCoefficientMulR_w0 & 1) != 0;
                        }

                        if (!divisionLatestBit) { // divFactor - 1
                            final long lowPart = divFactor_w0 + 0xFFFFFFFFL;
                            divFactor_w0 = lowPart & LONG_LOW_PART;
                            divFactor_w21 = divFactor_w21 + 0xFFFFFFFFFFFFFFFFL + (lowPart >>> 32);
                        }

                        {
                            final long lowPart = coefficientMulR_w0 + divFactor_w0;
                            coefficientMulR_w0 = lowPart & LONG_LOW_PART;
                            coefficientMulR_w21 += divFactor_w21 + (lowPart >>> 32);
                        }
                    }
                    { // / divFactor
                        {
                            final long r21 = coefficientMulR_w21 % divFactor01;
                            coefficientMulR_w21 /= divFactor01;
                            coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor01;
                        }

                        if (divFactor02 > 1) {
                            final long r21 = coefficientMulR_w21 % divFactor02;
                            coefficientMulR_w21 /= divFactor02;
                            coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor02;
                        }

                        if (divFactor03 > 1) {
                            final long r21 = coefficientMulR_w21 % divFactor03;
                            coefficientMulR_w21 /= divFactor03;
                            coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor03;
                        }
                    }
                    { // * divFactor
                        {
                            final long lowMul = coefficientMulR_w0 * divFactor01;
                            coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                            coefficientMulR_w21 = coefficientMulR_w21 * divFactor01 + (lowMul >>> 32);
                        }

                        if (divFactor02 > 1) {
                            final long lowMul = coefficientMulR_w0 * divFactor02;
                            coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                            coefficientMulR_w21 = coefficientMulR_w21 * divFactor02 + (lowMul >>> 32);
                        }

                        if (divFactor03 > 1) {
                            final long lowMul = coefficientMulR_w0 * divFactor03;
                            coefficientMulR_w0 = lowMul & LONG_LOW_PART;
                            coefficientMulR_w21 = coefficientMulR_w21 * divFactor03 + (lowMul >>> 32);
                        }
                    }
                }
                // partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor / 2 - 1 + ((partsCoefficient / divFactor) & 1L)) / divFactor) * divFactor : 0;
                break;

            case UNNECESSARY:
                if (!isRoundedToReciprocalImpl(
                    addExponent, coefficientMulR_w21, coefficientMulR_w0,
                    divFactor01, divFactor02, divFactor03))
                    throw new ArithmeticException("Rounding necessary");

                return value;

            default:
                throw new IllegalArgumentException("Unsupported roundType(=" + roundType + ") value.");
        }

        { // / r
            final long r21 = coefficientMulR_w21 % r;
            coefficientMulR_w21 /= r;
            coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / r;

            if (coefficientMulR_w21 > Integer.MAX_VALUE) {
                final int dn = numberOfDigits(coefficientMulR_w21 / Integer.MAX_VALUE);
                final int f = (int) POWERS_OF_TEN[dn];

                partsExponent += dn;

                {
                    final long f21 = coefficientMulR_w21 % f;
                    coefficientMulR_w21 /= f;
                    coefficientMulR_w0 = ((f21 << 32) | coefficientMulR_w0) / f;
                }
            }

            partsCoefficient = ((LONG_LOW_PART & coefficientMulR_w21) << 32) + coefficientMulR_w0;
        }

        partsExponent += addExponent;
        if (partsCoefficient == 0)
            return JavaImpl.ZERO;

        return pack(partsSignMask, partsExponent, partsCoefficient, BID_ROUNDING_TO_NEAREST); // JavaImpl.fromParts(parts)
    }

    public static boolean isRoundedToReciprocal(final long value, final int r) {
        if (r < 1)
            throw new IllegalArgumentException("The r(=" + r + ") argument must be positive.");
        if (isNonFinite(value))
            return false;
//        if (Math.log10(r) > JavaImpl.MAX_EXPONENT) // Never can happens
//            return value;
//        if (Math.log10(r) < JavaImpl.MIN_EXPONENT)
//            return JavaImpl.ZERO;

//        final Decimal64Parts parts = tlsDecimal64Parts.get();
//        JavaImpl.toParts(value, parts);
        long partsCoefficient;
        long partsSignMask;
        int partsExponent;
        { // Copy-paste the toParts method for speedup
            partsSignMask = value & MASK_SIGN;

            if (isSpecial(value)) {
//                if (isNonFinite(value)) {
//                    partsExponent = 0;
//
//                    partsCoefficient = value & 0xFE03_FFFF_FFFF_FFFFL;
//                    if (UnsignedLong.isGreater(value & 0x0003_FFFF_FFFF_FFFFL, MAX_COEFFICIENT))
//                        partsCoefficient = value & ~MASK_COEFFICIENT;
//                    if (isInfinity(value))
//                        partsCoefficient = value & MASK_SIGN_INFINITY_NAN; // TODO: Why this was done??
//                } else
                {
                    // Check for non-canonical values.
                    final long coefficient = (value & LARGE_COEFFICIENT_MASK) | LARGE_COEFFICIENT_HIGH_BIT;
                    partsCoefficient = UnsignedLong.isGreater(coefficient, MAX_COEFFICIENT) ? 0 : coefficient;

                    // Extract exponent.
                    final long tmp = value >> EXPONENT_SHIFT_LARGE;
                    partsExponent = (int) (tmp & EXPONENT_MASK);
                }
            } else {

                // Extract exponent. Maximum biased value for "small exponent" is 0x2FF(*2=0x5FE), signed: []
                // upper 1/4 of the mask range is "special", as checked in the code above
                final long tmp = value >> EXPONENT_SHIFT_SMALL;
                partsExponent = (int) (tmp & EXPONENT_MASK);

                // Extract coefficient.
                partsCoefficient = (value & SMALL_COEFFICIENT_MASK);
            }
        }

        if (partsCoefficient == 0)
            return true;

        int unbiasedExponent = partsExponent - JavaImpl.EXPONENT_BIAS;

        if (unbiasedExponent >= 0) // value is already rounded
            return true;

        // Denormalize partsCoefficient to the maximal value to get the maximal precision after final r division
        {
            final int dn = numberOfDigits(partsCoefficient);
            /*if (dn < POWERS_OF_TEN.length - 1)*/
            {

                final int expShift = (POWERS_OF_TEN.length - 1) - dn;
                partsExponent -= expShift;
                unbiasedExponent -= expShift;
                partsCoefficient *= POWERS_OF_TEN[expShift];
            }
        }


        //Multiply partsCoefficient with r
        long coefficientMulR_w0, coefficientMulR_w21;
        {
            final long l0 = (LONG_LOW_PART & partsCoefficient) * r;
            coefficientMulR_w0 = LONG_LOW_PART & l0;
            coefficientMulR_w21 = (partsCoefficient >>> 32) * r + (l0 >>> 32);
        }

        //final long divFactor;
        final int divFactor01, divFactor02, divFactor03; // divFactor = divFactor1 * divFactor2 * divFactor3
        final int addExponent;
        {
            int absPower = -unbiasedExponent;
            final int maxPower = Math.min(absPower, Math.min(3 * 9, numberOfDigits(coefficientMulR_w21) + 10 /* low part */));
//            divFactor = POWERS_OF_TEN[maxPower];
            final int factor1Power = Math.min(maxPower, 9); // Int can hold max 1_000_000_000
            divFactor01 = (int) POWERS_OF_TEN[factor1Power];
            final int factor2Power = Math.min(maxPower - factor1Power, 9);
            divFactor02 = (int) POWERS_OF_TEN[factor2Power];
            divFactor03 = (int) POWERS_OF_TEN[maxPower - factor1Power - factor2Power];
            addExponent = absPower - maxPower;
        }

        return isRoundedToReciprocalImpl(addExponent, coefficientMulR_w21, coefficientMulR_w0,
            divFactor01, divFactor02, divFactor03);
    }

    static boolean isRoundedToReciprocalImpl(final int addExponent,
                                             long coefficientMulR_w21,
                                             long coefficientMulR_w0,
                                             final int divFactor01,
                                             final int divFactor02,
                                             final int divFactor03) {
        if (addExponent != 0 /*&& partsCoefficient != 0 - always true: checked earlier*/)
            return false;

        { // if (partsCoefficient % divFactor != 0) throw new ArithmeticException("Rounding necessary");
            {
                final long r21 = coefficientMulR_w21 % divFactor01;
                final long l = ((r21 << 32) | coefficientMulR_w0);
                if (l % divFactor01 != 0)
                    return false;
                coefficientMulR_w0 = l / divFactor01;
                coefficientMulR_w21 /= divFactor01;
            }

            if (divFactor02 > 1) {
                final long r21 = coefficientMulR_w21 % divFactor02;
                final long l = ((r21 << 32) | coefficientMulR_w0);
                if (l % divFactor02 != 0)
                    return false;
                coefficientMulR_w0 = l / divFactor02;
                coefficientMulR_w21 /= divFactor02;
            }

            if (divFactor03 > 1) {
                final long r21 = coefficientMulR_w21 % divFactor03;
                final long l = ((r21 << 32) | coefficientMulR_w0);
                if (l % divFactor03 != 0)
                    return false;
                // coefficientMulR_w0 = l / divFactor03; // No need division result
                // coefficientMulR_w21 /= divFactor03; // No need division result
            }
        }

        return true;
    }
}
