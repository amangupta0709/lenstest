package com.db.lenstest.domain;

import java.util.Arrays;

public enum TestStatus {
    UNKNOWN,
    PASSED,
    UNDEFINED,
    SKIPPED,
    FAILED;


    public static TestStatus parseStatus(final String statusString) {
        return Arrays.stream(values())
                .filter(x -> x.name().equalsIgnoreCase(statusString))
                .findAny()
                .orElse(UNKNOWN);
    }

    public static TestStatus computePriority(TestStatus a, TestStatus b) {
        return a.ordinal() > b.ordinal() ? a : b;
    }
}