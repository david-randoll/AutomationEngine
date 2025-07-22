package com.davidrandoll.automation.engine.spring.web.modules.conditions.http_query_param;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.web.AutomationEngineTest;
import com.davidrandoll.automation.engine.spring.web.events.AEHttpRequestEvent;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;


class HttpQueryParamConditionTest extends AutomationEngineTest {


    @Test
    void testShouldMatchWhenQueryParamEquals() {
        var yaml = """
                alias: query-param-equals
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpQueryParam
                    queryParams:
                      version:
                        equals: "v1"
                actions:
                  - action: logger
                    message: query param equals matched
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("version", "v1");
        var event = AEHttpRequestEvent.builder()
                .queryParams(queryParams)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchWhenNotEqualsAndExistsTrue() {
        var yaml = """
                alias: query-param-notEquals-exists
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpQueryParam
                    queryParams:
                      env:
                        notEquals: "test"
                      debug:
                        exists: true
                actions:
                  - action: logger
                    message: not test + debug exists
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("env", "prod");
        queryParams.add("debug", "true");
        var event = AEHttpRequestEvent.builder()
                .queryParams(queryParams)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldNotMatchWhenKeyFailsNotInCheck() {
        var yaml = """
                alias: query-param-notIn-fail
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpQueryParam
                    queryParams:
                      mode:
                        notIn: ["safe", "readonly"]
                actions:
                  - action: logger
                    message: failed notIn
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("mode", "safe");
        var event = AEHttpRequestEvent.builder()
                .queryParams(queryParams)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testShouldMatchMultipleQueryParams() {
        var yaml = """
                alias: query-param-multiple
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpQueryParam
                    queryParams:
                      token:
                        regex: "^abc-[0-9]+$"
                      version:
                        in: ["v1", "v2"]
                      user:
                        equals: "admin"
                actions:
                  - action: logger
                    message: multiple params matched
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("token", "abc-12345");
        queryParams.add("version", "v1");
        queryParams.add("user", "admin");
        var event = AEHttpRequestEvent.builder()
                .queryParams(queryParams)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldNotMatchWhenExistsFalseButParamPresent() {
        var yaml = """
                alias: query-param-exists-false-fail
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpQueryParam
                    queryParams:
                      testMode:
                        exists: false
                actions:
                  - action: logger
                    message: should not match
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("testMode", "true");
        var event = AEHttpRequestEvent.builder()
                .queryParams(queryParams)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testShouldMatchWhenParamMissingAndExistsFalse() {
        var yaml = """
                alias: query-param-missing-exists-false
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpQueryParam
                    queryParams:
                      debug:
                        exists: false
                actions:
                  - action: logger
                    message: param missing as expected
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        var event = AEHttpRequestEvent.builder()
                .queryParams(queryParams) // No debug param
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldHandleCaseSensitivityAndEmptyValues() {
        var yaml = """
                alias: query-param-case-empty
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpQueryParam
                    queryParams:
                      emptyKey:
                        equals: ""
                      CASE_KEY:
                        equals: "VaLuE"
                actions:
                  - action: logger
                    message: case and empty handled
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("emptyKey", "");
        queryParams.add("CASE_KEY", "VaLuE");
        var event = AEHttpRequestEvent.builder()
                .queryParams(queryParams)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchWhenOneOfMultipleQueryParamValuesMatches() {
        var yaml = """
                alias: multi-value-query-match
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpQueryParam
                    queryParams:
                      category:
                        equals: "books"
                actions:
                  - action: logger
                    message: matched one of multiple values
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("category", "movies");
        queryParams.add("category", "books");
        queryParams.add("category", "games");
        var event = AEHttpRequestEvent.builder()
                .queryParams(queryParams)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldNotMatchWhenQueryParamValueIsEmptyString() {
        var yaml = """
                alias: query-empty-value-fail
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpQueryParam
                    queryParams:
                      status:
                        equals: "active"
                actions:
                  - action: logger
                    message: should not match empty value
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("status", "");
        var event = AEHttpRequestEvent.builder()
                .queryParams(queryParams)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isFalse();
    }

    @Test
    void testShouldMatchRegexWithSpecialCharacters() {
        var yaml = """
                alias: query-regex-special-chars
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpQueryParam
                    queryParams:
                      code:
                        regex: "^\\\\d{3}-[A-Z]{2}\\\\*?$"
                actions:
                  - action: logger
                    message: matched regex with special chars
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("code", "123-AB*");
        var event = AEHttpRequestEvent.builder()
                .queryParams(queryParams)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchUnexpectedQueryParamKey() {
        var yaml = """
                alias: query-unexpected-key
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpQueryParam
                    queryParams:
                      expected:
                        equals: "yes"
                actions:
                  - action: logger
                    message: should not match unexpected key
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("expected", "yes");
        var event = AEHttpRequestEvent.builder()
                .queryParams(queryParams)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchSpecialCharacterKeys() {
        var yaml = """
                alias: query-param-special-keys
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpQueryParam
                    queryParams:
                      api_key:
                        equals: "123"
                      user-id:
                        equals: "abc"
                actions:
                  - action: logger
                    message: matched special keys
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("api_key", "123");
        queryParams.add("user-id", "abc");
        var event = AEHttpRequestEvent.builder()
                .queryParams(queryParams)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }

    @Test
    void testShouldMatchMixedCaseQueryParam() {
        var yaml = """
                alias: query-param-case-sensitivity
                triggers:
                  - trigger: alwaysTrue
                conditions:
                  - condition: httpQueryParam
                    queryParams:
                      Mode:
                        equals: "Debug"
                actions:
                  - action: logger
                    message: matched mixed case
                """;

        var automation = factory.createAutomation("yaml", yaml);
        engine.register(automation);

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("Mode", "Debug");
        var event = AEHttpRequestEvent.builder()
                .queryParams(queryParams)
                .build();

        var context = EventContext.of(event);
        engine.publishEvent(context);

        assertThat(automation.allConditionsMet(context)).isTrue();
    }


}