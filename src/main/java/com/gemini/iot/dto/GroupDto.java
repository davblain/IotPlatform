package com.gemini.iot.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Data
public class GroupDto {
    UUID uuid;
    String admin;
    String name;
    List<String> devices;
}
