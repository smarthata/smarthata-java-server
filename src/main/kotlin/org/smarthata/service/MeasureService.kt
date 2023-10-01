package org.smarthata.service;

import org.smarthata.model.Device;
import org.smarthata.model.Measure;
import org.smarthata.model.Sensor;
import org.smarthata.repository.DeviceRepository;
import org.smarthata.repository.MeasureRepository;
import org.smarthata.repository.SensorRepository;
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

    public MeasureService(DeviceRepository deviceRepository, SensorRepository sensorRepository, MeasureRepository measureRepository) {
        this.deviceRepository = deviceRepository;
        this.sensorRepository = sensorRepository;
        this.measureRepository = measureRepository;
    }

    public Map<String, Measure> findTopByDevice(Integer deviceId) {
        Device device = deviceRepository.findByIdOrElseThrow(deviceId);
        return device.sensors.stream()
                .map(measureRepository::findTopBySensorOrderByDateDesc)
                .collect(Collectors.toMap(measure -> measure.sensor.name, Function.identity()));
    }

    public List<Measure> save(Integer deviceId, Map<String, String> params) {
        Device device = deviceRepository.findByIdOrElseThrow(deviceId);

        Date date = new Date();

        return params.entrySet().stream()
                .filter(this::isValid)
                .map(entry -> createMeasure(device, date, entry))
                .map(measureRepository::save)
                .collect(Collectors.toList());
    }

    private Measure createMeasure(Device device, Date date, Map.Entry<String, String> entry) {
        Sensor sensor = findOrCreateSensor(device, entry.getKey());
        Double value = Double.valueOf(entry.getValue());
        return new Measure(sensor, value, date);
    }

    private boolean isValid(Map.Entry<String, String> entry) {
        Double value = Double.valueOf(entry.getValue());
        return value > -50 && value < 120;
    }

    private Sensor findOrCreateSensor(final Device device, final String name) {
        return device.sensors
                .stream()
                .filter(s -> s.name.equals(name))
                .findAny()
                .orElseGet(() -> sensorRepository.save(new Sensor(device, name)));
    }

}
