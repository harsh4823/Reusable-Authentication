package com.auth.auth_app.controller;

import com.auth.auth_app.model.AvailabilityResponse;
import com.auth.auth_app.model.OnboardingRequest;
import com.auth.auth_app.model.OnboardingResponse;
import com.auth.auth_app.repository.AuthUserRepository;
import com.auth.auth_app.service.IOnBoardingClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/onboard")
@RequiredArgsConstructor
public class OnBoardingClientController {

    private final IOnBoardingClientService onBoardingClientService;
    private final AuthUserRepository authUserRepository;

    @PostMapping
    public ResponseEntity<OnboardingResponse> onboard(
            @Valid @RequestBody OnboardingRequest request
    ){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(onBoardingClientService.onboard(request));
    }

    @GetMapping("/availability/email")
    public ResponseEntity<AvailabilityResponse> checkEmailAvailability(
            @RequestParam String email
    ) {
        boolean exists = authUserRepository.existsByEmailIgnoreCase(email);

        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new AvailabilityResponse(false, "Email already exists"));
        }

        return ResponseEntity.ok(new AvailabilityResponse(true, "Email is available"));
    }
}
