package com.iot.tsa.service;

import com.iot.tsa.util.db.QueryCriteria;
import com.iot.tsa.model.SensorData;
import com.iot.tsa.repository.SensorDataRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SensorDataService {

    private static final String[] SELECT_CRITERIA_LAST_VALUE = {"last(*)"};
    private static final String[] GROUP_BY_CRITERIA_TENANT_ID_ID = {"tenantId", "id"};
    private final SensorDataRepository repository;

    public SensorDataService(SensorDataRepository repository) {
        this.repository = repository;
    }

    public List<SensorData> findAllByCriteria(QueryCriteria criteria) {
        return repository.findAllByCriteria(criteria);
    }

    public List<SensorData> findAllLastValueByTenantId(String tenantId) {
        QueryCriteria criteria = new QueryCriteria.Builder()
                .tenantId(tenantId)
                .selectCriteria(SELECT_CRITERIA_LAST_VALUE)
                .groupByCriteria(GROUP_BY_CRITERIA_TENANT_ID_ID)
                .build();

        return repository.findAllByCriteria(criteria);


    }

    public List<SensorData> findAllLastValueByTenantIdAndId(String tenantId, String id) {
        QueryCriteria criteria = new QueryCriteria.Builder()
                .tenantId(tenantId)
                .id(id)
                .selectCriteria(SELECT_CRITERIA_LAST_VALUE)
                .groupByCriteria(GROUP_BY_CRITERIA_TENANT_ID_ID)
                .build();

        return repository.findAllByCriteria(criteria);
    }
}
