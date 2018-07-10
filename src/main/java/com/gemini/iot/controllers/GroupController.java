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


    @PostMapping("group/{id}/member")
    @ResponseBody
    String addMember(@PathVariable(name = "id") String uuid,Authentication authentication, @RequestParam String username) throws UserNotFoundException {
       List<GroupDto> groups =  userService.getAdministratedGroup(authentication.getName());
       GroupDto group = groups.stream()
               .filter(groupDto ->  groupDto.getUuid().toString().equals(uuid))
               .findFirst().orElseThrow(() -> new AccessDeniedException("You have not permissions for add to group"));
       groupService.addMember(group.getUuid(),username);
       return "SUCCESS";
    }

    @DeleteMapping("group/{id}/member")
    @ResponseBody
    String deleteMember(@PathVariable(name = "id") String uuid,Authentication authentication, @RequestParam String username) throws UserNotFoundException {
        List<GroupDto> groups =  userService.getAdministratedGroup(authentication.getName());
        GroupDto group = groups.stream()
                .filter(groupDto ->  groupDto.getUuid().toString().equals(uuid))
                .findFirst().orElseThrow(() -> new AccessDeniedException("You have not permissions for delete from group"));
        groupService.deleteMember(group.getUuid(),username);
        return "SUCCESS";
    }

}
