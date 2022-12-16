package deltix.dfp;

class UnsignedInteger {
    private UnsignedInteger() {
    }

    @Deprecated(/*"The is(Not)?(Less|Greater)?(Equal)? functions faster than the compare() call."*/)
    public static int compare(final int x, final int y) {
        return Integer.compare(x + Integer.MIN_VALUE, y + Integer.MIN_VALUE);
    }

    public static boolean isLess(final int a, final int b) {
        return a + Integer.MIN_VALUE < b + Integer.MIN_VALUE;
    }

    public static boolean isLessOrEqual(final int a, final int b) {
        return a + Integer.MIN_VALUE <= b + Integer.MIN_VALUE;
    }

    public static boolean isGreater(final int a, final int b) {
        return a + Integer.MIN_VALUE > b + Integer.MIN_VALUE;
    }

    public static boolean isGreaterOrEqual(final int a, final int b) {
        return a + Integer.MIN_VALUE >= b + Integer.MIN_VALUE;
    }
}
