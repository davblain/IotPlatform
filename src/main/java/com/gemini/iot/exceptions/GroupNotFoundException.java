package com.gemini.iot.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST,reason = "Group not found")
public class GroupNotFoundException extends RuntimeException{
    public GroupNotFoundException(String uuid) {
        super("Group "+ uuid+" not found");
    }
}
