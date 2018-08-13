package org.smarthata.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "device_id"})
)
@Entity
public class Config {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @NotNull
    private String name;

    @NotNull
    private String value;

    private Units units;

    @JsonIgnore
    @NotNull
    @ManyToOne
    @JoinColumn(name = "device_id")
    private Device device;

    public Integer getId() {
        return id;
    }

    public Config setId(final Integer id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Config setName(final String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return value;
    }

    public Config setValue(final String value) {
        this.value = value;
        return this;
    }

    public Units getUnits() {
        return units;
    }

    public Config setUnits(final Units units) {
        this.units = units;
        return this;
    }

    public Device getDevice() {
        return device;
    }

    public Config setDevice(final Device device) {
        this.device = device;
        return this;
    }
}