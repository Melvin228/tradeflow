package com.melvin.tradeflow.user.domain;

public final class UserConstraints {
    public static final int MAX_EMAIL_LENGTH = 320;
    public static final int MAX_DISPLAY_NAME_LENGTH = 120;
    public static final int MIN_PASSWORD_LENGTH = 12;
    public static final int MAX_PASSWORD_LENGTH = 128;
    public static final int MAX_PASSWORD_HASH_LENGTH = 255;

    private UserConstraints() {
    }
}
