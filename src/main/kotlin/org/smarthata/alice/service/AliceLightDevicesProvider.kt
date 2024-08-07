package org.smarthata.alice.service

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
class AliceLightDevicesProvider(
    private val lightService: LightService,
) : AliceDevicesProvider("light-") {

    override fun devices() =
        lightService.lightState.map { createDevice(deviceId = it.key) }

    override fun action(device: Device): Device {
        logger.info("Action for device: $device")

        val deviceId = device.id.removePrefix(prefix)

        val onOff = device.capabilities.firstOrNull { it.type == DEVICES_CAPABILITIES_ON_OFF }

        return if (onOff?.state?.value != null) {
            val value = onOff.state!!.value as Boolean
            logger.info("Changing light $deviceId to state $value")
            lightService.updateLight(deviceId, value, ALICE)

            createDevice(deviceId, fillState = true, actionResult = ActionResult(status = "DONE"))
        } else {
            createDevice(
                deviceId,
                fillState = true,
                actionResult = ActionResult(
                    errorCode = "UNKNOWN",
                    errorMessage = "Could not find on_off capability"
                )
            )
        }
    }

    override fun createDevice(
        deviceId: String,
        fillState: Boolean,
        actionResult: ActionResult?,
    ): Device {
        val room = Room.getFromRoomCode(deviceId.replace("-night", ""))
        return Device(
            id = prefix + deviceId,
            name = if (deviceId.contains("night")) "Ночник" else "Свет",
            room = room.rusName,
            type = "devices.types.light",
            capabilities = listOf(
                OnOffCapability(
                    state = if (fillState) lightState(deviceId, actionResult) else null
                )
            )
        )
    }

    private fun lightState(room: String, actionResult: ActionResult?) = BooleanState(
        instance = "on",
        value = lightService.lightState(room),
        actionResult = actionResult,
    )

}
