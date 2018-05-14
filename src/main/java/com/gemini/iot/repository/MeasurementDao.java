package com.gemini.iot.repository;

import com.gemini.iot.dto.ChangeDataRequest;
import com.gemini.iot.dto.MeasurementData;
import com.gemini.iot.models.Device;
import com.gemini.iot.models.definitions.MeasurementDefinition;
import org.influxdb.annotation.Measurement;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.influxdb.InfluxDBTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
public class MeasurementDao {

    @Autowired
    InfluxDBTemplate<Point> influxDbTemplate;

    static public String measurePostfix="_data";
   public List<List<Object>> findLastMeasurementData(Device device) {
       String deviceUUID = device.getUuid().toString();
       String measures = device.getDeviceDefinition().getMeasuresDefinitions().stream()
               .map(MeasurementDefinition::getName)
               .map(name -> "\""+name+measurePostfix+"\"")
               .collect(Collectors.joining(","));
       String statement = "SELECT LAST(*) from " + measures + " where \"device_uuid\"= " + "'" + deviceUUID + "'" ;
       Query query = new Query(statement, influxDbTemplate.getDatabase());
       QueryResult result = influxDbTemplate.query(query, TimeUnit.MILLISECONDS);
       List<List<Object>> values = result.getResults().stream().findAny().flatMap(res -> Optional.ofNullable(res.getSeries()))
               .map(series -> series.stream()
                       .sorted(Comparator.comparing(QueryResult.Series::getName))
                       .map(serie -> serie.getValues().get(0).stream()
                               .skip(1)
                               .filter(Objects::nonNull)
                               .collect(Collectors.toList()))
               .collect(Collectors.toList())).orElse(new ArrayList<>());
       return values;
   }

   public void writeMeasurementData(ChangeDataRequest measuredData) {
       Point.Builder pointBuilder = Point.measurement(measuredData.getMeasurement()+measurePostfix);
       for (int i = 0; i < measuredData.getData().size() ; i++) {
           pointBuilder.addField("field"+i,measuredData.getData().get(i));
       }
       pointBuilder.tag("device_uuid",measuredData.getUuid());
       if (measuredData.getData().size()!= 0) {
           influxDbTemplate.write(pointBuilder.build());
       }
   }

   //TODO NEED REFACTOR PLEASE
   public  List<MeasurementData> selectMeasurementData(Device device,Long from, Long to, Long count ) {
       String deviceUUID = device.getUuid().toString();

       String measures = device.getDeviceDefinition().getMeasuresDefinitions().stream()
               .map(MeasurementDefinition::getName)
               .map(name -> "\""+name+measurePostfix+"\"")
               .collect(Collectors.joining(","));

       Integer countOfValues = device.getDeviceDefinition().getMeasuresDefinitions().stream().
               map(MeasurementDefinition::getCountOfMeasure).max(Integer::compareTo).orElse(0);

       StringBuilder statement = new StringBuilder().append("SELECT MEAN(/field/) ");
       statement.append("from ")
               .append(measures)
               .append(" where \"device_uuid\"= ")
               .append("'")
               .append(deviceUUID)
               .append("'");

       if (from != null) {
           statement.append(" AND ")
                   .append(" time ")
                   .append(" >= ")
                   .append("'")
                   .append(from)
                   .append("'");
       }
       if (to!= null) {
           statement.append(" AND ")
                   .append(" time ")
                   .append(" <= ")
                   .append("'")
                   .append(to)
                   .append("'");
       }
       statement.append(" GROUP by TIME(10s) FILL(previous)");
       Query query = new Query(statement.toString(), influxDbTemplate.getDatabase());
       QueryResult result = influxDbTemplate.query(query , TimeUnit.SECONDS);

       return result.getResults().stream().findAny().map( res -> {
           QueryResult.Series mainSeries = res.getSeries().get(0);
           List<MeasurementData> measurementDataList = new ArrayList<>();
           for (int j = 0; j < res.getSeries().get(0).getValues().size() ; j++) {
               List<List<Object>> list = new ArrayList<>();
               MeasurementData measurementData = new MeasurementData();
               List<Object> timedData = mainSeries.getValues().get(j);
               measurementData.setTime((Double) timedData.get(0));
               for (int i = 0; i < res.getSeries().size() ; i++) {
                   List<Object> buf  =  res.getSeries().get(i).getValues().get(j).stream().skip(1).collect(Collectors.toList());
                   list.add(buf);
               }
               measurementData.setTimedData(list);
               measurementDataList.add(measurementData);
           }
           return measurementDataList;
       }).orElse(new ArrayList<>());



   }





}
