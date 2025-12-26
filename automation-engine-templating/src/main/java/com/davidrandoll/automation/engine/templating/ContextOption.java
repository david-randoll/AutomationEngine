package com.davidrandoll.automation.engine.templating;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContextOption {
    @JsonAlias({"templatingType", "templating_type", "templateEngine", "template_engine", "templateType", "template_type"})
    private String templatingType;
}