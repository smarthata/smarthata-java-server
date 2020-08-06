package org.smarthata.service.device;

import org.smarthata.service.message.AbstractSmarthataMessageListener;
import org.smarthata.service.message.EndpointType;
import org.smarthata.service.message.SmarthataMessage;
import org.smarthata.service.message.SmarthataMessageBroker;
import org.springframework.stereotype.Service;

import static org.smarthata.service.message.EndpointType.SYSTEM;
import static org.smarthata.service.message.EndpointType.TM;

@Service
public class HeatingBedroomDevice extends AbstractSmarthataMessageListener {

    private Integer bedroomTemp = 30;

    public HeatingBedroomDevice(SmarthataMessageBroker messageBroker) {
        super(messageBroker);
    }

    public int getBedroomTemp() {
        return bedroomTemp;
    }

    public void setBedroomTemp(Integer bedroomTemp) {
        this.bedroomTemp = bedroomTemp;
        sendTempToBroker(bedroomTemp.toString());
    }

    public void incBedroomTemp() {
        bedroomTemp++;
        sendTempToBroker(bedroomTemp.toString());
    }

    public void decBedroomTemp() {
        bedroomTemp--;
        sendTempToBroker(bedroomTemp.toString());
    }

    private void sendTempToBroker(String floorTemp) {
        SmarthataMessage message = new SmarthataMessage("/bedroom/in", floorTemp, TM);
        messageBroker.broadcastSmarthataMessage(message);
    }

    @Override
    public void receiveSmarthataMessage(SmarthataMessage message) {
        if (message.getPath().equals("/bedroom/in")) {
            bedroomTemp = Integer.valueOf(message.getText());
        }
    }

    @Override
    public EndpointType getEndpointType() {
        return SYSTEM;
    }
}
