package org.smarthata.repository;

import org.smarthata.model.Device;
import org.smarthata.model.Sensor;
import org.springframework.data.repository.CrudRepository;

public interface SensorRepository extends CrudRepository<Sensor, Integer> {

    default Sensor findByIdOrElseThrow(Integer sensorId) {
        return findById(sensorId)
                .orElseThrow(() -> new RuntimeException("Sensor Not Found"));
    }

    Iterable<Sensor> findByDevice(Device device);
}