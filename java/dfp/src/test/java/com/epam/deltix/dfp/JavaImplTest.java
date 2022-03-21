package com.epam.deltix.dfp;

import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Random;

import static com.epam.deltix.dfp.JavaImpl.*;
import static com.epam.deltix.dfp.TestUtils.*;
import static com.epam.deltix.dfp.TestUtils.checkInMultipleThreads;
import static org.junit.Assert.*;

public class JavaImplTest {
    private final Random random = new Random();

    @Test
    public void constantNaN() {
        final long value = JavaImpl.NaN;
        assertTrue(JavaImpl.isNaN(value));
        assertFalse(JavaImpl.isFinite(value));
        assertFalse(JavaImpl.isInfinity(value));
        assertFalse(JavaImpl.isNegativeInfinity(value));
        assertFalse(JavaImpl.isPositiveInfinity(value));
    }

    @Test
    public void constantPositiveInfinity() {
        final long value = JavaImpl.POSITIVE_INFINITY;
        assertFalse(JavaImpl.signBit(value));
        assertTrue(JavaImpl.isInfinity(value));
        assertFalse(JavaImpl.isNegativeInfinity(value));
        assertTrue(JavaImpl.isPositiveInfinity(value));
        assertFalse(JavaImpl.isFinite(value));
    }

    @Test
    public void constantNegativeInfinity() {
        final long value = JavaImpl.NEGATIVE_INFINITY;
        assertTrue(JavaImpl.signBit(value));
        assertTrue(JavaImpl.isInfinity(value));
        assertFalse(JavaImpl.isPositiveInfinity(value));
        assertTrue(JavaImpl.isNegativeInfinity(value));
        assertFalse(JavaImpl.isFinite(value));
    }

    @Test
    public void testConstants() {
        // Relationships between internal representation constants
        assertTrue((JavaImpl.MASK_STEERING_BITS & JavaImpl.MASK_INFINITY_AND_NAN) == JavaImpl.MASK_STEERING_BITS);
        assertTrue((JavaImpl.MASK_STEERING_BITS | JavaImpl.MASK_INFINITY_AND_NAN) == JavaImpl.MASK_INFINITY_AND_NAN);

        assertTrue((JavaImpl.MASK_INFINITY_AND_NAN & JavaImpl.MASK_INFINITY_NAN) == JavaImpl.MASK_INFINITY_AND_NAN);
        assertTrue((JavaImpl.MASK_INFINITY_AND_NAN | JavaImpl.MASK_INFINITY_NAN) == JavaImpl.MASK_INFINITY_NAN);


        assertTrue(JavaImpl.fromUInt32(0) == JavaImpl.ZERO);
        assertTrue(JavaImpl.fromUInt32(1) == Decimal64Utils.ONE);
        assertTrue(JavaImpl.fromUInt32(2) == Decimal64Utils.TWO);

        assertTrue(JavaImpl.fromParts(new Decimal64Parts(0, EXPONENT_BIAS, 0)) == JavaImpl.ZERO);
        assertFalse(JavaImpl.fromParts(new Decimal64Parts(0, EXPONENT_BIAS - 1, 0)) == JavaImpl.ZERO);
        assertFalse(JavaImpl.fromParts(new Decimal64Parts(0, EXPONENT_BIAS, MASK_SIGN)) == JavaImpl.ZERO);

        // equals
        assertTrue(Decimal64Utils.equals(JavaImpl.fromParts(new Decimal64Parts(0, EXPONENT_BIAS, MASK_SIGN)), JavaImpl.ZERO));
        assertTrue(Decimal64Utils.equals(JavaImpl.fromParts(new Decimal64Parts(0, EXPONENT_BIAS - 1, 0)), JavaImpl.ZERO));

        assertTrue(JavaImpl.fromParts(new Decimal64Parts(0, EXPONENT_BIAS, MASK_SIGN)) == JavaImpl.negate(JavaImpl.ZERO));
        assertTrue(JavaImpl.negate(JavaImpl.fromParts(new Decimal64Parts(0, EXPONENT_BIAS, MASK_SIGN))) == JavaImpl.ZERO);

        assertTrue(JavaImpl.fromParts(new Decimal64Parts(2, EXPONENT_BIAS, 0)) == Decimal64Utils.TWO);
        assertFalse(JavaImpl.fromParts(new Decimal64Parts(20, EXPONENT_BIAS - 1, 0)) == Decimal64Utils.TWO);
        assertFalse(JavaImpl.fromParts(new Decimal64Parts(2, EXPONENT_BIAS, MASK_SIGN)) == Decimal64Utils.TWO);

        // equals
        assertFalse(Decimal64Utils.equals(JavaImpl.fromParts(new Decimal64Parts(1000, EXPONENT_BIAS, MASK_SIGN)), Decimal64Utils.THOUSAND));
        assertTrue(Decimal64Utils.equals(JavaImpl.fromParts(new Decimal64Parts(10000, EXPONENT_BIAS - 1, 0)), Decimal64Utils.THOUSAND));

        assertTrue(JavaImpl.fromParts(new Decimal64Parts(1000, EXPONENT_BIAS, MASK_SIGN)) == JavaImpl.negate(Decimal64Utils.THOUSAND));
        assertTrue(JavaImpl.negate(JavaImpl.fromParts(new Decimal64Parts(1000, EXPONENT_BIAS, MASK_SIGN))) == Decimal64Utils.THOUSAND);
    }

    @Test
    public void fromInt32() {
        assertTrue(JavaImpl.fromInt32(0) == JavaImpl.ZERO);
        assertTrue(Decimal64Utils.equals(JavaImpl.fromInt32(0), JavaImpl.ZERO));

        assertTrue(JavaImpl.fromInt32(1) == Decimal64Utils.ONE);
        assertTrue(JavaImpl.fromInt32(1000000) == Decimal64Utils.MILLION);
        assertTrue(JavaImpl.fromInt32(-1000000) == Decimal64Utils.fromDouble(-1000000.0));
    }

