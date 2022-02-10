package com.epam.deltix.dfp;

import org.junit.Assert;
import org.junit.Test;

import static com.epam.deltix.dfp.TestUtils.*;

public class Decimal64Test {
    @Test
    public void unbox() {
        Assert.assertEquals(5L, Decimal64.toUnderlying(Decimal64.fromUnderlying(5L)));
        Assert.assertEquals(Decimal64Utils.NULL, Decimal64.toUnderlying(null));
    }

    @Test
    public void equality() {

        Decimal64 a = Decimal64.fromDouble(12345.6789);
        Decimal64 a2 = Decimal64.fromDouble(12345.6789);
        Decimal64 b = Decimal64.fromDouble(42);
        String s = "12345.6789";

        Assert.assertTrue(Decimal64.isNull(null));
        Assert.assertTrue(Decimal64.isNull(Decimal64.NULL));
        Assert.assertTrue(Decimal64.isNull(Decimal64.fromUnderlying(Decimal64Utils.NULL)));
        Assert.assertFalse(Decimal64.isNull(Decimal64.NaN));
        Assert.assertFalse(Decimal64.isNull(Decimal64.fromUnderlying(Decimal64Utils.NaN)));
        Assert.assertFalse(Decimal64.isNull(Decimal64.POSITIVE_INFINITY));
        Assert.assertFalse(Decimal64.isNull(Decimal64.fromUnderlying(Decimal64Utils.POSITIVE_INFINITY)));
        Assert.assertFalse(Decimal64.isNull(Decimal64.NEGATIVE_INFINITY));
        Assert.assertFalse(Decimal64.isNull(Decimal64.fromUnderlying(Decimal64Utils.NEGATIVE_INFINITY)));
        Assert.assertFalse(Decimal64.isNull(Decimal64.ONE));
        Assert.assertFalse(Decimal64.isNull(Decimal64.fromDouble(-2.0)));


        Assert.assertTrue("null == null", Decimal64.equals(null, (Object) null));
        Assert.assertTrue("null == null", Decimal64.equals(null, (Decimal64) null));
        Assert.assertTrue("null == null", Decimal64.isIdentical(null, (Object) null));
        Assert.assertTrue("null == null", Decimal64.isIdentical(null, (Decimal64) null));


        Assert.assertFalse("null != a", Decimal64.equals(null, (Object) a));
        Assert.assertFalse("null != a", Decimal64.equals(null, (Decimal64) a));
        Assert.assertFalse("null != a", Decimal64.isIdentical(null, (Object) a));
        Assert.assertFalse("null != a", Decimal64.isIdentical(null, (Decimal64) a));


        Assert.assertFalse("null != s", Decimal64.equals(null, (Object) s));
        Assert.assertFalse("null != s", Decimal64.equals(null, s));
        Assert.assertFalse("null != s", Decimal64.isIdentical(null, s));
        Assert.assertFalse("null != s", Decimal64.isIdentical(null, (Object) s));


        Assert.assertFalse("a != null ", Decimal64.equals(a, null));
        Assert.assertFalse("a != null ", Decimal64.isIdentical(a, null));

        Assert.assertFalse("a != null ", a.equals(null));
        Assert.assertFalse("a != null ", a.isIdentical(null));

        Assert.assertFalse("a != string", Decimal64.equals(a, s));
        Assert.assertFalse("a != string", Decimal64.isIdentical(a, s));
        Assert.assertFalse("a != string", a.equals(s));
        Assert.assertFalse("a != string", a.isIdentical(s));

        Assert.assertTrue("a == a", a == a);
        Assert.assertTrue("a == a", Decimal64.equals(a, a));
        Assert.assertTrue("a == a", Decimal64.isIdentical(a, a));
        Assert.assertTrue("a == a2", Decimal64.equals(a, a2));
        Assert.assertTrue("a == a2", Decimal64.isIdentical(a, a2));

        // Note: these comparisons may be of interest for Value Type Agent
        Assert.assertTrue("a != b", a != b);
        Assert.assertTrue("a != b", (Object) a != (Object) b);
        Assert.assertTrue("a != s", (Object) a != (Object) s);

        Assert.assertFalse("a != b", Decimal64.equals(a, b));
        Assert.assertFalse("a != b", Decimal64.isIdentical(a, b));

        Assert.assertTrue("+NaN == -NaN", Decimal64.equals(Decimal64.NaN, Decimal64.NaN.negate()));
        Assert.assertTrue("+NaN == -NaN", Decimal64.NaN.equals(Decimal64.NaN.negate()));
        Assert.assertTrue("+NaN == -NaN", Decimal64.equals(Decimal64.NaN.negate(), Decimal64.NaN));
        Assert.assertTrue("+NaN == -NaN", Decimal64.NaN.negate().equals(Decimal64.NaN));
        Assert.assertFalse("+Nan != -NaN", Decimal64.isIdentical(Decimal64.NaN, Decimal64.NaN.negate()));
        Assert.assertFalse("+NaN != -NaN", Decimal64.NaN.isIdentical(Decimal64.NaN.negate()));
        Assert.assertFalse("+Nan != -NaN", Decimal64.isIdentical(Decimal64.NaN.negate(), Decimal64.NaN));
        Assert.assertFalse("+NaN != -NaN", Decimal64.NaN.negate().isIdentical(Decimal64.NaN));

        // Equality-only tests for compareTo()
        Assert.assertFalse("+NaN != +NaN", 0 != Decimal64.NaN.compareTo(Decimal64.NaN));
        Assert.assertFalse("+NaN != -NaN", 0 != Decimal64.NaN.compareTo(Decimal64.NaN.negate()));
        Assert.assertFalse("+NaN != -NaN", 0 != Decimal64.NaN.negate().compareTo(Decimal64.NaN.negate()));

        Assert.assertTrue("a == a", 0 == a.compareTo(a));
        Assert.assertTrue("a == a2", 0 == a.compareTo(a2));
        Assert.assertFalse("a != b", 0 == a.compareTo(b));
        // TODO: Check if compareTo throws
    }

