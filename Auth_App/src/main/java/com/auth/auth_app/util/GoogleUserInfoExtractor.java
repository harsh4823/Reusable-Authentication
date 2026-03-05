package com.auth.auth_app.util;

import com.auth.auth_app.model.OAuth2UserInfo;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GoogleUserInfoExtractor implements IOAuth2UserInfoExtractor{
    @Override
    public OAuth2UserInfo extractUserInfo(Map<String, Object> attributes) {
        return new OAuth2UserInfo(
                attributes.get("sub").toString(),
                (String) attributes.get("email"),
                (String) attributes.get("name"),
                (String) attributes.get("picture")
        );
    }

    @Override
    public boolean supports(String providerName){
        return "google".equalsIgnoreCase(providerName);
    }
}
