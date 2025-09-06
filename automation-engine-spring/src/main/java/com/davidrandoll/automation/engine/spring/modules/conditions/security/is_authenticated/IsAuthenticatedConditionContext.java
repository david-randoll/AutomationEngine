package com.davidrandoll.automation.engine.spring.modules.conditions.security.is_authenticated;

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
        IsAuthenticatedConditionContext.Fields.alias,
        IsAuthenticatedConditionContext.Fields.description
})
public class IsAuthenticatedConditionContext implements IConditionContext {
    private String alias;
    private String description;
}