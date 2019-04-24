package org.smarthata.service.health;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DeviceHealth {

    private String devicePath;
    private DeviceStatus status;

    private LocalDateTime updateTime;
    private LocalDateTime lastNotificationTime;

    public DeviceHealth(String devicePath) {
        this.devicePath = devicePath;
        this.status = DeviceStatus.OFFLINE;
    }
}
