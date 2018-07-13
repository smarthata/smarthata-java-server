package org.smarthata.service.tm.command;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Order(1)
@Service
public class Heating implements Command {

    private static final String HEATING = "heating";

    private static final List<String> ROOMS = asList("Кухня", "Зал", "Прихожая");
    private static final List<String> ROOM_OPERATIONS = asList("+", "-", "⇐");
    private static final Map<String, Byte> map = createMap();

    private static Map<String, Byte> createMap() {
        Map<String, Byte> map = new HashMap<>();
        for (String room : ROOMS) {
            map.put(room, (byte) 18);
        }
        return map;
    }


    @Override
    public boolean isProcessed(final String name) {
        return HEATING.equalsIgnoreCase(name);
    }

    @Override
    public BotApiMethod answer(final List<String> path, final String chatId, final Integer messageId) {

        if (path.isEmpty()) {
            if (messageId == null) {
                return aSimpleSendMessage(chatId, "Выберите комнату:", createRoomButtons());
            } else {
                return anEditMessageText(messageId, createRoomButtons(), "Выберите комнату:");
            }
        }


        String room = path.get(0);
        Byte temp = map.get(room);
        if (temp == null) {
            return aSimpleSendMessage(chatId, "Комната не найдена: " + room);
        }

        path.remove(0);
        if (!path.isEmpty()) {
            if ("+".equals(path.get(0))) {
                map.put(room, ++temp);
            } else if ("-".equals(path.get(0))) {
                map.put(room, --temp);
            } else if ("⇐".equals(path.get(0))) {
                return answer(emptyList(), null, messageId);
            }
        }

        String text = String.format("%s температура: %s°C", room, temp);
        if (messageId == null) {
            SendMessage message = new SendMessage();
            message.setReplyMarkup(createPlusMinusButtons(room));
            message.setText(text);
            return message;
        } else {
            return anEditMessageText(messageId, createPlusMinusButtons(room), text);
        }
    }

    private static BotApiMethod anEditMessageText(final Integer messageId, final InlineKeyboardMarkup roomButtons, final String s) {
        EditMessageText message = new EditMessageText();
        message.setMessageId(messageId);
        message.setReplyMarkup(roomButtons);
        message.setText(s);
        return message;
    }


    private InlineKeyboardMarkup createRoomButtons() {
        return new InlineKeyboardMarkup().setKeyboard(singletonList(
                ROOMS.stream()
                        .map(this::createButton)
                        .collect(Collectors.toList())));
    }

    private InlineKeyboardMarkup createPlusMinusButtons(final String room) {
        return new InlineKeyboardMarkup().setKeyboard(singletonList(
                ROOM_OPERATIONS.stream()
                        .map(num -> createButton(room, num))
                        .collect(Collectors.toList())));
    }

    private InlineKeyboardButton createButton(final String room) {
        String text = String.format("%s (%s°C)", room, map.get(room));
        return new InlineKeyboardButton()
                .setText(text)
                .setCallbackData("/" + HEATING + "/" + room);
    }

    private InlineKeyboardButton createButton(final String room, final String num) {
        return new InlineKeyboardButton()
                .setText(num)
                .setCallbackData("/" + HEATING + "/" + room + "/" + num);
    }
}
