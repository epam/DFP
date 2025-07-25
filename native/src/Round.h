#pragma once

#include <stdbool.h>
#include <bid_conf.h>
#include <bid_functions.h>

typedef enum 
{
    UP = 0,
    DOWN = 1,
    CEILING = 2,
    FLOOR = 3,
    HALF_UP = 4,
    HALF_DOWN = 5,
    HALF_EVEN = 6,
} DFP64RoundingMode;

BID_EXTERN_C BID_UINT64 dfp64_round(BID_UINT64 value, int n, enum DFP64RoundingMode roundType);
BID_EXTERN_C bool dfp64_is_rounded(BID_UINT64 value, int n);

BID_EXTERN_C BID_UINT64 dfp64_round_to_reciprocal(BID_UINT64 value, int r, enum DFP64RoundingMode roundType);
BID_EXTERN_C bool dfp64_is_rounded_to_reciprocal(BID_UINT64, int r);

BID_EXTERN_C BID_UINT64 dfp64_shorten_mantissa(BID_UINT64 value, BID_SINT64 delta, int minZerosCount);
