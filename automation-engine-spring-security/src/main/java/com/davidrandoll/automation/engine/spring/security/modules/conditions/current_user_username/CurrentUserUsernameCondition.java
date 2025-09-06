package com.davidrandoll.automation.engine.spring.security.modules.conditions.current_user_username;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@RequiredArgsConstructor
public class CurrentUserUsernameCondition extends PluggableCondition<CurrentUserUsernameConditionContext> {
    
    @Override
    public boolean isSatisfied(EventContext ec, CurrentUserUsernameConditionContext cc) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        String actualUsername = null;
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserDetails) {
            actualUsername = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            actualUsername = (String) principal;
        } else {
            actualUsername = principal.toString();
        }
        
        return cc.getExpectedUsername() != null && 
               cc.getExpectedUsername().equals(actualUsername);
    }
}