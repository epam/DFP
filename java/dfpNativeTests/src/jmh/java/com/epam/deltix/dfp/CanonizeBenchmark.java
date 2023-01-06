package com.epam.deltix.dfp;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(time = 2, iterations = 4)
@Measurement(time = 2, iterations = 4)
@State(Scope.Thread)
@Fork(3)
public class CanonizeBenchmark {
    /* 10., 1000_000., 123.456789123, 1.23, null(=NaN) */
    @Param({"3584865303386914826", "3584865303387914816", "3503800633551035011", "3566850904877432955", "-128"})
    private long decimalValue;

    @Benchmark
    public long canonize() {
        return Decimal64Utils.canonize(decimalValue);
    }

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
            .include(".*" + UnaryOperationBenchmark.class.getSimpleName() + ".*")
            .forks(1)
            .build();
        new Runner(opt).run();
    }
}

