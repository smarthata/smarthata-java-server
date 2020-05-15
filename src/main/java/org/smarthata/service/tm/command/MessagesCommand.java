package org.smarthata.service.tm.command;

import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import java.util.List;


@Service
public class MessagesCommand extends AbstractCommand {

    private static final String MESSAGES = "messages";

    public MessagesCommand() {
        super(MESSAGES);
    }

    @Override
    public BotApiMethod<?> answer(final List<String> path, final String chatId, final Integer messageId) {
        String text = Strings.join(path.iterator(), ',');
        return aSimpleSendMessage(chatId, text);
    }
}
