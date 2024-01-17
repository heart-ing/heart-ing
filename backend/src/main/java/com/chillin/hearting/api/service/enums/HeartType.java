package com.chillin.hearting.api.service.enums;

public enum HeartType {
    DEFAULT,
    SPECIAL,
    EVENT;

    public static boolean isDefault(String type) {
        return DEFAULT.name().equals(type);
    }

    public static boolean isSpecial(String type) {
        return SPECIAL.name().equals(type);
    }

    public static boolean isEvent(String type) {
        return EVENT.name().equals(type);
    }
}
