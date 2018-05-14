package com.gemini.iot.handlers;

import com.gemini.iot.dto.ChangeDataRequest;
import com.gemini.iot.dto.OutEvent;
import com.gemini.iot.events.ChangeDataEvent;
import com.gemini.iot.exceptions.DeviceNotFoundException;
import com.gemini.iot.models.Device;
import com.gemini.iot.repository.DeviceDao;
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

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Transactional
public class ChangeListener {

    private final DeviceService deviceService;

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final OutboundChannel webSocketChannel;
    @Autowired
    private MeasurementDao measurementDao;
    public ChangeListener(DeviceService deviceService,  @Qualifier("webSocketOutbound") OutboundChannel outboundChannel, SimpMessagingTemplate simpMessagingTemplate) {
        this.deviceService = deviceService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.webSocketChannel = outboundChannel;
    }

    @EventListener
    @Async
    void handle(ChangeDataEvent e) throws DeviceNotFoundException {
        deviceService.writeMeasurementData(e.getData());
        webSocketChannel.pushEvent(new OutEvent<>(e.getData().getUuid(),"change_data",e.getData().getMeasurement(), e.getData().getData()));

    }

}
