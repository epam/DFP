package deltix.dfp;

import org.junit.Test;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Random;

public class BigDecimalTest {
    @Test
    public void conversionToBigDecimalAndBack() {
        final int seed = new SecureRandom().nextInt();
        final Random random = new Random(seed);
        @Decimal final long d64Pi = Decimal64Utils.fromDouble(Math.PI);

        final double testLogRange = Math.log(Double.MAX_VALUE / Math.PI);

        for (@Decimal final long testValue : new long[]{
            Decimal64Utils.ZERO,
            Decimal64Utils.MIN_VALUE,
            Decimal64Utils.MAX_VALUE,
            Decimal64Utils.MIN_POSITIVE_VALUE,
            Decimal64Utils.MAX_NEGATIVE_VALUE
        }) {
            toBigDecimalAndBack(testValue);
            toBigDecimalAndBack(Decimal64Utils.negate(testValue));
        }

        final long tic = System.nanoTime();
        while (System.nanoTime() - tic < 5 * 1000_000_000L) {
            for (int i = 0; i < 1000_000; ++i) {
                double testDouble = Math.exp((random.nextDouble() * 2 - 1) * testLogRange);
                if (random.nextInt(1000) < 500)
                    testDouble = -testDouble;
                final long aD64 = Decimal64Utils.multiply(Decimal64Utils.fromDouble(testDouble), d64Pi); // Expand mantissa

                toBigDecimalAndBack(aD64);
            }
        }
    }

    private static void toBigDecimalAndBack(@Decimal final long aD64) {
        final String aStr = Decimal64Utils.toString(aD64);

        final BigDecimal big = Decimal64Utils.toBigDecimal(aD64);

        String bigStr = big.toPlainString();
        if (bigStr.contains("."))
            bigStr = trimBackZerosAndDot(bigStr);

        if (!aStr.equals(bigStr))
            throw new RuntimeException("Conversion of " + aD64 + " error: " + aStr + " != " + bigStr);

        final long bD64 = Decimal64Utils.fromBigDecimalExact(big);

        if (!Decimal64Utils.equals(aD64, bD64))
            throw new RuntimeException("Restoration fail on " + aD64 + ": !equals(" + aStr + ", " + Decimal64Utils.toString(bD64) + ")");
    }

    private static String trimBackZerosAndDot(String str) {
        int trimIndex = str.length();
        while (trimIndex > 0 && str.charAt(trimIndex - 1) == '0')
            trimIndex--;
        if (trimIndex != str.length()) {
            if (trimIndex > 0 && str.charAt(trimIndex - 1) == '.')
                trimIndex--;
            str = str.substring(0, trimIndex);
        }
        return str;
    }

    @Test
    public void bigDecimalToDecimal64CornerCases() {
        final BigDecimal dbPi = BigDecimal.valueOf(Math.PI);

        for (final BigDecimal testBigDecimal : new BigDecimal[]{
            BigDecimal.valueOf(Long.MAX_VALUE, 0).add(BigDecimal.valueOf(123L, 0)),
            BigDecimal.valueOf(Long.MIN_VALUE, 0),
            BigDecimal.valueOf(Long.MAX_VALUE, 0),
            BigDecimal.ZERO,
            BigDecimal.valueOf(Integer.MAX_VALUE / 2, 200),
            BigDecimal.valueOf(Integer.MAX_VALUE / 2, -200),
            BigDecimal.valueOf(Long.MAX_VALUE / 2, 100),
            BigDecimal.valueOf(Long.MAX_VALUE / 2, -100),
            BigDecimal.valueOf(Long.MAX_VALUE / 2, 100).multiply(dbPi).multiply(dbPi),
        }) {
            toDecimal64(testBigDecimal);
            toDecimal64(testBigDecimal.negate());
        }
    }

    private static void toDecimal64(final BigDecimal a) {
        @Decimal final long b = Decimal64Utils.fromBigDecimal(a);

        final String aStr = a.toPlainString();
        String bStr = Decimal64Utils.toString(b);
        if (!aStr.contains(".") && aStr.length() != bStr.length())
            throw new RuntimeException("BigDecimal(=" + a + ") conversion to Decimal64 order mismatch.");

        bStr = trimBackZerosAndDot(Decimal64Utils.toString(b));
        if (!bStr.isEmpty())
            bStr = bStr.substring(0, bStr.length() - 1); // Remove rounded symbol

        if (!aStr.startsWith(bStr))
            throw new RuntimeException("BigDecimal(=" + a + ") conversion to Decimal64 precision loss.");
    }
}
