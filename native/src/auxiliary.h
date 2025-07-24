#pragma once

#include <stdbool.h>
#include <bid_conf.h>
#include <bid_functions.h>
#include <bid_internal.h>

/* = -0x80L */
#define DFP_NAN_NULL            0xFFFFFFFFFFFFFF80ull 
#define MASK_INFINITY_AND_NAN   0x7800000000000000ull
#define MASK_INFINITY_NAN       0x7C00000000000000ull

#define LARGE_COEFFICIENT_MASK      0x0007FFFFFFFFFFFFull
#define LARGE_COEFFICIENT_HIGH_BIT  0x0020000000000000ull

#define SMALL_COEFFICIENT_MASK      0x001FFFFFFFFFFFFFull
#define MASK_COEFFICIENT            0x0001FFFFFFFFFFFFull

#define MAX_COEFFICIENT         9999999999999999ull
#define EXPONENT_MASK           0x03FF
#define EXPONENT_SHIFT_LARGE    51
#define EXPONENT_SHIFT_SMALL    53
#define EXPONENT_MASK_SMALL     ((BID_UINT64)EXPONENT_MASK << EXPONENT_SHIFT_SMALL)

#define EXPONENT_BIAS   398
#define MIN_EXPONENT   -383
#define MAX_EXPONENT    384

#define BID_UINT64_ZERO     0x31C0000000000000ull
#define BID_UINT64_NAN      0x7C00000000000000ull


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

static const BID_UINT64 POWERS_OF_TEN[] = {
    /*  0 */ 1L,
    /*  1 */ 10L,
    /*  2 */ 100L,
    /*  3 */ 1000L,
    /*  4 */ 10000L,
    /*  5 */ 100000L,
    /*  6 */ 1000000L,
    /*  7 */ 10000000L,
    /*  8 */ 100000000L,
    /*  9 */ 1000000000L,
    /* 10 */ 10000000000L,
    /* 11 */ 100000000000L,
    /* 12 */ 1000000000000L,
    /* 13 */ 10000000000000L,
    /* 14 */ 100000000000000L,
    /* 15 */ 1000000000000000L,
    /* 16 */ 10000000000000000L,
    /* 17 */ 100000000000000000L,
    /* 18 */ 1000000000000000000L
};

static inline int numberOfDigits(BID_UINT64 value) {
    for (int i = 1; i < sizeof(POWERS_OF_TEN) / sizeof(POWERS_OF_TEN[0]); i += 1)
        if (value < POWERS_OF_TEN[i])
            return i;
    return 19;
}
