package com.epam.deltix.dfp;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Random;

import static com.epam.deltix.dfp.TestUtils.*;
import static org.junit.Assert.*;

public class Decimal64UtilsTest {
    private static final int COUNT = 1000;

    private final Random random = new Random();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static void assertSimilar(double expected, double actual, double epsilon) {
        assertTrue(Math.abs(expected - actual) / Math.max(1.0, Math.abs(expected)) < epsilon);
    }

    private static long round(long x, long precision) {
        if (x >= 0) {
            return ((x + precision / 2) / precision) * precision;
        } else {
            return ((x - precision / 2) / precision) * precision;
        }
    }

    @Test
    public void doubleConversion() {
        for (int i = 0; i < COUNT; i += 1) {
            final double x = random.nextDouble();
            final long y = Decimal64Utils.fromDouble(x);
            final double z = Decimal64Utils.toDouble(y);
            assertSimilar(x, z, 1.0E-09);
        }
    }

    @Test
    public void fixedPointConversion() {
        final int numberOfDigits = 9;
        for (int i = 0; i < COUNT; i += 1) {
            final long m = random.nextLong() & 0xFFFF_FFFF_FFFFL;

            final long x = Decimal64Utils.fromFixedPoint(m, numberOfDigits);
            final double y = m * Math.pow(10.0, -numberOfDigits);
            assertSimilar(y, Decimal64Utils.toDouble(x), 1.0E-09);

            final long z = Decimal64Utils.toFixedPoint(x, numberOfDigits);
            assertEquals(z, m);
        }
    }

    @Test
    public void intConversion() {
        int m = Integer.MIN_VALUE; // Test min value first

        for (int i = 0; i < COUNT; i += 1) {
            final long dfp = Decimal64Utils.fromInt(m);
            assertEquals(m, Decimal64Utils.toInt(dfp));
            assertSimilar(m, Decimal64Utils.toDouble(dfp), 1.0E-09);
            m = random.nextInt();
        }
    }

    @Test
    public void longConversion() {
        for (int i = 0; i < COUNT; i += 1) {
            final long m = random.nextLong() & 0xFFFF_FFFF_FFFFL;

            final long x = Decimal64Utils.fromLong(m);
            assertEquals(m, Decimal64Utils.toLong(x));
            assertSimilar(m, Decimal64Utils.toDouble(x), 1.0E-09);
        }
    }

    @Test
    public void negation() {
        for (int i = 0; i < COUNT; i += 1) {
            final double x = random.nextDouble();
            final long y = Decimal64Utils.fromDouble(x);
            final long z = Decimal64Utils.negate(y);
            final double w = Decimal64Utils.toDouble(z);
            assertSimilar(-x, w, 1.0E-09);
        }
    }

    @Test
    public void addition() {
        for (int i = 0; i < COUNT; i += 1) {
            final double x = random.nextDouble();
            final double y = random.nextDouble();
            final double z = random.nextDouble();
            final double w = random.nextDouble();

            final long dx = Decimal64Utils.fromDouble(x);
            final long dy = Decimal64Utils.fromDouble(y);
            final long dz = Decimal64Utils.fromDouble(z);
            final long dw = Decimal64Utils.fromDouble(w);

            assertSimilar(x + y, Decimal64Utils.toDouble(Decimal64Utils.add(dx, dy)), 1.0E-09);
            assertSimilar(x + y + z, Decimal64Utils.toDouble(Decimal64Utils.add(dx, dy, dz)), 1.0E-09);
            assertSimilar(x + y + z + w, Decimal64Utils.toDouble(Decimal64Utils.add(dx, dy, dz, dw)), 1.0E-09);
        }
    }

    @Test
    public void subtraction() {
        for (int i = 0; i < COUNT; i += 1) {
            final double x = random.nextDouble();
            final double y = random.nextDouble();

            final long dx = Decimal64Utils.fromDouble(x);
            final long dy = Decimal64Utils.fromDouble(y);
            final long result = Decimal64Utils.subtract(dx, dy);

            assertSimilar(x - y, Decimal64Utils.toDouble(result), 1.0E-09);
        }
    }

