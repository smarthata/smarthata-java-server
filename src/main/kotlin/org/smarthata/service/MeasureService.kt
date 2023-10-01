package org.smarthata.service

import org.slf4j.LoggerFactory
import org.smarthata.model.Device
import org.smarthata.model.Measure
import org.smarthata.model.Sensor
import org.smarthata.repository.DeviceRepository
import org.smarthata.repository.MeasureRepository
import org.smarthata.repository.SensorRepository
import org.springframework.stereotype.Service
import java.util.Date

@Service
class MeasureService(
    private val deviceRepository: DeviceRepository,
    private val sensorRepository: SensorRepository,
    private val measureRepository: MeasureRepository
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun findTopByDevice(deviceId: Int): Map<String, Measure> =
        deviceRepository.findByIdOrElseThrow(deviceId).sensors
            .map { sensor: Sensor -> measureRepository.findTopBySensorOrderByDateDesc(sensor) }
            .associateBy { it.sensor.name }

    fun save(deviceId: Int, params: Map<String, String>): List<Measure> {
        val device = deviceRepository.findByIdOrElseThrow(deviceId)
        val date = Date()
        return params
            .mapValues { it.value.toDouble() }
            .filterValues { isValid(it) }
            .mapKeys { findOrCreateSensor(device, it.key) }
            .map { Measure(it.key, it.value, date) }
            .map { measureRepository.save(it) }
    }

    private fun isValid(value: Double): Boolean = value > -50 && value < 120

    private fun findOrCreateSensor(device: Device, name: String): Sensor =
        device.sensors.firstOrNull { s: Sensor -> s.name == name }
            ?: sensorRepository.save(Sensor(device, name))
}