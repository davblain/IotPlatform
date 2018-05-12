package com.gemini.iot.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST,reason = "Schedule not found")
public class ScheduleNotFoundException extends RuntimeException {
    public ScheduleNotFoundException(String uuid) {
        super("Schedule "+ uuid + " not found");
    }
}
