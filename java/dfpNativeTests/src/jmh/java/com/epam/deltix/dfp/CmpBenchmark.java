package com.epam.deltix.dfp;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(time = 3, iterations = 3)
@Measurement(time = 3, iterations = 3)
@Fork(3)
@State(Scope.Thread)
public class CmpBenchmark {
    /* 0., 1., 1. (non-canonized), 1000., Inf, NaN */
    @Param({"3584865303386914816", "3584865303386914817", "3450757314565799936", "3584865303386915816", "8646911284551352320", "8935141660703064064"})
    private long x;

    /* 0., 1., 1. (non-canonized), 1000., Inf, NaN */
    @Param({"3584865303386914816", "3584865303386914817", "3450757314565799936", "3584865303386915816", "8646911284551352320", "8935141660703064064"})
    private long y;

    @Benchmark
    public int compareToOrig() {
        return JavaImplCmp.compareOrig(x, y);
    }

    @Benchmark
    public int compareToNew() {
        return JavaImplCmp.compareNew(x, y);
    }
}
