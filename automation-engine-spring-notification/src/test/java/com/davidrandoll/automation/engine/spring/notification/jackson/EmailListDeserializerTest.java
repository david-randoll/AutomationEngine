package com.davidrandoll.automation.engine.spring.notification.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmailListDeserializerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Data
    static class TestContainer {
        @JsonDeserialize(using = EmailListDeserializer.class)
        private List<String> emails;
    }

    @Test
    void testDeserializeArray() throws Exception {
        String json = "{\"emails\": [\"email1@test.com\", \"email2@test.com\", \"email3@test.com\"]}";
        TestContainer result = objectMapper.readValue(json, TestContainer.class);

        assertThat(result.getEmails())
                .hasSize(3)
                .containsExactly("email1@test.com", "email2@test.com", "email3@test.com");
    }

    @Test
    void testDeserializeCommaSeparatedString() throws Exception {
        String json = "{\"emails\": \"email1@test.com,email2@test.com,email3@test.com\"}";
        TestContainer result = objectMapper.readValue(json, TestContainer.class);

        assertThat(result.getEmails())
                .hasSize(3)
                .containsExactly("email1@test.com", "email2@test.com", "email3@test.com");
    }

    @Test
    void testDeserializeSemicolonSeparatedString() throws Exception {
        String json = "{\"emails\": \"email1@test.com;email2@test.com;email3@test.com\"}";
        TestContainer result = objectMapper.readValue(json, TestContainer.class);

        assertThat(result.getEmails())
                .hasSize(3)
                .containsExactly("email1@test.com", "email2@test.com", "email3@test.com");
    }

    @Test
    void testDeserializeMixedSeparators() throws Exception {
        String json = "{\"emails\": \"email1@test.com,email2@test.com;email3@test.com\"}";
        TestContainer result = objectMapper.readValue(json, TestContainer.class);

        assertThat(result.getEmails())
                .hasSize(3)
                .containsExactly("email1@test.com", "email2@test.com", "email3@test.com");
    }

    @Test
    void testDeserializeSingleString() throws Exception {
        String json = "{\"emails\": \"email@test.com\"}";
        TestContainer result = objectMapper.readValue(json, TestContainer.class);

        assertThat(result.getEmails())
                .hasSize(1)
                .containsExactly("email@test.com");
    }

    @Test
    void testDeserializeWithWhitespace() throws Exception {
        String json = "{\"emails\": \" email1@test.com , email2@test.com ; email3@test.com \"}";
        TestContainer result = objectMapper.readValue(json, TestContainer.class);

        assertThat(result.getEmails())
                .hasSize(3)
                .containsExactly("email1@test.com", "email2@test.com", "email3@test.com");
    }

    @Test
    void testDeserializeEmptyString() throws Exception {
        String json = "{\"emails\": \"\"}";
        TestContainer result = objectMapper.readValue(json, TestContainer.class);

        assertThat(result.getEmails()).isEmpty();
    }

    @Test
    void testDeserializeArrayWithWhitespace() throws Exception {
        String json = "{\"emails\": [\" email1@test.com \", \" email2@test.com \"]}";
        TestContainer result = objectMapper.readValue(json, TestContainer.class);

        assertThat(result.getEmails())
                .hasSize(2)
                .containsExactly("email1@test.com", "email2@test.com");
    }

    @Test
    void testDeserializeEmptyArray() throws Exception {
        String json = "{\"emails\": []}";
        TestContainer result = objectMapper.readValue(json, TestContainer.class);

        assertThat(result.getEmails()).isEmpty();
    }

    @Test
    void testDeserializeArrayWithEmptyStrings() throws Exception {
        String json = "{\"emails\": [\"\", \"email@test.com\", \"\"]}";
        TestContainer result = objectMapper.readValue(json, TestContainer.class);

        assertThat(result.getEmails())
                .hasSize(1)
                .containsExactly("email@test.com");
    }
}
