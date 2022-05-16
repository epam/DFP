package com.epam.deltix.dfp;

class UnsignedLong {
    private UnsignedLong() {
    }

    @Deprecated(/*"The is(Not)?(Less|Greater)?(Equal)? functions faster than the compare() call."*/)
    public static int compare(final long x, final long y) {
        return Long.compare(x + Long.MIN_VALUE, y + Long.MIN_VALUE);
    }

    public static boolean isLess(final long a, final long b) {
        return a + Long.MIN_VALUE < b + Long.MIN_VALUE;
    }

    public static boolean isLessOrEqual(final long a, final long b) {
        return a + Long.MIN_VALUE <= b + Long.MIN_VALUE;
    }

    public static boolean isGreater(final long a, final long b) {
        return a + Long.MIN_VALUE > b + Long.MIN_VALUE;
    }

    public static boolean isGreaterOrEqual(final long a, final long b) {
        return a + Long.MIN_VALUE >= b + Long.MIN_VALUE;
    }

//    /**
//     * The copy-paste of the Guava (Apache-2.0 License )
//     * @param dividend The value to be divided.
//     * @param divisor  The value doing the dividing.
//     * @return The unsigned quotient of the first argument divided by the second argument
//     */
//    public static long guavaDivide(long dividend, long divisor) {
//        if (divisor < 0) { // i.e., divisor >= 2^63:
//            if (compare(dividend, divisor) < 0) {
//                return 0; // dividend < divisor
//            } else {
//                return 1; // dividend >= divisor
//            }
//        }
//
//        // Optimization - use signed division if dividend < 2^63
//        if (dividend >= 0) {
//            return dividend / divisor;
//        }
//
//        /*
//         * Otherwise, approximate the quotient, check, and correct if necessary. Our approximation is
//         * guaranteed to be either exact or one less than the correct value. This follows from fact that
//         * floor(floor(x)/i) == floor(x/i) for any real x and integer i != 0. The proof is not quite
//         * trivial.
//         */
//        long quotient = ((dividend >>> 1) / divisor) << 1;
//        long rem = dividend - quotient * divisor;
//        return quotient + (compare(rem, divisor) >= 0 ? 1 : 0);
//    }

    /**
     * The copy-paste of the OpenJDK Long.divideUnsigned
     *
     * @param dividend The value to be divided.
     * @param divisor  The value doing the dividing.
     * @return The unsigned quotient of the first argument divided by the second argument
     */
    public static long divide(final long dividend, final long divisor) {
        /* See Hacker's Delight (2nd ed), section 9.3 */
        if (divisor >= 0) {
            final long q = (dividend >>> 1) / divisor << 1;
            final long r = dividend - q * divisor;
            return q + ((r | ~(r - divisor)) >>> (Long.SIZE - 1));
        }
        return (dividend & ~(dividend - divisor)) >>> (Long.SIZE - 1);
    }

//    /**
//     * The copy-paste of the Guava (Apache-2.0 License )
//     * com.google.common.primitives.UnsignedLong.fromLongBits().doubleValue();
//     *
//     * @param value Unsigned long value
//     * @return double value
//     */
//    public static double guavaDoubleValue(final long value) {
//        if (value >= 0) {
//            return (double) value;
//        }
//        // The top bit is set, which means that the double value is going to come from the top 53 bits.
//        // So we can ignore the bottom 11, except for rounding. We can unsigned-shift right 1, aka
//        // unsigned-divide by 2, and convert that. Then we'll get exactly half of the desired double
//        // value. But in the specific case where the bottom two bits of the original number are 01, we
//        // want to replace that with 1 in the shifted value for correct rounding.
//        return (double) ((value >>> 1) | (value & 1)) * 2.0;
//    }

    /**
     * The copy-paste of the jOOU (Apache-2.0 License)
     * org.joou.ULong.doubleValue()
     *
     * @param value Unsigned long value
     * @return double value
     */
    public static double doubleValue(final long value) {
        return value >= 0 ? value : ((double) (value & Long.MAX_VALUE)) + Long.MAX_VALUE;
    }

    public static long longToDoubleRawBits(final long x) {
        return Double.doubleToRawLongBits(doubleValue(x));
    }

    /**
     * The copy-paste of the jOOU (Apache-2.0 License)
     * org.joou.ULong.doubleValue()
     *
     * @param value Unsigned long value
     * @return double value
     */
    public static float floatValue(final long value) {
        return value >= 0 ? value : ((float) (value & Long.MAX_VALUE)) + Long.MAX_VALUE;
    }

    public static long fromDoubleSafe(final double x) {
        if (x < 0 || !DoubleIsFinite(x) || x > uLongMaxDouble)
            throw new IllegalArgumentException("The x(=" + x + ") must be non-negative finite value in acceptable range.");
        return fromDouble(x);
    }

    public static long fromDouble(final double x) {
        if (x <= Long.MAX_VALUE)
            return (long) x;
        return (long) (x - Long.MAX_VALUE) | Long.MIN_VALUE;
    }

    private static final double uLongMaxDouble = 18446744073709551615.0;

    private static boolean DoubleIsFinite(final double d) { // 1.7 support
        return Math.abs(d) <= Double.MAX_VALUE;
    }

    public static long parse(String s, int radix)
        throws NumberFormatException {
        if (s == null) {
            throw new NumberFormatException("null");
        }

        int len = s.length();
        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar == '-') {
                throw new
                    NumberFormatException(String.format("Illegal leading minus sign " +
                    "on unsigned string %s.", s));
            } else {
                if (len <= 12 || // Long.MAX_VALUE in Character.MAX_RADIX is 13 digits
                    (radix == 10 && len <= 18)) { // Long.MAX_VALUE in base 10 is 19 digits
                    return Long.parseLong(s, radix);
                }

                // No need for range checks on len due to testing above.
                long first = Long.parseLong(s.substring(0, len - 1), radix);
                int second = Character.digit(s.charAt(len - 1), radix);
                if (second < 0) {
                    throw new NumberFormatException("Bad digit at end of " + s);
                }
                long result = first * radix + second;
                if (isLess(result, first)) {
                    /*
                     * The maximum unsigned value, (2^64)-1, takes at
                     * most one more digit to represent than the
                     * maximum signed value, (2^63)-1.  Therefore,
                     * parsing (len - 1) digits will be appropriately
                     * in-range of the signed parsing.  In other
                     * words, if parsing (len -1) digits overflows
                     * signed parsing, parsing len digits will
                     * certainly overflow unsigned parsing.
                     *
                     * The compareUnsigned check above catches
                     * situations where an unsigned overflow occurs
                     * incorporating the contribution of the final
                     * digit.
                     */
                    throw new NumberFormatException(String.format("String value %s exceeds " +
                        "range of unsigned long.", s));
                }
                return result;
            }
        } else {
            throw new NumberFormatException("For input string: \"" + s + "\"");
        }
    }
}
