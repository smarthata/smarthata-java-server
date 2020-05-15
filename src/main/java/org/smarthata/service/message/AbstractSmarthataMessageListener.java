package org.smarthata.service.message;

public abstract class AbstractSmarthataMessageListener implements SmarthataMessageListener {

    protected final SmarthataMessageBroker messageBroker;

    protected AbstractSmarthataMessageListener(SmarthataMessageBroker messageBroker) {
        this.messageBroker = messageBroker;
        messageBroker.register(this);
    }

}
