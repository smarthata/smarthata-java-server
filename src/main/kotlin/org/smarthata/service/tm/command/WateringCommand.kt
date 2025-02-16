package org.smarthata.service.tm.command;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.smarthata.service.message.EndpointType.TELEGRAM;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smarthata.model.Mode;
import org.smarthata.service.device.WateringService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Service
public class WateringCommand extends AbstractCommand {

    private static final String WATERING = "watering";
    private final WateringService wateringService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    public WateringCommand(WateringService wateringService) {
        super(WATERING);
        this.wateringService = wateringService;
    }

    @Override
    public BotApiMethod<?> answer(CommandRequest request) {
        if (request.hasNext()) {
            String part = request.next();
            logger.debug("main part {}", part);
            return switch (part) {
                case "mode" -> changeMode(request);
                case "settings" -> showSettings(request);
                case "wave" -> startWave(request);
                case "channel" -> showChannel(request);
                default -> showMainButtons(request, "This is not implemented:" + request.getPath());
            };
        }

        return showMainButtons(request, "Автополив:");
    }

    private BotApiMethod<?> changeMode(CommandRequest request) {
        if (request.hasNext()) {
            String newModeIndex = request.next();
            Mode newMode = Mode.valueOf(Integer.parseInt(newModeIndex));
            wateringService.updateMode(newMode, TELEGRAM);
            logger.info("Mode has been changed to {}", newMode);
            return showMainButtons(request, "Режим изменен");
        }
        return showMainButtons(request, "Режим автополива:");
    }

    private BotApiMethod<?> showMainButtons(CommandRequest request, String text) {
        Mode currentMode = Optional.of(wateringService.mode).orElse(Mode.OFF);

        int nextIndex = (currentMode.ordinal() + 1) % Mode.values().length;
        Mode next = Mode.values()[nextIndex];

        Map<String, String> map = new LinkedHashMap<>();
        map.put("mode/" + next.mode, "Режим: " + currentMode.name());
        if (currentMode != Mode.OFF) {
            map.put("channel", "Каналы: " + wateringService.channelStates.values());
            map.put("wave", "Запустить полив");
        }
        map.put("settings", "Настройки");
        map.put("back", "\uD83D\uDD19 Назад");

        InlineKeyboardMarkup keyboard = createButtons(emptyList(), map);
        return createTmMessage(request.getChatId(), request.getMessageId(), text, keyboard);
    }

    private BotApiMethod<?> showSettings(CommandRequest request) {
        if (request.hasNext()) {
            String part = request.next();
            logger.debug("setting part {}", part);
            return switch (part) {
                case "duration" -> showDurations(request);
                case "start" -> showStartTimes(request);
                case "blowing" -> startBlowing(request);
                default -> showMainButtons(request, "This is not implemented:" + request.getPath());
            };
        }
        String text = "Настройки полива:";

        Map<String, String> map = new LinkedHashMap<>();
        map.put("duration", "Продолжительность каналов (мин): " + wateringService.durations);
        map.put("start", "Время начала (ч): " + wateringService.startTimes);
        map.put("blowing", "Продувка");
        map.put("back", "\uD83D\uDD19 Назад");

        InlineKeyboardMarkup keyboard = createButtons(singletonList("settings"), map);

        return createTmMessage(request.getChatId(), request.getMessageId(), text, keyboard);
    }

    private BotApiMethod<?> showDurations(CommandRequest request) {
        String text = "Продолжительность (минуты): " + wateringService.durations;

        List<String> buttons = List.of("change", "back");
        InlineKeyboardMarkup keyboard = createButtons(singletonList("duration"), buttons);

        return createTmMessage(request.getChatId(), request.getMessageId(), text, keyboard);
    }

    private BotApiMethod<?> showStartTimes(CommandRequest request) {
        String text = "Время начала полива (часы): " + wateringService.startTimes;

        List<String> buttons = List.of("add", "remove", "back");
        InlineKeyboardMarkup keyboard = createButtons(singletonList("start"), buttons);

        return createTmMessage(request.getChatId(), request.getMessageId(), text, keyboard);
    }

    private BotApiMethod<?> startWave(CommandRequest request) {
        wateringService.wave(TELEGRAM);
        return showMainButtons(request, "Полив запущен");
    }

    private BotApiMethod<?> startBlowing(CommandRequest request) {
        wateringService.blowing(TELEGRAM);
        return showMainButtons(request, "Продувка запущена");
    }

    private BotApiMethod<?> showChannel(CommandRequest request) {
        if (request.hasNext()) {
            String part = request.next();
            if (part.equals("disable")) {
                wateringService.channelStates.keySet()
                    .forEach(ch -> wateringService.updateChannel(ch, 0, TELEGRAM));
            } else {
                int channel = Integer.parseInt(part);
                logger.info("channel {}", channel);
                if (request.hasNext()) {
                    int newState = Integer.parseInt(request.next());
                    logger.info("newState {}", newState);
                    wateringService.updateChannel(channel, newState, TELEGRAM);
                }
            }
        }

        String text = "Каналы: ";

        Map<String, String> out = new LinkedHashMap<>();
        wateringService.channelStates.forEach((key, value) -> out.put(
            key + "/" + (1 - value),
            key + (value == 1 ? " \uD83D\uDCA6" : "")
        ));
        out.put("disable", "Выключить");
        out.put("back", "\uD83D\uDD19 Назад");

        InlineKeyboardMarkup keyboard = createButtons(singletonList("channel"), out);
        return createTmMessage(request.getChatId(), request.getMessageId(), text, keyboard);
    }

}
