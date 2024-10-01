package org.smarthata.service.device;

import static org.smarthata.service.message.EndpointType.MQTT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smarthata.model.Mode;
import org.smarthata.service.message.AbstractSmarthataMessageListener;
import org.smarthata.service.message.EndpointType;
import org.smarthata.service.message.SmarthataMessage;
import org.smarthata.service.message.SmarthataMessageBroker;
import org.springframework.stereotype.Service;

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

    private void saveModeToBroker(String text, EndpointType source) {
        messageBroker.broadcast(
            new SmarthataMessage("/watering/mode/in", text, source, MQTT, true));
    }

    public void wave(EndpointType source) {
        sendActionToBroker("wave", source);
        logger.info("Wave to broker sent");
    }

    public void blowing(EndpointType source) {
        sendActionToBroker("blowing", source);
        logger.info("Blowing to broker sent");
    }

    private void sendActionToBroker(String action, EndpointType source) {
        try {
            String text = objectMapper.writeValueAsString(Map.of("action", action));
            messageBroker.broadcast(
                new SmarthataMessage("/watering/in/json", text, source, MQTT, false));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<Integer, Integer> updateChannel(int channel, int state, EndpointType source) {
        channelStates.put(channel, state);
        sendChangeChannelBroker(channel - 1, state, source);
        return channelStates;
    }

    private void sendChangeChannelBroker(int channel, int state, EndpointType source) {
        try {
            String text = objectMapper.writeValueAsString(
                Map.of("channel", channel, "state", state));
            messageBroker.broadcast(
                new SmarthataMessage("/watering/in/json", text, source, MQTT, false));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void receiveSmarthataMessage(SmarthataMessage message) {
        try {
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
                    channelStates.put(4, map.get("o"));
                    channelStates.put(5, map.get("k"));
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public EndpointType endpointType() {
        return EndpointType.SYSTEM;
    }


    public void updateMode(Mode mode, EndpointType source) {
        this.mode = mode;
        saveModeToBroker(Integer.toString(mode.ordinal()), source);
    }

    public void updateStartTimes(List<Double> startTimes) {
        this.startTimes = startTimes;
    }

    public void updateDurations(List<Integer> durations) {
        this.durations = durations;
    }

}
