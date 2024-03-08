package com.chillin.hearting.exception;

public class NoHeartStrategyException extends RuntimeException {
    public NoHeartStrategyException() {
        super("해당 하트 전략을 찾을 수 없습니다.");
    }
}
