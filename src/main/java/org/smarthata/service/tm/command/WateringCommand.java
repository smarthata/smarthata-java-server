package org.smarthata.service.tm.command;

import lombok.extern.slf4j.Slf4j;
import org.smarthata.model.Mode;
import org.smarthata.service.device.WateringService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Slf4j
@Service
public class WateringCommand extends AbstractCommand {

    private static final String WATERING = "watering";
    private final WateringService wateringService;


    public WateringCommand(WateringService wateringService) {
        super(WATERING);
        this.wateringService = wateringService;
    }

    @Override
    public BotApiMethod<?> answer(CommandRequest request) {
        if (request.hasNext()) {
            String part = request.next();
            log.debug("part {}", part);
            switch (part) {
                case "mode":
                    if (request.hasNext()) {
                        String newModeIndex = request.next();
                        Mode newMode = Mode.valueOf(Integer.parseInt(newModeIndex));
                        wateringService.setMode(newMode);
                        log.info("Mode has been changed to {}", newMode);
                    }
                    break;
                case "duration":
                    return showDurations(request);
                case "start":
                    return showStartTimes(request);
                case "wave":
                    return showWave(request);
                case "channel":
                    return showChannel(request);
                default:
                    return showMainButtons(request, "This is not implemented:" + request.getPath());
            }

        }

        return showMainButtons(request, "Watering:");
    }

    private BotApiMethod<?> showMainButtons(CommandRequest request, String text) {

        Mode currentMode = wateringService.getMode();
        if (currentMode == null) {
            currentMode = Mode.OFF;
        }

        int nextIndex = (currentMode.ordinal() + 1) % Mode.values().length;
        Mode next = Mode.values()[nextIndex];

        Map<String, String> map = new LinkedHashMap<>();
        map.put("mode/" + next.getMode(), "mode: " + currentMode.name());
        map.put("channel", "channels: " + wateringService.getChannelStates().values());
        map.put("wave/start", "Start wave");
        map.put("start", "start times (h): " + wateringService.getStartTimes());
        map.put("duration", "duration times (min): " + wateringService.getDurations());

        InlineKeyboardMarkup keyboard = createButtons(emptyList(), map);
        return createTmMessage(request.getChatId(), request.getMessageId(), text, keyboard);
    }

    private BotApiMethod<?> showDurations(CommandRequest request) {
        String text = "Watering durations (minute): " + wateringService.getDurations();

        List<String> buttons = List.of("change", "back");
        InlineKeyboardMarkup keyboard = createButtons(singletonList("duration"), buttons);

        return createTmMessage(request.getChatId(), request.getMessageId(), text, keyboard);
    }

    private BotApiMethod<?> showStartTimes(CommandRequest request) {
        String text = "Watering start times (hours): " + wateringService.getStartTimes();

        List<String> buttons = List.of("add", "remove", "back");
        InlineKeyboardMarkup keyboard = createButtons(singletonList("start"), buttons);

        return createTmMessage(request.getChatId(), request.getMessageId(), text, keyboard);
    }


    private BotApiMethod<?> showWave(CommandRequest request) {
        String text = "Wave started";

        wateringService.wave();

        InlineKeyboardMarkup keyboard = createButtons(singletonList("watering"), singletonList("back"));

        return createTmMessage(request.getChatId(), request.getMessageId(), text, keyboard);
    }

    private BotApiMethod<?> showChannel(CommandRequest request) {

        if (request.hasNext()) {
            String part = request.next();
            if (part.equals("disable")) {
                wateringService.getChannelStates().keySet()
                        .forEach(ch -> wateringService.updateChannel(ch, 0));
            } else {
                int channel = Integer.parseInt(part);
                log.info("channel {}", channel);
                if (request.hasNext()) {
                    int newState = Integer.parseInt(request.next());
                    log.info("newState {}", newState);
                    wateringService.updateChannel(channel, newState);
                }
            }
        }

        String text = "Channels: ";

        Map<String, String> out = new LinkedHashMap<>();
        wateringService.getChannelStates().forEach((key, value) -> out.put(
                key + "/" + (1 - value),
                key + ":" + (value == 1 ? "On" : "Off")
        ));
        out.put("disable", "disable");
        out.put("back", "back");

        InlineKeyboardMarkup keyboard = createButtons(singletonList("channel"), out);

        return createTmMessage(request.getChatId(), request.getMessageId(), text, keyboard);
    }

}
