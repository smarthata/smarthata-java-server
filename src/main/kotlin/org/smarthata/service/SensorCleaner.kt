package org.smarthata.service

import org.slf4j.LoggerFactory
import org.smarthata.model.Device
import org.smarthata.model.Sensor
import org.smarthata.repository.DeviceRepository
import org.smarthata.repository.SensorRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class SensorCleaner(
    private val measureCleaner: MeasureCleaner,
    private val sensorRepository: SensorRepository,
    private val deviceRepository: DeviceRepository
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun cleanAllDevices() {
        deviceRepository.findAll().forEach { cleanDevice(it) }
    }

    fun cleanDevice(device: Device) {
        sensorRepository.findByDevice(device).forEach { sensor ->
            cleanSensor(sensor)
        }
    }

    private fun cleanSensor(sensor: Sensor) {
        logger.info("Start sensor clean = {}", sensor)
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)
        var totalRemoved = 0
        totalRemoved += cleanSensorByStep(sensor, now.minus(1, ChronoUnit.YEARS), ChronoUnit.HOURS, 1)
        totalRemoved += cleanSensorByStep(sensor, now.minus(6, ChronoUnit.MONTHS), ChronoUnit.MINUTES, 30)
        totalRemoved += cleanSensorByStep(sensor, now.minus(3, ChronoUnit.MONTHS), ChronoUnit.MINUTES, 15)
        totalRemoved += cleanSensorByStep(sensor, now.minus(1, ChronoUnit.MONTHS), ChronoUnit.MINUTES, 5)
        totalRemoved += cleanSensorByStep(sensor, now.minus(1, ChronoUnit.WEEKS), ChronoUnit.MINUTES, 1)
        logger.info("Finish sensor clean = {} totalRemoved = {}", sensor, totalRemoved)
    }

    private fun cleanSensorByStep(
        sensor: Sensor,
        periodFullEndDate: LocalDateTime,
        stepUnit: ChronoUnit,
        stepCount: Int
    ): Int {
        var periodEndDate = periodFullEndDate
        val earliestDate = periodEndDate.minus(2, ChronoUnit.DAYS)
        var totalRemoved = 0L
        do {
            logger.debug(
                "Sensor {} date {} / {}  total removed {}",
                sensor.name,
                periodEndDate.toLocalDate(),
                earliestDate.toLocalDate(),
                totalRemoved
            )
            val periodStartDate = periodEndDate.minus(stepCount.toLong(), stepUnit)
            totalRemoved += measureCleaner.cleanPeriod(sensor, periodStartDate, periodEndDate)
            periodEndDate = periodStartDate
        } while (periodEndDate.isAfter(earliestDate))
        logger.debug("Sensor {} total removed {}", sensor.name, totalRemoved)
        return totalRemoved.toInt()
    }
}