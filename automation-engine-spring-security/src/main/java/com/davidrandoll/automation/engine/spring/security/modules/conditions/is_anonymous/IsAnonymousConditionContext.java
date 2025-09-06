package com.davidrandoll.automation.engine.spring.security.modules.conditions.is_anonymous;

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
        IsAnonymousConditionContext.Fields.alias,
        IsAnonymousConditionContext.Fields.description
})
public class IsAnonymousConditionContext implements IConditionContext {
    private String alias;
    private String description;
}