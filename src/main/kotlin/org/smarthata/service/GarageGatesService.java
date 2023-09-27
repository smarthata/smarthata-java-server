package org.smarthata.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smarthata.service.mqtt.MqttService;
import org.smarthata.service.tm.TmBot;
import org.smarthata.service.tm.command.CommandRequest;
import org.smarthata.service.tm.command.GarageCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;


enum GarageGateAction {
    OPEN("открыть"), CLOSE("закрыть"), NOTHING("");

    public final String text;

    GarageGateAction(String text) {
        this.text = text;
    }
}

@Service
public class GarageGatesService {

    private final MqttService mqttService;
    private final GarageCommand garageCommand;
    private final TmBot tmBot;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private LocalDateTime lastNotificationTime;

    public GarageGatesService(
            MqttService mqttService,
            GarageCommand garageCommand,
            @Autowired(required = false) TmBot tmBot
    ) {
        this.mqttService = mqttService;
        this.garageCommand = garageCommand;
        this.tmBot = tmBot;
    }


    @Scheduled(fixedDelay = 60, timeUnit = SECONDS)
    public void checkGarage() {
        try {
            logger.debug("Check garage heating");

            double streetTemp = findStreetTemp();
            double garageTemp = findGarageTemp();

            if (findStreetAverageTemp() <= 18) {
                logger.debug("Check for heating garage");
                GarageGateAction action = findGarageGateWarmingAction(streetTemp, garageTemp);
                if (action != GarageGateAction.NOTHING) {
                    logger.debug("Temp is good to {} gates", action.name());
                    if (DateUtils.isDateAfter(lastNotificationTime, Duration.ofMinutes(30))) {
                        sendMessage(action);
                    } else {
                        logger.debug("Notification was sent recently");
                    }
                }
            }
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private GarageGateAction findGarageGateWarmingAction(double streetTemp, double garageTemp) {
        if (streetTemp > garageTemp + 0.5 && !garageCommand.gatesOpen.get())
            return GarageGateAction.OPEN;
        if (streetTemp + 0.5 < garageTemp && garageCommand.gatesOpen.get())
            return GarageGateAction.CLOSE;
        return GarageGateAction.NOTHING;
    }

    private void sendMessage(GarageGateAction action) {
        String text = String.format("Можно %s гаражные ворота для прогрева", action.text);

        CommandRequest commandRequest = new CommandRequest(List.of(text), garageCommand.adminChatId, null);

        if (tmBot.sendMessageToTelegram(garageCommand.answer(commandRequest))) {
            lastNotificationTime = LocalDateTime.now();
        }
    }

    private double findStreetTemp() {
        double streetTemp = mqttService.findLastMessageAsDouble("/street/temp")
                .orElseThrow(() -> new RuntimeException("Street temp is not populated"));
        logger.debug("Street temp: {}", streetTemp);
        return streetTemp;
    }

    private double findStreetAverageTemp() {
        double streetAverageTemp = mqttService.findLastMessageAsDouble("/street/temp-average")
                .orElseThrow(() -> new RuntimeException("Average temp is not populated"));
        logger.debug("Street average temp: {}", streetAverageTemp);
        return streetAverageTemp;
    }

    private double findGarageTemp() {
        Double garageTemp = (Double) mqttService.findLastMessageFieldFromJson("/heating/garage/garage", "temp")
                .orElseThrow(() -> new RuntimeException("Garage data is not populated"));
        logger.debug("Garage temp: {}", garageTemp);
        return garageTemp;
    }

}