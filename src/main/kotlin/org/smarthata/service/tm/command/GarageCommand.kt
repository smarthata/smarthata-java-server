package org.smarthata.service.tm.command

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.smarthata.service.formatTemp
import org.smarthata.service.mqtt.MqttMessagesCache
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import java.util.concurrent.atomic.AtomicBoolean

@Service
class GarageCommand(
    private val mqttMessagesCache: MqttMessagesCache,
    @param:Value("\${bot.adminChatId}") val adminChatId: String,
) : AbstractCommand(GARAGE) {
    var gatesOpen: AtomicBoolean = AtomicBoolean(false)

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun answer(request: CommandRequest): BotApiMethod<*> {
        logger.info("Garage request: {}", request)

        var text = "Ворота " + (if (gatesOpen.get()) "открыты" else "закрыты")

        if (request.hasNext()) {
            when (val item = request.next()) {
                "open" -> {
                    gatesOpen.set(true)
                    text = "Принято! Ворота открыты"
                }

                "close" -> {
                    gatesOpen.set(false)
                    text = "Принято! Ворота закрыты"
                }

                else -> text = item
            }
        }

        val temps = mutableListOf<String>()
        findStreetTemp()?.let {
            temps.add("улица ${formatTemp(it)}")
        }
        findStreetAverageTemp()?.let {
            temps.add("среднесуточная ${formatTemp(it)}")
        }
        findGarageTemp()?.let {
            temps.add("гараж ${formatTemp(it)}")
        }
        if (temps.isNotEmpty()) {
            text += " (" + temps.joinToString(", ") + ")"
        }

        val map = mutableMapOf<String, String>()
        val action = if (gatesOpen.get()) "close" else "open"
        map[action] = action
        map["back"] = "\uD83D\uDD19 Назад"

        val buttons = createButtons(emptyList(), map, 2)
        return createTmMessage(request, text, buttons)
    }


    private fun findStreetTemp() =
        mqttMessagesCache.findLastMessageAsDouble("/street/temp")

    private fun findStreetAverageTemp() =
        mqttMessagesCache.findLastMessageAsDouble("/street/temp-average")

    private fun findGarageTemp() =
        mqttMessagesCache.findLastMessageFieldFromJson("/heating/garage/garage", "temp") as Double?

    companion object {
        private const val GARAGE = "garage"
    }
}
