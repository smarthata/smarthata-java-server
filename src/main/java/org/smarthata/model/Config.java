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

}