    @Test
    public void multiplication() {
        for (int i = 0; i < COUNT; i += 1) {
            final double x = random.nextDouble();
            final double y = random.nextDouble();
            final double z = random.nextDouble();
            final double w = random.nextDouble();

            final long dx = Decimal64Utils.fromDouble(x);
            final long dy = Decimal64Utils.fromDouble(y);
            final long dz = Decimal64Utils.fromDouble(z);
            final long dw = Decimal64Utils.fromDouble(w);

            assertSimilar(x * 13, Decimal64Utils.toDouble(Decimal64Utils.multiplyByInteger(dx, 13)), 1.0E-09);

            assertSimilar(x * 10, Decimal64Utils.toDouble(Decimal64Utils.scaleByPowerOfTen(dx, 1)), 1.0E-09);
            assertSimilar(x * 100, Decimal64Utils.toDouble(Decimal64Utils.scaleByPowerOfTen(dx, 2)), 1.0E-09);
            assertSimilar(x * 1000, Decimal64Utils.toDouble(Decimal64Utils.scaleByPowerOfTen(dx, 3)), 1.0E-09);

            assertSimilar(x * y, Decimal64Utils.toDouble(Decimal64Utils.multiply(dx, dy)), 1.0E-09);
            assertSimilar(x * y * z, Decimal64Utils.toDouble(Decimal64Utils.multiply(dx, dy, dz)), 1.0E-09);
            assertSimilar(x * y * z * w, Decimal64Utils.toDouble(Decimal64Utils.multiply(dx, dy, dz, dw)), 1.0E-09);
        }
    }

    @Test
    public void division() {
        for (int i = 0; i < COUNT; i += 1) {
            final double x = random.nextDouble();
            final double y = random.nextDouble();

            final long dx = Decimal64Utils.fromDouble(x);
            final long dy = Decimal64Utils.fromDouble(y);
            final long result = Decimal64Utils.divide(dx, dy);

            assertSimilar(x / 13, Decimal64Utils.toDouble(Decimal64Utils.divideByInteger(dx, 13)), 1.0E-09);

            assertSimilar(x / 10, Decimal64Utils.toDouble(Decimal64Utils.scaleByPowerOfTen(dx, -1)), 1.0E-09);
            assertSimilar(x / 100, Decimal64Utils.toDouble(Decimal64Utils.scaleByPowerOfTen(dx, -2)), 1.0E-09);
            assertSimilar(x / 1000, Decimal64Utils.toDouble(Decimal64Utils.scaleByPowerOfTen(dx, -3)), 1.0E-09);

            assertSimilar(x / y, Decimal64Utils.toDouble(result), 1.0E-09);
        }
    }

    @Test
    public void min() {
        assertEquals(Decimal64Utils.NaN, Decimal64Utils.min(Decimal64Utils.ZERO, Decimal64Utils.NaN));
        assertEquals(Decimal64Utils.NaN, Decimal64Utils.min(Decimal64Utils.NaN, Decimal64Utils.ZERO));
        assertEquals(Decimal64Utils.NaN, Decimal64Utils.min(Decimal64Utils.NaN, Decimal64Utils.NaN));

        for (int i = 0; i < COUNT; i += 1) {
            final double x = random.nextDouble();
            final double y = random.nextDouble();
            final double z = random.nextDouble();
            final double w = random.nextDouble();

            final long dx = Decimal64Utils.fromDouble(x);
            final long dy = Decimal64Utils.fromDouble(y);
            final long dz = Decimal64Utils.fromDouble(z);
            final long dw = Decimal64Utils.fromDouble(w);

            assertSimilar(Math.min(x, y), Decimal64Utils.toDouble(Decimal64Utils.min(dx, dy)), 1.0E-09);
            assertSimilar(Math.min(x, Math.min(y, z)), Decimal64Utils.toDouble(Decimal64Utils.min(dx, dy, dz)), 1.0E-09);
            assertSimilar(Math.min(Math.min(x, y), Math.min(z, w)), Decimal64Utils.toDouble(Decimal64Utils.min(dx, dy, dz, dw)), 1.0E-09);
        }
    }

