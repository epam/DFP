package com.epam.deltix.dfp;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(time = 5, timeUnit = TimeUnit.SECONDS, iterations = 2)
@Measurement(time = 5, timeUnit = TimeUnit.SECONDS, iterations = 5)
@State(Scope.Thread)
@Fork(2)
public class UnaryOperationBenchmark {
    private long decimalValue;

    @Setup
    public void setUp() {
        decimalValue = Decimal64Utils.fromDouble(new Random().nextDouble());
    }

    @Benchmark
    public long negate() {
        return Decimal64Utils.negate(decimalValue);
    }

    @Benchmark
    public long abs() {
        return Decimal64Utils.abs(decimalValue);
    }

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
            .include(".*" + UnaryOperationBenchmark.class.getSimpleName() + ".*")
            .forks(1)
            .build();
        new Runner(opt).run();
    }
}

