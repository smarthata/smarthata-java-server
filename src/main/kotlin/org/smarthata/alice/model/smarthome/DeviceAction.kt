package org.smarthata.alice.model.smarthome

data class DeviceAction(
    val payload: DeviceActionPayload

)

data class DeviceActionPayload(
    val devices: List<Device>
)
