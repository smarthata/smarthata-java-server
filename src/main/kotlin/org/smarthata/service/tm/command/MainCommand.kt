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
            Map.of("temp", "\uD83C\uDF21 Температура",
                    "heating", "\uD83D\uDD25 Отопление",
                    "light", "\uD83D\uDCA1 Освещение",
                    "watering", "\uD83D\uDCA6 Автополив",
                    "garage", "\uD83C\uDFCD Гараж",
                    "start", "▶\uFE0F Старт");

    public MainCommand() {
        super(START);
    }

    @Override
    public BotApiMethod<?> answer(CommandRequest request) {
        InlineKeyboardMarkup buttons = createButtons(emptyList(), devices);
        BotApiMethod<?> smarthataBot = createTmMessage(request.getChatId(), request.getMessageId(), "Smarthata bot", buttons);
        return smarthataBot;
    }

}
