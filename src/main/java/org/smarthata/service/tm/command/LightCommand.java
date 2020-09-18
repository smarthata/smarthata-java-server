package org.smarthata.service.tm.command;

import org.smarthata.service.device.LightService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public BotApiMethod<?> answer(final List<String> path, final String chatId, final Integer messageId) {
        if (path.isEmpty()) {
            return showRoomButtons(emptyList(), chatId, messageId);
        }

        String room = path.remove(0);
        boolean isEnabled = lightService.getLight(room);
        lightService.setLight(room, isEnabled ? "0" : "1");

        return showRoomButtons(emptyList(), chatId, messageId);
    }

    private BotApiMethod<?> showRoomButtons(List<String> path, String chatId, Integer messageId) {
        String text = "Освещение в комнатах:";
        Map<String, String> rooms = LightCommand.rooms.stream()
                .collect(Collectors.toMap(room -> room, room -> room + ": " + (lightService.getLight(room) ? "on" : "off")));
        InlineKeyboardMarkup buttons = createButtons(path, rooms, 2);
        return createTmMessage(chatId, messageId, text, buttons);
    }

}
