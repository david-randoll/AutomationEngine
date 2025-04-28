package com.automation.engine.http.modules.actions.send_http_request;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.AutomationEngineTest;
import com.automation.engine.http.utils.JsonNodeMatcher;
import com.automation.engine.modules.events.time_based.TimeBasedEvent;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class SendHttpRequestActionTest extends AutomationEngineTest {

    @Test
    void testSendHttpRequest_jsonBody_storeToVariable() {
        var yaml = """
                alias: send-json-store
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/echo
                    method: POST
                    contentType: application/json
                    body:
                      hello: world
                    storeToVariable: response
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

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
                    url: http://localhost:%s/sendHttpRequest/echo
                    method: POST
                    contentType: application/x-www-form-urlencoded
                    body:
                      name: Alice
                      age: 30
                    storeToVariable: formResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

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
                    url: http://localhost:%s/sendHttpRequest/echo
                    method: POST
                    contentType: multipart/form-data
                    body:
                      items:
                        - apple
                        - banana
                      note: test
                    storeToVariable: multiResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

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
                    url: http://localhost:%s/sendHttpRequest/echo
                    method: POST
                    contentType: application/json
                    body:
                      key: value
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

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
                    url: http://localhost:%s/sendHttpRequest/not-found
                    method: GET
                    storeToVariable: errorResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));

        engine.publishEvent(event);

        JsonNode errorResponse = (JsonNode) event.getMetadata("errorResponse");
        assertThat(errorResponse.asText()).isEqualTo("Not found");
    }

    @Test
    void testSendHttpRequest_serverError() {
        var yaml = """
                alias: send-500
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/error
                    method: GET
                    storeToVariable: errorVar
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));

        engine.publishEvent(event);

        var errorResponse = (JsonNode) event.getMetadata("errorVar");
        assertThat(errorResponse.asText()).isEqualTo("Internal error");
    }

    @Test
    void testSendHttpRequest_basicGet() {
        var yaml = """
                alias: basic-get
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/basic
                    method: GET
                    storeToVariable: responseVar
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("responseVar");
        assertThat(response.get("message").asText()).isEqualTo("Basic GET success");
    }

    @Test
    void testSendHttpRequest_getWithQueryParams() {
        var yaml = """
                alias: get-with-query
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/query?foo=bar&num=123
                    method: GET
                    storeToVariable: responseVar
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("responseVar");
        assertThat(response.get("query").get("foo").asText()).isEqualTo("bar");
        assertThat(response.get("query").get("num").asText()).isEqualTo("123");
    }

    @Test
    void testSendHttpRequest_getWithPathVariable() {
        var yaml = """
                alias: get-with-path
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/path/42
                    method: GET
                    storeToVariable: responseVar
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("responseVar");
        assertThat(response.get("id").asInt()).isEqualTo(42);
    }

    @Test
    void testSendHttpRequest_getWithHeaders() {
        var yaml = """
                alias: get-with-headers
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/headers
                    method: GET
                    headers:
                      X-Custom-Header: my-header-value
                      Authorization: Bearer dummy-token
                    storeToVariable: responseVar
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("responseVar");
        var headers = response.get("headers");
        var customHeader = JsonNodeMatcher.getPathWithWildcards(headers, "X-Custom-Header");
        var authorization = JsonNodeMatcher.getPathWithWildcards(headers, "Authorization");
        // check if the headers are present by case-insensitive key
        assertThat(customHeader.asText()).isEqualTo("my-header-value");
        assertThat(authorization.asText()).isEqualTo("Bearer dummy-token");
    }

    @Test
    void testSendHttpRequest_getWithRequestBody() {
        var yaml = """
                alias: get-with-body
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/body
                    method: GET
                    contentType: application/json
                    body:
                      key: value
                    storeToVariable: responseVar
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("responseVar");
        assertThat(response.get("body").get("key").asText()).isEqualTo("value");
    }

}