package org.smarthata.service;

import lombok.extern.log4j.Log4j2;
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
@Log4j2
public class SensorCleaner {

    private final MeasureCleaner measureCleaner;
    private final SensorRepository sensorRepository;
    private final DeviceRepository deviceRepository;


    public SensorCleaner(MeasureCleaner measureCleaner, SensorRepository sensorRepository, DeviceRepository deviceRepository) {
        this.measureCleaner = measureCleaner;
        this.sensorRepository = sensorRepository;
        this.deviceRepository = deviceRepository;
    }

    @Async
    public void cleanAllDevices() {
        deviceRepository.findAll().forEach(device -> cleanDevice(device.getId()));
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
        log.info("Start sensor clean = {}", sensor);

        LocalDateTime now = LocalDateTime.now().truncatedTo(HOURS);

        int totalRemoved = 0;
        totalRemoved += cleanSensorByStep(sensor, now.minus(1, YEARS), HOURS, 1);
        totalRemoved += cleanSensorByStep(sensor, now.minus(6, MONTHS), MINUTES, 30);
        totalRemoved += cleanSensorByStep(sensor, now.minus(3, MONTHS), MINUTES, 15);
        totalRemoved += cleanSensorByStep(sensor, now.minus(1, MONTHS), MINUTES, 5);
        totalRemoved += cleanSensorByStep(sensor, now.minus(1, WEEKS), MINUTES, 1);

        log.info("Finish sensor clean = {} totalRemoved = {}", sensor, totalRemoved);
    }

    private Long cleanSensorByStep(Sensor sensor, LocalDateTime periodEndDate, ChronoUnit stepUnit, int stepCount) {

        LocalDateTime earliestDate = periodEndDate.minus(2, DAYS);

        Long totalRemoved = 0L;
        do {
            log.debug("Sensor {} date {} / {}  total removed {}", sensor.getName(), periodEndDate.toLocalDate(), earliestDate.toLocalDate(), totalRemoved);
            LocalDateTime periodStartDate = periodEndDate.minus(stepCount, stepUnit);
            totalRemoved += measureCleaner.cleanPeriod(sensor, periodStartDate, periodEndDate);
            periodEndDate = periodStartDate;
        } while (periodEndDate.isAfter(earliestDate));
        log.debug("Sensor {} total removed {}", sensor.getName(), totalRemoved);
        return totalRemoved;

    }

}
