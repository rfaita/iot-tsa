package com.iot.tsp.consumer;

import com.iot.tsp.service.SensorDataService;
import com.iot.tsp.model.SensorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.stereotype.Component;

@Component
public class SensorDataConsumer {

    private final static Logger LOGGER = LoggerFactory.getLogger(SensorDataConsumer.class.getName());
    private final SensorDataService sensorService;

    public SensorDataConsumer(SensorDataService sensorService) {
        this.sensorService = sensorService;
    }

    @StreamListener(Sink.INPUT)
    public void handle(SensorData sensorData) {
        LOGGER.info("Sensor Data : {}", sensorData);

        this.sensorService.write(sensorData);
    }

}
