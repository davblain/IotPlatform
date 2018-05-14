package com.gemini.iot.services;


import com.gemini.iot.dto.OutEvent;

public interface OutboundChannel {
    void pushEvent(OutEvent event);

}
