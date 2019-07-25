package org.smarthata.service.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SmarthataMessageBroker {

    private static final Logger LOG = LoggerFactory.getLogger(SmarthataMessageBroker.class);

    private final List<SmarthataMessageListener> listeners = new ArrayList<>();

    public void broadcastSmarthataMessageRetained(SmarthataMessage message) {
        message.setRetained(true);
        broadcastSmarthataMessage(message);
    }
    public void broadcastSmarthataMessage(SmarthataMessage message) {
        LOG.debug("Broadcasting message: {}", message);

        listeners.stream()
                .filter(listener -> isNeedSendMessage(message, listener.getEndpointType()))
                .forEach(listener -> listener.receiveSmarthataMessage(message));
    }

    public void register(SmarthataMessageListener listener) {
        listeners.add(listener);
    }

    private boolean isNeedSendMessage(SmarthataMessage message, EndpointType endpointType) {
        EndpointType destination = message.getDestination();
        return endpointType != message.getSource() && (endpointType == destination || destination == null);
    }

}
