package com.melvin.tradeflow.user.application;

public class UserEmailAlreadyRegisteredException extends RuntimeException {
    public UserEmailAlreadyRegisteredException() {
        super("An account with this email already exists");
    }
}
