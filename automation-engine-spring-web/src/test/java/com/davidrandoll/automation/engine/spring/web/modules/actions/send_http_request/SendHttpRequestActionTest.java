package com.davidrandoll.automation.engine.spring.web.modules.actions.send_http_request;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.modules.events.time_based.TimeBasedEvent;
import com.davidrandoll.automation.engine.spring.web.utils.JsonNodeMatcher;
import com.davidrandoll.automation.engine.test.AutomationEngineTest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@Import(SendHttpRequestController.class)
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

    @Test
    void testSendHttpRequest_putSimpleJson() {
        var yaml = """
                alias: send-put-simple
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/put/simple
                    method: PUT
                    contentType: application/json
                    body:
                      name: "David"
                      role: "admin"
                    storeToVariable: putSimpleResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("putSimpleResponse");
        assertThat(response.get("message").asText()).isEqualTo("Updated David as admin");
    }

    @Test
    void testSendHttpRequest_putWithPathVariable() {
        var yaml = """
                alias: send-put-path-variable
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/put/user/42
                    method: PUT
                    contentType: application/json
                    body:
                      email: "david@example.com"
                    storeToVariable: putPathVariableResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("putPathVariableResponse");
        assertThat(response.get("userId").asInt()).isEqualTo(42);
    }

    @Test
    void testSendHttpRequest_putWithQueryParams() {
        var yaml = """
                alias: send-put-query-params
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/put/query?active=true
                    method: PUT
                    contentType: application/json
                    body:
                      username: "david"
                    storeToVariable: putQueryResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("putQueryResponse");
        assertThat(response.get("active").asBoolean()).isTrue();
    }

    @Test
    void testSendHttpRequest_putWithHeaders() {
        var yaml = """
                alias: send-put-with-headers
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/put/headers
                    method: PUT
                    contentType: application/json
                    headers:
                      X-Update-Mode: "force"
                    body:
                      username: "david"
                    storeToVariable: putHeaderResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("putHeaderResponse");
        assertThat(response.get("mode").asText()).isEqualTo("force");
    }

    @Test
    void testSendHttpRequest_putEmptyBody() {
        var yaml = """
                alias: send-put-empty-body
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/put/emptyBody
                    method: PUT
                    contentType: application/json
                    body: {}
                    storeToVariable: putEmptyBodyResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("putEmptyBodyResponse");
        assertThat(response.get("status").asText()).isEqualTo("Empty body received");
    }

    @Test
    void testSendHttpRequest_putInvalidJson() {
        var yaml = """
                alias: send-put-invalid-json
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/put/invalidJson
                    method: PUT
                    contentType: application/json
                    rawBody: "{ broken: json "
                    storeToVariable: putInvalidJsonResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("putInvalidJsonResponse");
        assertThat(response.get("error").asText()).isEqualTo("Invalid JSON");
    }

    @Test
    void testSendHttpRequest_putLargePayload() {
        var yaml = """
                alias: send-put-large-payload
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/put/large
                    method: PUT
                    contentType: application/json
                    body:
                      bigText: "%s"
                    storeToVariable: putLargeResponse
                """.formatted(port, "X".repeat(30_000)); // 30KB payload

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("putLargeResponse");
        assertThat(response.get("length").asInt()).isEqualTo(30_000);
    }

    @Test
    void testSendHttpRequest_putMultipartForm() {
        var yaml = """
                alias: send-put-multipart
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/put/multipart
                    method: PUT
                    contentType: multipart/form-data
                    body:
                      description: "Test upload"
                    storeToVariable: putMultipartResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("putMultipartResponse");
        assertThat(response.get("description").asText()).isEqualTo("Test upload");
    }

    @Test
    void testSendHttpRequest_putNoContentType() {
        var yaml = """
                alias: send-put-no-content-type
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/put/noContentType
                    method: PUT
                    body:
                      info: "noContentType"
                    storeToVariable: putNoContentTypeResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("putNoContentTypeResponse");
        assertThat(response.get("result").asText()).isEqualTo("Handled without content-type");
    }

    @Test
    void testPut_missingPathVariable() {
        var yaml = """
                alias: put-missing-path-variable
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/putWithoutId
                    method: PUT
                    storeToVariable: missingPathVar
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("missingPathVar");
        assertThat(response.asText()).contains("Missing id");
    }

    @Test
    void testPut_extraQueryParams() {
        var yaml = """
                alias: put-extra-query
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/putWithQuery?id=123&extra=unexpected
                    method: PUT
                    storeToVariable: extraQuery
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("extraQuery");
        assertThat(response.get("id").asText()).isEqualTo("123");
    }

    @Test
    void testPut_nonExistingEndpoint() {
        var yaml = """
                alias: put-non-existing
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/notFoundEndpoint
                    method: PUT
                    storeToVariable: nonExisting
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("nonExisting");
        var errorResponse = response.get("error");
        assertThat(errorResponse.asText()).contains("Not Found");
    }

    @Test
    void testPut_wrongMethod() {
        var yaml = """
                alias: put-wrong-method
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/putWithQuery?id=1
                    method: GET
                    storeToVariable: wrongMethod
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("wrongMethod");
        var errorResponse = response.get("error");
        assertThat(errorResponse.asText()).contains("Method Not Allowed");
    }

    @Test
    void testPut_largeBody() {
        var largeBody = "A".repeat(5_000_000); // 5MB

        var yaml = """
                alias: put-large-body
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/putLargeBody
                    method: PUT
                    contentType: application/json
                    body:
                      largeField: "%s"
                    storeToVariable: largeBody
                """.formatted(port, largeBody);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("largeBody");
        assertThat(response.get("status").asText()).isEqualTo("received large body");
    }

    @Test
    void testPut_timeout() {
        var yaml = """
                alias: put-timeout
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/putTimeout
                    method: PUT
                    storeToVariable: timeoutResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("timeoutResponse");
        assertThat(response.asText()).contains("Request timeout");
    }

    @Test
    void testPut_emptyPath() {
        var yaml = """
                alias: put-empty-path
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/
                    method: PUT
                    storeToVariable: emptyPath
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("emptyPath");
        var errorResponse = response.get("error");
        assertThat(errorResponse.asText()).contains("Not Found");
    }

    @Test
    void testPut_noBody() {
        var yaml = """
                alias: put-no-body
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/putNoBody
                    method: PUT
                    storeToVariable: noBodyResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("noBodyResponse");
        assertThat(response.asText()).isEqualTo("No body provided");
    }

    @Test
    void testPut_overwriteBehavior() {
        var yaml = """
                alias: put-overwrite
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/putOverwrite?id=123
                    method: PUT
                    body:
                      field: "initial"
                    storeToVariable: firstPut
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/putOverwrite?id=123
                    method: PUT
                    body:
                      field: "updated"
                    storeToVariable: secondPut
                """.formatted(port, port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var updatedResponse = (JsonNode) event.getMetadata("secondPut");
        assertThat(updatedResponse.get("field").asText()).isEqualTo("updated");
    }

    @Test
    void testPut_nonJsonContentType() {
        var yaml = """
                alias: put-non-json
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/putPlainText
                    method: PUT
                    contentType: text/plain
                    body: "plain text body"
                    storeToVariable: plainTextResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("plainTextResponse");
        assertThat(response.asText()).contains("plain text body");
    }

    @Test
    void testPut_unicodeInBody() {
        var yaml = """
                alias: put-unicode
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/putUnicode
                    method: PUT
                    body:
                      message: "Hello 🌎🚀"
                    storeToVariable: unicodeResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("unicodeResponse");
        assertThat(response.get("message").asText()).isEqualTo("Hello 🌎🚀");
    }

    @Test
    void testPut_deeplyNestedJson() {
        var yaml = """
                alias: put-nested-json
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/putNestedJson
                    method: PUT
                    body:
                      level1:
                        level2:
                          level3:
                            level4:
                              level5: "deep value"
                    storeToVariable: nestedJsonResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("nestedJsonResponse");
        assertThat(response.at("/level1/level2/level3/level4/level5").asText()).isEqualTo("deep value");
    }

    @Test
    void testPatch_missingPathVariable() {
        var yaml = """
                alias: patch-missing-path-variable
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/patchWithoutId
                    method: PATCH
                    storeToVariable: missingPathVar
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("missingPathVar");
        assertThat(response.asText()).contains("Missing id");
    }

    @Test
    void testPatch_extraQueryParams() {
        var yaml = """
                alias: patch-extra-query
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/patchWithQuery?id=123&extra=unexpected
                    method: PATCH
                    storeToVariable: extraQuery
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("extraQuery");
        assertThat(response.get("id").asText()).isEqualTo("123");
    }

    @Test
    void testPatch_nonExistingEndpoint() {
        var yaml = """
                alias: patch-non-existing
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/notFoundEndpoint
                    method: PATCH
                    storeToVariable: nonExisting
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("nonExisting");
        var message = response.get("message");
        assertThat(message.asText()).contains("No static resource sendHttpRequest/notFoundEndpoint.");
    }

    @Test
    void testPatch_wrongMethod() {
        var yaml = """
                alias: patch-wrong-method
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/patchWithQuery?id=1
                    method: POST
                    storeToVariable: wrongMethod
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("wrongMethod");
        var errorResponse = response.get("error");
        assertThat(errorResponse.asText()).contains("Method Not Allowed");
    }

    @Test
    void testPatch_largeBody() {
        var largeBody = "B".repeat(5_000_000); // 5MB

        var yaml = """
                alias: patch-large-body
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/patchLargeBody
                    method: PATCH
                    contentType: application/json
                    body:
                      largeField: "%s"
                    storeToVariable: largeBody
                """.formatted(port, largeBody);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("largeBody");
        assertThat(response.get("status").asText()).isEqualTo("received large body");
    }

    @Test
    void testPatch_timeout() {
        var yaml = """
                alias: patch-timeout
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/patchTimeout
                    method: PATCH
                    storeToVariable: timeoutResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("timeoutResponse");
        assertThat(response.asText()).contains("Request timeout");
    }

    @Test
    void testPatch_emptyPath() {
        var yaml = """
                alias: patch-empty-path
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/patch/emptyPath
                    method: PATCH
                    storeToVariable: emptyPathResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("emptyPathResponse");
        var errorResponse = response.get("error");
        assertThat(errorResponse.asText()).contains("Not Found");
    }

    @Test
    void testPatch_noBodyNoContentLength() {
        var yaml = """
                alias: patch-no-body
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/patchNoBody
                    method: PATCH
                    storeToVariable: noBodyResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("noBodyResponse");
        assertThat(response.get("status").asText()).isEqualTo("no body received");
    }

    @Test
    void testPatch_partialUpdate() {
        var yaml = """
                alias: patch-partial-update
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/patchPartial
                    method: PATCH
                    contentType: application/json
                    body:
                      name: "updated-name"
                    storeToVariable: partialUpdateResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("partialUpdateResponse");
        assertThat(response.get("name").asText()).isEqualTo("updated-name");
        assertThat(response.get("otherField").asText()).isEqualTo("default");
    }

    @Test
    void testPatch_nonJsonContentType() {
        var yaml = """
                alias: patch-non-json
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/patchText
                    method: PATCH
                    contentType: text/plain
                    body: "plain text patch"
                    storeToVariable: nonJsonResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("nonJsonResponse");
        assertThat(response.asText()).isEqualTo("Received plain text patch");
    }

    @Test
    void testPatch_unicodeEmojiInBody() {
        var yaml = """
                alias: patch-unicode-emoji
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/patchUnicode
                    method: PATCH
                    contentType: application/json
                    body:
                      message: "Hello 👋🌍"
                    storeToVariable: unicodeResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("unicodeResponse");
        assertThat(response.get("echo").asText()).isEqualTo("Hello 👋🌍");
    }

    @Test
    void testPatch_deepNestedJson() {
        var yaml = """
                alias: patch-deep-nested
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/patchDeepJson
                    method: PATCH
                    contentType: application/json
                    body:
                      level1:
                        level2:
                          level3:
                            level4:
                              key: "value"
                    storeToVariable: deepNestedResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var response = (JsonNode) event.getMetadata("deepNestedResponse");
        assertThat(response.at("/level1/level2/level3/level4/key").asText()).isEqualTo("value");
    }

    @Test
    void testDeleteNoPath() {
        var yaml = """
                alias: delete-no-path
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/
                    method: DELETE
                    storeToVariable: deleteResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var deleteResponse = (JsonNode) event.getMetadata("deleteResponse");
        var message = deleteResponse.get("message");
        assertThat(message.asText()).isEqualTo("No static resource sendHttpRequest.");
    }

    @Test
    void testDeleteWithPathVariables() {
        var yaml = """
                alias: delete-path-variable
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/delete/12345
                    method: DELETE
                    storeToVariable: deleteResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var deleteResponse = (JsonNode) event.getMetadata("deleteResponse");
        assertThat(deleteResponse.asText()).isEqualTo("Item 12345 deleted");
    }

    @Test
    void testDeleteNoBody() {
        var yaml = """
                alias: delete-no-body
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/deleteNoBody
                    method: DELETE
                    storeToVariable: deleteResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var deleteResponse = (JsonNode) event.getMetadata("deleteResponse");
        assertThat(deleteResponse.asText()).isEqualTo("Not Found");
    }

    @Test
    void testDeleteWithQueryParams() {
        var yaml = """
                alias: delete-with-query-params
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/deleteWithQuery?id=12345
                    method: DELETE
                    storeToVariable: deleteResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var deleteResponse = (JsonNode) event.getMetadata("deleteResponse");
        assertThat(deleteResponse.asText()).isEqualTo("Query param id 12345 deleted");
    }

    @Test
    void testDeleteWithHeaders() {
        var yaml = """
                alias: delete-with-headers
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/deleteWithHeaders
                    method: DELETE
                    headers:
                      X-Custom-Header: "some-header-value"
                    storeToVariable: deleteResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var deleteResponse = (JsonNode) event.getMetadata("deleteResponse");
        assertThat(deleteResponse.asText()).isEqualTo("Headers received");
    }

    @Test
    void testDeleteInvalidPath() {
        var yaml = """
                alias: delete-invalid-path
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/deleteInvalid
                    method: DELETE
                    storeToVariable: deleteResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var deleteResponse = (JsonNode) event.getMetadata("deleteResponse");
        assertThat(deleteResponse.asText()).isEqualTo("Not Found");
    }

    @Test
    void testDeleteWithBody() {
        var yaml = """
                alias: delete-with-body
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/deleteWithBody
                    method: DELETE
                    body:
                      id: 12345
                    storeToVariable: deleteResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var deleteResponse = (JsonNode) event.getMetadata("deleteResponse");
        assertThat(deleteResponse.asText()).isEqualTo("Item 12345 deleted with body");
    }

    @Test
    void testDeleteInvalidPathVariable() {
        var yaml = """
                alias: delete-invalid-path-variable
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/deleteWithInvalidPathVariable/invalid-id
                    method: DELETE
                    storeToVariable: deleteResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var deleteResponse = (JsonNode) event.getMetadata("deleteResponse");
        assertThat(deleteResponse.asText()).isEqualTo("Item invalid-id not found");
    }

    @Test
    void testDeleteWithInvalidQueryParam() {
        var yaml = """
                alias: delete-with-invalid-query-param
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/deleteWithInvalidQueryParam?param=invalid
                    method: DELETE
                    storeToVariable: deleteResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var deleteResponse = (JsonNode) event.getMetadata("deleteResponse");
        assertThat(deleteResponse.asText()).isEqualTo("Invalid query parameter");
    }

    @Test
    void testDeleteWithInvalidHeaders() {
        var yaml = """
                alias: delete-with-invalid-headers
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/deleteWithInvalidHeaders
                    method: DELETE
                    headers:
                      X-Invalid-Header: "invalid-value"
                    storeToVariable: deleteResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var deleteResponse = (JsonNode) event.getMetadata("deleteResponse");
        assertThat(deleteResponse.asText()).isEqualTo("Invalid Header");
    }


    @Test
    void testDeleteMalformedJsonBody() {
        var yaml = """
                alias: delete-malformed-json
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/deleteMalformedJson
                    method: DELETE
                    body:
                      { "id": "abc" }  # Malformed JSON
                    storeToVariable: deleteResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var deleteResponse = (JsonNode) event.getMetadata("deleteResponse");
        assertThat(deleteResponse.asText()).isEqualTo("Malformed JSON");
    }

    @Test
    void testDeleteWithExtraQueryParams() {
        var yaml = """
                alias: delete-with-extra-query
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/deleteWithExtraQuery?extra=param&param=12345
                    method: DELETE
                    storeToVariable: deleteResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var deleteResponse = (JsonNode) event.getMetadata("deleteResponse");
        assertThat(deleteResponse.asText()).isEqualTo("Item 12345 deleted with extra params");
    }

    @Test
    void testDeleteEmptyBody() {
        var yaml = """
                alias: delete-empty-body
                triggers:
                  - trigger: alwaysTrue
                actions:
                  - action: sendHttpRequest
                    url: http://localhost:%s/sendHttpRequest/deleteEmptyBody
                    method: DELETE
                    body: {}
                    storeToVariable: deleteResponse
                """.formatted(port);

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        var event = new EventContext(new TimeBasedEvent(LocalTime.now()));
        engine.publishEvent(event);

        var deleteResponse = (JsonNode) event.getMetadata("deleteResponse");
        assertThat(deleteResponse.asText()).isEqualTo("Empty body received");
    }


}