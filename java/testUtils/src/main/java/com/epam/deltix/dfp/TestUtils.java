package com.epam.deltix.dfp;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Assert;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import static com.epam.deltix.dfp.Decimal64Utils.MAX_SIGNIFICAND_DIGITS;
import static com.epam.deltix.dfp.Decimal64Utils.toDebugString;
import static org.junit.Assert.assertEquals;

public class TestUtils {
    static final int NTests = 10_000_000;

    @Decimal
    static final long[] specialValues = {
        0x2FEB29430A256D21L /*NativeImpl.fromFloat64(Math.PI)*/,
        0xAFE9A8434EC8E225L /*NativeImpl.fromFloat64(-Math.E)*/,
        Decimal64Utils.NaN,
        Decimal64Utils.NaN | 1000000000000000L,
        Decimal64Utils.negate(Decimal64Utils.NaN),
        Decimal64Utils.negate(Decimal64Utils.NaN | 1000000000000000L),
        Decimal64Utils.POSITIVE_INFINITY,
        Decimal64Utils.POSITIVE_INFINITY | 1000000000000000L,
        Decimal64Utils.NEGATIVE_INFINITY,
        Decimal64Utils.NEGATIVE_INFINITY | 1000000000000000L,
        Decimal64Utils.ZERO,
        JavaImplAdd.SPECIAL_ENCODING_MASK64 | 1000000000000000L,
        0x5740000000000000L /*NativeImpl.fromFixedPoint64(0L, -300)*/,
        0x0C40000000000000L /* NativeImpl.fromFixedPoint64(0L, 300)*/,
        0x5FE05AF3107A4000L /*NativeImpl.fromFixedPoint64(1L, Decimal64Utils.MIN_EXPONENT)*/,
        0x01C0000000000001L /*NativeImpl.fromFixedPoint64(1L, Decimal64Utils.MAX_EXPONENT)*/,
        Decimal64Utils.MIN_VALUE,
        Decimal64Utils.MAX_VALUE,
        Decimal64Utils.MIN_POSITIVE_VALUE,
        Decimal64Utils.MAX_NEGATIVE_VALUE,
        0x1L /*NativeImpl.fromFixedPoint64(1L, 398)*/,
        Decimal64Utils.ONE,
        0x2FE38D7EA4C68000L /*NativeImpl.fromFixedPoint64(10000000000000000L, 16)*/,
        0x31E38D7EA4C68000L /*NativeImpl.fromInt64(10000000000000000L)*/,
        Decimal64Utils.ONE | 0x7000000000000000L,
        Decimal64Utils.negate(Decimal64Utils.ONE | 0x7000000000000000L),
    };

    public interface Consumer<T> {
        void accept(T t);
    }

    public interface BiConsumer<T, U> {
        void accept(T t, U u);
    }

    public interface Function<T, R> {
        R apply(T t);
    }

    private static String composeMsg(Object a, String b) {
        String aStr;
        return a != null && !(aStr = a.toString()).equals("") ? aStr + ", " + Character.toLowerCase(b.charAt(0)) + b.substring(1) : b;
    }

    static void fail(long expected, long actual, Object message) {
        Assert.fail(String.format("%s: expected: %s, actual: %s", message, toDebugString(expected), toDebugString(actual)));
    }

    static void fail(Decimal64 expected, Decimal64 actual, Object message) {
        fail(expected.value, actual.value, message);
    }

    static void failWithDelta(long expected, long actual, Object message) {
        long delta = Decimal64Utils.subtract(actual, expected);
        Assert.fail(String.format("%s: expected=%s, actual=%s, delta=%s",
            message, toDebugString(expected), toDebugString(actual), toDebugString(delta)));
    }

    static void failWithDelta(Decimal64 expected, Decimal64 actual, Object message) {
        fail(expected.value, actual.value, message);
    }


    public static void assertDecimalIdentical(long expected, long actual, Object message) {
        if (expected != actual)
            fail(expected, actual, message);
    }

