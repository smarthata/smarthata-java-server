package org.smarthata.service.mqtt

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.smarthata.service.DateUtils
import org.smarthata.service.message.AbstractSmarthataMessageListener
import org.smarthata.service.message.EndpointType
import org.smarthata.service.message.SmarthataMessage
import org.smarthata.service.message.SmarthataMessageBroker
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

@Service
class MqttMessagesCache(
    messageBroker: SmarthataMessageBroker,
    private val objectMapper: ObjectMapper,
) : AbstractSmarthataMessageListener(messageBroker) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val lastMessages: ConcurrentHashMap<String, LastMessage> = ConcurrentHashMap()

    override fun receiveSmarthataMessage(message: SmarthataMessage) {
        lastMessages[message.path] = LastMessage(message.text, LocalDateTime.now())
    }

    override fun endpointType(): EndpointType = EndpointType.SYSTEM

    private fun findLastMessage(topic: String): String? =
        lastMessages[topic]?.let {
            if (DateUtils.isDateAfter(it.dateTime, Duration.ofMinutes(5))) null
            else it.message
        }

    fun findLastMessageAsDouble(topic: String): Double? =
        findLastMessage(topic)?.toDouble()

    private fun findLastMessageAsMap(topic: String): Map<String, Any> =
        findLastMessage(topic)?.let {
            try {
                objectMapper.readValue(it, object : TypeReference<Map<String, Any>>() {})
            } catch (e: JsonProcessingException) {
                logger.error("Failed to parse json: {}", it, e)
                throw RuntimeException(e)
            }
        } ?: mapOf()

    fun findLastMessageFieldFromJson(topic: String, field: String): Any? =
        findLastMessageAsMap(topic)[field]
}