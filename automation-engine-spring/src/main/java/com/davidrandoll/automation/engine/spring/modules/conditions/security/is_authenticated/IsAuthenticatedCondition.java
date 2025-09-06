package com.davidrandoll.automation.engine.spring.modules.conditions.security.is_authenticated;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RequiredArgsConstructor
public class IsAuthenticatedCondition extends PluggableCondition<IsAuthenticatedConditionContext> {
    
    @Override
    public boolean isSatisfied(EventContext ec, IsAuthenticatedConditionContext cc) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getName());
    }
}