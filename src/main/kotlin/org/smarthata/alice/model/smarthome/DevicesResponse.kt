package org.smarthata.alice.model.smarthome

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

data class DevicesResponse(
    val requestId: String,
    val payload: DevicesPayload,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DevicesPayload(
    @JsonProperty("user_id")
    val userId: String? = null,
    val devices: List<Device>,
)
