package org.smarthata.rest

import org.smarthata.model.Measure
import org.smarthata.service.MeasureService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/devices/{deviceId}/measures")
class MeasureController(private val measureService: MeasureService) {
    @GetMapping
    fun measures(@PathVariable deviceId: Int): Map<String, Measure> = measureService.findTopByDevice(deviceId)

    @PostMapping
    fun savePost(@PathVariable deviceId: Int, @RequestParam params: Map<String, String>): List<Measure> =
        measureService.save(deviceId, params)
}