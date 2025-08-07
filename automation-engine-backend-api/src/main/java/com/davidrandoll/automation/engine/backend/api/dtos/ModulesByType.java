package com.davidrandoll.automation.engine.backend.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModulesByType {
    private List<ModuleType> types;
}