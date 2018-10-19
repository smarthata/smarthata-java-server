package org.smarthata.service.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SmarthataMessageBroker {

    private static final Logger LOG = LoggerFactory.getLogger(SmarthataMessageBroker.class);

    @Autowired
    private List<SmarthataMessageListener> listeners;

    public void broadcastSmarthataMessage(SmarthataMessage message) {
        LOG.info("Broadcasting message: {}", message);
        listeners.forEach(listener -> listener.receiveSmarthataMessage(message));
    }

}
