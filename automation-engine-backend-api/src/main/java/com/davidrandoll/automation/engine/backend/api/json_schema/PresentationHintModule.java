package com.davidrandoll.automation.engine.backend.api.json_schema;

import com.davidrandoll.automation.spi.annotation.PresentationHint;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom Victools module to process @PresentationHint annotations and add presentation
 * metadata to JSON Schema as custom properties (x-presentation-*).
 * <p>
 * This module extracts presentation hints from field annotations and makes them available
 * to any presentation layer (UI, CLI, etc.) through the generated JSON Schema.
 * </p>
 */
@Slf4j
class PresentationHintModule implements Module {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
        builder.forFields()
                .withInstanceAttributeOverride((attributes, field, context) -> {
                    PresentationHint hint = field.getAnnotation(PresentationHint.class);
                    if (hint == null) {
                        return;
                    }

                    // Widget type
                    if (hint.widget() != PresentationHint.Widget.AUTO) {
                        attributes.put("x-presentation-widget", hint.widget().name().toLowerCase());
                    }

                    // Placeholder
                    if (!hint.placeholder().isEmpty()) {
                        attributes.put("x-presentation-placeholder", hint.placeholder());
                    }

                    // Help text
                    if (!hint.helpText().isEmpty()) {
                        attributes.put("x-presentation-help", hint.helpText());
                    }

                    // Dropdown options and labels
                    if (hint.dropdownOptions().length > 0) {
                        ArrayNode optionsArray = OBJECT_MAPPER.createArrayNode();
                        for (String option : hint.dropdownOptions()) {
                            optionsArray.add(option);
                        }
                        attributes.set("x-presentation-dropdown-options", optionsArray);

                        if (hint.dropdownLabels().length > 0) {
                            ArrayNode labelsArray = OBJECT_MAPPER.createArrayNode();
                            for (String label : hint.dropdownLabels()) {
                                labelsArray.add(label);
                            }
                            attributes.set("x-presentation-dropdown-labels", labelsArray);
                        }
                    }

                    // Monaco editor configuration
                    if (!hint.monacoLanguage().equals("text")) {
                        attributes.put("x-presentation-monaco-language", hint.monacoLanguage());
                    }

                    if (!hint.monacoOptions().isEmpty()) {
                        try {
                            JsonNode monacoOptionsJson = OBJECT_MAPPER.readTree(hint.monacoOptions());
                            attributes.set("x-presentation-monaco-options", monacoOptionsJson);
                        } catch (Exception e) {
                            log.warn("Failed to parse Monaco options JSON: {}", hint.monacoOptions(), e);
                        }
                    }

                    // Custom component
                    if (!hint.customComponent().isEmpty()) {
                        attributes.put("x-presentation-custom-component", hint.customComponent());
                    }

                    // Order
                    if (hint.order() != 0) {
                        attributes.put("x-presentation-order", hint.order());
                    }

                    // Validation hints (supplement Jakarta validation)
                    ObjectNode validationNode = null;

                    if (hint.min() != Double.NEGATIVE_INFINITY) {
                        if (validationNode == null) validationNode = OBJECT_MAPPER.createObjectNode();
                        validationNode.put("min", hint.min());
                    }

                    if (hint.max() != Double.POSITIVE_INFINITY) {
                        if (validationNode == null) validationNode = OBJECT_MAPPER.createObjectNode();
                        validationNode.put("max", hint.max());
                    }

                    if (!hint.pattern().isEmpty()) {
                        if (validationNode == null) validationNode = OBJECT_MAPPER.createObjectNode();
                        validationNode.put("pattern", hint.pattern());
                    }

                    if (!hint.validationMessage().isEmpty()) {
                        if (validationNode == null) validationNode = OBJECT_MAPPER.createObjectNode();
                        validationNode.put("message", hint.validationMessage());
                    }

                    if (hint.required()) {
                        if (validationNode == null) validationNode = OBJECT_MAPPER.createObjectNode();
                        validationNode.put("required", true);
                    }

                    if (validationNode != null) {
                        attributes.set("x-presentation-validation", validationNode);
                    }

                    // Read-only
                    if (hint.readOnly()) {
                        attributes.put("x-presentation-readonly", true);
                    }

                    // Custom properties
                    if (!hint.customProps().isEmpty()) {
                        try {
                            JsonNode customPropsJson = OBJECT_MAPPER.readTree(hint.customProps());
                            attributes.set("x-presentation-custom-props", customPropsJson);
                        } catch (Exception e) {
                            log.warn("Failed to parse custom properties JSON: {}", hint.customProps(), e);
                        }
                    }
                });
    }
}
