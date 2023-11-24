cmake_minimum_required (VERSION 3.22)
project (native VERSION ${VERSION})

if(NOT CMAKE_BUILD_TYPE)
	set(CMAKE_BUILD_TYPE Release)
endif()

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${CMAKE_C_FLAGS_IN}")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${CMAKE_CXX_FLAGS_IN}")

if (TARGET)
	set(TARGET_FLAGS "--target=${TARGET} ${TARGET_EX}")
	if (SYSROOT)
		set(TARGET_FLAGS "${TARGET_FLAGS} --sysroot=${SYSROOT} -I${SYSROOT}/include/c++/v1")
	endif()
	set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${TARGET_FLAGS}")
	set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${TARGET_FLAGS}")
endif()

if (MSVC AND CMAKE_C_COMPILER_ID STREQUAL "Clang")
	set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -w")
	set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -w")
endif()

message(STATUS "CMAKE_SYSTEM_PROCESSOR: " ${CMAKE_SYSTEM_PROCESSOR})
message(STATUS "CMAKE_C_COMPILER_TARGET: " ${CMAKE_C_COMPILER_TARGET})
message(STATUS "CMAKE_CXX_COMPILER_TARGET: " ${CMAKE_CXX_COMPILER_TARGET})
message(STATUS "CMAKE_CROSSCOMPILING: " ${CMAKE_CROSSCOMPILING})
message(STATUS "CMAKE_C_FLAGS: " ${CMAKE_C_FLAGS})
message(STATUS "CMAKE_CXX_FLAGS: " ${CMAKE_CXX_FLAGS})

if (JAVA_PREFIX)
	file(WRITE ${CMAKE_INSTALL_PREFIX}/cmakevariables.json "\
	{\n\
		\"CMAKE_SYSTEM_NAME\": \"${CMAKE_SYSTEM_NAME}\",\n\
		\"CMAKE_TOOLCHAIN_FILE\": \"${CMAKE_TOOLCHAIN_FILE}\",\n\
		\"CMAKE_SYSTEM_PROCESSOR\": \"${CMAKE_SYSTEM_PROCESSOR}\",\n\
		\"CMAKE_C_COMPILER_TARGET\": \"${CMAKE_C_COMPILER_TARGET}\",\n\
		\"CMAKE_CROSSCOMPILING\": \"${CMAKE_CROSSCOMPILING}\",\n\
		\"CMAKE_SIZEOF_VOID_P\": \"${CMAKE_SIZEOF_VOID_P}\",\n\
		\"CMAKE_BUILD_TYPE\": \"${CMAKE_BUILD_TYPE}\",\n\
		\"CMAKE_C_COMPILER\": \"${CMAKE_C_COMPILER}\",\n\
		\"CMAKE_C_COMPILER_ID\": \"${CMAKE_C_COMPILER_ID}\",\n\
		\"CMAKE_C_COMPILER_VERSION\": \"${CMAKE_C_COMPILER_VERSION}\",\n\
		\"CMAKE_C_FLAGS\": \"${CMAKE_C_FLAGS}\",\n\
	}")
	set(BUILD_SHARED_LIBS ON)
endif()

set(CALL_BY_REF "0" CACHE STRING "The numerical arguments and results are passed by reference")
set(GLOBAL_RND "1" CACHE STRING "The rounding mode is a global variable _IDEC_glbround")
set(GLOBAL_FLAGS "1" CACHE STRING "The exception status flags are represented by a global variable _IDEC_glbflags")

if(APPLE)
	add_definitions(-DAPPLE)
	add_definitions(-DMINIMAL_SILENT_MODE_EXCEPTION_HANDLER)
elseif(UNIX)
	add_definitions(-DLINUX)
	add_definitions(-Dlinux)
elseif(WIN32)
	add_definitions(-DWINDOWS)
	add_definitions(-Dwinnt)
	add_definitions(-MT)
endif()
add_definitions(-DUSE_COMPILER_F128_TYPE=0)
add_definitions(-DUSE_COMPILER_F80_TYPE=0)
add_definitions(-DUSE_NATIVE_QUAD_TYPE=0)
add_definitions(-Defi2)
add_definitions(-DDECIMAL_CALL_BY_REFERENCE=${CALL_BY_REF})
add_definitions(-DDECIMAL_GLOBAL_ROUNDING=${GLOBAL_RND})
add_definitions(-DDECIMAL_GLOBAL_EXCEPTION_FLAGS=${GLOBAL_FLAGS})

