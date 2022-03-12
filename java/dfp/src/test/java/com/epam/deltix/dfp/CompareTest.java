package com.epam.deltix.dfp;

import org.junit.Test;

import static com.epam.deltix.dfp.JavaImplTest.specialValues;
import static com.epam.deltix.dfp.TestUtils.NTests;
import static com.epam.deltix.dfp.TestUtils.checkInMultipleThreads;

public class CompareTest {
    interface IDecimalCompare {
        boolean compare(final long a, final long b);
    }

    public static void compareMtTest(IDecimalCompare nativeImpl, IDecimalCompare javaImpl) throws Exception {
        for (final long x : specialValues)
            for (final long y : specialValues) {
                final boolean nativeRet = nativeImpl.compare(x, y);
                final boolean javaRet = javaImpl.compare(x, y);

                if (javaRet != nativeRet)
                    throw new RuntimeException("The decimal 0x" + Long.toHexString(x) + "L vs 0x" +
                        Long.toHexString(y) + "L = " + nativeRet + ", but java return " + javaRet);
            }

        checkInMultipleThreads(() -> {
            final TestUtils.RandomDecimalsGenerator random = new TestUtils.RandomDecimalsGenerator();
            for (int i = 0; i < NTests; ++i) {
                final long x = random.nextX();
                final long y = random.generator.nextDouble() < 0.1 ? x : random.nextY();

                final boolean nativeRet = nativeImpl.compare(x, y);
                final boolean javaRet = javaImpl.compare(x, y);

                if (javaRet != nativeRet)
                    throw new RuntimeException("The decimal 0x" + Long.toHexString(x) + "L vs 0x" +
                        Long.toHexString(y) + "L = " + nativeRet + ", but java return " + javaRet);
            }
        });
    }

    interface IDecimalCheck {
        boolean check(final long x);
    }

    public static void checkMtTest(IDecimalCheck nativeImpl, IDecimalCheck javaImpl) throws Exception {
        for (final long x : specialValues) {
            final boolean nativeRet = nativeImpl.check(x);
            final boolean javaRet = javaImpl.check(x);

            if (javaRet != nativeRet)
                throw new RuntimeException("The decimal 0x" + Long.toHexString(x) + "L = " +
                    nativeRet + ", but java return " + javaRet);
        }

        checkInMultipleThreads(() -> {
            final TestUtils.RandomDecimalsGenerator random = new TestUtils.RandomDecimalsGenerator();
            for (int i = 0; i < NTests; ++i) {
                final long x = random.nextX();

                final boolean nativeRet = nativeImpl.check(x);
                final boolean javaRet = javaImpl.check(x);

                if (javaRet != nativeRet)
                    throw new RuntimeException("The decimal 0x" + Long.toHexString(x) + "L = " +
                        nativeRet + ", but java return " + javaRet);
            }
        });
    }

    @Test
    public void isEqualMtTest() throws Exception {
        compareMtTest(NativeImpl::isEqual, JavaImplCmp::bid64_quiet_equal);
    }

    @Test
    public void isNotEqualMtTest() throws Exception {
        compareMtTest(NativeImpl::isNotEqual, JavaImplCmp::bid64_quiet_not_equal);
    }

    @Test
    public void isLessMtTest() throws Exception {
        compareMtTest(NativeImpl::isLess, JavaImplCmp::bid64_quiet_less);
    }

    @Test
    public void isLessOrEqualMtTest() throws Exception {
        compareMtTest(NativeImpl::isLessOrEqual, JavaImplCmp::bid64_quiet_less_equal);
    }

    @Test
    public void isGreaterMtTest() throws Exception {
        compareMtTest(NativeImpl::isGreater, JavaImplCmp::bid64_quiet_greater);
    }

    @Test
    public void isGreaterOrEqualMtTest() throws Exception {
        compareMtTest(NativeImpl::isGreaterOrEqual, JavaImplCmp::bid64_quiet_greater_equal);
    }

    @Test
    public void isZeroMtTest() throws Exception {
        checkMtTest(NativeImpl::isZero, JavaImplCmp::bid64_isZero);
    }

    @Test
    public void isNonZeroMtTest() throws Exception {
        checkMtTest(NativeImpl::isNonZero, x -> JavaImplCmp.bid64_quiet_not_equal(x, Decimal64Utils.ZERO));
    }

    @Test
    public void isPositiveMtTest() throws Exception {
        checkMtTest(NativeImpl::isPositive, x -> JavaImplCmp.bid64_quiet_greater(x, Decimal64Utils.ZERO));
    }

    @Test
    public void isNegativeMtTest() throws Exception {
        checkMtTest(NativeImpl::isNegative, x -> JavaImplCmp.bid64_quiet_less(x, Decimal64Utils.ZERO));
    }

    @Test
    public void isNonPositiveMtTest() throws Exception {
        checkMtTest(NativeImpl::isNonPositive, x -> JavaImplCmp.bid64_quiet_less_equal(x, Decimal64Utils.ZERO));
    }

    @Test
    public void isNonNegativeMtTest() throws Exception {
        checkMtTest(NativeImpl::isNonNegative, x -> JavaImplCmp.bid64_quiet_greater_equal(x, Decimal64Utils.ZERO));
    }
}
