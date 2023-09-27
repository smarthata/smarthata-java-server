package org.smarthata.service;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.smarthata.model.Measure;
import org.smarthata.model.Sensor;
import org.smarthata.repository.MeasureRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@Log4j2
public class MeasureCleaner {

    private final MeasureRepository measureRepository;

    public MeasureCleaner(MeasureRepository measureRepository) {
        this.measureRepository = measureRepository;
    }

    @Transactional
    public Long cleanPeriod(Sensor sensor, LocalDateTime start, LocalDateTime end) {
        log.debug("Period {} - {}", start, end);

        Date startDate = convertToDateViaInstant(start);
        Date endDate = convertToDateViaInstant(end);

        Long count = measureRepository.countBySensorAndDateBetween(sensor, startDate, endDate);

        if (count <= 1) {
            log.debug("Skip due to small count {}", count);
            return 0L;
        }

        long mid = (endDate.getTime() - startDate.getTime()) / 2;
        Date mediumDate = new Date(startDate.getTime() + mid);

        Double avg = measureRepository.avgValueBySensorAndDateBetween(sensor.id, startDate, endDate);

        Measure measure = new Measure(sensor, avg, mediumDate);
        log.debug("count = {} values avg = {}  medDate = {}, measure = {}", count, avg, mediumDate, measure);

        Long removed = measureRepository.deleteBySensorAndDateBetween(sensor, startDate, endDate);

        measureRepository.save(measure);

        return removed;
    }

    Date convertToDateViaInstant(LocalDateTime dateToConvert) {
        return java.util.Date
                .from(dateToConvert.atZone(ZoneId.systemDefault())
                        .toInstant());
    }

}
