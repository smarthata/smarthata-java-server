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

    override fun endpointType(): EndpointType = EndpointType.MQTT

    private fun findLastMessage(topic: String): Optional<String> {
        val lastMessage = lastMessages[topic]
        return if (lastMessage == null || DateUtils.isDateAfter(lastMessage.dateTime, Duration.ofMinutes(5))) {
            Optional.empty()
        } else Optional.of(lastMessage.message)
    }

    fun findLastMessageAsDouble(topic: String): Optional<Double> =
        findLastMessage(topic).map { s: String -> s.toDouble() }

    private fun findLastMessageAsMap(topic: String): Optional<Map<String, Any>> {
        val json = findLastMessage(topic)
        return Optional.of(
            if (json.isEmpty) mapOf()
            else
                try {

                    objectMapper.readValue(json.get(), object : TypeReference<Map<String, Any>>() {})
                } catch (e: JsonProcessingException) {
                    logger.error("Failed to parse json: {}", json, e)
                    throw RuntimeException(e)
                }
        )
    }

    fun findLastMessageFieldFromJson(topic: String, field: String): Optional<Any> {
        val optional = findLastMessageAsMap(topic)
        if (optional.isPresent) {
            val map = optional.get()
            if (map.containsKey(field)) {
                return Optional.ofNullable(map[field])
            }
        }
        return Optional.empty()
    }
}