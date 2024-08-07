package org.smarthata.alice.model.smarthome.notification

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class NotificationResponse(
    @JsonProperty("request_id")
    val requestId: UUID,
    val status: String,
)
