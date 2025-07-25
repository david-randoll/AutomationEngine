package com.davidrandoll.automation.engine.spring.web.modules.conditions.http_full_path;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.davidrandoll.automation.engine.spring.web.modules.conditions.MatchContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HttpFullPathConditionContext extends MatchContext implements IConditionContext {
    private String alias;
    private String description;
}