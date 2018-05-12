package com.gemini.iot.controllers;

import com.gemini.iot.dto.DeviceDto;
import com.gemini.iot.dto.GroupDto;
import com.gemini.iot.exceptions.UserNotFoundException;
import com.gemini.iot.services.DeviceService;
import com.gemini.iot.services.GroupService;
import com.gemini.iot.services.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api")
public class GroupController {

    final private GroupService groupService;
    final private UserService userService;
    final private  DeviceService deviceService;
    public GroupController(GroupService groupService, UserService userService, DeviceService deviceService) {
        this.userService = userService;
        this.groupService = groupService;
        this.deviceService = deviceService;
    }

    @PostMapping("group")
    @ResponseBody
    GroupDto createGroup(Authentication authentication, @RequestBody String name) {
        return groupService.createGroup(userService.getUserByUsername(authentication.getName()),name);
    }
    @GetMapping("group/{id}")
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseBody
    GroupDto getGroup(@PathVariable(name = "id") String uuid, Authentication authentication) {
        if (groupService.getMembers(UUID.fromString(uuid)).stream().anyMatch(u -> u.getUsername().equals(authentication.getName()))) {
            return groupService.getGroupById(UUID.fromString(uuid));
        }
        else  throw  new AccessDeniedException("You have not permissions for get group");
    }
    @RequestMapping(value = "group/{uuid}/devices", method = RequestMethod.GET)
    public List<DeviceDto> getDevicesOfGroup(@PathVariable(name = "id") String uuid, Authentication authentication) {
        if (groupService.getMembers(UUID.fromString(uuid)).stream().anyMatch(u -> u.getUsername().equals(authentication.getName()))) {
            return deviceService.getDevicesByGroup(UUID.fromString(uuid));
        }
        else  throw  new AccessDeniedException("You have not permissions for get group");
    }
    @PostMapping("group/member")
    @ResponseBody
    String addMember(Authentication authentication, @RequestParam String username) throws UserNotFoundException {
       GroupDto group =  userService.getAdministratedGroup(authentication.getName());
       groupService.addMember(group.getUuid(),username);
       return "SUCCESS";
    }
    @DeleteMapping("group/member")
    @ResponseBody
    String deleteMember(Authentication authentication, @RequestParam String username) throws UserNotFoundException {
        GroupDto group =  userService.getAdministratedGroup(authentication.getName());
        groupService.deleteMember(group.getUuid(),username);
        return "SUCCESS";
    }

}
