package com.epam.deltix.dfp;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
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
@Fork(3)
@State(Scope.Thread)
public class ConversionBenchmark {
    private double doubleValue;
    private double doubleRoundableValue;
    private long infValue;
    private long nanValue;
    private long decimalValue;
    private long decimalRoundableValue;
    private long decimalRoundableValue2;
    private long longIntegerValue;
    private int integerValue;
    private long hugeLongValue;

    @Setup
    public void setUp() {
        doubleValue = new Random().nextDouble();
        doubleRoundableValue = 920000000000000.0;
        infValue = Decimal64Utils.NEGATIVE_INFINITY;
        nanValue = Decimal64Utils.NaN;
        decimalValue = Decimal64Utils.fromDecimalDouble(doubleValue);
        longIntegerValue = (long) (10000.0 * doubleValue);
        integerValue = (int) (10000.0 * doubleValue);
        hugeLongValue = 100000000000000000L + (long) (10000.0 * doubleValue) * 10000;
        decimalRoundableValue = Decimal64Utils.fromLong(920000000000000L);
        decimalRoundableValue2 = Decimal64Utils.fromFixedPoint(9200000000000000L, 8);
    }

    public long fromDouble() {
        return Decimal64Utils.fromDouble(doubleValue);
    }

    @Benchmark
    public double toDouble() {
        return Decimal64Utils.toDouble(decimalValue);
    }

    @Benchmark
    public void fromFixedPointLong(final Blackhole blackhole) {
        blackhole.consume(Decimal64Utils.fromFixedPoint(longIntegerValue, 4));
    }

    @Benchmark
    public void fromFixedPointHugeLong(final Blackhole blackhole) {
        blackhole.consume(Decimal64Utils.fromFixedPoint(hugeLongValue, 4));
    }

    @Benchmark
    public void fromFixedPointInt(final Blackhole blackhole) {
        blackhole.consume(Decimal64Utils.fromFixedPoint(integerValue, 4));
    }

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void toFixedPoint(final Blackhole blackhole) {
        blackhole.consume(Decimal64Utils.toFixedPoint(decimalValue, 4));
    }

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
            .include(".*" + ConversionBenchmark.class.getSimpleName() + ".*")
            .forks(1)
            .build();
        new Runner(opt).run();
    }
}

