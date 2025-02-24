package com.epam.deltix.dfp;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Random;

import static com.epam.deltix.dfp.TestUtils.*;


public class FromDoubleTest {

    public void decimalConversionSample() {
        String[] v = {
            "9.200000000000000", "9.199999999999999", "9.200000000000001", "9.200000000000002", "9.200000000000003",
            "9.200000000000005", "9.200000000000006", "9.200000000000007", "9.200000000000008", "9.200000000000009",
            "9.200000000000000E100", "9.199999999999999E100", "9.200000000000001E100", "9.200000000000002E100", "9.200000000000003E100",
            "9.200000000000005E100", "9.200000000000006E100", "9.200000000000007E100", "9.200000000000008E100", "9.200000000000009E100"
        };

        applyTo(
            new Consumer<String>() {
                @Override
                public void accept(String s) {
                    decimalAndDoubleConversion(s);
                }
            }, v);
        applyTo(
            new Consumer<String>() {
                @Override
                public void accept(String s) {
                    bigDecimalAndDoubleConversion(s);
                }
            }, v);
    }

    private void decimalAndDoubleConversion(String s) {
        Decimal64 x0 = Decimal64.parse(s);
        double x1 = x0.toDouble();
        Decimal64 x2 = Decimal64.fromDouble(x1);
        double x3 = x2.toDouble();
        System.out.printf("D64: %17s  ->f64: %17s  ->f64->D64: %17s ->f64->D64->f64: %17s%n", x0, x1, x2, x3);
    }

    private void bigDecimalAndDoubleConversion(String s) {
        BigDecimal y0 = new BigDecimal(s);
        double y1 = y0.doubleValue();
        BigDecimal y2 = new BigDecimal(y1);
        double y3 = y2.doubleValue();
        System.out.printf("BD64: %17s  ->f64: %17s  ->f64->BD64: %17s ->f64->BD64->f64: %17s%n", y0, y1, y2, y3);
    }


    // @Test
    public void etc() {
        decimalConversionSample();
        System.out.print(new BigDecimal(9.2));
    }


    boolean checkDoubleConversionDefault(Decimal64 x) {
        return x.equals(Decimal64.fromDouble(x.toDouble()));
    }

    private static void checkDecimalDoubleConversion(Decimal64 x, String s) {
        Decimal64 x2 = Decimal64.fromDecimalDouble(x.toDouble());

        Assert.assertEquals(x.toDouble(), x2.toDouble(), 0);
        if (!x2.equals(Decimal64.fromDouble(x.toDouble())))
            assertDecimalIdentical(x2.canonize(), x2, "fromDecimalDouble(x) failed to canonize() the result");

        if (null != s) {
            Assert.assertEquals(x.toDouble(), Double.parseDouble(s), 0);
            if (-1 == s.indexOf("E")) {
                Assert.assertEquals(s, x2.toString());
            }
        }
    }

    private static void checkDecimalDoubleConversion(Decimal64 x) {
        checkDecimalDoubleConversion(x, null);
    }


    private static void checkDecimalDoubleConversion(String s) {
        checkDecimalDoubleConversion(Decimal64.fromUnderlying(decimalFromString(s)), s);
    }


    @Test
    public void testFromDecimalDoubleConversionSpecial() {
        // Tests for "Special" number handling
        applyTo(
            new Consumer<String>() {
                @Override
                public void accept(String s) {
                    FromDoubleTest.checkDecimalDoubleConversion(s);
                }
            },
            "0", "NaN", "Infinity", "-Infinity",
            "1E0", "1000000000000000E0",
            "0.000000009412631", "0.95285752",
            "9.2", "25107188000000000000000000000000000000000000000000000000",
            "9.888888888888888",
            // Exponent limits
            "-1E-308", "-1000000000000000E-322"
        );
    }


    @Test
    public void testFromDecimalDoubleConversion() {
        for (int i = 0; i < N; i++) {
            Decimal64 x = getRandomDecimal(1000_0000_0000_0000L);
            checkDecimalDoubleConversion(x);
        }
    }

    @Test
    public void testFromDecimalDoubleConversionLongMantissa() {
        for (int i = 0; i < N; i++) {
            Decimal64 x = getRandomDecimal(9000_0000_0000_0000L, 1000_0000_0000_0000L);
            checkDecimalDoubleConversion(x);
        }
    }

    @Test
    public void mantissaZerosCombinationsTest() {
        mantissaZerosCombinations(
            new BiConsumer<Long, Integer>() {
                @Override
                public void accept(Long m, Integer l) {
                    checkDecimalDoubleConversion(Decimal64.fromFixedPoint(m, l));
                }
            });
        mantissaZerosCombinations(
            new BiConsumer<Long, Integer>() {
                @Override
                public void accept(Long m, Integer l) {
                    checkDecimalDoubleConversion(Decimal64.fromFixedPoint(m / POWERS_OF_TEN[l], 0));
                }
            });
    }

