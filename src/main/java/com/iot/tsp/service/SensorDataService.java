package com.iot.tsp.service;

import com.iot.tsp.model.SensorData;
import com.iot.tsp.repository.SensorDataRepository;
import org.springframework.stereotype.Service;

@Service
public class SensorDataService {

    private final SensorDataRepository repository;

    public SensorDataService(SensorDataRepository repository) {
        this.repository = repository;
    }

    public void write(SensorData sensorData) {
        this.repository.write(sensorData);

    }


}
