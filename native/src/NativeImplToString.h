#pragma once

#include <bid_conf.h>
#include <bid_functions.h>

BID_EXTERN_C const char * dfp64_to_string(BID_UINT64 value);
BID_EXTERN_C const char * dfp64_to_string_2(BID_UINT64 value, char decimalMark);
BID_EXTERN_C const char * dfp64_to_string_3(BID_UINT64 value, char decimalMark, char* buffer512);
BID_EXTERN_C const char * dfp64_to_string_4(BID_UINT64 value, char decimalMark, char* buffer512, int floatStyle);

BID_EXTERN_C const char * dfp64_to_scientific_string(BID_UINT64 value);
BID_EXTERN_C const char * dfp64_to_scientific_string_2(BID_UINT64 value, char decimalMark);
BID_EXTERN_C const char * dfp64_to_scientific_string_3(BID_UINT64 value, char decimalMark, char *buffer512);
