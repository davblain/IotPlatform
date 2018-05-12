package com.gemini.iot.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST,reason = "Action not found")
public class NoSuchActionException extends RuntimeException {
    public  NoSuchActionException(String action) {
        super("Action "+ action + " not found");
    }
}
