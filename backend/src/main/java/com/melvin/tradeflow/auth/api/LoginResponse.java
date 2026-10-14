package com.melvin.tradeflow.auth.api;

import java.util.Date;

public record LoginResponse(String token, Date createdAt, Date expiresAt) {
}
