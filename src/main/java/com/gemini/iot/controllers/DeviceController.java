package com.gemini.iot.controllers;

import com.gemini.iot.dto.DeviceDto;
import com.gemini.iot.dto.MeasurementData;
import com.gemini.iot.models.Device;
import com.gemini.iot.repository.DeviceDao;
import com.gemini.iot.services.DeviceService;
import com.gemini.iot.services.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Id;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api")
public class DeviceController {

    final private UserService userService;
    final private DeviceDao deviceDao;
    final private ModelMapper modelMapper;
    final private DeviceService deviceService;

    public DeviceController(UserService userService, DeviceDao deviceDao, DeviceService deviceService,
                            ModelMapper modelMapper){
        this.userService = userService;
        this.deviceDao = deviceDao;
        this.modelMapper = modelMapper;
        this.deviceService  = deviceService;
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
                                        @RequestParam(value = "count",required = false) Long count ) {
        if ((authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))||
                deviceService.checkForOwner(authentication.getName(), UUID.fromString(uuid))) {
            return deviceService.getMeasurementDataOfDevice( UUID.fromString(uuid),to,from,count);
        }
        throw new AccessDeniedException("You have not permissions for get data");
    }


}
