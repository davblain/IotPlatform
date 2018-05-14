package com.gemini.iot.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OutEvent<T> {
    private String type;
    private String uuid;
    private String name;
    List<T> data;
    public OutEvent( String uuid,String type, String name,List<T> source) {
        this.type = type;
        this.uuid = uuid;
        this.name = name;
        this.data = source;
    }
}