    @Test
    public void max() {
        assertEquals(Decimal64Utils.NaN, Decimal64Utils.max(Decimal64Utils.ZERO, Decimal64Utils.NaN));
        assertEquals(Decimal64Utils.NaN, Decimal64Utils.max(Decimal64Utils.NaN, Decimal64Utils.ZERO));
        assertEquals(Decimal64Utils.NaN, Decimal64Utils.max(Decimal64Utils.NaN, Decimal64Utils.NaN));

        for (int i = 0; i < COUNT; i += 1) {
            final double x = random.nextDouble();
            final double y = random.nextDouble();
            final double z = random.nextDouble();
            final double w = random.nextDouble();

            final long dx = Decimal64Utils.fromDouble(x);
            final long dy = Decimal64Utils.fromDouble(y);
            final long dz = Decimal64Utils.fromDouble(z);
            final long dw = Decimal64Utils.fromDouble(w);

            assertSimilar(Math.max(x, y), Decimal64Utils.toDouble(Decimal64Utils.max(dx, dy)), 1.0E-09);
            assertSimilar(Math.max(x, Math.max(y, z)), Decimal64Utils.toDouble(Decimal64Utils.max(dx, dy, dz)), 1.0E-09);
            assertSimilar(Math.max(Math.max(x, y), Math.max(z, w)), Decimal64Utils.toDouble(Decimal64Utils.max(dx, dy, dz, dw)), 1.0E-09);
        }
    }

    @Test
    public void abs() {
        for (int i = 0; i < COUNT; i += 1) {
            final double x = random.nextDouble() * (random.nextBoolean() ? 1 : -1);

            final long dx = Decimal64Utils.fromDouble(x);
            final long dy = Decimal64Utils.abs(dx);

            assertSimilar(Math.abs(x), Decimal64Utils.toDouble(dy), 1.0E-09);
        }
    }

    @Test
    public void average() {
        for (int i = 0; i < COUNT; i += 1) {
            final double x = random.nextDouble();
            final double y = random.nextDouble();

            final long dx = Decimal64Utils.fromDouble(x);
            final long dy = Decimal64Utils.fromDouble(y);
            final long result = Decimal64Utils.average(dx, dy);

            assertSimilar((x + y) / 2, Decimal64Utils.toDouble(result), 1.0E-09);
        }
    }

    @Test
    public void round() {
        final int numberOfDigits = 9;
        final long multiplier = (long) Math.pow(10, numberOfDigits);
        for (int i = 0; i < COUNT; i += 1) {
            final long x = random.nextLong() & 0xFFFF_FFFF_FFFFL;
            final long p = random.nextLong() & 0xFFFF_FFFFL;

            final long dx = Decimal64Utils.fromFixedPoint(x, numberOfDigits);
            final long dp = Decimal64Utils.fromFixedPoint(p, numberOfDigits);
            final long result = Decimal64Utils.round(dx, dp);

            assertSimilar(round(x, p), Decimal64Utils.toDouble(result) * multiplier, 1.0E-09);
        }
    }

    @Test
    public void equalsObject() {
        Assert.assertTrue(Decimal64Utils.equals(Decimal64Utils.NaN, Decimal64Utils.NaN));
        Assert.assertTrue(Decimal64Utils.equals(5L, Decimal64.fromUnderlying(5L)));
        Assert.assertFalse(Decimal64Utils.equals(5L, (Object) 6L));
        Assert.assertFalse(Decimal64Utils.equals(5L, null));
        Assert.assertFalse(Decimal64Utils.equals(5L, "string"));
    }

