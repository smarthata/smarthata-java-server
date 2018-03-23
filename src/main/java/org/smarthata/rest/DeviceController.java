package org.smarthata.rest;

import org.smarthata.model.Device;
import org.smarthata.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/devices")
public class DeviceController {

    private final DeviceRepository deviceRepository;

    @Autowired
    public DeviceController(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @GetMapping
    public Iterable<Device> findAll() {
        return deviceRepository.findAll();
    }

    @PostMapping
    public Device save(@RequestBody Device device) {
        return deviceRepository.save(device);
    }

    @GetMapping("/{deviceId}")
    public Device findById(@PathVariable Integer deviceId) {
        return deviceRepository.findByIdOrElseThrow(deviceId);
    }

    @PutMapping("/{deviceId}")
    public Device put(@PathVariable Integer deviceId, @RequestBody Device device) {
        Device saved = deviceRepository.findByIdOrElseThrow(deviceId);
        saved.setName(device.getName());
        return deviceRepository.save(saved);
    }
}
