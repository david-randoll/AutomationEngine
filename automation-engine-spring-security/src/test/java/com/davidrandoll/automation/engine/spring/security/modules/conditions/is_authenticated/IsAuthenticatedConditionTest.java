package com.davidrandoll.automation.engine.spring.security.modules.conditions.is_authenticated;

import com.davidrandoll.automation.engine.core.events.EventContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

class IsAuthenticatedConditionTest {

    private IsAuthenticatedCondition condition;
    private IsAuthenticatedConditionContext context;
    private EventContext eventContext;

    @BeforeEach
    void setUp() {
        condition = new IsAuthenticatedCondition();
        context = new IsAuthenticatedConditionContext();
        eventContext = new EventContext(null, null, null);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnFalseWhenNoAuthentication() {
        boolean result = condition.isSatisfied(eventContext, context);
        assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenAnonymousUser() {
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken("anonymousUser", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        boolean result = condition.isSatisfied(eventContext, context);
        assertFalse(result);
    }

    @Test
    void shouldReturnTrueWhenAuthenticatedUser() {
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken("testuser", "password");
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        boolean result = condition.isSatisfied(eventContext, context);
        assertTrue(result);
    }
}