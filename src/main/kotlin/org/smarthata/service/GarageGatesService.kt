package org.smarthata.service

import org.slf4j.LoggerFactory
import org.smarthata.service.DateUtils.isDateAfter
import org.smarthata.service.mqtt.MqttMessagesCache
import org.smarthata.service.tm.TmBot
import org.smarthata.service.tm.command.CommandRequest
import org.smarthata.service.tm.command.GarageCommand
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime

@Service
@ConditionalOnBean(TmBot::class)
class GarageGatesService(
    private val mqttMessagesCache: MqttMessagesCache,
    private val garageCommand: GarageCommand,
    private val tmBot: TmBot,
    @Value("\${garage.gates.average-temp-heating:18.0}") private val averageTempForHeating: Double,
    @Value("\${garage.gates.average-temp-cooldown:23.0}") private val averageTempForCooldown: Double,
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private var lastNotificationTime: LocalDateTime? = null

    @Scheduled(cron = "0 */5 * * * *")
    fun checkGarage() {
        val averageTemp = findStreetAverageTemp()
        if (averageTemp == null) {
            logger.error("Average street temp is not found")
            return
        }
        val streetTemp = findStreetTemp()
        if (streetTemp == null) {
            logger.error("Street temp is not found")
            return
        }
        if (averageTemp <= averageTempForHeating) {
            logger.debug("Check for heating garage")
            handleGarageGatesAction(findGarageGateWarmingAction(streetTemp, findGarageTemp()), "heating")
        }else if (averageTemp >= averageTempForCooldown) {
            logger.debug("Check for cooldown garage")
            handleGarageGatesAction(findGarageGateCooldownAction(streetTemp, findGarageTemp()), "cooldown")
        } else {
            logger.debug("Heating of garage is not needed")
        }
    }

    private fun handleGarageGatesAction(action: GarageGateAction, reason: String) {
        if (action == GarageGateAction.OPEN || action == GarageGateAction.CLOSE) {
            logger.debug("Temp is good to {} gates", action.name)
            if (isDateAfter(lastNotificationTime, Duration.ofMinutes(44))) {
                sendTelegramMessage(action, reason)
            } else {
                logger.debug("Notification was sent recently")
            }
        } else if (action == GarageGateAction.ERROR) {
            logger.error("Garage temp is not found")
        }
    }

    private fun findGarageGateWarmingAction(streetTemp: Double, garageTemp: Double?): GarageGateAction =
        if (garageTemp == null && streetTemp <= averageTempForHeating)
            GarageGateAction.CLOSE
        else if (garageTemp == null)
            GarageGateAction.ERROR
        else if (streetTemp > garageTemp + 1.0 && !isGatesOpened())
            GarageGateAction.OPEN
        else if (garageTemp > streetTemp + 1.0 && isGatesOpened())
            GarageGateAction.CLOSE
        else
            GarageGateAction.NOTHING

    private fun findGarageGateCooldownAction(streetTemp: Double, garageTemp: Double?): GarageGateAction =
        if (garageTemp == null && streetTemp >= averageTempForCooldown)
            GarageGateAction.CLOSE
        else if (garageTemp == null)
            GarageGateAction.ERROR
        else if (streetTemp > garageTemp + 1.0 && isGatesOpened())
            GarageGateAction.CLOSE
        else if (garageTemp > streetTemp + 1.0 && !isGatesOpened())
            GarageGateAction.OPEN
        else
            GarageGateAction.NOTHING

    private fun isGatesOpened() = garageCommand.gatesOpen.get()

    private fun sendTelegramMessage(action: GarageGateAction, reason: String) {
        val text = when (action) {
            GarageGateAction.OPEN -> "Можно открыть гаражные ворота ($reason)"
            GarageGateAction.CLOSE -> "Нужно закрыть гаражные ворота ($reason)"
            else -> "Ничего делать не нужно"
        }

        val commandRequest = CommandRequest(listOf(text), garageCommand.adminChatId, null)
        if (tmBot.sendMessageToTelegram(garageCommand.answer(commandRequest))) {
            lastNotificationTime = LocalDateTime.now()
        }
    }

    private fun findStreetTemp(): Double? = mqttMessagesCache.findLastMessageAsDouble("/street/temp")
        .also { logger.debug("Street temp: {}", it) }

    private fun findStreetAverageTemp(): Double? = mqttMessagesCache.findLastMessageAsDouble("/street/temp-average")
        .also { logger.debug("Street average temp: {}", it) }

    private fun findGarageTemp(): Double? =
        (mqttMessagesCache.findLastMessageFieldFromJson("/heating/garage/garage", "temp") as Double?)
            .also { logger.debug("Garage temp: {}", it) }

    internal enum class GarageGateAction {
        OPEN, CLOSE, NOTHING, ERROR;
    }
}
