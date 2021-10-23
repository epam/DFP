#include "Common.h"


#define OPN128(mcr__name, mcr__body, ...)                                                                               \
DDFP_API(void) PPCAT(API_PREFIX, mcr__name) (__VA_ARGS__, BID_UINT64 *retLow, BID_UINT64 *retHigh) {                    \
    mcr__body;                                                                                                          \
    *retLow = ret.w[0];                                                                                                 \
    *retHigh = ret.w[1];                                                                                                \
}                                                                                                                       \
JNI_API(void) PPCAT(PPCAT(Java_, JAVA_PREFIX), mcr__name) (JNIEnv *jEnv, jclass jClass, __VA_ARGS__, jobject retObj) {  \
    mcr__body;                                                                                                          \
    (*jEnv)->SetLongField(jEnv, retObj, jDecimal128Low, ret.w[0]);                                                      \
    (*jEnv)->SetLongField(jEnv, retObj, jDecimal128High, ret.w[1]);                                                     \
}                                                                                                                       \

#define OPNR128(mcr__name, mcr__type, mcr__body, ...)                                                                   \
DDFP_API(mcr__type) PPCAT(API_PREFIX, mcr__name) (__VA_ARGS__) {                                                        \
    mcr__body;                                                                                                          \
    return ret;                                                                                                         \
}                                                                                                                       \
JNI_API(mcr__type) PPCAT(PPCAT(Java_, JAVA_PREFIX), mcr__name) (JNIEnv *jEnv, jclass jClass, __VA_ARGS__) {             \
    mcr__body;                                                                                                          \
    return ret;                                                                                                         \
}                                                                                                                       \
JNI_API(mcr__type) PPCAT(PPCAT(JavaCritical_, JAVA_PREFIX), mcr__name) (__VA_ARGS__) {                                  \
    mcr__body;                                                                                                          \
    return ret;                                                                                                         \
}                                                                                                                       \

#define OPN128_BOOL(mcr__name, mcr__body, ...)         OPNR128(mcr__name, intBool, mcr__body, __VA_ARGS__)

#define DEF128ARG(arg__name)    BID_UINT64 arg__name##Low, BID_UINT64 arg__name##High
#define COMB128ARG(arg__name)   BID_UINT128 arg__name = { {arg__name##Low, arg__name##High} }

JNIEnv* jEnv;
jclass jDecimal128Class;
jfieldID jDecimal128Low;
jfieldID jDecimal128High;

JNI_API(char*) PPCAT(PPCAT(Java_, JAVA_PREFIX), init) (JNIEnv* env, jclass jClass) {
    jEnv = env;

    jDecimal128Class = (*env)->FindClass(jEnv, JAVA_DECIMAL128_CLASS_PATH);
    if (!jDecimal128Class)
        return "Can't find '" JAVA_DECIMAL128_CLASS_PATH "' class";

    jDecimal128Low = (*env)->GetFieldID(jEnv, jDecimal128Class, JAVA_DECIMAL128_FIELD_LOW, "J");
    if (!jDecimal128Low)
        return "Can't find '" JAVA_DECIMAL128_FIELD_LOW "' field of the '" JAVA_DECIMAL128_CLASS_PATH "' class.";

    jDecimal128High = (*env)->GetFieldID(jEnv, jDecimal128Class, JAVA_DECIMAL128_FIELD_HIGH, "J");
    if (!jDecimal128High)
        return "Can't find '" JAVA_DECIMAL128_FIELD_HIGH "' field of the '" JAVA_DECIMAL128_CLASS_PATH "' class.";

    return NULL;
}

static const BID_UINT128 bid128NanConst = { 0x7C00000000000000ull, 0x7C00000000000000ull };
static const BID_UINT128 bid128ZeroConst = { 0x31C0000000000000ull, 0x31C0000000000000ull };
static const BID_UINT128 bid128TwoConst = { 0x31C0000000000002ull, 0x31C0000000000002ull };

//region Conversion

