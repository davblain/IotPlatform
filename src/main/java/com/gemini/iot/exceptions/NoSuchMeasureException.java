package com.gemini.iot.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST,reason = "Measure not found")
public class NoSuchMeasureException extends RuntimeException  {

    public NoSuchMeasureException(String measure) {
        super("Measure "+ measure + " not found");
    }
}
