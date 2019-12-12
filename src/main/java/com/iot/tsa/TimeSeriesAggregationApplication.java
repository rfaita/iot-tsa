package com.iot.tsa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.ForwardedHeaderFilter;

import javax.servlet.Filter;

@SpringBootApplication
public class TimeSeriesAggregationApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimeSeriesAggregationApplication.class, args);
    }

    @Bean
    public Filter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

}