OPN128(bid128FromBid64, BID_UINT128 ret = bid64_to_bid128(x), BID_UINT64 x)
OPN128(bid128FromFloat64, BID_UINT128 ret = binary64_to_bid128(x), double x)
OPN128(bid128FromFloat32, BID_UINT128 ret = binary32_to_bid128(x), float x)
OPN128(bid128FromInt64, BID_UINT128 ret = bid128_from_int64(x), int64 x)
OPN128(bid128FromUInt64, BID_UINT128 ret = bid128_from_uint64(x), uint64 x)
OPNR128(bid128ToBid64, BID_UINT64, COMB128ARG(x); BID_UINT64 ret = bid128_to_bid64(x), DEF128ARG(x))
OPNR128(bid128ToFloat64, double, COMB128ARG(x); double ret = bid128_to_binary64(x), DEF128ARG(x))
OPNR128(bid128ToFloat32, float, COMB128ARG(x); float ret = bid128_to_binary32(x), DEF128ARG(x))
OPNR128(bid128ToInt64, int64, COMB128ARG(x); int64 ret = bid128_to_int64_xint(x), DEF128ARG(x))
OPNR128(bid128ToUInt64, uint64, COMB128ARG(x); uint64 ret = bid128_to_uint64_xint(x), DEF128ARG(x))
OPN128(bid128FromFixedPoint64, BID_UINT128 ret = bid128_scalbn(bid128_from_int64(mantissa), -tenPowerFactor), int64 mantissa, int32 tenPowerFactor)
OPNR128(bid128ToFixedPoint, int64, COMB128ARG(x); int64 ret = bid128_to_int64_xint(bid128_scalbn(x, numberOfDigits)), DEF128ARG(x), int32 numberOfDigits)

//endregion

//region Classification

OPN128_BOOL(bid128IsNaN, COMB128ARG(x); intBool ret = bid128_isNaN(x), DEF128ARG(x))
OPN128_BOOL(bid128IsInfinity, COMB128ARG(x); intBool ret = bid128_isInf(x), DEF128ARG(x))
OPN128_BOOL(bid128IsPositiveInfinity, COMB128ARG(x); intBool ret = (intBool)(bid128_isInf(x) && !bid128_isSigned(x)), DEF128ARG(x))
OPN128_BOOL(bid128IsNegativeInfinity, COMB128ARG(x); intBool ret = (intBool)(bid128_isInf(x) && bid128_isSigned(x)), DEF128ARG(x))
OPN128_BOOL(bid128IsFinite, COMB128ARG(x); intBool ret = bid128_isFinite(x), DEF128ARG(x))
OPN128_BOOL(bid128IsNormal, COMB128ARG(x); intBool ret = bid128_isNormal(x), DEF128ARG(x))
OPN128_BOOL(bid128SignBit, COMB128ARG(x); intBool ret = bid128_isSigned(x), DEF128ARG(x))

//endregion

//region Comparison

DDFP_API(int32) PPCAT(API_PREFIX, bid128Compare) ( DEF128ARG(a), DEF128ARG(b)) {
    COMB128ARG(a);
    COMB128ARG(b);
    if (bid128_quiet_less(a, b))
        return -1;
    if (bid128_quiet_greater(a, b))
        return 1;
    if (bid128_quiet_equal(a, b))
        return 0;
    return bid128_isNaN(b) - bid128_isNaN(a);
}
JNI_API(int32) PPCAT(PPCAT(Java_, JAVA_PREFIX), bid128Compare) (void *jEnv, void *jClass,  DEF128ARG(a), DEF128ARG(b)) {
    COMB128ARG(a);
    COMB128ARG(b);
    if (bid128_quiet_less(a, b))
        return -1;
    if (bid128_quiet_greater(a, b))
        return 1;
    if (bid128_quiet_equal(a, b))
        return 0;
    return bid128_isNaN(a) - bid128_isNaN(b);
}
JNI_API(int32) PPCAT(PPCAT(JavaCritical_, JAVA_PREFIX), bid128Compare) ( DEF128ARG(a), DEF128ARG(b)) {
    COMB128ARG(a);
    COMB128ARG(b);
    if (bid128_quiet_less(a, b))
        return -1;
    if (bid128_quiet_greater(a, b))
        return 1;
    if (bid128_quiet_equal(a, b))
        return 0;
    return bid128_isNaN(a) - bid128_isNaN(b);
}

