package com.davidrandoll.automation.engine.spring.security;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.security.modules.conditions.current_user_has_role.CurrentUserHasRoleCondition;
import com.davidrandoll.automation.engine.spring.security.modules.conditions.current_user_has_role.CurrentUserHasRoleConditionContext;
import com.davidrandoll.automation.engine.spring.security.modules.conditions.current_user_username.CurrentUserUsernameCondition;
import com.davidrandoll.automation.engine.spring.security.modules.conditions.current_user_username.CurrentUserUsernameConditionContext;
import com.davidrandoll.automation.engine.spring.security.modules.conditions.is_anonymous.IsAnonymousCondition;
import com.davidrandoll.automation.engine.spring.security.modules.conditions.is_anonymous.IsAnonymousConditionContext;
import com.davidrandoll.automation.engine.spring.security.modules.conditions.is_authenticated.IsAuthenticatedCondition;
import com.davidrandoll.automation.engine.spring.security.modules.conditions.is_authenticated.IsAuthenticatedConditionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test demonstrating all Spring Security conditions working together
 * to implement common security scenarios.
 */
class SpringSecurityConditionsIntegrationTest {

    private EventContext eventContext;

    @BeforeEach
    void setUp() {
        eventContext = new EventContext(null, null, null);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void scenarioAdminUserWorkflow() {
        // Simulate admin user authentication
        authenticateUser("admin", List.of("ROLE_ADMIN", "ROLE_USER"));
        
        // Test all conditions for admin workflow
        assertTrue(new IsAuthenticatedCondition().isSatisfied(eventContext, new IsAuthenticatedConditionContext()));
        assertFalse(new IsAnonymousCondition().isSatisfied(eventContext, new IsAnonymousConditionContext()));
        
        CurrentUserUsernameConditionContext usernameCtx = new CurrentUserUsernameConditionContext();
        usernameCtx.setExpectedUsername("admin");
        assertTrue(new CurrentUserUsernameCondition().isSatisfied(eventContext, usernameCtx));
        
        CurrentUserHasRoleConditionContext roleCtx = new CurrentUserHasRoleConditionContext();
        roleCtx.setRequiredRoles(List.of("ADMIN"));
        roleCtx.setRequireAllRoles(false);
        assertTrue(new CurrentUserHasRoleCondition().isSatisfied(eventContext, roleCtx));
    }

    @Test
    void scenarioRegularUserWorkflow() {
        // Simulate regular user authentication
        authenticateUser("user123", List.of("ROLE_USER"));
        
        // Test conditions for regular user workflow
        assertTrue(new IsAuthenticatedCondition().isSatisfied(eventContext, new IsAuthenticatedConditionContext()));
        assertFalse(new IsAnonymousCondition().isSatisfied(eventContext, new IsAnonymousConditionContext()));
        
        CurrentUserUsernameConditionContext usernameCtx = new CurrentUserUsernameConditionContext();
        usernameCtx.setExpectedUsername("user123");
        assertTrue(new CurrentUserUsernameCondition().isSatisfied(eventContext, usernameCtx));
        
        // Should NOT have admin role
        CurrentUserHasRoleConditionContext adminRoleCtx = new CurrentUserHasRoleConditionContext();
        adminRoleCtx.setRequiredRoles(List.of("ADMIN"));
        adminRoleCtx.setRequireAllRoles(false);
        assertFalse(new CurrentUserHasRoleCondition().isSatisfied(eventContext, adminRoleCtx));
        
        // Should have user role
        CurrentUserHasRoleConditionContext userRoleCtx = new CurrentUserHasRoleConditionContext();
        userRoleCtx.setRequiredRoles(List.of("USER"));
        userRoleCtx.setRequireAllRoles(false);
        assertTrue(new CurrentUserHasRoleCondition().isSatisfied(eventContext, userRoleCtx));
    }

    @Test
    void scenarioAnonymousUserWorkflow() {
        // No authentication - anonymous user
        
        // Test conditions for anonymous workflow
        assertFalse(new IsAuthenticatedCondition().isSatisfied(eventContext, new IsAuthenticatedConditionContext()));
        assertTrue(new IsAnonymousCondition().isSatisfied(eventContext, new IsAnonymousConditionContext()));
        
        CurrentUserUsernameConditionContext usernameCtx = new CurrentUserUsernameConditionContext();
        usernameCtx.setExpectedUsername("anyone");
        assertFalse(new CurrentUserUsernameCondition().isSatisfied(eventContext, usernameCtx));
        
        CurrentUserHasRoleConditionContext roleCtx = new CurrentUserHasRoleConditionContext();
        roleCtx.setRequiredRoles(List.of("USER"));
        roleCtx.setRequireAllRoles(false);
        assertFalse(new CurrentUserHasRoleCondition().isSatisfied(eventContext, roleCtx));
    }

    @Test
    void scenarioMultiRoleValidation() {
        // Simulate user with multiple roles
        authenticateUser("poweruser", List.of("ROLE_ADMIN", "ROLE_MODERATOR", "ROLE_USER"));
        
        // Test requiring ALL specific roles
        CurrentUserHasRoleConditionContext allRolesCtx = new CurrentUserHasRoleConditionContext();
        allRolesCtx.setRequiredRoles(List.of("ADMIN", "MODERATOR"));
        allRolesCtx.setRequireAllRoles(true);
        assertTrue(new CurrentUserHasRoleCondition().isSatisfied(eventContext, allRolesCtx));
        
        // Test requiring ALL roles including one user doesn't have
        CurrentUserHasRoleConditionContext missingRoleCtx = new CurrentUserHasRoleConditionContext();
        missingRoleCtx.setRequiredRoles(List.of("ADMIN", "SUPER_ADMIN"));
        missingRoleCtx.setRequireAllRoles(true);
        assertFalse(new CurrentUserHasRoleCondition().isSatisfied(eventContext, missingRoleCtx));
        
        // Test requiring ANY of several roles
        CurrentUserHasRoleConditionContext anyRoleCtx = new CurrentUserHasRoleConditionContext();
        anyRoleCtx.setRequiredRoles(List.of("ADMIN", "SUPER_ADMIN"));
        anyRoleCtx.setRequireAllRoles(false);
        assertTrue(new CurrentUserHasRoleCondition().isSatisfied(eventContext, anyRoleCtx));
    }

    private void authenticateUser(String username, List<String> roles) {
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(java.util.stream.Collectors.toList());
        
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                username, "password", authorities);
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}