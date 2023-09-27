package org.smarthata.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "device_id"})
)
@Entity
public class Config {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer id;

    @NotNull
    public String name;

    @NotNull
    public String value;

    public Units units;

    @JsonIgnore
    @NotNull
    @ManyToOne
    @JoinColumn(name = "device_id")
    public Device device;

}