OPN128_BOOL(bid128IsEqual, COMB128ARG(a); COMB128ARG(b); intBool ret = bid128_quiet_equal(a, b), DEF128ARG(a), DEF128ARG(b))
OPN128_BOOL(bid128IsNotEqual, COMB128ARG(a); COMB128ARG(b); intBool ret = bid128_quiet_not_equal(a, b), DEF128ARG(a), DEF128ARG(b))
OPN128_BOOL(bid128IsLess, COMB128ARG(a); COMB128ARG(b); intBool ret = bid128_quiet_less(a, b), DEF128ARG(a), DEF128ARG(b))
OPN128_BOOL(bid128IsLessOrEqual, COMB128ARG(a); COMB128ARG(b); intBool ret = bid128_quiet_less_equal(a, b), DEF128ARG(a), DEF128ARG(b))
OPN128_BOOL(bid128IsGreater, COMB128ARG(a); COMB128ARG(b); intBool ret = bid128_quiet_greater(a, b), DEF128ARG(a), DEF128ARG(b))
OPN128_BOOL(bid128IsGreaterOrEqual, COMB128ARG(a); COMB128ARG(b); intBool ret = bid128_quiet_greater_equal(a, b), DEF128ARG(a), DEF128ARG(b))
OPN128_BOOL(bid128IsZero, COMB128ARG(a); intBool ret = bid128_isZero(a), DEF128ARG(a))
OPN128_BOOL(bid128IsNonZero, COMB128ARG(a); intBool ret = bid128_quiet_not_equal(a, bid128ZeroConst), DEF128ARG(a))
OPN128_BOOL(bid128IsPositive, COMB128ARG(a); intBool ret = bid128_quiet_greater(a, bid128ZeroConst), DEF128ARG(a))
OPN128_BOOL(bid128IsNegative, COMB128ARG(a); intBool ret = bid128_quiet_less(a, bid128ZeroConst), DEF128ARG(a))
OPN128_BOOL(bid128IsNonPositive, COMB128ARG(a); intBool ret = bid128_quiet_less_equal(a, bid128ZeroConst), DEF128ARG(a))
OPN128_BOOL(bid128IsNonNegative, COMB128ARG(a); intBool ret = bid128_quiet_greater_equal(a, bid128ZeroConst), DEF128ARG(a))

//endregion

//region Rounding

OPN128(bid128RoundTowardsPositiveInfinity, COMB128ARG(a); BID_UINT128 ret = bid128_round_integral_positive(a), DEF128ARG(a))
OPN128(bid128RoundTowardsNegativeInfinity, COMB128ARG(a); BID_UINT128 ret = bid128_round_integral_negative(a), DEF128ARG(a))
OPN128(bid128RoundTowardsZero, COMB128ARG(a); BID_UINT128 ret = bid128_round_integral_zero(a), DEF128ARG(a))
OPN128(bid128RoundToNearestTiesAwayFromZero, COMB128ARG(a); BID_UINT128 ret = bid128_round_integral_nearest_away(a), DEF128ARG(a))

//endregion

//region Minimum & Maximum

OPN128(bid128Max2, COMB128ARG(a); COMB128ARG(b);
    BID_UINT128 ret = bid128_isNaN(a) || bid128_isNaN(b) ? bid128NanConst : bid128_maxnum(a, b),
    DEF128ARG(a), DEF128ARG(b))
OPN128(bid128Max3, COMB128ARG(a); COMB128ARG(b); COMB128ARG(c);
    BID_UINT128 ret = bid128_isNaN(a) || bid128_isNaN(b) || bid128_isNaN(c) ? bid128NanConst : bid128_maxnum(bid128_maxnum(a, b), c),
    DEF128ARG(a), DEF128ARG(b), DEF128ARG(c))
OPN128(bid128Max4, COMB128ARG(a); COMB128ARG(b); COMB128ARG(c); COMB128ARG(d);
    BID_UINT128 ret = bid128_isNaN(a) || bid128_isNaN(b) || bid128_isNaN(c) || bid128_isNaN(d) ? bid128NanConst : bid128_maxnum(bid128_maxnum(a, b), bid128_maxnum(c, d)),
    DEF128ARG(a), DEF128ARG(b), DEF128ARG(c), DEF128ARG(d))
