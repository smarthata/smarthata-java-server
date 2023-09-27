package org.smarthata.service

import java.time.Duration
import java.time.LocalDateTime

object DateUtils {
    @JvmStatic
    fun isDateAfter(time: LocalDateTime?, duration: Duration): Boolean {
        return time == null || Duration.between(time, LocalDateTime.now()) > duration
    }
}