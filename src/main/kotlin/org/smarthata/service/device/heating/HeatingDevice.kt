package org.smarthata.service.device.heating

import java.util.concurrent.atomic.AtomicReference

private const val UNDEFINED_TEMP = -127.0

internal class HeatingDevice(
    val queueActualTemp: String,
    val expectedTemp: AtomicReference<Double>,
) {
    var actualTemp: Double = UNDEFINED_TEMP
    var enabled: Boolean = true

    fun getExpectedTempQueue() = "$queueActualTemp/in"
    fun getEnabledQueue() = "$queueActualTemp/enabled"

}
