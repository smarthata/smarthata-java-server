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

@Slf4j
@Service
public class LightService extends AbstractSmarthataMessageListener {
    public static final Map<String, String> translations = Map.of(
            "all", "Везде",
            "bathroom", "Ванная",
            "bedroom", "Спальня",
            "canopy", "Навес",
            "room-egor", "Детская Егора",
            "room-liza", "Детская Лизы",
            "stairs-night", "Ночник на лестнице",
            "stairs", "Лестница");

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
    public synchronized void setLight(String room, boolean newState, EndpointType source) {
        log.info("IN Switch light room = {}, newState = {}, currentState = {}", room, newState, lightState.get(room));
        lightState.put(room, newState);

        Map<String, Object> map = Map.of("room", room, "state", newState);
        sendToBroker(objectMapper.writeValueAsString(map), source);
        log.info("OUT Switch light room = {}, newState = {}", room, newState);
    }

    @SneakyThrows
    public void enableLightTemporary(String room, long seconds, EndpointType source) {
        log.info("IN enable light temporary room = {}", room);

        lightState.put(room, true);

        Map<String, Object> map = Map.of(
                "room", room,
                "state", true,
                "time", seconds);
        sendToBroker(objectMapper.writeValueAsString(map), source);

        log.info("OUT enable light temporary room = {}", room);
    }

    private void sendToBroker(String text, EndpointType source) {
        SmarthataMessage message = new SmarthataMessage("/light/in", text, source);
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
