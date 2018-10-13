package org.smarthata.service;

import org.smarthata.model.Device;
import org.smarthata.model.Measure;
import org.smarthata.model.Sensor;
import org.smarthata.repository.DeviceRepository;
import org.smarthata.repository.MeasureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChartService {

    private final DeviceRepository deviceRepository;
    private final MeasureRepository measureRepository;

    @Autowired
    public ChartService(DeviceRepository deviceRepository, MeasureRepository measureRepository) {
        this.deviceRepository = deviceRepository;
        this.measureRepository = measureRepository;
    }

    public List<List> getChartData(Integer deviceId, int hours) {
        Device device = deviceRepository.findByIdOrElseThrow(deviceId);

        Date startDate = getStartDate(hours);
        List<Measure> allMeasures = measureRepository.findBySensorInAndDateAfterOrderByDateAsc(device.getSensors(), startDate);

        List<List> list = new ArrayList<>(allMeasures.size() / device.getSensors().size());
        List<String> headers = getHeaders(device.getSensors());
        list.add(headers);

        Map<Date, List<Measure>> map = allMeasures.stream()
                .collect(Collectors.groupingBy(Measure::getDate));
        map = new TreeMap<>(map);
        map.forEach((date, measuresLine) -> list.add(makeLine(headers, date, measuresLine)));

        return list;
    }

    private List<Object> makeLine(List<String> headers, Date date, List<Measure> measuresLine) {
        List<Object> line = new ArrayList<>(headers.size());
        line.add(new SimpleDateFormat("HH:mm").format(date));

        Map<String, Double> measureByName = measuresLine.stream()
                .collect(Collectors.toMap(measure -> measure.getSensor().getName(), Measure::getValue));

        headers.stream()
                .filter(s -> !"Time".equalsIgnoreCase(s))
                .map(header -> measureByName.getOrDefault(header, 0.0))
                .forEach(line::add);

        return line;
    }

    private Date getStartDate(int hours) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -hours);
        return cal.getTime();
    }

    private List<String> getHeaders(List<Sensor> sensors) {
        List<String> headers = sensors.stream()
                .map(Sensor::getName)
                .sorted()
                .collect(Collectors.toList());
        headers.add(0, "Time");
        return headers;
    }

}