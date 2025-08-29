package com.davidrandoll.automation.engine.modules.triggers.always_true;

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
        AlwaysTrueTriggerContext.Fields.alias,
        AlwaysTrueTriggerContext.Fields.description
})
public class AlwaysTrueTriggerContext implements ITriggerContext {
    private String alias;
    private String description;
}
