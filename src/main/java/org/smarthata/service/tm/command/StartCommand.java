package org.smarthata.service.tm.command;

import com.google.common.collect.ImmutableList;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

import static java.util.Arrays.asList;

@Service
public class StartCommand extends AbstractCommand {

    private static final String START = "start";
    private static final List<String> DEVICES = asList("/stats", "/temp", "/heating", "/hum");

    public StartCommand() {
        super(START);
    }

    @Override
    public BotApiMethod<?> answer(final List<String> path, final String chatId, final Integer messageId) {
        return aSimpleSendMessage(chatId, "Choose option:", createMainButtons());
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
        DEVICES.forEach(device -> row.add(new KeyboardButton(device)));
        return ImmutableList.of(row);
    }
}
