package org.smarthata.service;

import org.smarthata.service.message.SmarthataMessage;
import org.smarthata.service.message.SmarthataMessageBroker;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;

import static org.smarthata.service.message.EndpointType.MQTT;
import static org.smarthata.service.message.EndpointType.SYSTEM;

@Service
public class CronMqttUpdater {

    private final SmarthataMessageBroker messageBroker;

    public CronMqttUpdater(SmarthataMessageBroker messageBroker) {
        this.messageBroker = messageBroker;
    }


    @Scheduled(cron = "0 * * * * *")
    public void sendSecondOfDay() {
        int secondOfDay = calcSecondOfDay();
        messageBroker.broadcast(new SmarthataMessage("/time/second-of-day", Integer.toString(secondOfDay), SYSTEM, MQTT, true));
    }

    @Scheduled(cron = "0 * * * * *")
    public void sendMinuteOfDay() {
        int minuteOfDay = calcSecondOfDay() / 60;
        messageBroker.broadcast(new SmarthataMessage("/time/minute-of-day", Integer.toString(minuteOfDay), SYSTEM, MQTT, true));
    }

    private int calcSecondOfDay() {
        return LocalTime.now(ZoneId.systemDefault()).toSecondOfDay();
    }

}
