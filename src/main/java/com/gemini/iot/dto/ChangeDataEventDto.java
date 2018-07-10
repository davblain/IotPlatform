package com.gemini.iot.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChangeDataEventDto {
    String uuid;
    String measurement;
    List<Double> data;
}
