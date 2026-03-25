package com.auth.auth_app.config;

import com.auth.auth_app.Exception.CustomAuthenticationEntryPoint;
import com.auth.auth_app.filter.CsrfTokenFilter;
import com.auth.auth_app.filter.JWTTokenGeneratorFilter;
import com.auth.auth_app.filter.JWTTokenValidatorFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig {

    private final Oauth2SuccessHandler oauth2SuccessHandler;
    private final JWTTokenValidatorFilter jwtTokenValidatorFilter;

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler = new CsrfTokenRequestAttributeHandler();
        return httpSecurity
                .authorizeHttpRequests(req -> req
                        .requestMatchers("/login","/auth/login","/auth/register","/auth/refresh","/auth/certs").permitAll()
                        .anyRequest().authenticated())
                .formLogin(Customizer.withDefaults())
                .httpBasic(hbc -> hbc.authenticationEntryPoint(new CustomAuthenticationEntryPoint()))
                .oauth2Login(oauth2->oauth2
                        .failureHandler((request, response, exception) -> {
                            log.error("Oauth2 error : {}", exception.getMessage());
                        })
                        .successHandler(oauth2SuccessHandler))
                .httpBasic(hbc-> hbc.authenticationEntryPoint(new CustomAuthenticationEntryPoint()))
//                .exceptionHandling(ehc -> ehc.authenticationEntryPoint(new CustomAuthenticationEntryPoint()))
                .sessionManagement(session -> session
                        .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::changeSessionId))
                .cors(corsConfig->corsConfig.configurationSource(request -> {
                    CorsConfiguration corsConfiguration = new CorsConfiguration();
                    corsConfiguration.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
                    corsConfiguration.setAllowedMethods(Collections.singletonList("*"));
                    corsConfiguration.setAllowCredentials(true);
                    corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));
                    corsConfiguration.setExposedHeaders(Arrays.asList("Authorization"));
                    corsConfiguration.setMaxAge(3600L);
                    return corsConfiguration;
                }))
                .csrf(csrfConfig -> csrfConfig
                        .csrfTokenRequestHandler(csrfTokenRequestAttributeHandler)
                        .ignoringRequestMatchers("/login","/auth/login","/auth/refresh")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .addFilterAfter(new CsrfTokenFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(new JWTTokenGeneratorFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(jwtTokenValidatorFilter, BasicAuthenticationFilter.class)
                .build();
    }
}
