package com.gemini.iot.mqtt;

import lombok.extern.apachecommons.CommonsLog;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.mosquitto.uri}")
    public String uri;
    @Value("${spring.mosquitto.username}")
    public String username;
    @Value("${spring.mosquitto.password}")
    public String password;
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setServerURIs(uri);
        factory.setUserName(username);
        factory.setPassword(password);
        factory.setPersistence( new MemoryPersistence());
        return factory;
    }

    @Bean
    public IMqttClient mqttClient() throws MqttException {
       return mqttClientFactory().getClientInstance(uri,"testClient2");
    }

    @Bean
    public  MqttConnectOptions getConnectionOptions() {
        return  mqttClientFactory().getConnectionOptions();
    }




}
