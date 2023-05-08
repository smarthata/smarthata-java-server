package org.smarthata.service.tm.command;

import org.smarthata.service.device.LightService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class LightCommand extends AbstractCommand {

    private static final String LIGHT = "light";
    private static final Map<String, String> translations = Map.of(
            "all", "Везде",
            "bathroom", "Ванная",
            "bedroom", "Спальня",
            "canopy", "Навес",
            "room-egor", "Детская Егора",
            "room-liza", "Детская Лизы",
            "stairs-night", "Ночник на лестнице",
            "stairs", "Свет на лестнице");

    private final LightService lightService;

    public LightCommand(LightService lightService) {
        super(LIGHT);
        this.lightService = lightService;
    }

    @Override
    public BotApiMethod<?> answer(CommandRequest request) {

        String text = "Освещение в комнатах:";

        int temporary = 0;
        if (request.hasNext()) {
            String part = request.next();
            switch (part) {
                case "1min" -> temporary = 1;
                case "5min" -> temporary = 5;
            }
            if (request.hasNext()) {
                if (temporary > 0) {
                    String room = request.next();
                    lightService.enableLightTemporary(room, TimeUnit.MINUTES.toSeconds(temporary));
                    text = "Принято " + room + "!";
                } else {
                    String action = request.next();
                    switch (action) {
                        case "on" -> {
                            lightService.setLight(part, "1");
                            text = "Включено\n" + text;
                        }
                        case "off" -> {
                            lightService.setLight(part, "0");
                            text = "Выключено\n" + text;
                        }
                    }
                }
            }
            if (temporary > 0) return showTemporaryView(request, temporary, text);
        }


        return showMainView(request, text);
    }

    private BotApiMethod<?> showMainView(CommandRequest request, String text) {
        // /light
        Map<String, String> rooms = new LinkedHashMap<>();
        lightService.getLightState().forEach((room, roomState) -> {
            String action = !roomState ? "on" : "off";
            String currentStatus = roomState ? "on" : "off";
            rooms.put(room + "/" + action,
                    translations.getOrDefault(room, room) + ": " + currentStatus);
        });

        rooms.put("1min", "1 мин");
        rooms.put("5min", "5 мин");
        rooms.put("back", "Назад");
        InlineKeyboardMarkup buttons = createButtons(List.of(), rooms, 2);
        return createTmMessage(request.getChatId(), request.getMessageId(), text, buttons);
    }

    private BotApiMethod<?> showTemporaryView(CommandRequest request, int temporary, String text) {
        // /light
        text += "\nВключить на %d мин:".formatted(temporary);

        Map<String, String> rooms = new LinkedHashMap<>();
        lightService.getLightState().forEach((room, roomState) -> {
            String currentStatus = roomState ? "on" : "off";
            rooms.put(room,
                    translations.getOrDefault(room, room) + ": " + currentStatus);
        });
        rooms.put("back", "Назад");
        List<String> path = request.getPath();
        if (path.size() > 2) path = path.subList(0, 1);
        InlineKeyboardMarkup buttons = createButtons(path, rooms, 2);
        return createTmMessage(request.getChatId(), request.getMessageId(), text, buttons);
    }

}
