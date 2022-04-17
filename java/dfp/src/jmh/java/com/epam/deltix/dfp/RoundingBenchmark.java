package com.epam.deltix.dfp;

import org.apache.commons.math3.random.MersenneTwister;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

import static com.epam.deltix.dfp.Decimal64Utils.MAX_SIGNIFICAND_DIGITS;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(time = 3, iterations = 1)
@Measurement(time = 3, iterations = 3)
@State(Scope.Thread)
public class RoundingBenchmark {
    private long[] decimalValues;
    public static int fixedSeed = 42 * 42 * 42 * 42 * 42;

    @Setup
    public void setUp() {
        final TestUtils.RandomDecimalsGenerator generator =
            new TestUtils.RandomDecimalsGenerator(new MersenneTwister(fixedSeed), 64,
                -MAX_SIGNIFICAND_DIGITS, -MAX_SIGNIFICAND_DIGITS + 1);
        decimalValues = new long[1000];
        for (int i = 0; i < decimalValues.length; ++i)
            decimalValues[i] = generator.nextX();
    }

    @Benchmark
    public void roundTowardsPositiveInfinityNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.roundTowardsPositiveInfinity(decimalValues[i]));
    }

    @Benchmark
    public void roundTowardsPositiveInfinity(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.roundTowardsPositiveInfinity(decimalValues[i]));
    }

    @Benchmark
    public void roundTowardsNegativeInfinityNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.roundTowardsNegativeInfinity(decimalValues[i]));
    }

    @Benchmark
    public void roundTowardsNegativeInfinity(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.roundTowardsNegativeInfinity(decimalValues[i]));
    }

    @Benchmark
    public void roundTowardsZeroNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.roundTowardsZero(decimalValues[i]));
    }

    @Benchmark
    public void roundTowardsZero(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.roundTowardsZero(decimalValues[i]));
    }

    @Benchmark
    public void roundToNearestTiesAwayFromZeroNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.roundToNearestTiesAwayFromZero(decimalValues[i]));
    }

    @Benchmark
    public void roundToNearestTiesAwayFromZero(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.roundToNearestTiesAwayFromZero(decimalValues[i]));
    }

    @Benchmark
    public void roundToNearestTiesToEvenNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.roundToNearestTiesToEven(decimalValues[i]));
    }

    @Benchmark
    public void roundToNearestTiesToEven(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.roundToNearestTiesToEven(decimalValues[i]));
    }
}
