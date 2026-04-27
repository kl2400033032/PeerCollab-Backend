package com.peercollab.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class AuthCookieService {

    @Value("${app.auth.cookie.name}")
    private String cookieName;

    @Value("${app.auth.cookie.secure}")
    private boolean secureCookie;

    @Value("${app.auth.cookie.same-site}")
    private String sameSite;

    @Value("${app.auth.cookie.domain:}")
    private String cookieDomain;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    public String buildAuthCookieHeader(String token) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .maxAge(jwtExpiration / 1000)
                .sameSite(sameSite);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain.trim());
        }

        return builder.build().toString();
    }

    public String buildLogoutCookieHeader() {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .maxAge(0)
                .sameSite(sameSite);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain.trim());
        }

        return builder.build().toString();
    }

    public String getCookieName() {
        return cookieName;
    }

    public String buildHeaderName() {
        return HttpHeaders.SET_COOKIE;
    }
}
