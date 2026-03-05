package com.auth.auth_app.util;

import com.auth.auth_app.model.OAuth2UserInfo;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GithubUserInfoExtractor implements IOAuth2UserInfoExtractor{
    @Override
    public OAuth2UserInfo extractUserInfo(Map<String, Object> attributes) {
        return new OAuth2UserInfo(
                attributes.get("id").toString(),
                (String) attributes.get("email"),
                (String) attributes.get("name"),
                (String) attributes.get("avatar_url")
        );
    }

    @Override
    public boolean supports(String providerName) {
        return "github".equalsIgnoreCase(providerName);
    }
}
