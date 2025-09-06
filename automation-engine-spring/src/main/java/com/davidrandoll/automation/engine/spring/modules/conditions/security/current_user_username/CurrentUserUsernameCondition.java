package com.davidrandoll.automation.engine.spring.modules.conditions.security.current_user_username;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RequiredArgsConstructor
public class CurrentUserUsernameCondition extends PluggableCondition<CurrentUserUsernameConditionContext> {
    
    @Override
    public boolean isSatisfied(EventContext ec, CurrentUserUsernameConditionContext cc) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        String currentUsername = authentication.getName();
        if (currentUsername == null || "anonymousUser".equals(currentUsername)) {
            return false;
        }
        
        return currentUsername.equals(cc.getExpectedUsername());
    }
}