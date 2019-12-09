package com.iot.tsa.controller;

import com.iot.tsa.enums.TimeUnit;
import com.iot.tsa.model.QueryCriteria;
import com.iot.tsa.model.SensorData;
import com.iot.tsa.service.SensorDataService;
import com.iot.tsa.util.TimeFormatter;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.ControllerLinkBuilder;
import org.springframework.web.bind.annotation.*;

import static com.iot.tsa.util.TimeFormatter.toZulu;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/sensorData")
public class SensorDataController {

    private final static String[] AGGREGATE_SELECT_CRITERIA = new String[]{"median(*)", "mean(*)", "max(*)", "min(*)"};

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

        CollectionModel<SensorData> resource = new CollectionModel<>(ret);

        resource.add(linkTo(methodOn(SensorDataController.class)
                .findAllByIdAndTenantId(tenantId, id, from, to, selectCriteria, intervalValue, intervalUnit))
                .withSelfRel()
                .expand()
                .withType("GET"));

        resource.add(addShortcut("lastMinute", tenantId, id, "now()-1m",
                null, null));
        resource.add(addShortcut("lastMinutesGroupBy", tenantId, id, "now()-5m",
                AGGREGATE_SELECT_CRITERIA, TimeUnit.S));
        resource.add(addShortcut("lastHourGroupBy", tenantId, id, "now()-1h",
                AGGREGATE_SELECT_CRITERIA, TimeUnit.M));
        resource.add(addShortcut("lastDayGroupBy", tenantId, id, "now()-1d",
                AGGREGATE_SELECT_CRITERIA, TimeUnit.H));
        resource.add(addShortcut("lastMonthGroupBy", tenantId, id, "now()-30d",
                AGGREGATE_SELECT_CRITERIA, TimeUnit.D));


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

    private final Link addDrillDown(Instant from, String tenantId, String id) {
        return addDrillDown(from, tenantId, id, null, null, null);
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
