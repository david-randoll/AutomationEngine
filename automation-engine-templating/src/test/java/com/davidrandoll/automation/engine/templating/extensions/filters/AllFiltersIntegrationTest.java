package com.davidrandoll.automation.engine.templating.extensions.filters;

import com.davidrandoll.automation.engine.templating.AutomationEngineApplication;
import com.davidrandoll.automation.engine.templating.TemplateProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test that verifies all filters work correctly in the Spring context.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AutomationEngineApplication.class)
@ExtendWith(SpringExtension.class)
class AllFiltersIntegrationTest {

    @Autowired
    private TemplateProcessor templateProcessor;

    @Test
    void testJsonFilter() throws Exception {
        String template = "{{ user | json }}";
        Map<String, Object> data = Map.of("user", Map.of("name", "Alice", "age", 30));

        String result = templateProcessor.process(template, data);

        assertThat(result).contains("\"name\":\"Alice\"");
        assertThat(result).contains("\"age\":30");
    }

    @Test
    void testJsonFilterWithPrettyPrint() throws Exception {
        String template = "{{ user | json(pretty=true) }}";
        Map<String, Object> data = Map.of("user", Map.of("name", "Bob"));

        String result = templateProcessor.process(template, data);

        assertThat(result).contains("\"name\"");
        assertThat(result).contains("\n"); // Pretty print includes newlines
    }

    @Test
    void testFromJsonFilter() throws Exception {
        String template = "{% set parsed = jsonStr | fromJson %}{{ parsed.name }}";
        Map<String, Object> data = Map.of("jsonStr", "{\"name\":\"Charlie\",\"age\":25}");

        String result = templateProcessor.process(template, data);

        assertThat(result).isEqualTo("Charlie");
    }

    @Test
    void testDateFormatFilter() throws Exception {
        String template = "{{ timestamp | date_format('yyyy-MM-dd') }}";
        LocalDateTime timestamp = LocalDateTime.of(2023, 12, 25, 14, 30);
        Map<String, Object> data = Map.of("timestamp", timestamp);

        String result = templateProcessor.process(template, data);

        assertThat(result).isEqualTo("2023-12-25");
    }

    @Test
    void testCoalesceFilter() throws Exception {
        String template = "{{ missing | coalesce('default') }}";
        Map<String, Object> data = Map.of();

        String result = templateProcessor.process(template, data);

        assertThat(result).isEqualTo("default");
    }

    @Test
    void testCoalesceFilterWithNonNull() throws Exception {
        String template = "{{ value | coalesce('default') }}";
        Map<String, Object> data = Map.of("value", "actual");

        String result = templateProcessor.process(template, data);

        assertThat(result).isEqualTo("actual");
    }

    @Test
    void testBase64EncodeFilter() throws Exception {
        String template = "{{ text | base64encode }}";
        Map<String, Object> data = Map.of("text", "hello");

        String result = templateProcessor.process(template, data);

        assertThat(result).isEqualTo("aGVsbG8=");
    }

    @Test
    void testBase64DecodeFilter() throws Exception {
        String template = "{{ encoded | base64decode }}";
        Map<String, Object> data = Map.of("encoded", "aGVsbG8=");

        String result = templateProcessor.process(template, data);

        assertThat(result).isEqualTo("hello");
    }

    @Test
    void testUrlEncodeFilter() throws Exception {
        String template = "{{ text | urlEncode }}";
        Map<String, Object> data = Map.of("text", "hello world");

        String result = templateProcessor.process(template, data);

        assertThat(result).isEqualTo("hello+world");
    }

    @Test
    void testUrlDecodeFilter() throws Exception {
        String template = "{{ encoded | urlDecode }}";
        Map<String, Object> data = Map.of("encoded", "hello+world");

        String result = templateProcessor.process(template, data);

        assertThat(result).isEqualTo("hello world");
    }

    @Test
    void testFilterChaining() throws Exception {
        // Test multiple filters in sequence
        String template = "{{ text | base64encode | urlEncode }}";
        Map<String, Object> data = Map.of("text", "hello world");

        String result = templateProcessor.process(template, data);

        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
    }

    @Test
    void testComplexScenario() throws Exception {
        // Realistic scenario: Format a user object with multiple filters
        String template = """
            User: {{ user.name | coalesce('Unknown') }}
            Created: {{ user.createdAt | date_format('MMM dd, yyyy') }}
            """;

        LocalDateTime timestamp = LocalDateTime.of(2023, 12, 25, 10, 30);
        Map<String, Object> user = Map.of(
            "name", "Alice",
            "createdAt", timestamp
        );
        Map<String, Object> data = Map.of("user", user);

        String result = templateProcessor.process(template, data);

        assertThat(result).contains("User: Alice");
        assertThat(result).contains("Created: Dec 25, 2023");
    }
}

