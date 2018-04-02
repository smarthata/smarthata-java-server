package org.smarthata.rest;

import org.smarthata.model.Device;
import org.smarthata.model.Measure;
import org.smarthata.model.Sensor;
import org.smarthata.repository.DeviceRepository;
import org.smarthata.repository.MeasureRepository;
import org.smarthata.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devices")
public class DeviceController {

    private final DeviceRepository deviceRepository;
    private final SensorRepository sensorRepository;
    private final MeasureRepository measureRepository;

    @Autowired
    public DeviceController(DeviceRepository deviceRepository,
                            SensorRepository sensorRepository,
                            MeasureRepository measureRepository) {
        this.deviceRepository = deviceRepository;
        this.sensorRepository = sensorRepository;
        this.measureRepository = measureRepository;
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

    @GetMapping("/{deviceId}/measures")
    public List<Measure> measures(@PathVariable Integer deviceId) {
        Device device = deviceRepository.findByIdOrElseThrow(deviceId);
        return measureRepository.findBySensorIn(device.getSensors());
    }

    @GetMapping("/sensors")
    public Iterable<Sensor> findByDevice(@PathVariable Integer deviceId) {
        Device device = deviceRepository.findByIdOrElseThrow(deviceId);
        return sensorRepository.findByDevice(device);
    }

    @PostMapping("/sensors")
    public Sensor save(@PathVariable Integer deviceId, @RequestBody Sensor sensor) {
        Device device = deviceRepository.findByIdOrElseThrow(deviceId);
        sensor.setDevice(device);
        return sensorRepository.save(sensor);
    }

}
