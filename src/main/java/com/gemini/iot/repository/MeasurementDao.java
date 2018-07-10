package com.gemini.iot.repository;

import com.gemini.iot.dto.ChangeDataEventDto;
import com.gemini.iot.dto.MeasurementData;
import com.gemini.iot.models.Device;
import com.gemini.iot.models.definitions.MeasurementDefinition;
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
       if (device.getDeviceDefinition().getMeasuresDefinitions().isEmpty()) return new ArrayList<>();
       String measures = device.getDeviceDefinition().getMeasuresDefinitions().stream()
               .map(MeasurementDefinition::getName)
               .map(name -> "\""+name+measurePostfix+"\"")
               .collect(Collectors.joining(","));
       String statement = "SELECT LAST(*) from " + measures + " where \"device_uuid\"= " + "'" + deviceUUID + "'" ;
       Query query = new Query(statement, influxDbTemplate.getDatabase());
       QueryResult result = influxDbTemplate.query(query, TimeUnit.SECONDS);
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

   public void writeMeasurementData(ChangeDataEventDto measuredData) {
       Point.Builder pointBuilder = Point.measurement(measuredData.getMeasurement()+measurePostfix);
       for (int i = 0; i < measuredData.getData().size() ; i++) {
           pointBuilder.addField("field"+i,measuredData.getData().get(i));
       }
       pointBuilder.tag("device_uuid",measuredData.getUuid());
       if (measuredData.getData().size()!= 0) {
           influxDbTemplate.write(pointBuilder.build());
       }
   }

   public List<Object> findLatestData(long to,String deviceUUID,String measurement) {
       StringBuilder statement = new StringBuilder().append( "SELECT /field/ from ")
               .append(measurement)
               .append(" where \"device_uuid\"= ")
               .append("'")
               .append(deviceUUID)
               .append("'")
               .append(" AND ")
               .append(" time")
               .append("<= ")
               .append(to)
               .append("ms")
               .append(" ORDER BY time DESC")
               .append(" LIMIT 1 ");
       Query query = new Query(statement.toString(), influxDbTemplate.getDatabase());
       QueryResult result = influxDbTemplate.query(query, TimeUnit.MILLISECONDS);
       return result.getResults().stream().findAny()
               .flatMap(res -> Optional.ofNullable(res.getSeries()))
               .map(series -> series.get(0).getValues().get(0).stream().skip(1)
                       .filter(Objects::nonNull)
                       .collect(Collectors.toList()))
               .orElse(new ArrayList<>());
   }
   //TODO NEED REFACTOR PLEASE
   public  List<MeasurementData> selectMeasurementData(Device device,Long from, Long to, Long count, Long groupInSeconds ) {
       if (device.getDeviceDefinition().getMeasuresDefinitions().isEmpty()) return new ArrayList<>();
       final String deviceUUID = device.getUuid().toString();
       String measures = device.getDeviceDefinition().getMeasuresDefinitions().stream()
               .map(MeasurementDefinition::getName)
               .map(name -> "\""+name+measurePostfix+"\"")
               .collect(Collectors.joining(","));

       Integer countOfValues = device.getDeviceDefinition().getMeasuresDefinitions().stream().
               map(MeasurementDefinition::getDimension).max(Integer::compareTo).orElse(0);

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
       if (to == null) {
           to = System.currentTimeMillis();
       }
       statement.append(" AND ")
                   .append(" time")
                   .append("<= ")
                   .append(to)
                   .append("ms");

       if (count == null) {
           count = 10L;

       }
       if (groupInSeconds == null){
           groupInSeconds = 10L;
       }
       statement.append(" GROUP by ")
               .append("TIME(")
               .append(groupInSeconds)
               .append("s)")
               .append(" ORDER BY time desc")
               .append(" LIMIT ")
               .append(count);

       Query query = new Query(statement.toString(), influxDbTemplate.getDatabase());
       QueryResult result = influxDbTemplate.query(query , TimeUnit.SECONDS);

       Long finalTo = to;
       return result.getResults().stream().findAny().map(res -> {

           if(res.getSeries()==null ||res.getSeries().isEmpty())  return  new ArrayList<MeasurementData>();
           QueryResult.Series mainSeries = res.getSeries().get(0);
           Map<String,List<Object>> latestData = device.getDeviceDefinition().getMeasuresDefinitions().stream().map(MeasurementDefinition::getName)
                   .map(name -> name+measurePostfix)
          .collect(Collectors.toMap(Object::toString,name -> findLatestData(finalTo,deviceUUID,name)));

           return mainSeries.getValues().stream()
                   .map(value -> value.get(0))
                   .map( time -> {
                       MeasurementData measurementData = new MeasurementData();
                       measurementData.setTime((Double) time);
                       List<List<Object>> list = device.getDeviceDefinition().getMeasuresDefinitions().stream()
                               .map(MeasurementDefinition::getName)
                               .sorted()
                               .map( name -> name+measurePostfix)
                               .map( name -> {
                                   return res.getSeries().stream().filter( series -> series.getName().equals(name))
                                           .findFirst()
                                           .map( series -> series.getValues().stream()
                                                   .filter( values -> values.get(0).equals(time))
                                                   .findFirst()
                                                   .map( values -> values.stream().skip(1).filter(Objects::nonNull).collect(Collectors.toList()))
                                                   .map(list1 -> {
                                                       if (list1.isEmpty()) {
                                                           return  latestData.get(name);
                                                       } else return list1;
                                                   })
                                                   .orElse(null))
                                           .orElse(null);
                                   }).collect(Collectors.toList());

                       measurementData.setTimedData(list);
                       return  measurementData;
                   }).collect(Collectors.toList());
       }).orElse(new ArrayList<>());
}





}