    public static void assertDecimalIdentical(long expected, long actual) {
        assertDecimalIdentical(expected, actual, "Values should be identical");
    }

    public static void assertDecimalIdentical(Decimal64 expected, Decimal64 actual, Object message) {
        assertDecimalIdentical(expected.value, actual.value, message);
    }

    public static void assertDecimalIdentical(Decimal64 expected, Decimal64 actual) {
        assertDecimalIdentical(expected.value, actual.value);
    }

    public static void assertDecimalNotIdentical(long expected, long actual, Object message) {
        if (expected == actual)
            fail(expected, actual, message);
    }

    public static void assertDecimalNotIdentical(long expected, long actual) {
        assertDecimalNotIdentical(expected, actual, "Values should be different");
    }

    public static void assertDecimalNotIdentical(Decimal64 expected, Decimal64 actual, Object message) {
        assertDecimalNotIdentical(expected.value, actual.value, message);
    }

    public static void assertDecimalNotIdentical(Decimal64 expected, Decimal64 actual) {
        assertDecimalNotIdentical(expected.value, actual.value);
    }

    public static void assertDecimalEqual(long expected, long actual, Object message) {
        if (!Decimal64Utils.equals(expected, actual))
            fail(expected, actual, message);
    }

    public static void assertDecimalEqual(long expected, long actual) {
        assertDecimalEqual(expected, actual, "Values should be equal");
    }

    public static void assertDecimalEqual(Decimal64 expected, Decimal64 actual, Object message) {
        assertDecimalEqual(expected.value, actual.value, message);
    }

    public static void assertDecimalEqual(Decimal64 expected, Decimal64 actual) {
        assertDecimalEqual(expected.value, actual.value);
    }

    public static void assertDecimalEqualNotIdentical(long expected, long actual, Object message) {
        assertDecimalEqual(expected, actual, composeMsg(message, "Values should be equal"));
        assertDecimalNotIdentical(expected, actual, composeMsg(message, "Values should be equal but different"));
    }

    public static void assertDecimalEqualNotIdentical(Decimal64 expected, Decimal64 actual, Object message) {
        assertDecimalNotIdentical(expected, actual);
    }

    public static void assertDecimalEqualHashCode(long expected, long actual, Object message, boolean expectEqual) {
        int hashCodeExpected = Decimal64Utils.hashCode(expected);
        int hashCodeActual = Decimal64Utils.hashCode(actual);
        if ((hashCodeExpected == hashCodeActual) != expectEqual) {
            fail(expected, actual, composeMsg(message,
                String.format("Hash codes should be %s (expected: %x != actual: %x)",
                    expectEqual ? "equal " : "different", hashCodeExpected, hashCodeActual)
            ));
        }
    }

    public static void assertDecimalEqualHashCode(Decimal64 expected, Decimal64 actual, Object message, boolean expectEqual) {
        assertDecimalEqualHashCode(expected.value, actual.value, message, expectEqual);
    }

    public static void assertDecimalEqualIdentityHashCode(long expected, long actual, Object message, boolean expectEqual) {
        int hashCodeExpected = Decimal64Utils.identityHashCode(expected);
        int hashCodeActual = Decimal64Utils.identityHashCode(actual);
        if ((hashCodeExpected == hashCodeActual) != expectEqual) {
            fail(expected, actual, composeMsg(message,
                String.format("Identity hash codes should be %s (expected: %x != actual: %x)",
                    expectEqual ? "equal " : "different", hashCodeExpected, hashCodeActual)
            ));
        }
    }

    public static void assertDecimalEqualIdentityHashCode(Decimal64 expected, Decimal64 actual, Object message, boolean expectEqual) {
        assertDecimalEqualIdentityHashCode(expected.value, actual.value, message, expectEqual);
    }

