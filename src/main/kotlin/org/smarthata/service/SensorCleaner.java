package org.smarthata.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smarthata.model.Device;
import org.smarthata.model.Sensor;
import org.smarthata.repository.DeviceRepository;
import org.smarthata.repository.SensorRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static java.time.temporal.ChronoUnit.*;

@Service
public class SensorCleaner {

    private final MeasureCleaner measureCleaner;
    private final SensorRepository sensorRepository;
    private final DeviceRepository deviceRepository;


    public SensorCleaner(MeasureCleaner measureCleaner, SensorRepository sensorRepository, DeviceRepository deviceRepository) {
        this.measureCleaner = measureCleaner;
        this.sensorRepository = sensorRepository;
        this.deviceRepository = deviceRepository;
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Async
    public void cleanAllDevices() {
        deviceRepository.findAll().forEach(device -> cleanDevice(device.id));
    }

    @Async
    public void cleanDevice(Integer deviceId) {
        Device device = deviceRepository.findByIdOrElseThrow(deviceId);

        for (Sensor sensor : sensorRepository.findByDevice(device)) {
            cleanSensor(sensor);
        }
    }

    @Async
    public void cleanSensor(Integer sensorId) {
        cleanSensor(sensorRepository.findByIdOrElseThrow(sensorId));
    }


    private void cleanSensor(Sensor sensor) {
        logger.info("Start sensor clean = {}", sensor);

        LocalDateTime now = LocalDateTime.now().truncatedTo(HOURS);

        int totalRemoved = 0;
        totalRemoved += cleanSensorByStep(sensor, now.minus(1, YEARS), HOURS, 1);
        totalRemoved += cleanSensorByStep(sensor, now.minus(6, MONTHS), MINUTES, 30);
        totalRemoved += cleanSensorByStep(sensor, now.minus(3, MONTHS), MINUTES, 15);
        totalRemoved += cleanSensorByStep(sensor, now.minus(1, MONTHS), MINUTES, 5);
        totalRemoved += cleanSensorByStep(sensor, now.minus(1, WEEKS), MINUTES, 1);

        logger.info("Finish sensor clean = {} totalRemoved = {}", sensor, totalRemoved);
    }

    private Long cleanSensorByStep(Sensor sensor, LocalDateTime periodEndDate, ChronoUnit stepUnit, int stepCount) {

        LocalDateTime earliestDate = periodEndDate.minus(2, DAYS);

        Long totalRemoved = 0L;
        do {
            logger.debug("Sensor {} date {} / {}  total removed {}", sensor.name, periodEndDate.toLocalDate(), earliestDate.toLocalDate(), totalRemoved);
            LocalDateTime periodStartDate = periodEndDate.minus(stepCount, stepUnit);
            totalRemoved += measureCleaner.cleanPeriod(sensor, periodStartDate, periodEndDate);
            periodEndDate = periodStartDate;
        } while (periodEndDate.isAfter(earliestDate));
        logger.debug("Sensor {} total removed {}", sensor.name, totalRemoved);
        return totalRemoved;

    }

}
