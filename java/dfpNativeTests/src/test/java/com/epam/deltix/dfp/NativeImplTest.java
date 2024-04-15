package com.epam.deltix.dfp;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.Random;

import static com.epam.deltix.dfp.JavaImpl.*;
import static com.epam.deltix.dfp.TestUtils.*;
import static com.epam.deltix.dfp.TestUtils.POWERS_OF_TEN;
import static org.junit.Assert.*;

public class NativeImplTest {
    @Test
    public void fromInt32Advanced() {
        final Random random = new SecureRandom();
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
    public void fromFixedPointFast() {
        final Random random = new SecureRandom();
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
    public void fromInt64() {

        assertDecimalEqual(JavaImpl.ZERO, NativeImpl.fromInt64(0));
        assertDecimalIdentical(JavaImpl.ZERO, NativeImpl.fromInt64(0));

        assertDecimalIdentical(Decimal64Utils.ONE, NativeImpl.fromInt64(1));
        assertDecimalIdentical(Decimal64Utils.MILLION, NativeImpl.fromInt64(1000000));
        assertDecimalIdentical(Decimal64Utils.fromDouble(-1000000.0), NativeImpl.fromInt64(-1000000));
    }

    /**
     * Test binary and numeric values of the constants defined in Decimal64Utils
     * Decimal64Utils static constructor and tests are supposed to only be dependent from JavaImpl class, not NativeImpl
     * and therefore we want a separate test to verify the validity of its constants that invoked NativeImpl
     */
    @Test
    public void testConstants() {

        assertDecimalIdentical(NativeImpl.fromInt64(0), Decimal64Utils.ZERO);
        assertDecimalIdentical(NativeImpl.fromInt64(1), Decimal64Utils.ONE);
        assertDecimalIdentical(NativeImpl.fromInt64(2), Decimal64Utils.TWO);
        assertDecimalIdentical(NativeImpl.fromInt64(10), Decimal64Utils.TEN);
        assertDecimalIdentical(NativeImpl.fromInt64(100), Decimal64Utils.HUNDRED);
        assertDecimalIdentical(NativeImpl.fromInt64(1000), Decimal64Utils.THOUSAND);
        assertDecimalIdentical(NativeImpl.fromInt64(1000_000), Decimal64Utils.MILLION);
        assertDecimalIdentical(NativeImpl.fromFixedPoint64(1, 1), Decimal64Utils.ONE_TENTH);
        assertDecimalIdentical(NativeImpl.fromFixedPoint64(1, 2), Decimal64Utils.ONE_HUNDREDTH);

        assertDecimalEqual(NativeImpl.fromFloat64(0), Decimal64Utils.ZERO);
        assertDecimalEqual(NativeImpl.fromFloat64(1), Decimal64Utils.ONE);
        assertDecimalEqual(NativeImpl.fromFloat64(2), Decimal64Utils.TWO);
        assertDecimalEqual(NativeImpl.fromFloat64(10), Decimal64Utils.TEN);
        assertDecimalEqual(NativeImpl.fromFloat64(100), Decimal64Utils.HUNDRED);
        assertDecimalEqual(NativeImpl.fromFloat64(1000), Decimal64Utils.THOUSAND);
        assertDecimalEqual(NativeImpl.fromFloat64(1000_000), Decimal64Utils.MILLION);
        assertDecimalEqual(NativeImpl.fromFloat64(0.1), Decimal64Utils.ONE_TENTH);
        assertDecimalEqual(NativeImpl.fromFloat64(0.01), Decimal64Utils.ONE_HUNDREDTH);
    }

    @Test
    public void unCanonizedRound() {
        final long zeroU = 0x2FE0000000000000L;
        final long f = 0x2F638D7EA4C68000L; // 0.0001
        final long zeroP = Decimal64Utils.multiply(zeroU, f);

        assertDecimalEqual(Decimal64Utils.ZERO, NativeImpl.roundTowardsPositiveInfinity(zeroP));
        assertDecimalEqual(Decimal64Utils.ZERO, Decimal64Utils.ceil(zeroP));
    }

    @Test
    public void issue89FromFloatVsDouble() {
        final float x = 3.15f;
        final Decimal64 fd64 = Decimal64.fromFloat(x);
        final Decimal64 fd64d = Decimal64.fromDecimalFloat(x);
        Decimal64Utils.fromDouble(x);
//        In Java (double) == 3.1500000953674316, so the only way to convert value correctly - use string conversion.
//        But the string conversion could be slow and also allocate memory.
    }

    @Test
    public void issue89FromFloat() throws Exception {
        checkWithCoverage(NativeImpl::fromFloat32, Decimal64Utils::fromFloat);
    }

    @Test
    public void fromDecimalDoubleVsString() throws Exception {
        for (final long x : specialValues)
            checkDecimalDoubleVsStringCase(x);

        checkInMultipleThreads(() -> {
            final MersenneTwister random = new MersenneTwister();
            for (int i = 0; i < NTests; ++i)
                checkDecimalDoubleVsStringCase(random.nextDouble());
        });
    }

    private static void checkDecimalDoubleVsStringCase(final double x) {
        final long testRet = Decimal64Utils.fromDecimalDouble(x);
        final long refRet = Decimal64Utils.parse(Double.toString(x));

        if (!Decimal64Utils.equals(testRet, refRet))
            throw new RuntimeException("The function(" + x + " = 0x" + Long.toHexString(Double.doubleToRawLongBits(x)) +
                "L) return " + Decimal64Utils.toScientificString(testRet) + " = 0x" + Long.toHexString(testRet) +
                "L instead of " + Decimal64Utils.toScientificString(refRet) + " = 0x" + Long.toHexString(refRet) + "L");
    }

    @Test
    public void testParserRounding() {
        final double dbl = Double.longBitsToDouble(0x3fd962c0b1334d10L);
        final String dblStr = Double.toString(dbl);

        final Decimal64 d64Parse = Decimal64.parse(dblStr);
        final Decimal64 d64Double = Decimal64.fromDouble(dbl);
        final Decimal64 d64DecimalDouble = Decimal64.fromDouble(dbl);

        assertEquals(d64Parse, d64Double);
        assertEquals(d64Parse, d64DecimalDouble);
    }
}
