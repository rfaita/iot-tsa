package com.iot.tsp.command;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.influxdb.InfluxDBTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializerCommand implements CommandLineRunner {

    private final InfluxDBTemplate influxDBTemplate;

    public DatabaseInitializerCommand(InfluxDBTemplate influxDBTemplate) {
        this.influxDBTemplate = influxDBTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        influxDBTemplate.createDatabase();
    }
}
