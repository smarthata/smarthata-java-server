package org.smarthata.service.device.heating;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


class HeatingDevice {
    public final String queueActualTemp;
    public final String queueExpectedTemp;
    public final String queueEnabled;
    public final AtomicReference<Double> actualTemp = new AtomicReference<>(0.0);
    public final AtomicReference<Double> expectedTemp;
    public final AtomicInteger enabled = new AtomicInteger(1);

    HeatingDevice(String baseQueue, AtomicReference<Double> expectedTemp) {
        this.queueActualTemp = baseQueue;
        this.queueExpectedTemp = baseQueue + "/in";
        this.queueEnabled = baseQueue + "/enabled";
        this.expectedTemp = expectedTemp;
    }
}
