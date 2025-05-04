package com.davidrandoll.automation.engine.modules.conditions.always_true;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlwaysTrueConditionContext implements IConditionContext {
    private String alias;
}
