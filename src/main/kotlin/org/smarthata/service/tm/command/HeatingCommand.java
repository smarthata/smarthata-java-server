package org.smarthata.service.tm.command;

import org.smarthata.service.device.heating.HeatingService;
import org.smarthata.service.device.Room;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.smarthata.service.device.Room.*;
import static org.smarthata.service.message.EndpointType.TELEGRAM;


@Order(1)
@Service
public class HeatingCommand extends AbstractCommand {

    private static final String HEATING = "heating";
    private static final String CELSIUS = "°C";

    private final HeatingService heatingService;

    public HeatingCommand(HeatingService heatingService) {
        super(HEATING);
        this.heatingService = heatingService;
    }


    @Override
    public BotApiMethod<?> answer(CommandRequest request) {

        if (request.hasNext()) {
            String house = request.next();
            return switch (house) {
                case "house" -> processHouse(request);
                case "garage" -> processGarage(request);
                case "config" -> processConfig(request);
                default -> {
                    String text = "Unknown house";
                    yield createTmMessage(request.chatId, request.messageId, text);
                }
            };
        }

        String text = "Выберите помещение:";
        Map<String, String> buttons = new LinkedHashMap<>();
        buttons.put("house", "\uD83C\uDFE0 Дом");
        buttons.put("garage", "\uD83C\uDFCD Гараж");
        buttons.put("config", "⚙ Настройка");
        buttons.put("back", "\uD83D\uDD19 Назад");
        return createTmMessage(request.chatId, request.messageId,
                text, createButtons(request.path, buttons, 2));
    }

    private BotApiMethod<?> processHouse(CommandRequest request) {

        if (request.hasNext()) {
            String room = request.next();
            return switch (room) {
                case "floor" -> processRoom(request, HALL);
                case "bedroom" -> processRoom(request, BEDROOM);
                default -> {
                    String text = "Unknown room";
                    yield createTmMessage(request.chatId, request.messageId, text);
                }
            };
        }

        String text = "Выберите помещение:";
        Map<String, String> buttons = new LinkedHashMap<>();
        buttons.put("floor", showTempInRoom("Зал", HALL));
        buttons.put("bedroom", showTempInRoom("Спальня", BEDROOM));
        buttons.put("back", "\uD83D\uDD19 Назад");

        return createTmMessage(request.chatId, request.messageId,
                text, createButtons(request.path, buttons));
    }

    private String showTempInRoom(String roomName, Room room) {
//        if (heatingDevice.isActualTempExists(room)) {
//            return String.format("%s: %.1f%s/%.1f%s", roomName, heatingDevice.getActualTemp(room), CELSIUS,
//                    heatingDevice.getExpectedTemp(room), CELSIUS);
//        }
        return String.format("%s: %.1f%s", roomName, heatingService.expectedTemp(room), CELSIUS);
    }

    private BotApiMethod<?> processGarage(CommandRequest request) {

        if (request.hasNext()) {
            String device = request.next();
            return switch (device) {
                case "garage" -> processRoom(request, GARAGE);
                case "workshop" -> processRoom(request, WORKSHOP);
                default -> {
                    String text = "Unknown device";
                    yield createTmMessage(request.chatId, request.messageId, text);
                }
            };
        }

        String text = "Выберите помещение:";
        String v1 = showTempInRoom("Гараж", GARAGE);
        String v2 = showTempInRoom("Мастерская", WORKSHOP);
        Map<String, String> buttons = new LinkedHashMap<>();
        buttons.put("garage", v1);
        buttons.put("workshop", v2);
        buttons.put("back", "\uD83D\uDD19 Назад");
        return createTmMessage(request.chatId, request.messageId,
                text, createButtons(request.path, buttons));
    }

    private BotApiMethod<?> processRoom(CommandRequest request, Room room) {

        if (request.hasNext()) {
            String next = request.next();
            try {
                heatingService.incExpectedTemp(room, Double.parseDouble(next));
            } catch (NumberFormatException e) {
                String text = "Unknown command: " + next;
                return createTmMessage(request.chatId, request.messageId, text);
            }
        }

        String roomName = room.name().toLowerCase(Locale.ROOT);
        String text = String.format("Temp %s: %1.1f%s", roomName, heatingService.expectedTemp(room), CELSIUS);
        InlineKeyboardMarkup buttons = createButtons(
                request.createPathRemoving("-0.5", "+0.5", "-1", "+1"),
                List.of("-0.5", "+0.5", "-1", "+1", "set", "back"),
                2
        );
        return createTmMessage(request.chatId, request.messageId, text, buttons);
    }


    private BotApiMethod<?> processConfig(CommandRequest request) {

        if (request.hasNext()) {
            String config = request.next();
            switch (config) {
                case "restart" -> heatingService.sendAction("restart", 0, TELEGRAM);
                case "mixer" -> {
                    return processMixer(request);
                }
            }
        }

        String text = "Настройки:\n";

        Map<String, String> buttons = new LinkedHashMap<>();
        buttons.put("restart", "restart");
        buttons.put("mixer", "mixer: " + heatingService.mixerPosition);
        buttons.put("back", "\uD83D\uDD19 Назад");
        return createTmMessage(request.chatId, request.messageId, text, createButtons(List.of("config"), buttons));
    }

    private BotApiMethod<?> processMixer(CommandRequest request) {

        List<Integer> values = List.of(-120, -60, -30, -15, 15, 30);

        String commandText = "";
        if (request.hasNext()) {
            String command = request.next();
            Integer value = values.stream().filter(integer -> command.equals(integer.toString())).findFirst().orElse(0);
            heatingService.sendAction("mixer-move", value, TELEGRAM);
            commandText = "Принято " + command + "! ";
        }

        String text = commandText + "Mixer: " + heatingService.mixerPosition;

        Map<String, String> buttons = new LinkedHashMap<>();
        for (Integer value : values) {
            buttons.put(value.toString(), value.toString());
        }
        buttons.put("back", "\uD83D\uDD19 Назад");
        return createTmMessage(request.chatId, request.messageId, text, createButtons(List.of("config", "mixer"), buttons));
    }

}
