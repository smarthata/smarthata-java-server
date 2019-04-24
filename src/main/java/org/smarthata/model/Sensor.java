package org.smarthata.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String name;

    private Units units;

    @JsonIgnore
    @NotNull
    @ManyToOne
    @JoinColumn(name = "device_id")
    private Device device;

    public Sensor(@NotNull Device device, String name) {
        this.name = name;
        this.device = device;
    }
}
