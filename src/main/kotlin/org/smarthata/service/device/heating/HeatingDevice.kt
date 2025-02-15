package org.smarthata.service.device.heating;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


class HeatingDevice {
    public final String queueActualTemp;
    public final String queueExpectedTemp;
    public final String queueEnabled;
    public final AtomicReference<Double> actualTemp = new AtomicReference<>(-127.0);
    public final AtomicReference<Double> expectedTemp;
    public final AtomicBoolean enabled = new AtomicBoolean(true);

    HeatingDevice(String baseQueue, AtomicReference<Double> expectedTemp) {
        this.queueActualTemp = baseQueue;
        this.queueExpectedTemp = baseQueue + "/in";
        this.queueEnabled = baseQueue + "/enabled";
        this.expectedTemp = expectedTemp;
    }
}
