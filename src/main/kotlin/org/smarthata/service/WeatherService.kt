package org.smarthata.service

import org.slf4j.LoggerFactory
import org.smarthata.model.Measure
import org.smarthata.repository.MeasureRepository
import org.smarthata.repository.SensorRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.floor

@Service
class WeatherService(
    private val sensorRepository: SensorRepository,
    private val measureRepository: MeasureRepository,
    private val measureService: MeasureService,
    restTemplateBuilder: RestTemplateBuilder,
    @Value("\${narodmon.mac}") private val mac: String,
    @Value("\${narodmon.enabled}") private val narodmonEnabled: Boolean
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val restTemplate: RestTemplate = restTemplateBuilder.build()


    fun calcAverageDailyStreetTemperature(): Double {
        val streetSensor = sensorRepository.findByIdOrElseThrow(STREET_TEMP_SENSOR_ID)
        val dailyAverage = round(measureRepository.avgValueBySensorAndDateBetween(streetSensor.id, aDaysAgo(1), Date()))
        val dailyAverageSensor = sensorRepository.findByIdOrElseThrow(STREET_AVG_TEMP_SENSOR_ID)
        val measure = Measure(dailyAverageSensor, dailyAverage, Date())
        return measureRepository.save(measure).value
    }

    fun calcAverageWeeklyStreetTemperature(): Double {
        val dailyAverageSensor = sensorRepository.findByIdOrElseThrow(STREET_AVG_TEMP_SENSOR_ID)
        val weeklyAverage = round(measureRepository.avgValueBySensorAndDateBetween(dailyAverageSensor.id, aDaysAgo(7), Date()))
        val weeklyAverageSensor = sensorRepository.findByIdOrElseThrow(STREET_WEEKLY_AVG_TEMP_SENSOR_ID)
        val measure = Measure(weeklyAverageSensor, weeklyAverage, Date())
        return measureRepository.save(measure).value
    }

    @Scheduled(cron = "0 */5 * * * *")
    fun sendDataToNarodmon() {
        if (!narodmonEnabled) {
            return
        }
        val streetSensor = sensorRepository.findByIdOrElseThrow(STREET_TEMP_SENSOR_ID)
        val lastMeasure = measureRepository.findTopBySensorOrderByDateDesc(streetSensor)
        val aLittleBitAgo = Date().time - TimeUnit.MINUTES.toMillis(2)
        if (lastMeasure.date.before(Date(aLittleBitAgo))) {
            logger.error("Does not have time to publish into narodmon")
            return
        }
        val temp = lastMeasure.value
        try {
            val url = String.format("http://narodmon.ru/get?ID=%s&street=%s", mac, round(temp))
            val result = restTemplate.getForObject(url, String::class.java)
            logger.info("Temp {} sent to narodmon.ru result: {}", temp, result)
        } catch (e: RestClientException) {
            logger.error("Failed to send street temp {} to narodmon.ru", temp, e)
        }
    }

    private fun round(number: Double): Double {
        return floor(number * 10) / 10
    }

    private fun aDaysAgo(days: Long): Date {
        val yesterday = LocalDateTime.now().minusDays(days)
        return Date.from(yesterday.atZone(ZoneId.systemDefault()).toInstant())
    }

    companion object {
        private const val STREET_TEMP_SENSOR_ID = 13
        private const val STREET_AVG_TEMP_SENSOR_ID = 14
        private const val STREET_WEEKLY_AVG_TEMP_SENSOR_ID = 15
    }
}