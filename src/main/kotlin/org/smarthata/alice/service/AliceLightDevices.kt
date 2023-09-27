package org.smarthata.alice.service

import org.slf4j.LoggerFactory
import org.smarthata.alice.model.smarthome.ActionResult
import org.smarthata.alice.model.smarthome.Device
import org.smarthata.alice.model.smarthome.OnOffCapability
import org.smarthata.alice.model.smarthome.State
import org.smarthata.service.device.LightService
import org.smarthata.service.message.EndpointType.ALICE
import org.springframework.stereotype.Service

@Service
class AliceLightDevices(
    private var lightService: LightService,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun devices() =
        lightService.lightState.map { createDeviceForRoom(room = it.key) }

    fun query(device: Device): Device {
        logger.info("Query for device: $device")
        val room = device.id.removePrefix(LIGHT_PREFIX)
        return createDeviceForRoom(room, fillState = true)
    }

    fun action(action: Device): Device? {
        logger.info("Action for device: $action")

        val room = action.id.removePrefix(LIGHT_PREFIX)

        val onOff = action.capabilities.firstOrNull { it.type == OnOffCapability().type }

        return if (onOff?.state?.value != null) {
            val value = onOff.state!!.value!!
            logger.info("Changing light $room to state $value")
            lightService.updateLight(room, value, ALICE)

            createDeviceForRoom(room, actionResult = ActionResult(status = "DONE"))
        } else {
            createDeviceForRoom(
                room, actionResult = ActionResult(
                    errorCode = "UNKNOWN",
                    errorMessage = "Could not find on_off capability"
                )
            )
        }
    }

    private fun createDeviceForRoom(room: String, fillState: Boolean = false, actionResult: ActionResult? = null): Device {
        val name = LightService.translations.getOrDefault(room, room)
        return Device(
            id = LIGHT_PREFIX + room,
            name = "Свет $name",
            room = name,
            type = "devices.types.light",
            capabilities = if (actionResult == null) listOf(
                OnOffCapability(
                    state = if (fillState)
                        State(
                            instance = "on",
                            value = lightService.lightState(room),
                        )
                    else
                        null
                )
            ) else emptyList(),
            actionResult = actionResult
        )
    }

    companion object {
        private const val LIGHT_PREFIX = "light-"
    }
}
