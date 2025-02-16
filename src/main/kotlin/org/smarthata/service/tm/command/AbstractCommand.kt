package org.smarthata.service.tm.command

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

abstract class AbstractCommand(val command: String) : Command {
    override fun command(): String = command

    protected fun createButtons(path: List<String>, buttons: List<String>, buttonsInRow: Int = BUTTONS_IN_ROW) =
        InlineKeyboardMarkup(
            buttons.map { createButton(it, path, it) }
                .chunked(buttonsInRow)
        )

    protected fun createButtons(path: List<String>, buttons: Map<String, String>, buttonsInRow: Int = BUTTONS_IN_ROW): InlineKeyboardMarkup =
        InlineKeyboardMarkup(
            buttons.entries.map { (key, value) -> createButton(value, path, key) }
                .chunked(buttonsInRow)
        )

    protected fun createButton(text: String, path: List<String>, pathSuffix: String): InlineKeyboardButton {
        val fullPath = mutableListOf<String>()
        if (command.isNotEmpty()) fullPath.add(command)
        fullPath.addAll(path)
        if ("back" == pathSuffix) {
            fullPath.removeLast()
        } else {
            fullPath.add(pathSuffix)
        }

        val callbackData = "/" + fullPath.joinToString("/")

        return InlineKeyboardButton().apply {
            this.text = text
            this.callbackData = callbackData
        }
    }

    companion object {
        const val BUTTONS_IN_ROW: Int = 1
    }
}
