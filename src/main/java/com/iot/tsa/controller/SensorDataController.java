package com.iot.tsa.controller;

import com.iot.tsa.enums.TimeUnit;
import com.iot.tsa.model.LastSensorsDataResource;
import com.iot.tsa.model.SensorData;
import com.iot.tsa.model.SensorDataResource;
import com.iot.tsa.model.SensorsDataResource;
import com.iot.tsa.service.SensorDataService;
import com.iot.tsa.util.db.QueryCriteria;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/timeseries")
public class SensorDataController {


    private static final String X_TENANT_ID = "X-TenantId";
    private final SensorDataService service;

    public SensorDataController(SensorDataService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public SensorsDataResource findAllByIdAndTenantId(@RequestHeader(X_TENANT_ID) String tenantId,
                                                      @PathVariable String id,
                                                      @RequestParam(required = false) String from,
                                                      @RequestParam(required = false) String to,
                                                      @RequestParam(required = false) String[] selectCriteria,
                                                      @RequestParam(required = false) Long intervalValue,
                                                      @RequestParam(required = false) TimeUnit intervalUnit) {

        QueryCriteria criteria = new QueryCriteria.Builder()
                .id(id)
                .tenantId(tenantId)
                .selectCriteria(selectCriteria)
                .intervalValue(intervalValue)
                .intervalUnit(intervalUnit)
                .from(from)
                .to(to)
                .build();

        List<SensorData> data = service.findAllByCriteria(criteria);

        List<SensorDataResource> ret =
                data.stream()
                        .map(sensorData ->
                                new SensorDataResource(
                                        sensorData,
                                        sensorData.getTime(),
                                        tenantId, id, selectCriteria,
                                        intervalValue, intervalUnit
                                )
                        )
                        .collect(Collectors.toList());

        return new SensorsDataResource(ret, tenantId, id, from, to, selectCriteria, intervalValue, intervalUnit);

    }

    @GetMapping("/lastvalues")
    public LastSensorsDataResource findAllLastValueByTenantId(@RequestHeader(X_TENANT_ID) String tenantId) {

        return new LastSensorsDataResource(service.findAllLastValueByTenantId(tenantId), tenantId);


    }

    @GetMapping("/lastvalue/{id}")
    public LastSensorsDataResource findAllLastValueByTenantIdAndId(
            @RequestHeader(X_TENANT_ID) String tenantId,
            @PathVariable String id) {

        return new LastSensorsDataResource(
                service.findAllLastValueByTenantIdAndId(tenantId, id),
                tenantId, id);


    }

}
