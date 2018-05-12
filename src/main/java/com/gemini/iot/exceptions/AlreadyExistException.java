package com.gemini.iot.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST,reason = "User Already Exists")
public class AlreadyExistException extends RuntimeException {
    public AlreadyExistException(String username) {
        super("User "+ username+ " already exists");
    }
}
