package com.gemini.iot.events;

import com.gemini.iot.dto.ChangeDataRequest;
import com.gemini.iot.dto.MeasurementDataRequest;
import lombok.Data;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ChangeDataEvent extends ApplicationEvent {
    ChangeDataRequest data;

    public ChangeDataEvent(Object source, ChangeDataRequest data) {
        super(source);
        this.data = data;
    }
}
