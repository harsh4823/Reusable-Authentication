package com.auth.auth_app.controller;

import com.auth.auth_app.repository.TokenRepository;
import com.auth.auth_app.util.AuthUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.PublicKey;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TokenIntrospectionController {

    private final AuthUtil authUtil;
    private final TokenRepository tokenRepository;

    @PostMapping("/{realm}/protocol/openid-connect/token/introspect")
    public ResponseEntity<Map<String,Object>> introspect(
            @PathVariable String realm,
            @RequestParam("token") String token
    ){
        Map<String,Object> response = new LinkedHashMap<>();

        if (tokenRepository.isAccessTokenBlacklisted(token)){
            response.put("active",false);
            response.put("reason","Token has been revoked");
            return ResponseEntity.ok(response);
        }

        try {
            PublicKey publicKey = authUtil.getPublicKey();
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (claims.getExpiration().before(new Date())){
                response.put("active",false);
                response.put("reason","Token has been expired");
                return ResponseEntity.ok(response);
            }

            response.put("active",true);
            response.put("sub",claims.get("email",String.class));
            response.put("email",claims.get("email",String.class));
            response.put("authorities",claims.get("authorities",String.class));
            response.put("realm",realm);
            response.put("exp",claims.getExpiration().getTime()/1000);
            response.put("iat",claims.getIssuedAt().getTime()/1000);

        }catch (Exception e){
            response.put("active",false);
            response.put("reason","Invalid token");
        }
        return ResponseEntity.ok(response);
    }
}
