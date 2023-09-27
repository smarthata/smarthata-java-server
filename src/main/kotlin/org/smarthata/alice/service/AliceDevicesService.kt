package org.smarthata.alice.service

import org.slf4j.LoggerFactory
import org.smarthata.alice.model.smarthome.Device
import org.smarthata.alice.model.smarthome.DeviceAction
import org.smarthata.alice.model.smarthome.DeviceQuery
import org.smarthata.alice.model.smarthome.DevicesPayload
import org.smarthata.alice.model.smarthome.DevicesResponse
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AliceDevicesService(
    private var lightDevices: AliceLightDevices,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun getDevices(): List<Device> {
        return lightDevices.devices()
    }

    fun devicesQuery(deviceQuery: DeviceQuery): DevicesResponse {
        return DevicesResponse(
            requestId = UUID.randomUUID().toString(),
            payload = DevicesPayload(
                devices = deviceQuery.devices.mapNotNull {
                    if (it.id.startsWith("light")) {
                        lightDevices.query(it)
                    } else {
                        null
                    }
                }
            )
        )
    }

    fun devicesAction(deviceAction: DeviceAction): DevicesResponse {
        val actionPayload = deviceAction.payload
        return DevicesResponse(
            requestId = UUID.randomUUID().toString(),
            payload = DevicesPayload(
                devices = actionPayload.devices.mapNotNull { device ->
                    logger.info("Action for device: $device")
                    if (device.id.startsWith("light")) {
                        lightDevices.action(device)
                    } else {
                        null
                    }
                })
        )
    }
}
