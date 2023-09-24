package org.smarthata.alice.model.smarthome

import com.fasterxml.jackson.annotation.JsonInclude

data class DevicesResponse(
    val requestId: String,
    val payload: DevicesPayload,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DevicesPayload(
    val userId: String? = null,
    val devices: List<Device>,
)
