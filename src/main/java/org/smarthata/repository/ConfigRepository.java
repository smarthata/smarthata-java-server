package org.smarthata.repository;

import org.smarthata.model.Config;
import org.smarthata.model.Device;
import org.springframework.data.repository.CrudRepository;

public interface ConfigRepository extends CrudRepository<Config, Integer> {

    default Config findByIdOrElseThrow(Integer configId) {
        return findById(configId)
                .orElseThrow(() -> new RuntimeException("Config Not Found"));
    }

    Iterable<Config> findByDevice(Device device);
}