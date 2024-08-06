package org.smarthata.alice.service

import org.smarthata.alice.model.smarthome.Device

abstract class AliceDevicesProvider(val prefix: String) {
    abstract fun devices(): List<Device>
    abstract fun query(device: Device): Device
    abstract fun action(device: Device): Device?
}
