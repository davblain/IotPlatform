package com.gemini.iot.models.definitions;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "action_definitions")
public class ActionDefinition {
    @Id
    String name;
    @Column
    Integer countOfValue;
}
