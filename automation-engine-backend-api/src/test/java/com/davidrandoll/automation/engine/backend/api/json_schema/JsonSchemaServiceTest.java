package com.davidrandoll.automation.engine.backend.api.json_schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import(JsonSchemaConfig.class)
class JsonSchemaServiceTest {

    @Autowired
    private SchemaGenerator schemaGenerator;

    @Test
    void generateSchema_ShouldGenerateSchema() {
        JsonSchemaService service = new JsonSchemaService(schemaGenerator);
        JsonNode schema = service.generateSchema(TestPojo.class);

        assertThat(schema).isNotNull();
        assertThat(schema.get("type").asText()).isEqualTo("object");
        assertThat(schema.get("properties").get("name").get("type").asText()).isEqualTo("string");
    }

    @Test
    void generateSchema_ShouldIncludeBlockTypeAttribute() {
        JsonSchemaService service = new JsonSchemaService(schemaGenerator);
        JsonNode schema = service.generateSchema(TestTrigger.class);

        assertThat(schema.get("x-block-type").asText()).isEqualTo("trigger");
    }

    @Test
    void generateSchema_ShouldHandleJsonAnySetter() {
        JsonSchemaService service = new JsonSchemaService(schemaGenerator);
        JsonNode schema = service.generateSchema(TestAnySetter.class);

        assertThat(schema.get("additionalProperties")).isNotNull();
    }

    @Test
    void generateSchema_ShouldUseFullyQualifiedNames() {
        JsonSchemaService service = new JsonSchemaService(schemaGenerator);
        JsonNode schema = service.generateSchema(TestParentClass.class);

        assertThat(schema).isNotNull();

        // Check that $defs section exists
        JsonNode defs = schema.get("$defs");
        assertThat(defs).isNotNull();

        // Verify that the nested class uses fully qualified name
        String expectedName = "com.davidrandoll.automation.engine.backend.api.json_schema.JsonSchemaServiceTest.TestParentClass.NestedClass";
        assertThat(defs.has(expectedName))
                .as("Nested class should use fully qualified name")
                .isTrue();

        // Verify that simple names don't exist
        assertThat(defs.has("NestedClass"))
                .as("Simple class name should not exist")
                .isFalse();
    }

    @Test
    void generateSchema_ShouldHandleMultipleLevelsOfNesting() {
        JsonSchemaService service = new JsonSchemaService(schemaGenerator);
        JsonNode schema = service.generateSchema(TestOuterClass.class);

        assertThat(schema).isNotNull();

        JsonNode defs = schema.get("$defs");
        assertThat(defs).isNotNull();

        // Verify that deeply nested classes use fully qualified names
        String expectedMiddleName = "com.davidrandoll.automation.engine.backend.api.json_schema.JsonSchemaServiceTest.TestOuterClass.MiddleClass";
        String expectedInnerName = "com.davidrandoll.automation.engine.backend.api.json_schema.JsonSchemaServiceTest.TestOuterClass.MiddleClass.InnerClass";
        
        assertThat(defs.has(expectedMiddleName))
                .as("Middle nested class should use fully qualified name")
                .isTrue();

        assertThat(defs.has(expectedInnerName))
                .as("Deeply nested class should use fully qualified name")
                .isTrue();
    }

    static class TestPojo {
        public String name;
    }

    static class TestTrigger extends com.davidrandoll.automation.engine.creator.triggers.TriggerDefinition {
    }

    static class TestAnySetter {
        @com.fasterxml.jackson.annotation.JsonAnySetter
        public java.util.Map<String, String> properties;
    }

    static class TestParentClass {
        public String parentField;
        public NestedClass nested;

        static class NestedClass {
            public String nestedField;
        }
    }

    static class TestOuterClass {
        public String outerField;
        public MiddleClass middle;

        static class MiddleClass {
            public String middleField;
            public InnerClass inner;

            static class InnerClass {
                public String innerField;
            }
        }
    }
}
