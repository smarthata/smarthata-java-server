package org.smarthata.rest

import org.smarthata.model.Config
import org.smarthata.repository.ConfigRepository
import org.smarthata.repository.DeviceRepository
import org.springframework.web.bind.annotation.*

@RestController
class ConfigController(
    private val deviceRepository: DeviceRepository,
    private val configRepository: ConfigRepository
) {
    @GetMapping("/devices/{deviceId}/configs")
    fun findByDevice(@PathVariable deviceId: Int): List<Config> =
        deviceRepository.findByIdOrElseThrow(deviceId).configs

    @PostMapping("/devices/{deviceId}/configs")
    fun save(@PathVariable deviceId: Int, @RequestBody config: Config): Config {
        val device = deviceRepository.findByIdOrElseThrow(deviceId)
        config.device = device
        return configRepository.save(config)
    }

    @GetMapping("/configs/{configId}")
    fun findById(@PathVariable configId: Int): Config = configRepository.findByIdOrElseThrow(configId)

    @PutMapping("/configs/{configId}")
    fun put(@PathVariable configId: Int, @RequestBody config: Config): Config {
        val saved = configRepository.findByIdOrElseThrow(configId)
        saved.name = config.name
        saved.value = config.value
        saved.units = config.units
        return configRepository.save(saved)
    }
}