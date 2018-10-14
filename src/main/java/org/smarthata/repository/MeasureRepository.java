package org.smarthata.repository;

import org.smarthata.model.Measure;
import org.smarthata.model.Sensor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface MeasureRepository extends CrudRepository<Measure, Integer> {

    Page<Measure> findBySensor(Sensor sensor, Pageable pageable);

    List<Measure> findBySensorInAndDateAfterOrderByDateAsc(Collection<Sensor> sensors, Date date);
    List<Measure> findBySensorInAndDateBetweenOrderByDateAsc(Collection<Sensor> sensors, Date startDate, Date endDate);

    Measure findTopBySensorOrderByDateDesc(Sensor sensor);
}