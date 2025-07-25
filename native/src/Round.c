#include "Round.h"
#include "auxiliary.h"

#define LONG_LOW_PART       0xFFFFFFFFull
#define INT32_MAX_VALUE     0x7fffffffull

int int_max(int x, int y) {
    return x >= y ? x : y;
}

int int_min(int x, int y) {
    return x <= y ? x : y;
}

bool is_rounded_impl(int addExponent, long partsCoefficient, long divFactor) {
    return addExponent == 0 && partsCoefficient % divFactor == 0;
}

BID_UINT64 dfp64_round(BID_UINT64 value, int32 n, DFP64RoundingMode roundType) {
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
    case UP:
        partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor - 1) / divFactor) * divFactor : divFactor;
        break;

    case DOWN:
        partsCoefficient = addExponent == 0 ? (partsCoefficient / divFactor) * divFactor : 0;
        break;

    case CEILING:
        if (partsSignMask >= 0/*!parts.isNegative()*/)
            partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor - 1) / divFactor) * divFactor : divFactor;
        else
            partsCoefficient = addExponent == 0 ? (partsCoefficient / divFactor) * divFactor : 0;
        break;

    case FLOOR:
        if (partsSignMask >= 0/*!parts.isNegative()*/)
            partsCoefficient = addExponent == 0 ? (partsCoefficient / divFactor) * divFactor : 0;
        else
            partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor - 1) / divFactor) * divFactor : divFactor;
        break;

    case HALF_UP:
        partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor / 2) / divFactor) * divFactor : 0;
        break;

    case HALF_DOWN:
        partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor / 2 - 1) / divFactor) * divFactor : 0;
        break;

    case HALF_EVEN:
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

