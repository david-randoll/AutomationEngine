package com.automation.engine.http.modules.actions;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.AutomationEngineTest;
import com.automation.engine.modules.events.time_based.TimeBasedEvent;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SendHttpRequestActionTest extends AutomationEngineTest {

    @Test
    void testSendHttpRequest_jsonBody_storeToVariable() {
        var yaml = """
                alias: send-json-store
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:8080/echo
                    method: POST
                    contentType: application/json
                    body:
                      hello: world
                    storeToVariable: response
                """;

        var automation = factory.createAutomation("yaml", yaml);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        JsonNode response = (JsonNode) event.getMetadata("response");
        assertThat(response.at("/hello").asText()).isEqualTo("world");
    }

    @Test
    void testSendHttpRequest_formUrlEncoded() {
        var yaml = """
                alias: send-form
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:8080/echo
                    method: POST
                    contentType: application/x-www-form-urlencoded
                    body:
                      name: Alice
                      age: 30
                    storeToVariable: formResponse
                """;

        var automation = factory.createAutomation("yaml", yaml);
        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        JsonNode response = (JsonNode) event.getMetadata("formResponse");
        assertThat(response.at("/name").asText()).isEqualTo("Alice");
        assertThat(response.at("/age").asText()).isEqualTo("30");
    }

    @Test
    void testSendHttpRequest_multipartListValues() {
        var yaml = """
                alias: send-multipart
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:8080/echo
                    method: POST
                    contentType: multipart/form-data
                    body:
                      items:
                        - apple
                        - banana
                      note: test
                    storeToVariable: multiResponse
                """;

        var automation = factory.createAutomation("yaml", yaml);
        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        JsonNode response = (JsonNode) event.getMetadata("multiResponse");
        assertThat(response.at("/items/0").asText()).isEqualTo("apple");
        assertThat(response.at("/items/1").asText()).isEqualTo("banana");
        assertThat(response.at("/note").asText()).isEqualTo("test");
    }

    @Test
    void testSendHttpRequest_noStoreVariable() {
        var yaml = """
                alias: send-no-store
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:8080/echo
                    method: POST
                    contentType: application/json
                    body:
                      key: value
                """;

        var automation = factory.createAutomation("yaml", yaml);
        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        assertThat(event.getMetadata().keySet()).doesNotContain("response");
    }

    @Test
    void testSendHttpRequest_clientError() {
        var yaml = """
                alias: send-404
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:8080/not-found
                    method: GET
                    storeToVariable: errorResponse
                """;

        var automation = factory.createAutomation("yaml", yaml);
        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));

        assertThatThrownBy(() -> engine.publishEvent(event))
                .hasMessageContaining("Client error: 404");
    }

    @Test
    void testSendHttpRequest_serverError() {
        var yaml = """
                alias: send-500
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:8080/error
                    method: GET
                    storeToVariable: errorVar
                """;

        var automation = factory.createAutomation("yaml", yaml);
        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));

        assertThatThrownBy(() -> engine.publishEvent(event))
                .hasMessageContaining("Server error: 500");
    }

}