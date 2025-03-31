package com.automation.engine.modules.conditions.always_false;

import com.automation.engine.core.conditions.IConditionContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlwaysFalseConditionContext implements IConditionContext {
    private String alias;
}
