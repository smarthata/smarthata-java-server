package org.smarthata.repository;

import org.smarthata.model.Measure;
import org.smarthata.model.Sensor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface MeasureRepository extends CrudRepository<Measure, Integer> {

    Page<Measure> findBySensor(Sensor sensor, Pageable pageable);
    List<Measure> findBySensorAndDateAfter(Sensor sensor, Date date);

    List<Measure> findBySensorInAndDateBetweenOrderByDateAsc(Collection<Sensor> sensors, Date startDate, Date endDate);

    @Query(value = "SELECT AVG(value) FROM measure WHERE sensor_id = ?1 and date BETWEEN ?2 AND ?3",
            nativeQuery = true)
    Double avgValueBySensorAndDateBetween(Integer sensor_id, Date startDate, Date endDate);
    Long countBySensorAndDateBetween(Sensor sensor, Date startDate, Date endDate);
    Long deleteBySensorAndDateBetween(Sensor sensor, Date startDate, Date endDate);

    Measure findTopBySensorOrderByDateDesc(Sensor sensor);
}
