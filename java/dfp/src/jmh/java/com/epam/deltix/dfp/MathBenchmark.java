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
public class MathBenchmark {
    private long[] decimalValues;

    @Setup
    public void setUp() {
        final Random random = new Random();

        decimalValues = new long[1003];
        for (int i = 0; i < decimalValues.length; ++i)
            decimalValues[i] = Decimal64Utils.fromFixedPoint(random.nextLong(), -(random.nextInt(80) - 40 - 15));
    }

    @Benchmark
    public void addNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.add(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void addJava(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(JavaImplAdd.add(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void addNop(Blackhole bh) {
        for (int i = 1; i < 1000; ++i)
            bh.consume(decimalValues[i]);
    }

    @Benchmark
    public void addMulTwoCalls(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.add(decimalValues[i], Decimal64Utils.multiply(decimalValues[i + 1], decimalValues[i + 2])));
    }

    @Benchmark
    public void addMulOneCall(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.multiplyAndAdd(decimalValues[i + 1], decimalValues[i + 2], decimalValues[i]));
    }

    @Benchmark
    public void basketPnlOld(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.subtract(decimalValues[i], Decimal64Utils.multiply(decimalValues[i + 1], decimalValues[i + 2])));
    }

    @Benchmark
    public void basketPnlNew(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.negate(Decimal64Utils.multiplyAndAdd(decimalValues[i + 1], decimalValues[i + 2], Decimal64Utils.negate(decimalValues[i]))));
    }

    @Benchmark
    public void fdimOld(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.max(Decimal64Utils.ZERO, Decimal64Utils.add(decimalValues[i], decimalValues[i + 1])));
    }

    @Benchmark
    public void fdimNew(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.bid64Fdim(decimalValues[i], Decimal64Utils.negate(decimalValues[i + 1])));
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(".*" + MathBenchmark.class.getSimpleName() + ".*")
            .forks(1)
            .build();
        new Runner(opt).run();
    }
}
