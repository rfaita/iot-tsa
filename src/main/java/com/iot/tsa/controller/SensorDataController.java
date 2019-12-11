package com.iot.tsa.controller;

import com.iot.tsa.enums.TimeUnit;
import com.iot.tsa.util.db.Now;
import com.iot.tsa.util.db.QueryCriteria;
import com.iot.tsa.model.SensorData;
import com.iot.tsa.service.SensorDataService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.web.bind.annotation.*;

import static com.iot.tsa.util.TimeFormatter.toZulu;
import static com.iot.tsa.util.db.Now.RELATIVE_TIME;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/sensorData")
public class SensorDataController {

    private final static String[] DEFAULT_AGGREGATE_SELECT_CRITERIA
            = new String[]{"median(*)", "mean(*)", "max(*)", "min(*)"};

    private final SensorDataService service;

    public SensorDataController(SensorDataService service) {
        this.service = service;
    }

    @GetMapping("/{tenantId}/{id}")
    public CollectionModel<SensorData> findAllByIdAndTenantId(@PathVariable String tenantId,
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

        List<SensorData> ret = service.findAllByCriteria(criteria);

        if (intervalValue != null && intervalUnit != null) {
            ret.stream()
                    .forEach(sensorData ->
                            sensorData.add(addDrillDown(sensorData.getTime(),
                                    tenantId, id, selectCriteria,
                                    intervalValue, intervalUnit)));
        }

        return createResource(tenantId, id, from, to, selectCriteria, intervalValue, intervalUnit, ret);

    }


    private final CollectionModel<SensorData> createResource(String tenantId,
                                                             String id,
                                                             String from,
                                                             String to,
                                                             String[] selectCriteria,
                                                             Long intervalValue,
                                                             TimeUnit intervalUnit,
                                                             List<SensorData> data) {
        CollectionModel<SensorData> resource = new CollectionModel<>(data);

        resource.add(linkTo(methodOn(SensorDataController.class)
                .findAllByIdAndTenantId(tenantId, id, from, to, selectCriteria, intervalValue, intervalUnit))
                .withSelfRel()
                .expand()
                .withType("GET"));

        resource.add(addShortcut("lastMinute", tenantId, id, RELATIVE_TIME.concat("-1m"),
                null, null));
        resource.add(addShortcut("lastMinutesGroupBy", tenantId, id, RELATIVE_TIME.concat("-5m"),
                DEFAULT_AGGREGATE_SELECT_CRITERIA, TimeUnit.S));
        resource.add(addShortcut("lastHourGroupBy", tenantId, id, RELATIVE_TIME.concat("-1h"),
                DEFAULT_AGGREGATE_SELECT_CRITERIA, TimeUnit.M));
        resource.add(addShortcut("lastDayGroupBy", tenantId, id, RELATIVE_TIME.concat("-1d"),
                DEFAULT_AGGREGATE_SELECT_CRITERIA, TimeUnit.H));
        resource.add(addShortcut("lastMonthGroupBy", tenantId, id, RELATIVE_TIME.concat("-30d"),
                DEFAULT_AGGREGATE_SELECT_CRITERIA, TimeUnit.D));


        return resource;
    }

    private final Link addShortcut(String linkName, String tenantId, String id, String from, String[] selectCriteria, TimeUnit timeUnit) {
        return linkTo(methodOn(SensorDataController.class)
                .findAllByIdAndTenantId(
                        tenantId, id, from, null,
                        selectCriteria,
                        timeUnit != null ? timeUnit.getGroupByFactor() : null, timeUnit)
        )
                .withRel(linkName)
                .expand()
                .withType("GET");
    }

    private final Link addDrillDown(Instant from, String tenantId, String id,
                                    String[] selectCriteria,
                                    Long intervalValue, TimeUnit intervalUnit) {
        return linkTo(methodOn(SensorDataController.class)
                .findAllByIdAndTenantId(tenantId, id,
                        toZulu(from),
                        toZulu(from.plus(intervalValue, intervalUnit.getChronoUnit())),
                        intervalUnit.getLowerUnit() != null ? selectCriteria : null,
                        intervalUnit.getLowerUnit() != null ?
                                intervalUnit.getLowerUnit().getGroupByFactor() : null,
                        intervalUnit.getLowerUnit()
                )
        )
                .withRel("drillDown")
                .expand()
                .withType("GET");

    }
}
