package com.gemini.iot.inbound;


import com.gemini.iot.dto.ChangeDataRequest;
import com.gemini.iot.events.ChangeDataEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("send_data")
public class PublishDataController {
    private final ApplicationEventPublisher publisher;

    public PublishDataController(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    String publishData(@RequestBody ChangeDataRequest data) {
        publisher.publishEvent(new ChangeDataEvent(this,data));
        return "SUCCESS";
    }
}
