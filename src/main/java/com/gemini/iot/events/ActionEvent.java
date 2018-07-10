package com.gemini.iot.events;

import com.gemini.iot.dto.Action;
import lombok.Data;
import org.springframework.context.ApplicationEvent;

public class ActionEvent extends ApplicationEvent {
     Action action;

    public ActionEvent(Object source, Action action) {
        super(source);
        this.action = action;
    }

    public Action getAction() {
        return action;
    }
}
