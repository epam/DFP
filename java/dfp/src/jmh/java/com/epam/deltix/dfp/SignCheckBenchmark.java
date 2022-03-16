package com.epam.deltix.dfp;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(time = 5, timeUnit = TimeUnit.SECONDS, iterations = 2)
@Measurement(time = 5, timeUnit = TimeUnit.SECONDS, iterations = 5)
@State(Scope.Thread)
public class SignCheckBenchmark {
    @State(Scope.Thread)
    public static class BenchmarkState {
        public @Decimal
        final long value = Decimal64Utils.fromDouble(Math.PI);
    }

    @Benchmark
    public static void javaIsPositive(final BenchmarkState state, final Blackhole blackhole) {
        blackhole.consume(JavaImplCmp.isPositive(state.value));
    }

    @Benchmark
    public static void nativeIsPositive(final BenchmarkState state, final Blackhole blackhole) {
        blackhole.consume(NativeImpl.isPositive(state.value));
    }
}
