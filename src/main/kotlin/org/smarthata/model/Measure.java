package org.smarthata.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@ToString
public class Measure {

    @JsonIgnore
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

    public Measure(Sensor sensor, Double value, Date date) {
        this.sensor = sensor;
        this.value = value;
        this.date = date;
    }

}
