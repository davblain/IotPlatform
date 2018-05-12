package com.gemini.iot.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST,reason = "User not found")
public class UserNotFoundException extends  RuntimeException {
        public UserNotFoundException(String username) {
            super("User "+ username+ "not found");
        }

}
