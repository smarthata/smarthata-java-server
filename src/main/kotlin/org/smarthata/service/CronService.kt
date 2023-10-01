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
    @Scheduled(cron = "0 */1 * * * *")
    fun calcAverageStreetTemp() {
        messageBroker.broadcast(
            smarthataMessage(
                weatherService.calcAverageDailyStreetTemperature(),
                "/street/temp-average"
            )
        )
        messageBroker.broadcast(
            smarthataMessage(
                weatherService.calcAverageWeeklyStreetTemperature(),
                "/street/temp-weekly-average"
            )
        )
    }

    private fun smarthataMessage(value: Double, topic: String) = SmarthataMessage(
        topic,
        value.toString(),
        EndpointType.SYSTEM,
        EndpointType.MQTT,
        true
    )

    @Scheduled(cron = "0 0 4 * * *")
    fun cleanSensorsData() {
        sensorCleaner.cleanAllDevices()
    }
}
