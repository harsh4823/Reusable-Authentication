package com.auth.auth_app.service;

public interface IOtpAuthenticationService {
    String generateAndSendOtp(String email);
    boolean validateOtp(String email,String otp);
}
