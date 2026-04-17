package com.auth.auth_app.controller;

import com.auth.auth_app.entity.AuthUser;
import com.auth.auth_app.model.UserInfoResponse;
import com.auth.auth_app.repository.AuthUserRepository;
import com.auth.auth_app.util.AuthUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.PublicKey;

@RestController
@RequiredArgsConstructor
public class UserInfoController {

    private final AuthUtil authUtil;
    private final AuthUserRepository authUserRepository;

    @GetMapping("/{realm}/protocol/openid-connect/userinfo")
    public ResponseEntity<?> getUserInfo(
            @PathVariable String realm,
            HttpServletRequest request
    ){
        String jwt = authUtil.extractJwt(request);

        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Missing Authorization Header");
        }

        try{
            PublicKey publicKey = authUtil.getPublicKey();
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();

            String email = claims.get("email", String.class);

            AuthUser authUser = authUserRepository.findByEmail(email)
                    .orElse(null);

            if (authUser == null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found");
            }

            String tokenRealm =  claims.get("realm", String.class);

            if (tokenRealm != null && !tokenRealm.equals(realm) && !tokenRealm.equals("master")){
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Token realm does not match with requested realm");
            }

            UserInfoResponse response = new UserInfoResponse(
                    email,
                    email,
                    authUser.getName(),
                    authUser.getImage(),
                    realm,
                    true
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token");
        }
    }
}
