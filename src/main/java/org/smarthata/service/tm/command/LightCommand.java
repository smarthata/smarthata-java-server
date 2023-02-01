package org.smarthata.service.tm.command;

import org.smarthata.service.device.LightService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Service
public class LightCommand extends AbstractCommand {

    private static final String LIGHT = "light";
    private static final Map<String, String> translations = Map.of("bathroom", "Ванная", "bedroom", "Спальня", "canopy", "Навес", "room-egor", "Детская Егора", "room-liza", "Детская Лизы", "stairs-night", "Ночник на лестнице", "stairs", "Свет на лестнице");

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
                        lightService.setLight(room, "0");
                        break;
                    case "5min":
                        lightService.enableLightTemporary(room, TimeUnit.MINUTES.toSeconds(5));
                        break;
                    case "1min":
                        lightService.enableLightTemporary(room, TimeUnit.MINUTES.toSeconds(1));
                        break;
                }
            }
            return showRoom(request, room);
        }

        return showRoomButtons(request);
    }

    private BotApiMethod<?> showRoomButtons(CommandRequest request) {
        String text = "Освещение в комнатах:";
        Map<String, String> rooms = new TreeMap<>();
        lightService.getLightState().forEach(
                (room, state) -> rooms.put(room,
                        translations.getOrDefault(room, room) + ": " + (state ? "on" : "off")));
        InlineKeyboardMarkup buttons = createButtons(emptyList(), rooms);
        return createTmMessage(request.getChatId(), request.getMessageId(), text, buttons);
    }

    private BotApiMethod<?> showRoom(CommandRequest request, String room) {
        Boolean state = lightService.getLight(room);
        String text = "Освещение в комнате " + room + ": " + (state ? "on" : "off");

        Map<String, String> keys = new LinkedHashMap<>();
        keys.put("1min", "1 мин");
        keys.put("5min", "5 мин");
        if (state) {
            keys.put("off", "Выключить");
        } else {
            keys.put("on", "Включить");
        }
        keys.put("back", "Назад");

        InlineKeyboardMarkup buttons = createButtons(singletonList(room), keys, 2);
        return createTmMessage(request.getChatId(), request.getMessageId(), text, buttons);
    }

}
