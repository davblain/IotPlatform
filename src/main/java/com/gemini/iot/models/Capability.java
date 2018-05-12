package com.gemini.iot.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class Capability {
    List<String> data = new ArrayList<>();
    List<String> action = new ArrayList<>();
}
