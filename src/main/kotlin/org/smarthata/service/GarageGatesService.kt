package org.smarthata.service

import org.slf4j.LoggerFactory
import org.smarthata.service.DateUtils.isDateAfter
import org.smarthata.service.mqtt.MqttMessagesCache
import org.smarthata.service.tm.TmBot
import org.smarthata.service.tm.command.CommandRequest
import org.smarthata.service.tm.command.GarageCommand
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Service
class GarageGatesService(
    private val mqttMessagesCache: MqttMessagesCache,
    private val garageCommand: GarageCommand,
    @param:Autowired(required = false) private val tmBot: TmBot,
    @Value("\${garage.gates.average-temp-threshold:20.0}") private val averageTempThreshold: Double,
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private var lastNotificationTime: LocalDateTime? = null

    @Scheduled(fixedDelay = 60, timeUnit = TimeUnit.SECONDS)
    fun checkGarage() {
        try {
            if (findStreetAverageTemp() <= averageTempThreshold) {
                logger.debug("Check for heating garage")
                val streetTemp = findStreetTemp()
                val garageTemp = findGarageTemp()
                val action = findGarageGateWarmingAction(streetTemp, garageTemp)
                if (action != GarageGateAction.NOTHING) {
                    logger.debug("Temp is good to {} gates", action.name)
                    if (isDateAfter(lastNotificationTime, Duration.ofMinutes(30))) {
                        sendTelegramMessage(action)
                    } else {
                        logger.debug("Notification was sent recently")
                    }
                }
            }
        } catch (e: RuntimeException) {
            logger.error("Error in garage gates check", e)
        }
    }

    private fun findGarageGateWarmingAction(streetTemp: Double, garageTemp: Double): GarageGateAction =
        if (streetTemp > garageTemp + 0.5 && !garageCommand.gatesOpen.get())
            GarageGateAction.OPEN
        else
            if (streetTemp + 0.5 < garageTemp && garageCommand.gatesOpen.get())
                GarageGateAction.CLOSE
            else
                GarageGateAction.NOTHING

    private fun sendTelegramMessage(action: GarageGateAction) {
        val text = when (action) {
            GarageGateAction.OPEN -> "Можно открыть гаражные ворота для прогрева"
            GarageGateAction.CLOSE -> "Нужно закрыть гаражные ворота, зима близко"
            else -> "Ничего делать не нужно"
        }

        val commandRequest = CommandRequest(listOf(text), garageCommand.adminChatId, null)
        if (tmBot.sendMessageToTelegram(garageCommand.answer(commandRequest))) {
            lastNotificationTime = LocalDateTime.now()
        }
    }

    private fun findStreetTemp(): Double = mqttMessagesCache.findLastMessageAsDouble("/street/temp")
        .orElseThrow { RuntimeException("Street temp is not populated") }
        .also { logger.debug("Street temp: {}", it) }

    private fun findStreetAverageTemp(): Double = mqttMessagesCache.findLastMessageAsDouble("/street/temp-average")
        .orElseThrow { RuntimeException("Average temp is not populated") }
        .also { logger.debug("Street average temp: {}", it) }

    private fun findGarageTemp(): Double =
        (mqttMessagesCache.findLastMessageFieldFromJson("/heating/garage/garage", "temp")
            .orElseThrow { RuntimeException("Garage data is not populated") } as Double)
            .also { logger.debug("Garage temp: {}", it) }

    internal enum class GarageGateAction {
        OPEN, CLOSE, NOTHING;
    }
}
