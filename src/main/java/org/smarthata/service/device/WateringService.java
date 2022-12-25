package org.smarthata.service.device;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
import static org.smarthata.service.message.EndpointType.USER;

@Slf4j
@Service
public class WateringService extends AbstractSmarthataMessageListener {

    private final ObjectMapper objectMapper;

    private Mode mode = null;

    private List<Double> startTimes;
    private List<Integer> durations;

    private Map<Integer, Integer> channelStates = new HashMap<>(5);

    protected WateringService(SmarthataMessageBroker messageBroker, ObjectMapper objectMapper) {
        super(messageBroker);
        this.objectMapper = objectMapper;
    }

    private void sendModeToBroker(String text) {
        SmarthataMessage message = new SmarthataMessage("/watering/mode/in", text, USER, MQTT, true);
        messageBroker.broadcastSmarthataMessage(message);
    }

    public void wave() {
        sendActionToBroker("wave");
        log.info("Wave to broker sent");
    }

    @SneakyThrows
    private void sendActionToBroker(String action) {
        String text = objectMapper.writeValueAsString(Map.of("action", action));
        SmarthataMessage message = new SmarthataMessage("/watering/in/json", text, USER, MQTT, false);
        messageBroker.broadcastSmarthataMessage(message);
    }

    public Map<Integer, Integer> updateChannel(int channel, int state) {
        channelStates.put(channel, state);
        sendChangeChannelBroker(channel - 1, state);
        return channelStates;
    }

    @SneakyThrows
    private void sendChangeChannelBroker(int channel, int state) {
        String text = objectMapper.writeValueAsString(Map.of("channel", channel, "state", state));

        SmarthataMessage message = new SmarthataMessage("/watering/in/json", text, USER, MQTT, false);
        messageBroker.broadcastSmarthataMessage(message);
    }

    @SneakyThrows
    @Override
    @SuppressWarnings("unchecked")
    public void receiveSmarthataMessage(SmarthataMessage message) {
        switch (message.getPath()) {
            case "/watering/mode/in":
                mode = Mode.valueOf(Integer.parseInt(message.getText()));
                log.info("Watering mode changed to {}", mode);
                break;
            case "/watering/start/in":
                startTimes = objectMapper.readValue(message.getText(), List.class);
                log.info("Watering startTimes {}", startTimes);
                break;
            case "/watering/duration/in":
                durations = objectMapper.readValue(message.getText(), List.class);
                log.info("Watering durations {}", durations);
                break;
            case "/watering":
                Map<String, Integer> map = objectMapper.readValue(message.getText(), Map.class);
//                /watering {"g1":0,"g2":0,"g3":0,"k":0,"o":0,"hours":1}

                channelStates.put(1, map.get("g1"));
                channelStates.put(2, map.get("g2"));
                channelStates.put(3, map.get("g3"));
                channelStates.put(4, map.get("k"));
                channelStates.put(5, map.get("o"));

                log.info("Watering durations {}", durations);
                break;
        }
    }

    @Override
    public EndpointType getEndpointType() {
        return EndpointType.SYSTEM;
    }


    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        sendModeToBroker(Integer.toString(mode.ordinal()));
    }

    public List<Double> getStartTimes() {
        return startTimes;
    }

    public void setStartTimes(List<Double> startTimes) {
        this.startTimes = startTimes;
    }

    public List<Integer> getDurations() {
        return durations;
    }

    public void setDurations(List<Integer> durations) {
        this.durations = durations;
    }

    public Map<Integer, Integer> getChannelStates() {
        return channelStates;
    }
}
