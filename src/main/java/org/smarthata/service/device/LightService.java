package org.smarthata.service.device;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.smarthata.service.message.AbstractSmarthataMessageListener;
import org.smarthata.service.message.EndpointType;
import org.smarthata.service.message.SmarthataMessage;
import org.smarthata.service.message.SmarthataMessageBroker;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static org.smarthata.service.message.EndpointType.ALICE;

@Slf4j
@Service
public class LightService extends AbstractSmarthataMessageListener {

    private final ObjectMapper objectMapper;

    private Map<String, Boolean> actions = new HashMap<>();

    protected LightService(SmarthataMessageBroker messageBroker, ObjectMapper objectMapper) {
        super(messageBroker);
        this.objectMapper = objectMapper;
    }

    public Boolean getLight(String room) {
        log.debug("Get light room = {}", room);
        return actions.get(room);
    }

    @SneakyThrows
    public void setLight(String room, String action) {
        log.info("Switch light room = {}, action = {}", room, action);

        actions.put(room, "1".equals(action));

        Map<String, Object> map = Map.of("room", room, "state", actions.get(room));

        String message = objectMapper.writeValueAsString(map);
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
