package org.smarthata.repository;

import org.smarthata.model.Device;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface DeviceRepository extends CrudRepository<Device, Integer> {

    default Device findByIdOrElseThrow(Integer deviceId) {
        return findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device Not Found"));
    }

    Optional<Device> findByMac(String mac);

    default Device findByMacOrElseThrow(String mac) {
        return findByMac(mac)
                .orElseThrow(() -> new RuntimeException("Device Not Found"));
    }

}