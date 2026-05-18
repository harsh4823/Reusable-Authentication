package com.auth.auth_app.model;

public record LoginResponse(String status , String accessToken,String refreshToken) {
}
