package org.smarthata.service.tm.command

import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

@Service
class StartCommand : AbstractCommand(START) {
    override fun answer(request: CommandRequest) =
        aSimpleSendMessage(request.chatId, "Smarthata bot", createMainButtons())

    private fun createMainButtons(): ReplyKeyboardMarkup {
        val keyboard = ReplyKeyboardMarkup()
        keyboard.selective = true
        keyboard.resizeKeyboard = true
        keyboard.oneTimeKeyboard = false
        keyboard.keyboard = createKeyboardRows()
        return keyboard
    }

    private fun createKeyboardRows(): List<KeyboardRow> =
        listOf(KeyboardRow()
            .apply { addAll(devices.map { KeyboardButton(it) }) })

    companion object {
        private const val START = "start"
        private val devices = listOf("/temp", "/heating", "/light", "/watering")
    }
}
