package com.gemini.iot.exceptions;

import com.gemini.iot.models.Device;
import com.gemini.iot.models.definitions.DeviceDefinition;

public class DeviceDefinitionNotFoundException extends RuntimeException{

    public DeviceDefinitionNotFoundException(Long uuid) {
        super("Device Definition with Id "+uuid+" not found");
    }
}
