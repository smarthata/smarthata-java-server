package org.smarthata.service.tm.command;

import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;


@Service
public class MessagesCommand extends AbstractCommand {

    private static final String MESSAGES = "messages";

    public MessagesCommand() {
        super(MESSAGES);
    }

    @Override
    public BotApiMethod<?> answer(CommandRequest request) {
        String text = Strings.join(request.getPath().iterator(), ',');
        return aSimpleSendMessage(request.getChatId(), text).build();
    }
}
