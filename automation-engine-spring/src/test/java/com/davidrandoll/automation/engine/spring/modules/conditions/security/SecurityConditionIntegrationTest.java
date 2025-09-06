package com.davidrandoll.automation.engine.spring.modules.conditions.security;

import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.spring.modules.events.DefaultEvent;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConditionIntegrationTest extends AutomationEngineTest {

    @Test
    void testIsAuthenticatedConditionIntegrationWithYaml() {
        // Setup authenticated user
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        var authentication = new UsernamePasswordAuthenticationToken("testuser", "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        var yaml = """
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: isAuthenticatedCondition
                actions:
                  - action: logger
                    message: "User is authenticated!"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act
        engine.publishEvent(new DefaultEvent());

        // Assert
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("User is authenticated!"));
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void testIsAnonymousConditionIntegrationWithYaml() {
        // Ensure no authentication (anonymous)
        SecurityContextHolder.clearContext();
        
        var yaml = """
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: isAnonymousCondition
                actions:
                  - action: logger
                    message: "User is anonymous!"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act
        engine.publishEvent(new DefaultEvent());

        // Assert
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("User is anonymous!"));
    }

    @Test
    void testCurrentUserUsernameConditionIntegrationWithYaml() {
        // Setup authenticated user
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        var authentication = new UsernamePasswordAuthenticationToken("john.doe", "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        var yaml = """
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: currentUserUsernameCondition
                    expectedUsername: "john.doe"
                actions:
                  - action: logger
                    message: "Username matches!"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act
        engine.publishEvent(new DefaultEvent());

        // Assert
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("Username matches!"));
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCurrentUserHasRoleConditionIntegrationWithYaml() {
        // Setup authenticated user with admin role
        var authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        var authentication = new UsernamePasswordAuthenticationToken("admin.user", "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        var yaml = """
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: currentUserHasRoleCondition
                    requiredRoles:
                      - "ADMIN"
                    requireAll: false
                actions:
                  - action: logger
                    message: "User has admin role!"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act
        engine.publishEvent(new DefaultEvent());

        // Assert
        assertThat(logAppender.getLoggedMessages())
                .anyMatch(msg -> msg.contains("User has admin role!"));
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void testSecurityConditionBlocksExecution() {
        // Setup user without admin role
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        var authentication = new UsernamePasswordAuthenticationToken("regular.user", "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        var yaml = """
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: currentUserHasRoleCondition
                    requiredRoles:
                      - "ADMIN"
                    requireAll: false
                actions:
                  - action: logger
                    message: "This should not be logged!"
                """;

        Automation automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        // Act
        engine.publishEvent(new DefaultEvent());

        // Assert - message should NOT be logged since user doesn't have ADMIN role
        assertThat(logAppender.getLoggedMessages())
                .noneMatch(msg -> msg.contains("This should not be logged!"));
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }
}