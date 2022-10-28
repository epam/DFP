package com.epam.deltix.dfp;

import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.Random;
import java.util.stream.IntStream;

import static com.epam.deltix.dfp.JavaImpl.*;
import static com.epam.deltix.dfp.TestUtils.*;
import static com.epam.deltix.dfp.TestUtils.POWERS_OF_TEN;
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
        JavaImpl.parse("asdf", 0, 0, BID_ROUNDING_TO_NEAREST);
    }

    @Test(expected = NumberFormatException.class)
    public void parseNonDigits() {
        JavaImpl.parse("asdf", 0, 4, BID_ROUNDING_TO_NEAREST);
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

    @Test
    public void TestRoundRandomly() throws Exception {
        final int randomPointMaxOffset = Decimal64Utils.MAX_SIGNIFICAND_DIGITS + Decimal64Utils.MAX_SIGNIFICAND_DIGITS / 4;

        checkInMultipleThreads(() -> {
            final RandomDecimalsGenerator random = new RandomDecimalsGenerator(
                new MersenneTwister(), 1,
                Decimal64Utils.MIN_EXPONENT + randomPointMaxOffset,
                Decimal64Utils.MAX_EXPONENT - randomPointMaxOffset);

            final RoundingMode[] roundingModes = RoundingMode.values();

            for (int ri = 0; ri < NTests / 10 /* Test will be very slow without this division */ ; ++ri) {
                final int randomOffset = random.generator.nextInt(randomPointMaxOffset * 2 + 1) - randomPointMaxOffset;

                final long inValue = random.nextX();
                final int roundPoint = random.getXExp() + randomOffset;
                final RoundingMode roundType = roundingModes[random.generator.nextInt(roundingModes.length)];
                checkRound(inValue, -roundPoint, roundType);
            }
        });
    }

    @Test
    public void TestRoundCase() {
        for (final long testValue : specialValues)
            for (final int roundPoint : new int[]{20, 10, 5, 3, 1, 0, -1, -2, -6, -11, -19})
                for (final RoundingMode roundType : RoundingMode.values())
                    checkRound(testValue, roundPoint, roundType);

        checkRound(0x2feb29430a256d21L, -1, RoundingMode.UP);
        checkRound(-5787416479386436811L, 1, RoundingMode.HALF_UP);
        checkRound(3439124486823148033L, 1, RoundingMode.FLOOR);
        checkRound(-1444740417884338647L, 0, RoundingMode.HALF_UP);
        checkRound(3439028411434681001L, -7, RoundingMode.HALF_UP);
        checkRound(-5778759999361643774L, 2, RoundingMode.HALF_UP);
        checkRound(3448058746773778910L, -4, RoundingMode.CEILING);
        checkRound(1417525816301142050L, -209, RoundingMode.CEILING);
        checkRound(2996092184105885832L, -61, RoundingMode.CEILING);
        checkRound(-922689384669825404L, -236, RoundingMode.FLOOR);
    }

    @Test
    public void TestRoundFromJavadoc() {
        final String[] inputStrings = {"5.5", "2.5", "1.6", "1.1", "1.0", "-1.0", "-1.1", "-1.6", "-2.5", "-5.5"};
        final long[] inputNumbers = new long[inputStrings.length];
        for (int i = 0; i < inputNumbers.length; ++i)
            inputNumbers[i] = Decimal64Utils.parse(inputStrings[i]);

        checkRoundCases(inputNumbers, RoundingMode.UP, new int[]{6, 3, 2, 2, 1, -1, -2, -2, -3, -6});
        checkRoundCases(inputNumbers, RoundingMode.DOWN, new int[]{5, 2, 1, 1, 1, -1, -1, -1, -2, -5});
        checkRoundCases(inputNumbers, RoundingMode.CEILING, new int[]{6, 3, 2, 2, 1, -1, -1, -1, -2, -5});
        checkRoundCases(inputNumbers, RoundingMode.FLOOR, new int[]{5, 2, 1, 1, 1, -1, -2, -2, -3, -6});
        checkRoundCases(inputNumbers, RoundingMode.HALF_UP, new int[]{6, 3, 2, 1, 1, -1, -1, -2, -3, -6});
        checkRoundCases(inputNumbers, RoundingMode.HALF_DOWN, new int[]{5, 2, 2, 1, 1, -1, -1, -2, -2, -5});
        checkRoundCases(inputNumbers, RoundingMode.HALF_EVEN, new int[]{6, 2, 2, 1, 1, -1, -1, -2, -2, -6});

        final ArithmeticException expectedException = new ArithmeticException("Rounding necessary");
        final Object[] refUnnecessary = {
            expectedException,
            expectedException,
            expectedException,
            expectedException,
            Decimal64Utils.fromInt(1),
            Decimal64Utils.fromInt(-1),
            expectedException,
            expectedException,
            expectedException,
            expectedException
        };
        for (int i = 0; i < inputNumbers.length; ++i) {
            Object testRet;
            try {
                testRet = Decimal64Utils.round(inputNumbers[i], 0, RoundingMode.UNNECESSARY);
            } catch (ArithmeticException e) {
                testRet = e;
            }
            Object refRet = refUnnecessary[i];
            final boolean isEq;
            if (testRet instanceof ArithmeticException || refRet instanceof ArithmeticException)
                isEq = testRet.toString().equals(refRet.toString());
            else
                isEq = Decimal64Utils.isEqual((long) testRet, (long) refRet);
            if (!isEq)
                throw new RuntimeException("The round(0x" + Long.toHexString(inputNumbers[i]) + "L, 0, " + RoundingMode.UNNECESSARY +
                    ") = " + refUnnecessary[i] + ", but test return " + testRet);
        }
    }

    private static void checkRoundCases(final long[] inputNumbers, final RoundingMode roundType, final int[] refValue) {
        for (int i = 0; i < inputNumbers.length; ++i) {
            final long testRet = Decimal64Utils.round(inputNumbers[i], 0, roundType);
            final long refRet = Decimal64Utils.fromInt(refValue[i]);
            if (!Decimal64Utils.isEqual(refRet, testRet))
                throw new RuntimeException("The round(0x" + Long.toHexString(inputNumbers[i]) + "L(=" +
                    Decimal64Utils.toScientificString(inputNumbers[i]) + "), 0, " + roundType +
                    ") = 0x" + Long.toHexString(refRet) + "L(=" + Decimal64Utils.toScientificString(refRet) +
                    "), but test return 0x" + Long.toHexString(testRet) + "L(=" + Decimal64Utils.toScientificString(testRet) + ")");
        }
    }

    private static BigDecimal posOffset = new BigDecimal("1e+500");
    private static BigDecimal negOffset = new BigDecimal("-1e+500");

    private static void checkRound(final long inValue, final int roundPoint, final RoundingMode roundType) {
        String testStr;
        try {
            final long testValue = Decimal64Utils.round(inValue, roundPoint, roundType);
            if (Decimal64Utils.isFinite(testValue))
                testStr = Decimal64Utils.toBigDecimal(testValue).stripTrailingZeros().toString();
            else
                testStr = Decimal64Utils.toString(testValue);
        } catch (final ArithmeticException e) {
            testStr = e.toString();
        }

        String refStr;
        if (Decimal64Utils.isFinite(inValue)) {
            try {
                BigDecimal bd = Decimal64Utils.toBigDecimal(inValue);
                BigDecimal offset = Decimal64Utils.isPositive(inValue) ? posOffset : negOffset;
                bd = bd.add(offset);
                final int newPrecision = bd.precision() - bd.scale() + roundPoint;
                refStr = bd.round(new MathContext(newPrecision, roundType)).subtract(offset).stripTrailingZeros().toString();
            } catch (final ArithmeticException e) {
                refStr = e.toString();
            }
        } else {
            refStr = Decimal64Utils.toString(inValue);
        }

        if (!refStr.equals(testStr))
            throw new RuntimeException("Case checkRound(0x" + Long.toHexString(inValue) +
                "L, " + roundPoint + ", " + roundType.getClass().getSimpleName() + "." + roundType +
                "); error: Value " + Decimal64Utils.toScientificString(inValue) +
                " BigDecimal rounding (=" + refStr + ") != Decimal64Utils rounding (=" + testStr + ")");
    }

    @Test
    public void unCanonizedRound() {
        final long zeroU = 0x2FE0000000000000L;
        final long f = 0x2F638D7EA4C68000L; // 0.0001
        final long zeroP = Decimal64Utils.multiply(zeroU, f);

        assertDecimalEqual(Decimal64Utils.ZERO, Decimal64Utils.round(zeroP, 0, RoundingMode.CEILING));
    }

    @Test
    public void testRoundToReciprocal() {
        testRoundToReciprocalCase(Decimal64Utils.parse("0.125"), 8, RoundingMode.UNNECESSARY);

        testRoundToReciprocalCase(/*687034157780582.4*/ 3582728445709979648L, 1440395186, RoundingMode.HALF_EVEN);

        testRoundToReciprocalCase(/*0.000000000093*/ 3476778912330023005L, 76984627, RoundingMode.CEILING);

        testRoundToReciprocalCase(/*-0.000000000079*/ -5746593124524752817L, 1850110060, RoundingMode.DOWN);

        testRoundToReciprocalCase(/*-0.0001*/ -5674535530486824959L, 579312130, RoundingMode.DOWN);

        testRoundToReciprocalCase(/*-0.000000000923*/ -5746593124524751973L, 1, RoundingMode.UP);
        testRoundToReciprocalCase(/*-0.000000000923*/ -5746593124524751973L, 15292403, RoundingMode.UP);
        testRoundToReciprocalCase(/*0.00000000000043*/ 3458764513820540971L, 63907328, RoundingMode.UP);

        testRoundToReciprocalCase(Decimal64Utils.parse("0.9999999999999999"), Integer.MAX_VALUE, RoundingMode.DOWN);

        final RoundingMode[] roundingModes = {
            RoundingMode.UP, RoundingMode.DOWN,
            RoundingMode.CEILING, RoundingMode.FLOOR,
            RoundingMode.HALF_UP, RoundingMode.HALF_DOWN, RoundingMode.HALF_EVEN};

        final ThreadLocal<Random> tlsRandom = ThreadLocal.withInitial(SecureRandom::new);

        IntStream.range(0, 10_000_000).parallel().forEach(ri -> {
            final Random random = tlsRandom.get();

            final int mantissaLen = random.nextInt(Decimal64Utils.MAX_SIGNIFICAND_DIGITS) + 1;
            final long mantissa = random.nextLong() % POWERS_OF_TEN[mantissaLen];

            final int exp = random.nextInt(20) - Decimal64Utils.MAX_SIGNIFICAND_DIGITS;

            @Decimal final long value = Decimal64Utils.fromFixedPoint(mantissa, -exp);

            final int n = Math.abs(random.nextInt());

            final RoundingMode roundingMode = roundingModes[random.nextInt(roundingModes.length)];

            testRoundToReciprocalCase(value, n, roundingMode);
        });
    }

    private static void testRoundToReciprocalCase(@Decimal final long value, final int n, final RoundingMode roundingMode) {
        @Decimal final long roundedValue = Decimal64Utils.roundToReciprocal(value, n, roundingMode);

        final BigDecimal ref = Decimal64Utils.toBigDecimal(value).multiply(new BigDecimal(n)).setScale(0, roundingMode)
            .divide(new BigDecimal(n), mcDecimal64);

        if (ref.compareTo(Decimal64Utils.toBigDecimal(roundedValue)) != 0)
            throw new RuntimeException("The testRoundToReciprocalCase(/*" + Decimal64Utils.toString(value) + "*/ " + value +
                "L, " + n + ", RoundingMode." + roundingMode + ") error: roundedValue(=" + Decimal64Utils.toString(roundedValue) + ") != ref(=" + ref + ").");
    }

    static final MathContext mcDecimal64 = new MathContext(Decimal64Utils.MAX_SIGNIFICAND_DIGITS, RoundingMode.HALF_UP);

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
            final String testStr = Decimal64Utils.toString(value);
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

    @Test
    public void testFmaWithCoverage() throws Exception {
        for (final long x : specialValues)
            for (final long y : specialValues)
                for (final long z : specialValues)
                    checkFmaCase(x, y, z);

        checkInMultipleThreads(() -> {
            final RandomDecimalsGenerator random = new RandomDecimalsGenerator();
            for (int i = 0; i < NTests; ++i)
                checkFmaCase(random.nextX(), random.nextX(), random.nextX());
        });
    }

    public static void checkFmaCase(final long x, final long y, final long z) {
        final long testRet = Decimal64Utils.multiplyAndAdd(x, y, z);
        final long refRet = NativeImpl.multiplyAndAdd(x, y, z);

        if (testRet != refRet)
            throw new RuntimeException("The function(0x" + Long.toHexString(x) + "L, 0x" + Long.toHexString(y) +
                "L, 0x" + Long.toHexString(z) + "L) = 0x" + Long.toHexString(refRet) + "L, but test return 0x" + Long.toHexString(testRet) + "L");
    }

    @Test
    public void testAddWithCoverage() throws Exception {
        checkCases(NativeImpl::add2, Decimal64Utils::add,
            ((long) EXPONENT_BIAS << EXPONENT_SHIFT_SMALL) | 1000000000000000L,
            MASK_SIGN | ((long) (EXPONENT_BIAS - MAX_FORMAT_DIGITS - 1) << EXPONENT_SHIFT_SMALL) | 5000000000000001L,
            0xecb08366cd530a32L, 0xb2fc7ab89d54c15dL,
            0x335bb3b1068d9bd8L, 0x32ee619e7226bc85L);

        checkWithCoverage(NativeImpl::add2, Decimal64Utils::add);
    }

    @Test
    public void testSubWithCoverage() throws Exception {
        checkWithCoverage(NativeImpl::subtract, Decimal64Utils::subtract);
    }

    @Test
    public void testMulWithCoverage() throws Exception {
        checkCases(NativeImpl::multiply2, Decimal64Utils::multiply,
            ((long) EXPONENT_BIAS << EXPONENT_SHIFT_SMALL) | 1000000000000000L,
            MASK_SIGN | ((long) (EXPONENT_BIAS - MAX_FORMAT_DIGITS - 1) << EXPONENT_SHIFT_SMALL) | 5000000000000001L);

        checkWithCoverage(NativeImpl::multiply2, Decimal64Utils::multiply);
    }

    @Test
    public void testDivWithCoverage() throws Exception {
        checkCases(NativeImpl::divide, Decimal64Utils::divide,
            ((long) EXPONENT_BIAS << EXPONENT_SHIFT_SMALL) | 1000000000000000L,
            MASK_SIGN | ((long) (EXPONENT_BIAS - MAX_FORMAT_DIGITS - 1) << EXPONENT_SHIFT_SMALL) | 5000000000000001L,
            0x31a000000000000dL, 0x2e800000000006d1L,
            0x30A0EFABDABB1574L, 0x30A0000062DF732AL,
            0x31c38d7ea4c68000L, 0xafb1c37937e08001L);

        checkWithCoverage(NativeImpl::divide, Decimal64Utils::divide);
    }

    @Test
    public void testDiv2WithCoverage() throws Exception {
        checkCases(x -> NativeImpl.divide(x, Decimal64Utils.TWO), JavaImplDiv::div2,
            0x2feb29430a256d21L, 0x5fe05af3107a4000L, 0xf7fb86f26fc0ffffL, 0xafe9a8434ec8e225L);

        checkWithCoverage(x -> NativeImpl.divide(x, Decimal64Utils.TWO), JavaImplDiv::div2);
    }

    @Test
    public void testMinWithCoverage() throws Exception {
        checkWithCoverage(NativeImpl::min2, Decimal64Utils::min);
    }

    @Test
    public void testMaxWithCoverage() throws Exception {
        checkWithCoverage(NativeImpl::max2, Decimal64Utils::max);
    }

    @Test
    public void testMean2Coverage() throws Exception {
        checkWithCoverage(NativeImpl::mean2, Decimal64Utils::mean);
    }

    @Test
    public void testMultiplyByInt32Coverage() throws Exception {
        checkWithCoverage(
            (a, b) -> NativeImpl.multiplyByInt32(a, (int) b),
            (a, b) -> Decimal64Utils.multiplyByInteger(a, (int) b));
    }

    @Test
    public void testMultiplyByInt64Coverage() throws Exception {
        checkWithCoverage(NativeImpl::multiplyByInt64, Decimal64Utils::multiplyByInteger);
    }

    @Test
    public void testDivideByInt32Coverage() throws Exception {
        checkWithCoverage(
            (a, b) -> NativeImpl.divideByInt32(a, (int) b),
            (a, b) -> Decimal64Utils.divideByInteger(a, (int) b));
    }

    @Test
    public void testDivideByInt64Coverage() throws Exception {
        checkWithCoverage(NativeImpl::divideByInt64, Decimal64Utils::divideByInteger);
    }

    @Test
    public void testDoubleToDecimalCoverage() throws Exception {
        checkWithCoverage(
            x -> NativeImpl.fromFloat64(Double.longBitsToDouble(x)),
            x -> Decimal64Utils.fromDouble(Double.longBitsToDouble(x)));
    }

    @Test
    public void testDecimalToDoubleCoverage() throws Exception {
        checkWithCoverage(
            x -> Double.doubleToRawLongBits(NativeImpl.toFloat64(x)),
            x -> Double.doubleToRawLongBits(Decimal64Utils.toDouble(x)));
    }

    @Test
    public void testInt64ToDecimalCoverage() throws Exception {
        checkWithCoverage(NativeImpl::fromInt64, Decimal64Utils::fromLong);
    }

    @Test
    public void testDecimalToInt64Coverage() throws Exception {
        checkWithCoverage(NativeImpl::toInt64, Decimal64Utils::toLong);
    }

    @Test
    public void testScaleByPowerOfTenCoverage() throws Exception {
        checkWithCoverage(
            (x, n) -> NativeImpl.scaleByPowerOfTen(x, (int) (n % 450)),
            (x, n) -> Decimal64Utils.scaleByPowerOfTen(x, (int) (n % 450)));
    }

    @Test
    public void testFromFixedPoint64Coverage() throws Exception {
        checkWithCoverage(
            (x, n) -> NativeImpl.fromFixedPoint64(x, (int) (n % 450)),
            (x, n) -> Decimal64Utils.fromFixedPoint(x, (int) (n % 450)));
    }

    @Test
    public void testToFixedPointCoverage() throws Exception {
        checkWithCoverage(
            (x, n) -> NativeImpl.toFixedPoint(x, (int) (n % 450)),
            (x, n) -> Decimal64Utils.toFixedPoint(x, (int) (n % 450)));
    }

    @Test
    public void testRoundTowardsPositiveInfinity() throws Exception {
        checkWithCoverage(NativeImpl::roundTowardsPositiveInfinity, Decimal64Utils::roundTowardsPositiveInfinity);
    }

    @Test
    public void testRoundTowardsNegativeInfinity() throws Exception {
        checkWithCoverage(NativeImpl::roundTowardsNegativeInfinity, Decimal64Utils::roundTowardsNegativeInfinity);
    }

    @Test
    public void testRoundTowardsZero() throws Exception {
        checkWithCoverage(NativeImpl::roundTowardsZero, Decimal64Utils::roundTowardsZero);
    }

    @Test
    public void testRoundToNearestTiesAwayFromZero() throws Exception {
        checkWithCoverage(NativeImpl::roundToNearestTiesAwayFromZero, Decimal64Utils::roundToNearestTiesAwayFromZero);
    }

    @Test
    public void testRoundToNearestTiesToEven() throws Exception {
        checkWithCoverage(NativeImpl::roundToNearestTiesToEven, Decimal64Utils::roundToNearestTiesToEven);
    }

    @Test
    public void testRoundCeiling() throws Exception {
        checkEqualityWithCoverage(NativeImpl::roundTowardsPositiveInfinity, x -> Decimal64Utils.round(x, 0, RoundingMode.CEILING));
    }

    @Test
    public void testRoundFloor() throws Exception {
        checkEqualityWithCoverage(NativeImpl::roundTowardsNegativeInfinity, x -> Decimal64Utils.round(x, 0, RoundingMode.FLOOR));
    }

    @Test
    public void testRoundDown() throws Exception {
        checkEqualityWithCoverage(NativeImpl::roundTowardsZero, x -> Decimal64Utils.round(x, 0, RoundingMode.DOWN));
    }

    @Test
    public void testRoundHalfUp() throws Exception {
        checkEqualityWithCoverage(NativeImpl::roundToNearestTiesAwayFromZero, x -> Decimal64Utils.round(x, 0, RoundingMode.HALF_UP));
    }

    @Test
    public void testRoundHalfEven() throws Exception {
        checkEqualityWithCoverage(NativeImpl::roundToNearestTiesToEven, x -> Decimal64Utils.round(x, 0, RoundingMode.HALF_EVEN));
    }

    @Test
    public void testNextUpCoverage() throws Exception {
        checkWithCoverage(NativeImpl::nextUp, Decimal64Utils::nextUp);
    }

    @Test
    public void testNextDownCoverage() throws Exception {
        checkWithCoverage(NativeImpl::nextDown, Decimal64Utils::nextDown);
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
            for (int i = 0; i < NTests / 10; ++i)
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
        return (fpsf.status & JavaImplParse.BID_INVALID_FORMAT) == 0;
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
            assertEquals(fpsf.status, JavaImplParse.BID_EXACT_STATUS);
            assertEquals(doubleParseOk, decimalParseOk(fpsf));
        }
        {
            final String testStr = "00..";
            JavaImplParse.FloatingPointStatusFlag fpsf = new JavaImplParse.FloatingPointStatusFlag();
            Decimal64 value = Decimal64.fromUnderlying(JavaImplParse.bid64_from_string(testStr, 0, testStr.length(), fpsf, roundMode));
            DoubleHolder doubleHolder = new DoubleHolder();
            final boolean doubleParseOk = doubleTryParse(testStr, doubleHolder);
            assertEquals(Decimal64.NaN, value);
            assertEquals(fpsf.status, JavaImplParse.BID_INVALID_FORMAT);
            assertEquals(doubleParseOk, decimalParseOk(fpsf));
        }
        {
            final String testStr = "000235";
            JavaImplParse.FloatingPointStatusFlag fpsf = new JavaImplParse.FloatingPointStatusFlag();
            Decimal64 value = Decimal64.fromUnderlying(JavaImplParse.bid64_from_string(testStr, 0, testStr.length(), fpsf, roundMode));
            DoubleHolder doubleHolder = new DoubleHolder();
            final boolean doubleParseOk = doubleTryParse(testStr, doubleHolder);
            assertEquals(Decimal64.fromInt(235), value);
            assertEquals(fpsf.status, JavaImplParse.BID_EXACT_STATUS);
            assertEquals(doubleParseOk, decimalParseOk(fpsf));
        }
        {
            final String testStr = "00.0000235";
            JavaImplParse.FloatingPointStatusFlag fpsf = new JavaImplParse.FloatingPointStatusFlag();
            Decimal64 value = Decimal64.fromUnderlying(JavaImplParse.bid64_from_string(testStr, 0, testStr.length(), fpsf, roundMode));
            DoubleHolder doubleHolder = new DoubleHolder();
            final boolean doubleParseOk = doubleTryParse(testStr, doubleHolder);
            assertEquals(Decimal64.fromFixedPoint(235, 7), value);
            assertEquals(fpsf.status, JavaImplParse.BID_EXACT_STATUS);
            assertEquals(doubleParseOk, decimalParseOk(fpsf));
        }
        {
            final String testStr = "1234512345123451234500000";
            JavaImplParse.FloatingPointStatusFlag fpsf = new JavaImplParse.FloatingPointStatusFlag();
            Decimal64 value = Decimal64.fromUnderlying(JavaImplParse.bid64_from_string(testStr, 0, testStr.length(), fpsf, roundMode));
            DoubleHolder doubleHolder = new DoubleHolder();
            final boolean doubleParseOk = doubleTryParse(testStr, doubleHolder);
            assertEquals(Decimal64.fromFixedPoint(1234512345123451L, -9), value);
            assertEquals(fpsf.status, JavaImplParse.BID_INEXACT_EXCEPTION);
            assertEquals(doubleParseOk, decimalParseOk(fpsf));
        }
        {
            final String testStr = "1234512345123451234500000e+12345123451234512345";
            JavaImplParse.FloatingPointStatusFlag fpsf = new JavaImplParse.FloatingPointStatusFlag();
            Decimal64 value = Decimal64.fromUnderlying(JavaImplParse.bid64_from_string(testStr, 0, testStr.length(), fpsf, roundMode));
            DoubleHolder doubleHolder = new DoubleHolder();
            final boolean doubleParseOk = doubleTryParse(testStr, doubleHolder);
            assertEquals(Decimal64.POSITIVE_INFINITY, value);
            assertEquals(fpsf.status, JavaImplParse.BID_INEXACT_EXCEPTION | JavaImplParse.BID_OVERFLOW_EXCEPTION);
            assertEquals(doubleParseOk, decimalParseOk(fpsf));
        }
        {
            final String testStr = "  -5000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000  ";
            JavaImplParse.FloatingPointStatusFlag fpsf = new JavaImplParse.FloatingPointStatusFlag();
            Decimal64 value = Decimal64.fromUnderlying(JavaImplParse.bid64_from_string(testStr, 0, testStr.length(), fpsf, roundMode));
            DoubleHolder doubleHolder = new DoubleHolder();
            final boolean doubleParseOk = doubleTryParse(testStr, doubleHolder);
            assertEquals(Decimal64.NEGATIVE_INFINITY, value);
            assertEquals(fpsf.status, JavaImplParse.BID_INEXACT_EXCEPTION | JavaImplParse.BID_OVERFLOW_EXCEPTION);
            assertEquals(doubleParseOk, decimalParseOk(fpsf));
        }
        {
            final String testStr = "0.00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000005";
            JavaImplParse.FloatingPointStatusFlag fpsf = new JavaImplParse.FloatingPointStatusFlag();
            Decimal64 value = Decimal64.fromUnderlying(JavaImplParse.bid64_from_string(testStr, 0, testStr.length(), fpsf, roundMode));
            DoubleHolder doubleHolder = new DoubleHolder();
            final boolean doubleParseOk = doubleTryParse(testStr, doubleHolder);
            assertEquals(Decimal64.ZERO, value);
            assertEquals(fpsf.status, JavaImplParse.BID_INEXACT_EXCEPTION | JavaImplParse.BID_UNDERFLOW_EXCEPTION);
            assertEquals(doubleParseOk, decimalParseOk(fpsf));
        }
        {
            final String testStr = "  123 x99  ";
            JavaImplParse.FloatingPointStatusFlag fpsf = new JavaImplParse.FloatingPointStatusFlag();
            Decimal64 value = Decimal64.fromUnderlying(JavaImplParse.bid64_from_string(testStr, 0, testStr.length(), fpsf, roundMode));
            DoubleHolder doubleHolder = new DoubleHolder();
            final boolean doubleParseOk = doubleTryParse(testStr, doubleHolder);
            assertEquals(Decimal64.NaN, value);
            assertEquals(fpsf.status, JavaImplParse.BID_INVALID_FORMAT);
            assertEquals(doubleParseOk, decimalParseOk(fpsf));
        }
    }

    @Test
    public void tryParseInvalidString() {
        final @Decimal long value = Decimal64Utils.tryParse("INVALID", Decimal64Utils.NaN);
        assertTrue(Decimal64Utils.isNaN(value));
    }

    @Test
    public void testPartsSplitWithCoverage() throws Exception {
        for (final long x : specialValues) {
            if (!Decimal64Utils.isFinite(x))
                continue;
            testPartsSplitCase(x);
        }

        checkInMultipleThreads(() -> {
            final RandomDecimalsGenerator random = new RandomDecimalsGenerator();
            for (int i = 0; i < NTests / 10; ++i)
                testPartsSplitCase(random.nextX());
        });
    }

    private static void testPartsSplitCase(final long testValue) {
        final BigDecimal bd = Decimal64Utils.toBigDecimal(testValue);

        final long mantissa = Decimal64Utils.getUnscaledValue(testValue);
        final int exp = Decimal64Utils.getScale(testValue);

        assertEquals("The decimal 0x" + Long.toHexString(testValue) + "L(=" +
                Decimal64Utils.toScientificString(testValue) + ") unscaled value error.",
            bd.unscaledValue().longValueExact(), mantissa);

        assertEquals("The decimal 0x" + Long.toHexString(testValue) + "L(=" +
            Decimal64Utils.toScientificString(testValue) + ") exponent error.", bd.scale(), exp);

        assertTrue("The decimal 0x" + Long.toHexString(testValue) + "L(=" +
                Decimal64Utils.toScientificString(testValue) + ") reconstruction error.",
            Decimal64Utils.equals(testValue, Decimal64Utils.fromFixedPoint(mantissa, exp)));
    }

    @Test(expected = NumberFormatException.class)
    public void testBigDecimalExact() {
        final BigDecimal bd = Decimal64Utils.toBigDecimal(Decimal64Utils.fromBigDecimalExact(
            new BigDecimal(new BigInteger("3201921152691614969"))));
    }

    @Test
    public void testParse() {
        final String testStr = "0.123456789123456789";
        final Decimal64Status decimal64Status = dfpTlsStatus.get();
        Decimal64Utils.tryParse(testStr, 0, testStr.length(), decimal64Status);
        Assert.assertFalse(decimal64Status.isExact());
        Assert.assertTrue(decimal64Status.isInexact());
    }

    static ThreadLocal<Decimal64Status> dfpTlsStatus =
        ThreadLocal.withInitial(Decimal64Status::new);
}
