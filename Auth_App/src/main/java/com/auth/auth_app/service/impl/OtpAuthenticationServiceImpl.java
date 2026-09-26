package com.auth.auth_app.service.impl;

import com.auth.auth_app.service.IOtpAuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpAuthenticationServiceImpl implements IOtpAuthenticationService {

    private final SecureRandom random = new SecureRandom();
    private final StringRedisTemplate stringRedisTemplate;
    private final JavaMailSender javaMailSender;

    private static final String REDIS_KEY_PREFIX = "otp:";
    private static final int OTP_EXPIRATION_MINUTES = 5;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Override
    public String generateAndSendOtp(String email) {
        String otpString = String.valueOf(100000 + random.nextInt(900000));
        String redisKey = REDIS_KEY_PREFIX + email;

        stringRedisTemplate.opsForValue().set(redisKey, otpString, Duration.ofMinutes(OTP_EXPIRATION_MINUTES));

        try {
            sendTextEmail(email, "OTP for Authentication", "Your OTP is: " + otpString);
            return "Otp Sent Successfully!";
        } catch (MailException e) {
            stringRedisTemplate.delete(redisKey);
            log.error("Failed to send OTP to {}. Redis key rolled back.", email, e);
            throw new RuntimeException("Failed to send OTP email. Please try again.");
        }
    }

    @Override
    public boolean validateOtp(String email, String otp) {
        String redisKey = REDIS_KEY_PREFIX + email;
        String storedOtp = stringRedisTemplate.opsForValue().get(redisKey);

        if (otp != null && otp.equals(storedOtp)) {
            stringRedisTemplate.delete(redisKey);
            return true;
        }
        return false;
    }

    private void sendTextEmail(String toRecipient, String subject, String body) throws MailException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailUsername);
        message.setTo(toRecipient);
        message.setSubject(subject);
        message.setText(body);

        javaMailSender.send(message);
        log.info("Email successfully sent to: {}", toRecipient);
    }
}