OPN128(bid128Min2, COMB128ARG(a); COMB128ARG(b);
    BID_UINT128 ret = bid128_isNaN(a) || bid128_isNaN(b) ? bid128NanConst : bid128_minnum(a, b),
    DEF128ARG(a), DEF128ARG(b))
OPN128(bid128Min3, COMB128ARG(a); COMB128ARG(b); COMB128ARG(c);
    BID_UINT128 ret = bid128_isNaN(a) || bid128_isNaN(b) || bid128_isNaN(c) ? bid128NanConst : bid128_minnum(bid128_minnum(a, b), c),
    DEF128ARG(a), DEF128ARG(b), DEF128ARG(c))
OPN128(bid128Min4, COMB128ARG(a); COMB128ARG(b); COMB128ARG(c); COMB128ARG(d);
    BID_UINT128 ret = bid128_isNaN(a) || bid128_isNaN(b) || bid128_isNaN(c) || bid128_isNaN(d) ? bid128NanConst : bid128_minnum(bid128_minnum(a, b), bid128_minnum(c, d)),
    DEF128ARG(a), DEF128ARG(b), DEF128ARG(c), DEF128ARG(d))

//endregion

//region Arithmetic

OPN128(bid128Negate, COMB128ARG(a); BID_UINT128 ret = bid128_negate(a), DEF128ARG(a))
OPN128(bid128Abs, COMB128ARG(a); BID_UINT128 ret = bid128_abs(a), DEF128ARG(a))

OPN128(bid128Add2, COMB128ARG(a); COMB128ARG(b); BID_UINT128 ret = bid128_add(a, b), DEF128ARG(a), DEF128ARG(b))
OPN128(bid128Add3, COMB128ARG(a); COMB128ARG(b); COMB128ARG(c); BID_UINT128 ret = bid128_add(bid128_add(a, b), c), DEF128ARG(a), DEF128ARG(b), DEF128ARG(c))
OPN128(bid128Add4, COMB128ARG(a); COMB128ARG(b); COMB128ARG(c); COMB128ARG(d); BID_UINT128 ret = bid128_add(bid128_add(a, b), bid128_add(c, d)), DEF128ARG(a), DEF128ARG(b), DEF128ARG(c), DEF128ARG(d))

OPN128(bid128Subtract, COMB128ARG(a); COMB128ARG(b); BID_UINT128 ret = bid128_sub(a, b), DEF128ARG(a), DEF128ARG(b))

OPN128(bid128Multiply2, COMB128ARG(a); COMB128ARG(b); BID_UINT128 ret = bid128_mul(a, b), DEF128ARG(a), DEF128ARG(b))
OPN128(bid128Multiply3, COMB128ARG(a); COMB128ARG(b); COMB128ARG(c); BID_UINT128 ret = bid128_mul(bid128_mul(a, b), c), DEF128ARG(a), DEF128ARG(b), DEF128ARG(c))
OPN128(bid128Multiply4, COMB128ARG(a); COMB128ARG(b); COMB128ARG(c); COMB128ARG(d); BID_UINT128 ret = bid128_mul(bid128_mul(a, b), bid128_mul(c, d)), DEF128ARG(a), DEF128ARG(b), DEF128ARG(c), DEF128ARG(d))

OPN128(bid128MultiplyByInt32, COMB128ARG(a); BID_UINT128 ret = bid128qd_mul(a, bid64_from_int32(b)), DEF128ARG(a), int32 b)
OPN128(bid128MultiplyByInt64, COMB128ARG(a); BID_UINT128 ret = bid128_mul(a, bid128_from_int64(b)), DEF128ARG(a), int64 b)

OPN128(bid128Divide, COMB128ARG(a); COMB128ARG(b); BID_UINT128 ret = bid128_div(a, b), DEF128ARG(a), DEF128ARG(b))
OPN128(bid128DivideByInt32, COMB128ARG(a); BID_UINT128 ret = bid128qd_div(a, bid64_from_int32(b)), DEF128ARG(a), int32 b)
OPN128(bid128DivideByInt64, COMB128ARG(a); BID_UINT128 ret = bid128_div(a, bid128_from_int64(b)), DEF128ARG(a), int64 b)

