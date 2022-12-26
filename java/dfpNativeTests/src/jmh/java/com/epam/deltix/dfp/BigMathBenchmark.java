package com.epam.deltix.dfp;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(time = 3, iterations = 1)
@Measurement(time = 3, iterations = 3)
public class BigMathBenchmark {
    final static MathContext bigDecimalMathContext = MathContext.DECIMAL64;
    final static MathContext bigDecimalMathContextTwice = MathContext.DECIMAL128;

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        @Param({"341971653"})
        public int randomSeed;

        @Param({"1000", "1000000"})
        public int dataSize;

        /**
         * Sing ('+' positive only; '-' negative only; '*' - random)  /  Mantissa minimal digits number  /
         * Mantissa maximal digits number  /  Minimal exponent value  /  Maximal exponent value
         */
        @Param({"*/8/8/-2/-2", "*/12/12/-2/-4", "+/16/16/-4/-8"})
        public String format;

        public long[] decimal64Underlying;
        public Decimal64[] decimal64;
        public BigDecimal[] bigDecimal;


        @Setup(Level.Trial)
        public void setUp() {
            final Random random = new Random(randomSeed);

            decimal64Underlying = new long[dataSize + 2]; // Additional elements for exactly dataSize operations
            decimal64 = new Decimal64[decimal64Underlying.length];
            bigDecimal = new BigDecimal[decimal64Underlying.length];

            final String[] formatParts = format.split("/");
            if (formatParts.length != 5)
                throw new RuntimeException("Unsupported data format(=" + format + ").");

            final char signChar;
            if (formatParts[0].equals("+") || formatParts[0].equals("-") || formatParts[0].equals("*"))
                signChar = formatParts[0].charAt(0);
            else
                throw new RuntimeException("Unsupported sign part(=" + formatParts[0] + ") in data format(=" + format + ").");

            int mantissaDigitsNumberMin = Integer.parseInt(formatParts[1]);
            int mantissaDigitsNumberMax = Integer.parseInt(formatParts[2]);
            if (mantissaDigitsNumberMax < mantissaDigitsNumberMin) { // Make it properly ordered for random.nextInt()
                final int t = mantissaDigitsNumberMax;
                mantissaDigitsNumberMax = mantissaDigitsNumberMin;
                mantissaDigitsNumberMin = t;
            }
            final int mantissaDigitsNumberRange = mantissaDigitsNumberMax + 1 - mantissaDigitsNumberMin;

            int exponentMin = Integer.parseInt(formatParts[3]);
            int exponentMax = Integer.parseInt(formatParts[4]);
            if (exponentMax < exponentMin) { // Make it properly ordered for random.nextInt()
                final int t = exponentMax;
                exponentMax = exponentMin;
                exponentMin = t;
            }
            final int exponentRange = exponentMax + 1 - exponentMin;

            final char[] mantissa = new char[mantissaDigitsNumberMax + 1];
            for (int i = 0; i < decimal64Underlying.length; ++i) {
                final int digitsNumber = mantissaDigitsNumberMin + random.nextInt(mantissaDigitsNumberRange);
                final int exponent = exponentMin + random.nextInt(exponentRange);

                int mi = 0;

                mantissa[mi++] = signChar != '*' ? signChar : (random.nextInt(2) > 0 ? '+' : '-');

                for (int di = 0; di < digitsNumber; ++di) {
                    mantissa[mi++] = di == 0 || di == digitsNumber - 1
                        ? (char) ('1' + random.nextInt(9))
                        : (char) ('0' + random.nextInt(10));
                }

                final String number = String.copyValueOf(mantissa, 0, mi) + "e" + (exponent >= 0 ? "+" : "") + exponent;

                decimal64Underlying[i] = Decimal64Utils.parse(number);
                decimal64[i] = Decimal64.parse(number); // Of course, Decimal64.fromUnderlying() in much faster, but let's operate with it as black box.
                bigDecimal[i] = new BigDecimal(number, bigDecimalMathContext);
            }
        }
    }

    @Benchmark
    public static void nop(final Blackhole blackhole, final BenchmarkState state) {
        final long[] values = state.decimal64Underlying;
        for (int i = 0, ie = state.dataSize; i < ie; ++i)
            blackhole.consume(values[i]);
    }

    //region Add
    @Benchmark
    public static void addDecimal64Underlying(final Blackhole blackhole, final BenchmarkState state) {
        final long[] values = state.decimal64Underlying;
        for (int i = 0, ie = state.dataSize; i < ie; ++i)
            blackhole.consume(Decimal64Utils.add(values[i], values[i + 1]));
    }

    @Benchmark
    public static void addDecimal64(final Blackhole blackhole, final BenchmarkState state) {
        final Decimal64[] values = state.decimal64;
        for (int i = 0, ie = state.dataSize; i < ie; ++i)
            blackhole.consume(values[i].add(values[i + 1]));
    }

    @Benchmark
    public static void addBigDecimal(final Blackhole blackhole, final BenchmarkState state) {
        final BigDecimal[] values = state.bigDecimal;
        for (int i = 0, ie = state.dataSize; i < ie; ++i)
            blackhole.consume(values[i].add(values[i + 1], bigDecimalMathContext));
    }
    //endregion

    //region Mul
    @Benchmark
    public static void mulDecimal64Underlying(final Blackhole blackhole, final BenchmarkState state) {
        final long[] values = state.decimal64Underlying;
        for (int i = 0, ie = state.dataSize; i < ie; ++i)
            blackhole.consume(Decimal64Utils.multiply(values[i], values[i + 1]));
    }

    @Benchmark
    public static void mulDecimal64(final Blackhole blackhole, final BenchmarkState state) {
        final Decimal64[] values = state.decimal64;
        for (int i = 0, ie = state.dataSize; i < ie; ++i)
            blackhole.consume(values[i].multiply(values[i + 1]));
    }

    @Benchmark
    public static void mulBigDecimal(final Blackhole blackhole, final BenchmarkState state) {
        final BigDecimal[] values = state.bigDecimal;
        for (int i = 0, ie = state.dataSize; i < ie; ++i)
            blackhole.consume(values[i].multiply(values[i + 1], bigDecimalMathContext));
    }
    //endregion

    //region Div
    @Benchmark
    public static void divDecimal64Underlying(final Blackhole blackhole, final BenchmarkState state) {
        final long[] values = state.decimal64Underlying;
        for (int i = 0, ie = state.dataSize; i < ie; ++i)
            blackhole.consume(Decimal64Utils.divide(values[i], values[i + 1]));
    }

    @Benchmark
    public static void divDecimal64(final Blackhole blackhole, final BenchmarkState state) {
        final Decimal64[] values = state.decimal64;
        for (int i = 0, ie = state.dataSize; i < ie; ++i)
            blackhole.consume(values[i].divide(values[i + 1]));
    }

    @Benchmark
    public static void divBigDecimal(final Blackhole blackhole, final BenchmarkState state) {
        final BigDecimal[] values = state.bigDecimal;
        for (int i = 0, ie = state.dataSize; i < ie; ++i)
            blackhole.consume(values[i].divide(values[i + 1], bigDecimalMathContext));
    }
    //endregion

    //region Fma - for portfolio calculation
    @Benchmark
    public static void fmaDecimal64Underlying(final Blackhole blackhole, final BenchmarkState state) {
        final long[] values = state.decimal64Underlying;
        for (int i = 0, ie = state.dataSize; i < ie; ++i)
            blackhole.consume(Decimal64Utils.multiplyAndAdd(values[i], values[i + 1], values[i + 2]));
    }

    @Benchmark
    public static void fmaDecimal64(final Blackhole blackhole, final BenchmarkState state) {
        final Decimal64[] values = state.decimal64;
        for (int i = 0, ie = state.dataSize; i < ie; ++i)
            blackhole.consume(values[i].multiplyAndAdd(values[i + 1], values[i + 2]));
    }

    @Benchmark
    public static void fmaBigDecimal(final Blackhole blackhole, final BenchmarkState state) {
        final BigDecimal[] values = state.bigDecimal;
        for (int i = 0, ie = state.dataSize; i < ie; ++i)
            blackhole.consume(values[i].multiply(values[i + 1], bigDecimalMathContextTwice).add(values[i + 2], bigDecimalMathContext));
    }
    //endregion

    //region Compare
    @Benchmark
    public static void compareDecimal64Underlying(final Blackhole blackhole, final BenchmarkState state) {
        final long[] values = state.decimal64Underlying;
        for (int i = 0, ie = state.dataSize; i < ie; ++i)
            blackhole.consume(Decimal64Utils.compareTo(values[i], values[i + 1]));
    }

    @Benchmark
    public static void compareDecimal64(final Blackhole blackhole, final BenchmarkState state) {
        final Decimal64[] values = state.decimal64;
        for (int i = 0, ie = state.dataSize; i < ie; ++i)
            blackhole.consume(values[i].compareTo(values[i + 1]));
    }

    @Benchmark
    public static void compareBigDecimal(final Blackhole blackhole, final BenchmarkState state) {
        final BigDecimal[] values = state.bigDecimal;
        for (int i = 0, ie = state.dataSize; i < ie; ++i)
            blackhole.consume(values[i].compareTo(values[i + 1]));
    }
    //endregion
}
