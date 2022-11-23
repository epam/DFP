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
public class BinaryOperationBenchmark {
    private long decimalValue1;
    private long decimalValue2;

    @Setup
    public void setUp() {
        final Random random = new Random();
        decimalValue1 = Decimal64Utils.fromDouble(random.nextDouble());
        decimalValue2 = Decimal64Utils.fromDouble(random.nextDouble());
    }

    @Benchmark
    public long addition() {
        return Decimal64Utils.add(decimalValue1, decimalValue2);
    }

    @Benchmark
    public long subtraction() {
        return Decimal64Utils.subtract(decimalValue1, decimalValue2);
    }

    @Benchmark
    public long multiplication() {
        return Decimal64Utils.multiply(decimalValue1, decimalValue2);
    }

    @Benchmark
    public long division() {
        return Decimal64Utils.divide(decimalValue1, decimalValue2);
    }

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
            .include(".*" + BinaryOperationBenchmark.class.getSimpleName() + ".*")
            .forks(1)
            .build();
        new Runner(opt).run();
    }
}

