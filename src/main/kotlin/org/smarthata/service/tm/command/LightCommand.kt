package org.smarthata.service.tm.command

import org.smarthata.service.device.LightService
import org.smarthata.service.device.Room
import org.smarthata.service.message.EndpointType.TELEGRAM
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import java.util.concurrent.TimeUnit.MINUTES

@Service
class LightCommand(private val lightService: LightService) : AbstractCommand(LIGHT) {
    override fun answer(request: CommandRequest): BotApiMethod<*> {
        var text = "Освещение в комнатах:"

        var temporary = 0
        if (request.hasNext()) {
            val part = request.next()
            when (part) {
                "1min" -> temporary = 1
                "5min" -> temporary = 5
            }
            if (request.hasNext()) {
                if (temporary > 0) {
                    val room = request.next()
                    lightService.enableLightTemporary(room, MINUTES.toSeconds(temporary.toLong()), TELEGRAM)
                    text = "Принято $room!"
                } else {
                    val action = request.next()
                    when (action) {
                        "on" -> {
                            lightService.updateLight(part, true, TELEGRAM)
                            text = "Включено\n$text"
                        }

                        "off" -> {
                            lightService.updateLight(part, false, TELEGRAM)
                            text = "Выключено\n$text"
                        }
                    }
                }
            }
            if (temporary > 0) return showTemporaryView(request, temporary, text)
        }


        return showMainView(request, text)
    }

    private fun showMainView(request: CommandRequest, text: String): BotApiMethod<*> {
        val rooms = lightService.lightState.entries
            .associate { (room, state) ->
                val action = if (!state) "on" else "off"
                val currentStatus = if (state) " \uD83D\uDCA1" else ""
                Pair("$room/$action", getRusName(room) + currentStatus)
            }.toMutableMap()

        rooms["1min"] = "1 мин"
        rooms["5min"] = "5 мин"
        rooms["back"] = "\uD83D\uDD19 Назад"
        val buttons = createButtons(listOf(), rooms, 2)
        return createTmMessage(request, text, buttons)
    }

    private fun showTemporaryView(request: CommandRequest, temporary: Int, prefix: String): BotApiMethod<*> {

        val text = "$prefix\nВключить на $temporary мин:"

        val rooms = mutableMapOf<String, String>()
        lightService.lightState.forEach { (room: String, roomState: Boolean) ->
            val currentStatus = if (roomState) " \uD83D\uDCA1" else ""
            rooms[room] = getRusName(room) + ": " + currentStatus
        }
        rooms["back"] = "\uD83D\uDD19 Назад"
        var path = request.path
        if (path.size > 2) path = path.subList(0, 1)
        val buttons = createButtons(path, rooms, 2)
        return createTmMessage(request, text, buttons)
    }

    companion object {
        private const val LIGHT = "light"

        private fun getRusName(room: String) = if (room == "stairs-night") {
            "Ночник"
        } else {
            Room.getFromRoomCode(room).rusName
        }
    }
}