    @Test
    public void constants() {
        assertSimilar(0, Decimal64Utils.toDouble(Decimal64Utils.ZERO), 1.0E-09);

        assertSimilar(1, Decimal64Utils.toDouble(Decimal64Utils.ONE), 1.0E-09);
        assertSimilar(2, Decimal64Utils.toDouble(Decimal64Utils.TWO), 1.0E-09);
        assertSimilar(10, Decimal64Utils.toDouble(Decimal64Utils.TEN), 1.0E-09);
        assertSimilar(100, Decimal64Utils.toDouble(Decimal64Utils.HUNDRED), 1.0E-09);
        assertSimilar(1000, Decimal64Utils.toDouble(Decimal64Utils.THOUSAND), 1.0E-09);
        assertSimilar(1000_000, Decimal64Utils.toDouble(Decimal64Utils.MILLION), 1.0E-09);

        assertSimilar(0.1, Decimal64Utils.toDouble(Decimal64Utils.ONE_TENTH), 1.0E-09);
        assertSimilar(0.01, Decimal64Utils.toDouble(Decimal64Utils.ONE_HUNDREDTH), 1.0E-09);
    }

    @Test
    public void testSpecial() {
        assertFalse(Decimal64Utils.isNaN(Decimal64Utils.ZERO));
        assertFalse(Decimal64Utils.isInfinity(Decimal64Utils.ZERO));
        assertFalse(Decimal64Utils.isPositiveInfinity(Decimal64Utils.ZERO));
        assertFalse(Decimal64Utils.isNegativeInfinity(Decimal64Utils.ZERO));
        assertTrue(Decimal64Utils.isFinite(Decimal64Utils.ZERO));

        assertTrue(Decimal64Utils.isNaN(Decimal64Utils.NaN));
        assertFalse(Decimal64Utils.isInfinity(Decimal64Utils.NaN));
        assertFalse(Decimal64Utils.isPositiveInfinity(Decimal64Utils.NaN));
        assertFalse(Decimal64Utils.isNegativeInfinity(Decimal64Utils.NaN));
        assertFalse(Decimal64Utils.isFinite(Decimal64Utils.NaN));

        assertFalse(Decimal64Utils.isNaN(Decimal64Utils.POSITIVE_INFINITY));
        assertTrue(Decimal64Utils.isInfinity(Decimal64Utils.POSITIVE_INFINITY));
        assertTrue(Decimal64Utils.isPositiveInfinity(Decimal64Utils.POSITIVE_INFINITY));
        assertFalse(Decimal64Utils.isNegativeInfinity(Decimal64Utils.POSITIVE_INFINITY));
        assertFalse(Decimal64Utils.isFinite(Decimal64Utils.POSITIVE_INFINITY));

        assertFalse(Decimal64Utils.isNaN(Decimal64Utils.NEGATIVE_INFINITY));
        assertTrue(Decimal64Utils.isInfinity(Decimal64Utils.NEGATIVE_INFINITY));
        assertFalse(Decimal64Utils.isPositiveInfinity(Decimal64Utils.NEGATIVE_INFINITY));
        assertTrue(Decimal64Utils.isNegativeInfinity(Decimal64Utils.NEGATIVE_INFINITY));
        assertFalse(Decimal64Utils.isFinite(Decimal64Utils.NEGATIVE_INFINITY));

        final double x = random.nextDouble();

        final long dx = Decimal64Utils.fromDouble(x);
        final long dy = Decimal64Utils.ZERO;
        final long result = Decimal64Utils.add(dx, dy);

        assertSimilar(x, Decimal64Utils.toDouble(result), 1.0E-09);
    }

