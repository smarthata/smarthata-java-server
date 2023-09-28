package org.smarthata.service.health

import org.slf4j.LoggerFactory
import org.smarthata.service.health.DeviceHealth.DeviceStatus
import org.smarthata.service.message.AbstractSmarthataMessageListener
import org.smarthata.service.message.EndpointType
import org.smarthata.service.message.SmarthataMessage
import org.smarthata.service.message.SmarthataMessageBroker
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime

@Service
class DeviceHealthCheckService(
    messageBroker: SmarthataMessageBroker,
    @Value("\${health.devices}") devices: List<String>
) : AbstractSmarthataMessageListener(messageBroker) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val deviceTimeMap: Map<String, DeviceHealth> = devices.associateWith {
        DeviceHealth(it)
    }

    override fun receiveSmarthataMessage(message: SmarthataMessage) {
        deviceTimeMap[message.path]?.let {
            if (it.status == DeviceStatus.OFFLINE) {
                it.status = DeviceStatus.ACTIVE
                it.lastNotificationTime = null
                sendMessage("Device is active: " + it.devicePath)
            }
            it.updateTime = LocalDateTime.now()
        }
    }

    override fun endpointType(): EndpointType = EndpointType.SYSTEM

    @Scheduled(cron = "0 * * * * *")
    fun check() {
        val offlineDevices = findOfflineDevices()
        logger.debug("offlineDevices = $offlineDevices")
        sendNotifications(offlineDevices)
    }

    private fun findOfflineDevices(): List<DeviceHealth> = deviceTimeMap.values
        .filter { isPastMore(it.updateTime, OFFLINE_DURATION) }
        .map {
            it.status = DeviceStatus.OFFLINE
            it
        }

    private fun sendNotifications(offlineDevices: List<DeviceHealth>) {
        offlineDevices.forEach {
            if (isPastMore(it.lastNotificationTime, NOTIFICATION_DURATION)) {
                it.lastNotificationTime = LocalDateTime.now()
                sendMessage("Device is offline: " + it.devicePath)
            }
        }
    }

    private fun isPastMore(time: LocalDateTime?, duration: Duration): Boolean {
        return time == null || Duration.between(time, LocalDateTime.now()) > duration
    }

    private fun sendMessage(text: String) {
        messageBroker.broadcast(SmarthataMessage("/messages", text, EndpointType.SYSTEM, EndpointType.TELEGRAM))
    }

    companion object {
        private val OFFLINE_DURATION = Duration.ofMinutes(30)
        private val NOTIFICATION_DURATION = Duration.ofHours(6)
    }
}