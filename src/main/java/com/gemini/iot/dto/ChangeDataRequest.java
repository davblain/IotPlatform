package com.gemini.iot.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChangeDataRequest {
    String uuid;
    String measurement;
    List<Double> data;
}
