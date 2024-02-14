package com.chillin.hearting.api.service.enums;

public enum HeartType {
    DEFAULT,
    SPECIAL,
    EVENT;

    public static boolean isDefault(String type) {
        return DEFAULT.name().equals(type);
    }
}
