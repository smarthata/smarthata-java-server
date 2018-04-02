package org.smarthata.repository;

import org.smarthata.model.Measure;
import org.smarthata.model.Sensor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MeasureRepository extends CrudRepository<Measure, Integer> {

    List<Measure> findBySensor(Sensor sensor);
}