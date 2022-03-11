package com.epam.deltix.dfp;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

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
        TestUtils.RandomDecimalsGenerator generator = new TestUtils.RandomDecimalsGenerator();
        decimalValues = new long[1003];
        for (int i = 0; i < decimalValues.length; ++i)
            decimalValues[i] = generator.nextX();
    }

    @Benchmark
    public void divNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.divide(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void divJava(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(JavaImplDiv.bid64_div(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void mulNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.multiply2(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void mulJava(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(JavaImplMul.bid64_mul(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void addNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.add2(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void addJava(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(JavaImplAdd.bid64_add(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void nopJustIter(Blackhole bh) {
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

//    @Benchmark
//    public void fdimNew(Blackhole bh) {
//        for (int i = 0; i < 1000; ++i)
//            bh.consume(NativeImpl.bid64Fdim(decimalValues[i], Decimal64Utils.negate(decimalValues[i + 1])));
//    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(".*" + MathBenchmark.class.getSimpleName() + ".*")
            .forks(1)
            .build();
        new Runner(opt).run();
    }
}

