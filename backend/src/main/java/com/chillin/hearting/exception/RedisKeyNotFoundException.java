package com.chillin.hearting.exception;

public class RedisKeyNotFoundException extends RuntimeException {

    public RedisKeyNotFoundException() {
        super("Redis Key가 존재하지 않습니다.");
    }

    public RedisKeyNotFoundException(String key) {
        super("Redis에 "+key+ " 가 존재하지 않습니다.");
    }
}
