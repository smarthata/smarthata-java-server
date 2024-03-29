package org.smarthata.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;


@Entity
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer id;

    public String name;

    public Units units;

    @JsonIgnore
    @NotNull
    @ManyToOne
    @JoinColumn(name = "device_id")
    public Device device;

    public Sensor(@NotNull Device device, String name) {
        this.name = name;
        this.device = device;
    }

    public Sensor() {
    }
}
