package com.gemini.iot.dto;

import lombok.Data;

import java.util.List;
@Data
public class ActionData {
    Double time;
    List<List<Object>> timedData;
}
