package org.smarthata.service.tm.command;

import org.smarthata.service.device.HeatingBedroomDevice;
import org.smarthata.service.device.HeatingFloorDevice;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Order(1)
@Service
public class HeatingCommand extends AbstractCommand {

    private static final String HEATING = "heating";
    private static final String CELSIUS = "°C";

    private final HeatingFloorDevice heatingFloorDevice;
    private final HeatingBedroomDevice heatingBedroomDevice;

    public HeatingCommand(HeatingFloorDevice heatingFloorDevice, HeatingBedroomDevice heatingBedroomDevice) {
        super(HEATING);
        this.heatingFloorDevice = heatingFloorDevice;
        this.heatingBedroomDevice = heatingBedroomDevice;
    }


    @Override
    public BotApiMethod<?> answer(final List<String> path, final String chatId, final Integer messageId) {

        if (path.isEmpty()) {
            String text = "Device:";
            Map<String, String> buttons = Map.of("floor", "floor: " + heatingFloorDevice.getFloorTemp() + CELSIUS,
                    "bedroom", "bedroom: " + heatingBedroomDevice.getBedroomTemp() + CELSIUS);
            return createTmMessage(chatId, messageId, text, createButtons(path, buttons));
        }

        String device = path.remove(0);
        switch (device) {
            case "floor":
                return processFloorDevice(path, chatId, messageId);
            case "bedroom":
                return processBedroomDevice(path, chatId, messageId);
            default:
                String text = "Unknown device";
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

        String text = String.format("Base floor temp: %d°C", heatingFloorDevice.getFloorTemp());
        InlineKeyboardMarkup buttons = createButtons(singletonList("floor"), "inc", "dec", "back");
        return createTmMessage(chatId, messageId, text, buttons);
    }

    private BotApiMethod<?> processBedroomDevice(List<String> path, String chatId, Integer messageId) {
        if (!path.isEmpty()) {
            String operation = path.remove(0);
            switch (operation) {
                case "inc":
                    heatingBedroomDevice.incBedroomTemp();
                    break;
                case "dec":
                    heatingBedroomDevice.decBedroomTemp();
                    break;
                case "back":
                    return answer(emptyList(), chatId, messageId);
                default:
                    String text = "Unknown command: " + operation;
                    return createTmMessage(chatId, messageId, text);
            }
        }

        String text = String.format("Bedroom temp: %d°C", heatingBedroomDevice.getBedroomTemp());
        InlineKeyboardMarkup buttons = createButtons(singletonList("bedroom"), "inc", "dec", "back");
        return createTmMessage(chatId, messageId, text, buttons);
    }

}
