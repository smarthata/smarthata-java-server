package org.smarthata.rest;

import org.smarthata.model.Device;
import org.smarthata.model.Sensor;
import org.smarthata.repository.DeviceRepository;
import org.smarthata.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devices")
public class DeviceController {

    private final DeviceRepository deviceRepository;
    private final SensorRepository sensorRepository;

    @Autowired
    public DeviceController(DeviceRepository deviceRepository,
                            SensorRepository sensorRepository) {
        this.deviceRepository = deviceRepository;
        this.sensorRepository = sensorRepository;
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

    @GetMapping("/{deviceId}/sensors")
    public List<Sensor> findByDevice(@PathVariable Integer deviceId) {
        Device device = deviceRepository.findByIdOrElseThrow(deviceId);
        return device.getSensors();
    }

    @PostMapping("/{deviceId}/sensors")
    public Sensor save(@PathVariable Integer deviceId, @RequestBody Sensor sensor) {
        Device device = deviceRepository.findByIdOrElseThrow(deviceId);
        sensor.setDevice(device);
        return sensorRepository.save(sensor);
    }

}
