package org.smarthata.service.tm.command

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.smarthata.model.Mode
import org.smarthata.service.device.WateringService
import org.smarthata.service.message.EndpointType
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.BotApiMethod

@Service
class WateringCommand(private val wateringService: WateringService) : AbstractCommand(WATERING) {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)


    override fun answer(request: CommandRequest): BotApiMethod<*> {
        if (request.hasNext()) {
            val part = request.next()
            logger.debug("main part {}", part)
            return when (part) {
                "mode" -> changeMode(request)
                "settings" -> showSettings(request)
                "wave" -> startWave(request)
                "channel" -> showChannel(request)
                else -> showMainButtons(request, "This is not implemented:" + request.path)
            }
        }

        return showMainButtons(request, "Автополив:")
    }

    private fun changeMode(request: CommandRequest): BotApiMethod<*> {
        if (request.hasNext()) {
            val newModeIndex = request.next()
            val newMode = Mode.valueOf(newModeIndex.toInt())
            wateringService.updateMode(newMode, EndpointType.TELEGRAM)
            logger.info("Mode has been changed to {}", newMode)
            return showMainButtons(request, "Режим изменен")
        }
        return showMainButtons(request, "Режим автополива:")
    }

    private fun showMainButtons(request: CommandRequest, text: String): BotApiMethod<*> {
        val currentMode = wateringService.mode

        val next = currentMode.nextMode()

        val map = mutableMapOf<String, String>()
        map["mode/" + next.mode] = "Режим: " + currentMode.name
        if (currentMode != Mode.OFF) {
            map["channel"] = "Каналы: " + wateringService.channelStates.values
            map["wave"] = "Запустить полив"
        }
        map["settings"] = "Настройки"
        map["back"] = "\uD83D\uDD19 Назад"

        val keyboard = createButtons(emptyList(), map)
        return createTmMessage(request, text, keyboard)
    }

    private fun showSettings(request: CommandRequest): BotApiMethod<*> {
        if (request.hasNext()) {
            val part = request.next()
            logger.debug("setting part {}", part)
            return when (part) {
                "duration" -> showDurations(request)
                "start" -> showStartTimes(request)
                "blowing" -> startBlowing(request)
                else -> showMainButtons(request, "This is not implemented:" + request.path)
            }
        }
        val text = "Настройки полива:"

        val map= mutableMapOf<String, String>()
        map["duration"] = "Продолжительность каналов (мин): " + wateringService.durations
        map["start"] = "Время начала (ч): " + wateringService.startTimes
        map["blowing"] = "Продувка"
        map["back"] = "\uD83D\uDD19 Назад"

        val keyboard = createButtons(listOf("settings"), map)

        return createTmMessage(request, text, keyboard)
    }

    private fun showDurations(request: CommandRequest): BotApiMethod<*> {
        val text = "Продолжительность (минуты): " + wateringService.durations

        val buttons = listOf("change", "back")
        val keyboard = createButtons(listOf("duration"), buttons)

        return createTmMessage(request, text, keyboard)
    }

    private fun showStartTimes(request: CommandRequest): BotApiMethod<*> {
        val text = "Время начала полива (часы): " + wateringService.startTimes

        val buttons = listOf("add", "remove", "back")
        val keyboard = createButtons(listOf("start"), buttons)

        return createTmMessage(request, text, keyboard)
    }

    private fun startWave(request: CommandRequest): BotApiMethod<*> {
        wateringService.wave(EndpointType.TELEGRAM)
        return showMainButtons(request, "Полив запущен")
    }

    private fun startBlowing(request: CommandRequest): BotApiMethod<*> {
        wateringService.blowing(EndpointType.TELEGRAM)
        return showMainButtons(request, "Продувка запущена")
    }

    private fun showChannel(request: CommandRequest): BotApiMethod<*> {
        if (request.hasNext()) {
            val part = request.next()
            if (part == "disable") {
                wateringService.channelStates.keys
                    .forEach { wateringService.updateChannel(it, 0, EndpointType.TELEGRAM) }
            } else {
                val channel = part.toInt()
                logger.info("channel {}", channel)
                if (request.hasNext()) {
                    val newState = request.next().toInt()
                    logger.info("newState {}", newState)
                    wateringService.updateChannel(channel, newState, EndpointType.TELEGRAM)
                }
            }
        }

        val text = "Каналы: "

        val out= mutableMapOf<String, String>()
        wateringService.channelStates
            .forEach { (key: Int, value: Int) -> out[key.toString() + "/" + (1 - value)] = key.toString() + (if (value == 1) " \uD83D\uDCA6" else "") }
        out["disable"] = "Выключить"
        out["back"] = "\uD83D\uDD19 Назад"

        val keyboard = createButtons(listOf("channel"), out)
        return createTmMessage(request, text, keyboard)
    }

    companion object {
        private const val WATERING = "watering"
    }
}
