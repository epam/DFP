#include "Round.h"
#include "auxiliary.h"

static bool is_rounded_impl(int addExponent, long partsCoefficient, long divFactor) {
    return addExponent == 0 && partsCoefficient % divFactor == 0;
}

BID_UINT64 dfp64_round(BID_UINT64 value, int n, enum DFP64RoundingMode roundType) {
    if (isNonFinite(value))
        return value;
    if (n > MAX_EXPONENT)
        return value;
    if (n < MIN_EXPONENT)
        return BID_UINT64_ZERO;

    BID_UINT64 partsSignMask;
    int partsExponent;
    BID_UINT64 partsCoefficient;

    { // Copy-paste the toParts method for speedup
        partsSignMask = value & MASK_SIGN;

        if (isSpecial(value)) {
            //if (isNonFinite(value)) {
            //    partsExponent = 0;
            //    partsCoefficient = value & 0xFE03_FFFF_FFFF_FFFFL;
            //    if ((value & 0x0003_FFFF_FFFF_FFFFL) > MAX_COEFFICIENT)
            //        partsCoefficient = value & ~MASK_COEFFICIENT;
            //    if (isInfinity(value))
            //        partsCoefficient = value & MASK_SIGN_INFINITY_NAN; // TODO: Why this was done??
            //}
            //else
            {
                // Check for non-canonical values.
                BID_UINT64 coefficient = (value & LARGE_COEFFICIENT_MASK) | LARGE_COEFFICIENT_HIGH_BIT;
                partsCoefficient = coefficient > MAX_COEFFICIENT ? 0 : coefficient;

                // Extract exponent.
                BID_UINT64 tmp = value >> EXPONENT_SHIFT_LARGE;
                partsExponent = (int)(tmp & EXPONENT_MASK);
            }
        }
        else {

            // Extract exponent. Maximum biased value for "small exponent" is 0x2FF(*2=0x5FE), signed: []
            // upper 1/4 of the mask range is "special", as checked in the code above
            BID_UINT64 tmp = value >> EXPONENT_SHIFT_SMALL;
            partsExponent = (int)(tmp & EXPONENT_MASK);

            // Extract coefficient.
            partsCoefficient = (value & SMALL_COEFFICIENT_MASK);
        }
    }

    if (partsCoefficient == 0)
        return BID_UINT64_ZERO;

    int exponent = partsExponent - EXPONENT_BIAS + n;

    if (exponent >= 0) // value is already rounded
        return value;
    // All next - negative exponent case

    BID_UINT64 divFactor;
    int addExponent = 0;
    { // Truncate all digits except last one
        int absPower = -exponent;
        if (absPower >= MAX_FORMAT_DIGITS) {
            divFactor = MAX_COEFFICIENT + 1;
            int expShift = MAX_FORMAT_DIGITS;
            addExponent = absPower - expShift;

        }
        else {
            divFactor = POWERS_OF_TEN[absPower];
        }
    }

    // Process last digit
    switch (roundType) {
    case Up:
        partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor - 1) / divFactor) * divFactor : divFactor;
        break;

    case Down:
        partsCoefficient = addExponent == 0 ? (partsCoefficient / divFactor) * divFactor : 0;
        break;

    case Ceiling:
        if (partsSignMask >= 0/*!parts.isNegative()*/)
            partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor - 1) / divFactor) * divFactor : divFactor;
        else
            partsCoefficient = addExponent == 0 ? (partsCoefficient / divFactor) * divFactor : 0;
        break;

    case Floor:
        if (partsSignMask >= 0/*!parts.isNegative()*/)
            partsCoefficient = addExponent == 0 ? (partsCoefficient / divFactor) * divFactor : 0;
        else
            partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor - 1) / divFactor) * divFactor : divFactor;
        break;

    case HalfUp:
        partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor / 2) / divFactor) * divFactor : 0;
        break;

    case HalfDown:
        partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor / 2 - 1) / divFactor) * divFactor : 0;
        break;

    case HalfEven:
        partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor / 2 - 1 + ((partsCoefficient / divFactor) & 1L)) / divFactor) * divFactor : 0;
        break;

    //case Unnecessary:
    //    if (!is_rounded_impl(addExponent, partsCoefficient, divFactor))
    //        throw new ArithmeticException("Rounding necessary");
    //    return value;

    default:
        return BID_UINT64_NAN;
    }
    partsExponent += addExponent;
    if (partsCoefficient == 0)
        return BID_UINT64_ZERO;

    return get_BID64(partsSignMask, partsExponent, partsCoefficient, BID_ROUNDING_TO_NEAREST, 0/*nullptr*/);
}

bool dfp64_is_rounded(BID_UINT64 value, int n) {
    if (isNonFinite(value))
        return false;
    if (n > MAX_EXPONENT)
        return true;
    //if (n < MIN_EXPONENT)
    //    return BID_UINT64_ZERO;

    BID_UINT64 partsSignMask;
    int partsExponent;
    BID_UINT64 partsCoefficient;

    { // Copy-paste the toParts method for speedup
        partsSignMask = value & MASK_SIGN;

        if (isSpecial(value)) {
            //if (isNonFinite(value)) {
            //    partsExponent = 0;
            //    partsCoefficient = value & 0xFE03_FFFF_FFFF_FFFFL;
            //    if ((value & 0x0003_FFFF_FFFF_FFFFL) > MAX_COEFFICIENT)
            //        partsCoefficient = value & ~MASK_COEFFICIENT;
            //    if (isInfinity(value))
            //        partsCoefficient = value & MASK_SIGN_INFINITY_NAN; // TODO: Why this was done??
            //}
            //else
            {
                // Check for non-canonical values.
                BID_UINT64 coefficient = (value & LARGE_COEFFICIENT_MASK) | LARGE_COEFFICIENT_HIGH_BIT;
                partsCoefficient = coefficient > MAX_COEFFICIENT ? 0 : coefficient;

                // Extract exponent.
                BID_UINT64 tmp = value >> EXPONENT_SHIFT_LARGE;
                partsExponent = (int)(tmp & EXPONENT_MASK);
            }
        }
        else {

            // Extract exponent. Maximum biased value for "small exponent" is 0x2FF(*2=0x5FE), signed: []
            // upper 1/4 of the mask range is "special", as checked in the code above
            BID_UINT64 tmp = value >> EXPONENT_SHIFT_SMALL;
            partsExponent = (int)(tmp & EXPONENT_MASK);

            // Extract coefficient.
            partsCoefficient = (value & SMALL_COEFFICIENT_MASK);
        }
    }

    if (partsCoefficient == 0)
        return true;

    int exponent = partsExponent - EXPONENT_BIAS + n;

    if (exponent >= 0) // value is already rounded
        return true;
    // All next - negative exponent case

    { // Truncate all digits except last one
        int absPower = -exponent;
        if (absPower > MAX_FORMAT_DIGITS) {
            return false;

        }
        else {
            BID_UINT64 divFactor = POWERS_OF_TEN[absPower];
            return partsCoefficient % divFactor == 0;
        }
    }
}

BID_UINT64 dfp64_round_to_reciprocal(BID_UINT64 value, int r, enum DFP64RoundingMode roundType) {
}

bool dfp64_is_rounded_to_reciprocal(BID_UINT64 value, int r) {
}

BID_UINT64 dfp64_shorten_mantissa(BID_UINT64 value, long long delta, int minZerosCount) {

}
