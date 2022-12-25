package org.smarthata.model;

import java.util.Arrays;

public enum Mode {
    OFF(0), MANUAL(1), AUTO(2);

    private final int mode;

    Mode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public static Mode valueOf(int mode) {
        return Arrays.stream(values())
                .filter(m -> m.getMode() == mode)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Mode " + mode + " is not found"));
    }
}