    @Test
    public void compareTo() {
        for (int i = 0; i < COUNT; i += 1) {
            final double x = random.nextDouble();
            final double y = random.nextDouble();

            final long dx = Decimal64Utils.fromDouble(x);
            final long dy = Decimal64Utils.fromDouble(y);

            assertEquals(Double.compare(x, y), Decimal64Utils.compareTo(dx, dy));
            assertEquals(Double.compare(x, x), Decimal64Utils.compareTo(dx, dx));
            assertEquals(Double.compare(y, y), Decimal64Utils.compareTo(dy, dy));
        }

        assertEquals(0, Decimal64Utils.compareTo(Decimal64Utils.ONE, Decimal64Utils.ONE));
        assertEquals(-1, Decimal64Utils.compareTo(Decimal64Utils.ONE, Decimal64Utils.TWO));
        assertEquals(1, Decimal64Utils.compareTo(Decimal64Utils.TWO, Decimal64Utils.ONE));

        assertEquals(0, Decimal64Utils.compareTo(Decimal64Utils.POSITIVE_INFINITY, Decimal64Utils.POSITIVE_INFINITY));
        assertEquals(0, Decimal64Utils.compareTo(Decimal64Utils.NEGATIVE_INFINITY, Decimal64Utils.NEGATIVE_INFINITY));

        assertEquals(-1, Decimal64Utils.compareTo(Decimal64Utils.ONE, Decimal64Utils.POSITIVE_INFINITY));
        assertEquals(1, Decimal64Utils.compareTo(Decimal64Utils.ONE, Decimal64Utils.NEGATIVE_INFINITY));

        assertEquals(Double.compare(Double.NaN, Double.NaN), Decimal64Utils.compareTo(Decimal64Utils.NaN, Decimal64Utils.NaN));
        assertEquals(Double.compare(1.0, Double.NaN), Decimal64Utils.compareTo(Decimal64Utils.ONE, Decimal64Utils.NaN));
        assertEquals(Double.compare(Double.NaN, 1.0), Decimal64Utils.compareTo(Decimal64Utils.NaN, Decimal64Utils.ONE));
    }

    @Test
    public void comparisonWithZero() {
        assertFalse(Decimal64Utils.isZero(Decimal64Utils.NaN));
        assertFalse(Decimal64Utils.isZero(Decimal64Utils.POSITIVE_INFINITY));
        assertFalse(Decimal64Utils.isZero(Decimal64Utils.NEGATIVE_INFINITY));

        final long value0 = Decimal64Utils.negate(Decimal64Utils.ZERO);
        assertTrue(Decimal64Utils.isZero(value0));
        assertFalse(Decimal64Utils.isPositive(value0));
        assertFalse(Decimal64Utils.isNegative(value0));
        assertTrue(Decimal64Utils.isNonPositive(value0));
        assertTrue(Decimal64Utils.isNonNegative(value0));

        final long value1 = Decimal64Utils.negate(Decimal64Utils.ZERO);
        assertTrue(Decimal64Utils.isZero(value1));
        assertFalse(Decimal64Utils.isPositive(value1));
        assertFalse(Decimal64Utils.isNegative(value1));
        assertTrue(Decimal64Utils.isNonPositive(value1));
        assertTrue(Decimal64Utils.isNonNegative(value1));

        final long value2 = Decimal64Utils.ONE;
        assertFalse(Decimal64Utils.isZero(value2));
        assertTrue(Decimal64Utils.isPositive(value2));
        assertFalse(Decimal64Utils.isNegative(value2));
        assertFalse(Decimal64Utils.isNonPositive(value2));
        assertTrue(Decimal64Utils.isNonNegative(value2));

        final long value3 = Decimal64Utils.negate(Decimal64Utils.ONE);
        assertFalse(Decimal64Utils.isZero(value3));
        assertFalse(Decimal64Utils.isPositive(value3));
        assertTrue(Decimal64Utils.isNegative(value3));
        assertTrue(Decimal64Utils.isNonPositive(value3));
        assertFalse(Decimal64Utils.isNonNegative(value3));
    }

    @Test
    public void isEqual() {
        final String message = "checkEquality()";
        // TODO: better tests
        checkEquality(Decimal64Utils.ONE, Decimal64Utils.ONE);
        checkEquality(Decimal64Utils.POSITIVE_INFINITY, Decimal64Utils.POSITIVE_INFINITY);
        checkEquality(Decimal64Utils.NEGATIVE_INFINITY, Decimal64Utils.NEGATIVE_INFINITY);
    }

    private void checkEquality(long value1, long value2) {
        assertTrue(Decimal64Utils.isEqual(value1, value2));
    }

