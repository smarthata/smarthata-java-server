package org.smarthata.service.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SmarthataMessageBroker {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final List<SmarthataMessageListener> listeners = new ArrayList<>();

    public void broadcast(SmarthataMessage message) {
        logger.debug("Broadcasting message: {}", message);

        listeners.stream()
                .filter(listener -> isNeedSendMessage(message, listener.endpointType()))
                .forEach(listener -> listener.receiveSmarthataMessage(message));
    }

    public void register(SmarthataMessageListener listener) {
        listeners.add(listener);
    }

    private boolean isNeedSendMessage(SmarthataMessage message, EndpointType endpointType) {
        EndpointType destination = message.destination;
        return endpointType != message.source && (endpointType == destination || destination == null);
    }

}