    @Test
    public void extremeValuesOfExponentTest() {
        Decimal64 x = Decimal64.fromFixedPoint(1, 383 + 15);
        assertDecimalIdentical(x, Decimal64.MIN_POSITIVE_VALUE);
        assertDecimalIdentical(x.canonize(), Decimal64.MIN_POSITIVE_VALUE);
        x = Decimal64.fromFixedPoint(100, 400);
        assertDecimalIdentical(x.canonize(), Decimal64.MIN_POSITIVE_VALUE);
        x = Decimal64.fromFixedPoint(1000000000000000L, 413);
        assertDecimalIdentical(x.canonize(), Decimal64.MIN_POSITIVE_VALUE);

        x = Decimal64.fromFixedPoint(-1, 383 + 15);
        assertDecimalIdentical(x.canonize(), Decimal64.MAX_NEGATIVE_VALUE);

        x = Decimal64.fromFixedPoint(-1, 308);
        checkDecimalDoubleConversion(x);
        x = Decimal64.fromFixedPoint(-1000000000000000L, 322);
        checkDecimalDoubleConversion(x);
        x = Decimal64.fromFixedPoint(1, 0);
        checkDecimalDoubleConversion(x);
        x = Decimal64.fromFixedPoint(1000000000000000L, 0);
        checkDecimalDoubleConversion(x);
    }

    @Test
    public void testFromDecimalDoubleConversionsLongMantissa() {
        for (int i = 0; i < N; i++) {
            Decimal64 x = getRandomDecimal(1000000000000000L, 9999999999999999L);
            checkDecimalDoubleConversion(x);
        }
    }

    @Test
    public void testFromDecimalDoublePrecisionLoss() {
        final Random random = new Random();

        for (int i = 0; i < N / 10; ++i) {
            final long bin64Bits = random.nextLong();
            final double bin64 = Double.longBitsToDouble(bin64Bits);
            if (!Double.isFinite(bin64))
                continue;

            final Decimal64 dec64 = Decimal64.fromDecimalDouble(bin64);

            final double err = dec64.toBigDecimal().subtract(new BigDecimal(bin64)).abs().doubleValue();
            final double errUlp = err / Math.ulp(bin64);

            if (errUlp > 5)
                throw new RuntimeException("The ulpError=" + errUlp + " for bin64Bits=" + bin64Bits + "L: bin64(=" + bin64 + ") converted to dec64(=" + dec64.toScientificString() + ").");
        }
    }

    @Test
    public void testFromDecimalDoublesCases() {
        final Random random = new Random();
        for (int i = 0; i < N; ++i) {
            final double doubleValue = random.nextDouble();

            final Decimal64 b = Decimal64.fromDecimalDouble(doubleValue);
            final Decimal64 a = Decimal64.fromDouble(doubleValue);

            if (!a.equals(b) && String.format("%f", doubleValue).equals(b.toString()))
                throw new RuntimeException("doubleValue(=" + doubleValue + "): fromDecimalDouble(=" + b + ") - fromDouble(=" + a + ") = " + b.subtract(a));
        }
    }

    static final int N = 5000000;

    @Test
    public void testShortenMantissaDenormalized() {
        Assert.assertEquals(Decimal64.ZERO, Decimal64.ZERO.shortenMantissa(100, 1));

        Assert.assertEquals(Decimal64.ZERO,
            Decimal64.fromUnderlying(
                    JavaImpl.packBasic(0, JavaImpl.BIASED_EXPONENT_MAX_VALUE, 0))
                .shortenMantissa(100, 1));

        {
            final Decimal64 d64 = Decimal64.fromUnderlying(
                JavaImpl.packBasic(0, JavaImpl.BIASED_EXPONENT_MAX_VALUE, 1));
            Assert.assertEquals(d64, d64.shortenMantissa(100, 1));
        }

        {
            final Decimal64 d64 = Decimal64.fromUnderlying(
                JavaImpl.packBasic(0, JavaImpl.BIASED_EXPONENT_MAX_VALUE, 22));
            Assert.assertEquals(d64, d64.shortenMantissa(100000, 1));
        }

        {
            long m = 5999_9999_9999_8200L;
            int e = -18 + JavaImpl.EXPONENT_BIAS;

            long delta = m % 10000;
            delta = Math.min(delta, 10000 - delta);
            int z = 3;
            Decimal64 rNorm = Decimal64.fromUnderlying(JavaImpl.packBasic(0, e, m))
                .shortenMantissa(delta, z);

            Decimal64 rDenorm = Decimal64.fromUnderlying(JavaImpl.packBasic(0, e + 2, m / 100))
                .shortenMantissa(delta, z);

            Assert.assertEquals(rNorm, rDenorm);
        }
    }

    @Test
    public void testShortenMantissaBigDelta() {
        Assert.assertEquals(Decimal64.parse("10000000000000000"),
            Decimal64.parse("9999000000000000").shortenMantissa(JavaImpl.MAX_COEFFICIENT / 10, 2));
    }

