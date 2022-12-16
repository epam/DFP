package deltix.dfp;

import org.junit.Test;

import static deltix.dfp.TestUtils.*;

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
        checkWithCoverage(NativeImpl::compare, Decimal64Utils::compareTo);
    }

    @Test
    public void isEqualMtTest() throws Exception {
        isCompareMtTest(NativeImpl::isEqual, Decimal64Utils::isEqual);
    }

    @Test
    public void isNotEqualMtTest() throws Exception {
        isCompareMtTest(NativeImpl::isNotEqual, Decimal64Utils::isNotEqual);
    }

    @Test
    public void isLessMtTest() throws Exception {
        isCompareMtTest(NativeImpl::isLess, Decimal64Utils::isLess);
    }

    @Test
    public void isLessOrEqualMtTest() throws Exception {
        isCompareMtTest(NativeImpl::isLessOrEqual, Decimal64Utils::isLessOrEqual);
    }

    @Test
    public void isGreaterMtTest() throws Exception {
        isCompareMtTest(NativeImpl::isGreater, Decimal64Utils::isGreater);
    }

    @Test
    public void isGreaterOrEqualMtTest() throws Exception {
        isCompareMtTest(NativeImpl::isGreaterOrEqual, Decimal64Utils::isGreaterOrEqual);
    }

    @Test
    public void isZeroMtTest() throws Exception {
        checkMtTest(NativeImpl::isZero, Decimal64Utils::isZero);
    }

    @Test
    public void isNonZeroMtTest() throws Exception {
        checkMtTest(NativeImpl::isNonZero, Decimal64Utils::isNonZero);
    }

    @Test
    public void isPositiveMtTest() throws Exception {
        checkMtTest(NativeImpl::isPositive, Decimal64Utils::isPositive);
    }

    @Test
    public void isNegativeMtTest() throws Exception {
        checkMtTest(NativeImpl::isNegative, Decimal64Utils::isNegative);
    }

    @Test
    public void isNonPositiveMtTest() throws Exception {
        checkMtTest(NativeImpl::isNonPositive, Decimal64Utils::isNonPositive);
    }

    @Test
    public void isNonNegativeMtTest() throws Exception {
        checkMtTest(NativeImpl::isNonNegative, Decimal64Utils::isNonNegative);
    }

    @Test
    public void isNormalMtTest() throws Exception {
        checkMtTest(NativeImpl::isNormal, Decimal64Utils::isNormal);
    }
}
