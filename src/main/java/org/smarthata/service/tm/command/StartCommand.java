package org.smarthata.service.tm.command;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Collections;
import java.util.List;


@Service
public class StartCommand extends AbstractCommand {

    private static final String START = "start";
    private static final List<String> devices = List.of("/temp", "/heating", "/light", "/watering");

    public StartCommand() {
        super(START);
    }

    @Override
    public BotApiMethod<?> answer(CommandRequest request) {
        return aSimpleSendMessage(request.getChatId(), "Choose option:", createMainButtons());
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
        devices.forEach(device -> row.add(new KeyboardButton(device)));
        return Collections.singletonList(row);
    }
}
