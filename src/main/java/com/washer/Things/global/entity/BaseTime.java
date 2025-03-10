package com.washer.Things.global.entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseTime {
    private LocalDateTime createAt;
    private String formattedDate;
    private String dayOfWeek;
    private String yearMonthDay;

    @PrePersist
    protected void onCreate() {
        createAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM.dd");
        DateTimeFormatter yearMonthDayFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        yearMonthDay = createAt.format(yearMonthDayFormatter);
        formattedDate = createAt.format(dateFormatter);
        dayOfWeek = createAt.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN);
    }

}