#set(CMAKE_C_VISIBILITY_PRESET default)
#set(CMAKE_CXX_VISIBILITY_PRESET default)
#set(CMAKE_VISIBILITY_INLINES_HIDDEN 0)
set(CMAKE_POSITION_INDEPENDENT_CODE ON CACHE BOOL "Position-independent code flag")
#set(CMAKE_CXX_STANDARD 11)

if (MSVC)
	add_definitions(-D_CRT_SECURE_NO_DEPRECATE)
	add_definitions(-D_SCL_SECURE_NO_DEPRECATE)
	add_definitions(-D_CRT_NONSTDC_NO_DEPRECATE)
	add_definitions(-DNOMINMAX)
endif()

file(GLOB BID_SRCS
		src/thirdparty/IntelRDFPMathLib20U2/LIBRARY/src/*.c
		src/thirdparty/IntelRDFPMathLib20U2/LIBRARY/float128/dpml_ux_bid.c
		src/thirdparty/IntelRDFPMathLib20U2/LIBRARY/float128/dpml_ux_bessel.c
		src/thirdparty/IntelRDFPMathLib20U2/LIBRARY/float128/dpml_ux_cbrt.c
		src/thirdparty/IntelRDFPMathLib20U2/LIBRARY/float128/dpml_ux_erf.c
		src/thirdparty/IntelRDFPMathLib20U2/LIBRARY/float128/dpml_ux_exp.c
		src/thirdparty/IntelRDFPMathLib20U2/LIBRARY/float128/dpml_ux_int.c
		src/thirdparty/IntelRDFPMathLib20U2/LIBRARY/float128/dpml_ux_inv_hyper.c
		src/thirdparty/IntelRDFPMathLib20U2/LIBRARY/float128/dpml_ux_inv_trig.c
		src/thirdparty/IntelRDFPMathLib20U2/LIBRARY/float128/dpml_ux_lgamma.c
		src/thirdparty/IntelRDFPMathLib20U2/LIBRARY/float128/dpml_ux_log.c
		src/thirdparty/IntelRDFPMathLib20U2/LIBRARY/float128/dpml_ux_mod.c
		src/thirdparty/IntelRDFPMathLib20U2/LIBRARY/float128/dpml_ux_powi.c
		src/thirdparty/IntelRDFPMathLib20U2/LIBRARY/float128/dpml_ux_pow.c
		src/thirdparty/IntelRDFPMathLib20U2/LIBRARY/float128/dpml_ux_sqrt.c
		src/thirdparty/IntelRDFPMathLib20U2/LIBRARY/float128/dpml_ux_trig.c
		src/thirdparty/IntelRDFPMathLib20U2/LIBRARY/float128/dpml_ux_ops.c
		src/thirdparty/IntelRDFPMathLib20U2/LIBRARY/float128/dpml_ux_ops_64.c
		src/thirdparty/IntelRDFPMathLib20U2/LIBRARY/float128/dpml_four_over_pi.c
		src/thirdparty/IntelRDFPMathLib20U2/LIBRARY/float128/dpml_exception.c
		src/thirdparty/IntelRDFPMathLib20U2/LIBRARY/float128/sqrt_tab_t.c
		src/NativeImplToString.c)
		
file(GLOB PUBLIC_HEADERS "include/*")

add_library(dfp ${BID_SRCS} src/NativeImpl.c)
include_directories(src/thirdparty/IntelRDFPMathLib20U2/LIBRARY/src)
target_compile_definitions(dfp PRIVATE API_PREFIX=ddfp${VERSION_SUFFIX}_)
if (JAVA_PREFIX)
	target_compile_definitions(dfp PRIVATE JAVA_PREFIX=${JAVA_PREFIX})
endif()
set_target_properties(dfp PROPERTIES OUTPUT_NAME "ddfp${VERSION_SUFFIX}")
if (NOT JAVA_PREFIX)
	set_target_properties(dfp PROPERTIES PUBLIC_HEADER "${PUBLIC_HEADERS}")
endif()
install(TARGETS dfp)

if (JAVA_MATH_PREFIX)
	add_library(dfpmath ${BID_SRCS} src/NativeMathImpl.c)
	include_directories(src/thirdparty/IntelRDFPMathLib20U2/LIBRARY/src)
	target_compile_definitions(dfpmath PRIVATE API_PREFIX=ddfpmath${VERSION_SUFFIX}_)
	if (JAVA_MATH_PREFIX)
		target_compile_definitions(dfpmath PRIVATE JAVA_PREFIX=${JAVA_MATH_PREFIX})
	endif()
	set_target_properties(dfpmath PROPERTIES OUTPUT_NAME "ddfpmath${VERSION_SUFFIX}")
	install(TARGETS dfpmath)
endif()
