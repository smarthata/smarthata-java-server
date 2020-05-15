package org.smarthata.service.tm.command;

import javax.validation.constraints.NotNull;

public abstract class AbstractCommand implements Command {

    private final String command;

    public AbstractCommand(@NotNull String command) {
        this.command = command;
    }

    @Override
    public String getCommand() {
        return command;
    }
}
