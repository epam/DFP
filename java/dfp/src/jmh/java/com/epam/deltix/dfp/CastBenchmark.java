package com.epam.deltix.dfp;

import org.apache.commons.math3.random.MersenneTwister;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

import static com.epam.deltix.dfp.JavaImpl.BID_ROUNDING_TO_NEAREST;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(time = 3, iterations = 1)
@Measurement(time = 3, iterations = 3)
@State(Scope.Thread)
public class CastBenchmark {
    private double[] doubleValues;
    private long[] decimalValues;
    public static int fixedSeed = 42 * 42 * 42 * 42 * 42;

    @Setup
    public void setUp() {
        TestUtils.RandomDecimalsGenerator random = new TestUtils.RandomDecimalsGenerator(fixedSeed);
        doubleValues = new double[1000];
        decimalValues = new long[1000];
        for (int i = 0; i < doubleValues.length; ++i) {
            doubleValues[i] = Double.longBitsToDouble(random.generator.nextLong());
            decimalValues[i] = random.nextX();
        }
    }

    @Benchmark
    public void doubleToDecimalNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.fromFloat64(doubleValues[i]));
    }

    @Benchmark
    public void doubleToDecimal(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.fromDouble(doubleValues[i]));
    }

    @Benchmark
    public void decimalToDoubleNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.toFloat64(decimalValues[i]));
    }

    @Benchmark
    public void decimalToDouble(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.toDouble(decimalValues[i]));
    }

    @Benchmark
    public void int64ToDecimalNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.fromInt64(decimalValues[i] /* reinterpret decimal as just long */));
    }

    @Benchmark
    public void int64ToDecimal(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.fromLong(decimalValues[i] /* reinterpret decimal as just long */));
    }

    @Benchmark
    public void decimalToInt64Native(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.toInt64(decimalValues[i]));
    }

    @Benchmark
    public void decimalToInt64(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.toLong(decimalValues[i]));
    }

    @Benchmark
    public void scaleByPowerOfTenNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.scaleByPowerOfTen(decimalValues[i], (int) (decimalValues[i + 1] % 50)));
    }

    @Benchmark
    public void scaleByPowerOfTen(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.scaleByPowerOfTen(decimalValues[i], (int) (decimalValues[i + 1] % 50)));
    }


    @Benchmark
    public void fromFixedPointNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.fromFixedPoint64(decimalValues[i], (int) (decimalValues[i + 1] % 50)));
    }

    @Benchmark
    public void fromFixedPoint(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.fromFixedPoint(decimalValues[i], (int) (decimalValues[i + 1] % 50)));
    }


    @Benchmark
    public void toFixedPointNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.toFixedPoint(decimalValues[i], (int) (decimalValues[i + 1] % 50)));
    }

    @Benchmark
    public void toFixedPoint(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.toFixedPoint(decimalValues[i], (int) (decimalValues[i + 1] % 50)));
    }
}
