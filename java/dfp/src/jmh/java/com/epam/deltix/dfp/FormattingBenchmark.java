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

import static com.epam.deltix.dfp.MathBenchmark.fixedSeed;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(time = 3, iterations = 1)
@Measurement(time = 3, iterations = 3)
@State(Scope.Thread)
public class FormattingBenchmark {
    private final StringBuilder stringBuilder = new StringBuilder();
    private long decimalValue;
    @Param({"0.072876024093886854"})
    private double doubleValue;
    private long[] decimalValues;

    @Setup
    public void setUp() {
        decimalValue = Decimal64Utils.fromDouble(doubleValue);

        final TestUtils.RandomDecimalsGenerator random = new TestUtils.RandomDecimalsGenerator(fixedSeed);
        decimalValues = new long[1000];
        for (int i = 0; i < decimalValues.length; ++i)
            decimalValues[i] = random.nextX();
    }

    @Benchmark
    public Appendable appendToRefImplRet() throws IOException {
        stringBuilder.setLength(0);
        JavaImpl.appendToRefImpl(decimalValue, stringBuilder);
        return stringBuilder;
    }

    @Benchmark
    public void appendToRefImplBlackHole(Blackhole bh) throws IOException {
        stringBuilder.setLength(0);
        bh.consume(JavaImpl.appendToRefImpl(decimalValue, stringBuilder));
    }

    @Benchmark
    public void fastAppendToAppendable(Blackhole bh) throws IOException {
        stringBuilder.setLength(0);
        bh.consume(JavaImpl.fastAppendToAppendable(decimalValue, stringBuilder));
    }

    @Benchmark
    public void fastAppendToStringBuilder(Blackhole bh) {
        stringBuilder.setLength(0);
        bh.consume(JavaImpl.fastAppendToStringBuilder(decimalValue, stringBuilder));
    }

    @Benchmark
    public void appendDecimal(Blackhole bh) {
        stringBuilder.setLength(0);
        bh.consume(stringBuilder.append(Decimal64Utils.toString(decimalValue)));
    }

    @Benchmark
    public void appendViaDouble(Blackhole bh) {
        stringBuilder.setLength(0);
        bh.consume(stringBuilder.append(Decimal64Utils.toDouble(decimalValue)));
    }

    @Benchmark
    public void appendJustDouble(Blackhole bh) {
        stringBuilder.setLength(0);
        bh.consume(stringBuilder.append(doubleValue));
    }

    @Benchmark
    public void toStringJavaImpl(Blackhole bh) {
        for (int i = 0; i < decimalValues.length; ++i) {
            try {
                bh.consume(JavaImpl.appendToRefImpl(decimalValues[i], new StringBuilder()).toString());
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Benchmark
    public void toStringJavaFastImpl(Blackhole bh) {
        for (int i = 0; i < decimalValues.length; ++i)
            bh.consume(JavaImpl.fastToString(decimalValues[i]));
    }

    @Benchmark
    public void toScientificStringJavaFastImpl(Blackhole bh) {
        for (int i = 0; i < decimalValues.length; ++i)
            bh.consume(JavaImpl.fastToScientificString(decimalValues[i]));
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(".*" + FormattingBenchmark.class.getSimpleName() + ".*")
            .forks(1)
            .build();
        new Runner(opt).run();
    }
}