    @Test
    public void roundTowardsPositiveInfinity() {
        final String message = "checkRoundTowardsPositiveInfinity()";

        applyToPairs(
            new BiConsumer<String, String>() {
                @Override
                public void accept(String a, String b) {
                    checkFunction(
                        new TestUtils.Function<Long, Long>() {
                            @Override
                            public Long apply(Long aLong) {
                                return Decimal64Utils.roundTowardsPositiveInfinity(aLong);
                            }
                        }, a, b, message);
                }
            },
            "-1", "-1.0",
            "0", "-0.7",
            "0", "-0.5",
            "0", "-0.2",
            "0", "0.0",
            "1", "0.2",
            "1", "0.5",
            "1", "0.7",
            "1", "1.0"
        );

        @Decimal final long multiple = Decimal64Utils.parse("0.1");

        applyToPairs(
            new BiConsumer<String, String>() {
                @Override
                public void accept(String a, String b) {
                    checkFunction(
                        new Function<Long, Long>() {
                            @Override
                            public Long apply(Long x) {
                                return Decimal64Utils.roundTowardsPositiveInfinity(x, multiple);
                            }
                        }, a, b, message);
                }
            },
            "-0.1", "-0.10",
            "0", "-0.07",
            "0", "-0.05",
            "0", "-0.02",
            "0", "-0.02",
            "0", "0.0",
            "0.1", "0.02",
            "0.1", "0.05",
            "0.1", "0.07",
            "0.1", "0.10",
            "1000", "999.9001",
            "-1000", "-1000.0999"
        );
    }

    @Test
    public void roundTowardsNegativeInfinity() {
        final String message = "checkRoundTowardsNegativeInfinity()";

        applyToPairs(
            new BiConsumer<String, String>() {
                @Override
                public void accept(String a, String b) {
                    checkFunction(
                        new Function<Long, Long>() {
                            @Override
                            public Long apply(Long aLong) {
                                return Decimal64Utils.roundTowardsNegativeInfinity(aLong);
                            }
                        }, a, b, message);
                }
            },
            "-1", "-1.0",
            "-1", "-1.0",
            "-1", "-0.7",
            "-1", "-0.5",
            "-1", "-0.2",
            "0", "0.0",
            "0", "0.2",
            "0", "0.5",
            "0", "0.7",
            "1", "1.0"
        );

        @Decimal final long multiple = Decimal64Utils.parse("0.1");

        applyToPairs(
            new BiConsumer<String, String>() {
                @Override
                public void accept(String a, String b) {
                    checkFunction(
                        new Function<Long, Long>() {
                            @Override
                            public Long apply(Long x) {
                                return Decimal64Utils.roundTowardsNegativeInfinity(x, multiple);
                            }
                        }, a, b, message);
                }
            },
            "-0.1", "-0.10",
            "-0.1", "-0.07",
            "-0.1", "-0.05",
            "-0.1", "-0.02",
            "-0.1", "-0.02",
            "0", "0.0",
            "0", "0.02",
            "0", "0.05",
            "0", "0.07",
            "0.1", "0.10",
            "1000", "1000.09",
            "-1000", "-999.95"
        );
    }

    @Test
    public void roundTowardsZero() {
        applyToPairs(
            new BiConsumer<String, String>() {
                @Override
                public void accept(String s1, String s2) {
                    checkRoundTowardsZero(s1, s2);
                }
            },
            "-1", "-1.0",
            "0", "-0.7",
            "0", "-0.5",
            "0", "-0.2",
            "0", "0.0",
            "0", "0.2",
            "0", "0.5",
            "0", "0.7",
            "1", "1.0"
        );
    }

    private void checkRoundTowardsZero(String str1, String str2) {
        final String message = "roundTowardsZero()";
        assertEquals(message, str1, Decimal64Utils.toString(Decimal64Utils.roundTowardsZero(Decimal64Utils.parse(str2))));
    }

