package org.smarthata.config;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class MqttConfig {

    @Value("${mqtt.server-uri}")
    private String serverURI;
    @Value("${mqtt.username}")
    private String username;
    @Value("${mqtt.password}")
    private String password;

    @Bean
    public IMqttClient mqttClient() throws MqttException {
        String clientId = UUID.randomUUID().toString();
        IMqttClient publisher = new MqttClient(serverURI, clientId, new MemoryPersistence());

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(false);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        publisher.connect(options);

        return publisher;
    }

}
