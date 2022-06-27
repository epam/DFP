package com.epam.deltix.dfpmath;

import com.epam.deltix.dfp.Decimal64Utils;
import com.epam.deltix.dfp.TestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.apache.commons.math3.special.Erf;
import org.apache.commons.math3.special.Gamma;

import com.epam.deltix.utilities.*;
import java.security.SecureRandom;
import java.util.Random;

import static com.epam.deltix.dfp.TestUtils.*;
import static com.epam.deltix.dfpmath.Decimal64MathUtils.*;
import static com.epam.deltix.dfp.Decimal64Utils.*;

public class MathTest {
    private static final int N = 10_000;

    private static int getRandomSeed() {
        return 42; // new SecureRandom().nextLong();
    }

    static final double log2 = Math.log(2.0);

    static double doubleDiff(final double refValue, final double testValue) {
        return Math.log(Math.abs(refValue - testValue) /
            Math.max(Math.ulp(refValue), Math.ulp(testValue))) / log2;
    }

    static class TestCaseData {
        public Random random;
        public double rnd01;
        public double doubleOut;
        public long decimalOut;
    }

    static void testMathPrecision(final long N, final TestUtils.Consumer<TestCaseData> testFn, final double maxAcceptableError) {
        if (N < 0)
            throw new IllegalArgumentException("The N(=" + N + ") must be positive.");

        final long randomSeed = getRandomSeed();
        final Random random = new Random(randomSeed);

        double maxError = Double.NEGATIVE_INFINITY;
        long maxErrorIteration = -1;
        double maxErrorRnd01 = Double.NaN;
        double maxErrorDoubleOut = Double.NaN;
        double maxErrorDecimalOut = Double.NaN;
        long maxErrorDecimalLong = 0;

        final TestCaseData testData = new TestCaseData();
        testData.random = random;

        for (long i = 0; i < N; ++i) {
            final double rnd01 = random.nextDouble();

            testData.rnd01 = rnd01;
            testFn.accept(testData);
            final double decimalOut = toDouble(testData.decimalOut);
            double error = doubleDiff(testData.doubleOut, decimalOut);

            if (maxError < error) {
                maxError = error;
                maxErrorIteration = i;
                maxErrorRnd01 = rnd01;
                maxErrorDoubleOut = testData.doubleOut;
                maxErrorDecimalOut = decimalOut;
                maxErrorDecimalLong = testData.decimalOut;
            }
        }

        if (maxError > maxAcceptableError)
            throw new RuntimeException(String.format(
                "[RandomSeed=%d][Iteration=%d][Rnd01=%.17e] The error(=%g) between doubleValue(=%.17e) and decimalValue(=%.17e == %dL)" +
                    " is greater than maxAcceptableError(=%g).",
                randomSeed, maxErrorIteration, maxErrorRnd01, maxError, maxErrorDoubleOut, maxErrorDecimalOut, maxErrorDecimalLong,
                maxAcceptableError));

        if (Math.ceil(maxError) < maxAcceptableError) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            final String callerName = stackTrace.length > 2 ? stackTrace[2].getMethodName() + ": " : "";
            System.out.println(callerName + "The maxAcceptableError(=" + maxAcceptableError + ") looks to high for maxError(=" +
                maxError + ") estimated on " + N + " iterations.");
        }
    }

    @Test
    public void testExp() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 200;
                testCaseData.doubleOut = Math.exp(x);
                testCaseData.decimalOut = exp(fromDouble(x));
            }
        }, 6);
    }

    @Test
    public void testExp2() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 200;
                testCaseData.doubleOut = Math.pow(2.0, x);
                testCaseData.decimalOut = exp2(fromDouble(x));
            }
        }, 6);
    }

    @Test
    public void testExp10() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 200;
                testCaseData.doubleOut = Math.pow(10.0, x);
                testCaseData.decimalOut = exp10(fromDouble(x));
            }
        }, 7);
    }

    @Test
    public void testExpm1() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 200;
                testCaseData.doubleOut = Math.exp(x) - 1;
                testCaseData.decimalOut = expm1(fromDouble(x));
            }
        }, 18);
    }

    @Test
    public void testLog() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = testCaseData.rnd01 * 1e+100;
                testCaseData.doubleOut = Math.log(x);
                testCaseData.decimalOut = log(fromDouble(x));
            }
        }, 1);
    }

    @Test
    public void testLog2() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = testCaseData.rnd01 * 1e+100;
                testCaseData.doubleOut = Math.log(x) / log2;
                testCaseData.decimalOut = log2(fromDouble(x));
            }
        }, 1);
    }

    @Test
    public void testLog10() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = testCaseData.rnd01 * 1e+100;
                testCaseData.doubleOut = Math.log10(x);
                testCaseData.decimalOut = log10(fromDouble(x));
            }
        }, 0);
    }

    @Test
    public void testLog1p() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = testCaseData.rnd01 * 1e+100;
                testCaseData.doubleOut = Math.log1p(x);
                testCaseData.decimalOut = log1p(fromDouble(x));
            }
        }, 1);
    }

    @Test
    public void testPow() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 6;
                final double y = (testCaseData.random.nextDouble() - 0.5) * 200;
                testCaseData.doubleOut = Math.pow(x, y);
                testCaseData.decimalOut = pow(fromDouble(x), fromDouble(y));
            }
        }, 10);
    }

    @Test
    public void testFmod() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 1e+10;
                final double y = (testCaseData.random.nextDouble() - 0.5) * 1e+10;
                testCaseData.doubleOut = x % y;
                testCaseData.decimalOut = fmod(fromDouble(x), fromDouble(y));
            }
        }, 30);
    }

    @Test
    public void testHypot() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 1e+10;
                final double y = (testCaseData.random.nextDouble() - 0.5) * 1e+10;
                testCaseData.doubleOut = Math.hypot(x, y);
                testCaseData.decimalOut = hypot(fromDouble(x), fromDouble(y));
            }
        }, 4);
    }

    @Test
    public void testSin() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 1e+10;
                testCaseData.doubleOut = Math.sin(x);
                testCaseData.decimalOut = sin(fromDouble(x));
            }
        }, 54);
    }

    @Test
    public void testCos() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 1e+10;
                testCaseData.doubleOut = Math.cos(x);
                testCaseData.decimalOut = cos(fromDouble(x));
            }
        }, 54);
    }

    @Test
    public void testTan() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 1e+10;
                testCaseData.doubleOut = Math.tan(x);
                testCaseData.decimalOut = tan(fromDouble(x));
            }
        }, 54);
    }

    @Test
    public void testAsin() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 2;
                testCaseData.doubleOut = Math.asin(x);
                testCaseData.decimalOut = asin(fromDouble(x));
            }
        }, 11);
    }

    @Test
    public void testAcos() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 2;
                testCaseData.doubleOut = Math.acos(x);
                testCaseData.decimalOut = acos(fromDouble(x));
            }
        }, 25);
    }

    @Test
    public void testAtan() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 10;
                testCaseData.doubleOut = Math.atan(x);
                testCaseData.decimalOut = atan(fromDouble(x));
            }
        }, 3);
    }

    @Test
    public void testAtan2() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = testCaseData.rnd01 * 1e+10;
                final double y = testCaseData.random.nextDouble() * 1e+10;
                testCaseData.doubleOut = Math.atan2(x, y);
                testCaseData.decimalOut = atan2(fromDouble(x), fromDouble(y));
            }
        }, 4);
    }

    @Test
    public void testSinh() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 20;
                testCaseData.doubleOut = Math.sinh(x);
                testCaseData.decimalOut = sinh(fromDouble(x));
            }
        }, 4);
    }

    @Test
    public void testCosh() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 20;
                testCaseData.doubleOut = Math.cosh(x);
                testCaseData.decimalOut = cosh(fromDouble(x));
            }
        }, 4);
    }

    @Test
    public void testTanh() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 20;
                testCaseData.doubleOut = Math.tanh(x);
                testCaseData.decimalOut = tanh(fromDouble(x));
            }
        }, 3);
    }

    @Test
    public void testAsinh() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 1e+10;
                testCaseData.doubleOut = Math.log(x + Math.sqrt(x * x + 1));
                testCaseData.decimalOut = asinh(fromDouble(x));
            }
        }, 49);
    }

    @Test
    public void testAcosh() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = testCaseData.rnd01 * 1e+10 + 1;
                testCaseData.doubleOut = Math.log(x + Math.sqrt(x * x - 1));
                testCaseData.decimalOut = acosh(fromDouble(x));
            }
        }, 2);
    }

    @Test
    public void testAtanh() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 2;
                testCaseData.doubleOut = 0.5 * Math.log((1 + x) / (1 - x));
                testCaseData.decimalOut = atanh(fromDouble(x));
            }
        }, 25);
    }

    @Test
    public void testErf() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 100;
                testCaseData.doubleOut = Erf.erf(x);
                testCaseData.decimalOut = erf(fromDouble(x));
            }
        }, 5);
    }

    @Test
    public void testErfc() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 100;
                testCaseData.doubleOut = Erf.erfc(x);
                testCaseData.decimalOut = erfc(fromDouble(x));
            }
        }, 12);
    }

    @Test
    public void testTgamma() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 10;
                testCaseData.doubleOut = Gamma.gamma(x);
                testCaseData.decimalOut = tgamma(fromDouble(x));
            }
        }, 27);
    }

    @Test
    public void testLgamma() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 10;
                testCaseData.doubleOut = Gamma.logGamma(x);
                testCaseData.decimalOut = lgamma(fromDouble(x));
            }
        }, 26);
    }

    @Test
    public void testSqrt() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = testCaseData.rnd01 * 1e+10;
                testCaseData.doubleOut = Math.sqrt(x);
                testCaseData.decimalOut = sqrt(fromDouble(x));
            }
        }, 3);
    }

    @Test
    public void testCbrt() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = testCaseData.rnd01 * 1e+10;
                testCaseData.doubleOut = Math.cbrt(x);
                testCaseData.decimalOut = cbrt(fromDouble(x));
            }
        }, 3);
    }

    @Test
    public void testAdd() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 10;
                final double y = (testCaseData.random.nextDouble() - 0.5) * 10;
                testCaseData.doubleOut = x + y;
                testCaseData.decimalOut = add(fromDouble(x), fromDouble(y));
            }
        }, 26);
    }

    @Test
    public void testSub() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 10;
                final double y = (testCaseData.random.nextDouble() - 0.5) * 10;
                testCaseData.doubleOut = x - y;
                testCaseData.decimalOut = sub(fromDouble(x), fromDouble(y));
            }
        }, 26);
    }

    @Test
    public void testMul() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 1e+10;
                final double y = (testCaseData.random.nextDouble() - 0.5) * 1e+10;
                testCaseData.doubleOut = x * y;
                testCaseData.decimalOut = mul(fromDouble(x), fromDouble(y));
            }
        }, 4);
    }

    @Test
    public void testDiv() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 1e+10;
                final double y = (testCaseData.random.nextDouble() - 0.5) * 1e+10;
                testCaseData.doubleOut = x / y;
                testCaseData.decimalOut = div(fromDouble(x), fromDouble(y));
            }
        }, 4);
    }

    @Test
    public void testFma() {
        testMathPrecision(N, new TestUtils.Consumer<TestCaseData>() {
            @Override
            public void accept(TestCaseData testCaseData) {
                final double x = (testCaseData.rnd01 - 0.5) * 1e+10;
                final double y = (testCaseData.random.nextDouble() - 0.5) * 1e+10;
                final double z = (testCaseData.random.nextDouble() - 0.5) * 1e+10;
                testCaseData.doubleOut = x * y + z;
                testCaseData.decimalOut = fma(fromDouble(x), fromDouble(y), fromDouble(z));
            }
        }, 4);
    }

    @Test
    public void testQuietGreaterUnordered() {
        final long a = fromLong(5);
        final long b = fromLong(-10);
        final long n = NaN;

        Assert.assertEquals(isGreater(a, b), isGreaterUnordered(a, b));
        Assert.assertEquals(isGreater(b, a), isGreaterUnordered(b, a));
        Assert.assertTrue(isGreaterUnordered(a, n));
        Assert.assertTrue(isGreaterUnordered(n, a));
        Assert.assertTrue(isGreaterUnordered(b, n));
        Assert.assertTrue(isGreaterUnordered(n, b));
    }

    @Test
    public void testQuietLessUnordered() {
        final long a = fromLong(5);
        final long b = fromLong(-10);
        final long n = NaN;

        Assert.assertEquals(isLess(a, b), isLessUnordered(a, b));
        Assert.assertEquals(isLess(b, a), isLessUnordered(b, a));
        Assert.assertTrue(isLessUnordered(a, n));
        Assert.assertTrue(isLessUnordered(n, a));
        Assert.assertTrue(isLessUnordered(b, n));
        Assert.assertTrue(isLessUnordered(n, b));
    }

    @Test
    public void testNotGreater() {
        final long a = fromLong(5);
        final long b = fromLong(-10);
        final long n = NaN;

        Assert.assertEquals(isLessOrEqual(a, b), isNotGreater(a, b));
        Assert.assertEquals(isLessOrEqual(b, a), isNotGreater(b, a));
        Assert.assertTrue(isNotGreater(a, n));
        Assert.assertTrue(isNotGreater(n, a));
        Assert.assertTrue(isNotGreater(b, n));
        Assert.assertTrue(isNotGreater(n, b));
    }

    @Test
    public void testNotLess() {
        final long a = fromLong(5);
        final long b = fromLong(-10);
        final long n = NaN;

        Assert.assertEquals(isGreaterOrEqual(a, b), isNotLess(a, b));
        Assert.assertEquals(isGreaterOrEqual(b, a), isNotLess(b, a));
        Assert.assertTrue(isNotLess(a, n));
        Assert.assertTrue(isNotLess(n, a));
        Assert.assertTrue(isNotLess(b, n));
        Assert.assertTrue(isNotLess(n, b));
    }

    @Test
    public void testOrdered() {
        final long a = fromLong(5);
        final long b = fromLong(-10);
        final long n = NaN;

        Assert.assertTrue(isOrdered(a, b));
        Assert.assertFalse(isOrdered(a, n));
        Assert.assertFalse(isOrdered(n, a));
    }

    @Test
    public void testUnordered() {
        final long a = fromLong(5);
        final long b = fromLong(-10);
        final long n = NaN;

        Assert.assertFalse(isUnordered(a, b));
        Assert.assertTrue(isUnordered(a, n));
        Assert.assertTrue(isUnordered(n, a));
    }

    private static final String[] roundInput = {
        "0.1", "0.5", "0.9", // 0
        "1.1", "1.5", "1.9", // 1
        "2.1", "2.5", "2.9", // 2
        "3.1", "3.5", "3.9", // 3
        "-0.1", "-0.5", "-0.9", // -0
        "-1.1", "-1.5", "-1.9", // -1
        "-2.1", "-2.5", "-2.9", // -2
        "-3.1", "-3.5", "-3.9"}; // -2

    private static void testRoundFunc(final String[] refRound, final TestUtils.Function<Long, Long> roundFunc) {
        Assert.assertEquals(roundInput.length, refRound.length);

        for (int i = 0; i < roundInput.length; ++i) {
            final long roundedValue = roundFunc.apply(parse(roundInput[i]));
            if (!Decimal64Utils.equals(roundedValue, parse(refRound[i])))
                throw new RuntimeException("The " + roundInput[i] + " rounded to " +
                    Decimal64Utils.toString(roundedValue) + " instead of " + refRound[i]);
        }
    }

    @Test
    public void testRoundIntegralExact() {
        testRoundFunc(new String[]{
            "0", "0", "1", // 0
            "1", "2", "2", // 1
            "2", "2", "3", // 2
            "3", "4", "4", // 3
            "0", "0", "-1", // -0
            "-1", "-2", "-2", // -1
            "-2", "-2", "-3", // -2
            "-3", "-4", "-4" // -3
        }, new TestUtils.Function<Long, Long>() {
            @Override
            public Long apply(Long aLong) {
                return roundIntegralExact(aLong);
            }
        });
    }

    @Test
    public void testIntegralNearestEven() {
        testRoundFunc(new String[]{
            "0", "0", "1", // 0
            "1", "2", "2", // 1
            "2", "2", "3", // 2
            "3", "4", "4", // 3
            "0", "0", "-1", // -0
            "-1", "-2", "-2", // -1
            "-2", "-2", "-3", // -2
            "-3", "-4", "-4" // -3
        }, new TestUtils.Function<Long, Long>() {
            @Override
            public Long apply(Long aLong) {
                return roundIntegralNearestEven(aLong);
            }
        });
    }

    @Test
    public void testIntegralNegative() {
        testRoundFunc(new String[]{
            "0", "0", "0", // 0
            "1", "1", "1", // 1
            "2", "2", "2", // 2
            "3", "3", "3", // 3
            "-1", "-1", "-1", // -0
            "-2", "-2", "-2", // -1
            "-3", "-3", "-3", // -2
            "-4", "-4", "-4" // -3
        }, new TestUtils.Function<Long, Long>() {
            @Override
            public Long apply(Long aLong) {
                return roundIntegralNegative(aLong);
            }
        });
    }

    @Test
    public void testIntegralPositive() {
        testRoundFunc(new String[]{
            "1", "1", "1", // 0
            "2", "2", "2", // 1
            "3", "3", "3", // 2
            "4", "4", "4", // 3
            "0", "0", "0", // -0
            "-1", "-1", "-1", // -1
            "-2", "-2", "-2", // -2
            "-3", "-3", "-3" // -3
        }, new TestUtils.Function<Long, Long>() {
            @Override
            public Long apply(Long aLong) {
                return roundIntegralPositive(aLong);
            }
        });
    }

    @Test
    public void testIntegralZero() {
        testRoundFunc(new String[]{
            "0", "0", "0", // 0
            "1", "1", "1", // 1
            "2", "2", "2", // 2
            "3", "3", "3", // 3
            "0", "0", "0", // -0
            "-1", "-1", "-1", // -1
            "-2", "-2", "-2", // -2
            "-3", "-3", "-3" // -3
        }, new TestUtils.Function<Long, Long>() {
            @Override
            public Long apply(Long aLong) {
                return roundIntegralZero(aLong);
            }
        });
    }

    @Test
    public void testIntegralNearestAway() {
        testRoundFunc(new String[]{
            "0", "1", "1", // 0
            "1", "2", "2", // 1
            "2", "3", "3", // 2
            "3", "4", "4", // 3
            "0", "-1", "-1", // -0
            "-1", "-2", "-2", // -1
            "-2", "-3", "-3", // -2
            "-3", "-4", "-4" // -3
        }, new TestUtils.Function<Long, Long>() {
            @Override
            public Long apply(Long aLong) {
                return roundIntegralNearestAway(aLong);
            }
        });
    }

    @Test
    public void testNextAfter() {
        final long x = parse("3.14");
        assertDecimalEqual(nextUp(x), nextAfter(x, MILLION));
        assertDecimalEqual(nextDown(x), nextAfter(x, negate(MILLION)));
    }

    @Test
    public void testMinNum() {
        final long a = parse("5");
        final long b = parse("-10");
        assertDecimalEqual(b, minNum(a, b));
    }

    @Test
    public void testMinNumMag() {
        final long a = parse("5");
        final long b = parse("-10");
        assertDecimalEqual(a, minNumMag(a, b));
    }

    @Test
    public void testMaxNum() {
        final long a = parse("5");
        final long b = parse("-10");
        assertDecimalEqual(a, maxNum(a, b));
    }

    @Test
    public void testMaxNumMag() {
        final long a = parse("5");
        final long b = parse("-10");
        assertDecimalEqual(b, maxNumMag(a, b));
    }

    @Test
    public void testFromInt32() {
        final Random random = new Random(getRandomSeed());
        for (int i = 0; i < N; ++i) {
            final int rv = random.nextInt();
            assertDecimalEqual(parse(Integer.toString(rv)), fromInt32(rv), "RandomValue=" + rv);
        }
    }

    @Test
    public void testFromUInt32() {
        final Random random = new Random(getRandomSeed());
        for (int i = 0; i < N; ++i) {
            final int rv = Math.abs(random.nextInt());
            assertDecimalEqual(parse(Integer.toString(rv)), fromUInt32(rv), "RandomValue=" + rv);
        }
    }

    @Test
    public void testFromInt64() {
        final Random random = new Random(getRandomSeed());
        for (int i = 0; i < N; ++i) {
            final long rv = (long) ((random.nextDouble() - 0.5) * 2 * 9999999999999999L);
            assertDecimalEqual(parse(Long.toString(rv)), fromInt64(rv), "RandomValue=" + rv);
        }
    }

    @Test
    public void testFromUInt64() {
        final Random random = new Random(getRandomSeed());
        for (int i = 0; i < N; ++i) {
            final long rv = (long) (random.nextDouble() * 9999999999999999L);
            assertDecimalEqual(parse(Long.toString(rv)), fromUInt64(rv), "RandomValue=" + rv);
        }
    }

    @Test
    public void testIsSigned() {
        final Random random = new Random(getRandomSeed());
        for (int i = -100; i < 100; ++i) {
            final int rv = random.nextInt();
            Assert.assertEquals(isSigned(fromInt32(rv)), rv < 0);
        }
    }

    @Test
    public void testIsNormal() {
        Assert.assertTrue(isNormal(parse("3.14")));
        Assert.assertTrue(isNormal(parse("-3.14")));

        Assert.assertFalse(isNormal(ZERO));

        Assert.assertFalse(isNormal(NaN));
        Assert.assertFalse(isNormal(POSITIVE_INFINITY));
        Assert.assertFalse(isNormal(NEGATIVE_INFINITY));
    }

    @Test
    public void testIsSubnormal() {
        Assert.assertFalse(isSubnormal(parse("3.14")));
        Assert.assertFalse(isSubnormal(parse("-3.14")));

        Assert.assertFalse(isSubnormal(ZERO));

        Assert.assertFalse(isSubnormal(NaN));
        Assert.assertFalse(isSubnormal(POSITIVE_INFINITY));
        Assert.assertFalse(isSubnormal(NEGATIVE_INFINITY));
    }

    @Test
    public void testIsInf() {
        Assert.assertFalse(isInf(parse("3.14")));
        Assert.assertFalse(isInf(parse("-3.14")));

        Assert.assertFalse(isInf(ZERO));

        Assert.assertFalse(isInf(NaN));
        Assert.assertTrue(isInf(POSITIVE_INFINITY));
        Assert.assertTrue(isInf(NEGATIVE_INFINITY));
    }

    @Test
    public void testIsCanonical() {
        Assert.assertTrue(isCanonical(parse("3.14")));
        Assert.assertTrue(isCanonical(parse("-3.14")));

        Assert.assertTrue(isCanonical(ZERO));

        Assert.assertTrue(isCanonical(NaN));
        Assert.assertTrue(isCanonical(POSITIVE_INFINITY));
        Assert.assertTrue(isCanonical(NEGATIVE_INFINITY));
    }

    @Test
    public void testCopySign() {
        final Random random = new Random(getRandomSeed());
        for (int i = 0; i < N; ++i) {
            final long x = random.nextInt();
            final long y = random.nextInt();
            final long z = y >= 0 ? Math.abs(x) : -Math.abs(x);
            assertDecimalEqual(fromInt64(z),
                copySign(fromInt64(x), fromInt64(y)),
                "x=" + x + "; y=" + y + "; z=" + z);
        }
    }

    @Test
    public void testClassOfValue() {
        Assert.assertEquals(classOfValue(parse("3.14")), 8);
        Assert.assertEquals(classOfValue(parse("-3.14")), 3);

        Assert.assertEquals(classOfValue(ZERO), 6);

        Assert.assertEquals(classOfValue(NaN), 1);
        Assert.assertEquals(classOfValue(POSITIVE_INFINITY), 9);
        Assert.assertEquals(classOfValue(NEGATIVE_INFINITY), 2);
    }

    @Test
    public void testIsSameQuantum() {
        Assert.assertTrue(isSameQuantum(fromInt32(10), fromInt32(100)));
        Assert.assertFalse(isSameQuantum(fromInt32(10), parse("1e+1")));
    }

    @Test
    public void testIsTotalOrder() {
        final long x = fromInt32(5);
        final long y = fromInt32(-10);
        Assert.assertFalse(isTotalOrder(x, y));
        Assert.assertTrue(isTotalOrder(y, x));
        Assert.assertTrue(isTotalOrder(x, POSITIVE_INFINITY));
        Assert.assertTrue(isTotalOrder(NEGATIVE_INFINITY, x));
        Assert.assertTrue(isTotalOrder(NEGATIVE_INFINITY, NaN));
        Assert.assertTrue(isTotalOrder(POSITIVE_INFINITY, NaN));
    }

    @Test
    public void testIsTotalOrderMag() {
        final long x = fromInt32(5);
        final long y = fromInt32(-10);
        Assert.assertTrue(isTotalOrderMag(x, y));
        Assert.assertFalse(isTotalOrderMag(y, x));
        Assert.assertTrue(isTotalOrderMag(x, POSITIVE_INFINITY));
        Assert.assertFalse(isTotalOrderMag(NEGATIVE_INFINITY, x));
        Assert.assertTrue(isTotalOrderMag(NEGATIVE_INFINITY, NaN));
        Assert.assertTrue(isTotalOrderMag(POSITIVE_INFINITY, NaN));
    }

    @Test
    public void testRadix() {
        Assert.assertEquals(10, radix(fromInt32(2)));
    }

    @Test
    public void testRem() {
        for (int x = -1000; x < 1000; ++x)
            for (int y = -11; y < 11; ++y) {
                if (y == 0)
                    continue;
                final long X = fromInt32(x);
                final long Y = fromInt32(y);
                final long R = roundIntegralNearestEven(div(X, Y));
                final long Z = sub(X, mul(Y, R));
                assertDecimalEqual(Z, rem(X, Y), "x=" + x + "; y=" + y);
            }
    }

    @Test
    public void testIlogb() {
        Assert.assertEquals(3, ilogb(fromInt32(5432)));
        Assert.assertEquals(2, ilogb(parse("8e+2")));
        Assert.assertEquals(0, ilogb(fromDouble(Math.PI)));
        Assert.assertEquals(-6, ilogb(fromDouble(3.34034e-6)));
        Assert.assertEquals(-4, ilogb(parse("6e-4")));
    }

    @Test
    public void testScalbn() {
        final Random random = new Random(getRandomSeed());
        for (int i = 0; i < N; ++i) {
            final int x = random.nextInt();
            final int y = random.nextInt(200) - 100;
            assertDecimalEqual(parse(x + "e" + y), scalbn(fromInt32(x), y), "x=" + x + "; y=" + y);
        }
    }

    @Test
    public void testLdexp() {
        final Random random = new Random(getRandomSeed());
        for (int i = 0; i < N; ++i) {
            final int x = random.nextInt();
            final int y = random.nextInt(200) - 100;
            final long testValue = ldexp(fromInt32(x), y);
            assertDecimalEqual(parse(x + "e" + y), testValue, "x=" + x + "; y=" + y);
            assertDecimalEqual(scalbn(fromInt32(x), y), testValue, "x=" + x + "; y=" + y);
        }
    }

    @Test
    public void testQuantize() {
        final long x = scalbn(fromInt32(1234), 5);
        final long y = scalbn(fromInt32(56789), -2);
        Assert.assertEquals(5, getUnbiasedExponent(x));
        Assert.assertEquals(-2, getUnbiasedExponent(y));
        final long z = quantize(x, y);
        assertDecimalEqual(x, z);
        Assert.assertEquals(getUnbiasedExponent(y),
            getUnbiasedExponent(z));
    }

    @Test
    public void testToBinary32() {
        final Random random = new Random(getRandomSeed());
        for (int i = 0; i < N; ++i) {
            final int m = random.nextInt(2 * 10_000_000) - 10_000_000;
            final int e = random.nextInt(2 * 30) - 30;
            final float rv = Float.parseFloat(m + "e" + e);
            final float testValue = toBinary32(fromDouble(rv));
            Assert.assertEquals(testValue, rv, 0);
        }
    }

    @Test
    public void testToBinary64() {
        final Random random = new Random(getRandomSeed());
        for (int i = 0; i < N; ++i) {
            long m = (Math.abs(random.nextLong()) % 1000_000_000_000_000L) * (random.nextInt(2) > 0 ? 1 : -1);
            final int e = random.nextInt(2 * 300) - 300;
            final double rv = Double.parseDouble(m + "e" + e);
            final double testValue = toBinary64(fromDouble(rv));
            Assert.assertEquals(testValue, rv, 0);
        }
    }

    @Test
    public void testLogb() {
        assertDecimalEqual(parse("0"), logb(parse("3.14")));
        assertDecimalEqual(parse("7"), logb(parse("3.14e+7")));
        assertDecimalEqual(parse("-3"), logb(parse("314e-5")));
        assertDecimalEqual(parse("-3"), logb(parse("-314e-5")));
    }

    @Test
    public void testNearByInt() {
        testRoundFunc(new String[]{
            "0", "0", "1", // 0
            "1", "2", "2", // 1
            "2", "2", "3", // 2
            "3", "4", "4", // 3
            "0", "0", "-1", // -0
            "-1", "-2", "-2", // -1
            "-2", "-2", "-3", // -2
            "-3", "-4", "-4" // -3
        }, new TestUtils.Function<Long, Long>() {
            @Override
            public Long apply(Long aLong) {
                return nearByInt(aLong);
            }
        });
    }

    @Test
    public void testFdim() {
        final Random random = new Random(getRandomSeed());
        for (int i = 0; i < N; ++i) {
            final long x = scalbn(fromInt32(random.nextInt()), random.nextInt(200) - 100);
            final long y = scalbn(fromInt32(random.nextInt()), random.nextInt(200) - 100);
            assertDecimalEqual(
                isGreater(x, y) ? sub(x, y) : ZERO,
                fdim(x, y));
        }
    }

    @Test
    public void testQuantExp() {
        final long x = scalbn(fromInt32(1234), 5);
        final long y = scalbn(fromInt32(56789), -2);

        Assert.assertEquals(5, quantExp(x));
        Assert.assertEquals(-2, quantExp(y));
    }

    @Test
    public void testQuantum() {
        final long x = scalbn(fromInt32(1234), 5);
        final long y = scalbn(fromInt32(56789), -2);

        assertDecimalEqual(parse("1e+5"), quantum(x));
        assertDecimalEqual(parse("1e-2"), quantum(y));
    }

    @Test
    public void testVariablesMapper() {
        String version = NativeMathImpl.version;
        final boolean isSnapshot = version.endsWith("-SNAPSHOT");
        if (isSnapshot)
            version = version.substring(0, version.length() - "-SNAPSHOT".length());

        VariablesMapper varMapper = new VariablesMapper(NativeMathImplLoader.class, version);
        System.out.println("VariablesMapper.getPackage: " + varMapper.getPackage());
        System.out.println("VariablesMapper.getOs: " + varMapper.getOs());
        System.out.println("VariablesMapper.getArch: " + varMapper.getArch());
    }
}
