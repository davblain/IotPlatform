package com.gemini.iot.models.definitions;

import com.gemini.iot.models.definitions.ActionDefinition;
import com.gemini.iot.models.definitions.MeasurementDefinition;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "device_definition")
@Data
public class DeviceDefinition {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column
    String name;

    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(
            name = "device_measurement_definitions",
            joinColumns = @JoinColumn(
                    name = "device_def_id"),
            inverseJoinColumns = @JoinColumn(
                    name = "measurement_def_id"))
    List<MeasurementDefinition> measuresDefinitions;

    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(
            name = "device_action_definitions",
            joinColumns = @JoinColumn(
                    name = "device_def_id"),
            inverseJoinColumns = @JoinColumn(
                    name = "action_def_id"))
    List<ActionDefinition> actionsDefinitions;
}
