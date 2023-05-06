package org.smarthata.service.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.smarthata.service.DateUtils;
import org.smarthata.service.message.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import static org.smarthata.service.message.EndpointType.MQTT;

@Slf4j
@Service
public class MqttService extends AbstractSmarthataMessageListener implements IMqttMessageListener, SmarthataMessageListener {

    private final IMqttClient mqttClient;

    @Autowired
    public MqttService(IMqttClient mqttClient, SmarthataMessageBroker messageBroker) {
        super(messageBroker);
        this.mqttClient = mqttClient;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void subscribe() {
        log.info("Subscribing");
        try {
            mqttClient.subscribe("/#", 1, this);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        String text = new String(mqttMessage.getPayload());
        log.debug("messageArrived: [{}], [{}]", topic, text);
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
            checkConnection();
            if (mqttClient.isConnected()) {
                mqttClient.publish(topic, mqttMessage);
                log.debug("Message sent to mqtt: topic [{}], message [{}]", topic, message);
            }
        } catch (MqttException e) {
            log.error("Failed to send message: {}", e.getMessage(), e);
        }
    }

    private void checkConnection() throws MqttException {
        if (!mqttClient.isConnected()) {
            log.warn("Mqtt is not connected. Trying to reconnect");
            mqttClient.connect();
            log.warn("Mqtt connected: {}", mqttClient.isConnected());
            if (mqttClient.isConnected()) {
                subscribe();
            }
        }
    }
}
