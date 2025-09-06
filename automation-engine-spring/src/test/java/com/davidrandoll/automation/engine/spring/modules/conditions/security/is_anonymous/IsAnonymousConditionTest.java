package com.davidrandoll.automation.engine.spring.modules.conditions.security.is_anonymous;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.modules.events.DefaultEvent;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IsAnonymousConditionTest extends AutomationEngineTest {

    @Test
    void testIsAnonymousConditionReturnsTrueWhenNotAuthenticated() {
        // Ensure no authentication
        SecurityContextHolder.clearContext();
        
        IsAnonymousCondition condition = new IsAnonymousCondition();
        IsAnonymousConditionContext context = new IsAnonymousConditionContext();
        
        boolean result = condition.isSatisfied(EventContext.of(new DefaultEvent()), context);
        
        assertThat(result).isTrue();
    }

    @Test
    void testIsAnonymousConditionReturnsFalseWhenAuthenticated() {
        // Setup authenticated user (using constructor that makes it authenticated)
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        var authentication = new UsernamePasswordAuthenticationToken("testuser", "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        IsAnonymousCondition condition = new IsAnonymousCondition();
        IsAnonymousConditionContext context = new IsAnonymousConditionContext();
        
        boolean result = condition.isSatisfied(EventContext.of(new DefaultEvent()), context);
        
        assertThat(result).isFalse();
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void testIsAnonymousConditionReturnsTrueForAnonymousUser() {
        // Setup anonymous user (using unauthenticated constructor)
        var authentication = new UsernamePasswordAuthenticationToken("anonymousUser", null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        IsAnonymousCondition condition = new IsAnonymousCondition();
        IsAnonymousConditionContext context = new IsAnonymousConditionContext();
        
        boolean result = condition.isSatisfied(EventContext.of(new DefaultEvent()), context);
        
        assertThat(result).isTrue();
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }
}