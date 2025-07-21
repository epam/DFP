#pragma once

#include <bid_conf.h>
#include <bid_functions.h>

BID_EXTERN_C BID_SINT64 dfp64_get_unscaled_value(BID_UINT64 value, BID_SINT64 abnormalReturn);

BID_EXTERN_C int dfp64_get_scale(BID_UINT64 value, int abnormalReturn);
