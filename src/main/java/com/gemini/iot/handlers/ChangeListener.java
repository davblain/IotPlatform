package com.gemini.iot.handlers;

import com.gemini.iot.dto.OutEvent;
import com.gemini.iot.events.ActionEvent;
import com.gemini.iot.events.ChangeDataEvent;
import com.gemini.iot.events.TimeEvent;
import com.gemini.iot.exceptions.DeviceNotFoundException;
import com.gemini.iot.repository.MeasurementDao;
import com.gemini.iot.services.DeviceService;
import com.gemini.iot.services.OutboundChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ChangeListener {

    private final DeviceService deviceService;

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final OutboundChannel webSocketChannel;
    @Autowired
    private MeasurementDao measurementDao;

    @Autowired
    @Qualifier("mqttOutboundChannel")
    private OutboundChannel mqttOutbound;

    public ChangeListener(DeviceService deviceService,  @Qualifier("webSocketOutbound") OutboundChannel outboundChannel, SimpMessagingTemplate simpMessagingTemplate) {
        this.deviceService = deviceService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.webSocketChannel = outboundChannel;
    }

    @EventListener
    @Async
    void handle(ChangeDataEvent e) throws DeviceNotFoundException {
        deviceService.writeMeasurementData(e.getData());
        webSocketChannel.pushEvent(new OutEvent<>(e.getData().getUuid(),"data",e.getData().getMeasurement(), e.getData().getData()));

    }

    @EventListener
    @Async
    void handle(ActionEvent e)  {
        deviceService.writeActionData(e.getAction());
        mqttOutbound.pushEvent(new OutEvent<>(e.getAction().getUuid(),"action",e.getAction().getName(), e.getAction().getData()));
        webSocketChannel.pushEvent(new OutEvent<>(e.getAction().getUuid(),"action",e.getAction().getName(), e.getAction().getData()));
    }

    @EventListener
    void  handle(TimeEvent e) {
    }

}
