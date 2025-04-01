package com.automation.engine.modules.triggers.always_true;

import com.automation.engine.core.triggers.ITriggerContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlwaysTrueTriggerContext implements ITriggerContext {
    private String alias;
}
