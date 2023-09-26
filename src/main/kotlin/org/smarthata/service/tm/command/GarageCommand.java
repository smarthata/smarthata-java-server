package org.smarthata.service.tm.command;

import lombok.extern.slf4j.Slf4j;
import org.smarthata.service.mqtt.MqttService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j

@Service
public class GarageCommand extends AbstractCommand {

    private static final String GARAGE = "garage";

    @Value("${bot.adminChatId}")
    public String adminChatId;

    @Autowired
    private MqttService mqttService;

    public AtomicBoolean gatesOpen = new AtomicBoolean(false);

    public GarageCommand() {
        super(GARAGE);
    }

    @Override
    public BotApiMethod<?> answer(CommandRequest request) {

        log.info("Garage request: {}", request);


        String text = "Ворота " + (gatesOpen.get() ? "открыты" : "закрыты");

        if (request.hasNext()) {
            String item = request.next();
            switch (item) {
                case "open" -> {
                    gatesOpen.set(true);
                    text = "Принято! Ворота открыты";
                }
                case "close" -> {
                    gatesOpen.set(false);
                    text = "Принято! Ворота закрыты";
                }
                default -> text = item;
            }
        }

        List<String> temps = new LinkedList<>();
        getStreetTemp().ifPresent(t -> temps.add(String.format("улица %.1f°C", t)));
        getGarageTemp().ifPresent(t -> temps.add(String.format("гараж %.1f°C", (double) t)));
        if (temps.size() > 0) text += " (" + String.join(", ", temps) + ")";


        Map<String, String> map = new LinkedHashMap<>();
        String action = gatesOpen.get() ? "close" : "open";
        map.put(action, action);
        map.put("back", "Назад");

        InlineKeyboardMarkup buttons = createButtons(Collections.emptyList(), map, 2);
        return createTmMessage(request.getChatId(), request.getMessageId(), text, buttons);
    }


    private Optional<Double> getStreetTemp() {
        return mqttService.getLastMessageAsDouble("/street/temp");
    }

    private Optional<Object> getGarageTemp() {
        return mqttService.getLastMessageFieldFromJson("/heating/garage/garage", "temp");
    }

}