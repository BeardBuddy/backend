package com.beardbuddy.domain;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

final class TimeSupport {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    private TimeSupport() {
    }

    static String format(LocalTime time) {
        return time.format(HH_MM);
    }
}
