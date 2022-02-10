package com.epam.deltix.dfp;

import java.lang.annotation.*;

/**
 * Annotation used to mark class fields, local variables, arguments and return values of methods that the {@code long }
 * value must be treated as dfp floating-point value in accordance to IEEE754-2008.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Decimal {
}
