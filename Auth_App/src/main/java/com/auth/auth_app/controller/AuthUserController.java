package com.auth.auth_app.controller;

import com.auth.auth_app.constant.ApplicationConstant;
import com.auth.auth_app.entity.AuthUser;
import com.auth.auth_app.entity.RefreshToken;
import com.auth.auth_app.model.AuthUserDto;
import com.auth.auth_app.model.LoginRequest;
import com.auth.auth_app.model.LoginResponse;
import com.auth.auth_app.repository.AuthUserRepository;
import com.auth.auth_app.repository.TokenRepository;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthUserController {

    private final IAuthService authService;
    private final IRefreshTokenService refreshTokenService;
    private final AuthUtil authUtil;
    private final TokenRepository tokenRepository;
    private final AuthUserRepository authUserRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> apiLogin(@RequestBody LoginRequest loginRequest){
        try{
        LoginResponse loginResponse = authService.authenticateAndGenerateToken(loginRequest);

        return ResponseEntity.status(HttpStatus.OK).header(ApplicationConstant.JWT_HEADER,loginResponse.jwtToken())
                .body(loginResponse);
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
        String refreshTokenString = authUtil.extractRefreshToken(request);

        if (refreshTokenString == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Refresh Token is missing!");
        }

        if (tokenRepository.isRefreshTokenBlacklisted(refreshTokenString)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Refresh Token hs been revoked!");
        }

        return refreshTokenService.findByToken(refreshTokenString)
                .map(refreshTokenService::verifyRefreshToken)
                .map(RefreshToken::getAuthUser)
                .map(authUser -> {

                    String newAccessToken = authUtil.generateJWTToken(authUser);

                    tokenRepository.storeTokens(authUser.getUserId(),newAccessToken,refreshTokenString);

                    Cookie jwtCookie = new Cookie("jwt", newAccessToken);
                    jwtCookie.setHttpOnly(true);
                    jwtCookie.setPath("/");
                    jwtCookie.setMaxAge(900); // Only 15 minutes!
                    response.addCookie(jwtCookie);

                    return ResponseEntity.ok("Token successfully refreshed");
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    @PostMapping("/logout/single")
    public ResponseEntity<?> logoutFromSingleDevice(HttpServletRequest request, HttpServletResponse response) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AuthUser authUser = authUserRepository.findByEmail(email).orElse(null);

        if (authUser != null) {
            String jwt = authUtil.extractJwt(request);
            String refreshToken = authUtil.extractRefreshToken(request);

            authService.logoutFromSingleDevice(authUser.getUserId(), jwt, refreshToken);

            authUtil.clearBrowserCookies(response);
            return ResponseEntity.status(HttpStatus.OK).body("Logout successful");
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Logout failed");
    }

    @PostMapping("/logout/all")
    public ResponseEntity<?> logoutFromAllDevices(HttpServletResponse response) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AuthUser authUser = authUserRepository.findByEmail(email).orElse(null);

        if (authUser != null) {
            authService.logoutFromAllDevices(authUser.getUserId());
            authUtil.clearBrowserCookies(response);
            return ResponseEntity.status(HttpStatus.OK).body("Logout successful from all devices");
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Logout failed");
    }

    @GetMapping("/certs")
    public ResponseEntity<Map<String,String >> getPublicKey(){
        PublicKey publicKey = authUtil.getPublicKey();
        String base64PublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        Map<String, String> response = new HashMap<>();
        response.put("kty", "RSA");
        response.put("use", "sig");
        response.put("publicKey", base64PublicKey);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/hello")
    public String hello(){
        return "Hello World";
    }
}
