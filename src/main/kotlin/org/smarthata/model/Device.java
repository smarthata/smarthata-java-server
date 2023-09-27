package org.smarthata.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer id;

    public String name;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL)
    public List<Sensor> sensors;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL)
    public List<Config> configs;

}