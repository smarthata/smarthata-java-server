package org.smarthata.service;

import org.smarthata.service.message.SmarthataMessage;
import org.smarthata.service.message.SmarthataMessageBroker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;

import static org.smarthata.service.message.SmarthataMessage.SOURCE_CRON;

@Service
public class CronService {

    private final SmarthataMessageBroker messageBroker;

    @Autowired
    public CronService(SmarthataMessageBroker messageBroker) {
        this.messageBroker = messageBroker;
    }


    @Scheduled(cron = "0 * * * * *")
    public void sendTime() {
        LocalTime now = LocalTime.now(ZoneId.systemDefault());
        Integer secondOfDay = now.toSecondOfDay();
        SmarthataMessage message = new SmarthataMessage("/second-of-day", secondOfDay.toString(), SOURCE_CRON);
        message.setRetained(true);
        messageBroker.broadcastSmarthataMessage(message);
    }

    @Scheduled(cron = "0 0 8-10,12-14,17-19 * * *")
    public void sendStreetTemp() {
        SmarthataMessage message = new SmarthataMessage("/temp/street", "", SOURCE_CRON);
        messageBroker.broadcastSmarthataMessage(message);
    }

}
