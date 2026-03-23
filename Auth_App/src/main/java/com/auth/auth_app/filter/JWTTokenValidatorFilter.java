package com.auth.auth_app.filter;

import com.auth.auth_app.constant.ApplicationConstant;
import com.auth.auth_app.repository.TokenRepository;
import com.auth.auth_app.util.AuthUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Component
public class JWTTokenValidatorFilter extends OncePerRequestFilter {

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/login",
            "/auth/login",
            "/auth/register",
            "/user",
            "/auth/refresh"
    );

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwt = authUtil.extractJwt(request);
        if (jwt!=null){
            if (tokenRepository.isAccessTokenBlacklisted(jwt)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Session has been terminated. Please log in again.");
                return;
            }

            try {
                Environment env = getEnvironment();
                if (env!=null) {
                    String secret = env.getProperty(ApplicationConstant.JWT_SECRET, ApplicationConstant.JWT_SECRET_DEFAULT_VALUE);
                    SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                    if (secretKey!=null){
                        Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(jwt).getPayload();
                        String email = claims.get("email",String.class);
                        String authorities = claims.get("authorities",String.class);

                        Authentication authentication = new UsernamePasswordAuthenticationToken(email, null,
                                AuthorityUtils.commaSeparatedStringToAuthorityList(authorities));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (Exception e) {
                // ADD THIS LINE to print the true cause to your terminal
                System.out.println("JWT PARSING FAILED BECAUSE: " + e.getMessage());
                e.printStackTrace();

                throw new BadCredentialsException("Invalid Token received");
            }
        }
        filterChain.doFilter(request,response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return EXCLUDED_PATHS.contains(request.getServletPath());
    }
}
