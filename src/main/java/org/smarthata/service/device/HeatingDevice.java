package org.smarthata.service.device;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.AtomicDouble;
import lombok.Data;
import org.smarthata.service.message.AbstractSmarthataMessageListener;
import org.smarthata.service.message.EndpointType;
import org.smarthata.service.message.SmarthataMessage;
import org.smarthata.service.message.SmarthataMessageBroker;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static org.smarthata.service.device.Room.*;
import static org.smarthata.service.message.EndpointType.*;


@Data
class Device {
    private final String queueActualTemp;
    private final String queueExpectedTemp;
    private AtomicDouble actualTemp;
    private AtomicDouble expectedTemp;

    Device(String baseQueue, AtomicDouble expectedTemp) {
        this.queueActualTemp = baseQueue;
        this.queueExpectedTemp = baseQueue + "/in";
        this.expectedTemp = expectedTemp;
    }
}


@Service
public class HeatingDevice extends AbstractSmarthataMessageListener {

    private final Map<Room, Device> map = createMap();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public HeatingDevice(SmarthataMessageBroker messageBroker) {
        super(messageBroker);
    }

    private HashMap<Room, Device> createMap() {
        return new HashMap<>() {{
            put(FLOOR, new Device("/heating/floor", new AtomicDouble(30)));
            put(BEDROOM, new Device("/bedroom", new AtomicDouble(23)));
            put(BATHROOM, new Device("/bathroom", new AtomicDouble(23)));
            put(GARAGE, new Device("/heating/garage/garage", new AtomicDouble(15)));
            put(WORKSHOP, new Device("/heating/garage/workshop", new AtomicDouble(20)));
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
        map.get(room).setExpectedTemp(new AtomicDouble(temp));
        sendTempToBroker(room);
    }

    public void incExpectedTemp(Room room, double delta) {
        map.get(room).getExpectedTemp().addAndGet(delta);
        sendTempToBroker(room);
    }

    private void sendTempToBroker(Room room) {
        Device device = map.get(room);
        SmarthataMessage message = new SmarthataMessage(device.getQueueExpectedTemp(), device.getExpectedTemp().toString(), USER, MQTT, true);
        messageBroker.broadcastSmarthataMessage(message);
    }

    @Override
    public void receiveSmarthataMessage(SmarthataMessage message) {
        String path = message.getPath();
        map.values()
                .forEach(device -> {
                    if (path.equals(device.getQueueExpectedTemp())) {
                        device.setExpectedTemp(new AtomicDouble(Double.parseDouble(message.getText())));
                    } else if (path.equals(device.getQueueActualTemp())) {
                        try {
                            Map<Object, Object> map = objectMapper.readValue(message.getText(), Map.class);
                            AtomicDouble temp = new AtomicDouble((Double) map.get("temp"));
                            device.setActualTemp(temp);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public EndpointType getEndpointType() {
        return SYSTEM;
    }
}