    @Test
    public void fromInt32Advanced() {
        final int N = 10000;
        int m = Integer.MIN_VALUE; // Test min value first
        for (int i = 0; i < N; ++i) {
            final long dfp = (i & 1) > 0 ? JavaImpl.fromInt32(m) : JavaImpl.fromInt32V2(m);
            assertEquals(Decimal64Utils.toInt(dfp), m);
            assertDecimalIdentical(NativeImpl.fromInt64(m), dfp);
            assertDecimalIdentical(NativeImpl.fromFloat64(m), dfp);
            m = random.nextInt();
        }
    }

    @Test
    public void fromUInt32() {
        assertTrue(JavaImpl.fromUInt32(0) == JavaImpl.ZERO);
        assertTrue(Decimal64Utils.equals(JavaImpl.fromUInt32(0), JavaImpl.ZERO));

        assertFalse(JavaImpl.fromParts(new Decimal64Parts()) == JavaImpl.ZERO);


        assertTrue(JavaImpl.fromUInt32(1) == Decimal64Utils.ONE);
        assertFalse(JavaImpl.fromUInt32(-1) == Decimal64Utils.ONE);
        assertTrue(JavaImpl.fromUInt32(1000000) == Decimal64Utils.MILLION);
        assertFalse(JavaImpl.fromUInt32(-1000000) == Decimal64Utils.fromDouble(-1000000.0));
    }

    @Test
    public void appendTo() throws IOException {
        final StringBuilder string = new StringBuilder();

        string.setLength(0);
        assertTrue("NaN".equals(JavaImpl.appendToRefImpl(Decimal64Utils.NaN, string).toString()));

        string.setLength(0);
        assertTrue("Infinity".equals(JavaImpl.appendToRefImpl(Decimal64Utils.POSITIVE_INFINITY, string).toString()));

        string.setLength(0);
        assertTrue("-Infinity".equals(JavaImpl.appendToRefImpl(Decimal64Utils.NEGATIVE_INFINITY, string).toString()));

        string.setLength(0);
        assertTrue("100000010000".equals(JavaImpl.appendToRefImpl(Decimal64Utils.fromDouble(10000001E+04), string).toString()));

        string.setLength(0);
        assertTrue("10000001".equals(JavaImpl.appendToRefImpl(Decimal64Utils.fromDouble(10000001), string).toString()));

        string.setLength(0);
        assertTrue("1000.0001".equals(JavaImpl.appendToRefImpl(Decimal64Utils.fromDouble(10000001E-04), string).toString()));

        string.setLength(0);
        assertTrue("9.2".equals(JavaImpl.appendToRefImpl(Decimal64Utils.fromDecimalDouble(92E-01), string).toString()));
    }

    @Test
    public void fromFixedPointFastConsts() {
        assertDecimalIdentical(Decimal64Utils.ZERO, JavaImpl.fromFixedPointFast(0, 0));
        assertDecimalIdentical(Decimal64Utils.ONE, JavaImpl.fromFixedPointFast(1, 0));
        assertDecimalIdentical(Decimal64Utils.TWO, JavaImpl.fromFixedPointFast(2, 0));
        assertDecimalIdentical(Decimal64Utils.TEN, JavaImpl.fromFixedPointFast(10, 0));
        assertDecimalEqual(Decimal64Utils.TEN, JavaImpl.fromFixedPointFast(1, -1));
        assertDecimalIdentical(Decimal64Utils.HUNDRED, JavaImpl.fromFixedPointFast(100, 0));
        assertDecimalEqual(Decimal64Utils.HUNDRED, JavaImpl.fromFixedPointFast(1, -2));
        assertDecimalEqual(Decimal64Utils.THOUSAND, JavaImpl.fromFixedPointFast(1, -3));
        assertDecimalEqual(Decimal64Utils.MILLION, JavaImpl.fromFixedPointFast(1, -6));
        assertDecimalIdentical(Decimal64Utils.ONE_TENTH, JavaImpl.fromFixedPointFast(1, 1));
        assertDecimalIdentical(Decimal64Utils.ONE_HUNDREDTH, JavaImpl.fromFixedPointFast(1, 2));

        assertDecimalIdentical(Decimal64Utils.ZERO, JavaImpl.fromFixedPointFastUnsigned(0, 0));
        assertDecimalIdentical(Decimal64Utils.ONE, JavaImpl.fromFixedPointFastUnsigned(1, 0));
        assertDecimalIdentical(Decimal64Utils.TWO, JavaImpl.fromFixedPointFastUnsigned(2, 0));
        assertDecimalIdentical(Decimal64Utils.TEN, JavaImpl.fromFixedPointFastUnsigned(10, 0));
        assertDecimalEqual(Decimal64Utils.TEN, JavaImpl.fromFixedPointFastUnsigned(1, -1));
        assertDecimalIdentical(Decimal64Utils.HUNDRED, JavaImpl.fromFixedPointFastUnsigned(100, 0));
        assertDecimalEqual(Decimal64Utils.HUNDRED, JavaImpl.fromFixedPointFastUnsigned(1, -2));
        assertDecimalEqual(Decimal64Utils.THOUSAND, JavaImpl.fromFixedPointFastUnsigned(1, -3));
        assertDecimalEqual(Decimal64Utils.MILLION, JavaImpl.fromFixedPointFastUnsigned(1, -6));
    }

