package com.example.back.support;

import com.example.back.auth.security.AuthenticatedUser;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

public final class TestAuth {

    public static final Long USER_ID = 1L;
    public static final String EMAIL = "user@example.com";

    private TestAuth() {
    }

    public static void setAuthenticatedUser() {
        SecurityContextHolder.getContext().setAuthentication(authenticationToken());
    }

    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    public static RequestPostProcessor authenticatedRequest() {
        return SecurityMockMvcRequestPostProcessors.authentication(authenticationToken());
    }

    private static UsernamePasswordAuthenticationToken authenticationToken() {
        return new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(USER_ID, EMAIL),
                null,
                List.of()
        );
    }
}
