package org.smarthata.service.tm.command;

import org.smarthata.service.message.SmarthataMessage;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.smarthata.service.message.SmarthataMessage.SOURCE_TM;

@Order(1)
@Service
public class Heating extends AbstractCommand {

    private static final String HEATING = "heating";

    private static final List<String> ROOMS = asList("Кухня", "Зал", "Прихожая");
    private static final List<String> ROOM_OPERATIONS = asList("+", "-", "⇐");
    private static final Map<String, Byte> ROOMS_MAP = createRoomMap();

    private static Integer floorTemp = 25;

    public Heating() {
        super(HEATING);
    }

    private static Map<String, Byte> createRoomMap() {
        Map<String, Byte> map = new HashMap<>();
        for (String room : ROOMS) {
            map.put(room, (byte) 18);
        }
        return map;
    }


    @Override
    public BotApiMethod<?> answer(final List<String> path, final String chatId, final Integer messageId) {

        if (path.isEmpty()) {
            String text = String.format("Floor temp: %s°C", floorTemp);
            InlineKeyboardMarkup buttons = createButtons("inc", "dec");
            return createTmMessage(chatId, messageId, text, buttons);
        }

        String operation = path.remove(0);
        switch (operation) {
            case "inc":
                floorTemp++;
                sendTempToBroker();
                break;
            case "dec":
                floorTemp--;
                sendTempToBroker();
                break;
            default:
                String text = "Unknown command";
                InlineKeyboardMarkup buttons = createButtons("inc", "dec");
                return createTmMessage(chatId, messageId, text, buttons);
        }

        return answer(emptyList(), chatId, messageId);
    }

    private BotApiMethod<?> createTmMessage(final String chatId, final Integer messageId, final String text, final InlineKeyboardMarkup buttons) {
        if (messageId == null) {
            return aSimpleSendMessage(chatId, text, buttons);
        } else {
            return anEditMessageText(chatId, text, buttons, messageId);
        }
    }

    private void sendTempToBroker() {
        SmarthataMessage message = new SmarthataMessage("/heating/floor/in", floorTemp.toString(), SOURCE_TM);
        messageBroker.broadcastSmarthataMessage(message);
    }

    private BotApiMethod<?> roomLogic(List<String> path, String chatId, Integer messageId) {
        String room = path.remove(0);
        Byte temp = ROOMS_MAP.get(room);
        if (temp == null) {
            return aSimpleSendMessage(chatId, "Комната не найдена: " + room);
        }

        if (!path.isEmpty()) {
            if ("+".equals(path.get(0))) {
                ROOMS_MAP.put(room, ++temp);
            } else if ("-".equals(path.get(0))) {
                ROOMS_MAP.put(room, --temp);
            } else if ("⇐".equals(path.get(0))) {
                return answer(emptyList(), chatId, messageId);
            }
        }

        String text = String.format("%s temp: %s°C", room, temp);
        if (messageId == null) {
            SendMessage message = new SendMessage();
            message.setReplyMarkup(createPlusMinusButtons(room));
            message.setText(text);
            return message;
        } else {
            return anEditMessageText(chatId, text, createPlusMinusButtons(room), messageId);
        }
    }

    private static BotApiMethod anEditMessageText(String chatId, String text, InlineKeyboardMarkup roomButtons, Integer messageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setReplyMarkup(roomButtons);
        message.setText(text);
        return message;
    }


    private InlineKeyboardMarkup createRoomButtons() {
        return new InlineKeyboardMarkup().setKeyboard(singletonList(
                ROOMS.stream()
                        .map(this::createButton)
                        .collect(Collectors.toList())));
    }

    private InlineKeyboardMarkup createButtons(String... buttons) {
        List<InlineKeyboardButton> floor = Arrays.stream(buttons)
                .map(button -> createButton(button, button))
                .collect(Collectors.toList());
        return new InlineKeyboardMarkup().setKeyboard(singletonList(floor));
    }

    private InlineKeyboardMarkup createPlusMinusButtons(String text) {
        List<InlineKeyboardButton> list = ROOM_OPERATIONS.stream()
                .map(num -> createButton(num, text, num))
                .collect(Collectors.toList());
        return new InlineKeyboardMarkup().setKeyboard(singletonList(list));
    }

    private InlineKeyboardButton createButton(String room) {
        String text = String.format("%s (%s°C)", room, ROOMS_MAP.get(room));
        return createButton(text, room);
    }

    private InlineKeyboardButton createButton(String text, String... path) {
        return new InlineKeyboardButton()
                .setText(text)
                .setCallbackData("/" + HEATING + "/" + String.join("/", path));
    }
}
