package org.smarthata.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smarthata.service.message.SmarthataMessage;
import org.smarthata.service.message.SmarthataMessageBroker;
import org.smarthata.service.message.SmarthataMessageListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static java.time.LocalDateTime.now;

@Service
public class DeviceHealthCheckService implements SmarthataMessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceHealthCheckService.class);

    private static final List<String> DEVICES = Arrays.asList("/bedroom", "/bathroom", "/heating/floor", "/bedroom/humidifier");
    private static final Duration OFFLINE_DURATION = Duration.ofMinutes(10);
    private static final Duration NOTIFICATION_DURATION = Duration.ofMinutes(60);

    private final SmarthataMessageBroker messageBroker;
    private final Map<String, LocalDateTime> deviceTimeMap = new HashMap<>();
    private final Map<String, LocalDateTime> lastNotificationTime = new HashMap<>();


    public DeviceHealthCheckService(SmarthataMessageBroker messageBroker) {
        this.messageBroker = messageBroker;
        messageBroker.register(this);
    }


    @Override
    public void receiveSmarthataMessage(SmarthataMessage message) {
        deviceTimeMap.put(message.getPath(), now());
    }

    @Scheduled(cron = "0 * * * * *")
    public void check() {
        List<String> offlineDevices = getOfflineDevices();
        LOG.info("offlineDevices = " + offlineDevices);
        sendNotifications(offlineDevices);
    }

    private List<String> getOfflineDevices() {
        List<String> offlineDevices = new ArrayList<>();
        for (String device : DEVICES) {
            LocalDateTime time = deviceTimeMap.get(device);
            if (time == null || Duration.between(time, now()).compareTo(OFFLINE_DURATION) > 0) {
                offlineDevices.add(device);
            }
        }
        return offlineDevices;
    }

    private void sendNotifications(List<String> offlineDevices) {
        for (String offlineDevice : offlineDevices) {
            LocalDateTime lastNotification = lastNotificationTime.get(offlineDevice);
            if (lastNotification == null || Duration.between(lastNotification, now()).compareTo(NOTIFICATION_DURATION) > 0) {
                lastNotificationTime.put(offlineDevice, now());
                String text = "Device is offline: " + offlineDevice;
                SmarthataMessage message = new SmarthataMessage("/messages", text, SmarthataMessage.SOURCE_CRON);
                messageBroker.broadcastSmarthataMessage(message);
            }
        }
    }
}
