package com.epam.deltix.dfp;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

import static com.epam.deltix.dfp.JavaImpl.*;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(time = 2, iterations = 4)
@Measurement(time = 2, iterations = 4)
@State(Scope.Thread)
@Fork(3)
public class CanonizeBenchmark {
    /* 10., 1000_000., 123.456789123, 1.23, null(=NaN), 0., 1., 1000., 1000000., 1000000000. */
    @Param({"3584865303386914826", "3584865303387914816", "3503800633551035011", "3566850904877432955", "-128",
        "3584865303386914816", "3584865303386914817", "3584865303386915816", "3584865303387914816", "3584865304386914816"})
    private long decimalValue;

    @Benchmark
    public void canonize(Blackhole bh) {
        bh.consume(Decimal64Utils.canonize(decimalValue));
    }

    @Benchmark
    public void canonizeOrig(Blackhole bh) {
        bh.consume(canonizeFiniteOrig(decimalValue));
    }

    public static long canonizeFiniteOrig(final long value) {
        final long signMask = value & MASK_SIGN;
        long coefficient;
        int exponent;

        if (isSpecial(value)) {
            assert (isFinite(value));

            // Check for non-canonical values.
            final long x = (value & LARGE_COEFFICIENT_MASK) | LARGE_COEFFICIENT_HIGH_BIT;
            coefficient = x > MAX_COEFFICIENT ? 0 : x;

            // Extract exponent.
            final long tmp = value >> EXPONENT_SHIFT_LARGE;
            exponent = (int) (tmp & EXPONENT_MASK);
        } else {
            // Extract coefficient.
            coefficient = (value & SMALL_COEFFICIENT_MASK);

            // Extract exponent. Maximum biased value for "small exponent" is 0x2FF(*2=0x5FE), signed: []
            // upper 1/4 of the mask range is "special", as checked in the code above
            final long tmp = value >> EXPONENT_SHIFT_SMALL;
            exponent = (int) (tmp & EXPONENT_MASK);
        }

        if (coefficient == 0)
            return ZERO;

        long div10 = coefficient / 10;
        if (div10 * 10 != coefficient)
            return value;

        do {
            coefficient = div10;
            div10 /= 10;
            ++exponent;
        } while (div10 * 10 == coefficient);
        return pack(signMask, exponent, coefficient, BID_ROUNDING_TO_NEAREST);
    }

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
            .include(".*" + CanonizeBenchmark.class.getSimpleName() + ".*")
            .forks(1)
            .build();
        new Runner(opt).run();
    }
}

