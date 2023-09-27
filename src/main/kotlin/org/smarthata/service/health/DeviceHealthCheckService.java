package org.smarthata.service.health;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.smarthata.service.message.AbstractSmarthataMessageListener;
import org.smarthata.service.message.EndpointType;
import org.smarthata.service.message.SmarthataMessage;
import org.smarthata.service.message.SmarthataMessageBroker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static java.time.LocalDateTime.now;
import static org.smarthata.service.message.EndpointType.SYSTEM;
import static org.smarthata.service.message.EndpointType.TELEGRAM;

@Slf4j
@Service
public class DeviceHealthCheckService extends AbstractSmarthataMessageListener {

    private static final Duration OFFLINE_DURATION = Duration.ofMinutes(30);
    private static final Duration NOTIFICATION_DURATION = Duration.ofHours(6);

    private final Map<String, DeviceHealth> deviceTimeMap;
    private final List<String> devices;

    public DeviceHealthCheckService(SmarthataMessageBroker messageBroker,
                                    @Value("${health.devices}") List<String> devices) {
        super(messageBroker);

        this.devices = devices;
        deviceTimeMap = createMap(devices);
    }

    private static Map<String, DeviceHealth> createMap(List<String> devices) {
        Map<String, DeviceHealth> map = new HashMap<>();
        for (String device : devices) {
            map.put(device, new DeviceHealth(device));
        }
        return map;
    }


    @Override
    public void receiveSmarthataMessage(SmarthataMessage message) {
        DeviceHealth deviceHealth = deviceTimeMap.get(message.path);
        if (deviceHealth != null) {

            if (deviceHealth.status != DeviceStatus.ACTIVE) {
                deviceHealth.lastNotificationTime = null;
                sendMessage("Device is active: " + deviceHealth.devicePath);
            }

            deviceHealth.updateTime = now();
            deviceHealth.status = DeviceStatus.ACTIVE;
        }
    }

    @Override
    public EndpointType getEndpointType() {
        return SYSTEM;
    }

    @Scheduled(cron = "0 * * * * *")
    public void check() {
        List<DeviceHealth> offlineDevices = getOfflineDevices();
        log.debug("offlineDevices = " + offlineDevices);
        sendNotifications(offlineDevices);
    }

    private List<DeviceHealth> getOfflineDevices() {
        List<DeviceHealth> offlineDevices = new ArrayList<>();
        for (String device : devices) {
            DeviceHealth deviceHealth = deviceTimeMap.get(device);
            if (isDateAfter(deviceHealth.updateTime, OFFLINE_DURATION)) {
                deviceHealth.status = DeviceStatus.OFFLINE;
                offlineDevices.add(deviceHealth);
            }
        }
        return offlineDevices;
    }

    private void sendNotifications(List<DeviceHealth> offlineDevices) {
        for (DeviceHealth offlineDevice : offlineDevices) {
            if (isDateAfter(offlineDevice.lastNotificationTime, NOTIFICATION_DURATION)) {
                offlineDevice.lastNotificationTime = now();
                sendMessage("Device is offline: " + offlineDevice.devicePath);
            }
        }
    }

    private boolean isDateAfter(LocalDateTime time, Duration duration) {
        return time == null || Duration.between(time, now()).compareTo(duration) > 0;
    }

    private void sendMessage(String text) {
        SmarthataMessage message = new SmarthataMessage("/messages", text, SYSTEM, TELEGRAM);
        messageBroker.broadcastSmarthataMessage(message);
    }
}
