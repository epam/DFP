package com.epam.deltix.dfp;

import static com.epam.deltix.dfp.JavaImplAdd.LONG_LOW_PART;

public class Mul64Impl {
    public static long multiplyHigh(final long A, final long T) {
        return Math.multiplyHigh(A, T);
    }

    public static long unsignedMultiplyHigh(final long x, final long y) {
        return Math.multiplyHigh(x, y) +
            (y & (x >> 63)) +  // equivalent to `if (x < 0) result += y;`
            (x & (y >> 63)); // equivalent to `if (y < 0) result += x;`
    }
}
