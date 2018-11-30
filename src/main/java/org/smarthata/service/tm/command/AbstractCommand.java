package org.smarthata.service.tm.command;

import org.smarthata.service.message.SmarthataMessageBroker;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.NotNull;

public abstract class AbstractCommand implements Command {

    private final String command;

    public AbstractCommand(@NotNull String command) {
        this.command = command;
    }

    @Autowired
    protected SmarthataMessageBroker messageBroker;

    @Override
    public String getCommand() {
        return command;
    }
}
