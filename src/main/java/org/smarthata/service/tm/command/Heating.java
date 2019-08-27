package org.smarthata.service.tm.command;

import org.smarthata.service.message.SmarthataMessage;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.smarthata.service.message.EndpointType.TM;

@Order(1)
@Service
public class Heating extends AbstractCommand {

    private static final String HEATING = "heating";

    private static Integer floorTemp = 25;

    public Heating() {
        super(HEATING);
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
                return createTmMessage(chatId, messageId, text, null);
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
        SmarthataMessage message = new SmarthataMessage("/heating/floor/in", floorTemp.toString(), TM);
        messageBroker.broadcastSmarthataMessage(message);
    }

    private static BotApiMethod anEditMessageText(String chatId, String text, InlineKeyboardMarkup roomButtons, Integer messageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setReplyMarkup(roomButtons);
        message.setText(text);
        return message;
    }

    private InlineKeyboardMarkup createButtons(String... buttons) {
        List<InlineKeyboardButton> floor = Arrays.stream(buttons)
                .map(button -> createButton(button, button))
                .collect(Collectors.toList());
        return new InlineKeyboardMarkup().setKeyboard(singletonList(floor));
    }

    private InlineKeyboardButton createButton(String text, String... path) {
        return new InlineKeyboardButton()
                .setText(text)
                .setCallbackData("/" + HEATING + "/" + String.join("/", path));
    }
}
