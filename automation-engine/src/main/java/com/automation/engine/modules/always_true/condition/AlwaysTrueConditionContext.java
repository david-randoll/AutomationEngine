package com.automation.engine.modules.always_true.condition;

import com.automation.engine.core.conditions.IConditionContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlwaysTrueConditionContext implements IConditionContext {
    private String alias;
}
