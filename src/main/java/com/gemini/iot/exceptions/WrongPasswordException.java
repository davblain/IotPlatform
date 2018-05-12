package com.gemini.iot.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST,reason = "Wrong Password")
public class WrongPasswordException extends  RuntimeException {
    public WrongPasswordException() {
        super("Wrong Password");
    }

}