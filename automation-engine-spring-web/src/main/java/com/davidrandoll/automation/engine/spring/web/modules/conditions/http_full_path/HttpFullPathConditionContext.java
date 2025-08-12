package com.davidrandoll.automation.engine.spring.web.modules.conditions.http_full_path;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.davidrandoll.automation.engine.spring.web.modules.conditions.MatchContext;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants
@JsonPropertyOrder({
        HttpFullPathConditionContext.Fields.alias,
        HttpFullPathConditionContext.Fields.description
})
public class HttpFullPathConditionContext extends MatchContext implements IConditionContext {
    private String alias;
    private String description;
}