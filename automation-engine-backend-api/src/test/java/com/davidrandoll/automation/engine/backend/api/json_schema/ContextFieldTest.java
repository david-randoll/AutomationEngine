package com.davidrandoll.automation.engine.backend.api.json_schema;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.spring.spi.ContextField;
import com.davidrandoll.automation.engine.spring.spi.ContextField.Widget;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = JsonSchemaConfig.class)
class ContextFieldTest {

    @Autowired
    private SchemaGenerator schemaGenerator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testContextFieldWithWidget() throws Exception {
        JsonNode schema = schemaGenerator.generateSchema(TestContextWithWidget.class);

        String schemaJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
        System.out.println("Schema with Widget:");
        System.out.println(schemaJson);

        JsonNode properties = schema.get("properties");
        assertThat(properties).isNotNull();

        // Check textarea field has correct widget
        JsonNode textareaField = properties.get("textareaField");
        assertThat(textareaField).isNotNull();
        assertThat(textareaField.get("x-presentation-widget").asText()).isEqualTo("textarea");
    }

    @Test
    void testContextFieldWithHelpText() throws Exception {
        JsonNode schema = schemaGenerator.generateSchema(TestContextWithHelpText.class);

        String schemaJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
        System.out.println("Schema with HelpText:");
        System.out.println(schemaJson);

        JsonNode properties = schema.get("properties");
        assertThat(properties).isNotNull();

        JsonNode fieldWithHelp = properties.get("fieldWithHelp");
        assertThat(fieldWithHelp).isNotNull();
        assertThat(fieldWithHelp.get("x-presentation-help").asText())
                .isEqualTo("This is helpful text for the user");
    }

    @Test
    void testContextFieldWithPlaceholder() throws Exception {
        JsonNode schema = schemaGenerator.generateSchema(TestContextWithPlaceholder.class);

        String schemaJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
        System.out.println("Schema with Placeholder:");
        System.out.println(schemaJson);

        JsonNode properties = schema.get("properties");
        assertThat(properties).isNotNull();

        JsonNode fieldWithPlaceholder = properties.get("fieldWithPlaceholder");
        assertThat(fieldWithPlaceholder).isNotNull();
        assertThat(fieldWithPlaceholder.get("x-presentation-placeholder").asText())
                .isEqualTo("Enter value here...");
    }

    @Test
    void testContextFieldWithDropdown() throws Exception {
        JsonNode schema = schemaGenerator.generateSchema(TestContextWithDropdown.class);

        String schemaJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
        System.out.println("Schema with Dropdown:");
        System.out.println(schemaJson);

        JsonNode properties = schema.get("properties");
        assertThat(properties).isNotNull();

        JsonNode dropdownField = properties.get("dropdownField");
        assertThat(dropdownField).isNotNull();
        assertThat(dropdownField.get("x-presentation-widget").asText()).isEqualTo("dropdown");
        
        JsonNode options = dropdownField.get("x-presentation-dropdown-options");
        assertThat(options).isNotNull();
        assertThat(options.isArray()).isTrue();
        assertThat(options).hasSize(3);
        assertThat(options.get(0).asText()).isEqualTo("option1");
        assertThat(options.get(1).asText()).isEqualTo("option2");
        assertThat(options.get(2).asText()).isEqualTo("option3");

        JsonNode labels = dropdownField.get("x-presentation-dropdown-labels");
        assertThat(labels).isNotNull();
        assertThat(labels.isArray()).isTrue();
        assertThat(labels).hasSize(3);
        assertThat(labels.get(0).asText()).isEqualTo("Option One");
        assertThat(labels.get(1).asText()).isEqualTo("Option Two");
        assertThat(labels.get(2).asText()).isEqualTo("Option Three");
    }

