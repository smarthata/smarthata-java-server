package org.smarthata.service.device.heating;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import lombok.Data;

@Data
class HeatingDevice {
    private final String queueActualTemp;
    private final String queueExpectedTemp;
    private final String queueEnabled;
    private final AtomicReference<Double> actualTemp = new AtomicReference<>(0.0);
    private final AtomicReference<Double> expectedTemp;
    private final AtomicInteger enabled = new AtomicInteger(1);

    HeatingDevice(String baseQueue, AtomicReference<Double> expectedTemp) {
        this.queueActualTemp = baseQueue;
        this.queueExpectedTemp = baseQueue + "/in";
        this.queueEnabled = baseQueue + "/enabled";
        this.expectedTemp = expectedTemp;
    }
}
