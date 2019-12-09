package com.iot.tsa.repository;

import com.iot.tsa.model.SensorData;
import com.iot.tsa.model.QueryCriteria;
import com.iot.tsa.util.CustomInfluxDBResultMapper;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.data.influxdb.InfluxDBTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SensorDataRepository {

    private final InfluxDBTemplate<Point> influxDBTemplate;


    public SensorDataRepository(InfluxDBTemplate<Point> influxDBTemplate) {
        this.influxDBTemplate = influxDBTemplate;
    }


    public List<SensorData> query(Query query) {

        QueryResult queryResult = influxDBTemplate.query(query);

        return parse(queryResult);

    }

    private List<SensorData> parse(QueryResult queryResult) {

        CustomInfluxDBResultMapper resultMapper = new CustomInfluxDBResultMapper();

        return resultMapper.toPOJO(queryResult, SensorData.class);
    }

    public List<SensorData> findAllByCriteria(QueryCriteria criteria) {

        QueryCriteria newCriteria = new QueryCriteria.Builder()
                .database(influxDBTemplate.getDatabase())
                .table("sensorData")
                .id(criteria.getId())
                .tenantId(criteria.getTenantId())
                .selectCriteria(criteria.getSelectCriteria())
                .intervalValue(criteria.getIntervalValue())
                .to(criteria.getTo())
                .from(criteria.getFrom())
                .intervalUnit(criteria.getIntervalUnit())
                .build();
        return query(newCriteria.toQuery());
    }
}
