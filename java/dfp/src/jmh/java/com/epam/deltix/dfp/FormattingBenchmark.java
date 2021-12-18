package com.epam.deltix.dfp;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.util.Random;
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
    private long[] decimalValues;

    @Setup
    public void setUp() {
        decimalValue = Decimal64Utils.fromDouble(doubleValue);

        final Random random = new Random();

        decimalValues = new long[1000];
        for (int i = 0; i < decimalValues.length; ++i)
            decimalValues[i] = Decimal64Utils.scaleByPowerOfTen(
                Decimal64Utils.fromDouble(random.nextDouble() * 2 - 1),
                random.nextInt(30 * 2 + 1) - 30);
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

    @Benchmark
    public void javaImplToString(Blackhole bh) {
        for (int i=0; i<decimalValues.length; ++i)
            bh.consume(Decimal64Utils.toString(decimalValues[i]));
    }

    @Benchmark
    public void javaImplToStringFast(Blackhole bh) {
        for (int i=0; i<decimalValues.length; ++i)
            bh.consume(JavaImpl.toStringFast(decimalValues[i]));
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(".*" + FormattingBenchmark.class.getSimpleName() + ".*")
            .forks(1)
            .build();
        new Runner(opt).run();
    }
}

