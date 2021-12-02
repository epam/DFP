package com.epam.deltix.dfp;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(time = 2, iterations = 5)
@Measurement(time = 2, iterations = 5)
@State(Scope.Thread)
@Fork(2)
// runtime around 14 min on 3770

public class ParsingBenchmark {
    @Param({"0.072876024093886854", "9.2", "0.00000000000000000000000000092", "1048576", "1200000000000000000000000000"})
    private String stringValue;

    @Benchmark
    @Decimal
    public long parseDecimal() throws IOException {
        return Decimal64Utils.parse(stringValue);
    }

    @Benchmark
    @Decimal
    public long parseDecimalViaDouble() throws IOException {
        return Decimal64Utils.fromDouble(Double.parseDouble(stringValue));
    }

    @Benchmark
    @Decimal
    public long parseDecimalViaDoubleCorrected() throws IOException {
        return Decimal64Utils.fromDecimalDouble(Double.parseDouble(stringValue));
    }

    @Benchmark
    public double parseDouble() throws IOException {
        return Double.parseDouble(stringValue);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(".*" + UnaryOperationBenchmark.class.getSimpleName() + ".*")
            .forks(1)
            .build();
        new Runner(opt).run();
    }
}

