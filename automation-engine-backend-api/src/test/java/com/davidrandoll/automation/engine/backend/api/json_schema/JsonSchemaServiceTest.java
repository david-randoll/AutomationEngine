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
    void generateSchema_ShouldPrefixNestedClassDefinitions() {
        JsonSchemaService service = new JsonSchemaService(schemaGenerator);
        JsonNode schema = service.generateSchema(TestParentClass.class);

        assertThat(schema).isNotNull();

        // Check that $defs section exists
        JsonNode defs = schema.get("$defs");
        assertThat(defs).isNotNull();

        // Verify that the nested class has the parent class prefix
        // Note: Since TestParentClass is itself nested in JsonSchemaServiceTest,
        // the full name will be JsonSchemaServiceTest.TestParentClass.NestedClass
        assertThat(defs.has("JsonSchemaServiceTest.TestParentClass.NestedClass"))
                .as("Nested class should be prefixed with parent class name")
                .isTrue();

        // Verify that a non-nested class doesn't have a prefix
        assertThat(defs.has("NestedClass"))
                .as("Unprefixed nested class name should not exist")
                .isFalse();
    }

    @Test
    void generateSchema_ShouldHandleMultipleLevelsOfNesting() {
        JsonSchemaService service = new JsonSchemaService(schemaGenerator);
        JsonNode schema = service.generateSchema(TestOuterClass.class);

        assertThat(schema).isNotNull();

        JsonNode defs = schema.get("$defs");
        assertThat(defs).isNotNull();

        // Verify that deeply nested classes have full path prefix
        // Note: Since TestOuterClass is itself nested in JsonSchemaServiceTest,
        // the full names will include JsonSchemaServiceTest prefix
        assertThat(defs.has("JsonSchemaServiceTest.TestOuterClass.MiddleClass"))
                .as("Middle nested class should be prefixed with outer class name")
                .isTrue();

        assertThat(defs.has("JsonSchemaServiceTest.TestOuterClass.MiddleClass.InnerClass"))
                .as("Deeply nested class should have full path prefix")
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
