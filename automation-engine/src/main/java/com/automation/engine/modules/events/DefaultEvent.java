package com.automation.engine.modules.events;

import com.automation.engine.core.events.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;
import org.springframework.lang.NonNull;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@FieldNameConstants
public class DefaultEvent extends Event {

    @Override
    @NonNull
    public Map<String, Object> getFieldValue() {
        return Map.of();
    }
}
