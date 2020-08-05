package org.smarthata.service.tm.command;

import org.smarthata.service.device.HeatingFloorDevice;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Order(1)
@Service
public class HeatingCommand extends AbstractCommand {

    private static final String HEATING = "heating";
    private static final List<String> rooms = List.of("floor", "bedroom");

    private final HeatingFloorDevice heatingFloorDevice;

    public HeatingCommand(HeatingFloorDevice heatingFloorDevice) {
        super(HEATING);
        this.heatingFloorDevice = heatingFloorDevice;
    }


    @Override
    public BotApiMethod<?> answer(final List<String> path, final String chatId, final Integer messageId) {

        if (path.isEmpty()) {
            String text = "Device:";
            InlineKeyboardMarkup buttons = createButtons(path, rooms);
            return createTmMessage(chatId, messageId, text, buttons);
        }

        String device = path.remove(0);
        switch (device) {
            case "floor":
                return processFloorDevice(path, chatId, messageId);
            case "bedroom":
                String text = "Bedroom is not ready";
                return createTmMessage(chatId, messageId, text);
            default:
                text = "Unknown device";
                return createTmMessage(chatId, messageId, text);
        }

    }

    private BotApiMethod<?> processFloorDevice(List<String> path, String chatId, Integer messageId) {
        if (!path.isEmpty()) {
            String operation = path.remove(0);
            switch (operation) {
                case "inc":
                    heatingFloorDevice.incFloorTemp();
                    break;
                case "dec":
                    heatingFloorDevice.decFloorTemp();
                    break;
                case "back":
                    return answer(emptyList(), chatId, messageId);

                default:
                    String text = "Unknown command: " + operation;
                    return createTmMessage(chatId, messageId, text);
            }
        }

        String text = String.format("Base floor temp: %s°C", heatingFloorDevice.getFloorTemp());
        InlineKeyboardMarkup buttons = createButtons(singletonList("floor"), "inc", "dec", "back");
        return createTmMessage(chatId, messageId, text, buttons);
    }

}
