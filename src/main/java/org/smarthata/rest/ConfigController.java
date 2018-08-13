package org.smarthata.rest;

import org.smarthata.model.Config;
import org.smarthata.model.Device;
import org.smarthata.repository.ConfigRepository;
import org.smarthata.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ConfigController {

    private final DeviceRepository deviceRepository;
    private final ConfigRepository configRepository;

    @Autowired
    public ConfigController(DeviceRepository deviceRepository,
                            ConfigRepository configRepository) {
        this.deviceRepository = deviceRepository;
        this.configRepository = configRepository;
    }

    @GetMapping("/devices/{deviceId}/configs")
    public List<Config> findByDevice(@PathVariable Integer deviceId) {
        Device device = deviceRepository.findByIdOrElseThrow(deviceId);
        return device.getConfigs();
    }

    @PostMapping("/devices/{deviceId}/configs")
    public Config save(@PathVariable Integer deviceId, @RequestBody Config config) {
        Device device = deviceRepository.findByIdOrElseThrow(deviceId);
        config.setDevice(device);
        return configRepository.save(config);
    }

    @GetMapping("/configs/{configId}")
    public Config findById(@PathVariable Integer configId) {
        return configRepository.findByIdOrElseThrow(configId);
    }

    @PutMapping("/configs/{configId}")
    public Config put(@PathVariable Integer configId, @RequestBody Config config) {
        Config saved = configRepository.findByIdOrElseThrow(configId);
        saved.setName(config.getName());
        saved.setValue(config.getValue());
        saved.setUnits(config.getUnits());
        return configRepository.save(saved);
    }

}
