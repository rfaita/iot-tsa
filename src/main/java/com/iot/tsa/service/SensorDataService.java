package com.iot.tsa.service;

import com.iot.tsa.util.db.QueryCriteria;
import com.iot.tsa.model.SensorData;
import com.iot.tsa.repository.SensorDataRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SensorDataService {

    private final SensorDataRepository repository;

    public SensorDataService(SensorDataRepository repository) {
        this.repository = repository;
    }

    public List<SensorData> findAllByCriteria(QueryCriteria criteria) {
        return repository.findAllByCriteria(criteria);
    }
}
