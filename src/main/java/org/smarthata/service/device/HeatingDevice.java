package org.smarthata.service.device;

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
    private final String queue;
    private AtomicDouble temp;

    Device(String queue, AtomicDouble temp) {
        this.queue = queue;
        this.temp = temp;
    }
}


@Service
public class HeatingDevice extends AbstractSmarthataMessageListener {

    private final Map<Room, Device> map = createMap();

    public HeatingDevice(SmarthataMessageBroker messageBroker) {
        super(messageBroker);
    }

    private HashMap<Room, Device> createMap() {
        return new HashMap<>() {{
            put(FLOOR, new Device("/heating/floor/in", new AtomicDouble(30)));
            put(BEDROOM, new Device("/bedroom/in", new AtomicDouble(23)));
            put(BATHROOM, new Device("/bathroom/in", new AtomicDouble(23)));
            put(GARAGE, new Device("/heating/garage/garage/in", new AtomicDouble(15)));
            put(WORKSHOP, new Device("/heating/garage/workshop/in", new AtomicDouble(20)));
        }};
    }

    public double getTemp(Room room) {
        return map.get(room).getTemp().get();
    }

    public void setTemp(Room room, Double temp) {
        map.get(room).setTemp(new AtomicDouble(temp));
        sendTempToBroker(room);
    }

    public void incTemp(Room room, double delta) {
        map.get(room).getTemp().addAndGet(delta);
        sendTempToBroker(room);
    }

    private void sendTempToBroker(Room room) {
        Device device = map.get(room);
        SmarthataMessage message = new SmarthataMessage(device.getQueue(), device.getTemp().toString(), USER, MQTT, true);
        messageBroker.broadcastSmarthataMessage(message);
    }

    @Override
    public void receiveSmarthataMessage(SmarthataMessage message) {
        String path = message.getPath();
        map.values().stream()
                .filter(device -> path.equals(device.getQueue()))
                .forEach(device -> device.setTemp(new AtomicDouble(Double.parseDouble(message.getText()))));
    }

    @Override
    public EndpointType getEndpointType() {
        return SYSTEM;
    }
}
