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
@Warmup(time = 3, iterations = 1)
@Measurement(time = 3, iterations = 3)
@State(Scope.Thread)
public class FormattingBenchmark {
    private final StringBuilder string = new StringBuilder();
    private long decimalValue;
    @Param({"0.072876024093886854"})
    private double doubleValue;

    @Setup
    public void setUp() {
        decimalValue = Decimal64Utils.fromDouble(doubleValue);
    }

    @Benchmark
    public Appendable javaImpl() throws IOException {
        string.setLength(0);
        JavaImpl.appendTo(decimalValue, string);
        return string;
    }

    @Benchmark
    public Appendable viaDouble() throws IOException {
        string.setLength(0);
        string.append(Decimal64Utils.toDouble(decimalValue));
        return string;
    }

    @Benchmark
    public Appendable justDouble() throws IOException {
        string.setLength(0);
        string.append(doubleValue);
        return string;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(".*" + UnaryOperationBenchmark.class.getSimpleName() + ".*")
            .forks(1)
            .build();
        new Runner(opt).run();
    }
}

