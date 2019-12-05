package com.iot.tsp.repository;

import com.iot.tsp.model.SensorData;
import org.influxdb.dto.Point;
import org.springframework.data.influxdb.InfluxDBTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class SensorDataRepository {

    private final InfluxDBTemplate<Point> influxDBTemplate;


    public SensorDataRepository(InfluxDBTemplate<Point> influxDBTemplate) {
        this.influxDBTemplate = influxDBTemplate;
    }

    public void write(SensorData sensorData) {
        Point point = Point
                .measurement("sensorData")
                .time(sensorData.getTimestamp(), TimeUnit.MILLISECONDS)
                .tag("id", sensorData.getId())
                .tag("tenantId", sensorData.getTenantId())
                .fields(sensorData.getExtraFields())
                .build();
        influxDBTemplate.write(point);

    }

//    public List<SensorDataPoint> findAllByIdAndTenantId(String id, String tenantId) {
//
//        Query query = BoundParameterQuery.QueryBuilder
//                .newQuery("SELECT * FROM sensorData WHERE id = $id AND tenantId = $tenantId")
//                .forDatabase(influxDBTemplate.getDatabase())
//                .bind("id", id)
//                .bind("tenantId", tenantId)
//                .create();
//
//        return query(query);
//
//    }
//
//    public List<SensorDataPoint> query(Query query) {
//
//        QueryResult queryResult = influxDBTemplate.query(query);
//
//        queryResult.getResults().stream()
//                .map(result -> result.getSeries().get(0))
//                .map(serie -> serie.get)
//
//        return parse(queryResult);
//
//    }
//
//    private List<SensorDataPoint> parse(QueryResult queryResult) {
//
//        return resultMapper.toPOJO(queryResult, SensorDataPoint.class);
//    }

}
