package org.smarthata.service.tm.command

import org.springframework.core.annotation.Order
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard

@Order(1)
interface Command {
    fun command(): String

    fun answer(request: CommandRequest): BotApiMethod<*>

    fun createTmMessage(request: CommandRequest, text: String, buttons: InlineKeyboardMarkup? = null) =
        if (request.messageId == null) {
            aSimpleSendMessage(request.chatId, text, buttons)
        } else {
            anEditMessageText(request.chatId, text, buttons, request.messageId)
        }

    fun createTmMessage(chatId: String, messageId: Int?, text: String, buttons: InlineKeyboardMarkup? = null) =
        if (messageId == null) {
            aSimpleSendMessage(chatId, text, buttons)
        } else {
            anEditMessageText(chatId, text, buttons, messageId)
        }

    fun aSimpleSendMessage(chatId: String, text: String, keyboardMarkup: ReplyKeyboard? = null): SendMessage =
        SendMessage().apply {
            this.chatId = chatId
            this.text = text
            keyboardMarkup?.let { this.replyMarkup = it }
        }

    fun anEditMessageText(chatId: String, text: String, inlineKeyboardMarkup: InlineKeyboardMarkup?, messageId: Int?) =
        EditMessageText().apply {
            this.chatId = chatId
            this.messageId = messageId
            this.replyMarkup = inlineKeyboardMarkup
            this.text = text
        }
}

