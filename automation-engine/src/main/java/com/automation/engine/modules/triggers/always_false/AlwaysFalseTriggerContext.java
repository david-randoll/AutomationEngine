package com.automation.engine.modules.triggers.always_false;

import com.automation.engine.core.triggers.ITriggerContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlwaysFalseTriggerContext implements ITriggerContext {
    private String alias;
}
