package com.davidrandoll.automation.engine.spring.modules.conditions.security.current_user_has_role;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.modules.events.DefaultEvent;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CurrentUserHasRoleConditionTest extends AutomationEngineTest {

    @Test
    void testCurrentUserHasRoleConditionReturnsTrueWhenUserHasRequiredRole() {
        // Setup authenticated user with roles
        var authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        var authentication = new UsernamePasswordAuthenticationToken("testuser", "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        CurrentUserHasRoleCondition condition = new CurrentUserHasRoleCondition();
        CurrentUserHasRoleConditionContext context = new CurrentUserHasRoleConditionContext();
        context.setRequiredRoles(List.of("USER"));
        context.setRequireAll(false);
        
        boolean result = condition.isSatisfied(EventContext.of(new DefaultEvent()), context);
        
        assertThat(result).isTrue();
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCurrentUserHasRoleConditionReturnsTrueWhenUserHasRoleWithPrefix() {
        // Setup authenticated user with roles
        var authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        var authentication = new UsernamePasswordAuthenticationToken("testuser", "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        CurrentUserHasRoleCondition condition = new CurrentUserHasRoleCondition();
        CurrentUserHasRoleConditionContext context = new CurrentUserHasRoleConditionContext();
        context.setRequiredRoles(List.of("ROLE_USER"));
        context.setRequireAll(false);
        
        boolean result = condition.isSatisfied(EventContext.of(new DefaultEvent()), context);
        
        assertThat(result).isTrue();
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCurrentUserHasRoleConditionReturnsFalseWhenUserDoesNotHaveRole() {
        // Setup authenticated user with roles
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        var authentication = new UsernamePasswordAuthenticationToken("testuser", "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        CurrentUserHasRoleCondition condition = new CurrentUserHasRoleCondition();
        CurrentUserHasRoleConditionContext context = new CurrentUserHasRoleConditionContext();
        context.setRequiredRoles(List.of("ADMIN"));
        context.setRequireAll(false);
        
        boolean result = condition.isSatisfied(EventContext.of(new DefaultEvent()), context);
        
        assertThat(result).isFalse();
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCurrentUserHasRoleConditionRequireAllReturnsTrueWhenUserHasAllRoles() {
        // Setup authenticated user with roles
        var authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("ROLE_ADMIN"),
            new SimpleGrantedAuthority("ROLE_MODERATOR")
        );
        var authentication = new UsernamePasswordAuthenticationToken("testuser", "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        CurrentUserHasRoleCondition condition = new CurrentUserHasRoleCondition();
        CurrentUserHasRoleConditionContext context = new CurrentUserHasRoleConditionContext();
        context.setRequiredRoles(List.of("USER", "ADMIN"));
        context.setRequireAll(true);
        
        boolean result = condition.isSatisfied(EventContext.of(new DefaultEvent()), context);
        
        assertThat(result).isTrue();
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCurrentUserHasRoleConditionRequireAllReturnsFalseWhenUserMissingRole() {
        // Setup authenticated user with roles
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        var authentication = new UsernamePasswordAuthenticationToken("testuser", "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        CurrentUserHasRoleCondition condition = new CurrentUserHasRoleCondition();
        CurrentUserHasRoleConditionContext context = new CurrentUserHasRoleConditionContext();
        context.setRequiredRoles(List.of("USER", "ADMIN"));
        context.setRequireAll(true);
        
        boolean result = condition.isSatisfied(EventContext.of(new DefaultEvent()), context);
        
        assertThat(result).isFalse();
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCurrentUserHasRoleConditionReturnsFalseWhenNotAuthenticated() {
        // Ensure no authentication
        SecurityContextHolder.clearContext();
        
        CurrentUserHasRoleCondition condition = new CurrentUserHasRoleCondition();
        CurrentUserHasRoleConditionContext context = new CurrentUserHasRoleConditionContext();
        context.setRequiredRoles(List.of("USER"));
        context.setRequireAll(false);
        
        boolean result = condition.isSatisfied(EventContext.of(new DefaultEvent()), context);
        
        assertThat(result).isFalse();
    }

    @Test
    void testCurrentUserHasRoleConditionReturnsTrueWhenNoRolesRequired() {
        // Setup authenticated user with roles
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        var authentication = new UsernamePasswordAuthenticationToken("testuser", "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        CurrentUserHasRoleCondition condition = new CurrentUserHasRoleCondition();
        CurrentUserHasRoleConditionContext context = new CurrentUserHasRoleConditionContext();
        context.setRequiredRoles(List.of()); // Empty list
        context.setRequireAll(false);
        
        boolean result = condition.isSatisfied(EventContext.of(new DefaultEvent()), context);
        
        assertThat(result).isTrue();
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }
}