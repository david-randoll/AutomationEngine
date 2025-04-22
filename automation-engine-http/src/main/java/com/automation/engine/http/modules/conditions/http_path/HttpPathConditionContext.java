package com.automation.engine.http.modules.conditions.http_path;

import com.automation.engine.core.conditions.IConditionContext;
import com.automation.engine.http.modules.conditions.StringMatchContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HttpPathConditionContext extends StringMatchContext implements IConditionContext {
    private String alias;
}