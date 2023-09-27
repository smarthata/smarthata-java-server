package org.smarthata.service

import org.smarthata.service.message.EndpointType
import org.smarthata.service.message.SmarthataMessage
import org.smarthata.service.message.SmarthataMessageBroker
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalTime
import java.time.ZoneId

@Service
class CronMqttUpdater(private val messageBroker: SmarthataMessageBroker) {
    @Scheduled(cron = "0 * * * * *")
    fun sendSecondOfDay() {
        val secondOfDay = calcSecondOfDay()
        messageBroker.broadcast(
            SmarthataMessage(
                "/time/second-of-day",
                secondOfDay.toString(),
                EndpointType.SYSTEM,
                EndpointType.MQTT,
                true
            )
        )
    }

    @Scheduled(cron = "0 * * * * *")
    fun sendMinuteOfDay() {
        val minuteOfDay = calcSecondOfDay() / 60
        messageBroker.broadcast(
            SmarthataMessage(
                "/time/minute-of-day",
                minuteOfDay.toString(),
                EndpointType.SYSTEM,
                EndpointType.MQTT,
                true
            )
        )
    }

    private fun calcSecondOfDay(): Int = LocalTime.now(ZoneId.systemDefault()).toSecondOfDay()
}