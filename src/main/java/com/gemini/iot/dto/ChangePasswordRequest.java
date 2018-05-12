package com.gemini.iot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {
    String oldPassword;
    String newPassword;

}
