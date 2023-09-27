package org.smarthata.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smarthata.model.Measure;
import org.smarthata.model.Sensor;
import org.smarthata.repository.MeasureRepository;
import org.smarthata.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class WeatherService {

    private static final int STREET_TEMP_SENSOR_ID = 13;
    private static final int STREET_AVG_TEMP_SENSOR_ID = 14;

    private final SensorRepository sensorRepository;
    private final MeasureRepository measureRepository;

    private final RestTemplate restTemplate;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${narodmon.mac}")
    private String mac;

    @Autowired
    public WeatherService(SensorRepository sensorRepository,
                          MeasureRepository measureRepository,
                          RestTemplateBuilder restTemplateBuilder) {
        this.sensorRepository = sensorRepository;
        this.measureRepository = measureRepository;
        this.restTemplate = restTemplateBuilder.build();
    }

    public double calcAverageDailyStreetTemperature() {

        Sensor streetSensor = sensorRepository.findByIdOrElseThrow(STREET_TEMP_SENSOR_ID);

        double dailyAverage = round(measureRepository.findBySensorAndDateAfter(streetSensor, aDayAgo()).stream()
                .collect(Collectors.averagingDouble( it -> it.value)));

        Sensor dailyAverageSensor = sensorRepository.findByIdOrElseThrow(STREET_AVG_TEMP_SENSOR_ID);
        Measure measure = new Measure(dailyAverageSensor, dailyAverage, new Date());
        measureRepository.save(measure);

        return dailyAverage;
    }

    public void sendDataToNarodmon() {

        Sensor streetSensor = sensorRepository.findByIdOrElseThrow(STREET_TEMP_SENSOR_ID);

        Measure lastMeasure = measureRepository.findTopBySensorOrderByDateDesc(streetSensor);

        long aLittleBitAgo = new Date().getTime() - TimeUnit.MINUTES.toMillis(2);
        if (lastMeasure.date.before(new Date(aLittleBitAgo))) {
            logger.error("Does not have time to publish into narodmon");
            return;
        }

        Double temp = lastMeasure.value;
        try {
            String url = String.format("http://narodmon.ru/get?ID=%s&street=%s", mac, round(temp));

            String result = this.restTemplate.getForObject(url, String.class);

            logger.info("Temp {} sent to narodmon.ru result: {}", temp, result);
        } catch (RestClientException e) {
            logger.error("Failed to send street temp {} to narodmon.ru", temp, e);
        }

    }

    private double round(double number) {
        return Math.floor(number * 10) / 10;
    }

    private Date aDayAgo() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        return Date.from(yesterday.atZone(ZoneId.systemDefault()).toInstant());
    }
}
