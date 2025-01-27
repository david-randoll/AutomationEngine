package com.automation.engine.factory.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateRequest {
    private String alias;
    private List<Trigger> triggers = new ArrayList<>();
    private List<Condition> conditions = new ArrayList<>();
    private List<Action> actions = new ArrayList<>();
}