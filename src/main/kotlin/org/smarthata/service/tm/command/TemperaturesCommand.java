package org.smarthata.service.tm.command;

import org.smarthata.model.Measure;
import org.smarthata.model.Sensor;
import org.smarthata.repository.MeasureRepository;
import org.smarthata.repository.SensorRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import java.util.Date;
import java.util.concurrent.TimeUnit;


@Service
public class TemperaturesCommand extends AbstractCommand {

    private static final String TEMPERATURES = "temp";

    private final SensorRepository sensorRepository;
    private final MeasureRepository measureRepository;

    public TemperaturesCommand(SensorRepository sensorRepository, MeasureRepository measureRepository) {
        super(TEMPERATURES);
        this.sensorRepository = sensorRepository;
        this.measureRepository = measureRepository;
    }

    @Override
    public BotApiMethod<?> answer(CommandRequest request) {

        String text = createTextLineForSensor(13, "Улица")
                + createTextLineForSensor(2000239, "Спальня")
                + createTextLineForSensor(7, "Первый этаж")
                + createTextLineForSensor(4379072, "Ванная")
                + createTextLineForSensor(83720510, "Гараж")
                + createTextLineForSensor(85948634, "Мастерская");

        return aSimpleSendMessage(request.chatId, text).build();
    }

    private String createTextLineForSensor(int sensorId, String name) {
        Sensor sensor = sensorRepository.findByIdOrElseThrow(sensorId);
        Measure measure = measureRepository.findTopBySensorOrderByDateDesc(sensor);
        return String.format(name + " : %.1f°C (%d мин. назад)\n", measure.value, createMinutesAgo(measure.date));
    }

    private long createMinutesAgo(final Date date) {
        return TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - date.getTime());
    }

}