bool dfp64_is_rounded(BID_UINT64 value, int32 n) {
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

#define PAIR_DIV(divFactor)    {  BID_UINT64 r21 = coefficientMulR_w21 % divFactor;   coefficientMulR_w21 /= divFactor;   coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / divFactor;  }
#define PAIR_MUL(divFactor)    {  BID_UINT64 lowMul = coefficientMulR_w0 * divFactor;   coefficientMulR_w0 = lowMul & LONG_LOW_PART;   coefficientMulR_w21 = coefficientMulR_w21 * divFactor + (lowMul >> 32);  }

#define PAIR_MUL_TO_DIVFACTOR  {  PAIR_MUL(divFactor01)   if (divFactor02 > 1) PAIR_MUL(divFactor02)   if (divFactor03 > 1) PAIR_MUL(divFactor03)  }
#define PAIR_DIV_BY_DIVFACTOR  {  PAIR_DIV(divFactor01)   if (divFactor02 > 1) PAIR_DIV(divFactor02)   if (divFactor03 > 1) PAIR_DIV(divFactor03)  }

#define PAIR_DIV_MUL_DIVFACTOR {  PAIR_DIV_BY_DIVFACTOR   PAIR_MUL_TO_DIVFACTOR  }


BID_UINT64 dfp64_round_to_reciprocal(BID_UINT64 value, uint32 r, DFP64RoundingMode roundType) {
    if (isNonFinite(value))
        return value;
    //if (Math.log10(r) > JavaImpl.MAX_EXPONENT) // Never can happens
    //    return value;
    //if (Math.log10(r) < JavaImpl.MIN_EXPONENT)
    //    return JavaImpl.ZERO;

    BID_UINT64 partsCoefficient;
    BID_UINT64 partsSignMask;
    int partsExponent;
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

    int unbiasedExponent = partsExponent - EXPONENT_BIAS;

    if (unbiasedExponent >= 0) // value is already rounded
        return value;

    // Denormalize partsCoefficient to the maximal value to get the maximal precision after final r division
    {
        int dn = numberOfDigits(partsCoefficient);
        /*if (dn < POWERS_OF_TEN.length - 1)*/
        {

            int expShift = (sizeof(POWERS_OF_TEN)/sizeof(POWERS_OF_TEN[0]) - 1) - dn;
            partsExponent -= expShift;
            unbiasedExponent -= expShift;
            partsCoefficient *= POWERS_OF_TEN[expShift];
        }
    }


    //Multiply partsCoefficient with r
    BID_UINT64 coefficientMulR_w0, coefficientMulR_w21;
    {
        BID_UINT64 l0 = (LONG_LOW_PART & partsCoefficient) * r;
        coefficientMulR_w0 = LONG_LOW_PART & l0;
        coefficientMulR_w21 = (partsCoefficient >> 32) * r + (l0 >> 32);
    }

    //final long divFactor;
    uint32 divFactor01, divFactor02, divFactor03; // divFactor = divFactor1 * divFactor2 * divFactor3
    int addExponent;
    {
        int absPower = -unbiasedExponent;
        int maxPower = int_min(absPower, int_min(3 * 9, numberOfDigits(coefficientMulR_w21) + 10 /* low part */));
        //divFactor = POWERS_OF_TEN[maxPower];
        int factor1Power = int_min(maxPower, 9); // Int can hold max 1_000_000_000
        divFactor01 = (uint32)POWERS_OF_TEN[factor1Power];
        int factor2Power = int_min(maxPower - factor1Power, 9);
        divFactor02 = (uint32)POWERS_OF_TEN[factor2Power];
        divFactor03 = (uint32)POWERS_OF_TEN[maxPower - factor1Power - factor2Power];
        addExponent = absPower - maxPower;
    }

    // Process last digit
    switch (roundType) {
    case UP:
        // partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor - 1) / divFactor) * divFactor : divFactor;
        if (addExponent != 0) {
            {
                BID_UINT64 divFactor12 = (BID_UINT64)divFactor01 * divFactor02;
                coefficientMulR_w0 = LONG_LOW_PART & divFactor12;
                coefficientMulR_w21 = divFactor12 >> 32;

                if (divFactor03 > 1) PAIR_MUL(divFactor03)
            }

        }
        else { // addExponent != 0
            { // + divFactor - 1
                BID_UINT64 divFactor_w0, divFactor_w21;
                {
                    BID_UINT64 lowPart = (BID_UINT64)divFactor01 * divFactor02;
                    divFactor_w0 = lowPart & LONG_LOW_PART;
                    divFactor_w21 = lowPart >> 32;
                }
                if (divFactor03 > 1) {
                    BID_UINT64 lowMul = divFactor_w0 * divFactor03;
                    divFactor_w0 = lowMul & LONG_LOW_PART;
                    divFactor_w21 = divFactor_w21 * divFactor03 + (lowMul >> 32);
                }

                { // divFactor - 1
                    BID_UINT64 lowPart = divFactor_w0 + 0xFFFFFFFFull;
                    divFactor_w0 = lowPart & LONG_LOW_PART;
                    divFactor_w21 = divFactor_w21 + 0xFFFFFFFFFFFFFFFFull + (lowPart >> 32);
                }

                {
                    BID_UINT64 lowPart = coefficientMulR_w0 + divFactor_w0;
                    coefficientMulR_w0 = lowPart & LONG_LOW_PART;
                    coefficientMulR_w21 += divFactor_w21 + (lowPart >> 32);
                }
            }
            PAIR_DIV_MUL_DIVFACTOR
        }
        // partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor - 1) / divFactor) * divFactor : divFactor;
        break;

    case DOWN:
        // partsCoefficient = addExponent == 0 ? (partsCoefficient / divFactor) * divFactor : 0;
        if (addExponent != 0) {
            {
                coefficientMulR_w0 = 0;
                coefficientMulR_w21 = 0;
            }

        }
        else { // addExponent != 0
            PAIR_DIV_MUL_DIVFACTOR
        }
        // partsCoefficient = addExponent == 0 ? (partsCoefficient / divFactor) * divFactor : 0;
        break;

    case CEILING:
        if (partsSignMask >= 0/*!parts.isNegative()*/) {
            // partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor - 1) / divFactor) * divFactor : divFactor;
            if (addExponent != 0) {
                {
                    BID_UINT64 divFactor12 = (BID_UINT64)divFactor01 * divFactor02;
                    coefficientMulR_w0 = LONG_LOW_PART & divFactor12;
                    coefficientMulR_w21 = divFactor12 >> 32;

                    if (divFactor03 > 1) PAIR_MUL(divFactor03)
                }

            }
            else { // addExponent != 0
                { // + divFactor - 1
                    long divFactor_w0, divFactor_w21;
                    {
                        BID_UINT64 lowPart = (long)divFactor01 * divFactor02;
                        divFactor_w0 = lowPart & LONG_LOW_PART;
                        divFactor_w21 = lowPart >> 32;
                    }
                    if (divFactor03 > 1) {
                        BID_UINT64 lowMul = divFactor_w0 * divFactor03;
                        divFactor_w0 = lowMul & LONG_LOW_PART;
                        divFactor_w21 = divFactor_w21 * divFactor03 + (lowMul >> 32);
                    }

                    { // divFactor - 1
                        BID_UINT64 lowPart = divFactor_w0 + 0xFFFFFFFFull;
                        divFactor_w0 = lowPart & LONG_LOW_PART;
                        divFactor_w21 = divFactor_w21 + 0xFFFFFFFFFFFFFFFFull + (lowPart >> 32);
                    }

                    {
                        BID_UINT64 lowPart = coefficientMulR_w0 + divFactor_w0;
                        coefficientMulR_w0 = lowPart & LONG_LOW_PART;
                        coefficientMulR_w21 += divFactor_w21 + (lowPart >> 32);
                    }
                }
                PAIR_DIV_MUL_DIVFACTOR
            }
            // partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor - 1) / divFactor) * divFactor : divFactor;

        }
        else { // partsSignMask >= 0
            // partsCoefficient = addExponent == 0 ? (partsCoefficient / divFactor) * divFactor : 0;
            if (addExponent != 0) {
                {
                    coefficientMulR_w0 = 0;
                    coefficientMulR_w21 = 0;
                }

            }
            else { // addExponent != 0
                PAIR_DIV_MUL_DIVFACTOR
            }
            // partsCoefficient = addExponent == 0 ? (partsCoefficient / divFactor) * divFactor : 0;
        }
        break;

    case FLOOR:
        if (partsSignMask >= 0/*!parts.isNegative()*/) {
            // partsCoefficient = addExponent == 0 ? (partsCoefficient / divFactor) * divFactor : 0;
            if (addExponent != 0) {
                {
                    coefficientMulR_w0 = 0;
                    coefficientMulR_w21 = 0;
                }

            }
            else { // addExponent != 0
                PAIR_DIV_MUL_DIVFACTOR
            }
            // partsCoefficient = addExponent == 0 ? (partsCoefficient / divFactor) * divFactor : 0;

        }
        else { // partsSignMask >= 0
            // partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor - 1) / divFactor) * divFactor : divFactor;
            if (addExponent != 0) {
                {
                    BID_UINT64 divFactor12 = (long)divFactor01 * divFactor02;
                    coefficientMulR_w0 = LONG_LOW_PART & divFactor12;
                    coefficientMulR_w21 = divFactor12 >> 32;

                    if (divFactor03 > 1) PAIR_MUL(divFactor03)
                }

            }
            else { // addExponent != 0
                { // + divFactor - 1
                    long divFactor_w0, divFactor_w21;
                    {
                        BID_UINT64 lowPart = (long)divFactor01 * divFactor02;
                        divFactor_w0 = lowPart & LONG_LOW_PART;
                        divFactor_w21 = lowPart >> 32;
                    }
                    if (divFactor03 > 1) {
                        BID_UINT64 lowMul = divFactor_w0 * divFactor03;
                        divFactor_w0 = lowMul & LONG_LOW_PART;
                        divFactor_w21 = divFactor_w21 * divFactor03 + (lowMul >> 32);
                    }

                    { // divFactor - 1
                        BID_UINT64 lowPart = divFactor_w0 + 0xFFFFFFFFull;
                        divFactor_w0 = lowPart & LONG_LOW_PART;
                        divFactor_w21 = divFactor_w21 + 0xFFFFFFFFFFFFFFFFull + (lowPart >> 32);
                    }

                    {
                        BID_UINT64 lowPart = coefficientMulR_w0 + divFactor_w0;
                        coefficientMulR_w0 = lowPart & LONG_LOW_PART;
                        coefficientMulR_w21 += divFactor_w21 + (lowPart >> 32);
                    }
                }
                PAIR_DIV_MUL_DIVFACTOR
            }
            // partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor - 1) / divFactor) * divFactor : divFactor;
        }
        break;

    case HALF_UP:
        // partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor / 2) / divFactor) * divFactor : 0;
        if (addExponent != 0) {
            {
                coefficientMulR_w0 = 0;
                coefficientMulR_w21 = 0;
            }

        }
        else { // addExponent != 0
            { // + divFactor / 2
                long divFactor_w0, divFactor_w21;
                {
                    BID_UINT64 lowPart = (long)divFactor01 * divFactor02;
                    divFactor_w0 = lowPart & LONG_LOW_PART;
                    divFactor_w21 = lowPart >> 32;
                }
                if (divFactor03 > 1) {
                    BID_UINT64 lowMul = divFactor_w0 * divFactor03;
                    divFactor_w0 = lowMul & LONG_LOW_PART;
                    divFactor_w21 = divFactor_w21 * divFactor03 + (lowMul >> 32);
                }

                { // divFactor / 2
                    divFactor_w0 = ((divFactor_w21 & 1) << 31) | (divFactor_w0 >> 1);
                    divFactor_w21 = divFactor_w21 >> 1;
                }

                {
                    BID_UINT64 lowPart = coefficientMulR_w0 + divFactor_w0;
                    coefficientMulR_w0 = lowPart & LONG_LOW_PART;
                    coefficientMulR_w21 += divFactor_w21 + (lowPart >> 32);
                }
            }
            PAIR_DIV_MUL_DIVFACTOR
        }
        // partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor / 2) / divFactor) * divFactor : 0;
        break;

    case HALF_DOWN:
        // partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor / 2 - 1) / divFactor) * divFactor : 0;
        if (addExponent != 0) {
            {
                coefficientMulR_w0 = 0;
                coefficientMulR_w21 = 0;
            }

        }
        else { // addExponent != 0
            { // + divFactor / 2
                long divFactor_w0, divFactor_w21;
                {
                    BID_UINT64 lowPart = (long)divFactor01 * divFactor02;
                    divFactor_w0 = lowPart & LONG_LOW_PART;
                    divFactor_w21 = lowPart >> 32;
                }
                if (divFactor03 > 1) {
                    BID_UINT64 lowMul = divFactor_w0 * divFactor03;
                    divFactor_w0 = lowMul & LONG_LOW_PART;
                    divFactor_w21 = divFactor_w21 * divFactor03 + (lowMul >> 32);
                }

                { // divFactor / 2
                    divFactor_w0 = ((divFactor_w21 & 1) << 31) | (divFactor_w0 >> 1);
                    divFactor_w21 = divFactor_w21 >> 1;
                }

                { // divFactor - 1
                    BID_UINT64 lowPart = divFactor_w0 + 0xFFFFFFFFull;
                    divFactor_w0 = lowPart & LONG_LOW_PART;
                    divFactor_w21 = divFactor_w21 + 0xFFFFFFFFFFFFFFFFull + (lowPart >> 32);
                }

                {
                    BID_UINT64 lowPart = coefficientMulR_w0 + divFactor_w0;
                    coefficientMulR_w0 = lowPart & LONG_LOW_PART;
                    coefficientMulR_w21 += divFactor_w21 + (lowPart >> 32);
                }
            }
            PAIR_DIV_MUL_DIVFACTOR
        }
        // partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor / 2 - 1) / divFactor) * divFactor : 0;
        break;

    case HALF_EVEN:
        // partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor / 2 - 1 + ((partsCoefficient / divFactor) & 1L)) / divFactor) * divFactor : 0;
        if (addExponent != 0) {
            {
                coefficientMulR_w0 = 0;
                coefficientMulR_w21 = 0;
            }

        }
        else { // addExponent != 0
            { // + divFactor / 2
                long divFactor_w0, divFactor_w21;
                {
                    BID_UINT64 lowPart = (long)divFactor01 * divFactor02;
                    divFactor_w0 = lowPart & LONG_LOW_PART;
                    divFactor_w21 = lowPart >> 32;
                }
                if (divFactor03 > 1) {
                    BID_UINT64 lowMul = divFactor_w0 * divFactor03;
                    divFactor_w0 = lowMul & LONG_LOW_PART;
                    divFactor_w21 = divFactor_w21 * divFactor03 + (lowMul >> 32);
                }

                { // divFactor / 2
                    divFactor_w0 = ((divFactor_w21 & 1) << 31) | (divFactor_w0 >> 1);
                    divFactor_w21 = divFactor_w21 >> 1;
                }

                bool divisionLatestBit;
                { // ((partsCoefficient / divFactor) & 1L)
                    long tmpCoefficientMulR_w0 = coefficientMulR_w0;
                    long tmpCoefficientMulR_w21 = coefficientMulR_w21;
                    { // / divFactor
                        {
                            BID_UINT64 r21 = tmpCoefficientMulR_w21 % divFactor01;
                            tmpCoefficientMulR_w21 /= divFactor01;
                            tmpCoefficientMulR_w0 = ((r21 << 32) | tmpCoefficientMulR_w0) / divFactor01;
                        }

                        if (divFactor02 > 1) {
                            BID_UINT64 r21 = tmpCoefficientMulR_w21 % divFactor02;
                            tmpCoefficientMulR_w21 /= divFactor02;
                            tmpCoefficientMulR_w0 = ((r21 << 32) | tmpCoefficientMulR_w0) / divFactor02;
                        }

                        if (divFactor03 > 1) {
                            BID_UINT64 r21 = tmpCoefficientMulR_w21 % divFactor03;
                            // tmpCoefficientMulR_w21 /= divFactor03; // No need high words
                            tmpCoefficientMulR_w0 = ((r21 << 32) | tmpCoefficientMulR_w0) / divFactor03;
                        }
                    }

                    divisionLatestBit = (tmpCoefficientMulR_w0 & 1) != 0;
                }

                if (!divisionLatestBit) { // divFactor - 1
                    BID_UINT64 lowPart = divFactor_w0 + 0xFFFFFFFFull;
                    divFactor_w0 = lowPart & LONG_LOW_PART;
                    divFactor_w21 = divFactor_w21 + 0xFFFFFFFFFFFFFFFFull + (lowPart >> 32);
                }

                {
                    BID_UINT64 lowPart = coefficientMulR_w0 + divFactor_w0;
                    coefficientMulR_w0 = lowPart & LONG_LOW_PART;
                    coefficientMulR_w21 += divFactor_w21 + (lowPart >> 32);
                }
            }
            PAIR_DIV_MUL_DIVFACTOR
        }
        // partsCoefficient = addExponent == 0 ? ((partsCoefficient + divFactor / 2 - 1 + ((partsCoefficient / divFactor) & 1L)) / divFactor) * divFactor : 0;
        break;

    default:
        return BID_UINT64_NAN;
    }

    { // / r
        BID_UINT64 r21 = coefficientMulR_w21 % r;
        coefficientMulR_w21 /= r;
        coefficientMulR_w0 = ((r21 << 32) | coefficientMulR_w0) / r;

        if (coefficientMulR_w21 > INT32_MAX_VALUE) {
            int dn = numberOfDigits(coefficientMulR_w21 / INT32_MAX_VALUE);
            uint32 f = (uint32)POWERS_OF_TEN[dn];

            partsExponent += dn;

            {
                BID_UINT64 f21 = coefficientMulR_w21 % f;
                coefficientMulR_w21 /= f;
                coefficientMulR_w0 = ((f21 << 32) | coefficientMulR_w0) / f;
            }
        }

        partsCoefficient = ((LONG_LOW_PART & coefficientMulR_w21) << 32) + coefficientMulR_w0;
    }

    partsExponent += addExponent;
    if (partsCoefficient == 0)
        return BID_UINT64_ZERO;

    return get_BID64(partsSignMask, partsExponent, partsCoefficient, BID_ROUNDING_TO_NEAREST, 0/*nullptr*/);
}

bool dfp64_is_rounded_to_reciprocal(BID_UINT64 value, uint32 r) {
}

BID_UINT64 dfp64_shorten_mantissa(BID_UINT64 value, BID_SINT64 delta, uint32 minZerosCount) {

}
