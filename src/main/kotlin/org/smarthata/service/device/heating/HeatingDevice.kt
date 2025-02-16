package org.smarthata.service.device.heating

import java.util.concurrent.atomic.AtomicReference

private const val UNDEFINED_TEMP = -127.0

data class HeatingDevice(
    val actualTempQueue: String,
    val expectedTemp: AtomicReference<Double>,
    val nightMode: Boolean = false,
) {
    constructor(actualTempQueue: String, expectedTemp: Double, nightMode: Boolean = false) :
        this(actualTempQueue, AtomicReference(expectedTemp), nightMode)

    val expectedNightTemp = AtomicReference(20.0)

    var actualTemp: Double = UNDEFINED_TEMP
    var enabled: Boolean = true

    val expectedTempQueue = "$actualTempQueue/in"
    val expectedNightTempQueue = "$actualTempQueue/night/in"
    val enabledQueue = "$actualTempQueue/enabled"

}
