package org.smarthata.service.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smarthata.service.message.AbstractSmarthataMessageListener;
import org.smarthata.service.message.EndpointType;
import org.smarthata.service.message.SmarthataMessage;
import org.smarthata.service.message.SmarthataMessageBroker;
import org.smarthata.service.message.SmarthataMessageListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import static org.smarthata.service.message.EndpointType.MQTT;

@Service
public class MqttService extends AbstractSmarthataMessageListener implements IMqttMessageListener,
        SmarthataMessageListener {

    private final IMqttClient mqttClient;

    public MqttService(IMqttClient mqttClient, SmarthataMessageBroker messageBroker) {
        super(messageBroker);
        this.mqttClient = mqttClient;
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @EventListener(ApplicationReadyEvent.class)
    public void subscribe() {
        logger.info("Subscribing");
        try {
            mqttClient.subscribe("/#", 1, this);
        } catch (MqttException e) {
            logger.error("Error on mqtt subscribe", e);
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        String text = new String(mqttMessage.getPayload());
        logger.debug("messageArrived: [{}], [{}]", topic, text);
        messageBroker.broadcast(new SmarthataMessage(topic, text, MQTT));
    }

    @Override
    public void receiveSmarthataMessage(SmarthataMessage message) {
        publishMessageToMqtt(message.path, message.text, message.retained);
    }

    @Override
    public EndpointType endpointType() {
        return MQTT;
    }


    private void publishMessageToMqtt(String topic, String message, boolean retained) {
        try {
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setRetained(retained);
            checkConnection();
            if (mqttClient.isConnected()) {
                mqttClient.publish(topic, mqttMessage);
                logger.debug("Message sent to mqtt: topic [{}], message [{}]", topic, message);
            } else {
                logger.warn("Message is not sent, MQTT is not connected");
            }
        } catch (MqttException e) {
            logger.error("Failed to send message: {}", e.getMessage(), e);
        }
    }

    private void checkConnection() throws MqttException {
        if (!mqttClient.isConnected()) {
            logger.warn("Mqtt is not connected. Trying to reconnect");
            mqttClient.connect();
            logger.warn("Mqtt connected: {}", mqttClient.isConnected());
            if (mqttClient.isConnected()) {
                subscribe();
            }
        }
    }
}
