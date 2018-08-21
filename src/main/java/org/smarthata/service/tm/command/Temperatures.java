package org.smarthata.service.tm.command;

import org.smarthata.model.Measure;
import org.smarthata.model.Sensor;
import org.smarthata.repository.MeasureRepository;
import org.smarthata.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
public class Temperatures implements Command {

    private static final String TEMPERATURES = "temp";

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private MeasureRepository measureRepository;

    @Override
    public boolean isProcessed(final String name) {
        return TEMPERATURES.equalsIgnoreCase(name);
    }

    @Override
    public BotApiMethod answer(final List<String> path, final String chatId, final Integer messageId) {

        Sensor sensor = sensorRepository.findByIdOrElseThrow(13);
        Measure measure = measureRepository.findTopBySensorOrderByDateDesc(sensor);

        String text = String.format("Уличная температура: %.1f°C (%d мин. назад)", measure.getValue(), getMinutesAgo(measure.getDate()));

        return aSimpleSendMessage(chatId, text);
    }

    private long getMinutesAgo(final Date date) {
        return TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - date.getTime());
    }

}
