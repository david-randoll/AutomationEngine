package com.automation.engine.http.modules.conditions.http_response_status;

import com.automation.engine.core.conditions.IConditionContext;
import com.automation.engine.http.modules.conditions.MatchContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HttpResponseStatusConditionContext extends MatchContext implements IConditionContext {
    private String alias;
}