    @Test
    public void roundToNearestTiesAwayFromZero() {
        final String message = "checkRoundToNearestTiesAwayFromZero()";

        applyToPairs(
            new BiConsumer<String, String>() {
                @Override
                public void accept(String a, String b) {
                    checkFunction(
                        new Function<Long, Long>() {
                            @Override
                            public Long apply(Long aLong) {
                                return Decimal64Utils.roundToNearestTiesAwayFromZero(aLong);
                            }
                        }, a, b, message);
                }
            },
            "-1", "-1.0",
            "-1", "-0.7",
            "-1", "-0.5",
            "0", "-0.2",
            "0", "0.0",
            "0", "0.2",
            "1", "0.5",
            "1", "0.7",
            "1", "1.0",
            "10000", "9999.5",
            "-10000", "-9999.5"
        );

        @Decimal final long multiple = Decimal64Utils.parse("0.1");

        applyToPairs(
            new BiConsumer<String, String>() {
                @Override
                public void accept(String a, String b) {
                    checkFunction(
                        new Function<Long, Long>() {
                            @Override
                            public Long apply(Long x) {
                                return Decimal64Utils.roundToNearestTiesAwayFromZero(x, multiple);
                            }
                        }, a, b, message);
                }
            },
            "-0.1", "-0.10",
            "-0.1", "-0.07",
            "-0.1", "-0.05",
            "0", "-0.02",
            "0", "0.0",
            "0", "0.02",
            "0.1", "0.05",
            "0.1", "0.07",
            "0.1", "0.10",
            "1000", "999.95",
            "-1000", "-999.95"
        );
    }

    @Test
    public void roundToNearestTiesToEven() {
        final String message = "checkRoundToNearestTiesToEven()";

        applyToPairs(
            new BiConsumer<String, String>() {
                @Override
                public void accept(String a, String b) {
                    checkFunction(
                        new Function<Long, Long>() {
                            @Override
                            public Long apply(Long aLong) {
                                return Decimal64Utils.roundToNearestTiesToEven(aLong);
                            }
                        }, a, b, message);
                }
            },
            "-1", "-1.0",
            "-1", "-0.7",
            "0", "-0.5",
            "0", "-0.2",
            "0", "0.0",
            "0", "0.2",
            "0", "0.5",
            "1", "0.7",
            "1", "1.0",
            "10000", "9999.5",
            "-10000", "-9999.5"
        );

        @Decimal final long multiple = Decimal64Utils.parse("0.1");

        applyToPairs(
            new BiConsumer<String, String>() {
                @Override
                public void accept(String a, String b) {
                    checkFunction(
                        new Function<Long, Long>() {
                            @Override
                            public Long apply(Long x) {
                                return Decimal64Utils.roundToNearestTiesToEven(x, multiple);
                            }
                        }, a, b, message);
                }
            },
            "-0.1", "-0.10",
            "-0.1", "-0.07",
            "0", "-0.05",
            "0", "-0.02",
            "0", "0.0",
            "0", "0.02",
            "0", "0.05",
            "0.1", "0.07",
            "0.1", "0.10",
            "1000", "999.95",
            "-1000", "-999.95"
        );
    }

    @Test
    public void checkValueTypesAgentSupport() { // Check for missed *Checked functions
        final Class<Decimal64> objectClass = Decimal64.class;
        final Class<Decimal64Utils> utilityClass = Decimal64Utils.class;
        final String vtaSuffix = "Checked";

        for (final Method method : objectClass.getMethods()) {
            if (!method.getDeclaringClass().equals(objectClass))
                continue;

            if ((method.getModifiers() & Modifier.STATIC) == Modifier.STATIC) // Hm... The ValueTypeAgent do not map static functions yet
                continue;

            Class<?>[] parameterTypes = method.getParameterTypes();
            if ((method.getModifiers() & Modifier.STATIC) != Modifier.STATIC) {
                parameterTypes = Arrays.copyOf(parameterTypes, parameterTypes.length + 1);
                System.arraycopy(parameterTypes, 0, parameterTypes, 1, parameterTypes.length - 1);
                parameterTypes[0] = objectClass;
            }

            for (int i=0; i<parameterTypes.length; ++i) {
                if (parameterTypes[i].equals(objectClass))
                    parameterTypes[i] = long.class;
            }

            final String utilityMethodName = method.getName() + vtaSuffix;

            final Method utilityMethod;
            try {
                utilityMethod = utilityClass.getMethod(utilityMethodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Can't find complementary for method " + method);
            }

            if (utilityMethod.getAnnotation(Deprecated.class) == null)
                throw new RuntimeException("There is no @Deprecated annotation on method " + utilityMethod);
        }
    }
}
