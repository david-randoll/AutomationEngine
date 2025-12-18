package com.davidrandoll.automation.engine.spring.web.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonNodeMatcherDebug {
    @Test
    void debugEmptyStringMatch() {
        ObjectMapper mapper = new ObjectMapper();
        
        MultiValueMap<String, String> expected = new LinkedMultiValueMap<>();
        expected.add("status", "");
        
        MultiValueMap<String, String> actual = new LinkedMultiValueMap<>();
        actual.add("status", "");
        
        boolean result = JsonNodeMatcher.matches(expected, actual, mapper);
        System.out.println("Result: " + result);
        
        // matchesNode should return true, so matches should return false
        assertThat(result).isFalse();
    }
}
