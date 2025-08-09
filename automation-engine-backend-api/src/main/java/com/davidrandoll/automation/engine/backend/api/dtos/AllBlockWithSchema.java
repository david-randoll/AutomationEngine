package com.davidrandoll.automation.engine.backend.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllBlockWithSchema {
    private List<BlockType> types;
}