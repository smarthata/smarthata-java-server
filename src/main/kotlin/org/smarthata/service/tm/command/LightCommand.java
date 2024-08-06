package org.smarthata.service.tm.command;

import org.smarthata.service.device.LightService;
import org.smarthata.service.device.Room;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.smarthata.service.message.EndpointType.TELEGRAM;

@Service
public class LightCommand extends AbstractCommand {

    private static final String LIGHT = "light";

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
                    lightService.enableLightTemporary(room, TimeUnit.MINUTES.toSeconds(temporary), TELEGRAM);
                    text = "Принято " + room + "!";
                } else {
                    String action = request.next();
                    switch (action) {
                        case "on" -> {
                            lightService.updateLight(part, true, TELEGRAM);
                            text = "Включено\n" + text;
                        }
                        case "off" -> {
                            lightService.updateLight(part, false, TELEGRAM);
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
        lightService.lightState.forEach((room, roomState) -> {
            String action = !roomState ? "on" : "off";
            String currentStatus = roomState ? " \uD83D\uDCA1" : "";
            rooms.put(room + "/" + action, getRusName(room) + currentStatus);
        });

        rooms.put("1min", "1 мин");
        rooms.put("5min", "5 мин");
        rooms.put("back", "\uD83D\uDD19 Назад");
        InlineKeyboardMarkup buttons = createButtons(List.of(), rooms, 2);
        return createTmMessage(request.chatId, request.messageId, text, buttons);
    }

    private static String getRusName(String room) {
        String rusName;
        if (room.equals("stairs-night")) {
            rusName = "Ночник";
        } else {
            rusName = Room.getFromRoomCode(room).rusName;
        }
        return rusName;
    }

    private BotApiMethod<?> showTemporaryView(CommandRequest request, int temporary, String text) {
        // /light
        text += "\nВключить на %d мин:".formatted(temporary);

        Map<String, String> rooms = new LinkedHashMap<>();
        lightService.lightState.forEach((room, roomState) -> {
            String currentStatus = roomState ? " \uD83D\uDCA1" : "";
            rooms.put(room, getRusName(room) + ": " + currentStatus);
        });
        rooms.put("back", "\uD83D\uDD19 Назад");
        List<String> path = request.path;
        if (path.size() > 2) path = path.subList(0, 1);
        InlineKeyboardMarkup buttons = createButtons(path, rooms, 2);
        return createTmMessage(request.chatId, request.messageId, text, buttons);
    }

}
