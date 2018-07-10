package com.gemini.iot.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.gemini.iot.dto.OutEvent;
import org.eclipse.paho.client.mqttv3.MqttException;

public interface OutboundChannel {
    void pushEvent(OutEvent event);

}
