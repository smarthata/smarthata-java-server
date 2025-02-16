package org.smarthata.service.tm.command

import org.springframework.stereotype.Service

@Service
class MainCommand : AbstractCommand(START) {
    override fun answer(request: CommandRequest) =
        createTmMessage(request, "Smarthata bot", createButtons(emptyList(), devices))

    companion object {
        private const val START = ""
        private val devices = mapOf(
            "temp" to "\uD83C\uDF21 Температура",
            "heating" to "\uD83D\uDD25 Отопление",
            "light" to "\uD83D\uDCA1 Освещение",
            "watering" to "\uD83D\uDCA6 Автополив",
            "garage" to "\uD83C\uDFCD Гараж",
            "start" to "▶\uFE0F Старт")
    }
}