    @Test
    void testContextFieldWithMonacoEditor() throws Exception {
        JsonNode schema = schemaGenerator.generateSchema(TestContextWithMonaco.class);

        String schemaJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
        System.out.println("Schema with Monaco:");
        System.out.println(schemaJson);

        JsonNode properties = schema.get("properties");
        assertThat(properties).isNotNull();

        JsonNode monacoField = properties.get("scriptField");
        assertThat(monacoField).isNotNull();
        assertThat(monacoField.get("x-presentation-widget").asText()).isEqualTo("monaco_editor");
        assertThat(monacoField.get("x-presentation-monaco-language").asText()).isEqualTo("lua");
    }

    @Test
    void testContextFieldWithAllAttributes() throws Exception {
        JsonNode schema = schemaGenerator.generateSchema(TestContextWithAllAttributes.class);

        String schemaJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
        System.out.println("Schema with All Attributes:");
        System.out.println(schemaJson);

        JsonNode properties = schema.get("properties");
        assertThat(properties).isNotNull();

        JsonNode complexField = properties.get("complexField");
        assertThat(complexField).isNotNull();
        assertThat(complexField.get("x-presentation-widget").asText()).isEqualTo("textarea");
        assertThat(complexField.get("x-presentation-help").asText()).isEqualTo("Complex help text");
        assertThat(complexField.get("x-presentation-placeholder").asText()).isEqualTo("Complex placeholder");
    }

    @Test
    void testSchemaDescriptionSeparateFromContextField() throws Exception {
        JsonNode schema = schemaGenerator.generateSchema(TestContextMixedAnnotations.class);

        String schemaJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
        System.out.println("Schema with Mixed Annotations:");
        System.out.println(schemaJson);

        JsonNode properties = schema.get("properties");
        assertThat(properties).isNotNull();

        JsonNode mixedField = properties.get("mixedField");
        assertThat(mixedField).isNotNull();
        
        // Should have both description (from @SchemaDescription) and help text (from @ContextField)
        assertThat(mixedField.get("description").asText()).isEqualTo("This is the schema description");
        assertThat(mixedField.get("x-presentation-help").asText()).isEqualTo("This is the UI help text");
        assertThat(mixedField.get("x-presentation-widget").asText()).isEqualTo("textarea");
    }

    // Test context classes

    @Data
    public static class TestContextWithWidget implements IActionContext {
        private String alias;
        private String description;

        @ContextField(widget = Widget.TEXTAREA)
        private String textareaField;
    }

    @Data
    public static class TestContextWithHelpText implements IActionContext {
        private String alias;
        private String description;

        @ContextField(helpText = "This is helpful text for the user")
        private String fieldWithHelp;
    }

    @Data
    public static class TestContextWithPlaceholder implements IActionContext {
        private String alias;
        private String description;

        @ContextField(placeholder = "Enter value here...")
        private String fieldWithPlaceholder;
    }

    @Data
    public static class TestContextWithDropdown implements IActionContext {
        private String alias;
        private String description;

        @ContextField(
            widget = Widget.DROPDOWN,
            dropdownOptions = {"option1", "option2", "option3"},
            dropdownLabels = {"Option One", "Option Two", "Option Three"}
        )
        private String dropdownField;
    }

    @Data
    public static class TestContextWithMonaco implements IActionContext {
        private String alias;
        private String description;

        @ContextField(
            widget = Widget.MONACO_EDITOR,
            monacoLanguage = "lua",
            helpText = "Enter Lua script here"
        )
        private String scriptField;
    }

    @Data
    public static class TestContextWithAllAttributes implements IActionContext {
        private String alias;
        private String description;

        @ContextField(
            widget = Widget.TEXTAREA,
            placeholder = "Complex placeholder",
            helpText = "Complex help text"
        )
        private String complexField;
    }

    @Data
    public static class TestContextMixedAnnotations implements IActionContext {
        private String alias;
        private String description;

        @SchemaDescription("This is the schema description")
        @ContextField(
            widget = Widget.TEXTAREA,
            helpText = "This is the UI help text"
        )
        private String mixedField;
    }
}
