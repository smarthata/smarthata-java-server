package org.smarthata.service.tm.command;

import org.smarthata.service.device.HeatingDevice;
import org.smarthata.service.device.Room;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

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
                default:
                    String text = "Unknown house";
                    return createTmMessage(request.getChatId(), request.getMessageId(), text);
            }
        }

        String text = "Выберите помещение:";
        Map<String, String> buttons = Map.of(
                "house", "Дом",
                "garage", "Гараж",
                "back", "back"
        );
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
        String v1 = "Первый этаж: " + heatingDevice.getTemp(FLOOR) + CELSIUS;
        String v2 = "Спальня: " + heatingDevice.getTemp(BEDROOM) + CELSIUS;
        String v3 = "Ванная: " + heatingDevice.getTemp(BATHROOM) + CELSIUS;
        Map<String, String> buttons = Map.of(
                "floor", v1,
                "bedroom", v2,
                "bathroom", v3,
                "back", "back"
        );

        return createTmMessage(request.getChatId(), request.getMessageId(),
                text, createButtons(request.getPath(), buttons, 2));
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
        String v1 = "Гараж: " + heatingDevice.getTemp(GARAGE) + CELSIUS;
        String v2 = "Мастерская: " + heatingDevice.getTemp(WORKSHOP) + CELSIUS;
        Map<String, String> buttons = Map.of(
                "garage", v1,
                "workshop", v2,
                "back", "back"
        );
        return createTmMessage(request.getChatId(), request.getMessageId(),
                text, createButtons(request.getPath(), buttons, 2));
    }

    private BotApiMethod<?> processRoom(CommandRequest request, Room room) {

        if (request.hasNext()) {
            String next = request.next();
            try {
                heatingDevice.incTemp(room, Double.parseDouble(next));
            } catch (NumberFormatException e) {
                String text = "Unknown command: " + next;
                return createTmMessage(request.getChatId(), request.getMessageId(), text);
            }
        }

        String text = String.format("Temp %s: %1.1f°C", room.name().toLowerCase(Locale.ROOT),
                heatingDevice.getTemp(room));
        InlineKeyboardMarkup buttons = createButtons(request.getPathRemoving("-1", "-0.5", "+0.5", "+1"), "-1", "-0.5", "+0.5", "+1", "back");
        return createTmMessage(request.getChatId(), request.getMessageId(), text, buttons);
    }

}
