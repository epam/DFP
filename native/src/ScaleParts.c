#include <math.h>
#include <stdbool.h>
#include <string.h>
#include <stdlib.h>
#include "auxiliary.h"
#include "ScaleParts.h"

BID_EXTERN_C BID_SINT64 dfp64_get_unscaled_value(BID_UINT64 value, BID_SINT64 abnormalReturn) {
    bool sign = isNegativeSign(value);

    if (!isSpecial(value)) {
        BID_UINT64 coefficient = (value & SMALL_COEFF_MASK64);
        return sign ? -(BID_SINT64)coefficient : coefficient;
    }
    else {
        // special encodings
        if ((value & INFINITY_MASK64) == INFINITY_MASK64) {
            return abnormalReturn;    // NaN or Infinity
        }
        else {
            BID_UINT64 coeff = (value & LARGE_COEFF_MASK64) | LARGE_COEFF_HIGH_BIT64;
            if (coeff >= 10000000000000000ull)
                coeff = 0;
            return sign ? -(BID_SINT64)coeff : coeff;
        }
    }
}

BID_EXTERN_C int32 dfp64_get_scale(BID_UINT64 value, int abnormalReturn) {
    if (!isSpecial(value)) {
        return -((int)((value >> EXPONENT_SHIFT_SMALL64) & EXPONENT_MASK64) - EXPONENT_BIAS);
    }
    else {
        // special encodings
        if ((value & INFINITY_MASK64) == INFINITY_MASK64)
            return abnormalReturn;
        else
            return -((int)((value >> EXPONENT_SHIFT_LARGE64) & EXPONENT_MASK64) - EXPONENT_BIAS);
    }
}
