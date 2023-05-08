package org.smarthata.service.device;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.smarthata.service.message.AbstractSmarthataMessageListener;
import org.smarthata.service.message.EndpointType;
import org.smarthata.service.message.SmarthataMessage;
import org.smarthata.service.message.SmarthataMessageBroker;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.smarthata.service.message.EndpointType.USER;

@Slf4j
@Service
public class LightService extends AbstractSmarthataMessageListener {

    private final ObjectMapper objectMapper;

    private final Map<String, Boolean> lightState = new ConcurrentHashMap<>();

    protected LightService(SmarthataMessageBroker messageBroker, ObjectMapper objectMapper) {
        super(messageBroker);
        this.objectMapper = objectMapper;
    }

    public Map<String, Boolean> getLightState() {
        return lightState;
    }

    public Boolean getLight(String room) {
        log.debug("IN Get light room = {}", room);
        Boolean state = lightState.getOrDefault(room, false);
        log.debug("OUT Get light room = {}, state = {}", room, state);
        return state;
    }

    @SneakyThrows
    public synchronized void setLight(String room, String action) {
        log.info("IN Switch light room = {}, action = {}, currentState = {}", room, action, lightState.get(room));

        boolean newState = "1".equals(action) || "true".equals(action);
        lightState.put(room, newState);

        Map<String, Object> map = Map.of("room", room, "state", newState);
        sendToBroker(objectMapper.writeValueAsString(map));

        log.info("OUT Switch light room = {}, newState = {}", room, newState);
    }

    @SneakyThrows
    public void enableLightTemporary(String room, long seconds) {
        log.info("IN enable light temporary room = {}", room);

        lightState.put(room, true);

        Map<String, Object> map = Map.of(
                "room", room,
                "state", true,
                "time", seconds);
        sendToBroker(objectMapper.writeValueAsString(map));

        log.info("OUT enable light temporary room = {}", room);
    }

    private void sendToBroker(String text) {
        SmarthataMessage message = new SmarthataMessage("/light/in", text, USER);
        messageBroker.broadcastSmarthataMessage(message);
    }

    @SneakyThrows
    @Override
    @SuppressWarnings("unchecked")
    public void receiveSmarthataMessage(SmarthataMessage message) {
        if (message.getPath().equals("/light/state")) {
            Map<String, Boolean> map = objectMapper.readValue(message.getText(), Map.class);
            lightState.putAll(map);
        }
    }

    @Override
    public EndpointType getEndpointType() {
        return EndpointType.SYSTEM;
    }
}
