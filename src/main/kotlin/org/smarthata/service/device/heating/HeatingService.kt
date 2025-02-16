package org.smarthata.service.device.heating

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.smarthata.service.device.Room
import org.smarthata.service.message.AbstractSmarthataMessageListener
import org.smarthata.service.message.EndpointType
import org.smarthata.service.message.SmarthataMessage
import org.smarthata.service.message.SmarthataMessageBroker
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicInteger

@Service
class HeatingService(
    messageBroker: SmarthataMessageBroker,
    private val objectMapper: ObjectMapper,
) : AbstractSmarthataMessageListener(messageBroker) {
    private val map: Map<Room, HeatingDevice> = createMap()

    val mixerPosition: AtomicInteger = AtomicInteger(0)

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    private fun createMap() = mapOf(
        Room.HALL to HeatingDevice("/heating/floor", 30.0),
        Room.BEDROOM to HeatingDevice("/bedroom", 23.0, true),
        Room.GARAGE to HeatingDevice("/heating/garage/garage", 15.0),
        Room.WORKSHOP to HeatingDevice("/heating/garage/workshop", 20.0),
    )

    fun expectedTemp(room: Room) = map[room]!!.expectedTemp.get()

    fun actualTemp(room: Room) = map[room]?.actualTemp

    fun updateExpectedTemp(room: Room, temp: Double, source: EndpointType) {
        logger.info("Set temp [{}] for room [{}]", temp, room)
        map[room]?.apply {
            this.expectedTemp.set(temp)
        }?.also {
            saveTempToBroker(it, source)
        }
    }

    fun incExpectedTemp(room: Room, delta: Double) {
        logger.info("Inc temp [{}] for room [{}]", delta, room)
        map[room]?.apply {
            this.expectedTemp.getAndUpdate { value: Double -> value + delta }
        }?.also {
            saveTempToBroker(it, EndpointType.TELEGRAM)
        }
    }

    fun incExpectedNightTemp(room: Room, delta: Double) {
        logger.info("Inc night temp [{}] for room [{}]", delta, room)
        map[room]?.apply {
            this.expectedNightTemp.getAndUpdate { value: Double -> value + delta }
        }?.also {
            saveNightTempToBroker(it, EndpointType.TELEGRAM)
        }
    }

    fun isEnabled(room: Room) = map[room]?.enabled
        .also { logger.info("Get floor pomp for room {}", room) }

    fun expectedNightTemp(room: Room): Double = map[room]!!.expectedNightTemp.get()

    fun hasNightMode(room: Room) = map[room]!!.nightMode

    fun updateEnabled(room: Room, enabled: Boolean, source: EndpointType) {
        logger.info("Set enabled = [{}] for room {}", enabled, room)
        map[room]?.apply {
            this.enabled = enabled
        }?.also {
            saveEnabledToBroker(it, source)
        }
    }

    private fun saveTempToBroker(device: HeatingDevice, source: EndpointType) {
        messageBroker.broadcast(
            SmarthataMessage(device.expectedTempQueue, device.expectedTemp.toString(),
                source, EndpointType.MQTT, true))
    }

    private fun saveNightTempToBroker(device: HeatingDevice, source: EndpointType) {
        messageBroker.broadcast(
            SmarthataMessage(device.expectedNightTempQueue, device.expectedNightTemp.toString(),
                source, EndpointType.MQTT, true))
    }

    private fun saveEnabledToBroker(device: HeatingDevice, source: EndpointType) {
        messageBroker.broadcast(SmarthataMessage(device.enabledQueue,
            device.enabled.toString(),
            source, EndpointType.MQTT, true))
    }

    override fun receiveSmarthataMessage(message: SmarthataMessage) {
        try {
            if (message.path == "/heating/floor/mixer-position") {
                mixerPosition.set(message.text.toInt())
            } else {
                map.forEach { (room: Room, device: HeatingDevice) -> readInputMessage(message, room, device) }
            }
        } catch (e: Exception) {
            logger.error("Failed to read smarthata message {}", message, e)
        }
    }

    private fun readInputMessage(message: SmarthataMessage, room: Room, device: HeatingDevice) {
        when (message.path) {
            device.expectedTempQueue -> {
                device.expectedTemp.set(message.text.toDouble())
            }

            device.expectedNightTempQueue -> {
                device.expectedNightTemp.set(message.text.toDouble())
            }

            device.actualTempQueue -> {
                parseActualTemp(message.text)?.let { newActualTemp ->
                    logger.trace("Update room [{}] set actual temp [{}]", room, newActualTemp)
                    device.actualTemp = newActualTemp
                }
            }

            device.enabledQueue -> {
                device.enabled = message.text.toBoolean()
            }
        }
    }

    private fun parseActualTemp(json: String): Double? {
        return try {
            val typeRef = object : TypeReference<Map<Any, Any>>() {}
            val map: Map<Any, Any> = objectMapper.readValue(json, typeRef)
            if (map.containsKey("temp")) {
                (map["temp"] as Number).toDouble()
            } else null
        } catch (e: JsonProcessingException) {
            throw RuntimeException(e)
        }
    }

    override fun endpointType(): EndpointType = EndpointType.SYSTEM

    fun sendAction(action: String, source: EndpointType, value: Int? = null) {
        try {
            val map = mutableMapOf<String, Any>("action" to action)
            value?.let { map["value"] = value }
            val text = objectMapper.writeValueAsString(map)

            messageBroker.broadcast(SmarthataMessage("/heating/in/json", text, source))
        } catch (e: JsonProcessingException) {
            throw RuntimeException("Failed to write json", e)
        }
    }
}
