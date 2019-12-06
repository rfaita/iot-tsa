package com.iot.tsa.repository;

import com.iot.tsa.model.SensorData;
import com.iot.tsa.model.QueryCriteria;
import com.iot.tsa.util.CustomInfluxDBResultMapper;
import org.influxdb.dto.BoundParameterQuery;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.data.influxdb.InfluxDBTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

import static com.iot.tsa.util.TimeFormatter.toZulu;
import static org.influxdb.querybuilder.BuiltQuery.QueryBuilder.*;

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
                .timeGroupByCriteria(criteria.getTimeGroupByCriteria())
                .finalTime(criteria.getFinalTime())
                .initialTime(criteria.getInitialTime())
                .timeGroupByUnit(criteria.getTimeGroupByUnit())
                .build();
        return query(newCriteria.toQuery());
    }
}
