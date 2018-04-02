package org.smarthata.rest;

import org.smarthata.model.Measure;
import org.smarthata.model.Sensor;
import org.smarthata.repository.MeasureRepository;
import org.smarthata.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sensors")
public class SensorController {

    private final SensorRepository sensorRepository;
    private final MeasureRepository measureRepository;

    @Autowired
    public SensorController(SensorRepository sensorRepository, MeasureRepository measureRepository) {
        this.sensorRepository = sensorRepository;
        this.measureRepository = measureRepository;
    }

    @GetMapping
    public Iterable<Sensor> findAll() {
        return sensorRepository.findAll();
    }

    @GetMapping("/{sensorId}")
    public Sensor findById(@PathVariable Integer sensorId) {
        return sensorRepository.findByIdOrElseThrow(sensorId);
    }

    @PutMapping("/{sensorId}")
    public Sensor put(@PathVariable Integer sensorId, @RequestBody Sensor sensor) {
        Sensor saved = sensorRepository.findByIdOrElseThrow(sensorId);
        saved.setName(sensor.getName());
        saved.setUnits(sensor.getUnits());
        return sensorRepository.save(saved);
    }

    @GetMapping("/{sensorId}/measures")
    public List<Measure> measures(@PathVariable Integer sensorId) {
        Sensor sensor = sensorRepository.findByIdOrElseThrow(sensorId);
        return measureRepository.findBySensor(sensor);
    }

}
