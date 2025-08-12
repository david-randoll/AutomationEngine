package com.davidrandoll.automation.engine.spring.web.modules.conditions.http_latency;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.time.Duration;

@Data
@NoArgsConstructor
@FieldNameConstants
@JsonPropertyOrder({
        HttpLatencyConditionContext.Fields.alias,
        HttpLatencyConditionContext.Fields.description,
        HttpLatencyConditionContext.Fields.duration
})
public class HttpLatencyConditionContext implements IConditionContext {
    private String alias;
    private String description;

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