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
    public void doubleToDecimalJava(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(JavaImplCastBinary64.binary64_to_bid64(doubleValues[i], BID_ROUNDING_TO_NEAREST));
    }

    @Benchmark
    public void decimalToDoubleNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.toFloat64(decimalValues[i]));
    }

    @Benchmark
    public void decimalToDoubleJava(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(JavaImplCastBinary64.bid64_to_binary64(decimalValues[i], BID_ROUNDING_TO_NEAREST));
    }

    @Benchmark
    public void int64ToDecimalNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.fromInt64(decimalValues[i] /* reinterpret decimal as just long */ ));
    }

    @Benchmark
    public void int64ToDecimalJava(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(JavaImplCast.bid64_from_int64(decimalValues[i] /* reinterpret decimal as just long */, BID_ROUNDING_TO_NEAREST));
    }

    @Benchmark
    public void decimalToInt64Native(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.toInt64(decimalValues[i]));
    }

    @Benchmark
    public void decimalToInt64Java(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(JavaImplCast.bid64_to_int64_xint(decimalValues[i]));
    }
}
