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
@RequestMapping("/devices/{deviceId}/sensors")
public class SensorController {

    private final DeviceRepository deviceRepository;
    private final SensorRepository sensorRepository;
    private final MeasureRepository measureRepository;

    @Autowired
    public SensorController(DeviceRepository deviceRepository, SensorRepository sensorRepository, MeasureRepository measureRepository) {
        this.deviceRepository = deviceRepository;
        this.sensorRepository = sensorRepository;
        this.measureRepository = measureRepository;
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
    public Sensor findById(@PathVariable Integer deviceId, @PathVariable Integer sensorId) {
        return sensorRepository.findByIdOrElseThrow(sensorId);
    }

    @PutMapping("/{sensorId}")
    public Sensor put(@PathVariable Integer deviceId, @PathVariable Integer sensorId, @RequestBody Sensor sensor) {
        Sensor saved = sensorRepository.findByIdOrElseThrow(sensorId);
        saved.setName(sensor.getName());
        saved.setUnits(sensor.getUnits());
        return sensorRepository.save(saved);
    }

    @GetMapping("/{sensorId}/measures")
    public List<Measure> measures(@PathVariable Integer deviceId, @PathVariable Integer sensorId) {
        Sensor sensor = sensorRepository.findByIdOrElseThrow(sensorId);
        return measureRepository.findBySensor(sensor);
    }

}
