package org.smarthata.service.device.heating

import java.util.concurrent.atomic.AtomicReference

private const val UNDEFINED_TEMP = -127.0

data class HeatingDevice(
    val actualTempQueue: String,
    val expectedTemp: AtomicReference<Double>,
) {
    constructor(actualTempQueue: String, expectedTemp: Double) :
        this(actualTempQueue, AtomicReference(expectedTemp))

    var actualTemp: Double = UNDEFINED_TEMP
    var enabled: Boolean = true

    val expectedTempQueue = "$actualTempQueue/in"
    val enabledQueue = "$actualTempQueue/enabled"

}
