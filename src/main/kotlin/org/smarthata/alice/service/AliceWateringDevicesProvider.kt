package org.smarthata.alice.service

import org.slf4j.LoggerFactory
import org.smarthata.alice.model.smarthome.ActionResult
import org.smarthata.alice.model.smarthome.BooleanState
import org.smarthata.alice.model.smarthome.DEVICES_CAPABILITIES_ON_OFF
import org.smarthata.alice.model.smarthome.Device
import org.smarthata.alice.model.smarthome.OnOffCapability
import org.smarthata.service.device.Room
import org.smarthata.service.device.WateringService
import org.smarthata.service.message.EndpointType.ALICE
import org.springframework.stereotype.Service

@Service
class AliceWateringDevicesProvider(
    private val wateringService: WateringService,
) : AliceDevicesProvider(WATERING_PREFIX) {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun devices() =
        listOf(
            createDevice(deviceId = "1"),
            createDevice(deviceId = "2"),
            createDevice(deviceId = "3"),
            createDevice(deviceId = "4"),
            createDevice(deviceId = "5"),
        )

    override fun query(device: Device): Device {
        logger.info("Query for device: $device")
        val room = device.id.removePrefix(WATERING_PREFIX)
        return createDevice(room, fillState = true)
    }

    override fun action(device: Device): Device {
        logger.info("Action for device: $device")

        val deviceId = device.id.removePrefix(WATERING_PREFIX)

        val onOff = device.capabilities.firstOrNull { it.type == DEVICES_CAPABILITIES_ON_OFF }

        return if (onOff?.state?.value != null) {
            val value = onOff.state!!.value as Boolean
            logger.info("Changing watering $deviceId to state $value")
            wateringService.updateChannel(Integer.parseInt(deviceId), if (value) 1 else 0, ALICE)

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

    private fun createDevice(
        deviceId: String,
        fillState: Boolean = false,
        actionResult: ActionResult? = null,
    ): Device {
        return Device(
            id = WATERING_PREFIX + deviceId,
            name = rusName(deviceId),
            room = Room.STREET.rusName,
            type = "devices.types.openable.valve",
            capabilities = listOf(
                OnOffCapability(
                    state = if (fillState) wateringState(deviceId, actionResult) else null
                )
            )
        )
    }

    private fun rusName(deviceId: String) =
        when (deviceId) {
            "4" -> "Полив огорода"
            "5" -> "Капельный полив"
            else -> "Полив газона $deviceId"
        }


    private fun wateringState(deviceId: String, actionResult: ActionResult?) = BooleanState(
        instance = "on",
        value = wateringService.channelStates[deviceId.toInt()] == 1,
        actionResult = actionResult,
    )

    companion object {
        private const val WATERING_PREFIX = "watering-"
    }
}
