package org.smarthata.service.tm.command;

import com.google.common.collect.ImmutableList;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

@Service
public class StartCommand extends AbstractCommand {

    private static final String START = "start";

    public StartCommand() {
        super(START);
    }

    @Override
    public BotApiMethod<?> answer(final List<String> path, final String chatId, final Integer messageId) {
        return aSimpleSendMessage(chatId, "Выбирите команду:", createMainButtons());
    }

    private ReplyKeyboardMarkup createMainButtons() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setSelective(true);
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);
        keyboard.setKeyboard(createKeyboardRows());
        return keyboard;
    }

    private List<KeyboardRow> createKeyboardRows() {
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("/temp"));
        row.add(new KeyboardButton("/heating"));
        row.add(new KeyboardButton("/watering"));
        row.add(new KeyboardButton("/lighting"));
        return ImmutableList.of(row);
    }
}
