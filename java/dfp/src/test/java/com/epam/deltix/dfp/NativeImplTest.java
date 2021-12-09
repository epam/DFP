package com.epam.deltix.dfp;

import org.junit.Test;

import static com.epam.deltix.dfp.TestUtils.assertDecimalEqual;
import static com.epam.deltix.dfp.TestUtils.assertDecimalIdentical;

public class NativeImplTest {

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
}
