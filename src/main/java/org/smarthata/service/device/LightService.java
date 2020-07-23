package org.smarthata.service.device;

import lombok.extern.slf4j.Slf4j;
import org.smarthata.service.message.AbstractSmarthataMessageListener;
import org.smarthata.service.message.EndpointType;
import org.smarthata.service.message.SmarthataMessage;
import org.smarthata.service.message.SmarthataMessageBroker;
import org.springframework.stereotype.Service;

import static org.smarthata.service.message.EndpointType.ALICE;

@Slf4j
@Service
public class LightService extends AbstractSmarthataMessageListener {

    private String action;

    protected LightService(SmarthataMessageBroker messageBroker) {
        super(messageBroker);
    }

    public String getLight(String room) {
        log.debug("Get light room = {}", room);
        return action;
    }

    public void setLight(String room, String action) {
        log.info("Switch light room = {}, action = {}", room, action);

        this.action = action;

        String message = String.format("{\"%s\":\"%s\"}", room, action);
        sendToBroker(message);
    }

    private void sendToBroker(String text) {
        SmarthataMessage message = new SmarthataMessage("/light/in", text, ALICE);
        messageBroker.broadcastSmarthataMessage(message);
    }

    @Override
    public void receiveSmarthataMessage(SmarthataMessage message) {

    }

    @Override
    public EndpointType getEndpointType() {
        return EndpointType.SYSTEM;
    }
}
