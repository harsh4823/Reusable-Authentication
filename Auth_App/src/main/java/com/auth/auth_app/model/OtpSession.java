package com.auth.auth_app.model;

import java.time.Instant;

public record OtpSession(String otp, Instant expiration) {
}
