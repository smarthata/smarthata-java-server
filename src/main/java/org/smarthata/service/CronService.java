package org.smarthata.service;

import org.smarthata.service.message.SmarthataMessage;
import org.smarthata.service.message.SmarthataMessageBroker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

import static org.smarthata.service.message.SmarthataMessage.SOURCE_CRON;

@Service
public class CronService {

    private static final String HEATING_TEMP_FLOOR_REQUEST = "/heating/temp/floor/request";

    @Autowired
    private SmarthataMessageBroker messageBroker;


    @Scheduled(fixedRate = 600_000)
    public void sendTime() {
        SmarthataMessage message = new SmarthataMessage("/config/date-time", new Date().toString(), SOURCE_CRON);
        messageBroker.broadcastSmarthataMessage(message);
    }

    @Scheduled(cron = "0 0 21 * * *")
    public void turnOffHeating() {
        changeFloorTemp("20");
    }

    @Scheduled(cron = "0 0 7,18 * * *")
    public void turnOnHeating() {
        changeFloorTemp("27");
    }

    @Scheduled(cron = "0 0 12 * * *")
    public void turnOnHeating25() {
        changeFloorTemp("25");
    }

    @Scheduled(cron = "0 0 8-10,12-14,17-19 * * *")
    public void sendStreetTemp() {
        SmarthataMessage message = new SmarthataMessage("/temp/street", "", SOURCE_CRON);
        messageBroker.broadcastSmarthataMessage(message);
    }

    private void changeFloorTemp(final String floorTemp) {
        SmarthataMessage message = new SmarthataMessage(HEATING_TEMP_FLOOR_REQUEST, floorTemp, SOURCE_CRON);
        messageBroker.broadcastSmarthataMessage(message);

        String text = String.format("Температура пола установлена в %s°C", floorTemp);
        message = new SmarthataMessage("/messages", text, SOURCE_CRON);
        messageBroker.broadcastSmarthataMessage(message);
    }

}
