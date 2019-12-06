package com.iot.tsa.controller;

import com.iot.tsa.enums.TimeUnit;
import com.iot.tsa.model.QueryCriteria;
import com.iot.tsa.model.SensorData;
import com.iot.tsa.service.SensorDataService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sensorData")
public class SensorDataController {

    private final SensorDataService service;

    public SensorDataController(SensorDataService service) {
        this.service = service;
    }

    @GetMapping("/{tenantId}/{id}")
    public List<SensorData> findAllByIdAndTenantId(@PathVariable String tenantId,
                                                   @PathVariable String id,
                                                   @RequestParam(required = false) String initialTime,
                                                   @RequestParam(required = false) String finalTime,
                                                   @RequestParam(required = false) String[] selectCriteria,
                                                   @RequestParam(required = false) Long timeGroupByCriteria,
                                                   @RequestParam(required = false) TimeUnit timeGroupByUnit) {

        QueryCriteria criteria = new QueryCriteria.Builder()
                .id(id)
                .tenantId(tenantId)
                .selectCriteria(selectCriteria)
                .timeGroupByCriteria(timeGroupByCriteria)
                .timeGroupByUnit(timeGroupByUnit)
                .initialTime(initialTime)
                .finalTime(finalTime)
                .build();

        return service.findAllByCriteria(criteria);

    }
}
