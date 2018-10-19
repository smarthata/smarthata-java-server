package org.smarthata.service.tm.command;

import org.springframework.core.annotation.Order;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.List;

@Order(1)
public interface Command {

    String getCommand();

    BotApiMethod<?> answer(List<String> path, String chatId, Integer messageId);

    default SendMessage aSimpleSendMessage(String chatId, String text) {
        return new SendMessage(chatId, text);
    }

    default SendMessage aSimpleSendMessage(String chatId, String text, ReplyKeyboard keyboardMarkup) {
        return aSimpleSendMessage(chatId, text)
                .setReplyMarkup(keyboardMarkup);
    }
}

