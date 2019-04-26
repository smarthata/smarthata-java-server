package org.smarthata.service.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smarthata.service.message.EndpointType;
import org.smarthata.service.message.SmarthataMessage;
import org.smarthata.service.message.SmarthataMessageBroker;
import org.smarthata.service.message.SmarthataMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.smarthata.service.message.EndpointType.MQTT;

@Service
public class MqttService implements IMqttMessageListener, SmarthataMessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(MqttService.class);

    private final IMqttClient mqttClient;
    private final SmarthataMessageBroker messageBroker;

    @Autowired
    public MqttService(IMqttClient mqttClient, SmarthataMessageBroker messageBroker) throws MqttException {
        this.mqttClient = mqttClient;
        this.messageBroker = messageBroker;
        messageBroker.register(this);
        mqttClient.subscribe("/#", 1, this);
    }


    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        String text = new String(mqttMessage.getPayload());
        LOG.debug("messageArrived: [{}], [{}]", topic, text);
        SmarthataMessage message = new SmarthataMessage(topic, text, MQTT);
        messageBroker.broadcastSmarthataMessage(message);
    }

    @Override
    public void receiveSmarthataMessage(SmarthataMessage message) {
        publishMessageToMqtt(message.getPath(), message.getText(), message.isRetained());
    }

    @Override
    public EndpointType getEndpointType() {
        return MQTT;
    }

    private void publishMessageToMqtt(String topic, String message, boolean retained) {
        try {
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setRetained(retained);
            mqttClient.publish(topic, mqttMessage);
            LOG.debug("Message sent to mqtt: topic [{}], message [{}]", topic, message);
        } catch (MqttException e) {
            LOG.error("Failed to send message: {}", e.getMessage(), e);
        }
    }
}
