package com.gemini.iot.dto;

import lombok.Data;

import java.lang.reflect.Array;
import java.util.List;

@Data
public class MeasurementData {
    Double time;
    List<List<Object>> timedData;
}
