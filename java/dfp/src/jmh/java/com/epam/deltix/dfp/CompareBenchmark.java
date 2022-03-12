package com.epam.deltix.dfp;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(time = 3, iterations = 1)
@Measurement(time = 3, iterations = 3)
@State(Scope.Thread)
public class CompareBenchmark {
    private long[] decimalValues;

    @Setup
    public void setUp() {
        TestUtils.RandomDecimalsGenerator generator = new TestUtils.RandomDecimalsGenerator();
        decimalValues = new long[1001];
        for (int i = 0; i < decimalValues.length; ++i)
            decimalValues[i] = generator.nextX();
    }

    @Benchmark
    public void isEqualNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.isEqual(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void isEqualJava(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(JavaImplCmp.bid64_quiet_equal(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void isNotEqualNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.isNotEqual(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void isNotEqualJava(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(JavaImplCmp.bid64_quiet_not_equal(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void isLessNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.isLess(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void isLessJava(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(JavaImplCmp.bid64_quiet_less(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void isLessOrEqualNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.isLessOrEqual(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void isLessOrEqualJava(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(JavaImplCmp.bid64_quiet_less_equal(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void isGreaterNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.isGreater(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void isGreaterJava(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(JavaImplCmp.bid64_quiet_greater(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void isGreaterOrEqualNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.isGreaterOrEqual(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void isGreaterOrEqualJava(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(JavaImplCmp.bid64_quiet_greater_equal(decimalValues[i], decimalValues[i + 1]));
    }

    @Benchmark
    public void isZeroNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.isZero(decimalValues[i]));
    }

    @Benchmark
    public void isZeroJava(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(JavaImplCmp.bid64_isZero(decimalValues[i]));
    }

    @Benchmark
    public void isNonZeroNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.isNonZero(decimalValues[i]));
    }

    @Benchmark
    public void isNonZeroJava(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(JavaImplCmp.bid64_quiet_not_equal(decimalValues[i], Decimal64Utils.ZERO));
    }

    @Benchmark
    public void isPositiveNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.isPositive(decimalValues[i]));
    }

    @Benchmark
    public void isPositiveJava(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(JavaImplCmp.bid64_quiet_greater(decimalValues[i], Decimal64Utils.ZERO));
    }

    @Benchmark
    public void isNegativeNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.isNegative(decimalValues[i]));
    }

    @Benchmark
    public void isNegativeJava(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(JavaImplCmp.bid64_quiet_less(decimalValues[i], Decimal64Utils.ZERO));
    }

    @Benchmark
    public void isNonPositiveNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.isNonPositive(decimalValues[i]));
    }

    @Benchmark
    public void isNonPositiveJava(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(JavaImplCmp.bid64_quiet_less_equal(decimalValues[i], Decimal64Utils.ZERO));
    }

    @Benchmark
    public void isNonNegativeNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.isNonNegative(decimalValues[i]));
    }

    @Benchmark
    public void isNonNegativeJava(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(JavaImplCmp.bid64_quiet_greater_equal(decimalValues[i], Decimal64Utils.ZERO));
    }
}
