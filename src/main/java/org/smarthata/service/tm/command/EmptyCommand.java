package org.smarthata.service.tm.command;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import java.util.List;

@Order(100)
@Service
public class EmptyCommand implements Command {
    @Override
    public boolean isProcessed(final String name) {
        return true;
    }

    @Override
    public BotApiMethod answer(final List<String> path, final String chatId, final Integer messageId) {
        return null;
    }
}
