package org.smarthata.service;

import org.smarthata.service.message.SmarthataMessage;
import org.smarthata.service.message.SmarthataMessageBroker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static org.smarthata.service.message.EndpointType.MQTT;
import static org.smarthata.service.message.EndpointType.SYSTEM;

@Service
public class CronService {

    private final SmarthataMessageBroker messageBroker;
    private final WeatherService weatherService;

    @Autowired
    public CronService(SmarthataMessageBroker messageBroker, WeatherService weatherService) {
        this.messageBroker = messageBroker;
        this.weatherService = weatherService;
    }

    @Scheduled(cron = "0 0 8-10,12-14,17-19 * * *")
    public void sendStreetTemp() {
        SmarthataMessage message = new SmarthataMessage("/temp", "", SYSTEM);
        messageBroker.broadcastSmarthataMessage(message);
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void calcAverageDailyStreetTemp() {
        double averageDailyStreetTemperature = weatherService.calcAverageDailyStreetTemperature();

        SmarthataMessage message = new SmarthataMessage("/temp/average", Double.toString(averageDailyStreetTemperature), SYSTEM, MQTT);
        messageBroker.broadcastSmarthataMessageRetained(message);
    }

}
