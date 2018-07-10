package com.gemini.iot.outbound;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gemini.iot.dto.MqttEvent;
import com.gemini.iot.dto.OutEvent;
import com.gemini.iot.services.OutboundChannel;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;

@Component
public class MqttOutboundChannel implements OutboundChannel{

    @Autowired
    IMqttClient mqttClient;
    @Autowired
    MqttConnectOptions options;
    @PostConstruct
    void init() {
        try {
            mqttClient.connect(options);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void pushEvent(OutEvent event)  {
        try {

            MqttEvent mqttEvent = new MqttEvent();
            mqttEvent.setName(event.getName());
            mqttEvent.setValues(Arrays.asList(10.0));
            MqttMessage message = new MqttMessage();
            ObjectMapper objectMapper = new ObjectMapper();
            message.setPayload(objectMapper.writeValueAsBytes(mqttEvent));
            if (event.getType().equals("action")) {
                mqttClient.publish("device/"+event.getUuid()+"/action", message);
            } else {
                mqttClient.publish("device/"+event.getUuid()+"/data",message);
            }
        }catch ( MqttException | JsonProcessingException e) {
            e.printStackTrace();
        }


    }
}
