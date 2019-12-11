package com.iot.tsa.util.db;

import org.influxdb.querybuilder.Appendable;
import org.springframework.util.StringUtils;

public class Now implements Appendable {

    private final String operation;

    public final static String RELATIVE_TIME = "now()";

    public Now(String operation) {
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }

    @Override
    public void appendTo(StringBuilder sb) {
        if (!StringUtils.isEmpty(operation)) {
            sb.append(operation);
        }
    }

    public static final Boolean isRelativeTime(String time) {
        return time.contains(RELATIVE_TIME);
    }
}
