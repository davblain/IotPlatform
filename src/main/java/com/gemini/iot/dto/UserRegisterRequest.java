package com.gemini.iot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterRequest {
    private String username;
    private String password;
    private String email;
    public UserRegisterRequest() {
    }



}
