package com.automation.engine.modules.condition_building_block.or;

import com.automation.engine.core.conditions.ICondition;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties
public class OrConditionContext {
    private List<ICondition> conditions;
}