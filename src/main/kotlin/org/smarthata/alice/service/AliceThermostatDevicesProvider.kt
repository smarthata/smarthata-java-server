package org.smarthata.alice.service

import org.slf4j.LoggerFactory
import org.smarthata.alice.model.smarthome.ActionResult
import org.smarthata.alice.model.smarthome.BooleanState
import org.smarthata.alice.model.smarthome.DEVICES_CAPABILITIES_ON_OFF
import org.smarthata.alice.model.smarthome.DEVICES_CAPABILITIES_RANGE
import org.smarthata.alice.model.smarthome.Device
import org.smarthata.alice.model.smarthome.FloatState
import org.smarthata.alice.model.smarthome.OnOffCapability
import org.smarthata.alice.model.smarthome.RangeCapability
import org.smarthata.alice.model.smarthome.RangeParameter
import org.smarthata.service.device.Room
import org.smarthata.service.device.heating.HeatingService
import org.smarthata.service.message.EndpointType.ALICE
import org.springframework.stereotype.Service

@Service
class AliceThermostatDevicesProvider(
    private var heatingService: HeatingService,
): AliceDevicesProvider(THERMOSTAT_PREFIX) {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun devices() =
        listOf(
            createDevice(deviceId = "bedroom"),
            createDevice(deviceId = "bathroom"),
            createDevice(deviceId = "garage"),
            createDevice(deviceId = "workshop"),
            createDevice(deviceId = "hall"),
        )

    override fun query(device: Device): Device {
        logger.info("Query for device: $device")
        val deviceId = device.id.removePrefix(THERMOSTAT_PREFIX)
        return createDevice(deviceId, fillState = true)
    }

    override fun action(device: Device): Device? {
        logger.info("Action for device: $device")

        val deviceId = device.id.removePrefix(THERMOSTAT_PREFIX)

        val onOff = device.capabilities.firstOrNull { it.type == DEVICES_CAPABILITIES_ON_OFF }
        val range = device.capabilities.firstOrNull { it.type == DEVICES_CAPABILITIES_RANGE }

        val onOffActionResult: ActionResult? = if (onOff?.state?.value != null) {
            val value = onOff.state!!.value as Boolean
            logger.info("Changing thermostat $deviceId to state $value")
            heatingService.updateEnabled(Room.getFromRoomCode(deviceId), value, ALICE)
            ActionResult(status = "DONE")
        } else {
            null
        }

        val rangeActionResult: ActionResult? = if (range?.state?.value != null) {
            val value = range.state!!.value as Number
            logger.info("Changing thermostat $deviceId temperature to $value")
            heatingService.updateExpectedTemp(Room.getFromRoomCode(deviceId), value.toDouble(), ALICE)
            ActionResult(status = "DONE")
        } else {
            null
        }

        ActionResult(
            errorCode = "UNKNOWN",
            errorMessage = "Could not find on_off or range capability"
        )

        return createDevice(deviceId, fillState = true, onOffActionResult, rangeActionResult)
    }

    private fun createDevice(
        deviceId: String,
        fillState: Boolean = false,
        onOffActionResult: ActionResult? = null,
        rangeActionResult: ActionResult? = null,
    ): Device {
        val room = Room.getFromRoomCode(deviceId)
        return Device(
            id = THERMOSTAT_PREFIX + deviceId,
            name = "Термостат",
            room = room.rusName,
            type = "devices.types.thermostat",
            capabilities = listOf(
                OnOffCapability(
                    state = if (fillState) thermostatOnOffState(room, onOffActionResult) else null
                ),
                RangeCapability(
                    state = if (fillState) thermostatRangeState(room, rangeActionResult) else null,
                    parameters = RangeParameter(
                        range = RangeParameter.Range(5, 35, 0.5),
                    )
                )
            ),
        )
    }

    private fun thermostatOnOffState(room: Room, actionResult: ActionResult?) = BooleanState(
        instance = "on",
        value = heatingService.isEnabled(room),
        actionResult = actionResult,
    )

    private fun thermostatRangeState(room: Room, actionResult: ActionResult?) = FloatState(
        instance = "temperature",
        value = heatingService.expectedTemp(room),
        actionResult = actionResult,
    )

    companion object {
        private const val THERMOSTAT_PREFIX = "thermostat-"
    }
}
