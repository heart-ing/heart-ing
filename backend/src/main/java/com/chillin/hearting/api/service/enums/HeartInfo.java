package com.chillin.hearting.api.service.enums;

public enum HeartInfo {
    YELLOW(1L),
    BLUE(2L),
    GREEN(3L),
    PINK(4L),
    RED(5L),
    PLANET(6L),
    RAINBOW(7L),
    MINCHO(8L),
    SUNNY(9L),
    READING_GLASSES(10L),
    ICE_CREAM(11L),
    SHAMROCK(12L),
    FOUR_LEAF(13L),
    NOIR(14L),
    CARNATION(15L);

    final long id;

    HeartInfo(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public static boolean isLockedToNoLogin(long heartId) {
        return heartId == PINK.id || heartId == RED.id;
    }
}
