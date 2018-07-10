package com.gemini.iot.dto;

import lombok.Data;

import java.util.List;

@Data
public class ActionDto {
    String action;
    List<String> data;
}
