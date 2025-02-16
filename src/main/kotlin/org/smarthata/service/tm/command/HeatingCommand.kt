package org.smarthata.service.tm.command

import org.smarthata.service.device.Room
import org.smarthata.service.device.heating.HeatingService
import org.smarthata.service.message.EndpointType
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.BotApiMethod

@Order(1)
@Service
class HeatingCommand(private val heatingService: HeatingService) : AbstractCommand(HEATING) {
    override fun answer(request: CommandRequest): BotApiMethod<*> {
        if (request.hasNext()) {
            val house = request.next()
            return when (house) {
                "house" -> processHouse(request)
                "garage" -> processGarage(request)
                "config" -> processConfig(request)
                else -> {
                    val text = "Unknown house"
                    createTmMessage(request, text)
                }
            }
        }

        val text = "Выберите помещение:"
        val buttons = mutableMapOf<String, String>()
        buttons["house"] = "\uD83C\uDFE0 Дом"
        buttons["garage"] = "\uD83C\uDFCD Гараж"
        buttons["config"] = "⚙ Настройка"
        buttons["back"] = "\uD83D\uDD19 Назад"
        return createTmMessage(request, text, createButtons(request.path, buttons, 2))
    }

    private fun processHouse(request: CommandRequest): BotApiMethod<*> {
        if (request.hasNext()) {
            val room = request.next()
            return when (room) {
                "floor" -> processRoom(request, Room.HALL)
                "bedroom" -> processRoom(request, Room.BEDROOM)
                else -> {
                    val text = "Unknown room"
                    createTmMessage(request, text)
                }
            }
        }

        val text = "Выберите помещение:"
        val buttons = mutableMapOf<String, String>()
        buttons["floor"] = showTempInRoom("Зал", Room.HALL)
        buttons["bedroom"] = showTempInRoom("Спальня", Room.BEDROOM)
        buttons["back"] = "\uD83D\uDD19 Назад"

        return createTmMessage(request, text, createButtons(request.path, buttons))
    }

    private fun showTempInRoom(roomName: String, room: Room): String {
//        if (heatingDevice.isActualTempExists(room)) {
//            return "%s: %.1f%s/%.1f%s".format(roomName, heatingDevice.getActualTemp(room)), CELSIUS,
//                    heatingDevice.getExpectedTemp(room), CELSIUS);
//        }
        return "%s: %.1f%s".format(roomName, heatingService.expectedTemp(room), CELSIUS)
    }

    private fun processGarage(request: CommandRequest): BotApiMethod<*> {
        if (request.hasNext()) {
            val device = request.next()
            return when (device) {
                "garage" -> processRoom(request, Room.GARAGE)
                "workshop" -> processRoom(request, Room.WORKSHOP)
                else -> {
                    val text = "Unknown device"
                    createTmMessage(request, text)
                }
            }
        }

        val text = "Выберите помещение:"
        val v1 = showTempInRoom("Гараж", Room.GARAGE)
        val v2 = showTempInRoom("Мастерская", Room.WORKSHOP)
        val buttons = mutableMapOf<String, String>()
        buttons["garage"] = v1
        buttons["workshop"] = v2
        buttons["back"] = "\uD83D\uDD19 Назад"
        return createTmMessage(request, text, createButtons(request.path, buttons))
    }

    private fun processRoom(request: CommandRequest, room: Room): BotApiMethod<*> {
        if (request.hasNext()) {
            val next = request.next()
            try {
                heatingService.incExpectedTemp(room, next.toDouble())
            } catch (e: NumberFormatException) {
                val text = "Unknown command: $next"
                return createTmMessage(request, text)
            }
        }

        val roomName = room.name.lowercase()
        val text = "Temp %s: %1.1f%s".format(roomName, heatingService.expectedTemp(room),
            CELSIUS)
        val buttons = createButtons(
            request.createPathRemoving("-0.5", "+0.5", "-1", "+1"),
            listOf("-0.5", "+0.5", "-1", "+1", "set", "back"),
            2
        )
        return createTmMessage(request, text, buttons)
    }


    private fun processConfig(request: CommandRequest): BotApiMethod<*> {
        var restart = false
        if (request.hasNext()) {
            val config = request.next()
            when (config) {
                "restart" -> {
                    restart = true
                    heatingService.sendAction("restart", EndpointType.TELEGRAM, null)
                }

                "mixer" -> {
                    return processMixer(request)
                }
            }
        }

        var text = "Настройки:\n"
        if (restart) {
            text += "Рестарт отправлен.\n"
        }

        val buttons = mutableMapOf<String, String>()
        if (!restart) {
            buttons["restart"] = "restart"
        }
        buttons["mixer"] = "mixer: " + heatingService.mixerPosition
        buttons["back"] = "\uD83D\uDD19 Назад"
        return createTmMessage(request, text, createButtons(listOf("config"), buttons))
    }

    private fun processMixer(request: CommandRequest): BotApiMethod<*> {
        val values = listOf(-120, -60, -30, -15, 15, 30)

        var commandText = ""
        if (request.hasNext()) {
            val command = request.next()
            val value = values.stream().filter { integer: Int -> command == integer.toString() }
                .findFirst().orElse(0)
            heatingService.sendAction("mixer-move", EndpointType.TELEGRAM, value)
            commandText = "Принято $command! "
        }

        val text = commandText + "Mixer: " + heatingService.mixerPosition

        val buttons = mutableMapOf<String, String>()
        for (value in values) {
            buttons[value.toString()] = value.toString()
        }
        buttons["back"] = "\uD83D\uDD19 Назад"
        return createTmMessage(request, text, createButtons(listOf("config", "mixer"), buttons))
    }

    companion object {
        private const val HEATING = "heating"
        private const val CELSIUS = "°C"
    }
}
