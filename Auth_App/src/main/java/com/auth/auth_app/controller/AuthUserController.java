package com.auth.auth_app.controller;

import com.auth.auth_app.constant.ApplicationConstant;
import com.auth.auth_app.entity.AuthUser;
import com.auth.auth_app.entity.RefreshToken;
import com.auth.auth_app.model.AuthUserDto;
import com.auth.auth_app.model.LoginRequest;
import com.auth.auth_app.model.LoginResponse;
import com.auth.auth_app.repository.AuthUserRepository;
import com.auth.auth_app.service.IAuthService;
import com.auth.auth_app.service.ICloudinaryService;
import com.auth.auth_app.service.IRefreshTokenService;
import com.auth.auth_app.service.impl.RefreshTokenServiceImp;
import com.auth.auth_app.util.AuthUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthUserController {

    private final IAuthService authService;
    private final IRefreshTokenService refreshTokenService;
    private final AuthUtil authUtil;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> apiLogin(@RequestBody LoginRequest loginRequest){
        try{
        String jwt = authService.authenticateAndGenerateToken(loginRequest);
        return ResponseEntity.status(HttpStatus.OK).header(ApplicationConstant.JWT_HEADER,jwt)
                .body(new LoginResponse(HttpStatus.OK.getReasonPhrase(),jwt));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping(value = "/register" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(@ModelAttribute AuthUserDto authUserDto , @RequestParam MultipartFile profilePicture){
        try{
            authService.registerUser(authUserDto,profilePicture);
            return ResponseEntity.status(HttpStatus.CREATED).
                        body("Given user details are successfully registered");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
                    body("An exception occurred: " + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // 1. Extract the refresh token from the incoming cookies
        String refreshTokenString = null;
        if (request.getCookies() != null) {
            refreshTokenString = Arrays.stream(request.getCookies())
                    .filter(cookie -> "refresh_jwt".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (refreshTokenString == null) {
            return ResponseEntity.status(403).body("Refresh Token is missing!");
        }

        // 2. Validate the token and issue a new Access Token
        return refreshTokenService.findByToken(refreshTokenString)
                .map(refreshTokenService::verifyRefreshToken)
                .map(RefreshToken::getAuthUser)
                .map(authUser -> {
                    // Generate the brand new short-lived JWT
                    String newAccessToken = authUtil.generateJWTToken(authUser);

                    // Attach the new Access Token to the response
                    Cookie jwtCookie = new Cookie("jwt", newAccessToken);
                    jwtCookie.setHttpOnly(true);
                    jwtCookie.setPath("/");
                    jwtCookie.setMaxAge(900); // Only 15 minutes!
                    response.addCookie(jwtCookie);

                    return ResponseEntity.ok("Token successfully refreshed");
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    @GetMapping("/hello")
    public String hello(){
        return "Hello World";
    }
}
