package com.gemini.iot.mqtt;

import lombok.extern.apachecommons.CommonsLog;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
public class MqttOutBoundConfiguration {

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.getConnectionOptions().setServerURIs(new String[] { "tcp://localhost:1883"});
        factory.setPersistence( new MemoryPersistence());
        return factory;
    }

    @Bean
    public IMqttClient mqttClient() throws MqttException {
       return mqttClientFactory().getClientInstance("tcp://localhost:1883","testClient2");
    }




}
