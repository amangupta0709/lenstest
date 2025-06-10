package com.db.lenstest.domain;

public enum TestType {

    FEATURE("Feature"),
    SCENARIO("Scenario"),
    STEP("Step");

    private final String _type;

    TestType(final String type) {
        _type = type;
    }

    public String getType() {
        return _type;
    }

}
