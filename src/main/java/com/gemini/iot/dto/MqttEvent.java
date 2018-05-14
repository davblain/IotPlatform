package com.gemini.iot.dto;

import lombok.Data;

import java.util.List;
@Data
public class MqttEvent {
    String type;
    List<Double> values;
}
