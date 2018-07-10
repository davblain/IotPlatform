package com.gemini.iot.controllers;

import com.gemini.iot.dto.*;
import com.gemini.iot.events.ActionEvent;
import com.gemini.iot.events.ChangeDataEvent;
import com.gemini.iot.models.Device;
import com.gemini.iot.models.definitions.ActionDefinition;
import com.gemini.iot.models.definitions.DeviceDefinition;
import com.gemini.iot.models.definitions.MeasurementDefinition;
import com.gemini.iot.repository.ActionDefinitionDao;
import com.gemini.iot.repository.DeviceDao;
import com.gemini.iot.repository.MeasurementDao;
import com.gemini.iot.repository.MeasurementDefinitionDao;
import com.gemini.iot.services.DeviceService;
import com.gemini.iot.services.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api")
public class DeviceController {

    final private UserService userService;
    final private DeviceDao deviceDao;
    final private ModelMapper modelMapper;
    final private DeviceService deviceService;
    final private  ActionDefinitionDao actionDefinitionDao;
    final private MeasurementDefinitionDao measurementDefinitionDao;
    private final ApplicationEventPublisher publisher;

    public DeviceController(UserService userService, DeviceDao deviceDao, DeviceService deviceService,
                            ModelMapper modelMapper, ActionDefinitionDao actionDefinitionDao, MeasurementDefinitionDao measurementDefinitionDao, ApplicationEventPublisher publisher){
        this.userService = userService;
        this.deviceDao = deviceDao;
        this.modelMapper = modelMapper;
        this.measurementDefinitionDao= measurementDefinitionDao;
        this.deviceService  = deviceService;
        this.publisher = publisher;
        this.actionDefinitionDao = actionDefinitionDao;
    }



    @RequestMapping(value = "device_def",method = RequestMethod.POST)
    public DeviceDefinition registerNewDeviceDefinition(@RequestBody DeviceDefinitionDto definitionDto) {
        return deviceService.registerNewDeviceDefinition(definitionDto);
    }

    @RequestMapping(value = "action_def",method = RequestMethod.POST)
    public ActionDefinition registerNewActionDefinition(ActionDefinition actionDefinition) {
        return actionDefinitionDao.save(actionDefinition);
    }
    @RequestMapping(value = "measure_def",method = RequestMethod.POST)
    public MeasurementDefinition registerNewMeasurementDefinition(@RequestBody  MeasurementDefinition measurementDefinition) {
        return  measurementDefinitionDao.save(measurementDefinition);
    }

    @RequestMapping(value = "device", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Device registerDevice(Authentication authentication, @RequestBody Long model) {
        return deviceService.registerDevice(model);
    }

    @RequestMapping(value = "device", method = RequestMethod.GET)
    public List<DeviceDto> getDevices(Authentication authentication) {
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))){
            return deviceService.getDevices();
        }
        return deviceService.getDevicesByUsername(authentication.getName());
    }

    @RequestMapping("device/{uuid}")
    @ResponseBody
    DeviceDto getDevice(@PathVariable String uuid,Authentication authentication ) {
        if ((authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))||
                deviceService.checkForOwner(authentication.getName(), UUID.fromString(uuid))) {
            return deviceService.getDeviceById(UUID.fromString(uuid));
        }
        throw   new AccessDeniedException("You have not permissions for get device");
    }

    @RequestMapping("device/{uuid}/data")
    @ResponseBody
    List<MeasurementData> getDeviceData(@PathVariable String uuid,Authentication authentication,@RequestParam(value = "to",required = false) Long to,
                                        @RequestParam(value = "from",required = false) Long from,
                                        @RequestParam(value = "count",required = false) Long count ,
                                        @RequestParam(value = "group",required = false) Long group) {
        if ((authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))||
                deviceService.checkForOwner(authentication.getName(), UUID.fromString(uuid))) {
            return deviceService.getMeasurementDataOfDevice( UUID.fromString(uuid),to,from,count,group);
        }
        throw new AccessDeniedException("You have not permissions for get data");
    }

    @RequestMapping(value = "device/{uuid}/data",method = RequestMethod.POST)
    @ResponseBody
    void publishData(@RequestBody ChangeDataEventDto data, @PathVariable String uuid, Authentication authentication) {
        if ((authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) ||
                deviceService.checkForOwner(authentication.getName(), UUID.fromString(uuid))) {
            publisher.publishEvent(new ChangeDataEvent(this, data));
        }
    }


    @RequestMapping("device/{uuid}/action")
    @ResponseBody
    List<ActionData> getDeviceActionData(@PathVariable String uuid, Authentication authentication, @RequestParam(value = "to",required = false) Long to,
                                         @RequestParam(value = "from",required = false) Long from,
                                         @RequestParam(value = "count",required = false) Long count ) {
        if ((authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))||
                deviceService.checkForOwner(authentication.getName(), UUID.fromString(uuid))) {
            return deviceService.getActionDataOfDevice( UUID.fromString(uuid),to,from,count);
        }
        throw new AccessDeniedException("You have not permissions for get data");
    }


    @RequestMapping(value = "device/{uuid}/action", method = RequestMethod.POST)
    @ResponseBody
    void sendAction(@PathVariable String uuid, Authentication authentication, ActionDto actionRequest) {
        if ((authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))||
                deviceService.checkForOwner(authentication.getName(), UUID.fromString(uuid))) {
           publisher.publishEvent(new ActionEvent(this,new Action(uuid,actionRequest)));
        }
        throw new AccessDeniedException("You have not permissions for publish events");
    }


}
