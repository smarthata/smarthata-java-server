package org.smarthata.service.tm.command;

import org.springframework.core.annotation.Order;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

@Order(1)
public interface Command {

    boolean isProcessed(final String name);

    BotApiMethod answer(List<String> path, final String chatId, final Integer messageId);

    default SendMessage aSimpleSendMessage(final String chatId, final String text) {
        return new SendMessage(chatId, text);
    }

    default SendMessage aSimpleSendMessage(final String chatId, final String text, final InlineKeyboardMarkup keyboardMarkup) {
        return aSimpleSendMessage(chatId, text)
                .setReplyMarkup(keyboardMarkup);
    }
}

