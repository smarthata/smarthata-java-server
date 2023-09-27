package org.smarthata.service.mqtt;

import java.time.LocalDateTime;

data class LastMessage(
    val message: String,
    val dateTime: LocalDateTime = LocalDateTime.now(),
)
