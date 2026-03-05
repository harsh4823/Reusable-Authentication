package com.auth.auth_app.util;

import com.auth.auth_app.model.OAuth2UserInfo;

import java.util.Map;

public interface IOAuth2UserInfoExtractor {
    OAuth2UserInfo extractUserInfo(Map<String, Object> attributes);
    boolean supports(String providerName);
}
