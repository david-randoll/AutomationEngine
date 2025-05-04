package com.davidrandoll.automation.engine.http.modules.conditions.http_latency;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Data
@NoArgsConstructor
public class HttpLatencyConditionContext implements IConditionContext {
    private String alias;

    @JsonAlias({"timeout", "duration"})
    private Duration duration;

    @JsonAlias({"minutes", "minute"})
    public void setMinutes(long minutes) {
        this.duration = Duration.ofMinutes(minutes);
    }

    @JsonAlias({"seconds", "second"})
    public void setSeconds(long seconds) {
        this.duration = Duration.ofSeconds(seconds);
    }

    @JsonAlias({"milliseconds", "millisecond"})
    public void setMilliseconds(long milliseconds) {
        this.duration = Duration.ofMillis(milliseconds);
    }
}