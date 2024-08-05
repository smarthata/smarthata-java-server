package org.smarthata.alice.service

import org.slf4j.LoggerFactory
import org.smarthata.alice.model.smarthome.ActionResult
import org.smarthata.alice.model.smarthome.BooleanState
import org.smarthata.alice.model.smarthome.DEVICES_CAPABILITIES_ON_OFF
import org.smarthata.alice.model.smarthome.Device
import org.smarthata.alice.model.smarthome.OnOffCapability
import org.smarthata.service.device.LightService
import org.smarthata.service.device.Room
import org.smarthata.service.message.EndpointType.ALICE
import org.springframework.stereotype.Service

@Service
class AliceLightDevices(
    private val lightService: LightService,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun devices() =
        lightService.lightState.map { createDevice(deviceId = it.key) }

    fun query(device: Device): Device {
        logger.info("Query for device: $device")
        val room = device.id.removePrefix(LIGHT_PREFIX)
        return createDevice(room, fillState = true)
    }

    fun action(device: Device): Device? {
        logger.info("Action for device: $device")

        val deviceId = device.id.removePrefix(LIGHT_PREFIX)

        val onOff = device.capabilities.firstOrNull { it.type == DEVICES_CAPABILITIES_ON_OFF }

        return if (onOff?.state?.value != null) {
            val value = onOff.state!!.value as Boolean
            logger.info("Changing light $deviceId to state $value")
            lightService.updateLight(deviceId, value, ALICE)

            createDevice(deviceId, actionResult = ActionResult(status = "DONE"))
        } else {
            createDevice(
                deviceId, actionResult = ActionResult(
                errorCode = "UNKNOWN",
                errorMessage = "Could not find on_off capability"
            )
            )
        }
    }

    private fun createDevice(
        deviceId: String,
        fillState: Boolean = false,
        actionResult: ActionResult? = null,
    ): Device {
        val room = Room.getFromRoomCode(deviceId.replace("-night", ""))
        return Device(
            id = LIGHT_PREFIX + deviceId,
            name = if (deviceId.contains("night")) "Ночник" else "Свет",
            room = room.rusName,
            type = "devices.types.light",
            capabilities = if (actionResult == null) listOf(
                OnOffCapability(
                    state = if (fillState) lightState(deviceId) else null
                )
            ) else emptyList(),
            actionResult = actionResult
        )
    }

    private fun lightState(room: String) = BooleanState(
        instance = "on",
        value = lightService.lightState(room),
    )

    companion object {
        const val LIGHT_PREFIX = "light-"
    }
}
