package org.smarthata.service;

import java.time.Duration;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

public class DateUtils {
    public static boolean isDateAfter(LocalDateTime time, Duration duration) {
        return time == null || Duration.between(time, now()).compareTo(duration) > 0;
    }
}
