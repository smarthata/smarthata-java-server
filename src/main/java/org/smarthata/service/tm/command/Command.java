package org.smarthata.service.tm.command;

import org.springframework.core.annotation.Order;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.List;

@Order(1)
public interface Command {

    String getCommand();

    BotApiMethod<?> answer(List<String> path, String chatId, Integer messageId);

    default BotApiMethod<?> createTmMessage(String chatId, Integer messageId, String text) {
        return createTmMessage(chatId, messageId, text, null);
    }

    default BotApiMethod<?> createTmMessage(String chatId, Integer messageId, String text, InlineKeyboardMarkup buttons) {
        if (messageId == null) {
            return aSimpleSendMessage(chatId, text, buttons);
        } else {
            return anEditMessageText(chatId, text, buttons, messageId);
        }
    }

    default SendMessage aSimpleSendMessage(String chatId, String text) {
        return new SendMessage(chatId, text);
    }

    default SendMessage aSimpleSendMessage(String chatId, String text, ReplyKeyboard keyboardMarkup) {
        return aSimpleSendMessage(chatId, text)
                .setReplyMarkup(keyboardMarkup);
    }

    default BotApiMethod<?> anEditMessageText(String chatId, String text, InlineKeyboardMarkup inlineKeyboardMarkup, Integer messageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setReplyMarkup(inlineKeyboardMarkup);
        message.setText(text);
        return message;
    }
}

