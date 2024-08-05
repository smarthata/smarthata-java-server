package org.smarthata.alice.service

import org.slf4j.LoggerFactory
import org.smarthata.alice.model.smarthome.Device
import org.smarthata.alice.model.smarthome.DeviceAction
import org.smarthata.alice.model.smarthome.DeviceQuery
import org.smarthata.alice.model.smarthome.DevicesPayload
import org.smarthata.alice.model.smarthome.DevicesResponse
import org.smarthata.alice.service.AliceLightDevices.Companion.LIGHT_PREFIX
import org.smarthata.alice.service.AliceTempDevices.Companion.TEMP_PREFIX
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AliceDevicesService(
    private var lightDevices: AliceLightDevices,
    private var tempDevices: AliceTempDevices,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun devices(): List<Device> = lightDevices.devices() + tempDevices.devices()

    fun devicesQuery(deviceQuery: DeviceQuery) = DevicesResponse(
        requestId = UUID.randomUUID().toString(),
        payload = DevicesPayload(
            devices = deviceQuery.devices.mapNotNull {
                when {
                    it.id.startsWith("light") -> {
                        lightDevices.query(it)
                    }

                    it.id.startsWith("temp") -> {
                        tempDevices.query(it)
                    }

                    else -> {
                        null
                    }
                }
            }
        )
    )

    fun devicesAction(action: DeviceAction) = DevicesResponse(
        requestId = UUID.randomUUID().toString(),
        payload = DevicesPayload(
            devices = action.payload.devices.mapNotNull { device ->
                logger.info("Action for device: $device")
                when {
                    device.id.startsWith(LIGHT_PREFIX) -> {
                        lightDevices.action(device)
                    }

                    device.id.startsWith(TEMP_PREFIX) -> {
                        tempDevices.action(device)
                    }

                    else -> {
                        null
                    }
                }
            })
    )
}