    @Test
    public void fromFixedPointFast() {
        final int N = 10000;
        for (int exp = 398 - 0x2FF; exp <= 398; ++exp) {
            for (int j = 0; j < N; ++j) {
                final int mantissa = random.nextInt();
                assertDecimalEqual(NativeImpl.fromFixedPoint64(mantissa, exp), JavaImpl.fromFixedPointFast(mantissa, exp));

                if (mantissa >= 0) {
                    assertDecimalEqual(NativeImpl.fromFixedPoint64(mantissa, exp), JavaImpl.fromFixedPointFastUnsigned(mantissa, exp));
                }
            }

            assertDecimalEqual(NativeImpl.fromFixedPoint64(0, exp), JavaImpl.fromFixedPointFast(0, exp));
            assertDecimalEqual(NativeImpl.fromFixedPoint64(Integer.MIN_VALUE, exp), JavaImpl.fromFixedPointFast(Integer.MIN_VALUE, exp));
            assertDecimalEqual(NativeImpl.fromFixedPoint64(Integer.MAX_VALUE, exp), JavaImpl.fromFixedPointFast(Integer.MAX_VALUE, exp));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromFixedPointFastMin() {
        JavaImpl.fromFixedPointFast(0, 398 - 0x300);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromFixedPointFastMax() {
        JavaImpl.fromFixedPointFast(0, 399);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromFixedPointFastUMin() {
        JavaImpl.fromFixedPointFastUnsigned(0, 398 - 0x300);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromFixedPointFastUMax() {
        JavaImpl.fromFixedPointFastUnsigned(0, 399);
    }

    @SuppressWarnings("Duplicates")
    public static void checkToString(final String message, final String hex, final String expectedPlain, final String expectedScientific) {
        @Decimal final long dfp64 = UnsignedLong.parse(hex, 16);
        final String actual = Decimal64Utils.toString(dfp64);

        final String info = (message != null ? message : "") + " Hex = " + hex + ", Value = " + expectedScientific;
        assertEquals(info, expectedPlain, actual);

        @Decimal final long result1 = Decimal64Utils.parse(expectedPlain);
        assertEquals(info, expectedPlain, Decimal64Utils.toString(result1));

        @Decimal final long result2 = Decimal64Utils.parse(expectedScientific);
        assertEquals(info, expectedPlain, Decimal64Utils.toString(result2));

        @Decimal final long result3 = Decimal64Utils.tryParse(expectedPlain, Decimal64Utils.NaN);
        assertEquals(info, expectedPlain, Decimal64Utils.toString(result3));

        @Decimal final long result4 = Decimal64Utils.tryParse(expectedScientific, Decimal64Utils.NaN);
        assertEquals(info, expectedPlain, Decimal64Utils.toString(result4));
    }

    @Test
    public void toStringSpecialCase1() {
        checkToString(null, "000201905E5C7474", "0.0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000056466952345714", "+564669523457140E-398");
    }

    @Test
    public void toStringSpecialCase2() {
        checkToString(null, "31800000013474D8", "202150", "+20215000E-2");
    }

    @Test
    public void toStringSpecialCase3() {
        checkToString(null, "8020000000000000", "0", "-0E-397");
    }

    @Test
    public void toStringSpecialCase4() {
        checkToString(null, "30593A484825D4D1", "7100.956540261585", "+7100956540261585E-12");
    }

    @Test(expected = NumberFormatException.class)
    public void parseEmptyString() {
        JavaImpl.parse("asdf", 0, 0, 0);
    }

    @Test(expected = NumberFormatException.class)
    public void parseNonDigits() {
        JavaImpl.parse("asdf", 0, 4, 0);
    }

    @Test
    public void fastSignCheck() {
        for (@Decimal final long testValue : specialValues) {
            @Decimal final long negTestValue = Decimal64Utils.negate(testValue);
            checkValues(testValue, NativeImpl.isPositive(testValue), Decimal64Utils.isPositive(testValue));
            checkValues(negTestValue, NativeImpl.isPositive(negTestValue), Decimal64Utils.isPositive(negTestValue));

            checkValues(testValue, NativeImpl.isNonPositive(testValue), Decimal64Utils.isNonPositive(testValue));
            checkValues(negTestValue, NativeImpl.isNonPositive(negTestValue), Decimal64Utils.isNonPositive(negTestValue));

            checkValues(testValue, NativeImpl.isNegative(testValue), Decimal64Utils.isNegative(testValue));
            checkValues(negTestValue, NativeImpl.isNegative(negTestValue), Decimal64Utils.isNegative(negTestValue));

            checkValues(testValue, NativeImpl.isNonNegative(testValue), Decimal64Utils.isNonNegative(testValue));
            checkValues(negTestValue, NativeImpl.isNonNegative(negTestValue), Decimal64Utils.isNonNegative(negTestValue));

            checkValues(testValue, NativeImpl.isZero(testValue), Decimal64Utils.isZero(testValue));
            checkValues(negTestValue, NativeImpl.isZero(negTestValue), Decimal64Utils.isZero(negTestValue));

            checkValues(testValue, NativeImpl.isNonZero(testValue), Decimal64Utils.isNonZero(testValue));
            checkValues(negTestValue, NativeImpl.isNonZero(negTestValue), Decimal64Utils.isNonZero(negTestValue));

            if (!Decimal64Utils.isNaN(testValue)) {
                checkValues(testValue, NativeImpl.isPositive(testValue), Decimal64Utils.compareTo(testValue, Decimal64Utils.ZERO) > 0);
                checkValues(negTestValue, NativeImpl.isPositive(negTestValue), Decimal64Utils.compareTo(negTestValue, Decimal64Utils.ZERO) > 0);

                checkValues(testValue, NativeImpl.isNonPositive(testValue), Decimal64Utils.compareTo(testValue, Decimal64Utils.ZERO) <= 0);
                checkValues(negTestValue, NativeImpl.isNonPositive(negTestValue), Decimal64Utils.compareTo(negTestValue, Decimal64Utils.ZERO) <= 0);

                checkValues(testValue, NativeImpl.isNegative(testValue), Decimal64Utils.compareTo(testValue, Decimal64Utils.ZERO) < 0);
                checkValues(negTestValue, NativeImpl.isNegative(negTestValue), Decimal64Utils.compareTo(negTestValue, Decimal64Utils.ZERO) < 0);

                checkValues(testValue, NativeImpl.isNonNegative(testValue), Decimal64Utils.compareTo(testValue, Decimal64Utils.ZERO) >= 0);
                checkValues(negTestValue, NativeImpl.isNonNegative(negTestValue), Decimal64Utils.compareTo(negTestValue, Decimal64Utils.ZERO) >= 0);
            }

            checkValues(testValue, NativeImpl.isZero(testValue), Decimal64Utils.compareTo(testValue, Decimal64Utils.ZERO) == 0);
            checkValues(negTestValue, NativeImpl.isZero(negTestValue), Decimal64Utils.compareTo(negTestValue, Decimal64Utils.ZERO) == 0);

            checkValues(testValue, NativeImpl.isNonZero(testValue), Decimal64Utils.compareTo(testValue, Decimal64Utils.ZERO) != 0);
            checkValues(negTestValue, NativeImpl.isNonZero(negTestValue), Decimal64Utils.compareTo(negTestValue, Decimal64Utils.ZERO) != 0);
        }
    }

    private static void checkValues(@Decimal final long value, final boolean refCond, final boolean testCond) {
        if (refCond != testCond)
            throw new RuntimeException("TestValue(=" + Decimal64Utils.toString(value) + ") check error: refCond(=" + refCond + ") != testCond(" + testCond + ").");
    }

    private static String round(String valueIn, final int n, final RoundType roundType) {
        String value = valueIn;

        boolean isNegSign = false;
        if (value.charAt(0) == '-' || value.charAt(0) == '+') {
            isNegSign = value.charAt(0) == '-';
            value = value.substring(1);
        }

        if (value.equals("NaN") || value.equals("Infinity"))
            return valueIn;

        int latestPoint;
        {
            int dotPoint = value.indexOf('.');
            if (dotPoint < 0)
                dotPoint = value.length();
            latestPoint = dotPoint + n + (n > 0 ? 0 : -1);
            if (latestPoint >= value.length() - 1)
                return (isNegSign ? "-" : "") + value;
            if (latestPoint < 0) {
                final String zerosStr;
                {
                    final StringBuilder zeros = new StringBuilder(-latestPoint);
                    for (int i = 0; i < -latestPoint; ++i)
                        zeros.append('0');
                    zerosStr = zeros.toString();
                }
                value = zerosStr + value;
                latestPoint += zerosStr.length();
            }
        }

        {
            value = '0' + value;
            latestPoint += 1;
        }

        final String fixedPart = value.substring(0, latestPoint + 1);
        final int fixedExp;
        {
            int dotPoint = value.indexOf('.');
            if (dotPoint < 0)
                dotPoint = value.length();
            fixedExp = Math.max(0, dotPoint - 1 - latestPoint);
        }
        switch (roundType) {
            case ROUND:
                if (latestPoint + 1 >= value.length())
                    return formatMantissaExp(isNegSign, fixedPart, fixedExp);
                char nextChar = '0';
                if (latestPoint + 1 < value.length())
                    nextChar = value.charAt(latestPoint + 1);
                if (nextChar == '.')
                    nextChar = latestPoint + 2 < value.length() ? value.charAt(latestPoint + 2) : '0';
                return formatMantissaExp(isNegSign, nextChar >= '5' ? incMantissa(fixedPart) : fixedPart, fixedExp);
            case TRUNC:
                return formatMantissaExp(isNegSign, fixedPart, fixedExp);
            case FLOOR:
                if (!isNegSign)
                    return formatMantissaExp(isNegSign, fixedPart, fixedExp);
                else
                    return formatMantissaExp(isNegSign, isNonZero(value, latestPoint + 1) ? incMantissa(fixedPart) : fixedPart, fixedExp);
            case CEIL:
                if (!isNegSign)
                    return formatMantissaExp(isNegSign, isNonZero(value, latestPoint + 1) ? incMantissa(fixedPart) : fixedPart, fixedExp);
                else
                    return formatMantissaExp(isNegSign, fixedPart, fixedExp);
            default:
                throw new IllegalArgumentException("Unsupported roundType(=" + roundType + ") value.");
        }
    }

    private static String formatMantissaExp(final boolean isNegSign, String value, final int exp) {
        if (exp > 0) {
            final StringBuilder sb = new StringBuilder(exp);
            for (int i = 0; i < exp; ++i)
                sb.append('0');
            value = value + sb;
        }

        {
            int leftIndex;
            for (leftIndex = 0; leftIndex < value.length(); ++leftIndex)
                if (value.charAt(leftIndex) != '0')
                    break;
            if (leftIndex < value.length() && value.charAt(leftIndex) == '.')
                leftIndex--;
            value = value.substring(leftIndex);
        }

        {
            final int dotIndex = value.indexOf('.');
            if (dotIndex >= 0) {
                int rightIndex = value.length();
                while (rightIndex > dotIndex && (value.charAt(rightIndex - 1) == '0'))
                    --rightIndex;
                if (rightIndex - 1 == dotIndex)
                    rightIndex = dotIndex;
                value = value.substring(0, rightIndex);
            }
        }

        return value.isEmpty() || value.equals("0") ? "0" : (isNegSign ? "-" : "") + value;
    }

    private static String incMantissa(final String str) {
        char[] chars = str.toCharArray();
        int carry = 1;
        for (int ii = chars.length - 1; ii >= 0 && carry > 0; --ii) {
            if (chars[ii] == '.' || chars[ii] == '-')
                continue;
            if (chars[ii] < '0' && chars[ii] > '9')
                throw new IllegalArgumentException("Unsupported character at [" + ii + "] in string '" + str + "'.");
            final int ch = chars[ii] - '0' + carry;
            if (ch > 9) {
                chars[ii] = '0';
                carry = 1;
            } else {
                chars[ii] = (char) ('0' + ch);
                carry = 0;
            }
        }
        if (carry != 0) {
            chars = Arrays.copyOf(chars, chars.length + 1);
            final int firstDigit = chars[0] == '-' ? 1 : 0;
            System.arraycopy(chars, firstDigit, chars, firstDigit + 1, chars.length - 1 - firstDigit);
            chars[firstDigit] = '1';
        }
        return new String(chars);
    }

    private static boolean isNonZero(final String str, final int i) {
        for (int ii = i, ie = str.length(); ii < ie; ++ii) {
            final char c = str.charAt(ii);
            if (c >= '1' && c <= '9')
                return true;
        }
        return false;
    }

    @Test
    public void TestRoundRandomly() throws Exception {
        final int randomPointMaxOffset = Decimal64Utils.MAX_SIGNIFICAND_DIGITS + Decimal64Utils.MAX_SIGNIFICAND_DIGITS / 4;

        checkInMultipleThreads(() -> {
            final RandomDecimalsGenerator random = new RandomDecimalsGenerator(
                new MersenneTwister(), 1,
                Decimal64Utils.MIN_EXPONENT + randomPointMaxOffset,
                Decimal64Utils.MAX_EXPONENT - randomPointMaxOffset);

            for (int ri = 0; ri < NTests / 10 /* Test will be very slow without this division */ ; ++ri) {
                final int randomOffset = random.generator.nextInt(randomPointMaxOffset * 2 + 1) - randomPointMaxOffset;

                final long inValue = random.nextX();
                final int roundPoint = random.getXExp() + randomOffset;
                final RoundType roundType;
                switch (random.generator.nextInt(4)) {
                    case 0:
                        roundType = RoundType.ROUND;
                        break;
                    case 1:
                        roundType = RoundType.TRUNC;
                        break;
                    case 2:
                        roundType = RoundType.FLOOR;
                        break;
                    case 3:
                        roundType = RoundType.CEIL;
                        break;
                    default:
                        throw new RuntimeException("Unsupported case for round type generation.");
                }

                checkRound(inValue, -roundPoint, roundType);
            }
        });
    }

    @Test
    public void TestRoundCase() {
        for (final long testValue : specialValues)
            for (final int roundPoint : new int[]{20, 10, 5, 3, 1, 0, -1, -2, -6, -11, -19})
                for (final RoundType roundType : RoundType.values())
                    checkRound(testValue, roundPoint, roundType);

        checkRound(-5787416479386436811L, 1, RoundType.ROUND);
        checkRound(3439124486823148033L, 1, RoundType.FLOOR);
        checkRound(-1444740417884338647L, 0, RoundType.ROUND);
        checkRound(3439028411434681001L, -7, RoundType.ROUND);
        checkRound(-5778759999361643774L, 2, RoundType.ROUND);
        checkRound(3448058746773778910L, -4, RoundType.CEIL);
        checkRound(1417525816301142050L, -209, RoundType.CEIL);
        checkRound(2996092184105885832L, -61, RoundType.CEIL);
        checkRound(-922689384669825404L, -236, RoundType.FLOOR);
    }

    private static void checkRound(final long inValue, final int roundPoint, final RoundType roundType) {
        final long testValue = JavaImpl.round(inValue, roundPoint, roundType);
        final String inStr = Decimal64Utils.toString(inValue);
        final String roundStr = round(inStr, roundPoint, roundType);
        final String testStr = Decimal64Utils.toString(testValue);
        if (!roundStr.equals(testStr))
            throw new RuntimeException("Case checkRound(" + inValue + "L, " + roundPoint + ", RoundType." + roundType +
                "); error: input value (=" + inStr + ") string rounding (=" + roundStr + ") != decimal rounding (=" + testStr + ")");
    }

    @Test
    public void unCanonizedRound() {
        final long zeroU = 0x2FE0000000000000L;
        final long f = 0x2F638D7EA4C68000L; // 0.0001
        final long zeroP = Decimal64Utils.multiply(zeroU, f);

        assertDecimalEqual(Decimal64Utils.ZERO, Decimal64Utils.round(zeroP, 0, RoundType.CEIL));
    }

    @Test
    public void testToStringRandomly() throws Exception {
        checkInMultipleThreads(() -> {
            final RandomDecimalsGenerator random = new RandomDecimalsGenerator();
            for (int ri = 0; ri < NTests; ++ri)
                checkStrEq(random.nextX());
        });
    }

    private static void checkStrEq(final long value) {
        try {
            final String inStr = JavaImpl.appendToRefImpl(value, new StringBuilder()).toString();
            final String testStr = JavaImpl.fastToString(value);
            if (!inStr.equals(testStr))
                throw new RuntimeException("Case toString(" + value + "L) error: ref toString(=" + inStr + ") != test toString(=" + testStr + ")");
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testToString() {
        checkStrEq(Decimal64Utils.parse("-3220237490000000"));
        checkStrEq(Decimal64Utils.parse("-6.0123980000"));
        checkStrEq(Decimal64Utils.parse("-0.1239867"));
    }

    @Decimal
    static final long[] specialValues = {
        Decimal64Utils.fromDouble(Math.PI),
        Decimal64Utils.fromDouble(-Math.E),
        Decimal64Utils.NaN,
        Decimal64Utils.NaN | 1000000000000000L,
        Decimal64Utils.POSITIVE_INFINITY,
        Decimal64Utils.POSITIVE_INFINITY | 1000000000000000L,
        Decimal64Utils.NEGATIVE_INFINITY,
        Decimal64Utils.NEGATIVE_INFINITY | 1000000000000000L,
        Decimal64Utils.ZERO,
        JavaImplAdd.SPECIAL_ENCODING_MASK64 | 1000000000000000L,
        Decimal64Utils.fromFixedPoint(0L, -300),
        Decimal64Utils.fromFixedPoint(0L, 300),
        Decimal64Utils.ONE,
        Decimal64Utils.fromFixedPoint(10000000000000000L, 16),
        Decimal64Utils.fromLong(10000000000000000L),
    };

    @Test
    public void testAddWithCoverage() throws Exception {
        testAddCase(((long) EXPONENT_BIAS << EXPONENT_SHIFT_SMALL) | 1000000000000000L,
            MASK_SIGN | ((long) (EXPONENT_BIAS - MAX_FORMAT_DIGITS - 1) << EXPONENT_SHIFT_SMALL) | 5000000000000001L);

        for (final long x : specialValues)
            for (final long y : specialValues)
                testAddCase(x, y);

        checkInMultipleThreads(() -> {
            final RandomDecimalsGenerator random = new RandomDecimalsGenerator();
            for (int i = 0; i < NTests; ++i) {
                random.makeNextPair();
                testAddCase(random.getX(), random.getY());
            }
        });
    }

    @Test
    public void testAddCases() {
        testAddCase(0xecb08366cd530a32L, 0xb2fc7ab89d54c15dL);
        testAddCase(0x335bb3b1068d9bd8L, 0x32ee619e7226bc85L);
    }

    private void testAddCase(final long x, final long y) {
        final long javaRet = JavaImplAdd.bid64_add(x, y);
        final long nativeRet = NativeImpl.add2(x, y);

        if (javaRet != nativeRet)
            throw new RuntimeException("The decimal 0x" + Long.toHexString(x) + "L + 0x" + Long.toHexString(y) +
                "L = 0x" + Long.toHexString(nativeRet) + "L, but java return 0x" + Long.toHexString(javaRet) + "L");
    }

    @Test
    public void testMulWithCoverage() throws Exception {
        testMulCase(((long) EXPONENT_BIAS << EXPONENT_SHIFT_SMALL) | 1000000000000000L,
            MASK_SIGN | ((long) (EXPONENT_BIAS - MAX_FORMAT_DIGITS - 1) << EXPONENT_SHIFT_SMALL) | 5000000000000001L);

        for (final long x : specialValues)
            for (final long y : specialValues)
                testMulCase(x, y);

        checkInMultipleThreads(() -> {
            final RandomDecimalsGenerator random = new RandomDecimalsGenerator();
            for (int i = 0; i < NTests; ++i) {
                random.makeNextPair();
                testMulCase(random.getX(), random.getY());
            }
        });
    }

    private void testMulCase(final long x, final long y) {
        final long javaRet = JavaImplMul.bid64_mul(x, y);
        final long nativeRet = NativeImpl.multiply2(x, y);

        if (javaRet != nativeRet)
            throw new RuntimeException("The decimal 0x" + Long.toHexString(x) + "L * 0x" + Long.toHexString(y) +
                "L = 0x" + Long.toHexString(nativeRet) + "L, but java return 0x" + Long.toHexString(javaRet) + "L");
    }

    @Test
    public void testDivWithCoverage() throws Exception {
        testDivCase(((long) EXPONENT_BIAS << EXPONENT_SHIFT_SMALL) | 1000000000000000L,
            MASK_SIGN | ((long) (EXPONENT_BIAS - MAX_FORMAT_DIGITS - 1) << EXPONENT_SHIFT_SMALL) | 5000000000000001L);

        for (final long x : specialValues)
            for (final long y : specialValues)
                testDivCase(x, y);

        checkInMultipleThreads(() -> {
            final RandomDecimalsGenerator random = new RandomDecimalsGenerator();
            for (int i = 0; i < NTests; ++i) {
                random.makeNextPair();
                testDivCase(random.getX(), random.getY());
            }
        });
    }

    @Test
    public void testDivCases() {
        testDivCase(0x31a000000000000dL, 0x2e800000000006d1L);
        testDivCase(0x30A0EFABDABB1574L, 0x30A0000062DF732AL);
        testDivCase(0x31c38d7ea4c68000L, 0xafb1c37937e08001L);
    }

    private void testDivCase(final long x, final long y) {
        final long javaRet = JavaImplDiv.bid64_div(x, y);
        final long nativeRet = NativeImpl.divide(x, y);

        if (javaRet != nativeRet)
            throw new RuntimeException("The decimal 0x" + Long.toHexString(x) + "L / 0x" + Long.toHexString(y) +
                "L = 0x" + Long.toHexString(nativeRet) + "L, but java return 0x" + Long.toHexString(javaRet) + "L");
    }

    @Test
    public void testMinWithCoverage() throws Exception {
        for (final long x : specialValues)
            for (final long y : specialValues)
                testMinCase(x, y);

        checkInMultipleThreads(() -> {
            final RandomDecimalsGenerator random = new RandomDecimalsGenerator();
            for (int i = 0; i < NTests; ++i)
                testMinCase(random.nextX(), random.generator.nextDouble() < 0.1 ? random.getX() : random.nextY());
        });
    }

    private void testMinCase(final long x, final long y) {
        final long javaRet = JavaImplMinMax.bid64_min_fix_nan(x, y);
        final long nativeRet = NativeImpl.min2(x, y);

        if (javaRet != nativeRet)
            throw new RuntimeException("The minimal of decimal 0x" + Long.toHexString(x) + "L vs 0x" + Long.toHexString(y) +
                "L = 0x" + Long.toHexString(nativeRet) + "L, but java return 0x" + Long.toHexString(javaRet) + "L");
    }

    @Test
    public void testMaxWithCoverage() throws Exception {
        for (final long x : specialValues)
            for (final long y : specialValues)
                testMaxCase(x, y);

        checkInMultipleThreads(() -> {
            final RandomDecimalsGenerator random = new RandomDecimalsGenerator();
            for (int i = 0; i < NTests; ++i)
                testMaxCase(random.nextX(), random.generator.nextDouble() < 0.1 ? random.getX() : random.nextY());
        });
    }

    private void testMaxCase(final long x, final long y) {
        final long javaRet = JavaImplMinMax.bid64_max_fix_nan(x, y);
        final long nativeRet = NativeImpl.max2(x, y);

        if (javaRet != nativeRet)
            throw new RuntimeException("The maximal of decimal 0x" + Long.toHexString(x) + "L vs 0x" + Long.toHexString(y) +
                "L = 0x" + Long.toHexString(nativeRet) + "L, but java return 0x" + Long.toHexString(javaRet) + "L");
    }

    @Test
    public void testToStringScientific() throws Exception {
        checkInMultipleThreads(() -> {
            final RandomDecimalsGenerator random = new RandomDecimalsGenerator();
            for (int i = 0; i < NTests; ++i) {
                final long x = random.nextX();
                final String xs = Decimal64Utils.toScientificString(x);
                final long y = Decimal64Utils.parse(xs);

                if (!Decimal64Utils.equals(x, y))
                    throw new RuntimeException("The decimal 0x" + Long.toHexString(x) + "L = " + Decimal64Utils.toString(x) + " != " + xs + "(" + Decimal64Utils.toString(y) + ")");
            }
        });
    }

    @Test
    public void testFormatting() throws Exception {
        checkInMultipleThreads(() -> {
            final RandomDecimalsGenerator random = new RandomDecimalsGenerator();
            for (int i = 0; i < NTests; ++i)
                checkFormattingValue(random.nextX());
        });
    }

    @Test
    public void testFormattingCase() {
        checkFormattingValue(0x3420000037ffff73L);
    }

    private static void checkFormattingValue(final long x) {
        {
            final String xs = Decimal64Utils.toString(x);
            final long y = Decimal64Utils.parse(xs);
            if (!Decimal64Utils.equals(x, y))
                throw new RuntimeException("toString error: The decimal " + xs + "(0x" + Long.toHexString(x) + "L) != " + Decimal64Utils.toString(y) + "(0x" + Long.toHexString(y) + "L)");
        }

        {
            final String xs = Decimal64Utils.toScientificString(x);
            final long y = Decimal64Utils.parse(xs);
            if (!Decimal64Utils.equals(x, y))
                throw new RuntimeException("toScientificString error: The decimal " + xs + "(0x" + Long.toHexString(x) + "L) != " + Decimal64Utils.toScientificString(y) + "(0x" + Long.toHexString(y) + "L)");
        }

        {
            final String xs = Decimal64Utils.appendTo(x, new StringBuilder()).toString();
            final long y = Decimal64Utils.parse(xs);
            if (!Decimal64Utils.equals(x, y))
                throw new RuntimeException("appendTo error: The decimal " + xs + "(0x" + Long.toHexString(x) + "L) != " + Decimal64Utils.toScientificString(y) + "(0x" + Long.toHexString(y) + "L)");
        }

        {
            final String xs = Decimal64Utils.scientificAppendTo(x, new StringBuilder()).toString();
            final long y = Decimal64Utils.parse(xs);
            if (!Decimal64Utils.equals(x, y))
                throw new RuntimeException("scientificAppendTo error: The decimal " + xs + "(0x" + Long.toHexString(x) + "L) != " + Decimal64Utils.toScientificString(y) + "(0x" + Long.toHexString(y) + "L)");
        }

        try {
            {
                final String xs = Decimal64Utils.appendTo(x, (Appendable) new StringBuilder()).toString();
                final long y = Decimal64Utils.parse(xs);
                if (!Decimal64Utils.equals(x, y))
                    throw new RuntimeException("Appendable appendTo error: The decimal " + xs + "(0x" + Long.toHexString(x) + "L) != " + Decimal64Utils.toScientificString(y) + "(0x" + Long.toHexString(y) + "L)");
            }

            {
                final String xs = Decimal64Utils.scientificAppendTo(x, (Appendable) new StringBuilder()).toString();
                final long y = Decimal64Utils.parse(xs);
                if (!Decimal64Utils.equals(x, y))
                    throw new RuntimeException("Appendable scientificAppendTo error: The decimal " + xs + "(0x" + Long.toHexString(x) + "L) != " + Decimal64Utils.toScientificString(y) + "(0x" + Long.toHexString(y) + "L)");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean decimalParseOk(JavaImplParse.FloatingPointStatusFlag fpsf) {
        return (fpsf.value & JavaImplParse.BID_INVALID_FORMAT) == 0;
    }

    private static class DoubleHolder {
        public double value;
    }

    private static boolean doubleTryParse(final String str, final DoubleHolder out) {
        try {
            out.value = Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            out.value = Double.NaN;
            return false;
        }
    }

    @Test
    public void TestParseReImpl() {
        int roundMode = 10; // JavaImplParse.BID_ROUNDING_TO_NEAREST;
        {
            final String testStr = "   000   ";
            JavaImplParse.FloatingPointStatusFlag fpsf = new JavaImplParse.FloatingPointStatusFlag();
            Decimal64 value = Decimal64.fromUnderlying(JavaImplParse.bid64_from_string(testStr, 0, testStr.length(), fpsf, roundMode));
            DoubleHolder doubleHolder = new DoubleHolder();
            final boolean doubleParseOk = doubleTryParse(testStr, doubleHolder);
            assertEquals(Decimal64.ZERO, value);
            assertEquals(fpsf.value, JavaImplParse.BID_EXACT_STATUS);
            assertEquals(doubleParseOk, decimalParseOk(fpsf));
        }
        {
            final String testStr = "00..";
            JavaImplParse.FloatingPointStatusFlag fpsf = new JavaImplParse.FloatingPointStatusFlag();
            Decimal64 value = Decimal64.fromUnderlying(JavaImplParse.bid64_from_string(testStr, 0, testStr.length(), fpsf, roundMode));
            DoubleHolder doubleHolder = new DoubleHolder();
            final boolean doubleParseOk = doubleTryParse(testStr, doubleHolder);
            assertEquals(Decimal64.NaN, value);
            assertEquals(fpsf.value, JavaImplParse.BID_INVALID_FORMAT);
            assertEquals(doubleParseOk, decimalParseOk(fpsf));
        }
        {
            final String testStr = "000235";
            JavaImplParse.FloatingPointStatusFlag fpsf = new JavaImplParse.FloatingPointStatusFlag();
            Decimal64 value = Decimal64.fromUnderlying(JavaImplParse.bid64_from_string(testStr, 0, testStr.length(), fpsf, roundMode));
            DoubleHolder doubleHolder = new DoubleHolder();
            final boolean doubleParseOk = doubleTryParse(testStr, doubleHolder);
            assertEquals(Decimal64.fromInt(235), value);
            assertEquals(fpsf.value, JavaImplParse.BID_EXACT_STATUS);
            assertEquals(doubleParseOk, decimalParseOk(fpsf));
        }
        {
            final String testStr = "00.0000235";
            JavaImplParse.FloatingPointStatusFlag fpsf = new JavaImplParse.FloatingPointStatusFlag();
            Decimal64 value = Decimal64.fromUnderlying(JavaImplParse.bid64_from_string(testStr, 0, testStr.length(), fpsf, roundMode));
            DoubleHolder doubleHolder = new DoubleHolder();
            final boolean doubleParseOk = doubleTryParse(testStr, doubleHolder);
            assertEquals(Decimal64.fromFixedPoint(235, 7), value);
            assertEquals(fpsf.value, JavaImplParse.BID_EXACT_STATUS);
            assertEquals(doubleParseOk, decimalParseOk(fpsf));
        }
        {
            final String testStr = "1234512345123451234500000";
            JavaImplParse.FloatingPointStatusFlag fpsf = new JavaImplParse.FloatingPointStatusFlag();
            Decimal64 value = Decimal64.fromUnderlying(JavaImplParse.bid64_from_string(testStr, 0, testStr.length(), fpsf, roundMode));
            DoubleHolder doubleHolder = new DoubleHolder();
            final boolean doubleParseOk = doubleTryParse(testStr, doubleHolder);
            assertEquals(Decimal64.fromFixedPoint(1234512345123451L, -9), value);
            assertEquals(fpsf.value, JavaImplParse.BID_INEXACT_EXCEPTION);
            assertEquals(doubleParseOk, decimalParseOk(fpsf));
        }
        {
            final String testStr = "1234512345123451234500000e+12345123451234512345";
            JavaImplParse.FloatingPointStatusFlag fpsf = new JavaImplParse.FloatingPointStatusFlag();
            Decimal64 value = Decimal64.fromUnderlying(JavaImplParse.bid64_from_string(testStr, 0, testStr.length(), fpsf, roundMode));
            DoubleHolder doubleHolder = new DoubleHolder();
            final boolean doubleParseOk = doubleTryParse(testStr, doubleHolder);
            assertEquals(Decimal64.POSITIVE_INFINITY, value);
            assertEquals(fpsf.value, JavaImplParse.BID_INEXACT_EXCEPTION | JavaImplParse.BID_OVERFLOW_EXCEPTION);
            assertEquals(doubleParseOk, decimalParseOk(fpsf));
        }
        {
            final String testStr = "  -5000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000  ";
            JavaImplParse.FloatingPointStatusFlag fpsf = new JavaImplParse.FloatingPointStatusFlag();
            Decimal64 value = Decimal64.fromUnderlying(JavaImplParse.bid64_from_string(testStr, 0, testStr.length(), fpsf, roundMode));
            DoubleHolder doubleHolder = new DoubleHolder();
            final boolean doubleParseOk = doubleTryParse(testStr, doubleHolder);
            assertEquals(Decimal64.NEGATIVE_INFINITY, value);
            assertEquals(fpsf.value, JavaImplParse.BID_INEXACT_EXCEPTION | JavaImplParse.BID_OVERFLOW_EXCEPTION);
            assertEquals(doubleParseOk, decimalParseOk(fpsf));
        }
        {
            final String testStr = "0.00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000005";
            JavaImplParse.FloatingPointStatusFlag fpsf = new JavaImplParse.FloatingPointStatusFlag();
            Decimal64 value = Decimal64.fromUnderlying(JavaImplParse.bid64_from_string(testStr, 0, testStr.length(), fpsf, roundMode));
            DoubleHolder doubleHolder = new DoubleHolder();
            final boolean doubleParseOk = doubleTryParse(testStr, doubleHolder);
            assertEquals(Decimal64.ZERO, value);
            assertEquals(fpsf.value, JavaImplParse.BID_INEXACT_EXCEPTION | JavaImplParse.BID_UNDERFLOW_EXCEPTION);
            assertEquals(doubleParseOk, decimalParseOk(fpsf));
        }
        {
            final String testStr = "  123 x99  ";
            JavaImplParse.FloatingPointStatusFlag fpsf = new JavaImplParse.FloatingPointStatusFlag();
            Decimal64 value = Decimal64.fromUnderlying(JavaImplParse.bid64_from_string(testStr, 0, testStr.length(), fpsf, roundMode));
            DoubleHolder doubleHolder = new DoubleHolder();
            final boolean doubleParseOk = doubleTryParse(testStr, doubleHolder);
            assertEquals(Decimal64.NaN, value);
            assertEquals(fpsf.value, JavaImplParse.BID_INVALID_FORMAT);
            assertEquals(doubleParseOk, decimalParseOk(fpsf));
        }
    }

    @Test
    public void tryParseInvalidString() {
        final @Decimal long value = Decimal64Utils.tryParse("INVALID", Decimal64Utils.NaN);
        assertTrue(Decimal64Utils.isNaN(value));
    }
}
