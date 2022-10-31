package com.epam.deltix.dfp;

import org.apache.commons.math3.random.MersenneTwister;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.epam.deltix.dfp.Decimal64Utils.MAX_SIGNIFICAND_DIGITS;
import static com.epam.deltix.dfp.TestUtils.POWERS_OF_TEN;

/**
 * 11th Gen Intel(R) Core(TM) i7-1185G7 @ 3.00GHz
 * Windows 10 Pro 21H2 19044.2130 *
 * JMH version: 1.25
 * VM version: JDK 11.0.14.1, Eclipse Adoptium OpenJDK 64-Bit Server VM, 11.0.14.1+1
 *
 * Benchmark                                                  Mode  Cnt      Score      Error  Units
 * RoundToReciprocalBenchmark.roundToNearestTiesAwayFromZero  avgt   15  62781,442 ± 2518,620  ns/op
 * RoundToReciprocalBenchmark.roundToNearestTiesToEven        avgt   15  67989,410 ± 6482,265  ns/op
 * RoundToReciprocalBenchmark.roundToReciprocalCeiling        avgt   15  35522,005 ± 1030,140  ns/op
 * RoundToReciprocalBenchmark.roundToReciprocalDown           avgt   15  31411,574 ±  667,242  ns/op
 * RoundToReciprocalBenchmark.roundToReciprocalFloor          avgt   15  35175,477 ± 1231,514  ns/op
 * RoundToReciprocalBenchmark.roundToReciprocalHalfDown       avgt   15  38549,762 ± 4718,530  ns/op
 * RoundToReciprocalBenchmark.roundToReciprocalHalfEven       avgt   15  39554,523 ±  772,916  ns/op
 * RoundToReciprocalBenchmark.roundToReciprocalHalfUp         avgt   15  34517,866 ± 1163,755  ns/op
 * RoundToReciprocalBenchmark.roundToReciprocalUp             avgt   15  38691,927 ± 2390,368  ns/op
 * RoundToReciprocalBenchmark.roundTowardsNegativeInfinity    avgt   15  65512,331 ± 1247,969  ns/op
 * RoundToReciprocalBenchmark.roundTowardsPositiveInfinity    avgt   15  65508,535 ± 2302,583  ns/op
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(time = 3, iterations = 1)
@Measurement(time = 3, iterations = 3)
@State(Scope.Thread)
public class RoundToReciprocalBenchmark {
    private long[] decimalValues;
    private int[] n;
    private long[] mul;
    public static int fixedSeed = 42 * 42 * 42 * 42 * 42;

    @Setup
    public void setUp() {
        final Random random = new SecureRandom();

        decimalValues = new long[1000];
        n = new int[decimalValues.length];
        mul = new long[decimalValues.length];

        for (int i = 0; i < decimalValues.length; ++i) {
            final int mantissaLen = random.nextInt(Decimal64Utils.MAX_SIGNIFICAND_DIGITS) + 1;
            final long mantissa = random.nextLong() % POWERS_OF_TEN[mantissaLen];

            final int exp = random.nextInt(20) - Decimal64Utils.MAX_SIGNIFICAND_DIGITS;

            decimalValues[i] = Decimal64Utils.fromFixedPoint(mantissa, -exp);

            n[i] = Math.abs(random.nextInt());

            mul[i] = Decimal64Utils.divideByInteger(Decimal64Utils.ONE, n[i]);
        }
    }

    @Benchmark
    public void roundTowardsPositiveInfinity(Blackhole bh) {
        for (int i = 0; i < decimalValues.length; ++i)
            bh.consume(Decimal64Utils.roundTowardsPositiveInfinity(decimalValues[i], mul[i]));
    }

    @Benchmark
    public void roundTowardsNegativeInfinity(Blackhole bh) {
        for (int i = 0; i < decimalValues.length; ++i)
            bh.consume(Decimal64Utils.roundTowardsNegativeInfinity(decimalValues[i], mul[i]));
    }

    @Benchmark
    public void roundToNearestTiesAwayFromZero(Blackhole bh) {
        for (int i = 0; i < decimalValues.length; ++i)
            bh.consume(Decimal64Utils.roundToNearestTiesAwayFromZero(decimalValues[i], mul[i]));
    }

    @Benchmark
    public void roundToNearestTiesToEven(Blackhole bh) {
        for (int i = 0; i < decimalValues.length; ++i)
            bh.consume(Decimal64Utils.roundToNearestTiesToEven(decimalValues[i], mul[i]));
    }

    @Benchmark
    public void roundToReciprocalUp(Blackhole bh) {
        for (int i = 0; i < decimalValues.length; ++i)
            bh.consume(Decimal64Utils.roundToReciprocal(decimalValues[i], n[i], RoundingMode.UP));
    }

    @Benchmark
    public void roundToReciprocalDown(Blackhole bh) {
        for (int i = 0; i < decimalValues.length; ++i)
            bh.consume(Decimal64Utils.roundToReciprocal(decimalValues[i], n[i], RoundingMode.DOWN));
    }

    @Benchmark
    public void roundToReciprocalCeiling(Blackhole bh) {
        for (int i = 0; i < decimalValues.length; ++i)
            bh.consume(Decimal64Utils.roundToReciprocal(decimalValues[i], n[i], RoundingMode.CEILING));
    }

    @Benchmark
    public void roundToReciprocalFloor(Blackhole bh) {
        for (int i = 0; i < decimalValues.length; ++i)
            bh.consume(Decimal64Utils.roundToReciprocal(decimalValues[i], n[i], RoundingMode.FLOOR));
    }

    @Benchmark
    public void roundToReciprocalHalfUp(Blackhole bh) {
        for (int i = 0; i < decimalValues.length; ++i)
            bh.consume(Decimal64Utils.roundToReciprocal(decimalValues[i], n[i], RoundingMode.HALF_UP));
    }

    @Benchmark
    public void roundToReciprocalHalfDown(Blackhole bh) {
        for (int i = 0; i < decimalValues.length; ++i)
            bh.consume(Decimal64Utils.roundToReciprocal(decimalValues[i], n[i], RoundingMode.HALF_DOWN));
    }

    @Benchmark
    public void roundToReciprocalHalfEven(Blackhole bh) {
        for (int i = 0; i < decimalValues.length; ++i)
            bh.consume(Decimal64Utils.roundToReciprocal(decimalValues[i], n[i], RoundingMode.HALF_EVEN));
    }
}
