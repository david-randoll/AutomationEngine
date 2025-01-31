package com.automation.engine.modules.condition_building_block.not;

import com.automation.engine.factory.request.Condition;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotConditionContext {
    private List<Condition> conditions = new ArrayList<>();
}