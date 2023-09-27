package org.smarthata.service.tm.command;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Map;

import static java.util.Collections.emptyList;


@Service
public class MainCommand extends AbstractCommand {

    private static final String START = "";
    private static final Map<String, String> devices =
            Map.of("temp", "Температура",
                    "heating", "Отопление",
                    "light", "Освещение",
                    "watering", "Автополив",
                    "garage", "Гараж",
                    "start", "Старт");

    public MainCommand() {
        super(START);
    }

    @Override
    public BotApiMethod<?> answer(CommandRequest request) {
        InlineKeyboardMarkup buttons = createButtons(emptyList(), devices);
        BotApiMethod<?> smarthataBot = createTmMessage(request.chatId, request.messageId, "Smarthata bot", buttons);
        return smarthataBot;
    }

}
