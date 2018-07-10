package com.gemini.iot.events;

import org.springframework.context.ApplicationEvent;

public class TimeEvent  extends ApplicationEvent {

    public TimeEvent(Object source) {
        super(source);
    }
}
