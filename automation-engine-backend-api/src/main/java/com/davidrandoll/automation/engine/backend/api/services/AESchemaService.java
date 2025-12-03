package com.davidrandoll.automation.engine.backend.api.services;

import com.davidrandoll.automation.engine.backend.api.dtos.AllBlockWithSchema;
import com.davidrandoll.automation.engine.backend.api.dtos.BlockType;
import com.davidrandoll.automation.engine.backend.api.dtos.BlocksByType;
import com.davidrandoll.automation.engine.backend.api.json_schema.JsonSchemaService;
import com.davidrandoll.automation.engine.core.IBlock;
import com.davidrandoll.automation.engine.core.actions.IAction;
import com.davidrandoll.automation.engine.core.conditions.ICondition;
import com.davidrandoll.automation.engine.core.result.IResult;
import com.davidrandoll.automation.engine.core.triggers.ITrigger;
import com.davidrandoll.automation.engine.core.variables.IVariable;
import com.davidrandoll.automation.engine.creator.AutomationDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class AESchemaService implements IAESchemaService {
    private final JsonSchemaService jsonSchemaService;
    private final ApplicationContext application;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Block type configurations: type name -> (bean suffix, discriminator field)
    private static final Map<String, BlockTypeConfig> BLOCK_TYPE_CONFIGS = Map.of(
            "triggers", new BlockTypeConfig("Trigger", "trigger"),
            "conditions", new BlockTypeConfig("Condition", "condition"),
            "actions", new BlockTypeConfig("Action", "action"),
            "variables", new BlockTypeConfig("Variable", "variable"));

    private record BlockTypeConfig(String beanSuffix, String discriminatorField) {
    }

    @Override
    public BlockType getAutomationDefinition() {
        var schema = jsonSchemaService.generateSchema(AutomationDefinition.class);
        return new BlockType(
                AutomationDefinition.class.getSimpleName(),
                "Automation Definition",
                "The parent block for all automations",
                schema,
                List.of());
    }

    @Override
    public BlocksByType getBlocksByType(String moduleType, Boolean includeSchema) {
        Class<? extends IBlock> clazz = getBeanByModule(moduleType);
        List<BlockType> types = getModuleByType(clazz, includeSchema);
        return new BlocksByType(types);
    }

    @Override
    public BlockType getSchemaByBlockName(String name) {
        // check if bean exists
        if (!application.containsBean(name)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No module found with name: " + name);
        }

        Object bean = application.getBean(name);
        if (!(bean instanceof IBlock module)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "%s is not a valid module".formatted(name));
        }
        var schema = jsonSchemaService.generateSchema(module.getContextType());
        return new BlockType(name, module, schema);
    }

    @Override
    public AllBlockWithSchema getAllBlockSchemas() {
        List<BlockType> types = getModuleByType(IBlock.class, true);
        return new AllBlockWithSchema(types);
    }

    private List<BlockType> getModuleByType(Class<? extends IBlock> clazz, Boolean includeSchema) {
        Map<String, ? extends IBlock> beans = application.getBeansOfType(clazz);
        return beans.entrySet().stream()
                .map(entry -> {
                    IBlock module = entry.getValue();
                    if (includeSchema == null || Boolean.FALSE.equals(includeSchema)) {
                        return new BlockType(entry.getKey(), module, null);
                    }
                    var schema = jsonSchemaService.generateSchema(module.getContextType());
                    return new BlockType(entry.getKey(), module, schema);
                })
                .toList();
    }

    private static Class<? extends IBlock> getBeanByModule(String type) {
        return switch (type.toLowerCase()) {
            case "action", "actions" -> IAction.class;
            case "condition", "conditions" -> ICondition.class;
            case "trigger", "triggers" -> ITrigger.class;
            case "result", "results" -> IResult.class;
            case "variable", "variables" -> IVariable.class;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid type: " + type);
        };
    }

    @Override
    public JsonNode getFullAutomationSchema() {
        // Start with the root schema from AutomationDefinition
        BlockType rootDefinition = getAutomationDefinition();
        ObjectNode rootSchema = (ObjectNode) rootDefinition.getSchema().deepCopy();

        // Add schema metadata
        rootSchema.put("$schema", "https://json-schema.org/draft/2020-12/schema");
        rootSchema.put("$id", "https://github.com/david-randoll/AutomationEngine/automation-engine-schema.json");
        rootSchema.put("title", "Automation Engine Definition");
        rootSchema.put("description",
                "Schema for defining automations in AutomationEngine. Automations can be defined in YAML or JSON format.");
        rootSchema.put("additionalProperties", false);

        // Get or create $defs section
        ObjectNode defs = rootSchema.has("$defs")
                ? (ObjectNode) rootSchema.get("$defs")
                : objectMapper.createObjectNode();

        // Process each block type (triggers, conditions, actions, variables)
        ObjectNode properties = (ObjectNode) rootSchema.get("properties");
        if (properties != null) {
            Iterator<String> fieldNames = properties.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                BlockTypeConfig config = BLOCK_TYPE_CONFIGS.get(fieldName);

                if (config != null) {
                    // Get all blocks of this type with their schemas
                    BlocksByType blocksByType = getBlocksByType(fieldName, true);
                    String defName = capitalize(fieldName.substring(0, fieldName.length() - 1)) + "Definition";

                    // Build the definition with conditional schemas
                    ObjectNode blockDefinition = buildBlockDefinition(blocksByType, config, defs);
                    defs.set(defName, blockDefinition);

                    // Update the property to reference the definition
                    ObjectNode propertyNode = (ObjectNode) properties.get(fieldName);
                    if (propertyNode.has("items")) {
                        ObjectNode items = (ObjectNode) propertyNode.get("items");
                        items.removeAll();
                        items.put("$ref", "#/$defs/" + defName);
                    }
                }
            }
        }

        rootSchema.set("$defs", defs);

        return rootSchema;
    }

    private ObjectNode buildBlockDefinition(BlocksByType blocksByType, BlockTypeConfig config, ObjectNode defs) {
        ObjectNode definition = objectMapper.createObjectNode();
        definition.put("type", "object");
        definition.put("description", "A " + config.discriminatorField() + " definition block");

        // Add required field for the discriminator
        ArrayNode required = objectMapper.createArrayNode();
        required.add(config.discriminatorField());
        definition.set("required", required);

        ArrayNode allOf = objectMapper.createArrayNode();

        // Build the enum of all block type names and base properties
        ObjectNode baseSchema = objectMapper.createObjectNode();
        ObjectNode baseProperties = objectMapper.createObjectNode();

        // Add discriminator field with enum of all block names
        ObjectNode discriminatorProp = objectMapper.createObjectNode();
        discriminatorProp.put("type", "string");
        discriminatorProp.put("description", "The " + config.discriminatorField() + " type");
        ArrayNode enumValues = objectMapper.createArrayNode();
        for (BlockType block : blocksByType.getTypes()) {
            String shortName = extractShortName(block.getName(), config.beanSuffix());
            enumValues.add(shortName);
        }
        discriminatorProp.set("enum", enumValues);
        baseProperties.set(config.discriminatorField(), discriminatorProp);

        baseSchema.set("properties", baseProperties);
        allOf.add(baseSchema);

        // Add conditional schemas for each block type
        for (BlockType block : blocksByType.getTypes()) {
            String shortName = extractShortName(block.getName(), config.beanSuffix());
            ObjectNode conditionalSchema = buildConditionalSchema(block, shortName, config, defs);
            allOf.add(conditionalSchema);
        }

        definition.set("allOf", allOf);
        return definition;
    }

    private ObjectNode buildConditionalSchema(BlockType block, String shortName, BlockTypeConfig config,
            ObjectNode defs) {
        ObjectNode conditionalSchema = objectMapper.createObjectNode();

        // Build "if" condition
        ObjectNode ifCondition = objectMapper.createObjectNode();
        ObjectNode ifProperties = objectMapper.createObjectNode();
        ObjectNode discriminatorConst = objectMapper.createObjectNode();
        discriminatorConst.put("const", shortName);
        ifProperties.set(config.discriminatorField(), discriminatorConst);
        ifCondition.set("properties", ifProperties);
        ArrayNode ifRequired = objectMapper.createArrayNode();
        ifRequired.add(config.discriminatorField());
        ifCondition.set("required", ifRequired);
        conditionalSchema.set("if", ifCondition);

        // Build "then" schema from the block's schema
        ObjectNode thenSchema = objectMapper.createObjectNode();
        JsonNode blockSchema = block.getSchema();

        if (blockSchema != null && blockSchema.has("properties")) {
            ObjectNode thenProperties = objectMapper.createObjectNode();

            // Add the discriminator field
            thenProperties.set(config.discriminatorField(),
                    objectMapper.createObjectNode().put("type", "string"));

            // Copy all properties from the block schema, processing nested references
            JsonNode schemaProperties = blockSchema.get("properties");
            Iterator<Map.Entry<String, JsonNode>> fields = schemaProperties.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                JsonNode processedValue = processSchemaNode(field.getValue(), blockSchema, defs);
                thenProperties.set(field.getKey(), processedValue);
            }

            thenSchema.set("properties", thenProperties);

            // Handle additionalProperties
            if (blockSchema.has("additionalProperties")) {
                thenSchema.set("additionalProperties", blockSchema.get("additionalProperties").deepCopy());
            } else {
                thenSchema.put("additionalProperties", false);
            }
        } else {
            // No schema available, allow additional properties
            thenSchema.put("additionalProperties", true);
        }

        conditionalSchema.set("then", thenSchema);
        return conditionalSchema;
    }

    private JsonNode processSchemaNode(JsonNode node, JsonNode sourceSchema, ObjectNode globalDefs) {
        if (node == null || !node.isObject()) {
            return node != null ? node.deepCopy() : null;
        }

        ObjectNode result = (ObjectNode) node.deepCopy();

        // Handle $ref - resolve and inline or move to global $defs
        if (result.has("$ref")) {
            String ref = result.get("$ref").asText();
            if (ref.startsWith("#/$defs/")) {
                String defName = ref.substring("#/$defs/".length());
                // Check if this definition exists in the source schema
                if (sourceSchema.has("$defs") && sourceSchema.get("$defs").has(defName)) {
                    JsonNode referencedDef = sourceSchema.get("$defs").get(defName);
                    // Add to global defs if not already present
                    if (!globalDefs.has(defName)) {
                        globalDefs.set(defName, processSchemaNode(referencedDef, sourceSchema, globalDefs));
                    }
                }
            }
        }

        // Recursively process nested objects
        if (result.has("properties")) {
            ObjectNode properties = (ObjectNode) result.get("properties");
            Iterator<Map.Entry<String, JsonNode>> fields = properties.fields();
            ObjectNode newProperties = objectMapper.createObjectNode();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                newProperties.set(field.getKey(), processSchemaNode(field.getValue(), sourceSchema, globalDefs));
            }
            result.set("properties", newProperties);
        }

        // Process items in arrays
        if (result.has("items")) {
            result.set("items", processSchemaNode(result.get("items"), sourceSchema, globalDefs));
        }

        // Process allOf, anyOf, oneOf
        for (String keyword : List.of("allOf", "anyOf", "oneOf")) {
            if (result.has(keyword) && result.get(keyword).isArray()) {
                ArrayNode array = (ArrayNode) result.get(keyword);
                ArrayNode newArray = objectMapper.createArrayNode();
                for (JsonNode item : array) {
                    newArray.add(processSchemaNode(item, sourceSchema, globalDefs));
                }
                result.set(keyword, newArray);
            }
        }

        return result;
    }

    private String extractShortName(String beanName, String suffix) {
        // Convert bean name like "alwaysTrueTrigger" to "alwaysTrue"
        String lowerSuffix = suffix.toLowerCase();
        if (beanName.toLowerCase().endsWith(lowerSuffix)) {
            return beanName.substring(0, beanName.length() - suffix.length());
        }
        return beanName;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
