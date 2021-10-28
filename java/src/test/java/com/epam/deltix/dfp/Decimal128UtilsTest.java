package com.epam.deltix.dfp;

import org.junit.Test;

import java.util.Random;

import static com.epam.deltix.dfp.Decimal64UtilsTest.COUNT;
import static com.epam.deltix.dfp.Decimal64UtilsTest.assertSimilar;

public class Decimal128UtilsTest {
    @Test
    public void doubleConversion() {
        Random random = new Random();

        Decimal128 d128 = new Decimal128();
        for (int i = 0; i < COUNT; ++i) {
            final double x = random.nextDouble();
            Decimal128Utils.fromDouble(x, d128);
            final double z = Decimal128Utils.toDouble(d128);
            assertSimilar(x, z, 1.0E-09);
        }
    }
}
