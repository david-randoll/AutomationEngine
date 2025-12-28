package com.davidrandoll.automation.engine.backend.api.json_schema;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = JsonSchemaConfig.class)
class SchemaDescriptionTest {

    @Autowired
    private SchemaGenerator schemaGenerator;

    @Test
    void testSchemaIncludesDescriptionsFromAnnotation() throws Exception {
        // Generate schema for a test context class with @SchemaDescription annotations
        JsonNode schema = schemaGenerator.generateSchema(TestActionContext.class);

        ObjectMapper mapper = new ObjectMapper();
        String schemaJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
        System.out.println("Generated Schema:");
        System.out.println(schemaJson);

        // Verify that descriptions are present in the schema
        JsonNode properties = schema.get("properties");
        assertThat(properties).isNotNull();

        // Check that the 'alias' field has a description
        JsonNode aliasProperty = properties.get("alias");
        assertThat(aliasProperty).isNotNull();
        JsonNode aliasDescription = aliasProperty.get("x-presentation-help");
        assertThat(aliasDescription).isNotNull();
        assertThat(aliasDescription.asText()).isEqualTo("Unique identifier for this action");

        // Check that the 'testField' field has a description
        JsonNode testFieldProperty = properties.get("testField");
        assertThat(testFieldProperty).isNotNull();
        JsonNode testFieldDescription = testFieldProperty.get("x-presentation-help");
        assertThat(testFieldDescription).isNotNull();
        assertThat(testFieldDescription.asText()).contains("Test field");
    }

    /**
     * Simple test context class for schema generation testing
     */
    @Data
    public static class TestActionContext implements IActionContext {
        @SchemaDescription("Unique identifier for this action")
        private String alias;

        @SchemaDescription("Human-readable description of what this action does")
        private String description;

        @SchemaDescription("Test field with a description")
        private String testField;

        @SchemaDescription("Number value for testing")
        private int numberValue;
    }
}