    public static long getRandomLong(int length) {
        long randomNum = POWERS_OF_TEN[length - 1] +
            ((long) (rng.nextDouble() * (POWERS_OF_TEN[length] - POWERS_OF_TEN[length - 1]))) + 1;
        if (randomNum % 10 == 0)
            --randomNum;
        return randomNum;
    }

    public static Decimal64 getRandomDecimal(long baseMantissa, long maxMantissa) {
        long mantissa = baseMantissa + (rng.nextLong() % maxMantissa);
        int exp = (rng.nextInt() & 127) - 64;
        return Decimal64.fromFixedPoint(mantissa, exp);
    }

    public static Decimal64 getRandomDecimal(long maxMantissa) {
        return getRandomDecimal(0, maxMantissa);
    }

    public static void mantissaZerosCombinations(BiConsumer<Long, Integer> func, int n) {
        for (int zerosLen = 1; zerosLen < 16; ++zerosLen) {
            long notZeroPart;

            for (int i = 1; i <= 16 - zerosLen; ++i) {
                for (int u = 0; u < n; ++u) {
                    notZeroPart = getRandomLong(i);
                    long mantissa = notZeroPart * POWERS_OF_TEN[zerosLen];
                    func.accept(mantissa, zerosLen);
                }
            }
        }
    }

    public static void partsCombinationsWithoutEndingZeros(BiConsumer<Long, Integer> func, int n) {

        for (int i = 1; i <= 16; ++i) {
            for (int u = 0; u < n; ++u) {
                long mantissa = getRandomLong(i);
                for (int exp = 398 - 0x2FF; exp <= 398; ++exp)
                    func.accept(mantissa, exp);
            }
        }
    }

    public static void mantissaZerosCombinations(BiConsumer<Long, Integer> func) {
        mantissaZerosCombinations(func, 1000);
    }

    public static void partsCombinationsWithoutEndingZeros(BiConsumer<Long, Integer> func) {
        partsCombinationsWithoutEndingZeros(func, 50);
    }

    public static void applyTo(Consumer<String> f, String... arrayOfArguments) {
        for (int i = 0, n = arrayOfArguments.length; i < n; i++) {
            f.accept(arrayOfArguments[i]);
        }
    }

    public static void applyToPairs(BiConsumer<String, String> f, String... arrayOfArgumentPairs) {
        for (int i = 0, n = arrayOfArgumentPairs.length; i < n; i += 2) {
            f.accept(arrayOfArgumentPairs[i], arrayOfArgumentPairs[i + 1]);
        }
    }

    /**
     * like. parse(), but also supports exponential representation with integer mantissa
     *
     * @param str
     * @return new Decimal64 as long
     */
    public static long decimalFromString(String str) {
        String[] parts = str.split("E");
        if (parts.length == 2)
            return Decimal64Utils.fromFixedPoint(Long.parseLong(parts[0]), -Integer.parseInt(parts[1]));

        return Decimal64Utils.parse(str);
    }

    private static void checkResult(String expectedStr, long result, String message) {
        assertEquals(message, expectedStr, Decimal64Utils.toString(result));
    }

    public static void checkFunction(Function<Long, Long> f, String expectedStr, String argStr, String message) {
        long arg = decimalFromString(argStr);
        checkResult(expectedStr, f.apply(arg), message);
    }

    public static void main(String[] args) {
        mantissaZerosCombinations(
            new BiConsumer<Long, Integer>() {
                @Override
                public void accept(Long a, Integer b) {
                    System.out.printf("%s %s\n", a, b);
                }
            }, 4);

        for (int i = 0; i < 100; i++) {
            System.out.printf("%s\n", getRandomLong(4));
        }
    }


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

    static final Random rng = new Random();

    public static void checkInMultipleThreads(final Runnable target) throws Exception {
        checkInMultipleThreads(Runtime.getRuntime().availableProcessors(), target);
    }

