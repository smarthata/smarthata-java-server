package org.smarthata.service.tm.command;

import org.smarthata.service.device.HeatingDevice;
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


@Order(1)
@Service
public class HeatingCommand extends AbstractCommand {

    private static final String HEATING = "heating";
    private static final String CELSIUS = "°C";

    private final HeatingDevice heatingDevice;

    public HeatingCommand(HeatingDevice heatingDevice) {
        super(HEATING);
        this.heatingDevice = heatingDevice;
    }


    @Override
    public BotApiMethod<?> answer(CommandRequest request) {

        if (request.hasNext()) {
            String house = request.next();
            switch (house) {
                case "house":
                    return processHouse(request);
                case "garage":
                    return processGarage(request);
                case "config":
                    return processConfig(request);
                default:
                    String text = "Unknown house";
                    return createTmMessage(request.getChatId(), request.getMessageId(), text);
            }
        }

        String text = "Выберите помещение:";
        Map<String, String> buttons = new LinkedHashMap<>();
        buttons.put("house", "Дом");
        buttons.put("garage", "Гараж");
        buttons.put("config", "Настройка");
        buttons.put("back", "Назад");
        return createTmMessage(request.getChatId(), request.getMessageId(),
                text, createButtons(request.getPath(), buttons, 2));
    }

    private BotApiMethod<?> processHouse(CommandRequest request) {

        if (request.hasNext()) {
            String room = request.next();
            switch (room) {
                case "floor":
                    return processRoom(request, FLOOR);
                case "bedroom":
                    return processRoom(request, BEDROOM);
                case "bathroom":
                    return processRoom(request, BATHROOM);
                default:
                    String text = "Unknown room";
                    return createTmMessage(request.getChatId(), request.getMessageId(), text);
            }
        }

        String text = "Выберите помещение:";
        String v1 = showTempInRoom("Первый этаж", FLOOR);
        String v2 = showTempInRoom("Спальня", BEDROOM);
        String v3 = showTempInRoom("Ванная", BATHROOM);
        Map<String, String> buttons = new LinkedHashMap<>();
        buttons.put("floor", v1);
        buttons.put("bedroom", v2);
        buttons.put("bathroom", v3);
        buttons.put("back", "Назад");

        return createTmMessage(request.getChatId(), request.getMessageId(),
                text, createButtons(request.getPath(), buttons));
    }

    private String showTempInRoom(String roomName, Room room) {
//        if (heatingDevice.isActualTempExists(room)) {
//            return String.format("%s: %.1f%s/%.1f%s", roomName, heatingDevice.getActualTemp(room), CELSIUS,
//                    heatingDevice.getExpectedTemp(room), CELSIUS);
//        }
        return String.format("%s: %.1f%s", roomName, heatingDevice.getExpectedTemp(room), CELSIUS);
    }

    private BotApiMethod<?> processGarage(CommandRequest request) {

        if (request.hasNext()) {
            String device = request.next();
            switch (device) {
                case "garage":
                    return processRoom(request, GARAGE);
                case "workshop":
                    return processRoom(request, WORKSHOP);
                default:
                    String text = "Unknown device";
                    return createTmMessage(request.getChatId(), request.getMessageId(), text);
            }
        }

        String text = "Выберите помещение:";
        String v1 = showTempInRoom("Гараж", GARAGE);
        String v2 = showTempInRoom("Мастерская", WORKSHOP);
        Map<String, String> buttons = new LinkedHashMap<>();
        buttons.put("garage", v1);
        buttons.put("workshop", v2);
        buttons.put("back", "Назад");
        return createTmMessage(request.getChatId(), request.getMessageId(),
                text, createButtons(request.getPath(), buttons));
    }

    private BotApiMethod<?> processRoom(CommandRequest request, Room room) {

        if (request.hasNext()) {
            String next = request.next();
            try {
                heatingDevice.incExpectedTemp(room, Double.parseDouble(next));
            } catch (NumberFormatException e) {
                String text = "Unknown command: " + next;
                return createTmMessage(request.getChatId(), request.getMessageId(), text);
            }
        }

        String roomName = room.name().toLowerCase(Locale.ROOT);
        String text = String.format("Temp %s: %1.1f%s", roomName, heatingDevice.getExpectedTemp(room), CELSIUS);
        InlineKeyboardMarkup buttons = createButtons(
                request.getPathRemoving("-0.5", "+0.5", "-1", "+1"),
                List.of("-0.5", "+0.5", "-1", "+1", "set", "back"),
                2
        );
        return createTmMessage(request.getChatId(), request.getMessageId(), text, buttons);
    }


    private BotApiMethod<?> processConfig(CommandRequest request) {

        if (request.hasNext()) {
            String config = request.next();
            switch (config) {
                case "restart" -> heatingDevice.sendAction("restart", 0);
                case "mixer" -> {
                    return processMixer(request);
                }
            }
        }

        String text = "Настройки:\n";

        Map<String, String> buttons = new LinkedHashMap<>();
        buttons.put("restart", "restart");
        buttons.put("mixer", "mixer: " + heatingDevice.mixerPosition);
        buttons.put("back", "Назад");
        return createTmMessage(request.getChatId(), request.getMessageId(), text, createButtons(List.of("config"), buttons));
    }

    private BotApiMethod<?> processMixer(CommandRequest request) {

        List<Integer> values = List.of(-120, -60, -30, -15, 15, 30);

        String commandText = "";
        if (request.hasNext()) {
            String command = request.next();
            Integer value = values.stream().filter(integer -> command.equals(integer.toString())).findFirst().orElse(0);
            heatingDevice.sendAction("mixer-move", value);
            commandText = "Принято " + command + "! ";
        }

        String text = commandText + "Mixer: " + heatingDevice.mixerPosition;

        Map<String, String> buttons = new LinkedHashMap<>();
        for (Integer value : values) {
            buttons.put(value.toString(), value.toString());
        }
        buttons.put("back", "Назад");
        return createTmMessage(request.getChatId(), request.getMessageId(), text, createButtons(List.of("config", "mixer"), buttons));
    }

}
