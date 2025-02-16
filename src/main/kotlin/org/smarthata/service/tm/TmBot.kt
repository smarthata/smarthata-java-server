package org.smarthata.service.tm

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.smarthata.service.message.EndpointType
import org.smarthata.service.message.SmarthataMessage
import org.smarthata.service.message.SmarthataMessageBroker
import org.smarthata.service.message.SmarthataMessageListener
import org.smarthata.service.tm.command.Command
import org.smarthata.service.tm.command.CommandRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Service
@ConditionalOnProperty(value = ["bot.enabled"], matchIfMissing = true)
final class TmBot(
    @Value("\${bot.token}") token: String?,
    @param:Value("\${bot.username}") private val username: String,
    @param:Value("\${bot.adminChatId}") private val adminChatId: String,
    commands: List<Command>,
    private val messageBroker: SmarthataMessageBroker,
) : TelegramLongPollingBot(token), SmarthataMessageListener {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    private val commandsMap = commands.groupBy { it.command() }
        .mapValues { it.value.first() }

    init {
        messageBroker.register(this)
    }

    override fun getBotUsername() = username

    override fun onUpdateReceived(update: Update) {
        logger.debug("Received update: {}", update)

        if (update.hasMessage()) {
            val message = update.message
            message.text?.let {
                onMessageReceived(message.chatId, it)
            }
        }

        if (update.hasCallbackQuery()) {
            val callback = update.callbackQuery
            callback.data?.let {
                onMessageReceived(callback.message.chatId, it, callback.message.messageId)
            }
        }
    }

    override fun receiveSmarthataMessage(message: SmarthataMessage) {
        if (message.path == "/messages") {
            sendMessageToTelegram(SendMessage(adminChatId, message.text))
        }
    }

    override fun endpointType() = EndpointType.TELEGRAM

    private fun onMessageReceived(chatId: Long, text: String, messageId: Int? = null) {
        logger.info("text: [{}], messageId {}", text, messageId)
        text.replace("@$username", "").let {
        val messageProcessed = processMessage(chatId, it, messageId)
            if (!messageProcessed && it.isNotEmpty()) {
                broadcast(it)
            }
        }
    }

    private fun processMessage(chatId: Long, text: String, messageId: Int?): Boolean {
        val path = (splitPath(text).takeIf { it.isNotEmpty() } ?: listOf(""))
            .toMutableList()
        logger.info("Process telegram message: path {}, text: {}", path, text)
        val commandName = path.removeAt(0)

        commandsMap[commandName]?.apply {
            logger.info("Found command: [{}]", commandName)
            sendMessageToTelegram(answer(CommandRequest(path, chatId.toString(), messageId)))
            return true
        }
        return false
    }

    private fun splitPath(text: String) =
        text.split("/".toRegex())
            .dropLastWhile { it.isEmpty() }
            .filter { it.isNotEmpty() }

    fun sendMessageToTelegram(botApiMethod: BotApiMethod<*>): Boolean {
        try {
            super.execute(botApiMethod)
            logger.debug("Message to telegram sent: {}", botApiMethod)
            return true
        } catch (e: TelegramApiException) {
            logger.error("Telegram Api Exception: {}", e.message, e)
        }
        return false
    }

    private fun broadcast(text: String) {
        val split = text.split("\\s".toRegex(), limit = 2)
        val message = SmarthataMessage(
            split.first(),
            split.last().takeIf { split.size == 2 } ?: "",
            EndpointType.TELEGRAM)
        messageBroker.broadcast(message)
    }
}
