package org.smarthata.service.message;

public interface SmarthataMessageListener {

    void receiveSmarthataMessage(SmarthataMessage message);

    EndpointType getEndpointType();

}
