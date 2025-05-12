package com.davidrandoll.automation.engine.modules.results.basic;

import com.davidrandoll.automation.engine.AutomationEngineTest;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.AutomationResult;
import com.davidrandoll.automation.engine.modules.events.time_based.TimeBasedEvent;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class BasicResultTest extends AutomationEngineTest {

    @Test
    void testRunAutomation_withCustomResult() {
        var yaml = """
                alias: return-custom-result
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Executing actions..."
                result:
                  alias: result-alias
                  success: true
                  message: "Operation completed"
                  recordId: 12345
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));

        AutomationResult result = engine.runAutomation(automation, eventContext);
        assertThat(result.isExecuted()).isTrue();
        JsonNode resultNode = (JsonNode) result.getResult().orElseThrow();

        assertThat(resultNode.get("success").asBoolean()).isTrue();
        assertThat(resultNode.get("message").asText()).isEqualTo("Operation completed");
        assertThat(resultNode.get("recordId").asInt()).isEqualTo(12345);
    }

    @Test
    void testRunAutomation_withSingleResultField() {
        var yaml = """
                alias: single-result
                triggers:
                  - trigger: alwaysTrue
                actions: []
                result:
                  status: "ok"
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isTrue();

        JsonNode resultNode = (JsonNode) result.getResult().orElseThrow();

        assertThat(resultNode.get("status").asText()).isEqualTo("ok");
    }

    @Test
    void testRunAutomation_withNestedResultFields() {
        var yaml = """
                alias: nested-result
                triggers:
                  - trigger: alwaysTrue
                actions: []
                result:
                  success: true
                  details:
                    createdAt: "2025-05-10T12:00:00Z"
                    createdBy: "admin"
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isTrue();

        JsonNode resultNode = (JsonNode) result.getResult().orElseThrow();
        assertThat(resultNode.get("success").asBoolean()).isTrue();
        assertThat(resultNode.get("details")).isNotNull();
        var detailsNode = resultNode.get("details");
        assertThat(detailsNode.get("createdAt").asText()).isEqualTo("2025-05-10T12:00:00Z");
        assertThat(detailsNode.get("createdBy").asText()).isEqualTo("admin");
    }

    @Test
    void testRunAutomation_withListResultField() {
        var yaml = """
                alias: list-result
                triggers:
                  - trigger: alwaysTrue
                actions: []
                result:
                  items:
                    - "item1"
                    - "item2"
                    - "item3"
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isTrue();
        JsonNode resultNode = (JsonNode) result.getResult().orElseThrow();
        var itemsNode = resultNode.get("items");
        assertThat(itemsNode).isNotNull();
        assertThat(itemsNode.isArray()).isTrue();
        assertThat(itemsNode.size()).isEqualTo(3);
        assertThat(itemsNode.get(0).asText()).isEqualTo("item1");
        assertThat(itemsNode.get(1).asText()).isEqualTo("item2");
        assertThat(itemsNode.get(2).asText()).isEqualTo("item3");
    }

    @Test
    void testRunAutomation_withMixedDataTypesInResult() {
        var yaml = """
                alias: mixed-result
                triggers:
                  - trigger: alwaysTrue
                actions: []
                result:
                  count: 42
                  active: false
                  username: "david"
                  tags:
                    - "tag1"
                    - "tag2"
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isTrue();
        JsonNode resultNode = (JsonNode) result.getResult().orElseThrow();
        assertThat(resultNode.get("count").asInt()).isEqualTo(42);
        assertThat(resultNode.get("active").asBoolean()).isFalse();
        assertThat(resultNode.get("username").asText()).isEqualTo("david");
        assertThat(resultNode.get("tags")).isNotNull();
        assertThat(resultNode.get("tags").isArray()).isTrue();
        assertThat(resultNode.get("tags").size()).isEqualTo(2);
        assertThat(resultNode.get("tags").get(0).asText()).isEqualTo("tag1");
        assertThat(resultNode.get("tags").get(1).asText()).isEqualTo("tag2");
    }

    @Test
    void testRunAutomation_withEmptyResult() {
        var yaml = """
                alias: empty-result
                triggers:
                  - trigger: alwaysTrue
                actions: []
                result: {}
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isTrue();
        JsonNode resultNode = (JsonNode) result.getResult().orElseThrow();
        assertThat(resultNode).isNotNull();
        assertThat(resultNode.isObject()).isTrue();
        assertThat(resultNode.size()).isZero();
        assertThat(resultNode.isEmpty()).isTrue();
    }

    @Test
    void testRunAutomation_skipped_noResult() {
        var yaml = """
                alias: skipped-automation
                triggers:
                  - trigger: alwaysFalse
                actions:
                  - action: logger
                    message: "You should not see this."
                result:
                  success: true
                  message: "This should not appear."
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);
        assertThat(result.isExecuted()).isFalse();
        assertThat(result.getResult()).isEmpty();
    }

    @Test
    void testRunAutomation_skipped_withMinimalYaml() {
        var yaml = """
                alias: minimal-skipped
                triggers:
                  - trigger: alwaysFalse
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isFalse();
        assertThat(result.getResult()).isEmpty();
    }

    @Test
    void testRunAutomation_withNullResultInYaml() {
        var yaml = """
                alias: null-result
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Still executes"
                result: null
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);
        assertThat(result.isExecuted()).isTrue();
        JsonNode resultNode = (JsonNode) result.getResult().orElseThrow();
        assertThat(resultNode.isNull()).isTrue();
    }

    @Test
    void testRunAutomation_withPrimitiveResult() {
        var yaml = """
                alias: primitive-result
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Primitive result"
                result: 42
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);
        assertThat(result.isExecuted()).isTrue();
        JsonNode resultNode = (JsonNode) result.getResult().orElseThrow();
        assertThat(resultNode).isNotNull();
        assertThat(resultNode.isInt()).isTrue();
        assertThat(resultNode.asInt()).isEqualTo(42);
    }

    @Test
    void testRunAutomation_withEmptyListResult() {
        var yaml = """
                alias: empty-list-result
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Empty list as result"
                result: []
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);
        assertThat(result.isExecuted()).isTrue();
        JsonNode resultNode = (JsonNode) result.getResult().orElseThrow();
        assertThat(resultNode).isNotNull();
        assertThat(resultNode.isArray()).isTrue();
        assertThat(resultNode.size()).isZero();
        assertThat(resultNode.isEmpty()).isTrue();
    }

    @Test
    void testRunAutomation_withEmptyMapResult() {
        var yaml = """
                alias: empty-map-result
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Empty map as result"
                result: {}
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);
        assertThat(result.isExecuted()).isTrue();
        JsonNode resultNode = (JsonNode) result.getResult().orElseThrow();
        assertThat(resultNode).isNotNull();
        assertThat(resultNode.isObject()).isTrue();
        assertThat(resultNode.size()).isZero();
        assertThat(resultNode.isEmpty()).isTrue();
    }

    @Test
    void testRunAutomation_withoutResultField() {
        var yaml = """
                alias: no-result-field
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "No result defined"
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);
        assertThat(result.isExecuted()).isTrue();
        JsonNode resultNode = (JsonNode) result.getResult().orElseThrow();
        assertThat(resultNode.isNull()).isTrue();
    }

    @Test
    void testRunAutomation_returnVariableInResult() {
        var yaml = """
                alias: return-variable-result
                variables:
                  - someValue: 99
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Using someValue"
                result:
                  valueFromVariable: "{{ someValue }}"
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isTrue();
        JsonNode resultNode = (JsonNode) result.getResult().orElseThrow();
        assertThat(resultNode).isNotNull();
        assertThat(resultNode.get("valueFromVariable").asInt()).isEqualTo(99);
    }

    @Test
    void testRunAutomation_returnUpdatedVariable() {
        var yaml = """
                alias: return-updated-variable
                variables:
                  - counter: 0
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: variable
                    counter: "{{ counter + 1 }}"
                result:
                  finalCounter: "{{ counter }}"
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isTrue();
        JsonNode resultNode = (JsonNode) result.getResult().orElseThrow();
        assertThat(resultNode).isNotNull();
        assertThat(resultNode.get("finalCounter").asInt()).isEqualTo(1);
    }

    @Test
    void testRunAutomation_returnMultipleVariables() {
        var yaml = """
                alias: return-multiple-variables
                variables:
                  - firstName: "John"
                    lastName: "Doe"
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Combining names"
                result:
                  fullName: "{{ firstName }} {{ lastName }}"
                  initials: "{{ firstName | slice(0,1) }}{{ lastName | slice(0,1) }}"
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isTrue();
        JsonNode resultNode = (JsonNode) result.getResult().orElseThrow();
        assertThat(resultNode).isNotNull();
        assertThat(resultNode.get("fullName").asText()).isEqualTo("John Doe");
        assertThat(resultNode.get("initials").asText()).isEqualTo("JD");
    }

    @Test
    void testRunAutomation_withNestedUserInfo() {
        var yaml = """
                alias: return-nested-user-info
                variables:
                  - firstName: "David"
                    lastName: "Randoll"
                    email: "david@example.com"
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Collecting user info..."
                result:
                  user:
                    fullName: "{{ firstName }} {{ lastName }}"
                    contact:
                      email: "{{ email }}"
                      initials: "{{ firstName | slice(0,1) }}{{ lastName | slice(0,1) }}"
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isTrue();

        JsonNode resultNode = (JsonNode) result.getResult().orElseThrow();
        assertThat(resultNode.path("user").path("fullName").asText()).isEqualTo("David Randoll");
        assertThat(resultNode.path("user").path("contact").path("email").asText()).isEqualTo("david@example.com");
        assertThat(resultNode.path("user").path("contact").path("initials").asText()).isEqualTo("DR");
    }

    @Test
    void testRunAutomation_withListOfItems() {
        var yaml = """
                alias: return-order-items
                variables:
                  - item1: "Laptop"
                    item2: "Mouse"
                    item3: "Keyboard"
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Preparing item list..."
                result:
                  items:
                    - "{{ item1 }}"
                    - "{{ item2 }}"
                    - "{{ item3 }}"
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isTrue();

        JsonNode resultNode = (JsonNode) result.getResult().orElseThrow();
        assertThat(resultNode.path("items")).hasSize(3);
        assertThat(resultNode.path("items").get(0).asText()).isEqualTo("Laptop");
        assertThat(resultNode.path("items").get(1).asText()).isEqualTo("Mouse");
        assertThat(resultNode.path("items").get(2).asText()).isEqualTo("Keyboard");
    }

    @Test
    void testRunAutomation_withDeepNestedFields() {
        var yaml = """
                alias: return-deep-nested-data
                variables:
                  - country: "USA"
                    state: "California"
                    city: "San Francisco"
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Building address..."
                result:
                  address:
                    country: "{{ country }}"
                    details:
                      state: "{{ state }}"
                      city: "{{ city }}"
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isTrue();

        JsonNode resultNode = (JsonNode) result.getResult().orElseThrow();
        assertThat(resultNode.path("address").path("country").asText()).isEqualTo("USA");
        assertThat(resultNode.path("address").path("details").path("state").asText()).isEqualTo("California");
        assertThat(resultNode.path("address").path("details").path("city").asText()).isEqualTo("San Francisco");
    }

    @Test
    void testRunAutomation_withPriceComputation() {
        var yaml = """
                alias: return-price-computation
                variables:
                  - unitPrice: 50
                    quantity: 3
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Calculating total..."
                result:
                  totalPrice: "{{ unitPrice * quantity }}"
                  details:
                    unit: "{{ unitPrice }}"
                    quantity: "{{ quantity }}"
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isTrue();

        JsonNode resultNode = (JsonNode) result.getResult().orElseThrow();
        assertThat(resultNode.path("totalPrice").asInt()).isEqualTo(150);
        assertThat(resultNode.path("details").path("unit").asInt()).isEqualTo(50);
        assertThat(resultNode.path("details").path("quantity").asInt()).isEqualTo(3);
    }

    @Test
    void testRunAutomation_withDefaultValue() {
        var yaml = """
                alias: return-with-default-value
                variables:
                  - userName: ""
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Checking user name..."
                result:
                  userName: "{{ userName | default('Guest') }}"
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isTrue();

        JsonNode resultNode = (JsonNode) result.getResult().orElseThrow();
        assertThat(resultNode.path("userName").asText()).isEqualTo("Guest");
    }

    @Test
    void testRunAutomation_emptyVariables() {
        var yaml = """
                alias: return-empty-variables
                variables:
                  - 
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Empty variables"
                result:
                  message: "No variables here"
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isTrue();
        JsonNode resultNode = (JsonNode) result.getResult().orElseThrow();
        assertThat(resultNode.path("message").asText()).isEqualTo("No variables here");
    }

    @Test
    void testRunAutomation_missingVariableUsage() {
        var yaml = """
                alias: return-missing-variable
                variables:
                  - firstName: "David"
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Trying to use missing lastName..."
                result:
                  fullName: "{{ firstName }} {{ lastName }}"
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isTrue();
        JsonNode resultNode = (JsonNode) result.getResult().orElseThrow();
        assertThat(resultNode.path("fullName").asText()).isEqualTo("David "); // Missing lastName results in blank
    }

    @Test
    void testRunAutomation_nullVariable() {
        var yaml = """
                alias: return-null-variable
                variables:
                  - firstName: null
                    lastName: "Smith"
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Handling null variable"
                result:
                  fullName: "{{ firstName | default('Unknown') }} {{ lastName }}"
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isTrue();
        JsonNode resultNode = (JsonNode) result.getResult().orElseThrow();
        assertThat(resultNode.path("fullName").asText()).isEqualTo("Unknown Smith");
    }

    @Test
    void testRunAutomation_numericComputationStringInputs() {
        var yaml = """
                alias: numeric-computation-string-input
                variables:
                  - price: "100"
                    tax: "15"
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Numeric operations with string numbers"
                result:
                  totalPrice: "{{ price | int + tax | int }}"
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isTrue();
        JsonNode resultNode = (JsonNode) result.getResult().orElseThrow();
        assertThat(resultNode.path("totalPrice").asInt()).isEqualTo(115);
    }

    @Test
    void testRunAutomation_specialCharactersInVariables() {
        var yaml = """
                alias: return-special-characters
                variables:
                  - companyName: "ACME & Sons, Inc."
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Special chars in variables"
                result:
                  company: "{{ companyName }}"
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isTrue();
        JsonNode resultNode = (JsonNode) result.getResult().orElseThrow();
        assertThat(resultNode.path("company").asText()).isEqualTo("ACME & Sons, Inc.");
    }

    @Test
    void testRunAutomation_reservedKeywordVariable() {
        var yaml = """
                alias: reserved-keyword-variable
                variables:
                  - default: "some value"
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: logger
                    message: "Testing reserved keyword"
                result:
                  output: "{{ default }}"
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var eventContext = new EventContext(new TimeBasedEvent(LocalTime.now()));
        AutomationResult result = engine.runAutomation(automation, eventContext);

        assertThat(result.isExecuted()).isTrue();
        JsonNode resultNode = (JsonNode) result.getResult().orElseThrow();
        assertThat(resultNode.path("output").asText()).isEqualTo("some value");
    }


}