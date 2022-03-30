package com.epam.deltix.dfp;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(time = 2, iterations = 3)
@Measurement(time = 2, iterations = 3)
@State(Scope.Thread)
@Fork(2)
public class RoundingBenchmark {
    /* 123.456789123, 1.23 */
    @Param({"3503800633551035011", "3566850904877432955"})
    private long decimalValue;

    @Setup
    public void setUp() {
        decimalValue = Decimal64Utils.fromDouble(new Random().nextDouble());
    }

    @Benchmark
    public long roundCeil() {
        return Decimal64Utils.ceiling(decimalValue);
    }

    @Benchmark
    public long roundCeilJava() {
        return Decimal64Utils.round(decimalValue, 0, RoundType.CEIL);
    }

    @Benchmark
    public long roundToNearestTiesAwayFromZero() {
        return Decimal64Utils.roundToNearestTiesAwayFromZero(decimalValue);
    }

    @Benchmark
    public long roundToNearestTiesToEven() {
        return Decimal64Utils.roundToNearestTiesToEven(decimalValue);
    }

    @Benchmark
    public long roundToNearestTiesAwayFromZeroD5() {
        return Decimal64Utils.roundToNearestTiesAwayFromZero(decimalValue,
            Decimal64Utils.fromFixedPoint(125, 3));
    }

    @Benchmark
    public long roundToNearestTiesToEvenD5() {
        return Decimal64Utils.roundToNearestTiesToEven(decimalValue,
            Decimal64Utils.fromFixedPoint(125, 3));
    }

    @Benchmark
    public long roundTowardsNegativeInfinity() {
        return Decimal64Utils.roundTowardsNegativeInfinity(decimalValue);
    }

    @Benchmark
    public long roundTowardsNegativeInfinityD5() {
        return Decimal64Utils.roundTowardsNegativeInfinity(decimalValue,
            Decimal64Utils.fromFixedPoint(125, 3));
    }

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
            .include(".*" + RoundingBenchmark.class.getSimpleName() + ".*")
            .forks(1)
            .build();
        new Runner(opt).run();
    }
}

