package org.smarthata.service

import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.smarthata.model.Measure
import org.smarthata.model.Sensor
import org.smarthata.repository.MeasureRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

@Service
class MeasureCleaner(private val measureRepository: MeasureRepository) {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    @Transactional
    fun cleanPeriod(sensor: Sensor, start: LocalDateTime, end: LocalDateTime): Long {
        logger.debug("Period {} - {}", start, end)
        val startDate = convertToDateViaInstant(start)
        val endDate = convertToDateViaInstant(end)
        val count = measureRepository.countBySensorAndDateBetween(sensor, startDate, endDate)
        if (count <= 1) {
            logger.debug("Skip due to small count {}", count)
            return 0L
        }
        val mid = (endDate.time - startDate.time) / 2
        val mediumDate = Date(startDate.time + mid)
        val avg = measureRepository.avgValueBySensorAndDateBetween(sensor.id, startDate, endDate)
        val measure = Measure(sensor, avg, mediumDate)
        logger.debug("count = {} values avg = {}  medDate = {}, measure = {}", count, avg, mediumDate, measure)
        val removed = measureRepository.deleteBySensorAndDateBetween(sensor, startDate, endDate)
        measureRepository.save(measure)
        return removed
    }

    fun convertToDateViaInstant(dateToConvert: LocalDateTime): Date = Date
        .from(
            dateToConvert.atZone(ZoneId.systemDefault())
                .toInstant()
        )
}