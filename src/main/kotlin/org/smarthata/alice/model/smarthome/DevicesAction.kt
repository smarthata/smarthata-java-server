package org.smarthata.alice.model.smarthome

data class DevicesAction(
    val payload: DevicesActionPayload,
) {
    data class DevicesActionPayload(
        val devices: List<Device>,
    )
}

