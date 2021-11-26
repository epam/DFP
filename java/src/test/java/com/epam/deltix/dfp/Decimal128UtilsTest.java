package com.epam.deltix.dfp;

import org.junit.Test;

import java.util.Random;

import static com.epam.deltix.dfp.Decimal64UtilsTest.COUNT;
import static com.epam.deltix.dfp.Decimal64UtilsTest.assertSimilar;

public class Decimal128UtilsTest {
    @Test
    public void doubleConversion() {
//        final Random random = new Random();
        final double x = Math.PI;

        //Decimal128Utils.setReturnValueFillType(Decimal128Utils.ReturnValueFillType.CALLBACK);

        Decimal128Underlying d128 = new Decimal128Underlying();
        for (int i = 0; i < 10_000_000; ++i) {
//            final double x = random.nextDouble();
            Decimal128Utils.fromDouble(x, d128);
//            final double z = Decimal128Utils.toDouble(d128);
//            assertSimilar(x, z, 1.0E-09);
        }


        long tic = System.nanoTime();
        for (int i = 0; i < 100_000_000; ++i) {
//            final double x = random.nextDouble();
            Decimal128Utils.fromDouble(x, d128);
//            final double z = Decimal128Utils.toDouble(d128);
//            assertSimilar(x, z, 1.0E-09);
        }
        long toc = System.nanoTime();

        System.out.println("Execution time: " + (toc - tic) / 1e+9);
        // JNI Set Fields: 6.9769637, 7.4652527, 7.152317, 7.308332501, 7.175216401, 7.2093531, 7.2149173
        // JNI Set callback: 17.2401215, 16.1532648, 17.3695266, 16.6589327, 16.1010652, 16.6816646, 16.3287013
    }
}
