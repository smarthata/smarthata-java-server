package org.smarthata.service.mqtt

import org.eclipse.paho.client.mqttv3.IMqttClient
import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.slf4j.LoggerFactory
import org.smarthata.service.message.AbstractSmarthataMessageListener
import org.smarthata.service.message.EndpointType
import org.smarthata.service.message.SmarthataMessage
import org.smarthata.service.message.SmarthataMessageBroker
import org.smarthata.service.message.SmarthataMessageListener
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class MqttService(
    private val mqttClient: IMqttClient,
    messageBroker: SmarthataMessageBroker,
) :
    AbstractSmarthataMessageListener(messageBroker), IMqttMessageListener {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @EventListener(ApplicationReadyEvent::class)
    fun subscribe() {
        logger.info("Subscribing")
        try {
            mqttClient.subscribe("/#", 1, this)
        } catch (e: MqttException) {
            logger.error("Error on mqtt subscribe", e)
        }
    }

    override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
        val text = String(mqttMessage.payload)
        logger.debug("messageArrived: [{}], [{}]", topic, text)
        messageBroker.broadcast(SmarthataMessage(topic, text, EndpointType.MQTT))
    }

    override fun receiveSmarthataMessage(message: SmarthataMessage) {
        publishMessageToMqtt(message.path, message.text, message.retained)
    }

    override fun endpointType(): EndpointType = EndpointType.MQTT

    private fun publishMessageToMqtt(topic: String, message: String, retained: Boolean) {
        try {
            val mqttMessage = MqttMessage(message.toByteArray())
            mqttMessage.isRetained = retained
            checkConnection()
            if (mqttClient.isConnected) {
                mqttClient.publish(topic, mqttMessage)
                logger.debug("Message sent to mqtt: topic [{}], message [{}]", topic, message)
            } else {
                logger.warn("Message is not sent, MQTT is not connected")
            }
        } catch (e: MqttException) {
            logger.error("Failed to send message: {}", e.message, e)
        }
    }

    @Throws(MqttException::class)
    private fun checkConnection() {
        if (!mqttClient.isConnected) {
            logger.warn("Mqtt is not connected. Trying to reconnect")
            mqttClient.connect()
            logger.warn("Mqtt connected: {}", mqttClient.isConnected)
            if (mqttClient.isConnected) {
                subscribe()
            }
        }
    }
}