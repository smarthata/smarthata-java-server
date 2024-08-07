package org.smarthata.alice.model.smarthome.notification

import org.smarthata.alice.model.smarthome.DevicesPayload

data class NotificationRequest(
    val ts: Long = System.currentTimeMillis() / 1000,
    val payload: DevicesPayload,
)
