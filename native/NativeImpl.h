#pragma once

#include <bid_conf.h>
#include <bid_functions.h>

#ifndef API_PREFIX
#define API_PREFIX    ddfp_
#endif

#ifndef JAVA_PREFIX
#define JAVA_PREFIX    com_epam_deltix_dfp_NativeImpl_
#endif

#ifdef __cplusplus
#define DDFP_MANGLING extern "C"
#else
#define DDFP_MANGLING
#endif

#if defined(_WIN32)
#define JNI_API(x) __declspec(dllexport) x __stdcall
#else
#define JNI_API(x) x __attribute__ ((externally_visible,visibility("default")))
#endif

#if defined(_WIN32)
#define DDFP_API(x) DDFP_MANGLING __declspec(dllexport) x __cdecl
#else
#define DDFP_API(x) DDFP_MANGLING x __attribute__ ((externally_visible,visibility("default")))
#endif

/*
 * Concatenate preprocessor tokens A and B without expanding macro definitions
 * (however, if invoked from a macro, macro arguments are expanded).
 */
#define PPCAT_NX(A, B) A ## B

/*
 * Concatenate preprocessor tokens A and B after macro-expanding them.
 */
#define PPCAT(A, B) PPCAT_NX(A, B)

typedef BID_UINT64          decimal64;
typedef char                int8;
typedef unsigned char       uint8;
typedef short               int16;
typedef unsigned short      uint16;
typedef int                 int32;
typedef unsigned int        uint32;
typedef long long           int64;
typedef unsigned long long  uint64;
typedef int                 intBool;

//https://stackoverflow.com/questions/4079243/how-can-i-use-sizeof-in-a-preprocessor-macro
#define STATIC_ASSERT(condition) typedef char p__LINE__[ (condition) ? 1 : -1];
STATIC_ASSERT(sizeof(int8) == 1)
STATIC_ASSERT(sizeof(uint8) == 1)
STATIC_ASSERT(sizeof(int16) == 2)
STATIC_ASSERT(sizeof(uint16) == 2)
STATIC_ASSERT(sizeof(int32) == 4)
STATIC_ASSERT(sizeof(uint32) == 4)
STATIC_ASSERT(sizeof(int64) == 8)
STATIC_ASSERT(sizeof(uint64) == 8)

#ifndef NOJAVA
#define JAVA_API_IMPL(X) X
#else
#define JAVA_API_IMPL(X)
#endif


#define OPNRR(mcr__name, mcr__type, mcr__body, ...)                                                         \
DDFP_API(mcr__type) PPCAT(API_PREFIX, mcr__name) (__VA_ARGS__) {                                            \
    mcr__body                                                                                               \
}                                                                                                           \
JAVA_API_IMPL(                                                                                              \
JNI_API(mcr__type) PPCAT(PPCAT(Java_, JAVA_PREFIX), mcr__name) (void *jEnv, void *jClass, __VA_ARGS__) {    \
    mcr__body                                                                                               \
}                                                                                                           \
JNI_API(mcr__type) PPCAT(PPCAT(JavaCritical_, JAVA_PREFIX), mcr__name) (__VA_ARGS__) {                      \
    mcr__body                                                                                               \
}                                                                                                           \
)

#define OPNR(mcr__name, mcr__type, mcr__body, ...)  OPNRR(mcr__name, mcr__type, return (mcr__body);, __VA_ARGS__)

#define OPN(mcr__name, mcr__body, ...)              OPNR(mcr__name, BID_UINT64, mcr__body, __VA_ARGS__)

#define OPN_BOOL(mcr__name, mcr__body, ...)         OPNR(mcr__name, intBool, mcr__body, __VA_ARGS__)

static const BID_UINT64 nanConst =  0x7C00000000000000ull;
static const BID_UINT64 zeroConst = 0x31C0000000000000ull;
static const BID_UINT64 twoConst =  0x31C0000000000002ull;

typedef double      Float64;
typedef float       Float32;
typedef int64       Int64;
typedef uint64      UInt64;
typedef int32       Int32;
typedef uint32      UInt32;
typedef int16       Int16;
typedef uint16      UInt16;
typedef int8        Int8;
typedef uint8       UInt8;
