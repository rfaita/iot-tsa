package com.iot.tsa.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.iot.tsa.util.UnmappedFields;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Measurement(name = "sensorData")
public class SensorData {

    @Column( name = "time")
    private Instant time;
    @UnmappedFields
    private Map<String, Object> extraFields;

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    @JsonAnyGetter
    public Map<String, Object> getExtraFields() {
        return extraFields;
    }

    public void setExtraFields(Map<String, Object> extraFields) {
        this.extraFields = extraFields;
    }

    @JsonAnySetter
    public void setExtraFields(String key, Object value) {
        this.extraFields.put(key, value);
    }

    @Override
    public String toString() {
        return "SensorData{" +
                "time=" + time +
                ", extraFields=" + extraFields +
                '}';
    }
}
