package org.smarthata.service.device;

import org.smarthata.service.message.*;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

import static org.smarthata.service.message.EndpointType.*;

@Service
public class HeatingFloorDevice extends AbstractSmarthataMessageListener {

    public static final String QUEUE_TEMP = "/heating/floor/in";
    private AtomicInteger temp = new AtomicInteger(30);

    public HeatingFloorDevice(SmarthataMessageBroker messageBroker) {
        super(messageBroker);
    }

    public int getTemp() {
        return temp.get();
    }

    public void setTemp(Integer temp) {
        this.temp = new AtomicInteger(temp);
        sendTempToBroker(temp.toString());
    }

    public void incTemp() {
        temp.incrementAndGet();
        sendTempToBroker(temp.toString());
    }

    public void decTemp() {
        temp.decrementAndGet();
        sendTempToBroker(temp.toString());
    }

    private void sendTempToBroker(String floorTemp) {
        SmarthataMessage message = new SmarthataMessage(QUEUE_TEMP, floorTemp, USER, MQTT, true);
        messageBroker.broadcastSmarthataMessage(message);
    }

    @Override
    public void receiveSmarthataMessage(SmarthataMessage message) {
        if (message.getPath().equals(QUEUE_TEMP)) {
            temp = new AtomicInteger(Integer.parseInt(message.getText()));
        }
    }

    @Override
    public EndpointType getEndpointType() {
        return SYSTEM;
    }
}
