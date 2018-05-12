package com.gemini.iot;

import com.gemini.iot.configuration.InfluxConfiguration;
import com.gemini.iot.dto.DeviceDefinitionDto;
import com.gemini.iot.dto.DeviceDto;
import com.gemini.iot.dto.MeasurementData;
import com.gemini.iot.models.Device;
import com.gemini.iot.models.definitions.ActionDefinition;
import com.gemini.iot.models.definitions.MeasurementDefinition;
import com.gemini.iot.models.definitions.DeviceDefinition;
import com.gemini.iot.repository.*;
import com.gemini.iot.services.DeviceService;
import com.gemini.iot.services.UserService;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.influxdb.InfluxDBTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {DeviceService.class,InfluxConfiguration.class,MeasurementDao.class, UserService.class, MyPlatformApplication.class})
@SpringBootTest(classes = MyPlatformApplication.class)
@DataJpaTest

public class MyPlatformApplicationTests {

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
	ActionDefinitionDao actionDefinitionDao;
	@Autowired
	DeviceService deviceService;
	@Autowired
	InfluxDBTemplate<Point> influxDbTemplate;
	@Autowired
	TestEntityManager testEntityManager;
	UUID testDeviceUUid;
	@Autowired MeasurementDao measurementDao;
	@Before
	public void setUp() {
		influxDbTemplate.createDatabase();
		MeasurementDefinition temperatureDefinition = new MeasurementDefinition();
		temperatureDefinition.setName("temperature");
		temperatureDefinition.setCountOfMeasure(1);
		MeasurementDefinition humidityDefinition = new MeasurementDefinition();
		humidityDefinition.setName("humidity");
		humidityDefinition.setCountOfMeasure(2);
		temperatureDefinition = measurementDefinitionDao.save(temperatureDefinition);
		humidityDefinition = measurementDefinitionDao.save(humidityDefinition);
		ActionDefinition actionDefinition = new ActionDefinition();
		actionDefinition.setName("switch_on/of");
		actionDefinition.setCountOfValue(1);
		actionDefinition = actionDefinitionDao.save(actionDefinition);
		DeviceDefinitionDto deviceDefinitionDto = new DeviceDefinitionDto();
		deviceDefinitionDto.setMeasuresDefinitions(Arrays.asList(temperatureDefinition.getName(),humidityDefinition.getName()));
		deviceDefinitionDto.setActionsDefinitions(Arrays.asList(actionDefinition.getName()));
		deviceDefinitionDto.setName("TestDevice");
		DeviceDefinition deviceDefinition = deviceService.registerNewDeviceDefinition(deviceDefinitionDto);
		Device device = deviceService.registerDevice(deviceDefinition.getId());
		testDeviceUUid = device.getUuid();
		Point p = Point.measurement(temperatureDefinition.getName())
				.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
				.tag("device_uuid",device.getUuid().toString())
				.addField("value_0",10)
				.build();
		influxDbTemplate.write(p);
		Point p2 = Point.measurement(humidityDefinition.getName())
				.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
				.tag("device_uuid",device.getUuid().toString())
				.addField("value_0",20)
				.addField("value_1",30)
				.build();
		influxDbTemplate.write(p2);
	}


	@Test
	public void testDefinitions() {
		Device device = deviceDao.findOne(testDeviceUUid);
		MeasurementDefinition measurementDefinition = device.getDeviceDefinition().getMeasuresDefinitions().get(0);
		MeasurementDefinition  humidityDefinition = device.getDeviceDefinition().getMeasuresDefinitions().get(1);
		Point p = Point.measurement(measurementDefinition.getName())
				.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
				.tag("device_uuid",device.getUuid().toString())
				.addField("0",10)
				.build();
		influxDbTemplate.write(p);

		String statement = "SELECT \"0\" from " + measurementDefinition.getName()+ " where \"device_uuid\"= "+"'"+testDeviceUUid+"'";
		QueryResult result = influxDbTemplate.query(new Query(statement,influxDbTemplate.getDatabase()),TimeUnit.MILLISECONDS);
	}

	@Test
	public void testGetDevices() {
		Device device = deviceDao.findOne(testDeviceUUid);
		List<DeviceDto> deviceDtos = deviceService.getDevices();

	}
	@Test
	public void  testGetDeviceById() {
		DeviceDto deviceDto = deviceService.getDeviceById(testDeviceUUid);
	}

	@Test
	public void testGetMeasureData() {
		Device device = deviceDao.findOne(testDeviceUUid);
		MeasurementDefinition temperatureDefinition = device.getDeviceDefinition().getMeasuresDefinitions().get(0);
		Point p = Point.measurement(temperatureDefinition.getName())
				.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
				.tag("device_uuid",device.getUuid().toString())
				.addField("value_0",15)
				.build();
		influxDbTemplate.write(p);
		List<MeasurementData> result = measurementDao.selectMeasurementData(device,null,null,null);
	}

	public void testGetDevice() {
		influxDbTemplate.createDatabase();

		Point p = Point.measurement("temperature")
				.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
				.tag("sensor_uuid","testUUId")
				.addField("value_0",12)
				.build();
	}
	@TestConfiguration
	static class Config {

		@Bean
		public ModelMapper restTemplateBuilder() {
			return new ModelMapper();
		}

	}
}
