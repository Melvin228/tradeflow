package com.melvin.tradeflow.user.domain;

public class UserNotActiveException extends IllegalStateException {
    public UserNotActiveException() {
        super("Only active users can log in");
    }
}
