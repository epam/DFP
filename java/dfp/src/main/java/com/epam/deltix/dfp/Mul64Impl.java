package com.epam.deltix.dfp;

import static com.epam.deltix.dfp.JavaImplAdd.LONG_LOW_PART;

class Mul64Impl {
    public static long multiplyHigh(final long __CX, final long __CY) {
        //__mul_64x64_to_128(ALBL, (A)_w0, (B)_w0);
        {
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

            return /*w1 =*/ __PH + (__PM >>> 32);
//            _ALBL_w0 = (__PM << 32) + (LONG_LOW_PART & __PL);
        }
    }

    public static long unsignedMultiplyHigh(final long A, final long T) {
        return multiplyHigh(A, T);
    }
}