OPN128(bid128MultiplyAndAdd, COMB128ARG(a); COMB128ARG(b); COMB128ARG(c); BID_UINT128 ret = bid128_fma(a, b, c), DEF128ARG(a), DEF128ARG(b), DEF128ARG(c))
OPN128(bid128ScaleByPowerOfTen, COMB128ARG(a); BID_UINT128 ret = bid128_scalbn(a, n), DEF128ARG(a), int32 n)
OPN128(bid128Mean2, COMB128ARG(a); COMB128ARG(b); BID_UINT128 ret = bid128_div(bid128_add(a, b), bid128TwoConst), DEF128ARG(a), DEF128ARG(b))


OPN128(bid128Exp, COMB128ARG(x); BID_UINT128 ret = bid128_exp(x), DEF128ARG(x))
OPN128(bid128Exp2, COMB128ARG(x); BID_UINT128 ret = bid128_exp2(x), DEF128ARG(x))
OPN128(bid128Exp10, COMB128ARG(x); BID_UINT128 ret = bid128_exp10(x), DEF128ARG(x))
OPN128(bid128Expm1, COMB128ARG(x); BID_UINT128 ret = bid128_expm1(x), DEF128ARG(x))
OPN128(bid128Log, COMB128ARG(x); BID_UINT128 ret = bid128_log(x), DEF128ARG(x))
OPN128(bid128Log2, COMB128ARG(x); BID_UINT128 ret = bid128_log2(x), DEF128ARG(x))
OPN128(bid128Log10, COMB128ARG(x); BID_UINT128 ret = bid128_log10(x), DEF128ARG(x))
OPN128(bid128Log1p, COMB128ARG(x); BID_UINT128 ret = bid128_log1p(x), DEF128ARG(x))
OPN128(bid128Pow, COMB128ARG(x); COMB128ARG(y); BID_UINT128 ret = bid128_pow(x, y), DEF128ARG(x), DEF128ARG(y))
OPN128(bid128Fmod, COMB128ARG(x); COMB128ARG(y); BID_UINT128 ret = bid128_fmod(x, y), DEF128ARG(x), DEF128ARG(y))
//OPN128(bid128Modf, COMB128ARG(x); COMB128ARG(y); BID_UINT128 ret = bid128_modf(x, iptr), DEF128ARG(x), DEF128ARG(*iptr))
OPN128(bid128Hypot, COMB128ARG(x); COMB128ARG(y); BID_UINT128 ret = bid128_hypot(x, y), DEF128ARG(x), DEF128ARG(y))
OPN128(bid128Sin, COMB128ARG(x); BID_UINT128 ret = bid128_sin(x), DEF128ARG(x))
OPN128(bid128Cos, COMB128ARG(x); BID_UINT128 ret = bid128_cos(x), DEF128ARG(x))
OPN128(bid128Tan, COMB128ARG(x); BID_UINT128 ret = bid128_tan(x), DEF128ARG(x))
OPN128(bid128Asin, COMB128ARG(x); BID_UINT128 ret = bid128_asin(x), DEF128ARG(x))
OPN128(bid128Acos, COMB128ARG(x); BID_UINT128 ret = bid128_acos(x), DEF128ARG(x))
OPN128(bid128Atan, COMB128ARG(x); BID_UINT128 ret = bid128_atan(x), DEF128ARG(x))
OPN128(bid128Atan2, COMB128ARG(y); COMB128ARG(x); BID_UINT128 ret = bid128_atan2(y, x), DEF128ARG(y), DEF128ARG(x))
OPN128(bid128Sinh, COMB128ARG(x); BID_UINT128 ret = bid128_sinh(x), DEF128ARG(x))
OPN128(bid128Cosh, COMB128ARG(x); BID_UINT128 ret = bid128_cosh(x), DEF128ARG(x))
OPN128(bid128Tanh, COMB128ARG(x); BID_UINT128 ret = bid128_tanh(x), DEF128ARG(x))
OPN128(bid128Asinh, COMB128ARG(x); BID_UINT128 ret = bid128_asinh(x), DEF128ARG(x))
OPN128(bid128Acosh, COMB128ARG(x); BID_UINT128 ret = bid128_acosh(x), DEF128ARG(x))
OPN128(bid128Atanh, COMB128ARG(x); BID_UINT128 ret = bid128_atanh(x), DEF128ARG(x))
OPN128(bid128Erf, COMB128ARG(x); BID_UINT128 ret = bid128_erf(x), DEF128ARG(x))
OPN128(bid128Erfc, COMB128ARG(x); BID_UINT128 ret = bid128_erfc(x), DEF128ARG(x))
OPN128(bid128Tgamma, COMB128ARG(x); BID_UINT128 ret = bid128_tgamma(x), DEF128ARG(x))
OPN128(bid128Lgamma, COMB128ARG(x); BID_UINT128 ret = bid128_lgamma(x), DEF128ARG(x))
OPN128(bid128Add, COMB128ARG(x); COMB128ARG(y); BID_UINT128 ret = bid128_add(x, y), DEF128ARG(x), DEF128ARG(y))
OPN128(bid128Sub, COMB128ARG(x); COMB128ARG(y); BID_UINT128 ret = bid128_sub(x, y), DEF128ARG(x), DEF128ARG(y))
OPN128(bid128Mul, COMB128ARG(x); COMB128ARG(y); BID_UINT128 ret = bid128_mul(x, y), DEF128ARG(x), DEF128ARG(y))
OPN128(bid128Div, COMB128ARG(x); COMB128ARG(y); BID_UINT128 ret = bid128_div(x, y), DEF128ARG(x), DEF128ARG(y))
OPN128(bid128Fma, COMB128ARG(x); COMB128ARG(y); COMB128ARG(z); BID_UINT128 ret = bid128_fma(x, y, z), DEF128ARG(x),  DEF128ARG(y), DEF128ARG(z))
OPN128(bid128Sqrt, COMB128ARG(x); BID_UINT128 ret = bid128_sqrt(x), DEF128ARG(x))
OPN128(bid128Cbrt, COMB128ARG(x); BID_UINT128 ret = bid128_cbrt(x), DEF128ARG(x))

