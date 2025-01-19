package com.automation.engine.modules.time_based;

import com.automation.engine.engine.triggers.TriggerContext;
import lombok.Data;

import java.time.LocalTime;

@Data
public class TimeBasedTriggerContext {
    private LocalTime beforeTime;
    private LocalTime afterTime;

    public TimeBasedTriggerContext(TriggerContext triggerContext) {
        if (triggerContext.getData() == null) return;
        var bt = triggerContext.getData().get("beforeTime");
        var at = triggerContext.getData().get("afterTime");
        this.beforeTime = bt == null ? null : LocalTime.parse(bt.toString());
        this.afterTime = at == null ? null : LocalTime.parse(at.toString());
    }
}