package com.gemini.iot;


import com.gemini.iot.models.Device;
import com.gemini.iot.models.Group;
import com.gemini.iot.models.Role;
import com.gemini.iot.models.User;
import com.gemini.iot.models.definitions.MeasurementDefinition;
import com.gemini.iot.models.definitions.DeviceDefinition;
import com.gemini.iot.repository.*;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.influxdb.InfluxDBTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Component
public class InitialDataLoader implements
        ApplicationListener<ContextRefreshedEvent> { ;
    @Value("${load.already-setup}") String  alreadySetup;

    @Autowired
    GroupDao groupDao;
    @Autowired
    RoleDao roleDao;
    @Autowired
    DeviceDao deviceDao;
    @Autowired
    MeasurementDefinitionDao measurementDefinitionDao;
    @Autowired
    DeviceDefinitionDao deviceDefinitionDao;
    @Autowired
    UserDao userDao;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private InfluxDBTemplate<Point> influxDBTemplate;
    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (!alreadySetup.equals("true")) {
            influxDBTemplate.createDatabase();
            createRoleIfNotFound("ROLE_ADMIN");
            createRoleIfNotFound("ROLE_USER");
            Role userRole = roleDao.findByName("ROLE_USER");
            Role adminRole = roleDao.findByName("ROLE_ADMIN");
            User user1 = new User();
            user1.setUsername("admin");
            user1.setPassword(passwordEncoder.encode("587238"));
            user1.setRoles(Arrays.asList(adminRole,userRole));
            user1 = userDao.save(user1);
            Group group =  new Group(user1,"");
            //Device device = deviceDao.save(new Device());
            group = groupDao.save(group);
            //device.setOwner(group);
            //deviceDao.save(device);
            User user2 = new User();
            user2.setUsername("davblain");
            user2.setPassword(passwordEncoder.encode("pass"));
            user2.setRoles(Arrays.asList(userRole));
            user2.getGroups().add(group);
            userDao.save(user2);
            MeasurementDefinition temperatureDefinition = new MeasurementDefinition();
            temperatureDefinition.setName("temperature");
            temperatureDefinition.setCountOfMeasure(1);
            temperatureDefinition = measurementDefinitionDao.save(temperatureDefinition);
            DeviceDefinition deviceDefinition = new DeviceDefinition();
            deviceDefinition.setName("temperatureTestSensor");
            deviceDefinition.setMeasuresDefinitions(Arrays.asList(temperatureDefinition));
            deviceDefinition = deviceDefinitionDao.save(deviceDefinition);
            Device device = new Device();
            device.setDeviceDefinition(deviceDefinition);
            device = deviceDao.save(device);
            System.out.println(device.getUuid());
            alreadySetup = "true";
        }

    }


    @Transactional
    public Role createRoleIfNotFound(String name) {

        Role role = roleDao.findByName(name);
        if (role == null) {
            role = new Role(name);
            roleDao.save(role);
        }
        return role;
    }
}