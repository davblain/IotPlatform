package com.gemini.iot.events;

import com.gemini.iot.dto.ChangeDataEventDto;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ChangeDataEvent extends ApplicationEvent {
    ChangeDataEventDto data;

    public ChangeDataEvent(Object source, ChangeDataEventDto data) {
        super(source);
        this.data = data;
    }
}