    public static void checkInMultipleThreads(final int threadsCount, final Runnable target) throws Exception {
        if (threadsCount < 1)
            throw new IllegalArgumentException("The threadsCount(=" + threadsCount + ") must be positive.");

        if (threadsCount == 1) {
            target.run();
        } else {
            final Thread[] threads = new Thread[threadsCount];
            final AtomicReference<Exception> lastException = new AtomicReference<>(null);

            for (int ti = 0; ti < threads.length; ++ti) {
                threads[ti] = new Thread(() -> {
                    try {
                        target.run();
                    } catch (final Exception e) {
                        lastException.set(e);
                    }
                });
                threads[ti].start();
            }

            for (final Thread thread : threads)
                thread.join();

            if (lastException.get() != null)
                throw lastException.get();
        }

    }

    public static class RandomDecimalsGenerator {
        final RandomGenerator generator;
        long x = Decimal64Utils.NaN;
        int xExp = 0;
        long y = Decimal64Utils.NaN;
        int yExp = 0;

        final int mantissaMaxShift;
        final int exponentRange;
        final int exponentOffset;

        private static final int TWICE_OF_MAX_SIGNIFICAND_DIGITS = MAX_SIGNIFICAND_DIGITS * 2;
        private static final int HALF_OF_MAX_SIGNIFICAND_DIGITS = MAX_SIGNIFICAND_DIGITS / 2;

        public RandomDecimalsGenerator() {
            this((int) System.nanoTime());
        }

        public RandomDecimalsGenerator(final int randomSeed) {
            this(new MersenneTwister(randomSeed), 1,
                -TWICE_OF_MAX_SIGNIFICAND_DIGITS - HALF_OF_MAX_SIGNIFICAND_DIGITS,
                TWICE_OF_MAX_SIGNIFICAND_DIGITS - HALF_OF_MAX_SIGNIFICAND_DIGITS);
        }

        public RandomDecimalsGenerator(
            final RandomGenerator generator,
            final int mantissaMinBits,
            final int exponentMin,
            final int exponentMax) {
            if (generator == null)
                throw new IllegalArgumentException("The random argument is null.");
            if (mantissaMinBits < 1 || 64 < mantissaMinBits)
                throw new IllegalArgumentException("The mantissaMinBits(=" + mantissaMinBits + ") must lie in [1..64] range");
            if (exponentMin < Decimal64Utils.MIN_EXPONENT || Decimal64Utils.MAX_EXPONENT < exponentMin)
                throw new IllegalArgumentException("The exponentMin(=" + exponentMin + ") must lie in [" +
                    Decimal64Utils.MIN_EXPONENT + ".." + Decimal64Utils.MAX_EXPONENT + "] range.");
            if (exponentMax < Decimal64Utils.MIN_EXPONENT || Decimal64Utils.MAX_EXPONENT < exponentMax)
                throw new IllegalArgumentException("The exponentMax(=" + exponentMax + ") must lie in [" +
                    Decimal64Utils.MIN_EXPONENT + ".." + Decimal64Utils.MAX_EXPONENT + "] range.");
            if (exponentMax <= exponentMin)
                throw new IllegalArgumentException("The exponentMin(=" + exponentMin +
                    ") must be less than the exponentMax(=" + exponentMax + ".");

            this.generator = generator;
            this.mantissaMaxShift = 64 - mantissaMinBits + 1 /*  for random.nextInt() exclusive upper bound */;
            this.exponentRange = exponentMax - exponentMin;
            this.exponentOffset = exponentMin;
        }

        public void makeNextPair() {
            nextX();
            nextY();
        }

        public long nextX() {
            xExp = generator.nextInt(exponentRange) + exponentOffset;
            return x = Decimal64Utils.fromFixedPoint(generator.nextLong() >> generator.nextInt(mantissaMaxShift), -xExp);
        }

        public long nextY() {
            yExp = generator.nextInt(exponentRange) + exponentOffset;
            return y = Decimal64Utils.fromFixedPoint(generator.nextLong() >> generator.nextInt(mantissaMaxShift), -yExp);
        }

