package org.smarthata.service.device;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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


@Data
class Device {
    private final String queueActualTemp;
    private final String queueExpectedTemp;
    private final String queueEnabled;
    private final AtomicReference<Double> actualTemp = new AtomicReference<>(0.0);
    private final AtomicReference<Double> expectedTemp;
    private final AtomicInteger enabled = new AtomicInteger(1);

    Device(String baseQueue, AtomicReference<Double> expectedTemp) {
        this.queueActualTemp = baseQueue;
        this.queueExpectedTemp = baseQueue + "/in";
        this.queueEnabled = baseQueue + "/enabled";
        this.expectedTemp = expectedTemp;
    }
}


@Service
@Slf4j
public class HeatingDevice extends AbstractSmarthataMessageListener {

    private final Map<Room, Device> map = createMap();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public final AtomicInteger mixerPosition = new AtomicInteger(0);

    public HeatingDevice(SmarthataMessageBroker messageBroker) {
        super(messageBroker);
    }

    private HashMap<Room, Device> createMap() {
        return new HashMap<>() {{
            put(FLOOR, new Device("/heating/floor", new AtomicReference<Double>(30.0)));
            put(BEDROOM, new Device("/bedroom", new AtomicReference<Double>(23.0)));
            put(BATHROOM, new Device("/bathroom", new AtomicReference<Double>(23.0)));
            put(GARAGE, new Device("/heating/garage/garage", new AtomicReference<Double>(15.0)));
            put(WORKSHOP, new Device("/heating/garage/workshop", new AtomicReference<Double>(20.0)));
        }};
    }

    public double getExpectedTemp(Room room) {
        return map.get(room).getExpectedTemp().get();
    }

    public boolean isActualTempExists(Room room) {
        return map.get(room).getActualTemp() != null;
    }

    public double getActualTemp(Room room) {
        return map.get(room).getActualTemp().get();
    }

    public void setExpectedTemp(Room room, Double temp) {
        log.info("Set temp [{}] for room [{}]", temp, room);
        Device device = map.get(room);
        device.getExpectedTemp().set(temp);
        sendTempToBroker(device);
    }

    public synchronized void incExpectedTemp(Room room, double delta) {
        log.info("Inc temp [{}] for room [{}]", delta, room);
        Device device = map.get(room);
        device.getExpectedTemp().getAndUpdate(value -> value + delta);
        sendTempToBroker(device);
    }

    public int getFloorPomp(Room room) {
        log.info("Get floor pomp for room {}", room);
        return map.get(room).getEnabled().get();
    }

    public void setFloorPomp(Room room, String floorPomp) {
        log.info("Set floor pomp [{}] for room {}", floorPomp, room);
        Device device = map.get(room);
        device.getEnabled().set(Integer.parseInt(floorPomp));
        sendEnabledToBroker(device);
    }

    private void sendTempToBroker(Device device) {
        SmarthataMessage message = new SmarthataMessage(device.getQueueExpectedTemp(), device.getExpectedTemp().toString(), USER, MQTT, true);
        messageBroker.broadcastSmarthataMessage(message);
    }

    private void sendEnabledToBroker(Device device) {
        SmarthataMessage message = new SmarthataMessage(device.getQueueEnabled(), device.getEnabled().toString(), USER, MQTT, true);
        messageBroker.broadcastSmarthataMessage(message);
    }

    @Override
    public void receiveSmarthataMessage(SmarthataMessage message) {
        map.forEach((room, device) -> readInputMessage(message, room, device));
    }

    private void readInputMessage(SmarthataMessage message, Room room, Device device) {
        String path = message.getPath();
        if (path.equals(device.getQueueExpectedTemp())) {
            device.getExpectedTemp().set(Double.parseDouble(message.getText()));
        } else if (path.equals(device.getQueueActualTemp())) {
            parseActualTemp(message, room, device);
        } else if (path.equals(device.getQueueEnabled())) {
            device.getEnabled().set(Integer.parseInt(message.getText()));
        } else if (path.equals("/heating/floor/mixer-position")) {
            mixerPosition.set(Integer.parseInt(message.getText()));
        }
    }

    private void parseActualTemp(SmarthataMessage message, Room room, Device device) {
        try {
            Map<Object, Object> map = objectMapper.readValue(message.getText(), Map.class);
            if (map.containsKey("temp")) {
                Double newActualTemp = (Double) map.get("temp");
                log.trace("Update room [{}] set actual temp [{}]", room, newActualTemp);
                device.getActualTemp().set(newActualTemp);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public EndpointType getEndpointType() {
        return SYSTEM;
    }
}