    /**
     * Combined method that checks both Decimal64Utils and Decimal64 canonize() and methods that use canonize(),
     * such as equal(), hashCode()
     */
    @Test
    public void canonizeTest() {
        long a = Decimal64Utils.fromFixedPoint(10000, 4);
        long b = Decimal64Utils.fromFixedPoint(1, 0);
        long c = Decimal64Utils.fromFixedPoint(10, 1);
        long d = Decimal64Utils.fromFixedPoint(100, 2);
        long e = Decimal64Utils.fromFixedPoint(1000, 3);

        checkCanonize(a, b);
        checkCanonize(b, c);
        checkCanonize(c, d);
        checkCanonize(d, e);
        checkCanonize(e, a);

        long nan1l = Decimal64Utils.NaN;
        long nan2l = nan1l + 20;
        checkCanonize(nan1l, nan2l);

        long posInf1l = Decimal64Utils.POSITIVE_INFINITY;
        long posInf2l = posInf1l + 10;
        checkCanonize(posInf1l, posInf2l);

        long negInf1l = Decimal64Utils.NEGATIVE_INFINITY;
        long negInf2l = negInf1l + 10;
        checkCanonize(negInf1l, negInf2l);

        a = Decimal64Utils.canonize(a);
        Assert.assertEquals(1.0, Decimal64Utils.toDouble(a), 0.00000000000001);

        long zero1 = Decimal64Utils.fromFixedPoint(0, 1);
        long zero2 = Decimal64Utils.fromFixedPoint(0, 2);
        long zero3 = Decimal64Utils.fromFixedPoint(0, 3);
        checkCanonize(zero1, zero2);
        checkCanonize(zero1, zero3);

        zero1 = Decimal64Utils.canonize(zero1);
        Assert.assertEquals(0.0, Decimal64Utils.toDouble(zero1), 0.00000000000001);

        a = (JavaImpl.BIASED_EXPONENT_MAX_VALUE + 0L << JavaImpl.EXPONENT_SHIFT_SMALL) + 10_000_000;
        b = (JavaImpl.BIASED_EXPONENT_MAX_VALUE - 1L << JavaImpl.EXPONENT_SHIFT_SMALL) + 100_000_000;
        checkCanonize(a, b);
    }

