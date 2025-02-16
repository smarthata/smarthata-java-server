package org.smarthata.service.tm.command

import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.BotApiMethod

@Service
class MessagesCommand : AbstractCommand(MESSAGES) {
    override fun answer(request: CommandRequest): BotApiMethod<*> {
        val text = request.path.joinToString(",")
        return createTmMessage(request, text)
    }

    companion object {
        private const val MESSAGES = "messages"
    }
}
