package org.smarthata.service.tm.command;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

import static java.util.Collections.emptyList;


@Service
public class MainCommand extends AbstractCommand {

    private static final String START = "";
    private static final List<String> devices = List.of("temp", "heating", "light", "watering", "start");

    public MainCommand() {
        super(START);
    }

    @Override
    public BotApiMethod<?> answer(CommandRequest request) {
        InlineKeyboardMarkup buttons = createButtons(emptyList(), devices);
        return createTmMessage(request.getChatId(), request.getMessageId(), "Smarthata bot", buttons);
    }

}
