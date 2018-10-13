package org.smarthata.service;

import org.smarthata.model.Device;
import org.smarthata.model.Measure;
import org.smarthata.model.Sensor;
import org.smarthata.repository.DeviceRepository;
import org.smarthata.repository.MeasureRepository;
import org.smarthata.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MeasureService {

    private final DeviceRepository deviceRepository;
    private final SensorRepository sensorRepository;
    private final MeasureRepository measureRepository;

    @Autowired
    public MeasureService(DeviceRepository deviceRepository, SensorRepository sensorRepository, MeasureRepository measureRepository) {
        this.deviceRepository = deviceRepository;
        this.sensorRepository = sensorRepository;
        this.measureRepository = measureRepository;
    }

    public Map<String, Measure> findTopByDevice(Integer deviceId) {
        Device device = deviceRepository.findByIdOrElseThrow(deviceId);
        return device.getSensors().stream()
                .map(measureRepository::findTopBySensorOrderByDateDesc)
                .collect(Collectors.toMap(measure -> measure.getSensor().getName(), Function.identity()));
    }

    public List<Measure> save(Integer deviceId, Map<String, String> params) {
        Device device = deviceRepository.findByIdOrElseThrow(deviceId);

        Date date = new Date();

        return params.keySet().stream()
                .map(name -> createMeasure(params, device, name, date))
                .map(measureRepository::save)
                .collect(Collectors.toList());
    }

    private Measure createMeasure(final Map<String, String> params, final Device device, final String name, Date date) {
        Sensor sensor = findOrCreateSensor(device, name);
        Double value = Double.valueOf(params.get(name));
        return new Measure(sensor, value, date);
    }

    private Sensor findOrCreateSensor(final Device device, final String name) {
        return device.getSensors()
                .stream()
                .filter(s -> s.getName().equals(name))
                .findAny()
                .orElseGet(() -> sensorRepository.save(new Sensor(device, name)));
    }

}
