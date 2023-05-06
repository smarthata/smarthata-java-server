package org.smarthata.service.tm.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j

@Service
public class GarageCommand extends AbstractCommand {

    private static final String GARAGE = "garage";

    @Value("${bot.adminChatId}")
    public String adminChatId;

    public AtomicBoolean gatesOpen = new AtomicBoolean(false);

    public GarageCommand() {
        super(GARAGE);
    }

    @Override
    public BotApiMethod<?> answer(CommandRequest request) {

        log.info("Garage request: {}", request);


        String text = "Ворота " + (gatesOpen.get() ? "открыты" : "закрыты");

        if (request.hasNext()) {
            String item = request.next();
            if (item.equals("gates")) {
                if (request.hasNext()) {
                    String action = request.next();
                    switch (action) {
                        case "open" -> gatesOpen.set(true);
                        case "close" -> gatesOpen.set(false);
                    }
                    text = "Принято! Ворота " + (gatesOpen.get() ? "открыты" : "закрыты");
                }
            } else {
                log.info("Unknown item {}", item);
                text = item;
            }
        }

        InlineKeyboardMarkup buttons = createButtons(List.of("gates"), List.of(gatesOpen.get() ? "close" : "open"));
        return createTmMessage(request.getChatId(), request.getMessageId(), text, buttons);
    }


}
