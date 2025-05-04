package com.washer.Things.global.util;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateUtil {

    public static Date toDate(LocalDateTime localDateTime) {
        return toDate(localDateTime, ZoneId.systemDefault());
    }

    public static Date toDate(LocalDateTime localDateTime, ZoneId zoneId) {
        return Date.from(localDateTime.atZone(zoneId).toInstant());
    }

    public static LocalDateTime getAtStartOfToday() {
        return LocalDate.now().atStartOfDay();
    }

    public static LocalDateTime getAtEndOfToday() {
        return LocalDate.now().atStartOfDay().plusDays(1);
    }
}