    static void checkCanonize(final long value1l, final long value2l) {
        Decimal64 value1 = Decimal64.fromUnderlying(value1l), value2 = Decimal64.fromUnderlying(value2l);
        String msg = "checkCanonize() failed";
        assertDecimalEqualNotIdentical(value1l, value2l, msg);
        assertDecimalEqualHashCode(value1l, value2l, msg, true);
        assertDecimalEqualIdentityHashCode(value1l, value2l, msg, false);

        assertDecimalEqualNotIdentical(value1, value2, msg);
        assertDecimalEqualHashCode(value1, value2, msg, true);
        assertDecimalEqualIdentityHashCode(value1, value2, msg, false);

        final Decimal64 value1c = value1.canonize();
        final Decimal64 value2c = value2.canonize();
        assertDecimalIdentical(value1c, value2c, msg);
        assertDecimalEqualIdentityHashCode(value1c, value2c, msg, true);

        assertDecimalIdentical(value1c.value, value2c.value, msg);
        assertDecimalEqualIdentityHashCode(value1c.value, value2c.value, msg, true);

        Assert.assertEquals(value1.toString(), value2.toString());
        Assert.assertTrue(value1c.equals(value2c));
        Assert.assertTrue(value2c.equals(value1c));
    }

    static void checkCanonize2(final long value1, final long value2) {
        Decimal64 a = Decimal64.fromUnderlying(value1);
        Decimal64 b = Decimal64.fromUnderlying(value2);

        Assert.assertEquals(a.toString(), b.toString());
        Decimal64 ac = a.canonize();
        Decimal64 bc = b.canonize();
        Assert.assertEquals(a.canonize(), b.canonize());
        Assert.assertEquals(ac, bc);
        Assert.assertEquals(a.canonize().toString(), b.canonize().toString());
        Assert.assertEquals(a.toString(), b.canonize().toString());
        Assert.assertEquals(a.canonize().toString(), b.toString());
        Assert.assertTrue(a.equals(b));
        Assert.assertTrue(b.equals(a));
    }

    @Test
    public void canonizeRandomTest() {
        mantissaZerosCombinations(
            new TestUtils.BiConsumer<Long, Integer>() {
                @Override
                public void accept(Long m, Integer l) {
                    long x = Decimal64Utils.fromFixedPoint(m, l);
                    long y = Decimal64Utils.fromFixedPoint(m / POWERS_OF_TEN[l], 0);

                    checkCanonize(x, y);
                    checkCanonize2(x, y);
                }
            });
        partsCombinationsWithoutEndingZeros(
            new TestUtils.BiConsumer<Long, Integer>() {
                @Override
                public void accept(Long m, Integer e) {
                    long x = Decimal64Utils.fromFixedPoint(m, e);
                    checkCanonize2(x, Decimal64Utils.canonize(x));
                }
            });
    }

    @Test
    public void canonizeZeroTest() {
        for (int exp = 398 - 0x2FF; exp <= 398; ++exp) {
            Decimal64 zero = Decimal64.fromFixedPoint(0, exp);
            checkCanonize2(zero.value, zero.canonize().value);
        }
    }

    @Test
    public void canonizeMantissaZerosCombinationsTest() {
        mantissaZerosCombinations(
            new BiConsumer<Long, Integer>() {
                @Override
                public void accept(Long m, Integer l) {
                    Decimal64 x = Decimal64.fromFixedPoint(m, l);
                    Decimal64 y = Decimal64.fromFixedPoint(m / POWERS_OF_TEN[(int) l], 0);
                    Assert.assertFalse(x.isIdentical(y));
                    Assert.assertTrue(x.canonize().isIdentical(y));
                }
            });
    }

