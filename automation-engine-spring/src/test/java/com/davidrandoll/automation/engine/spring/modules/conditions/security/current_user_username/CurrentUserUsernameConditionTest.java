package com.davidrandoll.automation.engine.spring.modules.conditions.security.current_user_username;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.modules.events.DefaultEvent;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CurrentUserUsernameConditionTest extends AutomationEngineTest {

    @Test
    void testCurrentUserUsernameConditionReturnsTrueWhenUsernameMatches() {
        // Setup authenticated user (using constructor that makes it authenticated)
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        var authentication = new UsernamePasswordAuthenticationToken("testuser", "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        CurrentUserUsernameCondition condition = new CurrentUserUsernameCondition();
        CurrentUserUsernameConditionContext context = new CurrentUserUsernameConditionContext();
        context.setExpectedUsername("testuser");
        
        boolean result = condition.isSatisfied(EventContext.of(new DefaultEvent()), context);
        
        assertThat(result).isTrue();
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCurrentUserUsernameConditionReturnsFalseWhenUsernameDoesNotMatch() {
        // Setup authenticated user (using constructor that makes it authenticated)
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        var authentication = new UsernamePasswordAuthenticationToken("testuser", "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        CurrentUserUsernameCondition condition = new CurrentUserUsernameCondition();
        CurrentUserUsernameConditionContext context = new CurrentUserUsernameConditionContext();
        context.setExpectedUsername("differentuser");
        
        boolean result = condition.isSatisfied(EventContext.of(new DefaultEvent()), context);
        
        assertThat(result).isFalse();
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCurrentUserUsernameConditionReturnsFalseWhenNotAuthenticated() {
        // Ensure no authentication
        SecurityContextHolder.clearContext();
        
        CurrentUserUsernameCondition condition = new CurrentUserUsernameCondition();
        CurrentUserUsernameConditionContext context = new CurrentUserUsernameConditionContext();
        context.setExpectedUsername("testuser");
        
        boolean result = condition.isSatisfied(EventContext.of(new DefaultEvent()), context);
        
        assertThat(result).isFalse();
    }

    @Test
    void testCurrentUserUsernameConditionReturnsFalseForAnonymousUser() {
        // Setup anonymous user (using unauthenticated constructor)
        var authentication = new UsernamePasswordAuthenticationToken("anonymousUser", null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        CurrentUserUsernameCondition condition = new CurrentUserUsernameCondition();
        CurrentUserUsernameConditionContext context = new CurrentUserUsernameConditionContext();
        context.setExpectedUsername("anonymousUser");
        
        boolean result = condition.isSatisfied(EventContext.of(new DefaultEvent()), context);
        
        assertThat(result).isFalse();
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }
}