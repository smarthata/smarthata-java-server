package org.smarthata.model;


import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
public class Measure {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @JsonIgnore
    @NotNull
    @ManyToOne
    @JoinColumn(name = "sensor_id")
    private Sensor sensor;

    private Double value;

    private Date date;

    public Measure() {
    }

    public Measure(Sensor sensor, Double value) {
        this.sensor = sensor;
        this.value = value;
        this.date = new Date();
    }

    public Integer getId() {
        return id;
    }

    public Measure setId(final Integer id) {
        this.id = id;
        return this;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public Measure setSensor(final Sensor sensor) {
        this.sensor = sensor;
        return this;
    }

    public Double getValue() {
        return value;
    }

    public Measure setValue(final Double value) {
        this.value = value;
        return this;
    }

    public Date getDate() {
        return date;
    }

    public Measure setDate(final Date date) {
        this.date = date;
        return this;
    }
}
