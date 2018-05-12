package com.gemini.iot.dto;

import com.gemini.iot.models.Capability;
import com.gemini.iot.models.State;
import com.gemini.iot.models.definitions.ActionDefinition;
import com.gemini.iot.models.definitions.MeasurementDefinition;
import lombok.Data;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Data
public class DeviceDto {
    UUID uuid;
    String owner;
    String model;
    Capability capability;
    State state;
}
