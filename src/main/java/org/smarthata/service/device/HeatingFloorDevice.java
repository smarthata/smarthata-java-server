package org.smarthata.service.device;

import org.smarthata.service.message.*;
import org.springframework.stereotype.Service;

import static org.smarthata.service.message.EndpointType.SYSTEM;
import static org.smarthata.service.message.EndpointType.TM;

@Service
public class HeatingFloorDevice extends AbstractSmarthataMessageListener {

    private Integer floorTemp = 30;

    public HeatingFloorDevice(SmarthataMessageBroker messageBroker) {
        super(messageBroker);
    }

    public int getFloorTemp() {
        return floorTemp;
    }

    public void setFloorTemp(Integer floorTemp) {
        this.floorTemp = floorTemp;
        sendTempToBroker(floorTemp.toString());
    }

    public void incFloorTemp() {
        floorTemp++;
        sendTempToBroker(floorTemp.toString());
    }

    public void decFloorTemp() {
        floorTemp--;
        sendTempToBroker(floorTemp.toString());
    }

    private void sendTempToBroker(String floorTemp) {
        SmarthataMessage message = new SmarthataMessage("/heating/floor/in", floorTemp, TM);
        messageBroker.broadcastSmarthataMessage(message);
    }

    @Override
    public void receiveSmarthataMessage(SmarthataMessage message) {
        if (message.getPath().equals("/heating/floor/in")) {
            floorTemp = Integer.valueOf(message.getText());
        }
    }

    @Override
    public EndpointType getEndpointType() {
        return SYSTEM;
    }
}
