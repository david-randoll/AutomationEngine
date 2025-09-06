package com.davidrandoll.automation.engine.spring.security.modules.conditions.current_user_has_role;

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
            return true; // No roles required, condition is satisfied
        }
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            return false;
        }
        
        // Convert authorities to role names
        List<String> userRoles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(this::normalizeRoleName)
                .collect(java.util.stream.Collectors.toList());
        
        // Normalize required roles for comparison
        List<String> normalizedRequiredRoles = requiredRoles.stream()
                .map(this::normalizeRoleName)
                .collect(java.util.stream.Collectors.toList());
        
        if (cc.isRequireAllRoles()) {
            // User must have ALL required roles
            return userRoles.containsAll(normalizedRequiredRoles);
        } else {
            // User must have ANY of the required roles
            return normalizedRequiredRoles.stream()
                    .anyMatch(userRoles::contains);
        }
    }
    
    /**
     * Normalize role names by ensuring they start with "ROLE_" prefix
     * This handles cases where roles are stored with or without the prefix
     */
    private String normalizeRoleName(String role) {
        if (role == null) {
            return null;
        }
        
        String trimmed = role.trim();
        if (trimmed.startsWith("ROLE_")) {
            return trimmed;
        } else {
            return "ROLE_" + trimmed;
        }
    }
}