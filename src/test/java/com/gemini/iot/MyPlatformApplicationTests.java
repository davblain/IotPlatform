package com.gemini.iot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gemini.iot.configuration.InfluxConfiguration;
import com.gemini.iot.configuration.WebSocketConf;
import com.gemini.iot.dto.*;
import com.gemini.iot.events.ChangeDataEvent;
import com.gemini.iot.handlers.ChangeListener;
import com.gemini.iot.models.Device;
import com.gemini.iot.models.definitions.ActionDefinition;
import com.gemini.iot.models.definitions.MeasurementDefinition;
import com.gemini.iot.models.definitions.DeviceDefinition;
import com.gemini.iot.mqtt.MqqtInboundConfiguration;
import com.gemini.iot.mqtt.MqttController;
import com.gemini.iot.mqtt.MqttOutBoundConfiguration;
import com.gemini.iot.outbound.WebSocketOutboundChannel;
import com.gemini.iot.repository.*;
import com.gemini.iot.secure.TokenUtils;
import com.gemini.iot.services.DeviceService;
import com.gemini.iot.services.UserService;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.data.influxdb.InfluxDBTemplate;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {DeviceService.class,
		InfluxConfiguration.class,
		MeasurementDao.class,
		UserService.class,
		MyPlatformApplication.class,
		ChangeListener.class,
		WebSocketConf.class,
		TokenUtils.class,
		MqttOutBoundConfiguration.class,
		MqqtInboundConfiguration.class,
		MqttController.class,
		ActionDao.class,
		IntegrationAutoConfiguration.class,
		WebSocketOutboundChannel.class})
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
	@Autowired
	ApplicationEventPublisher publisher;
	@Autowired ActionDao actionDao;
	@Autowired
	MqttPahoClientFactory mqttPahoClientFactory;
	@Autowired
	IMqttClient mqttAsyncClient;
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
		deviceDefinitionDto.setMeasuresDefinitions(Arrays.asList(temperatureDefinition.getName(), humidityDefinition.getName()));
		deviceDefinitionDto.setActionsDefinitions(Arrays.asList(actionDefinition.getName()));
		deviceDefinitionDto.setName("TestDevice");
		DeviceDefinition deviceDefinition = deviceService.registerNewDeviceDefinition(deviceDefinitionDto);
		Device device = deviceService.registerDevice(deviceDefinition.getId());
		testDeviceUUid = device.getUuid();
		Point p = Point.measurement(temperatureDefinition.getName()+MeasurementDao.measurePostfix)
				.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
				.tag("device_uuid", device.getUuid().toString())
				.addField("field0", 10)
				.build();
		influxDbTemplate.write(p);
		Point p2 = Point.measurement(humidityDefinition.getName()+ MeasurementDao.measurePostfix)
				.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
				.tag("type", "measure")
				.tag("device_uuid", device.getUuid().toString())
				.addField("field0", 20)
				.addField("field1", 30)
				.build();
		influxDbTemplate.write(p2);
		Point actionp = Point.measurement(actionDefinition.getName()+ActionDao.actionPostfix)
				.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
				.tag("device_uuid", device.getUuid().toString())
				.addField("field0", "on")
				.build();
		influxDbTemplate.write(actionp);

	}


	@Test
	public void testDefinitions() {
		Device device = deviceDao.findOne(testDeviceUUid);
		MeasurementDefinition measurementDefinition = device.getDeviceDefinition().getMeasuresDefinitions().get(0);
		MeasurementDefinition  humidityDefinition = device.getDeviceDefinition().getMeasuresDefinitions().get(1);
		Point p = Point.measurement(measurementDefinition.getName()+MeasurementDao.measurePostfix)
				.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
				.tag("device_uuid",device.getUuid().toString())
				.addField("field0",10)
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
	public void testListener() {
		ChangeDataRequest data = new ChangeDataRequest();
		data.setData(Arrays.asList(10.0));
		data.setMeasurement("temperature");
		data.setUuid(testDeviceUUid.toString());
		publisher.publishEvent(new ChangeDataEvent(this,data));
	}
	@Test
	public void testGetMeasureData() {
		Device device = deviceDao.findOne(testDeviceUUid);
		MeasurementDefinition temperatureDefinition = device.getDeviceDefinition().getMeasuresDefinitions().get(0);
		Point p = Point.measurement(temperatureDefinition.getName())
				.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
				.tag("type","measure")
				.tag("device_uuid",device.getUuid().toString())
				.addField("field0",15)
				.build();
		influxDbTemplate.write(p);
		List<MeasurementData> result = measurementDao.selectMeasurementData(device,null,null,null);
	}
	@Test
	public void getLastActions(){
		Device device = deviceDao.findOne(testDeviceUUid);
		List<List<String>> actionData = actionDao.findLastActionData(device);
		List<ActionData> dataList = actionDao.selectActionData(device,null,null,null);

	}
	@Test
	public void testMqtt() throws MqttException, JsonProcessingException, InterruptedException {
		mqttAsyncClient.connect();
		MqttEvent mqttEvent = new MqttEvent();
		mqttEvent.setType("temperature");
		mqttEvent.setValues(Arrays.asList(10.0));
		MqttMessage message = new MqttMessage();
		ObjectMapper objectMapper = new ObjectMapper();
		message.setPayload(objectMapper.writeValueAsBytes(mqttEvent));
		mqttAsyncClient.publish("data/"+testDeviceUUid.toString(),message);
		mqttAsyncClient.disconnect();
	}

	@Test
	public void testGetDevice() {
		influxDbTemplate.createDatabase();

		Point p = Point.measurement("temperature")
				.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
				.tag("type","measure")
				.tag("sensor_uuid","testUUId")
				.addField("field0",12)
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
