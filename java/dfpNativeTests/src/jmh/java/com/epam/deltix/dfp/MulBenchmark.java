package com.epam.deltix.dfp;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

import static com.epam.deltix.dfp.JavaImpl.*;
import static com.epam.deltix.dfp.JavaImpl.EXPONENT_SHIFT_SMALL;
import static com.epam.deltix.dfp.JavaImplAdd.LONG_LOW_PART;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(time = 3, iterations = 1)
@Measurement(time = 3, iterations = 3)
@State(Scope.Thread)
public class MulBenchmark {
    private long[] decimalValues;
    private double[] doubleValues;
    public static int fixedSeed = 42 * 42 * 42 * 42 * 42;

    @Setup
    public void setUp() {
        TestUtils.RandomDecimalsGenerator generator = new TestUtils.RandomDecimalsGenerator(fixedSeed);
        decimalValues = new long[1004];
        doubleValues = new double[decimalValues.length];
        for (int i = 0; i < decimalValues.length; ++i) {
            decimalValues[i] = generator.nextX();
            doubleValues[i] = Decimal64Utils.toDouble(decimalValues[i]);
        }
    }

    @Benchmark
    public void mul0(Blackhole bh) {
        for (int i = 0; i < 1000; ++i) {
            long __CX = decimalValues[i];
            long __CY = decimalValues[i + 1];
            long __CXH, __CXL, __CYH, __CYL, __PL, __PH, __PM, __PM2;
            __CXH = __CX >>> 32;
            __CXL = LONG_LOW_PART & __CX;
            __CYH = __CY >>> 32;
            __CYL = LONG_LOW_PART & __CY;

            __PM = __CXH * __CYL;
            __PH = __CXH * __CYH;
            __PL = __CXL * __CYL;
            __PM2 = __CXL * __CYH;
            __PH += (__PM >>> 32);
            __PM = (LONG_LOW_PART & __PM) + __PM2 + (__PL >>> 32);

            bh.consume(__PH + (__PM >>> 32));
            bh.consume((__PM << 32) + (LONG_LOW_PART & __PL));
        }
    }

    @Benchmark
    public void multiplyHigh(Blackhole bh) {
        for (int i = 0; i < 1000; ++i) {
            final long A = decimalValues[i];
            final long T = decimalValues[i + 1];
            bh.consume(Mul64Impl.multiplyHigh(A, T));
            bh.consume(A * T);
        }
    }

    @Benchmark
    public void unsignedMultiplyHigh(Blackhole bh) {
        for (int i = 0; i < 1000; ++i) {
            final long A = decimalValues[i];
            final long T = decimalValues[i + 1];
            bh.consume(Mul64Impl.unsignedMultiplyHigh(A, T));
            bh.consume(A * T);
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(".*" + MulBenchmark.class.getSimpleName() + ".*")
            .forks(1)
            .build();
        new Runner(opt).run();
    }
}
