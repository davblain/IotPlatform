package com.gemini.iot.models.definitions;

import lombok.Data;
import org.springframework.context.annotation.Primary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "measurement_definitions")
public class MeasurementDefinition {
    @Id
    String name;
    @Column
    Integer dimension;
    @Column
    String description;
}
