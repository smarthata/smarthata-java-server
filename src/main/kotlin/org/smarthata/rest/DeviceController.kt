package org.smarthata.rest

import org.smarthata.model.Device
import org.smarthata.model.Sensor
import org.smarthata.repository.DeviceRepository
import org.smarthata.repository.SensorRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/devices")
class DeviceController(
    private val deviceRepository: DeviceRepository,
    private val sensorRepository: SensorRepository
) {
    @GetMapping
    fun findAll(): Iterable<Device> = deviceRepository.findAll()

    @PostMapping
    fun save(@RequestBody device: Device): Device = deviceRepository.save(device)

    @GetMapping("/{deviceId}")
    fun findById(@PathVariable deviceId: Int): Device = deviceRepository.findByIdOrElseThrow(deviceId)

    @PutMapping("/{deviceId}")
    fun put(@PathVariable deviceId: Int, @RequestBody device: Device): Device {
        val saved = deviceRepository.findByIdOrElseThrow(deviceId)
        saved.name = device.name
        return deviceRepository.save(saved)
    }

    @GetMapping("/{deviceId}/sensors")
    fun findByDevice(@PathVariable deviceId: Int): List<Sensor> =
        deviceRepository.findByIdOrElseThrow(deviceId).sensors

    @PostMapping("/{deviceId}/sensors")
    fun save(@PathVariable deviceId: Int, @RequestBody sensor: Sensor): Sensor {
        sensor.device = deviceRepository.findByIdOrElseThrow(deviceId)
        return sensorRepository.save(sensor)
    }
}