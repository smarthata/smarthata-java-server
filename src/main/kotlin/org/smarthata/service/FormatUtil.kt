package org.smarthata.service

const val CELSIUS = "°C"
fun formatTemp(temp: Double) = "%.1f%s".format(temp, CELSIUS)
