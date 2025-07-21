#pragma once

#include <stdbool.h>
#include <bid_conf.h>
#include <bid_functions.h>
#include <bid_internal.h>

/* = -0x80L */
#define DFP_NAN_NULL            0xFFFFFFFFFFFFFF80ull 
#define MASK_INFINITY_AND_NAN   0x7800000000000000ull
#define MASK_INFINITY_NAN       0x7C00000000000000ull

#define EXPONENT_BIAS   398

static inline bool isNull(BID_UINT64 value) {
    return DFP_NAN_NULL == value;
}

static inline bool isNonFinite(BID_UINT64 value) {
    return (value & MASK_INFINITY_AND_NAN) == MASK_INFINITY_AND_NAN;
}

static inline bool isNaN(BID_UINT64 value) {
    return (value & MASK_INFINITY_NAN) == MASK_INFINITY_NAN;
}

static inline bool isNegativeSign(BID_UINT64 value) {
    return value & MASK_SIGN;
}

static inline bool isSpecial(BID_UINT64 value) {
    return (value & MASK_SPECIAL) == MASK_SPECIAL;
}
