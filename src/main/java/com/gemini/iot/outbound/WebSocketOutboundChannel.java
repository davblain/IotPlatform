package com.gemini.iot.outbound;

import com.gemini.iot.dto.OutEvent;
import com.gemini.iot.repository.DeviceDao;
import com.gemini.iot.services.OutboundChannel;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component("webSocketOutbound")
public class WebSocketOutboundChannel implements OutboundChannel {


    final private SimpMessagingTemplate simpMessagingTemplate;
    final private DeviceDao deviceDao;
    WebSocketOutboundChannel(SimpMessagingTemplate simpMessagingTemplate, DeviceDao deviceDao) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.deviceDao = deviceDao;
    }

    @Transactional
    @Override
    public void pushEvent(OutEvent event) {

        Optional.ofNullable(deviceDao.findOne(UUID.fromString(event.getUuid())))
                .flatMap(device -> Optional.ofNullable(device.getOwner()))
                .flatMap( owner -> Optional.ofNullable(owner.getMembers()))
                .ifPresent( members -> members.forEach(m -> simpMessagingTemplate.convertAndSendToUser(m.getUsername(),"/queue/private",event)));
    }


}
