package com.davidrandoll.automation.engine.spring.security;

import com.davidrandoll.automation.engine.AutomationEngine;
import com.davidrandoll.automation.engine.spring.security.modules.conditions.current_user_has_role.CurrentUserHasRoleCondition;
import com.davidrandoll.automation.engine.spring.security.modules.conditions.current_user_username.CurrentUserUsernameCondition;
import com.davidrandoll.automation.engine.spring.security.modules.conditions.is_anonymous.IsAnonymousCondition;
import com.davidrandoll.automation.engine.spring.security.modules.conditions.is_authenticated.IsAuthenticatedCondition;
import com.davidrandoll.automation.engine.spring.security.properties.AESpringSecurityEnabled;
import com.davidrandoll.automation.engine.spring.security.properties.AESpringSecurityProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;

@Configuration
@Conditional(AESpringSecurityEnabled.class)
@ConditionalOnClass(Authentication.class)
public class AESpringSecurityConfig {

    @Bean("isAuthenticatedCondition")
    @ConditionalOnMissingBean(name = "isAuthenticatedCondition", ignored = IsAuthenticatedCondition.class)
    public IsAuthenticatedCondition isAuthenticatedCondition(AutomationEngine engine) {
        return new IsAuthenticatedCondition();
    }

    @Bean("isAnonymousCondition")
    @ConditionalOnMissingBean(name = "isAnonymousCondition", ignored = IsAnonymousCondition.class)
    public IsAnonymousCondition isAnonymousCondition(AutomationEngine engine) {
        return new IsAnonymousCondition();
    }

    @Bean("currentUserUsernameCondition")
    @ConditionalOnMissingBean(name = "currentUserUsernameCondition", ignored = CurrentUserUsernameCondition.class)
    public CurrentUserUsernameCondition currentUserUsernameCondition(AutomationEngine engine) {
        return new CurrentUserUsernameCondition();
    }

    @Bean("currentUserHasRoleCondition")
    @ConditionalOnMissingBean(name = "currentUserHasRoleCondition", ignored = CurrentUserHasRoleCondition.class)
    public CurrentUserHasRoleCondition currentUserHasRoleCondition(AutomationEngine engine) {
        return new CurrentUserHasRoleCondition();
    }

    @Bean
    @ConfigurationProperties(prefix = "automation.engine.spring.security")
    public AESpringSecurityProperties properties() {
        return new AESpringSecurityProperties();
    }
}