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

    static class TestPojo {
        public String name;
    }

    static class TestTrigger extends com.davidrandoll.automation.engine.creator.triggers.TriggerDefinition {
    }

    static class TestAnySetter {
        @com.fasterxml.jackson.annotation.JsonAnySetter
        public java.util.Map<String, String> properties;
    }
}
