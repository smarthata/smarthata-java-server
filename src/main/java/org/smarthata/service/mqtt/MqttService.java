package org.smarthata.service.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smarthata.service.message.SmarthataMessage;
import org.smarthata.service.message.SmarthataMessageBroker;
import org.smarthata.service.message.SmarthataMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

import static org.smarthata.service.message.SmarthataMessage.SOURCE_MQTT;

@Service
public class MqttService implements IMqttMessageListener, SmarthataMessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(MqttService.class);
    public static final String HEATING_TEMP_FLOOR_REQUEST = "/heating/temp/floor/request";

    @Autowired
    private IMqttClient mqttClient;

    @Autowired
    private SmarthataMessageBroker messageBroker;


    @Scheduled(fixedRate = 60_000)
    public void sendTime() {
        publishMessageToMqtt("/date-time", new Date().toString());
    }

    @Scheduled(cron = "0 0 21 * * *")
    public void turnOffHeating() {
        publishMessageToMqtt(HEATING_TEMP_FLOOR_REQUEST, "20");
    }

    @Scheduled(cron = "0 0 7 * * *")
    public void turnOnHeating() {
        publishMessageToMqtt(HEATING_TEMP_FLOOR_REQUEST, "27");
    }

    @Scheduled(cron = "0 0 12 * * *")
    public void turnOnHeating25() {
        publishMessageToMqtt(HEATING_TEMP_FLOOR_REQUEST, "25");
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        LOG.info("messageArrived: {}, {}", topic, mqttMessage);
        String text = new String(mqttMessage.getPayload());
        SmarthataMessage message = new SmarthataMessage(topic, text, SOURCE_MQTT);
        messageBroker.broadcastSmarthataMessage(message);
    }

    @Override
    public void receiveSmarthataMessage(SmarthataMessage message) {
        if (!SOURCE_MQTT.equalsIgnoreCase(message.getSource())) {
            publishMessageToMqtt(message.getPath(), message.getText());
        }
    }

    private void publishMessageToMqtt(String topic, String message) {
        try {
            LOG.info("Try to send message to mqtt: topic [{}], message [{}]", topic, message);
            mqttClient.publish(topic, new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            LOG.error("Failed to send message: {}", e.getMessage(), e);
        }
    }
}
