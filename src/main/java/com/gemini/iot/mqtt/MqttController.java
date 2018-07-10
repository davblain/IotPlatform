package com.gemini.iot.mqtt;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gemini.iot.dto.ChangeDataEventDto;
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
            MqttEvent<Double> mqttEvent = objectMapper.readValue(data, MqttEvent.class);
            ChangeDataEventDto changeDataEventDto = new ChangeDataEventDto();
            String uuid;
            uuid = topic.replace("/data","");
            uuid = uuid.replace("device/","");
            changeDataEventDto.setUuid(uuid);
            changeDataEventDto.setMeasurement(mqttEvent.getName());
            changeDataEventDto.setData(mqttEvent.getValues());
            publisher.publishEvent(new ChangeDataEvent(this, changeDataEventDto));
        } catch (JsonMappingException e ) {
            throw  new ParseMessageException();
        }


        System.out.println(topic);
    }
}