    @Test
    public void numberConversionTest() {
        for (long i0 = 0; i0 < 9999999999999999L; i0 = (i0 << 1) + 1) {
            long x = i0;
            for (int i = 0; i < 2; i++, x = -x) {
                Assert.assertEquals(x, (long) Decimal64.fromDouble(x).toDouble());
                Assert.assertEquals(x, (long) Decimal64.fromDouble(x).toLong());
                Assert.assertTrue(Decimal64.fromDouble(x).equals(Decimal64.fromLong(x)));
                Assert.assertTrue(Decimal64.fromLong(x).equals(Decimal64.fromDouble(x)));

                if (Math.abs(x) <= Integer.MAX_VALUE) {
                    Assert.assertEquals(x, (long) Decimal64.fromLong(x).toInt());
                    Assert.assertEquals(x, (long) Decimal64.fromInt((int) x).toDouble());
                    Assert.assertEquals(x, (long) Decimal64.fromInt((int) x).toLong());
                    Assert.assertEquals(x, (long) Decimal64.fromInt((int) x).toInt());
                    Assert.assertTrue(Decimal64.fromDouble(x).equals(Decimal64.fromInt((int) x)));
                    Assert.assertTrue(Decimal64.fromLong(x).equals(Decimal64.fromInt((int) x)));
                } else {
                    // Expect integer overflow at some stage
                    Assert.assertNotEquals(x, (long) Decimal64.fromLong(x).toInt());
                    Assert.assertNotEquals(x, (long) Decimal64.fromInt((int) x).toDouble());
                    Assert.assertNotEquals(x, (long) Decimal64.fromInt((int) x).toLong());
                    Assert.assertNotEquals(x, (long) Decimal64.fromInt((int) x).toInt());
                    Assert.assertFalse(Decimal64.fromDouble(x).equals(Decimal64.fromInt((int) x)));
                    Assert.assertFalse(Decimal64.fromLong(x).equals(Decimal64.fromInt((int) x)));
                }
            }
        }
    }

    @Test
    public void toStringTest() {

        Assert.assertEquals("0", Decimal64.fromLong(0).toString());
        Assert.assertEquals("42", Decimal64.fromLong(42).toString());
        Assert.assertEquals(String.valueOf(Integer.MAX_VALUE), Decimal64.fromInt(Integer.MAX_VALUE).toString());
        Assert.assertEquals(String.valueOf(Integer.MIN_VALUE), Decimal64.fromInt(Integer.MIN_VALUE).toString());
        Assert.assertEquals(String.valueOf(Integer.MAX_VALUE), Decimal64.fromLong(Integer.MAX_VALUE).toString());
        Assert.assertEquals(String.valueOf(Integer.MIN_VALUE), Decimal64.fromLong(Integer.MIN_VALUE).toString());
        Assert.assertEquals(String.valueOf(Integer.MAX_VALUE), Decimal64.fromFixedPoint(Integer.MAX_VALUE, 0).toString());
        Assert.assertEquals(String.valueOf(Integer.MIN_VALUE), Decimal64.fromFixedPoint(Integer.MIN_VALUE, 0).toString());
        Assert.assertEquals("123.456", Decimal64.fromDouble(123.456).toString());
        Assert.assertEquals("123.4567", Decimal64.fromFixedPoint(1234567, 4).toString());

        Assert.assertEquals("null", String.valueOf((Object) null));
        Assert.assertEquals("null", String.valueOf((Decimal64) null));
        Assert.assertEquals("null", Decimal64.toString(null));
        Assert.assertEquals("null", Decimal64Utils.toString(Decimal64.toUnderlying((Decimal64) null)));

        Assert.assertEquals("NaN", String.valueOf(Decimal64.NaN));
        Assert.assertEquals("NaN", Decimal64.toString(Decimal64.NaN.negate()));
        Assert.assertEquals("Infinity", Decimal64.toString(Decimal64.POSITIVE_INFINITY));
        Assert.assertEquals("-Infinity", Decimal64.toString(Decimal64.NEGATIVE_INFINITY));

        Assert.assertEquals("0", Decimal64.toString(Decimal64.ZERO));
        Assert.assertEquals("1000000", Decimal64.toString(Decimal64.MILLION));
        Assert.assertEquals("0.01", Decimal64.toString(Decimal64.ONE_HUNDREDTH));
    }

}
