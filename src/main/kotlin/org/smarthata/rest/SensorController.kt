package org.smarthata.rest

import org.smarthata.model.Measure
import org.smarthata.model.Sensor
import org.smarthata.repository.MeasureRepository
import org.smarthata.repository.SensorRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/sensors")
class SensorController(
    private val sensorRepository: SensorRepository,
    private val measureRepository: MeasureRepository
) {
    @GetMapping
    fun findAll(): Iterable<Sensor> = sensorRepository.findAll()

    @GetMapping("/{sensorId}")
    fun findById(@PathVariable sensorId: Int?): Sensor = sensorRepository.findByIdOrElseThrow(sensorId)

    @PutMapping("/{sensorId}")
    fun put(@PathVariable sensorId: Int?, @RequestBody sensor: Sensor): Sensor {
        val saved = sensorRepository.findByIdOrElseThrow(sensorId)
        saved.name = sensor.name
        saved.units = sensor.units
        return sensorRepository.save(saved)
    }

    @GetMapping("/{sensorId}/measures")
    fun measures(
        @PathVariable sensorId: Int,
        @SortDefault(value = ["date"], direction = Sort.Direction.DESC) pageable: Pageable
    ): Page<Measure> {
        val sensor = sensorRepository.findByIdOrElseThrow(sensorId)
        return measureRepository.findBySensor(sensor, pageable)
    }
}