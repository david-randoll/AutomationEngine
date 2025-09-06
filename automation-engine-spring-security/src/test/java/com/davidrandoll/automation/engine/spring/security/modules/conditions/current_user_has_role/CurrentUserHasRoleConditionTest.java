package com.davidrandoll.automation.engine.spring.security.modules.conditions.current_user_has_role;

import com.davidrandoll.automation.engine.core.events.EventContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CurrentUserHasRoleConditionTest {

    private CurrentUserHasRoleCondition condition;
    private CurrentUserHasRoleConditionContext context;
    private EventContext eventContext;

    @BeforeEach
    void setUp() {
        condition = new CurrentUserHasRoleCondition();
        context = new CurrentUserHasRoleConditionContext();
        eventContext = new EventContext(null, null, null);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnFalseWhenNoAuthentication() {
        context.setRequiredRoles(List.of("ADMIN"));
        
        boolean result = condition.isSatisfied(eventContext, context);
        assertFalse(result);
    }

    @Test
    void shouldReturnTrueWhenUserHasRequiredRole() {
        // Setup authentication with ADMIN role
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "testuser", "password", 
            Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        context.setRequiredRoles(List.of("ADMIN"));
        context.setRequireAllRoles(false);
        
        boolean result = condition.isSatisfied(eventContext, context);
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenUserLacksRequiredRole() {
        // Setup authentication with USER role
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "testuser", "password", 
            Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        context.setRequiredRoles(List.of("ADMIN"));
        context.setRequireAllRoles(false);
        
        boolean result = condition.isSatisfied(eventContext, context);
        assertFalse(result);
    }

    @Test
    void shouldHandleRoleNormalization() {
        // Setup authentication with role without "ROLE_" prefix
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "testuser", "password", 
            Arrays.asList(new SimpleGrantedAuthority("ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        context.setRequiredRoles(List.of("ADMIN"));
        context.setRequireAllRoles(false);
        
        boolean result = condition.isSatisfied(eventContext, context);
        assertTrue(result);
    }

    @Test
    void shouldRequireAllRolesWhenFlagIsTrue() {
        // Setup authentication with only one of the required roles
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "testuser", "password", 
            Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        context.setRequiredRoles(List.of("ADMIN", "MODERATOR"));
        context.setRequireAllRoles(true);
        
        boolean result = condition.isSatisfied(eventContext, context);
        assertFalse(result);
    }

    @Test
    void shouldPassWhenUserHasAllRequiredRoles() {
        // Setup authentication with both required roles
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "testuser", "password", 
            Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_MODERATOR")
            )
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        context.setRequiredRoles(List.of("ADMIN", "MODERATOR"));
        context.setRequireAllRoles(true);
        
        boolean result = condition.isSatisfied(eventContext, context);
        assertTrue(result);
    }
}