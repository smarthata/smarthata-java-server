package org.smarthata.service.health;

import lombok.ToString;

import java.time.LocalDateTime;

@ToString
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
