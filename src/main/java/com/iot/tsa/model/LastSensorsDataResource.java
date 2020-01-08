package com.iot.tsa.model;

import com.iot.tsa.controller.SensorDataController;
import com.iot.tsa.enums.TimeUnit;
import org.springframework.hateoas.CollectionModel;

import java.util.List;

import static com.iot.tsa.util.db.Now.RELATIVE_TIME;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class LastSensorsDataResource extends CollectionModel<SensorData> {

    private final static String[] DEFAULT_AGGREGATE_SELECT_CRITERIA
            = new String[]{"median(*)", "mean(*)", "max(*)", "min(*)"};

    public LastSensorsDataResource(List<SensorData> data,
                                   String tenantId) {

        super(data,
                linkTo(methodOn(SensorDataController.class)
                        .findAllLastValueByTenantId(tenantId))
                        .withSelfRel()
                        .expand()
                        .withType("GET"));
    }

    public LastSensorsDataResource(List<SensorData> data,
                                   String tenantId,
                                   String id) {

        super(data,
                linkTo(methodOn(SensorDataController.class)
                        .findAllLastValueByTenantIdAndId(tenantId, id))
                        .withSelfRel()
                        .expand()
                        .withType("GET"));


        addShortcut("lastMinute", tenantId, id, RELATIVE_TIME.concat("-1m"),
                null, null);
        addShortcut("lastMinutesGroupBy", tenantId, id, RELATIVE_TIME.concat("-5m"),
                DEFAULT_AGGREGATE_SELECT_CRITERIA, TimeUnit.S);
        addShortcut("lastHourGroupBy", tenantId, id, RELATIVE_TIME.concat("-1h"),
                DEFAULT_AGGREGATE_SELECT_CRITERIA, TimeUnit.M);
        addShortcut("lastDayGroupBy", tenantId, id, RELATIVE_TIME.concat("-1d"),
                DEFAULT_AGGREGATE_SELECT_CRITERIA, TimeUnit.H);
        addShortcut("lastMonthGroupBy", tenantId, id, RELATIVE_TIME.concat("-30d"),
                DEFAULT_AGGREGATE_SELECT_CRITERIA, TimeUnit.D);

    }

    private final void addShortcut(String linkName, String tenantId, String id, String from, String[] selectCriteria, TimeUnit timeUnit) {
        this.add(linkTo(methodOn(SensorDataController.class)
                        .findAllByIdAndTenantId(
                                tenantId, id, from, null,
                                selectCriteria,
                                timeUnit != null ? timeUnit.getGroupByFactor() : null, timeUnit)
                )
                        .withRel(linkName)
                        .expand()
                        .withType("GET")

        );
    }
}
