package com.iot.tsa.enums;

public enum TimeUnit {

    S("s"), M("m"), H("h"), D("d");

    private final String unit;

    TimeUnit(String unit) {
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }
}
