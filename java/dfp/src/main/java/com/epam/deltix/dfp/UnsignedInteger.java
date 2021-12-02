package com.epam.deltix.dfp;

class UnsignedInteger {
    public static int compare(final int x, final int y) {
        return Integer.compare(x + Integer.MIN_VALUE, y + Integer.MIN_VALUE);
    }
}
