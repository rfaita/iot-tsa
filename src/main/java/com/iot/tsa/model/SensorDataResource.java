package com.iot.tsa.model;

import com.iot.tsa.controller.SensorDataController;
import com.iot.tsa.enums.TimeUnit;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import java.time.Instant;

import static com.iot.tsa.util.TimeFormatter.toZulu;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class SensorDataResource extends EntityModel<SensorData> {

    public SensorDataResource(SensorData sensorData,
                              Instant from, String tenantId, String id,
                              String[] selectCriteria,
                              Long intervalValue, TimeUnit intervalUnit) {
        super(sensorData);

        if (intervalValue != null && intervalUnit != null) {
            this.addDrillDown(from, tenantId, id, selectCriteria, intervalValue, intervalUnit);
        }


    }

    private final void addDrillDown(Instant from, String tenantId, String id,
                                    String[] selectCriteria,
                                    Long intervalValue, TimeUnit intervalUnit) {
        this.add(
                linkTo(methodOn(SensorDataController.class)
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
                        .withType("GET")
        );

    }
}