OPN128_BOOL(bid128QuietEqual, COMB128ARG(x); COMB128ARG(y); intBool ret = bid128_quiet_equal(x, y), DEF128ARG(x), DEF128ARG(y))
OPN128_BOOL(bid128QuietGreater, COMB128ARG(x); COMB128ARG(y); intBool ret = bid128_quiet_greater(x, y), DEF128ARG(x), DEF128ARG(y))
OPN128_BOOL(bid128QuietGreaterEqual, COMB128ARG(x); COMB128ARG(y); intBool ret = bid128_quiet_greater_equal(x, y), DEF128ARG(x), DEF128ARG(y))
OPN128_BOOL(bid128QuietGreaterUnordered, COMB128ARG(x); COMB128ARG(y); intBool ret = bid128_quiet_greater_unordered(x, y), DEF128ARG(x), DEF128ARG(y))
OPN128_BOOL(bid128QuietLess, COMB128ARG(x); COMB128ARG(y); intBool ret = bid128_quiet_less(x, y), DEF128ARG(x), DEF128ARG(y))
OPN128_BOOL(bid128QuietLessEqual, COMB128ARG(x); COMB128ARG(y); intBool ret = bid128_quiet_less_equal(x, y), DEF128ARG(x), DEF128ARG(y))
OPN128_BOOL(bid128QuietLessUnordered, COMB128ARG(x); COMB128ARG(y); intBool ret = bid128_quiet_less_unordered(x, y), DEF128ARG(x), DEF128ARG(y))
OPN128_BOOL(bid128QuietNotEqual, COMB128ARG(x); COMB128ARG(y); intBool ret = bid128_quiet_not_equal(x, y), DEF128ARG(x), DEF128ARG(y))
OPN128_BOOL(bid128QuietNotGreater, COMB128ARG(x); COMB128ARG(y); intBool ret = bid128_quiet_not_greater(x, y), DEF128ARG(x), DEF128ARG(y))
OPN128_BOOL(bid128QuietNotLess, COMB128ARG(x); COMB128ARG(y); intBool ret = bid128_quiet_not_less(x, y), DEF128ARG(x), DEF128ARG(y))
OPN128_BOOL(bid128QuietOrdered, COMB128ARG(x); COMB128ARG(y); intBool ret = bid128_quiet_ordered(x, y), DEF128ARG(x), DEF128ARG(y))
OPN128_BOOL(bid128QuietUnordered, COMB128ARG(x); COMB128ARG(y); intBool ret = bid128_quiet_unordered(x, y), DEF128ARG(x), DEF128ARG(y))

