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
    public static int fixedSeed = 42 * 42 * 42 * 42 * 42;

    @Setup
    public void setUp() {
        TestUtils.RandomDecimalsGenerator generator = new TestUtils.RandomDecimalsGenerator(fixedSeed);
        decimalValues = new long[1004];
        for (int i = 0; i < decimalValues.length; ++i)
            decimalValues[i] = generator.nextX();
    }

    @Benchmark
    public void fmaNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.multiplyAndAdd(decimalValues[i], decimalValues[i + 1], decimalValues[i + 2]));
    }

    @Benchmark
    public void fma(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.multiplyAndAdd(decimalValues[i], decimalValues[i + 1], decimalValues[i + 2]));
    }

    @Benchmark
    public void multiplyByInt32Native(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.multiplyByInt32(decimalValues[i], (int) decimalValues[i + 1]));
    }

    @Benchmark
    public void multiplyByInt32(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.multiplyByInteger(decimalValues[i], (int) decimalValues[i + 1]));
    }

    @Benchmark
    public void multiplyByInt64Native(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.multiplyByInt64(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void multiplyByInt64(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.multiplyByInteger(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void divideByInt32Native(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.divideByInt32(decimalValues[i], (int) decimalValues[i + 1]));
    }

    @Benchmark
    public void divideByInt32(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.divideByInteger(decimalValues[i], (int) decimalValues[i + 1]));
    }

    @Benchmark
    public void divideByInt64Native(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.divideByInt64(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void divideByInt64(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.divideByInteger(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void mean2Native(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.mean2(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void mean2(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.mean(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void minNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.min2(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void min(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.min(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void maxNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.max2(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void max(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.max(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void max4Native(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.max4(decimalValues[i], decimalValues[i + 1], decimalValues[i + 2], decimalValues[i + 3]));
    }

    @Benchmark
    public void max4(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.max(decimalValues[i], decimalValues[i + 1], decimalValues[i + 2], decimalValues[i + 3]));
    }

    @Benchmark
    public void divNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.divide(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void div(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.divide(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void div2(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(JavaImplDiv.div2(decimalValues[i]));
    }

    @Benchmark
    public void mulNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.multiply2(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void mul(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.multiply(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void addNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.add2(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void add(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.add(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void nextUpNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.nextUp(decimalValues[i]));
    }

    @Benchmark
    public void nextUp(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.nextUp(decimalValues[i]));
    }

    @Benchmark
    public void nextDownNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.nextDown(decimalValues[i]));
    }

    @Benchmark
    public void nextDown(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.nextDown(decimalValues[i]));
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
