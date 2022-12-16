package deltix.dfp;

abstract class TextUtils {
    public static boolean equalsIgnoringCase(final CharSequence s1, final int s1si, final int s1ei,
                                             final CharSequence s2) {
        if (s1ei < s1si)
            throw new IllegalArgumentException("End index is expected to be greater or equal to start index.");
        if (s2.length() != s1ei - s1si)
            return false;
        for (int i2 = 0, i1 = s1si; i2 < s1.length() && i1 < s1ei; i2 += 1, i1 += 1)
            if (Character.toLowerCase(s1.charAt(i1)) != Character.toLowerCase(s2.charAt(i2)))
                return false;
        return true;
    }
}
