package org.smarthata.service.health;


import java.time.LocalDateTime;

public class DeviceHealth {

    public String devicePath;
    public DeviceStatus status;

    public LocalDateTime updateTime;
    public LocalDateTime lastNotificationTime;

    public DeviceHealth(String devicePath) {
        this.devicePath = devicePath;
        this.status = DeviceStatus.ACTIVE;
    }
}
