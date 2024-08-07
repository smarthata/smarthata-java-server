package org.smarthata.alice.service

import org.smarthata.alice.model.smarthome.ActionResult
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
) : AliceDevicesProvider("temp-") {

    override fun devices() =
        listOf(
            createDevice(deviceId = "bedroom"),
            createDevice(deviceId = "bathroom"),
            createDevice(deviceId = "garage"),
            createDevice(deviceId = "workshop"),
            createDevice(deviceId = "street"),
        )

    override fun action(device: Device): Device? {
        logger.info("Action for device: $device")
        return null
    }

    override fun createDevice(
        deviceId: String,
        fillState: Boolean,
        actionResult: ActionResult?,
    ): Device {
        val room = Room.getFromRoomCode(deviceId)
        return Device(
            id = prefix + deviceId,
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

}
