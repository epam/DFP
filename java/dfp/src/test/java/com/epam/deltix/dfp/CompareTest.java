package com.epam.deltix.dfp;

import org.junit.Test;

import static com.epam.deltix.dfp.JavaImplTest.specialValues;
import static com.epam.deltix.dfp.TestUtils.NTests;
import static com.epam.deltix.dfp.TestUtils.checkInMultipleThreads;

public class CompareTest {
    interface IDecimalCompare {
        boolean compare(final long a, final long b);
    }

    public static void isCompareMtTest(final IDecimalCompare nativeImpl, final IDecimalCompare javaImpl) throws Exception {
        for (final long x : specialValues)
            for (final long y : specialValues)
                isCompareMtTestCase(nativeImpl, javaImpl, x, y);

        checkInMultipleThreads(() -> {
            final TestUtils.RandomDecimalsGenerator random = new TestUtils.RandomDecimalsGenerator();
            for (int i = 0; i < NTests; ++i)
                isCompareMtTestCase(nativeImpl, javaImpl, random.nextX(),
                    random.generator.nextDouble() < 0.1 ? random.getX() : random.nextY());
        });
    }

    static void isCompareMtTestCase(final IDecimalCompare nativeImpl, final IDecimalCompare javaImpl,
                                    final long x, final long y) {
        final boolean nativeRet = nativeImpl.compare(x, y);
        final boolean javaRet = javaImpl.compare(x, y);

        if (javaRet != nativeRet)
            throw new RuntimeException("The decimal 0x" + Long.toHexString(x) + "L vs 0x" +
                Long.toHexString(y) + "L = " + nativeRet + ", but java return " + javaRet);
    }

    interface IDecimalCheck {
        boolean check(final long x);
    }

    public static void checkMtTest(final IDecimalCheck nativeImpl, final IDecimalCheck javaImpl) throws Exception {
        for (final long x : specialValues)
            checkMtTestCase(nativeImpl, javaImpl, x);

        checkInMultipleThreads(() -> {
            final TestUtils.RandomDecimalsGenerator random = new TestUtils.RandomDecimalsGenerator();
            for (int i = 0; i < NTests; ++i)
                checkMtTestCase(nativeImpl, javaImpl, random.nextX());
        });
    }

    static void checkMtTestCase(final IDecimalCheck nativeImpl, final IDecimalCheck javaImpl, final long x) {
        final boolean nativeRet = nativeImpl.check(x);
        final boolean javaRet = javaImpl.check(x);

        if (javaRet != nativeRet)
            throw new RuntimeException("The decimal 0x" + Long.toHexString(x) + "L = " +
                nativeRet + ", but java return " + javaRet);
    }

    @Test
    public void compareMtTest() throws Exception {
        for (final long x : specialValues)
            for (final long y : specialValues)
                compareCheck(x, y);

        checkInMultipleThreads(() -> {
            final TestUtils.RandomDecimalsGenerator random = new TestUtils.RandomDecimalsGenerator();
            for (int i = 0; i < NTests; ++i)
                compareCheck(random.nextX(), random.generator.nextDouble() < 0.1 ? random.getX() : random.nextY());
        });
    }

    static void compareCheck(final long x, final long y) {
        final int nativeRet = NativeImpl.compare(x, y);
        final int javaRet = JavaImplCmp.bid64_quiet_compare(x, y);

        if (javaRet != nativeRet)
            throw new RuntimeException("The decimal 0x" + Long.toHexString(x) + "L vs 0x" +
                Long.toHexString(y) + "L = " + nativeRet + ", but java return " + javaRet);
    }

    @Test
    public void isEqualMtTest() throws Exception {
        isCompareMtTest(NativeImpl::isEqual, JavaImplCmp::bid64_quiet_equal);
    }

    @Test
    public void isNotEqualMtTest() throws Exception {
        isCompareMtTest(NativeImpl::isNotEqual, JavaImplCmp::bid64_quiet_not_equal);
    }

    @Test
    public void isLessMtTest() throws Exception {
        isCompareMtTest(NativeImpl::isLess, JavaImplCmp::bid64_quiet_less);
    }

    @Test
    public void isLessOrEqualMtTest() throws Exception {
        isCompareMtTest(NativeImpl::isLessOrEqual, JavaImplCmp::bid64_quiet_less_equal);
    }

    @Test
    public void isGreaterMtTest() throws Exception {
        isCompareMtTest(NativeImpl::isGreater, JavaImplCmp::bid64_quiet_greater);
    }

    @Test
    public void isGreaterOrEqualMtTest() throws Exception {
        isCompareMtTest(NativeImpl::isGreaterOrEqual, JavaImplCmp::bid64_quiet_greater_equal);
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
        checkMtTest(NativeImpl::isPositive, JavaImplCmp::isPositive);
    }

    @Test
    public void isNegativeMtTest() throws Exception {
        checkMtTest(NativeImpl::isNegative, JavaImplCmp::isNegative);
    }

    @Test
    public void isNonPositiveMtTest() throws Exception {
        checkMtTest(NativeImpl::isNonPositive, JavaImplCmp::isNonPositive);
    }

    @Test
    public void isNonNegativeMtTest() throws Exception {
        checkMtTest(NativeImpl::isNonNegative, JavaImplCmp::isNonNegative);
    }
}
