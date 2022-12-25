package org.smarthata.service.tm.command;

import org.smarthata.service.device.LightService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

@Service
public class LightCommand extends AbstractCommand {

    private static final String LIGHT = "light";
    public static final List<String> rooms = List.of("bedroom", "stairs", "cabinet", "bathroom", "children", "canopy");

    private final LightService lightService;

    public LightCommand(LightService lightService) {
        super(LIGHT);
        this.lightService = lightService;
    }

    @Override
    public BotApiMethod<?> answer(CommandRequest request) {
        if (request.hasNext()) {
            String room = request.next();
            if (request.hasNext()) {
                String action = request.next();
                switch (action) {
                    case "on":
                        lightService.setLight(room, "1");
                        break;
                    case "off":
                        lightService.setLight(room, "2");
                        break;
                }
            }
        }

        return showRoomButtons(request);
    }

    private BotApiMethod<?> showRoomButtons(CommandRequest request) {
        String text = "Освещение в комнатах:";
        Map<String, String> rooms = new LinkedHashMap<>();
        LightCommand.rooms.forEach(room -> {
            rooms.put(room + "/" + (lightService.getLight(room) ? "off" : "on"),
                    room + ": " + (lightService.getLight(room) ? "on" : "off"));
        });
        InlineKeyboardMarkup buttons = createButtons(emptyList(), rooms);
        return createTmMessage(request.getChatId(), request.getMessageId(), text, buttons);
    }

}
