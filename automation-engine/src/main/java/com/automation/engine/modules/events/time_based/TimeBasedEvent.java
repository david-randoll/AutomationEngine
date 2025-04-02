package com.automation.engine.modules.events.time_based;

import com.automation.engine.core.events.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;
import org.springframework.lang.NonNull;

import java.time.LocalTime;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@FieldNameConstants
public class TimeBasedEvent extends Event {
    private final LocalTime time;

    @Override
    @NonNull
    public Map<String, Object> getFieldValue() {
        return Map.of(
                Fields.time, this.time
        );
    }
}