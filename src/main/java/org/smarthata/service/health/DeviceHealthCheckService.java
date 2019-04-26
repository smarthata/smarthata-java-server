package org.smarthata.service.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smarthata.service.message.EndpointType;
import org.smarthata.service.message.SmarthataMessage;
import org.smarthata.service.message.SmarthataMessageBroker;
import org.smarthata.service.message.SmarthataMessageListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static java.time.LocalDateTime.now;
import static org.smarthata.service.message.EndpointType.SYSTEM;
import static org.smarthata.service.message.EndpointType.TM;

@Service
public class DeviceHealthCheckService implements SmarthataMessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceHealthCheckService.class);

    private static final List<String> DEVICES = Arrays.asList("/bedroom", "/bathroom", "/heating/floor", "/bedroom/humidifier");
    private static final Duration OFFLINE_DURATION = Duration.ofMinutes(30);
    private static final Duration NOTIFICATION_DURATION = Duration.ofHours(3);

    private final SmarthataMessageBroker messageBroker;
    private final Map<String, DeviceHealth> deviceTimeMap = createMap();

    private Map<String, DeviceHealth> createMap() {
        Map<String, DeviceHealth> map = new HashMap<>();
        for (String device : DEVICES) {
            map.put(device, new DeviceHealth(device));
        }
        return map;
    }

    public DeviceHealthCheckService(SmarthataMessageBroker messageBroker) {
        this.messageBroker = messageBroker;
        messageBroker.register(this);
    }


    @Override
    public void receiveSmarthataMessage(SmarthataMessage message) {
        DeviceHealth deviceHealth = deviceTimeMap.get(message.getPath());
        if (deviceHealth != null) {

            if (deviceHealth.getStatus() != DeviceStatus.ACTIVE) {
                deviceHealth.setLastNotificationTime(null);
                sendMessage("Device is active: " + deviceHealth.getDevicePath());
            }

            deviceHealth.setUpdateTime(now());
            deviceHealth.setStatus(DeviceStatus.ACTIVE);
        }
    }

    @Override
    public EndpointType getEndpointType() {
        return SYSTEM;
    }

    @Scheduled(cron = "0 * * * * *")
    public void check() {
        List<DeviceHealth> offlineDevices = getOfflineDevices();
        LOG.debug("offlineDevices = " + offlineDevices);
        sendNotifications(offlineDevices);
    }

    private List<DeviceHealth> getOfflineDevices() {
        List<DeviceHealth> offlineDevices = new ArrayList<>();
        for (String device : DEVICES) {
            DeviceHealth deviceHealth = deviceTimeMap.get(device);
            if (isDateAfter(deviceHealth.getUpdateTime(), OFFLINE_DURATION)) {
                deviceHealth.setStatus(DeviceStatus.OFFLINE);
                offlineDevices.add(deviceHealth);
            }
        }
        return offlineDevices;
    }

    private void sendNotifications(List<DeviceHealth> offlineDevices) {
        for (DeviceHealth offlineDevice : offlineDevices) {
            if (isDateAfter(offlineDevice.getLastNotificationTime(), NOTIFICATION_DURATION)) {
                offlineDevice.setLastNotificationTime(now());
                sendMessage("Device is offline: " + offlineDevice.getDevicePath());
            }
        }
    }

    private boolean isDateAfter(LocalDateTime time, Duration duration) {
        return time == null || Duration.between(time, now()).compareTo(duration) > 0;
    }

    private void sendMessage(String text) {
        SmarthataMessage message = new SmarthataMessage("/messages", text, SYSTEM, TM);
        messageBroker.broadcastSmarthataMessage(message);
    }
}
