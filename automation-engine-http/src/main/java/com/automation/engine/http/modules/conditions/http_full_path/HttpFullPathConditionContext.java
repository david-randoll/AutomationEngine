package com.automation.engine.http.modules.conditions.http_full_path;

import com.automation.engine.core.conditions.IConditionContext;
import com.automation.engine.http.modules.conditions.StringMatchContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HttpFullPathConditionContext extends StringMatchContext implements IConditionContext {
    private String alias;
}