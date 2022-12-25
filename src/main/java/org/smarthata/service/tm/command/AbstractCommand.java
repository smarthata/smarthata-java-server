package org.smarthata.service.tm.command;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractCommand implements Command {

    public static final int BUTTONS_IN_ROW = 1;
    private final String command;

    public AbstractCommand(@NotNull String command) {
        this.command = command;
    }

    @Override
    public String getCommand() {
        return command;
    }

    protected InlineKeyboardMarkup createButtons(List<String> path, List<String> buttons) {
        return createButtons(path, buttons, 1);
    }

    protected InlineKeyboardMarkup createButtons(List<String> path, List<String> buttons, int buttonsInRow) {
        List<InlineKeyboardButton> keyboards = buttons.stream()
                .map(button -> createButton(button, path, button))
                .collect(Collectors.toList());
        return InlineKeyboardMarkup.builder()
                .keyboard(createKeyboard(keyboards, buttonsInRow))
                .build();
    }

    protected InlineKeyboardMarkup createButtons(List<String> path, Map<String, String> buttons) {
        return createButtons(path, buttons, BUTTONS_IN_ROW);
    }

    protected InlineKeyboardMarkup createButtons(List<String> path, Map<String, String> buttons, int buttonsInRow) {
        List<InlineKeyboardButton> keyboards = buttons.entrySet().stream()
                .map(button -> createButton(button.getValue(), path, button.getKey()))
                .collect(Collectors.toList());
        return InlineKeyboardMarkup.builder()
                .keyboard(createKeyboard(keyboards, buttonsInRow))
                .build();
    }

    private List<List<InlineKeyboardButton>> createKeyboard(List<InlineKeyboardButton> keyboards, int buttonsInRow) {
        List<List<InlineKeyboardButton>> lists = new ArrayList<>();
        Iterator<InlineKeyboardButton> iterator = keyboards.iterator();
        while (iterator.hasNext()) {
            List<InlineKeyboardButton> line = new ArrayList<>(buttonsInRow);
            while (iterator.hasNext() && line.size() < buttonsInRow) {
                line.add(iterator.next());
            }
            lists.add(line);
        }
        return lists;
    }

    protected InlineKeyboardButton createButton(String text, List<String> path, String pathSuffix) {

        List<String> fullPath = new ArrayList<>(path.size() + 2);
        fullPath.add(command);
        fullPath.addAll(path);
        if ("back".equals(pathSuffix)) {
            fullPath.remove(fullPath.size() - 1);
            if (fullPath.isEmpty()) {
                fullPath.add("start");
            }
        } else {
            fullPath.add(pathSuffix);
        }

        String callbackData = "/" + String.join("/", fullPath);

        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }
}
