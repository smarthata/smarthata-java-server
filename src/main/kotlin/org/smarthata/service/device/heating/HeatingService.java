package org.smarthata.service.device.heating;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smarthata.service.device.Room;
import org.smarthata.service.message.AbstractSmarthataMessageListener;
import org.smarthata.service.message.EndpointType;
import org.smarthata.service.message.SmarthataMessage;
import org.smarthata.service.message.SmarthataMessageBroker;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.smarthata.service.device.Room.*;
import static org.smarthata.service.message.EndpointType.*;


@Service
public class HeatingService extends AbstractSmarthataMessageListener {

    private final ObjectMapper objectMapper;
    public HeatingService(SmarthataMessageBroker messageBroker, ObjectMapper objectMapper) {
        super(messageBroker);
        this.objectMapper = objectMapper;
    }

    private final Map<Room, HeatingDevice> map = createMap();
    public final AtomicInteger mixerPosition = new AtomicInteger(0);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private HashMap<Room, HeatingDevice> createMap() {
        return new HashMap<>() {{
            put(FLOOR, new HeatingDevice("/heating/floor", new AtomicReference<>(30.0)));
            put(BEDROOM, new HeatingDevice("/bedroom", new AtomicReference<>(23.0)));
            put(BATHROOM, new HeatingDevice("/bathroom", new AtomicReference<>(23.0)));
            put(GARAGE, new HeatingDevice("/heating/garage/garage", new AtomicReference<>(15.0)));
            put(WORKSHOP, new HeatingDevice("/heating/garage/workshop", new AtomicReference<>(20.0)));
        }};
    }

    public double expectedTemp(Room room) {
        return map.get(room).expectedTemp.get();
    }

    public boolean isActualTempExists(Room room) {
        return map.get(room).actualTemp != null;
    }

    public double actualTemp(Room room) {
        return map.get(room).actualTemp.get();
    }

    public void updateExpectedTemp(Room room, Double temp) {
        logger.info("Set temp [{}] for room [{}]", temp, room);
        HeatingDevice device = map.get(room);
        device.expectedTemp.set(temp);
        saveTempToBroker(device);
    }

    public synchronized void incExpectedTemp(Room room, double delta) {
        logger.info("Inc temp [{}] for room [{}]", delta, room);
        HeatingDevice device = map.get(room);
        device.expectedTemp.getAndUpdate(value -> value + delta);
        saveTempToBroker(device);
    }

    public int floorPomp(Room room) {
        logger.info("Get floor pomp for room {}", room);
        return map.get(room).enabled.get();
    }

    public void updateFloorPomp(Room room, String floorPomp) {
        logger.info("Set floor pomp [{}] for room {}", floorPomp, room);
        HeatingDevice device = map.get(room);
        device.enabled.set(Integer.parseInt(floorPomp));
        saveEnabledToBroker(device);
    }

    private void saveTempToBroker(HeatingDevice device) {
        messageBroker.broadcast(new SmarthataMessage(device.queueExpectedTemp, device.expectedTemp.toString(), TELEGRAM, MQTT, true));
    }

    private void saveEnabledToBroker(HeatingDevice device) {
        messageBroker.broadcast(new SmarthataMessage(device.queueEnabled, device.enabled.toString(), TELEGRAM, MQTT, true));
    }

    @Override
    public void receiveSmarthataMessage(SmarthataMessage message) {
        map.forEach((room, device) -> readInputMessage(message, room, device));
    }

    private void readInputMessage(SmarthataMessage message, Room room, HeatingDevice device) {
        String path = message.path;
        if (path.equals(device.queueExpectedTemp)) {
            device.expectedTemp.set(Double.parseDouble(message.text));
        } else if (path.equals(device.queueActualTemp)) {
            parseActualTemp(message, room, device);
        } else if (path.equals(device.queueEnabled)) {
            device.enabled.set(Integer.parseInt(message.text));
        } else if (path.equals("/heating/floor/mixer-position")) {
            mixerPosition.set(Integer.parseInt(message.text));
        }
    }

    @SuppressWarnings("unchecked")
    private void parseActualTemp(SmarthataMessage message, Room room, HeatingDevice device) {
        try {
            Map<Object, Object> map = objectMapper.readValue(message.text, Map.class);
            if (map.containsKey("temp")) {
                Double newActualTemp = (Double) map.get("temp");
                logger.trace("Update room [{}] set actual temp [{}]", room, newActualTemp);
                device.actualTemp.set(newActualTemp);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public EndpointType endpointType() {
        return SYSTEM;
    }

    public void sendAction(String action, int value, EndpointType source) {
        try {
            Map<String, Object> map = Map.of("action", action, "value", value);
            String text = objectMapper.writeValueAsString(map);

            messageBroker.broadcast(new SmarthataMessage("/heating/in/json", text, source));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
