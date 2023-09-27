package org.smarthata.config

import org.eclipse.paho.client.mqttv3.IMqttClient
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class MqttConfig(
    @Value("\${mqtt.server-uri}") private val serverURI: String,
    @Value("\${mqtt.username}") private val username: String,
    @Value("\${mqtt.password}") private val pass: String,
) {

    @Bean
    @Throws(MqttException::class)
    fun mqttClient(): IMqttClient {
        val clientId = UUID.randomUUID().toString()
        val publisher: IMqttClient = MqttClient(serverURI, clientId, MemoryPersistence())
        val options = MqttConnectOptions().apply {
            isAutomaticReconnect = false
            isCleanSession = true
            connectionTimeout = 10
            userName = username
            password = pass.toCharArray()
        }
        publisher.connect(options)
        return publisher
    }
}