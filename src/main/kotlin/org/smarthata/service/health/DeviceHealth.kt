package org.smarthata.service.health

import java.time.LocalDateTime

data class DeviceHealth(
    var devicePath: String,
) {
    var status: DeviceStatus = DeviceStatus.UNKNOWN
    var updateTime: LocalDateTime = LocalDateTime.now()
    var lastNotificationTime: LocalDateTime? = null

    enum class DeviceStatus {
        ACTIVE, OFFLINE, UNKNOWN
    }
}
