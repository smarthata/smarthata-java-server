package org.smarthata.service;

import org.smarthata.service.message.SmarthataMessage;
import org.smarthata.service.message.SmarthataMessageBroker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static org.smarthata.service.message.EndpointType.*;

@Service
public class CronService {

    private final SmarthataMessageBroker messageBroker;
    private final WeatherService weatherService;

    @Autowired
    public CronService(SmarthataMessageBroker messageBroker, WeatherService weatherService) {
        this.messageBroker = messageBroker;
        this.weatherService = weatherService;
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void calcAverageDailyStreetTemp() {
        double averageDailyStreetTemperature = weatherService.calcAverageDailyStreetTemperature();

        SmarthataMessage message = new SmarthataMessage("/street/temp-average", Double.toString(averageDailyStreetTemperature), SYSTEM, MQTT);
        messageBroker.broadcastSmarthataMessageRetained(message);
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void sendDataToNarodmon() {
        weatherService.sendDataToNarodmon();
    }

}
