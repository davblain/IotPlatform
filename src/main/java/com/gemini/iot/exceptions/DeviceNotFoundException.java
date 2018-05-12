package com.gemini.iot.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST,reason = "Device not found")
public class DeviceNotFoundException extends RuntimeException {
    public DeviceNotFoundException(String uuid) {
        super("Device "+ uuid + " not found");
    }
}
