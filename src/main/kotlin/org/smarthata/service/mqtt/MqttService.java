package org.smarthata.service.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.smarthata.service.message.EndpointType.MQTT;

record LastMessage(
        String message,
        LocalDateTime dateTime
) {
}

@Slf4j
@Service
public class MqttService extends AbstractSmarthataMessageListener implements IMqttMessageListener, SmarthataMessageListener {

    private final IMqttClient mqttClient;

    private final Map<String, LastMessage> lastMessages = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    @Autowired
    public MqttService(IMqttClient mqttClient, SmarthataMessageBroker messageBroker, ObjectMapper objectMapper) {
        super(messageBroker);
        this.mqttClient = mqttClient;
        this.objectMapper = objectMapper;
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
        messageBroker.broadcastSmarthataMessage(new SmarthataMessage(topic, text, MQTT));
        lastMessages.put(topic, new LastMessage(text, LocalDateTime.now()));
    }

    @Override
    public void receiveSmarthataMessage(SmarthataMessage message) {
        publishMessageToMqtt(message.getPath(), message.getText(), message.isRetained());
    }

    @Override
    public EndpointType getEndpointType() {
        return MQTT;
    }

    public Optional<String> getLastMessage(String topic) {
        LastMessage lastMessage = lastMessages.get(topic);
        if (lastMessage == null || DateUtils.isDateAfter(lastMessage.dateTime(), Duration.ofMinutes(5))) {
            return Optional.empty();
        }
        return Optional.of(lastMessage.message());
    }

    public Optional<Double> getLastMessageAsDouble(String topic) {
        return getLastMessage(topic).map(Double::parseDouble);
    }

    @SuppressWarnings("unchecked")
    public Optional<Map<String, Object>> getLastMessageAsMap(String topic) {

        Optional<String> json = getLastMessage(topic);
        if (json.isEmpty()) return Optional.empty();

        try {
            return Optional.of(objectMapper.readValue(json.get(), Map.class));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse json: {}", json, e);
            throw new RuntimeException(e);
        }
    }
    public Optional<Object> getLastMessageFieldFromJson(String topic, String field) {
        Optional<Map<String, Object>> optional = getLastMessageAsMap(topic);
        if (optional.isPresent()) {
            Map<String, Object> map = optional.get();
            if (map.containsKey(field)) {
                return Optional.ofNullable(map.get(field));
            }
        }
        return Optional.empty();
    }

    private void publishMessageToMqtt(String topic, String message, boolean retained) {
        try {
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setRetained(retained);
            checkConnection();
            if (mqttClient.isConnected()) {
                mqttClient.publish(topic, mqttMessage);
                log.debug("Message sent to mqtt: topic [{}], message [{}]", topic, message);
            } else {
                log.warn("Message is not sent, MQTT is not connected");
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
