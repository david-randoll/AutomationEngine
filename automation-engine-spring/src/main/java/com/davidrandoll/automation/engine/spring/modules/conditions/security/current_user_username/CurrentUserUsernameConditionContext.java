package com.davidrandoll.automation.engine.spring.modules.conditions.security.current_user_username;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
@JsonPropertyOrder({
        CurrentUserUsernameConditionContext.Fields.alias,
        CurrentUserUsernameConditionContext.Fields.description,
        CurrentUserUsernameConditionContext.Fields.expectedUsername
})
public class CurrentUserUsernameConditionContext implements IConditionContext {
    private String alias;
    private String description;
    private String expectedUsername;
}