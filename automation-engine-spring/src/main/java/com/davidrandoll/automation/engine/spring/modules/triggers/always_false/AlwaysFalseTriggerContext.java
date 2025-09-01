package com.davidrandoll.automation.engine.spring.modules.triggers.always_false;

import com.davidrandoll.automation.engine.core.triggers.ITriggerContext;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
@JsonPropertyOrder({
        AlwaysFalseTriggerContext.Fields.alias,
        AlwaysFalseTriggerContext.Fields.description
})
public class AlwaysFalseTriggerContext implements ITriggerContext {
    private String alias;
    private String description;
}
