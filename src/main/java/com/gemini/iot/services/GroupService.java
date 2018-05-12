package com.gemini.iot.services;

import com.gemini.iot.dto.GroupDto;
import com.gemini.iot.dto.DeviceDto;
import com.gemini.iot.dto.UserDto;
import com.gemini.iot.exceptions.DeviceNotFoundException;
import com.gemini.iot.exceptions.GroupNotFoundException;
import com.gemini.iot.exceptions.UserNotFoundException;
import com.gemini.iot.models.Device;
import com.gemini.iot.models.Group;
import com.gemini.iot.models.User;
import com.gemini.iot.repository.GroupDao;
import com.gemini.iot.repository.DeviceDao;
import com.gemini.iot.repository.UserDao;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GroupService {
    final private GroupDao groupDao;
    final private UserDao userDao;
    final private DeviceDao deviceDao;
    final private ModelMapper modelMapper;

    public GroupService(UserDao userDao, GroupDao groupDao, DeviceDao deviceDao, ModelMapper modelMapper) {
        this.deviceDao = deviceDao;
        this.groupDao = groupDao;
        this.userDao = userDao;
        this.modelMapper = modelMapper;
    }
    @Transactional
    public GroupDto createGroup(User user, String name) {
        return user.getGroups().stream()
                .filter(g -> g.getAdmin().equals(user))
                .findAny().map(g -> modelMapper.map(g,GroupDto.class))
                .orElse( modelMapper.map(groupDao.save(new Group(user,name)),GroupDto.class));
    }

    @Transactional
    public void addMember(@NotNull UUID uuid, @NotNull String username) {

        User user = Optional.ofNullable(userDao.findUserByUsername(username))
                .orElseThrow(()-> new UserNotFoundException(username));
        Group group = Optional.ofNullable(groupDao.findOne(uuid))
                .orElseThrow(()-> new GroupNotFoundException(uuid.toString()));
        group.getMembers().add(user);
        groupDao.save(group);
    }
    @Transactional
    public Device addDeviceToGroup(@NotNull UUID deviceUUID, @NotNull UUID groupUUID)  {
        Device device = Optional.ofNullable(deviceDao.findOne(deviceUUID))
                .orElseThrow(()-> new DeviceNotFoundException(deviceUUID.toString()));
        return Optional.ofNullable(groupDao.findOne(groupUUID)).map( g -> {
            device.setOwner(g);
            return deviceDao.save(device);
        }).orElseThrow(() -> new GroupNotFoundException(groupUUID.toString()));
    }
    @Transactional
    public GroupDto getGroupById(@NotNull UUID groupUuid) {
        return Optional.ofNullable(groupDao.findOne(groupUuid)).map(g -> modelMapper.map(g,GroupDto.class))
                .orElseThrow(() -> new GroupNotFoundException(groupUuid.toString()));
    }

    @Transactional
    public List<UserDto> getMembers(@NotNull UUID uuid) {
        return Optional.ofNullable(groupDao.getOne(uuid)).map(g -> g.getMembers().stream()
                .map(u -> modelMapper.map(u, UserDto.class))
                .collect(Collectors.toList()))
                .orElseThrow(()-> new GroupNotFoundException(uuid.toString()));
    }
    @Transactional
    public void deleteMember(@NotNull UUID groupUUID,@NotNull String username) {
        Group group = Optional.ofNullable(groupDao.findOne(groupUUID))
                .orElseThrow(()-> new GroupNotFoundException(groupUUID.toString()));
        User user = Optional.ofNullable(userDao.findUserByUsername(username))
                .orElseThrow(()-> new UserNotFoundException(username));
        user.getGroups().remove(group);
        userDao.save(user);
    }
}
