package com.db.lenstest.domain;

import java.util.Arrays;

public enum TestStatus {
    UNKNOWN("UNKNOWN", -1),
    PASSED("PASSED", 0),
    UNDEFINED("UNDEFINED", 10),
    SKIPPED("SKIPPED", 20),
    FAILED("FAILED", 30);

    private final String _result;
    private final int _priority;

    TestStatus(final String result, final int priority) {
        _result = result;
        _priority = priority;
    }

    public String getResult() {
        return _result;
    }

    public int getPriority() {
        return _priority;
    }

    public static TestStatus parseStatus(final String statusString) {
        return Arrays.stream(values())
                .filter(x -> x.getResult().equalsIgnoreCase(statusString))
                .findAny()
                .orElse(UNKNOWN);
    }

    public static TestStatus computePriority(final TestStatus a, final TestStatus b) {
        return a.getPriority() > b.getPriority() ? a : b;
    }

    public static TestStatus computePriority(final String a, final String b) {
        final TestStatus ar = parseStatus(a);
        final TestStatus br = parseStatus(b);
        return computePriority(ar, br);
    }
}