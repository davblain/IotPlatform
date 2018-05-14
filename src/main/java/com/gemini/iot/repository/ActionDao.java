package com.gemini.iot.repository;

import com.gemini.iot.dto.Action;
import com.gemini.iot.dto.ActionData;
import com.gemini.iot.dto.ChangeDataRequest;
import com.gemini.iot.models.Device;
import com.gemini.iot.models.definitions.ActionDefinition;
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
public class ActionDao {
    @Autowired
    InfluxDBTemplate<Point> influxDbTemplate;
    static public String actionPostfix="_action";

    public List<List<String>> findLastActionData(Device device) {
        String deviceUUID = device.getUuid().toString();
        String actions = device.getDeviceDefinition().getActionsDefinitions().stream()
                .map(ActionDefinition::getName)
                .map(name -> "\""+name+actionPostfix+"\"")
                .collect(Collectors.joining(","));
        String statement = "SELECT LAST(*) from " + actions + " where \"device_uuid\"= " + "'" + deviceUUID + "'" ;
        Query query = new Query(statement, influxDbTemplate.getDatabase());
        QueryResult result = influxDbTemplate.query(query, TimeUnit.MILLISECONDS);
        List<List<String>> values = result.getResults().stream().findAny().flatMap(res -> Optional.ofNullable(res.getSeries()))
                .map(series -> series.stream()
                        .sorted(Comparator.comparing(QueryResult.Series::getName))
                        .map(serie -> serie.getValues().get(0).stream()
                                .skip(1)
                                .filter(Objects::nonNull)
                                .map(Object::toString)
                                .collect(Collectors.toList()))
                        .collect(Collectors.toList())).orElse(new ArrayList<>());
        return values;
    }


    public void writeActionData(Action action) {
        Point.Builder pointBuilder = Point.measurement(action.getName()+actionPostfix);
        for (int i = 0; i < action.getData().size() ; i++) {
            pointBuilder.addField("field"+i,action.getData().get(i));
        }
        pointBuilder.tag("device_uuid",action.getUuid());
        if (action.getData().size()!= 0) {
            influxDbTemplate.write(pointBuilder.build());
        }
    }
    public  List<ActionData> selectActionData(Device device, Long from, Long to, Long count ) {
        String deviceUUID = device.getUuid().toString();

        String actions = device.getDeviceDefinition().getActionsDefinitions().stream()
                .map(ActionDefinition::getName)
                .map(name -> "\""+name+actionPostfix+"\"")
                .collect(Collectors.joining(","));

        Integer countOfValues = device.getDeviceDefinition().getActionsDefinitions().stream().
                map(ActionDefinition::getCountOfValue).max(Integer::compareTo).orElse(0);

        StringBuilder statement = new StringBuilder().append("SELECT MODE(*) ");
        statement.append("from ")
                .append(actions)
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
            List<ActionData> actionDataList = new ArrayList<>();
            for (int j = 0; j < res.getSeries().get(0).getValues().size() ; j++) {
                List<List<Object>> list = new ArrayList<>();
                ActionData actionData = new ActionData();
                List<Object> timedData = mainSeries.getValues().get(j);
                actionData.setTime((Double) timedData.get(0));
                for (int i = 0; i < res.getSeries().size() ; i++) {
                    List<Object> buf  =  res.getSeries().get(i).getValues().get(j).stream().skip(1).collect(Collectors.toList());
                    list.add(buf);
                }
                actionData.setTimedData(list);
                actionDataList.add(actionData);
            }
            return actionDataList;
        }).orElse(new ArrayList<>());



    }

}
