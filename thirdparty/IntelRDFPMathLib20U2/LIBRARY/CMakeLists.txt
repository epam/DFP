set(NAME bid)

file(GLOB ${NAME}_SRCS
		src/*.c
		float128/dpml_ux_bid.c
		float128/dpml_ux_bessel.c
		float128/dpml_ux_cbrt.c
		float128/dpml_ux_erf.c
		float128/dpml_ux_exp.c
		float128/dpml_ux_int.c
		float128/dpml_ux_inv_hyper.c
		float128/dpml_ux_inv_trig.c
		float128/dpml_ux_lgamma.c
		float128/dpml_ux_log.c
		float128/dpml_ux_mod.c
		float128/dpml_ux_powi.c
		float128/dpml_ux_pow.c
		float128/dpml_ux_sqrt.c
		float128/dpml_ux_trig.c
		float128/dpml_ux_ops.c
		float128/dpml_ux_ops_64.c
		float128/dpml_four_over_pi.c
		float128/dpml_exception.c
		float128/sqrt_tab_t.c
		../../../native/NativeImplToString.cpp)

add_library(${NAME} STATIC ${${NAME}_SRCS})

# Make sure the compiler can find include files for our library
# when other libraries or executables link to it
target_include_directories (${NAME} PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/src)
