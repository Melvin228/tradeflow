package com.melvin.tradeflow.user.domain;

public class InvalidUserDataException extends IllegalArgumentException {
    public InvalidUserDataException(String message) {
        super(message);
    }
}
