package org.smarthata.rest;

import org.smarthata.model.Device;
import org.smarthata.model.Sensor;
import org.smarthata.repository.DeviceRepository;
import org.smarthata.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/devices/{deviceId}/sensors")
public class SensorController {

    private final DeviceRepository deviceRepository;
    private final SensorRepository sensorRepository;

    @Autowired
    public SensorController(DeviceRepository deviceRepository, SensorRepository sensorRepository) {
        this.deviceRepository = deviceRepository;
        this.sensorRepository = sensorRepository;
    }

    @GetMapping
    public Iterable<Sensor> findByDevice(@PathVariable Integer deviceId) {
        Device device = deviceRepository.findByIdOrElseThrow(deviceId);
        return sensorRepository.findByDevice(device);
    }

    @PostMapping
    public Sensor save(@PathVariable Integer deviceId, @RequestBody Sensor sensor) {
        Device device = deviceRepository.findByIdOrElseThrow(deviceId);
        sensor.setDevice(device);
        return sensorRepository.save(sensor);
    }

    @GetMapping("/{sensorId}")
    public Sensor findById(@PathVariable Integer sensorId) {
        return sensorRepository.findByIdOrElseThrow(sensorId);
    }

    @PutMapping("/{sensorId}")
    public Sensor put(@PathVariable Integer deviceId, @PathVariable Integer sensorId, @RequestBody Sensor sensor) {
        Device device = deviceRepository.findByIdOrElseThrow(deviceId);
        Sensor saved = sensorRepository.findByIdOrElseThrow(sensorId);
        saved.setName(sensor.getName());
        saved.setUnits(sensor.getUnits());
        return sensorRepository.save(saved);
    }
}
