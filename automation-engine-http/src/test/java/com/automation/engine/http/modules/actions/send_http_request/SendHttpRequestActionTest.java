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

    @Test
    void testEmptyQueryParams() {
        var yaml = """
                alias: get-with-empty-query-params
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/emptyQuery?key=&anotherKey=
                    method: GET
                    storeToVariable: emptyQueryVar
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var emptyQueryResponse = (JsonNode) event.getMetadata("emptyQueryVar");
        assertThat(emptyQueryResponse.get("key").asText()).isEmpty();
        assertThat(emptyQueryResponse.get("anotherKey").asText()).isEmpty();
    }

    @Test
    void testSpecialCharacters() {
        var yaml = """
                alias: get-with-special-characters
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/special/üñîçødê?q=hello+world%%20test
                    method: GET
                    storeToVariable: specialCharVar
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var specialCharResponse = (JsonNode) event.getMetadata("specialCharVar");
        assertThat(specialCharResponse.get("path").asText()).isEqualTo("üñîçødê");
        assertThat(specialCharResponse.get("query").asText()).isEqualTo("hello world%20test");
    }

    @Test
    void testMissingPathVariable() {
        var yaml = """
                alias: get-missing-path-variable
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/path/
                    method: GET
                    storeToVariable: missingPathVar
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var missingPathResponse = (JsonNode) event.getMetadata("missingPathVar");
        assertThat(missingPathResponse.get("status").asInt()).isEqualTo(404);
    }

    @Test
    void testUnexpectedHeader() {
        var yaml = """
                alias: get-with-random-header
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/headers
                    method: GET
                    headers:
                      X-Random-Header: 12345
                    storeToVariable: randomHeaderVar
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var headerResponse = (JsonNode) event.getMetadata("randomHeaderVar");
        var headers = headerResponse.get("headers");
        assertThat(headers.get("x-random-header").asText()).isEqualTo("12345");
    }

    @Test
    void testLargeQueryString() {
        String bigQ = "a".repeat(3000);
        var yaml = """
                alias: get-large-query-string
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/largeQuery?q=%s
                    method: GET
                    storeToVariable: largeQueryVar
                """.formatted(port, bigQ);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var largeQueryResponse = (JsonNode) event.getMetadata("largeQueryVar");
        assertThat(largeQueryResponse.get("length").asInt()).isEqualTo(3000);
    }

    @Test
    void testContentTypeHeaderNoBody() {
        var yaml = """
                alias: get-content-type-no-body
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/contentTypeOnly
                    method: GET
                    contentType: application/json
                    storeToVariable: contentTypeVar
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var contentTypeResponse = (JsonNode) event.getMetadata("contentTypeVar");
        assertThat(contentTypeResponse.get("contentType").asText()).isEqualTo("application/json");
    }

    @Test
    void testSendHttpRequest_postJsonBody() {
        var yaml = """
            alias: send-post-json
            triggers:
              - trigger: alwaysTrue
            actions:
              - action: sendHttpRequest
                url: http://localhost:%s/sendHttpRequest/post/json
                method: POST
                contentType: application/json
                body:
                  name: David
                  age: 30
                storeToVariable: jsonResponse
            """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("jsonResponse");
        assertThat(response.get("message").asText()).isEqualTo("Received David aged 30");
    }

    @Test
    void testSendHttpRequest_postFormUrlencoded() {
        var yaml = """
            alias: send-post-form
            triggers:
              - trigger: alwaysTrue
            actions:
              - action: sendHttpRequest
                url: http://localhost:%s/sendHttpRequest/post/form
                method: POST
                contentType: application/x-www-form-urlencoded
                body:
                  username: david
                  password: secret
                storeToVariable: formResponse
            """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("formResponse");
        assertThat(response.get("status").asText()).isEqualTo("Form received");
    }

    @Test
    void testSendHttpRequest_postMultipartForm() {
        var yaml = """
            alias: send-post-multipart
            triggers:
              - trigger: alwaysTrue
            actions:
              - action: sendHttpRequest
                url: http://localhost:%s/sendHttpRequest/post/multipart
                method: POST
                contentType: multipart/form-data
                body:
                  description: "Test file upload"
                  fileContent: "FakeFileContentHere"
                storeToVariable: multipartResponse
            """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("multipartResponse");
        assertThat(response.get("result").asText()).isEqualTo("Multipart received");
    }

    @Test
    void testSendHttpRequest_postMissingBody() {
        var yaml = """
            alias: send-post-missing-body
            triggers:
              - trigger: alwaysTrue
            actions:
              - action: sendHttpRequest
                url: http://localhost:%s/sendHttpRequest/post/missing
                method: POST
                contentType: application/json
                storeToVariable: missingBodyResponse
            """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("missingBodyResponse");
        assertThat(response.get("error").asText()).isEqualTo("Missing body");
    }

    @Test
    void testSendHttpRequest_postCustomHeaders() {
        var yaml = """
            alias: send-post-headers
            triggers:
              - trigger: alwaysTrue
            actions:
              - action: sendHttpRequest
                url: http://localhost:%s/sendHttpRequest/post/headers
                method: POST
                headers:
                  X-Custom-Header: "my-header-value"
                  Authorization: "Bearer token123"
                body:
                  dummy: test
                storeToVariable: headerResponse
            """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("headerResponse");
        assertThat(response.get("customHeader").asText()).isEqualTo("my-header-value");
        assertThat(response.get("authorization").asText()).isEqualTo("Bearer token123");
    }

    @Test
    void testSendHttpRequest_postLargeBody() {
        var yaml = """
            alias: send-post-large
            triggers:
              - trigger: alwaysTrue
            actions:
              - action: sendHttpRequest
                url: http://localhost:%s/sendHttpRequest/post/large
                method: POST
                contentType: application/json
                body:
                  largeText: "%s"
                storeToVariable: largeResponse
            """.formatted(port, "A".repeat(10_000)); // 10KB of 'A'

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("largeResponse");
        assertThat(response.get("receivedLength").asInt()).isEqualTo(10_000);
    }

    @Test
    void testSendHttpRequest_postWrongContentType() {
        var yaml = """
            alias: send-post-wrong-content-type
            triggers:
              - trigger: alwaysTrue
            actions:
              - action: sendHttpRequest
                url: http://localhost:%s/sendHttpRequest/post/wrongContentType
                method: POST
                contentType: text/plain
                body: "This should fail"
                storeToVariable: wrongContentTypeResponse
            """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("wrongContentTypeResponse");
        assertThat(response.get("error").asText()).isEqualTo("Unsupported content type");
    }

    @Test
    void testSendHttpRequest_postEmptyJson() {
        var yaml = """
            alias: send-post-empty-json
            triggers:
              - trigger: alwaysTrue
            actions:
              - action: sendHttpRequest
                url: http://localhost:%s/sendHttpRequest/post/emptyJson
                method: POST
                contentType: application/json
                body: {}
                storeToVariable: emptyJsonResponse
            """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("emptyJsonResponse");
        assertThat(response.get("status").asText()).isEqualTo("Empty JSON received");
    }

    @Test
    void testSendHttpRequest_postInvalidJson() {
        var yaml = """
            alias: send-post-invalid-json
            triggers:
              - trigger: alwaysTrue
            actions:
              - action: sendHttpRequest
                url: http://localhost:%s/sendHttpRequest/post/invalidJson
                method: POST
                contentType: application/json
                rawBody: "{ invalid: json "
                storeToVariable: invalidJsonResponse
            """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("invalidJsonResponse");
        assertThat(response.get("error").asText()).isEqualTo("Invalid JSON");
    }

    @Test
    void testSendHttpRequest_postExtraFields() {
        var yaml = """
            alias: send-post-extra-fields
            triggers:
              - trigger: alwaysTrue
            actions:
              - action: sendHttpRequest
                url: http://localhost:%s/sendHttpRequest/post/extraFields
                method: POST
                contentType: application/json
                body:
                  name: "John"
                  age: 25
                  extra: "thisShouldBeIgnored"
                storeToVariable: extraFieldsResponse
            """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("extraFieldsResponse");
        assertThat(response.get("message").asText()).isEqualTo("Received John aged 25");
    }

    @Test
    void testSendHttpRequest_postLargeMultipart() {
        var yaml = """
            alias: send-post-large-multipart
            triggers:
              - trigger: alwaysTrue
            actions:
              - action: sendHttpRequest
                url: http://localhost:%s/sendHttpRequest/post/largeMultipart
                method: POST
                contentType: multipart/form-data
                body:
                  fileContent: "%s"
                storeToVariable: largeMultipartResponse
            """.formatted(port, "B".repeat(20_000)); // 20KB

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("largeMultipartResponse");
        assertThat(response.get("receivedLength").asInt()).isEqualTo(20_000);
    }

    @Test
    void testSendHttpRequest_postNoContentType() {
        var yaml = """
            alias: send-post-no-content-type
            triggers:
              - trigger: alwaysTrue
            actions:
              - action: sendHttpRequest
                url: http://localhost:%s/sendHttpRequest/post/noContentType
                method: POST
                body:
                  someKey: someValue
                storeToVariable: noContentTypeResponse
            """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("noContentTypeResponse");
        assertThat(response.get("status").asText()).isEqualTo("Default content-type handled");
    }

    @Test
    void testSendHttpRequest_postHeadersNoBody() {
        var yaml = """
            alias: send-post-headers-no-body
            triggers:
              - trigger: alwaysTrue
            actions:
              - action: sendHttpRequest
                url: http://localhost:%s/sendHttpRequest/post/headersNoBody
                method: POST
                headers:
                  X-Special-Header: "specialValue"
                storeToVariable: headersNoBodyResponse
            """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("headersNoBodyResponse");
        assertThat(response.get("header").asText()).isEqualTo("specialValue");
    }




}