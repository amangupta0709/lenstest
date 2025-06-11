package com.db.lenstest.domain;

public enum TestLevel {
    FEATURE("Feature"),
    SCENARIO("Scenario"),
    STEP("Step");

    private final String _type;

    TestLevel(final String type) {
        _type = type;
    }

    public String getType() {
        return _type;
    }
}

