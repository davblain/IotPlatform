package com.gemini.iot.mqtt;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gemini.iot.dto.ChangeDataRequest;
import com.gemini.iot.dto.MqttEvent;
import com.gemini.iot.events.ChangeDataEvent;
import com.gemini.iot.exceptions.ParseMessageException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MqttController {
    private final ApplicationEventPublisher publisher;

    public MqttController(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    void handleMessage(String topic, String data) throws IOException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            MqttEvent mqttEvent = objectMapper.readValue(data, MqttEvent.class);
            ChangeDataRequest changeDataRequest = new ChangeDataRequest();
            changeDataRequest.setUuid(topic.replace("data/",""));
            changeDataRequest.setMeasurement(mqttEvent.getType());
            changeDataRequest.setData(mqttEvent.getValues());
            publisher.publishEvent(new ChangeDataEvent(this,changeDataRequest));
        } catch (JsonMappingException e ) {
            throw  new ParseMessageException();
        }


        System.out.println(topic);
    }
}
