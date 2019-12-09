package com.iot.tsa.enums;

import java.time.temporal.ChronoUnit;

public enum TimeUnit {

    S(ChronoUnit.SECONDS, null, "s", 30L),
    M(ChronoUnit.MINUTES, S, "m", 5L),
    H(ChronoUnit.HOURS, M, "h", 1L),
    D(ChronoUnit.DAYS, H, "d", 1L);

    private final ChronoUnit chronoUnit;
    private final TimeUnit lowerUnit;
    private final String unit;
    private final Long groupByFactor;

    TimeUnit(ChronoUnit chronoUnit, TimeUnit lowerUnit, String unit, Long groupByFactor) {
        this.chronoUnit = chronoUnit;
        this.lowerUnit = lowerUnit;
        this.unit = unit;
        this.groupByFactor = groupByFactor;
    }


    public Long getGroupByFactor() {
        return groupByFactor;
    }

    public ChronoUnit getChronoUnit() {
        return chronoUnit;
    }

    public TimeUnit getLowerUnit() {
        return lowerUnit;
    }

    public String getUnit() {
        return unit;
    }
}
