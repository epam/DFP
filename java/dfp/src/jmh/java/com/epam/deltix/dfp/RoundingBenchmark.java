package com.epam.deltix.dfp;

import org.apache.commons.math3.random.MersenneTwister;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

import static com.epam.deltix.dfp.Decimal64Utils.MAX_SIGNIFICAND_DIGITS;

/**
 * 11th Gen Intel(R) Core(TM) i7-1185G7 @ 3.00GHz
 * Windows 10 Pro 21H2 19044.2130 *
 * JMH version: 1.25
 * VM version: JDK 11.0.14.1, OpenJDK 64-Bit Server VM, 11.0.14.1+1
 *
 * Benchmark                                               Mode  Cnt      Score      Error  Units
 * RoundingBenchmark.roundCeiling                          avgt   15  11290,324 ±  410,007  ns/op
 * RoundingBenchmark.roundDown                             avgt   15   8956,215 ±  554,607  ns/op
 * RoundingBenchmark.roundFloor                            avgt   15  12088,918 ± 1200,449  ns/op
 * RoundingBenchmark.roundHalfDown                         avgt   15  10622,215 ±  233,144  ns/op
 * RoundingBenchmark.roundHalfEven                         avgt   15  11821,198 ±  402,190  ns/op
 * RoundingBenchmark.roundHalfUp                           avgt   15  10446,453 ±  260,959  ns/op
 * RoundingBenchmark.roundToNearestTiesAwayFromZero        avgt   15  10130,422 ±  545,036  ns/op
 * RoundingBenchmark.roundToNearestTiesAwayFromZeroNative  avgt   15  11864,454 ±  655,077  ns/op
 * RoundingBenchmark.roundToNearestTiesToEven              avgt   15  13606,541 ± 1682,309  ns/op
 * RoundingBenchmark.roundToNearestTiesToEvenNative        avgt   15  14170,887 ± 1140,730  ns/op
 * RoundingBenchmark.roundTowardsNegativeInfinity          avgt   15  10964,737 ± 1040,513  ns/op
 * RoundingBenchmark.roundTowardsNegativeInfinityNative    avgt   15  13682,684 ±  176,024  ns/op
 * RoundingBenchmark.roundTowardsPositiveInfinity          avgt   15  11779,079 ± 1445,235  ns/op
 * RoundingBenchmark.roundTowardsPositiveInfinityNative    avgt   15  13693,661 ±  182,765  ns/op
 * RoundingBenchmark.roundTowardsZero                      avgt   15   9793,022 ±  560,275  ns/op
 * RoundingBenchmark.roundTowardsZeroNative                avgt   15  11522,545 ±  200,853  ns/op
 * RoundingBenchmark.roundUp                               avgt   15  10026,050 ±  414,826  ns/op
 */

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(time = 3, iterations = 1)
@Measurement(time = 3, iterations = 3)
@State(Scope.Thread)
public class RoundingBenchmark {
    private long[] decimalValues;
    public static int fixedSeed = 42 * 42 * 42 * 42 * 42;

    @Setup
    public void setUp() {
        final TestUtils.RandomDecimalsGenerator generator =
            new TestUtils.RandomDecimalsGenerator(new MersenneTwister(fixedSeed), 64,
                -MAX_SIGNIFICAND_DIGITS, -MAX_SIGNIFICAND_DIGITS + 1);
        decimalValues = new long[1000];
        for (int i = 0; i < decimalValues.length; ++i)
            decimalValues[i] = generator.nextX();
    }

    @Benchmark
    public void roundTowardsPositiveInfinityNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.roundTowardsPositiveInfinity(decimalValues[i]));
    }

    @Benchmark
    public void roundTowardsPositiveInfinity(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.roundTowardsPositiveInfinity(decimalValues[i]));
    }

    @Benchmark
    public void roundTowardsNegativeInfinityNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.roundTowardsNegativeInfinity(decimalValues[i]));
    }

    @Benchmark
    public void roundTowardsNegativeInfinity(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.roundTowardsNegativeInfinity(decimalValues[i]));
    }

    @Benchmark
    public void roundTowardsZeroNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.roundTowardsZero(decimalValues[i]));
    }

    @Benchmark
    public void roundTowardsZero(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.roundTowardsZero(decimalValues[i]));
    }

    @Benchmark
    public void roundToNearestTiesAwayFromZeroNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.roundToNearestTiesAwayFromZero(decimalValues[i]));
    }

    @Benchmark
    public void roundToNearestTiesAwayFromZero(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.roundToNearestTiesAwayFromZero(decimalValues[i]));
    }

    @Benchmark
    public void roundToNearestTiesToEvenNative(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(NativeImpl.roundToNearestTiesToEven(decimalValues[i]));
    }

    @Benchmark
    public void roundToNearestTiesToEven(Blackhole bh) {
        for (int i = 0; i < 1000; ++i)
            bh.consume(Decimal64Utils.roundToNearestTiesToEven(decimalValues[i]));
    }

    @Benchmark
    public void roundUp(Blackhole bh) {
        for (int i = 0; i < 1000; ++i) bh.consume(Decimal64Utils.round(decimalValues[i], 0, RoundingMode.UP));
    }

    @Benchmark
    public void roundDown(Blackhole bh) {
        for (int i = 0; i < 1000; ++i) bh.consume(Decimal64Utils.round(decimalValues[i], 0, RoundingMode.DOWN));
    }

    @Benchmark
    public void roundCeiling(Blackhole bh) {
        for (int i = 0; i < 1000; ++i) bh.consume(Decimal64Utils.round(decimalValues[i], 0, RoundingMode.CEILING));
    }

    @Benchmark
    public void roundFloor(Blackhole bh) {
        for (int i = 0; i < 1000; ++i) bh.consume(Decimal64Utils.round(decimalValues[i], 0, RoundingMode.FLOOR));
    }

    @Benchmark
    public void roundHalfUp(Blackhole bh) {
        for (int i = 0; i < 1000; ++i) bh.consume(Decimal64Utils.round(decimalValues[i], 0, RoundingMode.HALF_UP));
    }

    @Benchmark
    public void roundHalfDown(Blackhole bh) {
        for (int i = 0; i < 1000; ++i) bh.consume(Decimal64Utils.round(decimalValues[i], 0, RoundingMode.HALF_DOWN));
    }

    @Benchmark
    public void roundHalfEven(Blackhole bh) {
        for (int i = 0; i < 1000; ++i) bh.consume(Decimal64Utils.round(decimalValues[i], 0, RoundingMode.HALF_EVEN));
    }
}
