package org.smarthata.service

import org.smarthata.service.message.EndpointType
import org.smarthata.service.message.SmarthataMessage
import org.smarthata.service.message.SmarthataMessageBroker
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class CronService(
    private val messageBroker: SmarthataMessageBroker,
    private val weatherService: WeatherService,
    private val sensorCleaner: SensorCleaner,
) {
    @Scheduled(cron = "0 */5 * * * *")
    fun calcAverageDailyStreetTemp() {
        messageBroker.broadcast(
            SmarthataMessage(
                "/street/temp-average",
                weatherService.calcAverageDailyStreetTemperature().toString(),
                EndpointType.SYSTEM,
                EndpointType.MQTT,
                true
            )
        )
    }

    @Scheduled(cron = "0 0 4 * * *")
    fun cleanSensorsData() {
        sensorCleaner.cleanAllDevices()
    }
}
