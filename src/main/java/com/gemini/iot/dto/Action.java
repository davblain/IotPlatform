package com.gemini.iot.dto;

import com.gemini.iot.models.definitions.ActionDefinition;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class Action {
    String uuid;
    String name;
    List<String> data = new ArrayList<>();
    public Action() {

    }
    public Action(String uuid, ActionDto actionDto) {
        this.uuid = uuid;
        this.name= actionDto.action;
        this.data = actionDto.data;
    }
}
