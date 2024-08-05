package org.smarthata.service.device;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smarthata.service.message.AbstractSmarthataMessageListener;
import org.smarthata.service.message.EndpointType;
import org.smarthata.service.message.SmarthataMessage;
import org.smarthata.service.message.SmarthataMessageBroker;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LightService extends AbstractSmarthataMessageListener {

    private final ObjectMapper objectMapper;

    public final Map<String, Boolean> lightState = new ConcurrentHashMap<>();

    protected LightService(SmarthataMessageBroker messageBroker, ObjectMapper objectMapper) {
        super(messageBroker);
        this.objectMapper = objectMapper;
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Boolean lightState(String room) {
        return lightState.getOrDefault(room, false);
    }

    public synchronized void updateLight(String room, boolean newState, EndpointType source) {
        try {
            logger.info("IN Switch light room = {}, newState = {}, currentState = {}", room, newState, lightState.get(room));
            lightState.put(room, newState);

            Map<String, Object> map = Map.of("room", room, "state", newState);
            sendToBroker(objectMapper.writeValueAsString(map), source);
            logger.info("OUT Switch light room = {}, newState = {}", room, newState);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void enableLightTemporary(String room, long seconds, EndpointType source) {
        try {
            logger.info("IN enable light temporary room = {}", room);

            lightState.put(room, true);

            Map<String, Object> map = Map.of(
                    "room", room,
                    "state", true,
                    "time", seconds);
            sendToBroker(objectMapper.writeValueAsString(map), source);

            logger.info("OUT enable light temporary room = {}", room);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendToBroker(String text, EndpointType source) {
        messageBroker.broadcast(new SmarthataMessage("/light/in", text, source));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void receiveSmarthataMessage(SmarthataMessage message) {
        if (message.path.equals("/light/state")) {
            try {
                Map<String, Boolean> map = objectMapper.readValue(message.text, Map.class);
                lightState.putAll(map);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public EndpointType endpointType() {
        return EndpointType.SYSTEM;
    }
}
