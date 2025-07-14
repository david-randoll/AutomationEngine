package com.davidrandoll.automation.engine.http.modules.triggers.on_slow_http_request;

import com.davidrandoll.automation.engine.core.triggers.ITriggerContext;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Data
@NoArgsConstructor
public class OnSlowHttpRequestContext implements ITriggerContext {
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