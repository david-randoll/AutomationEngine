package com.davidrandoll.automation.engine.spring.modules.conditions.security.current_user_has_role;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
@JsonPropertyOrder({
        CurrentUserHasRoleConditionContext.Fields.alias,
        CurrentUserHasRoleConditionContext.Fields.description,
        CurrentUserHasRoleConditionContext.Fields.requiredRoles,
        CurrentUserHasRoleConditionContext.Fields.requireAll
})
public class CurrentUserHasRoleConditionContext implements IConditionContext {
    private String alias;
    private String description;
    private List<String> requiredRoles;
    private boolean requireAll = false; // if true, user must have ALL roles; if false, user must have ANY role
}