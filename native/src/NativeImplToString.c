#include <math.h>
#include <stdbool.h>
#include <string.h>
#include <stdlib.h>
#include "NativeImplToString.h"
#include <bid_conf.h>
#include <bid_functions.h>
#include <bid_internal.h>

static const BID_UINT64 DFP_NAN_NULL = 0xFFFFFFFFFFFFFF80ull; // = -0x80L;
static const BID_UINT64 MASK_INFINITY_AND_NAN = 0x7800000000000000ull;
static const BID_UINT64 MASK_INFINITY_NAN = 0x7C00000000000000L;

static const int EXPONENT_BIAS = 398;


static bool isNull(BID_UINT64 value) {
    return DFP_NAN_NULL == value;
}

static bool isNonFinite(BID_UINT64 value) {
    return (value & MASK_INFINITY_AND_NAN) == MASK_INFINITY_AND_NAN;
}

static bool isNaN(BID_UINT64 value) {
    return (value & MASK_INFINITY_NAN) == MASK_INFINITY_NAN;
}

#define BCD_TABLE_DIGITS 3
static const int BCD_DIVIDER = 1000000000;
static const int BCD_DIVIDER_GROUPS = 3; // log10(BCD_DIVIDER) / BCD_TABLE_DIGITS must be natural value

char* makeBcdTable() {
    int n = 1;
    for (int i = 0; i < BCD_TABLE_DIGITS; ++i)
        n *= 10;

    char *table = (char *)malloc(n * BCD_TABLE_DIGITS * sizeof(char));
    if (!table)
        return 0;

    char value[BCD_TABLE_DIGITS];

    memset(value, '0', BCD_TABLE_DIGITS * sizeof(char));

    for (int i = 0, ib = 0; i < n; ++i) {
        for (int j = 0; j < BCD_TABLE_DIGITS; ++j)
            table[ib++] = value[j];
        value[0] += 1;
        for (int j = 0; j < BCD_TABLE_DIGITS - 1; ++j) {
            if (value[j] <= '9')
                break;
            else {
                value[j] -= 10;
                value[j + 1] += 1;
            }
        }
    }

    return table;
}

static const char* BCD_TABLE; // makeBcdTable(BCD_TABLE_DIGITS);

