package org.smarthata.repository;

import org.smarthata.model.Device;
import org.springframework.data.repository.CrudRepository;

public interface DeviceRepository extends CrudRepository<Device, Integer> {

    default Device findByIdOrElseThrow(Integer deviceId) {
        return findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device Not Found"));
    }

}