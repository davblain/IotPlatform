package com.gemini.iot.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UserProfileDto {
    private UUID id;
    private String username;
    private List<String> groups;


}
