package org.smarthata.model;


import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

@Entity
public class Measure {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer id;

    @JsonIgnore
    @NotNull
    @ManyToOne
    @JoinColumn(name = "sensor_id")
    public Sensor sensor;

    public Double value;

    public Date date;

    public Measure(Sensor sensor, Double value, Date date) {
        this.sensor = sensor;
        this.value = value;
        this.date = date;
    }

    public Measure() {
    }
}