    @Test
    public void testShortenMantissaCase006() {
        String testString = "0.006";
        final double testValue = Double.parseDouble(testString);
        final double testX = 0.005999999999998265; // Math.nextDown(testValue);

        Decimal64 d64 = Decimal64.fromDouble(testX).shortenMantissa(1735, 1);
        Assert.assertEquals(testString, d64.toString());

        Decimal64.fromDouble(9.060176071990028E-7).shortenMantissa(2, 1);
    }

    @Test
    public void testShortenMantissaRandom() {
        final int randomSeed = new SecureRandom().nextInt();
        final Random random = new Random(randomSeed);

        try {
            for (int iteration = 0; iteration < N; ++iteration) {
                long mantissa = generateMantissa(random, Decimal64Utils.MAX_SIGNIFICAND_DIGITS);
                int error = random.nextInt(3) - 1;
                mantissa = Math.min(JavaImpl.MAX_COEFFICIENT, Math.max(0, mantissa + error));
                if (mantissa <= JavaImpl.MAX_COEFFICIENT / 10)
                    mantissa = mantissa * 10;

                long delta = generateMantissa(random, 0);
                if (delta > JavaImpl.MAX_COEFFICIENT/10)
                    delta = delta / 10;

                checkShortenMantissaCase(mantissa, delta);
            }
        } catch (final Throwable e) {
            throw new RuntimeException("Random seed " + randomSeed + " exception: " + e.getMessage(), e);
        }
    }

    @Test
    public void testShortenMantissaCase() {
        checkShortenMantissaCase(9999888877776001L, 1000);
        checkShortenMantissaCase(1230000000000000L, 80);
        checkShortenMantissaCase(1230000000000075L, 80);
        checkShortenMantissaCase(1229999999999925L, 80);
        checkShortenMantissaCase(4409286553495543L, 900);
        checkShortenMantissaCase(4409286553495000L, 1000);
        checkShortenMantissaCase(4409286550000000L, 81117294);
        checkShortenMantissaCase(9010100000000001L, 999999999999999L);
        checkShortenMantissaCase(8960196546869015L, 1);
        checkShortenMantissaCase(4700900091799999L, 947076117508L);
        checkShortenMantissaCase(5876471737721999L, 91086);
        checkShortenMantissaCase(6336494570000000L, 6092212816L);
        checkShortenMantissaCase(8960196546869011L, 999999999999999L);
        checkShortenMantissaCase(1519453608576584L, 3207L);
    }

    private static void checkShortenMantissaCase(final long mantissa, final long delta) {
        try {
            final long bestSolution = shortenMantissaDirect(mantissa, delta);

            final long test64 = Decimal64Utils.toLong(Decimal64Utils.shortenMantissa(Decimal64Utils.fromLong(mantissa), delta, 0));

            if (test64 != bestSolution)
                throw new RuntimeException("The mantissa(=" + mantissa + ") and delta(=" + delta + ") produce test64(=" + test64 + ") != bestSolution(=" + bestSolution + ").");
        } catch (Throwable e) {
            throw new RuntimeException("The mantissa(=" + mantissa + ") and delta(=" + delta + ") produce exception.", e);
        }
    }

    private static long shortenMantissaDirect(final long mantissa, final long delta) {
        long rgUp = mantissa + delta;
        long rgDown = mantissa - delta;

        if (mantissa <= JavaImpl.MAX_COEFFICIENT / 10 || mantissa > JavaImpl.MAX_COEFFICIENT)
            throw new IllegalArgumentException("The mantissa(=" + mantissa + ") must be in (" + JavaImpl.MAX_COEFFICIENT / 10 + ".." + JavaImpl.MAX_COEFFICIENT + "] range");

        long bestSolution = Long.MIN_VALUE;
        if (rgDown > 0) {
            long mUp = (mantissa / 10) * 10;
            long mFactor = 1;

            long bestDifference = Long.MAX_VALUE;
            int bestPosition = -1;

            for (int replacePosition = 0;
                 replacePosition < Decimal64Utils.MAX_SIGNIFICAND_DIGITS + 1;
                 ++replacePosition, mUp = (mUp / 100) * 10, mFactor *= 10) {
                for (int d = 0; d < 10; ++d) {
                    final long mTest = (mUp + d) * mFactor;
                    if (rgDown <= mTest && mTest <= rgUp) {
                        final long md = Math.abs(mantissa - mTest);
                        if (bestPosition < replacePosition ||
                            (bestPosition == replacePosition && bestDifference >= md)) {
                            bestPosition = replacePosition;
                            bestDifference = md;
                            bestSolution = mTest;
                        }
                    }
                }
            }
        } else {
            bestSolution = 0;
        }

        return bestSolution;
    }

    private static long generateMantissa(final Random random, int minimalLength) {
        int mLen = (1 + random.nextInt(Decimal64Utils.MAX_SIGNIFICAND_DIGITS) /*[1..16]*/);
        long m = 1 + random.nextInt(9);
        int i = 1;
        for (; i < mLen; ++i)
            m = m * 10 + random.nextInt(10);
        for (; i < minimalLength; ++i)
            m = m * 10;
        return m;
    }
}
