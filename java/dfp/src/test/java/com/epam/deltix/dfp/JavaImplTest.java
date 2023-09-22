package com.epam.deltix.dfp;

import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Assert;
import org.junit.Test;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.Random;

import static com.epam.deltix.dfp.Decimal64Utils.*;
import static com.epam.deltix.dfp.JavaImpl.*;
import static com.epam.deltix.dfp.JavaImplAdd.LONG_LOW_PART;
import static com.epam.deltix.dfp.JavaImplCmp.MASK_BINARY_SIG2;
import static com.epam.deltix.dfp.JavaImplCmp.MASK_BINARY_OR2;
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
        assertEquals("NaN", appendToRefImpl(Decimal64Utils.NaN, DECIMAL_MARK_DOT, string).toString());

        string.setLength(0);
        assertEquals("Infinity", appendToRefImpl(Decimal64Utils.POSITIVE_INFINITY, DECIMAL_MARK_DOT, string).toString());

        string.setLength(0);
        assertEquals("-Infinity", appendToRefImpl(Decimal64Utils.NEGATIVE_INFINITY, DECIMAL_MARK_DOT, string).toString());

        string.setLength(0);
        assertEquals("100000010000.0", JavaImpl.appendToRefImpl(Decimal64Utils.fromDouble(10000001E+04), DECIMAL_MARK_DOT, string).toString());

        string.setLength(0);
        assertEquals("10000001.0", JavaImpl.appendToRefImpl(Decimal64Utils.fromDouble(10000001), DECIMAL_MARK_DOT, string).toString());

        string.setLength(0);
        assertEquals("1000.0001", JavaImpl.appendToRefImpl(Decimal64Utils.fromDouble(10000001E-04), DECIMAL_MARK_DOT, string).toString());

        string.setLength(0);
        assertEquals("9.2", appendToRefImpl(Decimal64Utils.fromDecimalDouble(92E-01), DECIMAL_MARK_DOT, string).toString());
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
        checkToString(null, "31800000013474D8", "202150.0", "+20215000E-2");
    }

    @Test
    public void toStringSpecialCase3() {
        checkToString(null, "8020000000000000", "0.0", "-0E-397");
    }

    @Test
    public void toStringSpecialCase4() {
        checkToString(null, "30593A484825D4D1", "7100.956540261585", "+7100956540261585E-12");
    }

    @Test(expected = NumberFormatException.class)
    public void parseEmptyString() {
        Decimal64Utils.parse("asdf", 0, 0);
    }

    @Test(expected = NumberFormatException.class)
    public void parseNonDigits() {
        Decimal64Utils.parse("asdf", 0, 4);
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

    private static class MaxError {
        public long ulp = 0;
        public long val = 0;
        public int n = 0;
        public RoundingMode mode = null;
    }

    @Test
    public void testRoundToReciprocal() {
        testRoundToReciprocalCase(/*-0.00144*/ -5683542729741565808L, 55559375, RoundingMode.HALF_EVEN); // roundedValue(=-0.001440008999381293) != refValue(=-0.001439991000618707); Note: bigDecimal=-0.001439991000618707.

        testRoundToReciprocalCase(/*0.01*/ 3566850904877432833L, 56740250, RoundingMode.HALF_EVEN); // roundedValue(=0.009999991187913342) != refValue(=0.01000000881208666); Note: bigDecimal=0.01000000881208666.

        testRoundToReciprocalCase(/* 0.002 */ 3557843705622691842L, 98692250, RoundingMode.HALF_EVEN);

        testRoundToReciprocalCase(/*-0.1*/ -5647513932722601983L, 434753515, RoundingMode.HALF_EVEN); // roundedValue(=-0.09999999884992305) != roundedValueRec(=-0.100000001150077); Note: bigDecimal=-0.099999998849923042025318645209803536608553929690482203461885753835291530.


        testRoundToReciprocalCase(/*-0.08*/ -5656521131977342968L, 664150, RoundingMode.CEILING); // roundedValue(=-0.07999849431604307) != roundedValueRec(=-0.08); Note: bigDecimal=-0.08.

        testRoundToReciprocalCase(/*-2576127.8853*/ -5674535504725546107L, 383456473, RoundingMode.UP); // roundedValue(=-2576127.885299999) != ref(=-2576127.885300001)
        testRoundToReciprocalCase(Decimal64Utils.parse("0.9999999999999999"), Integer.MAX_VALUE, RoundingMode.DOWN);

        testRoundToReciprocalCase(Decimal64Utils.parse("0.125"), 8, RoundingMode.UNNECESSARY);

        testRoundToReciprocalCase(/*687034157780582.4*/ 3582728445709979648L, 1440395186, RoundingMode.HALF_EVEN);

        testRoundToReciprocalCase(/*0.000000000093*/ 3476778912330023005L, 76984627, RoundingMode.CEILING);

        testRoundToReciprocalCase(/*-0.000000000079*/ -5746593124524752817L, 1850110060, RoundingMode.DOWN);

        testRoundToReciprocalCase(/*-0.0001*/ -5674535530486824959L, 579312130, RoundingMode.DOWN);

        testRoundToReciprocalCase(/*-0.000000000923*/ -5746593124524751973L, 1, RoundingMode.UP);
        testRoundToReciprocalCase(/*-0.000000000923*/ -5746593124524751973L, 15292403, RoundingMode.UP);
        testRoundToReciprocalCase(/*0.00000000000043*/ 3458764513820540971L, 63907328, RoundingMode.UP);

        final RoundingMode[] roundingModes = {
            RoundingMode.UP, RoundingMode.DOWN,
            RoundingMode.CEILING, RoundingMode.FLOOR,
            RoundingMode.HALF_UP, RoundingMode.HALF_DOWN, RoundingMode.HALF_EVEN};

        final ThreadLocal<Random> tlsRandom = ThreadLocal.withInitial(SecureRandom::new);

        final MaxError maxErr = new MaxError();
        for (int ri = 0; ri < 1_000_000; ++ri) /*IntStream.range(0, 10_000_000).parallel().forEach(ri ->*/ {
            final Random random = tlsRandom.get();

            final int mantissaLen = random.nextInt(Decimal64Utils.MAX_SIGNIFICAND_DIGITS) + 1;
            final long mantissa = random.nextLong() % POWERS_OF_TEN[mantissaLen];

            final int exp = random.nextInt(20) - Decimal64Utils.MAX_SIGNIFICAND_DIGITS;

            int n = Math.abs(random.nextInt());
            if (n == 0)
                n = 1;

            final RoundingMode roundingMode = roundingModes[random.nextInt(roundingModes.length)];

            try {
                @Decimal final long value = Decimal64Utils.fromFixedPoint(mantissa, -exp);

                final long ulpErr = testRoundToReciprocalCase(value, n, roundingMode, false);
                if (maxErr.ulp < ulpErr) {
                    synchronized (maxErr) {
                        if (maxErr.ulp < ulpErr) {
                            maxErr.ulp = ulpErr;
                            maxErr.val = value;
                            maxErr.n = n;
                            maxErr.mode = roundingMode;
                        }
                    }
                }
            } catch (final Throwable e) {
                throw new RuntimeException("Error on processing case mantissa(=" + mantissa + "), exp(=" + exp +
                    "), n(=" + n + "), mode(=" + roundingMode + ")", e);
            }
        }

        if (maxErr.mode != null)
            testRoundToReciprocalCase(maxErr.val, maxErr.n, maxErr.mode, true);
    }

    private static long testRoundToReciprocalCase(@Decimal final long value, final int n, final RoundingMode roundingMode) {
        return testRoundToReciprocalCase(value, n, roundingMode, true);
    }

    private static long testRoundToReciprocalCase(@Decimal final long value, final int n, final RoundingMode roundingMode, final boolean throwException) {
        // Reciprocal calculation style.
//        final BigDecimal scaledValue = Decimal64Utils.toBigDecimal(value).multiply(new BigDecimal(n)).setScale(0, roundingMode);
//        final BigDecimal refValueBig = scaledValue.divide(new BigDecimal(n), mcDecimalN64);
//        final BigDecimal refValue = scaledValue.divide(new BigDecimal(n), mcDecimal);

        // Old calculation style
        final BigDecimal bigMul = BigDecimal.ONE.divide(new BigDecimal(n), mcDecimalN64);
        final BigDecimal refValueDiv = Decimal64Utils.toBigDecimal(value).divide(bigMul, mcDecimalN64).round(mcDecimalN48);
        final BigDecimal refValueDivScale = refValueDiv.setScale(0, roundingMode);
        final BigDecimal refValueBig = refValueDivScale.multiply(bigMul);
        final BigDecimal refValue = refValueBig.round(mcDecimal);

        final long roundedValue = Decimal64Utils.roundToReciprocal(value, n, roundingMode);
//        final long roundedValue = Decimal64Utils.roundToNearestTiesToEven(value, Decimal64Utils.divideByInteger(Decimal64Utils.ONE, n));


        final BigDecimal roundedValueBd = Decimal64Utils.toBigDecimal(roundedValue);
        if (refValue.compareTo(Decimal64Utils.toBigDecimal(roundedValue)) != 0) {
            if (throwException) {
                throw new RuntimeException("testRoundToReciprocalCase(/*" + Decimal64Utils.toString(value) + "*/ " +
                    value + "L, " + n + ", RoundingMode." + roundingMode +
                    "); // roundedValue(=" + Decimal64Utils.toString(roundedValue) + ") != refValue(=" + refValue +
                    ") with bigDecimal(=" + refValueBig + ".");

            } else {
                if (Decimal64Utils.isZero(roundedValue))
                    return 0;

                final long ulp = Decimal64Utils.isPositive(roundedValue)
                    ? Decimal64Utils.subtract(Decimal64Utils.nextUp(roundedValue), roundedValue)
                    : Decimal64Utils.subtract(roundedValue, Decimal64Utils.nextDown(roundedValue));
                return refValue.subtract(roundedValueBd).abs().divide(Decimal64Utils.toBigDecimal(ulp)).longValue();
            }
        } else {
            return 0;
        }
    }

    static final MathContext mcDecimal = new MathContext(Decimal64Utils.MAX_SIGNIFICAND_DIGITS, RoundingMode.HALF_UP);
    static final MathContext mcDecimalN48 = new MathContext(48, RoundingMode.HALF_UP);
    static final MathContext mcDecimalN64 = new MathContext(64, RoundingMode.HALF_UP);

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
            final String inStr = JavaImpl.appendToRefImpl(value, DECIMAL_MARK_DEFAULT, new StringBuilder()).toString();
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
            Decimal64 value = Decimal64.fromUnderlying(JavaImplParse.bid64_from_string(testStr, 0, testStr.length(), fpsf, roundMode, DECIMAL_MARK_ANY));
            DoubleHolder doubleHolder = new DoubleHolder();
            final boolean doubleParseOk = doubleTryParse(testStr, doubleHolder);
            assertEquals(Decimal64.ZERO, value);
            assertEquals(fpsf.status, JavaImplParse.BID_EXACT_STATUS);
            assertEquals(doubleParseOk, decimalParseOk(fpsf));
        }
        {
            final String testStr = "00..";
            JavaImplParse.FloatingPointStatusFlag fpsf = new JavaImplParse.FloatingPointStatusFlag();
            Decimal64 value = Decimal64.fromUnderlying(JavaImplParse.bid64_from_string(testStr, 0, testStr.length(), fpsf, roundMode, DECIMAL_MARK_ANY));
            DoubleHolder doubleHolder = new DoubleHolder();
            final boolean doubleParseOk = doubleTryParse(testStr, doubleHolder);
            assertEquals(Decimal64.NaN, value);
            assertEquals(fpsf.status, JavaImplParse.BID_INVALID_FORMAT);
            assertEquals(doubleParseOk, decimalParseOk(fpsf));
        }
        {
            final String testStr = "000235";
            JavaImplParse.FloatingPointStatusFlag fpsf = new JavaImplParse.FloatingPointStatusFlag();
            Decimal64 value = Decimal64.fromUnderlying(JavaImplParse.bid64_from_string(testStr, 0, testStr.length(), fpsf, roundMode, DECIMAL_MARK_ANY));
            DoubleHolder doubleHolder = new DoubleHolder();
            final boolean doubleParseOk = doubleTryParse(testStr, doubleHolder);
            assertEquals(Decimal64.fromInt(235), value);
            assertEquals(fpsf.status, JavaImplParse.BID_EXACT_STATUS);
            assertEquals(doubleParseOk, decimalParseOk(fpsf));
        }
        {
            final String testStr = "00.0000235";
            JavaImplParse.FloatingPointStatusFlag fpsf = new JavaImplParse.FloatingPointStatusFlag();
            Decimal64 value = Decimal64.fromUnderlying(JavaImplParse.bid64_from_string(testStr, 0, testStr.length(), fpsf, roundMode, DECIMAL_MARK_ANY));
            DoubleHolder doubleHolder = new DoubleHolder();
            final boolean doubleParseOk = doubleTryParse(testStr, doubleHolder);
            assertEquals(Decimal64.fromFixedPoint(235, 7), value);
            assertEquals(fpsf.status, JavaImplParse.BID_EXACT_STATUS);
            assertEquals(doubleParseOk, decimalParseOk(fpsf));
        }
        {
            final String testStr = "1234512345123451234500000";
            JavaImplParse.FloatingPointStatusFlag fpsf = new JavaImplParse.FloatingPointStatusFlag();
            Decimal64 value = Decimal64.fromUnderlying(JavaImplParse.bid64_from_string(testStr, 0, testStr.length(), fpsf, roundMode, DECIMAL_MARK_ANY));
            DoubleHolder doubleHolder = new DoubleHolder();
            final boolean doubleParseOk = doubleTryParse(testStr, doubleHolder);
            assertEquals(Decimal64.fromFixedPoint(1234512345123451L, -9), value);
            assertEquals(fpsf.status, JavaImplParse.BID_INEXACT_EXCEPTION);
            assertEquals(doubleParseOk, decimalParseOk(fpsf));
        }
        {
            final String testStr = "1234512345123451234500000e+12345123451234512345";
            JavaImplParse.FloatingPointStatusFlag fpsf = new JavaImplParse.FloatingPointStatusFlag();
            Decimal64 value = Decimal64.fromUnderlying(JavaImplParse.bid64_from_string(testStr, 0, testStr.length(), fpsf, roundMode, DECIMAL_MARK_ANY));
            DoubleHolder doubleHolder = new DoubleHolder();
            final boolean doubleParseOk = doubleTryParse(testStr, doubleHolder);
            assertEquals(Decimal64.POSITIVE_INFINITY, value);
            assertEquals(fpsf.status, JavaImplParse.BID_INEXACT_EXCEPTION | JavaImplParse.BID_OVERFLOW_EXCEPTION);
            assertEquals(doubleParseOk, decimalParseOk(fpsf));
        }
        {
            final String testStr = "  -5000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000  ";
            JavaImplParse.FloatingPointStatusFlag fpsf = new JavaImplParse.FloatingPointStatusFlag();
            Decimal64 value = Decimal64.fromUnderlying(JavaImplParse.bid64_from_string(testStr, 0, testStr.length(), fpsf, roundMode, DECIMAL_MARK_ANY));
            DoubleHolder doubleHolder = new DoubleHolder();
            final boolean doubleParseOk = doubleTryParse(testStr, doubleHolder);
            assertEquals(Decimal64.NEGATIVE_INFINITY, value);
            assertEquals(fpsf.status, JavaImplParse.BID_INEXACT_EXCEPTION | JavaImplParse.BID_OVERFLOW_EXCEPTION);
            assertEquals(doubleParseOk, decimalParseOk(fpsf));
        }
        {
            final String testStr = "0.00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000005";
            JavaImplParse.FloatingPointStatusFlag fpsf = new JavaImplParse.FloatingPointStatusFlag();
            Decimal64 value = Decimal64.fromUnderlying(JavaImplParse.bid64_from_string(testStr, 0, testStr.length(), fpsf, roundMode, DECIMAL_MARK_ANY));
            DoubleHolder doubleHolder = new DoubleHolder();
            final boolean doubleParseOk = doubleTryParse(testStr, doubleHolder);
            assertEquals(Decimal64.ZERO, value);
            assertEquals(fpsf.status, JavaImplParse.BID_INEXACT_EXCEPTION | JavaImplParse.BID_UNDERFLOW_EXCEPTION);
            assertEquals(doubleParseOk, decimalParseOk(fpsf));
        }
        {
            final String testStr = "  123 x99  ";
            JavaImplParse.FloatingPointStatusFlag fpsf = new JavaImplParse.FloatingPointStatusFlag();
            Decimal64 value = Decimal64.fromUnderlying(JavaImplParse.bid64_from_string(testStr, 0, testStr.length(), fpsf, roundMode, DECIMAL_MARK_ANY));
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

    //@@@ @Test
    public void parseRoundingTest() {
        final String inStr = "0.0000000012664996872106725";
        final Decimal64 testValue = Decimal64.parse(inStr);
        final String testStr = testValue.toString();
        if (!testStr.endsWith("3"))
            throw new RuntimeException("Why the values is not rounded up?");
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

    @Test
    public void testRoundDiv() {
        assertFalse(Decimal64.parse("1").isRounded(-1));
        assertTrue(Decimal64.parse("1").isRounded(0));
        assertTrue(Decimal64.parse("1").isRounded(1));
        assertFalse(Decimal64.parse("1.23").isRounded(1));
        assertFalse(Decimal64.parse("1.23").isRounded(-10));
        assertTrue(Decimal64.parse("1.23").isRounded(2));
        assertTrue(Decimal64.parse("1.23").isRounded(3));
        assertFalse(Decimal64.parse("1.23456789").isRounded(-0));
        assertFalse(Decimal64.parse("1.23456789").isRounded(7));
        assertTrue(Decimal64.parse("1.23456789").isRounded(8));
        assertTrue(Decimal64.parse("1.23456789").isRounded(9));
        assertTrue(Decimal64.parse("123E10").isRounded(-9));
        assertTrue(Decimal64.parse("123E10").isRounded(-10));
        assertFalse(Decimal64.parse("123E10").isRounded(-11));
        assertFalse(Decimal64.parse("-10E-10").isRounded(8));
        assertTrue(Decimal64.parse("-10E-10").isRounded(9));
        assertTrue(Decimal64.parse("-10E-10").isRounded(10));
        assertTrue(Decimal64.parse("0").isRounded(-11));
        assertFalse(Decimal64.parse("Inf").isRounded(0));
        assertFalse(Decimal64.parse("NaN").isRounded(-11));
        assertFalse(Decimal64.parse("-Inf").isRounded(10));
    }

    // @Test
    public void precisionLossDocGen() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final String roundXXXName = "roundToNearestTiesToEven";
        final String valueStr = "0.00144";
        final int r = 55559375;
        final RoundingMode roundType = RoundingMode.HALF_EVEN;

        final Decimal64 value = Decimal64.parse(valueStr);
        final Decimal64 multiple = Decimal64.ONE.divideByInteger(r);


        final Method roundXXXMethod = Decimal64.class.getMethod(roundXXXName, Decimal64.class);
        final Decimal64 roundXXXValue = (Decimal64) roundXXXMethod.invoke(value, multiple);

        final Decimal64 roundToReciprocalValue = value.roundToReciprocal(r, roundType);

        final BigDecimal bigMultiple = BigDecimal.ONE.divide(new BigDecimal(r), MathContext.DECIMAL128);

        final BigDecimal bigDecimalMultiplierWay =
            value.toBigDecimal()
                .divide(bigMultiple, MathContext.DECIMAL128)
                .setScale(0, roundType)
                .multiply(bigMultiple);

        final BigDecimal bigDecimalReciprocalWay =
            value.toBigDecimal()
                .multiply(new BigDecimal(r))
                .setScale(0, roundType)
                .divide(new BigDecimal(r), MathContext.DECIMAL128);


        System.out.println("| Argument  | Value |");
        System.out.println("|-----------|-------|");
        System.out.println("| value     | " + valueStr + " or underlying `" + Decimal64.toUnderlying(value) + "L` |");
        System.out.println("| r         | " + r + "|");
        System.out.println("| multiple  | " + multiple + " or `" + Decimal64.toUnderlying(value) + "L` |");
        System.out.println("| roundType | RoundingMode." + roundType + " |");
        System.out.println("");
        System.out.println("| Equation                       | Result                  |");
        System.out.println("|--------------------------------|-------------------------|");
        System.out.println("| `" + roundXXXName + "` | " + roundXXXValue + " |");
        System.out.println("| `roundToReciprocal`            | " + roundToReciprocalValue + " |");
        System.out.println("| `bigDecimalMultiplierWay`      | " + bigDecimalMultiplierWay + " |");
        System.out.println("| `bigDecimalReciprocalWay`      | " + bigDecimalReciprocalWay + " |");
    }

    @Test
    public void testReciprocalCalculation() {
        assertEquals(25600, getExactReciprocal(Decimal64.parse("0.0000390625")));
        //assertEquals(6, getExactReciprocal(Decimal64.ONE.divideByInteger(6))); // Fail, because Decimal64 can't calculate 1/(1/6) without precision loss
    }

    /**
     * Converts positive multiple to the exact reciprocal, or return -1 if there is no exact integer representation.
     *
     * @param multiple Positive multiplier value.
     * @return Integer value, reciprocal to multiple, or -1.
     */
    public static int getExactReciprocal(final Decimal64 multiple) {
        if (!multiple.isPositive())
            throw new IllegalArgumentException("The multiple(=" + multiple + ") must be positive.");

        final int r = Decimal64.ONE.divide(multiple).toInt();

        return Decimal64.ONE.divideByInteger(r).equals(multiple) ? r : -1;
    }

    @Test
    public void unsignedReplacementTest() {
        assertEquals(0x0003ffffffffffffL >= 1000000000000000L, UnsignedLong.isGreaterOrEqual(0x0003ffffffffffffL, 1000000000000000L));
        assertEquals(0x0003ffffffffffffL < Long.MAX_VALUE, UnsignedLong.isLess(0x0003ffffffffffffL, Long.MAX_VALUE));

        final long sig_x = (-1 & MASK_BINARY_SIG2) | MASK_BINARY_OR2;
        assertEquals(sig_x > 9999999999999999L, UnsignedLong.isGreater(sig_x, 9999999999999999L));
        assertEquals(sig_x < Long.MAX_VALUE, UnsignedLong.isLess(sig_x, Long.MAX_VALUE));
    }

    @Test
    public void div10Test() {
        final long coefficient = Long.MAX_VALUE / FAST_DIV10_RECIPROCAL; // Critical point
        assertTrue(coefficient > Integer.MAX_VALUE);

        final long r = coefficient / 10;

        long p = coefficient * FAST_DIV10_RECIPROCAL;
        final long coefficient10 = p >> FAST_DIV10_SHIFT;

        assertEquals(r, coefficient10);
    }

    @Test
    public void mul0() {
        final long[] testValues = new long[]{
            0, 4, 5, 10, 1153, 1155,
            Integer.MAX_VALUE - 11, Integer.MAX_VALUE - 1,
            Integer.MAX_VALUE, 0x80000000L,
            0x80000001L, 0x80000007L,
            Long.MAX_VALUE - 13, Long.MAX_VALUE - 1,
            Long.MAX_VALUE, 0x8000000000000000L,
            0x8000000000000001L, 0x8000000000000011L,
            0xFFFFFFFFFFFFFFFCL, 0xFFFFFFFFFFFFFFFFL,
        };

        final boolean[] doOp = new boolean[]{false, true};

        for (final long au : testValues)
            for (final long bu : testValues)
                for (final boolean negA : doOp)
                    for (final boolean negB : doOp)
                        for (final boolean shrA : doOp)
                            for (final boolean shrB : doOp) {

                                final long a = prepareArg(au, negA, shrA);
                                final long b = prepareArg(bu, negB, shrB);

                                final long w1, w0;
                                {
                                    long __CX = a;
                                    long __CY = b;
                                    long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
                                    __CXH = __CX >>> 32;
                                    __CXL = LONG_LOW_PART & __CX;
                                    __CYH = __CY >>> 32;
                                    __CYL = LONG_LOW_PART & __CY;

                                    __PM = __CXH * __CYL;
                                    __PH = __CXH * __CYH;
                                    __PL = __CXL * __CYL;
                                    __PM2 = __CXL * __CYH;
                                    __PH += (__PM >>> 32);
                                    __PM = (LONG_LOW_PART & __PM) + __PM2 + (__PL >>> 32);

                                    w1 = __PH + (__PM >>> 32);
                                    w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
                                }
                                final BigInteger rOld = unsignedLongToBigInteger(w1).multiply(twoPow64).add(unsignedLongToBigInteger(w0));

                                final BigInteger ab = unsignedLongToBigInteger(a);
                                final BigInteger bb = unsignedLongToBigInteger(b);
                                final BigInteger rb = ab.multiply(bb);

                                if (!rb.equals(rOld))
                                    throw new RuntimeException("The case " + a + " * " + b + " result " + rb + " != " + rOld);

                                final long m1 = Mul64Impl.unsignedMultiplyHigh(a, b);
                                final long m0 = a * b;

                                if (w1 != m1 || w0 != m0)
                                    throw new RuntimeException("The case " + Long.toHexString(a) + " * " + Long.toHexString(b) + " result [" +
                                        Long.toHexString(w1) + ", " + Long.toHexString(w0) + "] != [" +
                                        Long.toHexString(m1) + ", " + Long.toHexString(m0) + "]");
                            }
    }

    private static long prepareArg(long x, final boolean negX, final boolean shrX) {
        if (negX)
            x = -x;
        if (shrX)
            x = x >>> 1;
        return x;
    }

    private static final BigInteger twoPow64 = unsignedLongToBigInteger(0x100000000L).multiply(unsignedLongToBigInteger(0x100000000L));

    private static BigInteger unsignedLongToBigInteger(long x) {
        final byte[] p = new byte[9];
        for (int i = 0; i < p.length; ++i, x = x >>> 8)
            p[p.length - 1 - i] = (byte) (x & 0xFF);

        return new BigInteger(p);
    }

    @Test
    public void issue84ToStringAsDouble() throws IOException {
        for (final double dbl : new double[]{1234, 12340, 0.01234, 1234.567, 0.0, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN}) {
            final String str = Double.toString(dbl);

            final Decimal64 d64 = Decimal64.parse(str);
            final long d64u = Decimal64.toUnderlying(d64);
            final Decimal64Parts dParts = toParts(d64u);

            for (int shift = 0, f = 1; shift < 6; ++shift, f *= 10) {
                final long d64up = shift == 0 ? d64u : JavaImplMul.get_BID64(dParts.signMask, dParts.exponent - shift, dParts.coefficient * f);

                assertEquals(str, Decimal64Utils.toString(d64up));

                {
                    final CharArrayWriter writer = new CharArrayWriter(1024);
                    Decimal64Utils.appendTo(d64up, writer);
                    assertEquals(str, writer.toString());
                }

                {
                    final StringBuilder sb = new StringBuilder();
                    Decimal64Utils.appendTo(d64up, sb);
                    assertEquals(str, sb.toString());
                }

                if (!d64.isFinite())
                    break;
            }
        }
    }

    @Test
    public void issue84ToStringAsDoublePart2() throws IOException {
        final long d64u = Decimal64Utils.parse("1234.0");
        final Decimal64Parts dParts = toParts(d64u);

        final long d64Up = JavaImplMul.get_BID64(dParts.signMask, dParts.exponent - 6 - 6, dParts.coefficient * 1000_000);
        final String strUp = "0.001234";

        assertEquals(strUp, Decimal64Utils.toString(d64Up));

        {
            final CharArrayWriter writer = new CharArrayWriter(1024);
            Decimal64Utils.appendTo(d64Up, writer);
            assertEquals(strUp, writer.toString());
        }

        {
            final StringBuilder sb = new StringBuilder();
            Decimal64Utils.appendTo(d64Up, sb);
            assertEquals(strUp, sb.toString());
        }
    }
}