OPN128(bid128RoundIntegralExact, COMB128ARG(x); BID_UINT128 ret = bid128_round_integral_exact(x), DEF128ARG(x))
OPN128(bid128RoundIntegralNearestEven, COMB128ARG(x); BID_UINT128 ret = bid128_round_integral_nearest_even(x), DEF128ARG(x))
OPN128(bid128RoundIntegralNegative, COMB128ARG(x); BID_UINT128 ret = bid128_round_integral_negative(x), DEF128ARG(x))
OPN128(bid128RoundIntegralPositive, COMB128ARG(x); BID_UINT128 ret = bid128_round_integral_positive(x), DEF128ARG(x))
OPN128(bid128RoundIntegralZero, COMB128ARG(x); BID_UINT128 ret = bid128_round_integral_zero(x), DEF128ARG(x))
OPN128(bid128RoundIntegralNearestAway, COMB128ARG(x); BID_UINT128 ret = bid128_round_integral_nearest_away(x), DEF128ARG(x))

OPN128(bid128Nextup, COMB128ARG(x); BID_UINT128 ret = bid128_nextup(x), DEF128ARG(x))
OPN128(bid128Nextdown, COMB128ARG(x); BID_UINT128 ret = bid128_nextdown(x), DEF128ARG(x))
OPN128(bid128Nextafter, COMB128ARG(x); COMB128ARG(y); BID_UINT128 ret = bid128_nextafter(x, y), DEF128ARG(x), DEF128ARG(y))
OPN128(bid128Minnum, COMB128ARG(x); COMB128ARG(y); BID_UINT128 ret = bid128_minnum(x, y), DEF128ARG(x), DEF128ARG(y))
OPN128(bid128MinnumMag, COMB128ARG(x); COMB128ARG(y); BID_UINT128 ret = bid128_minnum_mag(x, y), DEF128ARG(x), DEF128ARG(y))
OPN128(bid128Maxnum, COMB128ARG(x); COMB128ARG(y); BID_UINT128 ret = bid128_maxnum(x, y), DEF128ARG(x), DEF128ARG(y))
OPN128(bid128MaxnumMag, COMB128ARG(x); COMB128ARG(y); BID_UINT128 ret = bid128_maxnum_mag(x, y), DEF128ARG(x), DEF128ARG(y))

OPN128(bid128FromInt32, BID_UINT128 ret = bid128_from_int32(x), int32 x)
OPN128(bid128FromUint32, BID_UINT128 ret = bid128_from_uint32(x), uint32 x)
//OPN128(bid128FromInt64, BID_UINT128 ret = bid128_from_int64(x), int64 x)
OPN128(bid128FromUint64, BID_UINT128 ret = bid128_from_uint64(x), uint64 x)

OPN128_BOOL(bid128IsSigned, COMB128ARG(x); intBool ret = bid128_isSigned(x), DEF128ARG(x))
//OPN128_BOOL(bid128IsNormal, COMB128ARG(x); intBool ret = bid128_isNormal(x), DEF128ARG(x))
OPN128_BOOL(bid128IsSubnormal, COMB128ARG(x); intBool ret = bid128_isSubnormal(x), DEF128ARG(x))
//OPN128_BOOL(bid128IsFinite, COMB128ARG(x); intBool ret = bid128_isFinite(x), DEF128ARG(x))
//OPN128_BOOL(bid128IsZero, COMB128ARG(x); intBool ret = bid128_isZero(x), DEF128ARG(x))
OPN128_BOOL(bid128IsInf, COMB128ARG(x); intBool ret = bid128_isInf(x), DEF128ARG(x))
OPN128_BOOL(bid128IsSignaling, COMB128ARG(x); intBool ret = bid128_isSignaling(x), DEF128ARG(x))
OPN128_BOOL(bid128IsCanonical, COMB128ARG(x); intBool ret = bid128_isCanonical(x), DEF128ARG(x))
//OPN128_BOOL(bid128IsNaN, COMB128ARG(x); intBool ret = bid128_isNaN(x), DEF128ARG(x))

