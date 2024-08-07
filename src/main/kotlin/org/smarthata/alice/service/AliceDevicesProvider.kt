package org.smarthata.alice.service

import org.slf4j.LoggerFactory
import org.smarthata.alice.model.smarthome.ActionResult
import org.smarthata.alice.model.smarthome.Device

abstract class AliceDevicesProvider(val prefix: String) {

    protected val logger = LoggerFactory.getLogger(javaClass)

    abstract fun devices(): List<Device>

    fun query(device: Device): Device {
        logger.info("Query for device: $device")
        val deviceId = device.id.removePrefix(prefix)
        return createDevice(deviceId, fillState = true)
    }

    abstract fun action(device: Device): Device?

    abstract fun createDevice(
        deviceId: String,
        fillState: Boolean = false,
        actionResult: ActionResult? = null,
    ): Device
}
