#include "NativeImpl.h"

//region Conversion

OPN(fromFloat64, binary64_to_bid64(x), double x)
OPN(fromFloat32, binary32_to_bid64(x), float x)
OPN(fromInt64, bid64_from_int64(x), int64 x)
OPN(fromUInt64, bid64_from_uint64(x), uint64 x)
OPNR(toFloat64, double, bid64_to_binary64(x), BID_UINT64 x)
OPNR(toFloat32, float, bid64_to_binary32(x), BID_UINT64 x)
OPNR(toInt64, int64, bid64_to_int64_xint(x), BID_UINT64 x)
OPNR(toUInt64, uint64, bid64_to_uint64_xint(x), BID_UINT64 x)
OPN(fromFixedPoint64, bid64_scalbn(bid64_from_int64(mantissa), -tenPowerFactor), int64 mantissa, int32 tenPowerFactor)
OPNR(toFixedPoint, int64, bid64_to_int64_xint(bid64_scalbn(value, numberOfDigits)), BID_UINT64 value, int32 numberOfDigits)

//endregion

//region Classification

OPN_BOOL(isNaN, bid64_isNaN(x), BID_UINT64 x)
OPN_BOOL(isInfinity, bid64_isInf(x), BID_UINT64 x)
OPN_BOOL(isPositiveInfinity, (intBool)(bid64_isInf(x) && !bid64_isSigned(x)), BID_UINT64 x)
OPN_BOOL(isNegativeInfinity, (intBool)(bid64_isInf(x) && bid64_isSigned(x)), BID_UINT64 x)
OPN_BOOL(isFinite, bid64_isFinite(x), BID_UINT64 x)
OPN_BOOL(isNormal, bid64_isNormal(x), BID_UINT64 x)
OPN_BOOL(signBit, bid64_isSigned(x), BID_UINT64 x)

//endregion

//region Comparison

DDFP_API(int32) PPCAT(API_PREFIX, compare) ( BID_UINT64 a, BID_UINT64 b) {
    if (bid64_quiet_less(a, b))
        return -1;
    if (bid64_quiet_greater(a, b))
        return 1;
    if (bid64_quiet_equal(a, b))
        return 0;
    return bid64_isNaN(b) - bid64_isNaN(a);
}
JNI_API(int32) PPCAT(PPCAT(Java_, JAVA_PREFIX), compare) (void *jEnv, void *jClass,  BID_UINT64 a, BID_UINT64 b) {
    if (bid64_quiet_less(a, b))
        return -1;
    if (bid64_quiet_greater(a, b))
        return 1;
    if (bid64_quiet_equal(a, b))
        return 0;
    return bid64_isNaN(a) - bid64_isNaN(b);
}
JNI_API(int32) PPCAT(PPCAT(JavaCritical_, JAVA_PREFIX), compare) ( BID_UINT64 a, BID_UINT64 b) {
    if (bid64_quiet_less(a, b))
        return -1;
    if (bid64_quiet_greater(a, b))
        return 1;
    if (bid64_quiet_equal(a, b))
        return 0;
    return bid64_isNaN(a) - bid64_isNaN(b);
}

OPN_BOOL(isEqual, bid64_quiet_equal(a, b), BID_UINT64 a, BID_UINT64 b)
OPN_BOOL(isNotEqual, bid64_quiet_not_equal(a, b), BID_UINT64 a, BID_UINT64 b)
OPN_BOOL(isLess, bid64_quiet_less(a, b), BID_UINT64 a, BID_UINT64 b)
OPN_BOOL(isLessOrEqual, bid64_quiet_less_equal(a, b), BID_UINT64 a, BID_UINT64 b)
OPN_BOOL(isGreater, bid64_quiet_greater(a, b), BID_UINT64 a, BID_UINT64 b)
OPN_BOOL(isGreaterOrEqual, bid64_quiet_greater_equal(a, b), BID_UINT64 a, BID_UINT64 b)
OPN_BOOL(isZero, bid64_isZero(a), BID_UINT64 a)
OPN_BOOL(isNonZero, bid64_quiet_not_equal(a, zeroConst), BID_UINT64 a)
OPN_BOOL(isPositive, bid64_quiet_greater(a, zeroConst), BID_UINT64 a)
OPN_BOOL(isNegative, bid64_quiet_less(a, zeroConst), BID_UINT64 a)
OPN_BOOL(isNonPositive, bid64_quiet_less_equal(a, zeroConst), BID_UINT64 a)
OPN_BOOL(isNonNegative, bid64_quiet_greater_equal(a, zeroConst), BID_UINT64 a)

