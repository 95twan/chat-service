package com.rodemtree.chatservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository<? extends Session> httpSessionRepository;


    public String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    public void refreshTTL(String httpSessionId) {
        Session httpSession = httpSessionRepository.findById(httpSessionId);
        if (httpSession != null) {
            httpSession.setLastAccessedTime(Instant.now());
        }
    }
}
