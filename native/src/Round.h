#pragma once

#include <stdbool.h>
#include <bid_conf.h>
#include <bid_functions.h>

enum DFP64RoundingMode
{
    Up = 0,
    Down = 1,
    Ceiling = 2,
    Floor = 3,
    HalfUp = 4,
    HalfDown = 5,
    HalfEven = 6,
};

BID_EXTERN_C BID_UINT64 dfp64_round(BID_UINT64 value, int n, enum DFP64RoundingMode roundType);
BID_EXTERN_C bool dfp64_is_rounded(BID_UINT64 value, int n);

BID_EXTERN_C BID_UINT64 dfp64_round_to_reciprocal(BID_UINT64 value, int r, enum DFP64RoundingMode roundType);
BID_EXTERN_C bool dfp64_is_rounded_to_reciprocal(BID_UINT64, int r);

BID_EXTERN_C BID_UINT64 dfp64_shorten_mantissa(BID_UINT64 value, BID_SINT64 delta, int minZerosCount);
