package com.davidrandoll.automation.engine.backend.api.json_schema;

import com.davidrandoll.automation.engine.spring.modules.actions.if_then_else.IfThenElseActionContext;
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
class NestedClassPrefixIntegrationTest {

    @Autowired
    private SchemaGenerator schemaGenerator;

    @Test
    void testIfThenElseActionContextNestedClassPrefix() {
        JsonSchemaService service = new JsonSchemaService(schemaGenerator);
        JsonNode schema = service.generateSchema(IfThenElseActionContext.class);

        assertThat(schema).isNotNull();

        JsonNode defs = schema.get("$defs");
        assertThat(defs).isNotNull();

        // Verify that the nested IfThenBlock class uses fully qualified name
        String expectedName = "com.davidrandoll.automation.engine.spring.modules.actions.if_then_else.IfThenElseActionContext.IfThenBlock";
        assertThat(defs.has(expectedName))
                .as("IfThenBlock should use fully qualified name")
                .isTrue();

        // Verify that the unprefixed name doesn't exist
        assertThat(defs.has("IfThenBlock"))
                .as("Unprefixed IfThenBlock should not exist")
                .isFalse();
    }
}
