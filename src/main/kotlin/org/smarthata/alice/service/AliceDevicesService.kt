package org.smarthata.alice.service

import org.slf4j.LoggerFactory
import org.smarthata.alice.model.smarthome.Device
import org.smarthata.alice.model.smarthome.DevicesAction
import org.smarthata.alice.model.smarthome.DevicesQuery
import org.smarthata.alice.model.smarthome.DevicesPayload
import org.smarthata.alice.model.smarthome.DevicesResponse
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AliceDevicesService(
    private val aliceDeviceProviders: List<AliceDevicesProvider>,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun devices(): List<Device> = aliceDeviceProviders.flatMap { it.devices() }

    fun devicesQuery(devicesQuery: DevicesQuery) = DevicesResponse(
        requestId = UUID.randomUUID().toString(),
        payload = DevicesPayload(
            devices = devicesQuery.devices.mapNotNull { device ->
                logger.info("Query for device: $device")
                aliceDeviceProviders.firstOrNull { aliceDevices -> device.id.startsWith(aliceDevices.prefix) }
                    ?.query(device)
            }
        )
    )

    fun devicesAction(action: DevicesAction) = DevicesResponse(
        requestId = UUID.randomUUID().toString(),
        payload = DevicesPayload(
            devices = action.payload.devices.mapNotNull { device ->
                logger.info("Action for device: $device")
                aliceDeviceProviders.firstOrNull { aliceDevices -> device.id.startsWith(aliceDevices.prefix) }
                    ?.action(device)
            })
    )
}
