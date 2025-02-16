package org.smarthata.model;

import java.util.Arrays;

public enum Mode {
    OFF(0), MANUAL(1), AUTO(2), UNDEFINED(3);

    public final int mode;

    Mode(int mode) {
        this.mode = mode;
    }

    public static Mode valueOf(int mode) {
        return Arrays.stream(values())
                .filter(m -> m.mode == mode)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Mode " + mode + " is not found"));
    }

    public Mode nextMode() {
        int nextIndex = (this.ordinal() + 1) % Mode.values().length;
        return Mode.values()[nextIndex];
    }
}
