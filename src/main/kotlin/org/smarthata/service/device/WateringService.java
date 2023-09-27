package org.smarthata.service.device;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smarthata.model.Mode;
import org.smarthata.service.message.AbstractSmarthataMessageListener;
import org.smarthata.service.message.EndpointType;
import org.smarthata.service.message.SmarthataMessage;
import org.smarthata.service.message.SmarthataMessageBroker;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.smarthata.service.message.EndpointType.MQTT;

@Service
public class WateringService extends AbstractSmarthataMessageListener {

    private final ObjectMapper objectMapper;

    public Mode mode = Mode.UNDEFINED;

    public List<Double> startTimes;
    public List<Integer> durations;

    public final Map<Integer, Integer> channelStates = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected WateringService(SmarthataMessageBroker messageBroker, ObjectMapper objectMapper) {
        super(messageBroker);
        this.objectMapper = objectMapper;
    }

    private void sendModeToBroker(String text, EndpointType source) {
        SmarthataMessage message = new SmarthataMessage("/watering/mode/in", text, source, MQTT, true);
        messageBroker.broadcastSmarthataMessage(message);
    }

    public void wave(EndpointType source) {
        sendActionToBroker("wave", source);
        logger.info("Wave to broker sent");
    }

    @SneakyThrows
    private void sendActionToBroker(String action, EndpointType source) {
        String text = objectMapper.writeValueAsString(Map.of("action", action));
        SmarthataMessage message = new SmarthataMessage("/watering/in/json", text, source, MQTT, false);
        messageBroker.broadcastSmarthataMessage(message);
    }

    public Map<Integer, Integer> updateChannel(int channel, int state, EndpointType source) {
        channelStates.put(channel, state);
        sendChangeChannelBroker(channel - 1, state, source);
        return channelStates;
    }

    @SneakyThrows
    private void sendChangeChannelBroker(int channel, int state, EndpointType source) {
        String text = objectMapper.writeValueAsString(Map.of("channel", channel, "state", state));

        SmarthataMessage message = new SmarthataMessage("/watering/in/json", text, source, MQTT, false);
        messageBroker.broadcastSmarthataMessage(message);
    }

    @SneakyThrows
    @Override
    @SuppressWarnings("unchecked")
    public void receiveSmarthataMessage(SmarthataMessage message) {
        switch (message.path) {
            case "/watering/mode/in" -> {
                mode = Mode.valueOf(Integer.parseInt(message.text));
                logger.info("Watering mode changed to {}", mode);
            }
            case "/watering/start/in" -> {
                startTimes = objectMapper.readValue(message.text, List.class);
                logger.info("Watering startTimes {}", startTimes);
            }
            case "/watering/duration/in" -> {
                durations = objectMapper.readValue(message.text, List.class);
                logger.info("Watering durations {}", durations);
            }
            case "/watering" -> {
                Map<String, Integer> map = objectMapper.readValue(message.text, Map.class);
                channelStates.put(1, map.get("g1"));
                channelStates.put(2, map.get("g2"));
                channelStates.put(3, map.get("g3"));
                channelStates.put(4, map.get("k"));
                channelStates.put(5, map.get("o"));
            }
        }
    }

    @Override
    public EndpointType endpointType() {
        return EndpointType.SYSTEM;
    }


    public void updateMode(Mode mode, EndpointType source) {
        this.mode = mode;
        sendModeToBroker(Integer.toString(mode.ordinal()), source);
    }

    public void updateStartTimes(List<Double> startTimes) {
        this.startTimes = startTimes;
    }

    public void updateDurations(List<Integer> durations) {
        this.durations = durations;
    }

}
