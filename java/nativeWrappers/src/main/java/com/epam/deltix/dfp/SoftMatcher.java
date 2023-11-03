package com.epam.deltix.dfp;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoftMatcher implements MatchResult {
    final Matcher matcher;
    final int groupCount;

    public SoftMatcher(final Pattern pattern, final CharSequence input) {
        this(pattern.matcher(input));
    }

    public SoftMatcher(final Matcher matcher) {
        this.matcher = matcher;
        this.groupCount = matcher.groupCount();
        matcher.find();
    }

    @Override
    public int start() {
        return matcher.start();
    }

    @Override
    public int start(int group) {
        return matcher.start(group);
    }

    @Override
    public int end() {
        return matcher.end();
    }

    @Override
    public int end(int group) {
        return matcher.end(group);
    }

    @Override
    public String group() {
        return matcher.group();
    }

    public String group(final int group) {
        final String ret = matcher.group(group);
        return ret != null ? ret : "";
    }

    @Override
    public int groupCount() {
        return matcher.groupCount();
    }

    public boolean matches() {
        return matcher.matches();
    }
}
