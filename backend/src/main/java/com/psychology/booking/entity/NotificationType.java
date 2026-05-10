package com.psychology.booking.entity;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NotificationType {
    REMINDER_1H("REMINDER_1H"),
    REMINDER_15M("REMINDER_15M"),
    AUTO_CANCEL("AUTO_CANCEL"),
    BOOKING_CONFIRMED("BOOKING_CONFIRMED"),
    BOOKING_REJECTED("BOOKING_REJECTED");

    @EnumValue
    @JsonValue
    private final String value;

    NotificationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
