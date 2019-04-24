package org.smarthata.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String name;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL)
    private List<Sensor> sensors;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL)
    private List<Config> configs;

}