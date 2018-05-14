package com.gemini.iot.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class State {
     List<List<Object>> data;
     List<List<String>> action;

}
