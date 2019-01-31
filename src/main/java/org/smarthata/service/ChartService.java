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
import java.util.stream.IntStream;

import static java.util.stream.Collectors.groupingBy;

@Service
public class ChartService {

    private final DeviceRepository deviceRepository;
    private final MeasureRepository measureRepository;

    @Autowired
    public ChartService(DeviceRepository deviceRepository, MeasureRepository measureRepository) {
        this.deviceRepository = deviceRepository;
        this.measureRepository = measureRepository;
    }

    public List<List> getChartData(Integer deviceId, int hours, int page, int points, Map<String, String> requestParams) {
        Device device = deviceRepository.findByIdOrElseThrow(deviceId);

        List<Measure> allMeasures = getMeasures(device, hours, page, points, requestParams);

        List<List> list = new ArrayList<>(allMeasures.size() / device.getSensors().size());
        List<String> headers = getHeaders(allMeasures);
        list.add(headers);

        SimpleDateFormat sdf = new SimpleDateFormat(getPattern(hours));

        Map<Date, List<Measure>> map = allMeasures.stream()
                .collect(groupingBy(Measure::getDate));
        map = new TreeMap<>(map);
        map.forEach((date, measuresLine) -> list.add(makeLine(headers, date, measuresLine, sdf)));

        return list;
    }

    private String getPattern(final int hours) {
        if (hours < 24) {
            return "HH:mm";
        } else if (hours < 24 * 7) {
            return "MM-dd HH:mm";
        } else if (hours < 24 * 7 * 4) {
            return "MM-dd";
        } else {
            return "yyyy-MM-dd";
        }
    }

    private List<Measure> getMeasures(Device device, int hours, int page, int points, final Map<String, String> requestParams) {
        Date startDate = getStartDate(hours, page);
        Date endDate = getEndDate(hours, page);
        List<Sensor> sensors = device.getSensors()
                .stream()
                .filter(sensor -> {
                    String requestParamsOrDefault = requestParams.getOrDefault(sensor.getName(), "1");
                    return !requestParamsOrDefault.equals("0");
                })
                .collect(Collectors.toList());
        final List<Measure> sourceList = measureRepository.findBySensorInAndDateBetweenOrderByDateAsc(sensors, startDate, endDate);

        points = Math.min(points, sourceList.size());
        List<Date> dates = buildChartDates(points, startDate, endDate);

        Map<Sensor, List<Measure>> measuresBySensors = sourceList.stream().collect(groupingBy(Measure::getSensor));

        return measuresBySensors.entrySet().stream()
                .map(sensorMeasures -> averageMeasuresForSensor(sensorMeasures.getKey(), sensorMeasures.getValue(), dates))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<Date> buildChartDates(int points, Date startDate, Date endDate) {
        long millisFullRange = endDate.getTime() - startDate.getTime();
        long millisStep = millisFullRange / points;

        return IntStream.rangeClosed(0, points)
                .mapToObj(i -> new Date(startDate.getTime() + i * millisStep))
                .collect(Collectors.toList());
    }

    private List<Measure> averageMeasuresForSensor(Sensor sensor, List<Measure> measures, List<Date> dates) {
        Map<Date, List<Measure>> datesWithMeasures = allocateMeasuresByDates(measures, dates);

        return datesWithMeasures.entrySet().stream()
                .map(measuresByDate -> createAverageMeasure(sensor, measuresByDate.getKey(), measuresByDate.getValue()))
                .collect(Collectors.toList());
    }

    private Map<Date, List<Measure>> allocateMeasuresByDates(final List<Measure> measures, final List<Date> dates) {
        Map<Date, List<Measure>> datesWithMeasures = dates.stream()
                .collect(Collectors.toMap(date -> date, measure -> new ArrayList<>(), CommonUtils.joinLists()));

        for (Measure measure : measures) {
            Date date = findClosestDate(dates, measure.getDate());
            datesWithMeasures.get(date).add(measure);
        }
        return datesWithMeasures;
    }

    private Measure createAverageMeasure(Sensor key, Date date, List<Measure> measures) {
        Measure measure = new Measure();
        measure.setSensor(key);
        measure.setDate(date);
        double value = measures.stream().mapToDouble(Measure::getValue).average().orElse(0);
        measure.setValue(CommonUtils.round(value, 1));
        return measure;
    }

    private Date findClosestDate(List<Date> dates, Date measureDate) {
        long minDiff = Long.MAX_VALUE;
        Date minDate = dates.get(0);

        for (Date date : dates) {
            long diff = Math.abs(date.getTime() - measureDate.getTime());
            if (diff < minDiff) {
                minDate = date;
                minDiff = diff;
            }
        }
        return minDate;
    }

    private List<Object> makeLine(List<String> headers, Date date, List<Measure> measuresLine, SimpleDateFormat sdf) {

        List<Object> line = new ArrayList<>(headers.size());
        line.add(sdf.format(date));

        Map<String, Double> measureByName = measuresLine.stream()
                .collect(Collectors.toMap(measure -> measure.getSensor().getName(), Measure::getValue));

        headers.stream()
                .filter(s -> !"Time".equalsIgnoreCase(s))
                .map(header -> measureByName.getOrDefault(header, 0.0))
                .forEach(line::add);

        return line;
    }

    private Date getStartDate(int hours, int page) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -hours * (page + 1));
        return cal.getTime();
    }

    private Date getEndDate(int hours, int page) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -hours * page);
        return cal.getTime();
    }

    private List<String> getHeaders(List<Measure> allMeasures) {
        List<String> headers = allMeasures.stream()
                .map(measure -> measure.getSensor().getName())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        headers.add(0, "Time");
        return headers;
    }

}