//endregion

//region Rounding

OPN(roundTowardsPositiveInfinity, bid64_round_integral_positive(x), BID_UINT64 x)
OPN(roundTowardsNegativeInfinity, bid64_round_integral_negative(x), BID_UINT64 x)
OPN(roundTowardsZero, bid64_round_integral_zero(x), BID_UINT64 x)
OPN(roundToNearestTiesAwayFromZero, bid64_round_integral_nearest_away(x), BID_UINT64 x)
OPN(roundToNearestTiesToEven, bid64_round_integral_nearest_even(x), BID_UINT64 x)

//endregion

//region Minimum & Maximum

OPN(max2, bid64_isNaN(a) || bid64_isNaN(b) ? nanConst : bid64_maxnum(a, b), BID_UINT64 a, BID_UINT64 b)
OPN(max3, bid64_isNaN(a) || bid64_isNaN(b) || bid64_isNaN(c) ? nanConst : bid64_maxnum(bid64_maxnum(a, b), c), BID_UINT64 a, BID_UINT64 b, BID_UINT64 c)
OPN(max4, bid64_isNaN(a) || bid64_isNaN(b) || bid64_isNaN(c) || bid64_isNaN(d) ? nanConst : bid64_maxnum(bid64_maxnum(a, b), bid64_maxnum(c, d)), BID_UINT64 a, BID_UINT64 b, BID_UINT64 c, BID_UINT64 d)
OPN(min2, bid64_isNaN(a) || bid64_isNaN(b) ? nanConst : bid64_minnum(a, b), BID_UINT64 a, BID_UINT64 b)
OPN(min3, bid64_isNaN(a) || bid64_isNaN(b) || bid64_isNaN(c) ? nanConst : bid64_minnum(bid64_minnum(a, b), c), BID_UINT64 a, BID_UINT64 b, BID_UINT64 c)
OPN(min4, bid64_isNaN(a) || bid64_isNaN(b) || bid64_isNaN(c) || bid64_isNaN(d) ? nanConst : bid64_minnum(bid64_minnum(a, b), bid64_minnum(c, d)), BID_UINT64 a, BID_UINT64 b, BID_UINT64 c, BID_UINT64 d)

//endregion

//region Arithmetic

OPN(negate, bid64_negate(x), BID_UINT64 x)
OPN(abs, bid64_abs(x), BID_UINT64 x)
OPN(add2, bid64_add(a, b), BID_UINT64 a, BID_UINT64 b)
OPN(add3, bid64_add(bid64_add(a, b), c), BID_UINT64 a, BID_UINT64 b, BID_UINT64 c)
OPN(add4, bid64_add(bid64_add(a, b), bid64_add(c, d)), BID_UINT64 a, BID_UINT64 b, BID_UINT64 c, BID_UINT64 d)
OPN(subtract, bid64_sub(a, b), BID_UINT64 a, BID_UINT64 b)
OPN(multiply2, bid64_mul(a, b), BID_UINT64 a, BID_UINT64 b)
OPN(multiply3, bid64_mul(bid64_mul(a, b), c), BID_UINT64 a, BID_UINT64 b, BID_UINT64 c)
OPN(multiply4, bid64_mul(bid64_mul(a, b), bid64_mul(c, d)), BID_UINT64 a, BID_UINT64 b, BID_UINT64 c, BID_UINT64 d)
OPN(multiplyByInt32, bid64_mul(a, bid64_from_int32(integer)), BID_UINT64 a, int32 integer)
OPN(multiplyByInt64, bid64_mul(a, bid64_from_int64(integer)), BID_UINT64 a, int64 integer)
OPN(divide, bid64_div(a, b), BID_UINT64 a, BID_UINT64 b)
OPN(divideByInt32, bid64_div(x, bid64_from_int32(integer)), BID_UINT64 x, int32 integer)
OPN(divideByInt64, bid64_div(x, bid64_from_int64(integer)), BID_UINT64 x, int64 integer)
OPN(multiplyAndAdd, bid64_fma(a, b, c), BID_UINT64 a, BID_UINT64 b, BID_UINT64 c)
OPN(scaleByPowerOfTen, bid64_scalbn(a, n) , BID_UINT64 a, int32 n)
OPN(mean2, bid64_div(bid64_add(a, b), twoConst), BID_UINT64 a, BID_UINT64 b)

//endregion

//region Special

OPN(nextUp, bid64_nextup(x), BID_UINT64 x)
OPN(nextDown, bid64_nextdown(x), BID_UINT64 x)

//endregion

