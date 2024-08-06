package org.smarthata.alice.service

import org.slf4j.LoggerFactory
import org.smarthata.alice.model.smarthome.Device
import org.smarthata.alice.model.smarthome.FloatState
import org.smarthata.alice.model.smarthome.Property
import org.smarthata.alice.model.smarthome.TempParameter
import org.smarthata.service.WeatherService
import org.smarthata.service.device.Room
import org.smarthata.service.device.heating.HeatingService
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AliceTempDevicesProvider(
    private var heatingService: HeatingService,
    private var weatherService: WeatherService,
) : AliceDevicesProvider(TEMP_PREFIX) {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun devices() =
        listOf(
            createDevice(deviceId = "bedroom"),
            createDevice(deviceId = "bathroom"),
            createDevice(deviceId = "garage"),
            createDevice(deviceId = "workshop"),
            createDevice(deviceId = "street"),
        )

    override fun query(device: Device): Device {
        logger.info("Query for device: $device")
        val deviceId = device.id.removePrefix(TEMP_PREFIX)
        return createDevice(deviceId, fillState = true)
    }

    override fun action(device: Device): Device? {
        logger.info("Action for device: $device")
        return null
    }

    private fun createDevice(
        deviceId: String,
        fillState: Boolean = false,
    ): Device {
        val room = Room.getFromRoomCode(deviceId)
        return Device(
            id = TEMP_PREFIX + deviceId,
            name = "Датчик температуры",
            room = room.rusName,
            type = "devices.types.sensor.climate",
            properties = listOf(Property(
                type = "devices.properties.float",
                retrievable = true,
                reportable = false,
                parameters = TempParameter(),
                state = if (fillState) floatState(room) else null,
                lastUpdated = LocalDateTime.now(),
            ))
        )
    }

    private fun floatState(room: Room) =
        FloatState("temperature",
            if (room == Room.STREET)
                weatherService.getLastStreetTemp()
            else
                heatingService.actualTemp(room)
        )

    companion object {
        private const val TEMP_PREFIX = "temp-"
    }
}
