package com.davidrandoll.automation.engine.spring.modules.conditions.security.current_user_has_role;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class CurrentUserHasRoleCondition extends PluggableCondition<CurrentUserHasRoleConditionContext> {
    
    @Override
    public boolean isSatisfied(EventContext ec, CurrentUserHasRoleConditionContext cc) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        List<String> requiredRoles = cc.getRequiredRoles();
        if (requiredRoles == null || requiredRoles.isEmpty()) {
            return true; // No roles required
        }
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            return false;
        }
        
        // Convert authorities to role names (with and without ROLE_ prefix)
        List<String> userRoles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        
        if (cc.isRequireAll()) {
            // User must have ALL required roles
            return requiredRoles.stream()
                    .allMatch(role -> hasRole(userRoles, role));
        } else {
            // User must have ANY of the required roles
            return requiredRoles.stream()
                    .anyMatch(role -> hasRole(userRoles, role));
        }
    }
    
    private boolean hasRole(List<String> userRoles, String requiredRole) {
        // Check both with and without ROLE_ prefix for flexibility
        return userRoles.contains(requiredRole) || 
               userRoles.contains("ROLE_" + requiredRole) ||
               (requiredRole.startsWith("ROLE_") && userRoles.contains(requiredRole.substring(5)));
    }
}