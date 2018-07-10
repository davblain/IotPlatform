package com.gemini.iot.services;

import com.gemini.iot.dto.*;
import com.gemini.iot.exceptions.*;
import com.gemini.iot.models.Capability;
import com.gemini.iot.models.Device;
import com.gemini.iot.models.State;
import com.gemini.iot.models.User;
import com.gemini.iot.models.definitions.ActionDefinition;
import com.gemini.iot.models.definitions.DeviceDefinition;
import com.gemini.iot.models.definitions.MeasurementDefinition;
import com.gemini.iot.repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    @Autowired
    MeasurementDefinitionDao measurementDefinitionDao;
    @Autowired
    ActionDefinitionDao actionDefinitionDao;
    @Autowired
    DeviceDefinitionDao deviceDefinitionDao;
    @Autowired
    DeviceDao deviceDao;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    UserService userService;
    @Autowired
    MeasurementDao measurementDao;
    @Autowired
    GroupDao groupDao;
    @Autowired
    ActionDao actionDao;

    @Scheduled
    public void  updateData() {

    }
    @Transactional
   public  DeviceDefinition registerNewDeviceDefinition(DeviceDefinitionDto definitionDto) {
       List<MeasurementDefinition> measurementDefinitions = definitionDto.getMeasuresDefinitions().stream()
               .map( measure -> Optional.ofNullable(measurementDefinitionDao.findOne(measure))
                        .orElseThrow( () -> new NoSuchMeasureException(measure)))
               .collect(Collectors.toList());
       List<ActionDefinition> actionDefinitions = definitionDto.getActionsDefinitions().stream()
               .map( actionDefinition -> Optional.ofNullable(actionDefinitionDao.findOne(actionDefinition))
                       .orElseThrow(() -> new NoSuchActionException(actionDefinition)))
               .collect(Collectors.toList());
        DeviceDefinition deviceDefinition = new DeviceDefinition();
        deviceDefinition.setName( definitionDto.getName());
        deviceDefinition.setActionsDefinitions(actionDefinitions);
        deviceDefinition.setMeasuresDefinitions(measurementDefinitions);
        return deviceDefinitionDao.save(deviceDefinition);
    }

    @Transactional
    public Device registerDevice(Long definitionId) {
        Device device = new Device();
        DeviceDefinition deviceDefinition = Optional.ofNullable(deviceDefinitionDao.findOne(definitionId))
                .orElseThrow(() -> new DeviceDefinitionNotFoundException(definitionId));
        device.setDeviceDefinition(deviceDefinition);
        return deviceDao.save(device);
    }
    @Transactional
    public DeviceDto getDeviceById(UUID uuid) {
        return Optional.ofNullable(deviceDao.findOne(uuid)).map(this::mapToDto)
                .orElseThrow(() -> new DeviceNotFoundException(uuid.toString()));
    }

    public List<DeviceDto> getDevices() {
        return deviceDao.findAll().stream().map(this::mapToDto)
                    .collect(Collectors.toList());
    }

    public List<MeasurementData> getMeasurementDataOfDevice(UUID deviceUuid,Long from, Long to, Long count,Long groupInSeconds) {
        return Optional.ofNullable(deviceDao.findOne(deviceUuid)).
                map(d -> measurementDao.selectMeasurementData(d,from,to,count,groupInSeconds))
                .orElseThrow(() -> new DeviceNotFoundException(deviceUuid.toString()));
    }

    public List<ActionData> getActionDataOfDevice(UUID deviceUuid, Long from, Long to, Long count) {
        return Optional.ofNullable(deviceDao.findOne(deviceUuid)).
                map(d -> actionDao.selectActionData(d,from,to,count))
                .orElseThrow(() -> new DeviceNotFoundException(deviceUuid.toString()));
    }

    @Transactional
    public void writeActionData(Action action) {
        boolean isDefined = Optional.ofNullable(deviceDao.findOne(UUID.fromString(action.getUuid())))
                .map( device -> device.getDeviceDefinition().getActionsDefinitions().stream()
                        .map(ActionDefinition::getName).anyMatch( name -> name.equals(action.getName())))
                .orElse(false);
        if (isDefined) {
            actionDao.writeActionData(action);
        }
    }

    @Transactional
    public void writeMeasurementData(ChangeDataEventDto measuredData) {
       boolean isDefined = Optional.ofNullable(deviceDao.findOne(UUID.fromString(measuredData.getUuid())))
                .map( device -> device.getDeviceDefinition().getMeasuresDefinitions().stream()
                        .map(MeasurementDefinition::getName).anyMatch( name -> name.equals(measuredData.getMeasurement())))
                .orElse(false);
       if (isDefined) {
           measurementDao.writeMeasurementData(measuredData);
       }
    }

    @Transactional
    public boolean checkForOwner(String username,UUID uuid) {
        return userService.getUserByUsername(username).getGroups().stream()
                .flatMap( g -> g.getDevices().stream())
                .anyMatch(d -> d.getUuid().equals(uuid));
    }

    @Transactional
    public List<DeviceDto> getDevicesByUsername(String name) throws UserNotFoundException {
        User user = userService.getUserByUsername(name);
        return user.getGroups().stream()
                .flatMap( group -> group.getDevices().stream())
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<DeviceDto> getDevicesByGroup(@NotNull UUID groupUUID) {
        return  Optional.ofNullable(groupDao.findOne(groupUUID)).map(g -> deviceDao.findDevicesByOwner(g).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList()))
                .orElseThrow(()-> new GroupNotFoundException(groupUUID.toString()));
    }

    private DeviceDto mapToDto(Device device) {
        DeviceDto deviceDto = modelMapper.map(device, DeviceDto.class);
        extractAdditionalData(device,deviceDto);
        return deviceDto;
    }

    private State createStateForDevice(Device device) {
        State deviceState = new State();
        deviceState.setAction(actionDao.findLastActionData(device));
        deviceState.setData( measurementDao.findLastMeasurementData(device));
        return  deviceState;
    }
    private void extractAdditionalData(Device device,@NotNull DeviceDto deviceDto) {
        deviceDto.setState(createStateForDevice(device));
        deviceDto.setModel(device.getDeviceDefinition().getName());
        List<String> dataCapability = device.getDeviceDefinition().getMeasuresDefinitions().stream()
                .map(MeasurementDefinition::getName).sorted().collect(Collectors.toList());
        Capability capability = new Capability();
        capability.setData(dataCapability);
        deviceDto.setCapability(capability);
    }

}
