package org.smarthata.service.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class MqttService implements IMqttMessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(MqttService.class);

    @Autowired
    private IMqttClient mqttClient;

    @Scheduled(fixedRate = 60_000)
    public void sendTime() {
        publishMessage("/date-time", new Date().toString());
    }

    @Scheduled(cron = "0 0 21 * * *")
    public void turnOffHeating() {
        publishMessage("/heating/temp/floor/request", "20");
    }

    @Scheduled(cron = "0 0 7 * * *")
    public void turnOnHeating() {
        publishMessage("/heating/temp/floor/request", "27");
    }

    @Scheduled(cron = "0 0 12 * * *")
    public void turnOnHeating25() {
        publishMessage("/heating/temp/floor/request", "25");
    }

    private void publishMessage(String topic, String message) {
        try {
            LOG.info("Try to send message to mqtt: [{}] {}", topic, message);
            mqttClient.publish(topic, new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            LOG.error("Failed to send message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        LOG.info("messageArrived: {}, {}", s, mqttMessage);
    }
}
