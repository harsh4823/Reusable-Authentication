package com.auth.auth_app.filter;

import com.auth.auth_app.constant.ApplicationConstant;
import com.auth.auth_app.util.AuthUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class JWTTokenGeneratorFilter extends OncePerRequestFilter {

    private AuthUtil authUtil;

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/login",
            "/auth/login",
            "/auth/register",
            "/user"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication!=null){
            Environment env = getEnvironment();
            if (env!=null){
                String jwt = authUtil.generateJWTToken(authentication);
                response.setHeader(ApplicationConstant.JWT_HEADER,jwt);

                Cookie jwtCookie = new Cookie("jwt", jwt);
                jwtCookie.setHttpOnly(true);     // Prevents JavaScript (XSS) from reading the token
                jwtCookie.setSecure(false);      // Set to true in production when using HTTPS
                jwtCookie.setPath("/");          // Available to all API endpoints
                jwtCookie.setMaxAge(30000);      // Expiration in seconds (align with JWT expiration)
                response.addCookie(jwtCookie);
            }
        }
        filterChain.doFilter(request,response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !EXCLUDED_PATHS.contains(request.getServletPath());
    }
}
