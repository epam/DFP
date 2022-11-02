package com.epam.deltix.dfp;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(time = 3, iterations = 1)
@Measurement(time = 3, iterations = 3)
@State(Scope.Thread)
public class ExceptionCatchBenchmark {
    private long value;
    private int n;
    private int r;

    @Setup
    public void setUp() {
        value = Decimal64Utils.parse("0.9999999999999999");
        n = 3;
        r = 1000;
    }

    private static boolean isRoundedToTenPower(final long value, final int n) {
        try {
            Decimal64Utils.round(value, n, RoundingMode.UNNECESSARY);
            return true;
        }
        catch (final ArithmeticException e) {
            return false;
        }
    }

    private static boolean isRoundedToReciprocal(final long value, final int r) {
        try {
            Decimal64Utils.roundToReciprocal(value, r, RoundingMode.UNNECESSARY);
            return true;
        }
        catch (final ArithmeticException e) {
            return false;
        }
    }

    @Benchmark
    public void isRoundedToTenPower(Blackhole bh) {
        bh.consume(Decimal64Utils.isRounded(value, n));
    }

    @Benchmark
    public void isRoundedToTenPowerCatch(Blackhole bh) {
        bh.consume(isRoundedToTenPower(value, n));
    }

    @Benchmark
    public void isRoundedToReciprocal(Blackhole bh) {
        bh.consume(Decimal64Utils.isRoundedToReciprocal(value, r));
    }

    @Benchmark
    public void isRoundedToReciprocalCatch(Blackhole bh) {
        bh.consume(isRoundedToReciprocal(value, r));
    }
}
