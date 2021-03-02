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
public class TemperaturesCommand extends AbstractCommand {

    private static final String TEMPERATURES = "temp";

    private final SensorRepository sensorRepository;
    private final MeasureRepository measureRepository;

    @Autowired
    public TemperaturesCommand(SensorRepository sensorRepository, MeasureRepository measureRepository) {
        super(TEMPERATURES);
        this.sensorRepository = sensorRepository;
        this.measureRepository = measureRepository;
    }

    @Override
    public BotApiMethod<?> answer(final List<String> path, final String chatId, final Integer messageId) {

        Sensor sensor = sensorRepository.findByIdOrElseThrow(13);
        Measure measure = measureRepository.findTopBySensorOrderByDateDesc(sensor);

        String text = String.format("Street temp: %.1f°C (%d мин. назад)", measure.getValue(), getMinutesAgo(measure.getDate()));

        return aSimpleSendMessage(chatId, text).build();
    }

    private long getMinutesAgo(final Date date) {
        return TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - date.getTime());
    }

}
