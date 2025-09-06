package com.davidrandoll.automation.engine.spring.security.modules.conditions.current_user_username;

import com.davidrandoll.automation.engine.core.events.EventContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class CurrentUserUsernameConditionTest {

    private CurrentUserUsernameCondition condition;
    private CurrentUserUsernameConditionContext context;
    private EventContext eventContext;

    @BeforeEach
    void setUp() {
        condition = new CurrentUserUsernameCondition();
        context = new CurrentUserUsernameConditionContext();
        eventContext = new EventContext(null, null, null);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnFalseWhenNoAuthentication() {
        context.setExpectedUsername("testuser");
        
        boolean result = condition.isSatisfied(eventContext, context);
        assertFalse(result);
    }

    @Test
    void shouldReturnTrueWhenUsernameMatches() {
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken("testuser", "password");
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        context.setExpectedUsername("testuser");
        
        boolean result = condition.isSatisfied(eventContext, context);
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenUsernameDoesNotMatch() {
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken("testuser", "password");
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        context.setExpectedUsername("admin");
        
        boolean result = condition.isSatisfied(eventContext, context);
        assertFalse(result);
    }

    @Test
    void shouldHandleUserDetailsPrincipal() {
        User userDetails = new User("admin", "password", Collections.emptyList());
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken(userDetails, "password");
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        context.setExpectedUsername("admin");
        
        boolean result = condition.isSatisfied(eventContext, context);
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenExpectedUsernameIsNull() {
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken("testuser", "password");
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        context.setExpectedUsername(null);
        
        boolean result = condition.isSatisfied(eventContext, context);
        assertFalse(result);
    }
}