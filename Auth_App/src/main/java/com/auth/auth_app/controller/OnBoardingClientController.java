package com.auth.auth_app.controller;

import com.auth.auth_app.model.OnboardingRequest;
import com.auth.auth_app.model.OnboardingResponse;
import com.auth.auth_app.service.IOnBoardingClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/onboard")
@RequiredArgsConstructor
public class OnBoardingClientController {

    private final IOnBoardingClientService onBoardingClientService;

    @PostMapping
    public ResponseEntity<OnboardingResponse> onboard(
            @Valid @RequestBody OnboardingRequest request
    ){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(onBoardingClientService.onboard(request));
    }
}
