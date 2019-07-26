package org.smarthata.service;

import org.smarthata.model.Measure;
import org.smarthata.model.Sensor;
import org.smarthata.repository.MeasureRepository;
import org.smarthata.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class WeatherService {

    private static final int STREET_TEMP_SENSOR_ID = 13;

    private final SensorRepository sensorRepository;
    private final MeasureRepository measureRepository;

    @Autowired
    public WeatherService(SensorRepository sensorRepository, MeasureRepository measureRepository) {
        this.sensorRepository = sensorRepository;
        this.measureRepository = measureRepository;
    }

    public double calcAverageDailyStreetTemperature() {

        Sensor sensor = sensorRepository.findByIdOrElseThrow(STREET_TEMP_SENSOR_ID);

        return round(measureRepository.findBySensorAndDateAfter(sensor, aDayAgo()).stream()
                .collect(Collectors.averagingDouble(Measure::getValue)));
    }

    private double round(double number) {
        return Math.floor(number * 10) / 10;
    }

    private Date aDayAgo() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        return Date.from(yesterday.atZone(ZoneId.systemDefault()).toInstant());
    }

}
