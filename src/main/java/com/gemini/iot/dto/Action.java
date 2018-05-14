package com.gemini.iot.dto;

import com.gemini.iot.models.definitions.ActionDefinition;
import lombok.Data;

import java.util.List;
@Data
public class Action {
    String uuid;
    String name;
    List<String> data;
}