int formatUIntFromBcdTable(int value, char* buffer, int bi) {
    if (!BCD_TABLE)
        BCD_TABLE = makeBcdTable();

    for (int blockIndex = 0; blockIndex < BCD_DIVIDER_GROUPS; ++blockIndex) {
        int newValue = (int)((unsigned long long)(2199023256ull * value) >> 41);
        int remainder = value - newValue * 1000;
        //final int remainder = value - ((newValue << 10) - (newValue << 4) - (newValue << 3));
        value = newValue;

        for (int j = 0, ti = remainder * BCD_TABLE_DIGITS /* (remainder << 1) + remainder */; j < BCD_TABLE_DIGITS; ++j, ++ti)
            buffer[--bi] = BCD_TABLE[ti];
    }

    return bi;
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

int numberOfDigits(BID_UINT64 value) {
    for (int i = 1; i < sizeof(POWERS_OF_TEN) / sizeof(POWERS_OF_TEN[0]); i += 1)
        if (value < POWERS_OF_TEN[i])
            return i;
    return 19;
}

#define bufferMinLength 511
#define bufferMinLengthWithZero 512
BID_THREAD char tls_to_string_buffer[bufferMinLengthWithZero];

const char* dfp64_to_string(BID_UINT64 value) {
    return dfp64_to_string_2(value, '.');
}

const char* dfp64_to_string_2(BID_UINT64 value, char decimalMark) {
    return dfp64_to_string_3(value, decimalMark, tls_to_string_buffer);
}

const char* dfp64_to_string_3(BID_UINT64 value, char decimalMark, char* buffer512) {
    if (isNull(value))
        return "null";

    if (isNonFinite(value)) {
        // Value is either Inf or NaN
        // TODO: Do we need SNaN?
        return isNaN(value) ? "NaN" : value < 0 ? "-Infinity" : "Infinity";
    }

    BID_UINT64 partsSignMask, partsCoefficient;
    int partsExponent;
    unpack_BID64(&partsSignMask, &partsExponent, &partsCoefficient, value);

    if (partsCoefficient == 0) { // return "0" + decimalMark + "0";
        buffer512[0] = '0';
        buffer512[1] = decimalMark;
        buffer512[2] = '0';
        buffer512[3] = '\0';
        return buffer512;
    }

    int exponent = partsExponent - EXPONENT_BIAS;

    buffer512[bufferMinLength] = '\0';

    if (exponent >= 0) {
        int bi = bufferMinLength;
        buffer512[--bi] = '0';
        buffer512[--bi] = decimalMark;
        for (int i = 0; i < exponent; ++i)
            buffer512[--bi] = '0';

        while (partsCoefficient > 0) {
            bi = formatUIntFromBcdTable((int)(partsCoefficient % BCD_DIVIDER), buffer512, bi);
            partsCoefficient /= BCD_DIVIDER;
        }

        while (buffer512[bi] == '0')
            ++bi;

        if (value < 0)
            buffer512[--bi] = '-';

        return buffer512 + bi;

    }
    else { // exponent < 0
        int bi = bufferMinLength;

        int digits = numberOfDigits(partsCoefficient);

        if (digits + exponent > 0) {
            long integralPart = partsCoefficient / POWERS_OF_TEN[-exponent];
            long fractionalPart = partsCoefficient % POWERS_OF_TEN[-exponent];

            while (fractionalPart > 0) {
                bi = formatUIntFromBcdTable((int)(fractionalPart % BCD_DIVIDER), buffer512, bi);
                fractionalPart /= BCD_DIVIDER;
            }

            int written = bufferMinLength - bi /* already written */;
            //if (written < -exponent /* must be written */)
            for (int ei = 0, ee = -exponent - written; ei < ee; ++ei)
                buffer512[--bi] = '0';

            bi = bufferMinLength + exponent; /* buffer512.length - (-exponent) */

            buffer512[--bi] = decimalMark;

            while (integralPart > 0) {
                bi = formatUIntFromBcdTable((int)(integralPart % BCD_DIVIDER), buffer512, bi);
                integralPart /= BCD_DIVIDER;
            }

            while (buffer512[bi] == '0')
                ++bi;

            if (value < 0)
                buffer512[--bi] = '-';

            int be = bufferMinLength;
            while (buffer512[be - 1] == '0')
                --be;

            if (buffer512[be - 1] == decimalMark && be < bufferMinLength)
                buffer512[be++] = '0';

            buffer512[be] = '\0';
            return buffer512 + bi;

        }
        else {
            while (partsCoefficient > 0) {
                bi = formatUIntFromBcdTable((int)(partsCoefficient % BCD_DIVIDER), buffer512, bi);
                partsCoefficient /= BCD_DIVIDER;
            }

            int written = bufferMinLength - bi /* already written */;
            //if (written < -exponent /* must be written */)
            for (int ei = 0, ee = -exponent - written; ei < ee; ++ei)
                buffer512[--bi] = '0';

            bi = bufferMinLength + exponent; /* buffer512.length - (-exponent) */

            buffer512[--bi] = decimalMark;
            buffer512[--bi] = '0';

            if (value < 0)
                buffer512[--bi] = '-';

            int be = bufferMinLength;
            while (buffer512[be - 1] == '0')
                --be;

            buffer512[be] = '\0';
            return buffer512 + bi;
        }
    }
}

const char SCIENTIFIC_ZERO[] = "0.000000000000000e+000";

const char* dfp64_to_scientific_string(BID_UINT64 value) {
    return dfp64_to_scientific_string_2(value, '.');
}

const char* dfp64_to_scientific_string_2(BID_UINT64 value, char decimalMark) {
    return dfp64_to_scientific_string_3(value, decimalMark, tls_to_string_buffer);
}

const char* dfp64_to_scientific_string_3(BID_UINT64 value, char decimalMark, char* buffer512) {
    if (isNull(value))
        return "null";

    if (isNonFinite(value)) {
        // Value is either Inf or NaN
        // TODO: Do we need SNaN?
        return isNaN(value) ? "NaN" : value < 0 ? "-Infinity" : "Infinity";
    }

    BID_UINT64 partsSignMask, partsCoefficient;
    int partsExponent;
    unpack_BID64(&partsSignMask, &partsExponent, &partsCoefficient, value);

    if (partsCoefficient == 0) {
        memcpy(buffer512, SCIENTIFIC_ZERO, sizeof(SCIENTIFIC_ZERO) / sizeof(SCIENTIFIC_ZERO[0]) + 1);
        buffer512[1] = decimalMark;
        return buffer512;
    }

    int exponent = partsExponent - EXPONENT_BIAS;

    int bi = MAX_FORMAT_DIGITS * 2 + 2, be = bi;
    while (partsCoefficient > 0) {
        bi = formatUIntFromBcdTable((int)(partsCoefficient % BCD_DIVIDER), buffer512, bi);
        partsCoefficient /= BCD_DIVIDER;
    }

    while (buffer512[bi] == '0')
        ++bi;

    exponent += be - bi - 1;

    for (int bee = MAX_FORMAT_DIGITS + bi; be < bee; ++be)
        buffer512[be] = '0';

    bi--;
    buffer512[bi] = buffer512[bi + 1];
    buffer512[bi + 1] = decimalMark;

    if (value < 0)
        buffer512[--bi] = '-';

    buffer512[be++] = 'e';
    buffer512[be++] = exponent >= 0 ? '+' : '-';
    {
        if (!BCD_TABLE)
            BCD_TABLE = makeBcdTable(BCD_TABLE_DIGITS);

        be += BCD_TABLE_DIGITS;
        for (int j = 0, ti = abs(exponent) * BCD_TABLE_DIGITS /* (remainder << 1) + remainder */; j < BCD_TABLE_DIGITS; ++j, ++ti)
            buffer512[be - 1 - j] = BCD_TABLE[ti];
    }

    buffer512[be] = '\0';
    return buffer512 + bi;
}
