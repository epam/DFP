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
    public static int fixedSeed = 42 * 42 * 42 * 42 * 42;

    @Setup
    public void setUp() {
        final MersenneTwister random = new MersenneTwister(fixedSeed);
        doubleValues = new double[1004];
        for (int i = 0; i < doubleValues.length; ++i)
            doubleValues[i] = Double.longBitsToDouble(random.nextLong());
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
}
