package org.teamkorea.backend.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 로그인 방식 받기 (google / naver)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        System.out.println("OAuth 로그인 provider: " + registrationId);

        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        System.out.println("OAuth 사용자 정보: " + oAuth2User.getAttributes());

        return oAuth2User;
    }
}