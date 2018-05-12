package com.gemini.iot.dto;

import com.gemini.iot.models.definitions.ActionDefinition;
import com.gemini.iot.models.definitions.MeasurementDefinition;
import lombok.Data;

import java.util.List;

@Data
public class DeviceDefinitionDto {
    Long id;

    String name;
    List<String> measuresDefinitions;


    List<String> actionsDefinitions;
}