OPN128(bid128Copy, COMB128ARG(x); BID_UINT128 ret = bid128_copy(x), DEF128ARG(x))
//OPN128(bid128Negate, COMB128ARG(x); BID_UINT128 ret = bid128_negate(x), DEF128ARG(x))
//OPN128(bid128Abs, COMB128ARG(x); BID_UINT128 ret = bid128_abs(x), DEF128ARG(x))
OPN128(bid128CopySign, COMB128ARG(x); COMB128ARG(y); BID_UINT128 ret = bid128_copySign(x, y), DEF128ARG(x), DEF128ARG(y))

OPNR128(bid128Class, int32, COMB128ARG(x); int32 ret = bid128_class(x), DEF128ARG(x))
OPN128_BOOL(bid128SameQuantum, COMB128ARG(x); COMB128ARG(y); intBool ret = bid128_sameQuantum(x, y), DEF128ARG(x), DEF128ARG(y))
OPN128_BOOL(bid128TotalOrder, COMB128ARG(x); COMB128ARG(y); intBool ret = bid128_totalOrder(x, y), DEF128ARG(x), DEF128ARG(y))
OPN128_BOOL(bid128TotalOrderMag, COMB128ARG(x); COMB128ARG(y); intBool ret = bid128_totalOrderMag(x, y), DEF128ARG(x), DEF128ARG(y))
OPNR128(bid128Radix, int32, COMB128ARG(x); int32 ret = bid128_radix(x), DEF128ARG(x))

OPN128(bid128Rem, COMB128ARG(x); COMB128ARG(y); BID_UINT128 ret = bid128_rem(x, y), DEF128ARG(x), DEF128ARG(y))
OPNR128(bid128Ilogb, int32, COMB128ARG(x); int32 ret = bid128_ilogb(x), DEF128ARG(x))
OPN128(bid128Scalbn, COMB128ARG(x); BID_UINT128 ret = bid128_scalbn(x, n), DEF128ARG(x), int32 n)
OPN128(bid128Ldexp, COMB128ARG(x); BID_UINT128 ret = bid128_ldexp(x, n), DEF128ARG(x), int32 n)

OPN128(bid128Quantize, COMB128ARG(x); COMB128ARG(y); BID_UINT128 ret = bid128_quantize(x, y), DEF128ARG(x), DEF128ARG(y))
OPNR128(bid128ToBinary32, float, COMB128ARG(x); float ret = bid128_to_binary32(x), DEF128ARG(x))
OPNR128(bid128ToBinary64, double, COMB128ARG(x); double ret = bid128_to_binary64(x), DEF128ARG(x))
OPN128(bid128Logb, COMB128ARG(x); BID_UINT128 ret = bid128_logb(x), DEF128ARG(x))
OPN128(bid128Nearbyint, COMB128ARG(x); BID_UINT128 ret = bid128_nearbyint(x), DEF128ARG(x))
OPNR128(bid128Llrint, int64, COMB128ARG(x); int64 ret = bid128_llrint(x), DEF128ARG(x))
OPNR128(bid128Llround, int64, COMB128ARG(x); int64 ret = bid128_llround(x), DEF128ARG(x))
OPN128(bid128Fdim, COMB128ARG(x); COMB128ARG(y); BID_UINT128 ret = bid128_fdim(x, y), DEF128ARG(x), DEF128ARG(y))
OPNR128(bid128Quantexp, int32, COMB128ARG(x); int32 ret = bid128_quantexp(x), DEF128ARG(x))
OPN128(bid128Quantum, COMB128ARG(x); BID_UINT128 ret = bid128_quantum(x), DEF128ARG(x))
OPNR128(bid128Llquantexp, int64, COMB128ARG(x); int64 ret = bid128_llquantexp(x), DEF128ARG(x))

//endregion

//region Special

OPN128(bid128NextUp, COMB128ARG(x); BID_UINT128 ret = bid128_nextup(x), DEF128ARG(x))
OPN128(bid128nextDown, COMB128ARG(x); BID_UINT128 ret = bid128_nextdown(x), DEF128ARG(x))

//endregion
