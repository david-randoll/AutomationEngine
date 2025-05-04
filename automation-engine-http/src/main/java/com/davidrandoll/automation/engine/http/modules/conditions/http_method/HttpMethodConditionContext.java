package com.davidrandoll.automation.engine.http.modules.conditions.http_method;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.davidrandoll.automation.engine.http.modules.conditions.MatchContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HttpMethodConditionContext extends MatchContext implements IConditionContext {
    private String alias;
}