        public long getX() {
            return x;
        }

        public int getXExp() {
            return xExp;
        }

        public long getY() {
            return y;
        }

        public int getYExp() {
            return yExp;
        }

        @Override
        public String toString() {
            return "RandomDecimalsGenerator{" +
                "x=" + Decimal64Utils.toScientificString(x) +
                ", xExp=" + xExp +
                ", y=" + Decimal64Utils.toScientificString(y) +
                ", yExp=" + yExp +
                '}';
        }
    }

    public static void checkWithCoverage(final TwoArgFn refFn, final TwoArgFn testFn) throws Exception {
        for (final long x : specialValues)
            for (final long y : specialValues)
                checkCase(x, y, refFn, testFn);

        checkInMultipleThreads(() -> {
            final RandomDecimalsGenerator random = new RandomDecimalsGenerator();
            for (int i = 0; i < NTests; ++i) {
                random.makeNextPair();
                checkCase(random.getX(), random.getY(), refFn, testFn);
            }
        });
    }

    public static void checkWithCoverage(final OneArgFn refFn, final OneArgFn testFn) throws Exception {
        for (final long x : specialValues)
            checkCase(x, refFn, testFn);

        checkInMultipleThreads(() -> {
            final MersenneTwister random = new MersenneTwister();
            for (int i = 0; i < NTests; ++i)
                checkCase(random.nextLong(), refFn, testFn);
        });
    }

    public static void checkEqualityWithCoverage(final OneArgFn refFn, final OneArgFn testFn) throws Exception {
        for (final long x : specialValues)
            checkEqualityCase(x, refFn, testFn);

        checkInMultipleThreads(() -> {
            final MersenneTwister random = new MersenneTwister();
            for (int i = 0; i < NTests; ++i)
                checkEqualityCase(random.nextLong(), refFn, testFn);
        });
    }

    public interface TwoArgFn {
        long apply(final long x, final long y);
    }

    public static void checkCase(final long x, final long y, final TwoArgFn refFn, final TwoArgFn testFn) {
        final long testRet = testFn.apply(x, y);
        final long refRet = refFn.apply(x, y);

        if (testRet != refRet)
            throw new RuntimeException("The function(0x" + Long.toHexString(x) + "L, 0x" + Long.toHexString(y) +
                "L) = 0x" + Long.toHexString(refRet) + "L, but test return 0x" + Long.toHexString(testRet) + "L");
    }

    public interface OneArgFn {
        long apply(final long x);
    }

    public static void checkEqualityCase(final long x, final OneArgFn refFn, final OneArgFn testFn) {
        final long testRet = testFn.apply(x);
        final long refRet = refFn.apply(x);

        if (testRet != refRet && Decimal64Utils.compareTo(testRet, refRet) != 0)
            throw new RuntimeException("The function(0x" + Long.toHexString(x) + "L) = 0x" +
                Long.toHexString(refRet) + "L, but test return 0x" + Long.toHexString(testRet) + "L");
    }

    public static void checkCase(final long x, final OneArgFn refFn, final OneArgFn testFn) {
        final long testRet = testFn.apply(x);
        final long refRet = refFn.apply(x);

        if (testRet != refRet)
            throw new RuntimeException("The function(0x" + Long.toHexString(x) + "L) = 0x" +
                Long.toHexString(refRet) + "L, but test return 0x" + Long.toHexString(testRet) + "L");
    }

    public static void checkCases(final OneArgFn refFn, final OneArgFn testFn, final long... x) {
        for (final long testValue : x)
            checkCase(testValue, refFn, testFn);
    }

    public static void checkCases(final TwoArgFn refFn, final TwoArgFn testFn, final long... xy) {
        for (int i = 0; i < xy.length; i += 2)
            checkCase(xy[i], xy[i + 1], refFn, testFn);
    }

    public static int getUnbiasedExponent(final long value) {
        return JavaImpl.toParts(value).getUnbiasedExponent();
    }
}
