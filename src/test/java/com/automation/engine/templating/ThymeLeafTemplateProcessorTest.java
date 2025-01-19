package com.automation.engine.templating;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThymeLeafTemplateProcessorTest {

    private final ThymeLeafTemplateProcessor thymeLeafTemplateProcessor = new ThymeLeafTemplateProcessor();

    @ParameterizedTest
    @MethodSource("provideTemplatesAndData")
    void testProcessTemplate(String template, Map<String, Object> data, String expectedOutput) {
        String result = thymeLeafTemplateProcessor.processTemplate(template, data);
        assertEquals(expectedOutput, result);
    }

    static Stream<Arguments> provideTemplatesAndData() {
        return Stream.of(
                Arguments.of(
                        "Hello, [[${name}]]!",
                        Map.of("name", "Alice"),
                        "Hello, Alice!"
                ),
                Arguments.of(
                        "Today is [[${day}]].",
                        Map.of("day", "Monday"),
                        "Today is Monday."
                ),
                Arguments.of(
                        "Price: [[${price}]], Discount: [[${discount}]].",
                        Map.of("price", "100", "discount", "10%"),
                        "Price: 100, Discount: 10%."
                ),
                Arguments.of(
                        "No placeholders here.",
                        Map.of("irrelevantKey", "irrelevantValue"),
                        "No placeholders here."
                ),
                Arguments.of(
                        "Multiple variables: [[${var1}]], [[${var2}]].",
                        Map.of("var1", "Value1", "var2", "Value2"),
                        "Multiple variables: Value1, Value2."
                ),
                // Conditional Example: "if"
                Arguments.of(
                        "Hello, [[${name}]]! [[${isAdult} ? 'You are an adult' : 'You are a minor']]",
                        Map.of("name", "Bob", "isAdult", true),
                        "Hello, Bob! You are an adult"
                ),
                Arguments.of(
                        "Hello, [[${name}]]! [[${isAdult} ? 'You are an adult' : 'You are a minor']]",
                        Map.of("name", "Charlie", "isAdult", false),
                        "Hello, Charlie! You are a minor"
                ),
                // Loop Example: Iterating over a list
                Arguments.of(
                        "Items: [[${#lists.size(items) > 0 ? 'Available items:' : 'No items available.'}]] [[${items}]]",
                        Map.of("items", List.of("Apple", "Banana", "Cherry")),
                        "Items: Available items: [Apple, Banana, Cherry]"
                ),
                // Conditional and Loop Combined
                Arguments.of(
                        "Greetings: [[${#lists.isEmpty(users) ? 'No users available' : 'Users are: ' + #lists.listJoin(users, ', ')}]]",
                        Map.of("users", List.of()),
                        "Greetings: No users available"
                ),
                // Correcting join and condition with #strings.join()
                Arguments.of(
                        "Greetings: [[${#lists.isEmpty(users) ? 'No users available' : #strings.listJoin(users, ', ')}]]",
                        Map.of("users", List.of("Alice", "Bob")),
                        "Greetings: Alice, Bob"
                ),
                // Nested condition: "if" inside a loop, using correct access for `user`
                Arguments.of(
                        "User List: [[#lists.each(users, user)]] [[${user.name}]] [[${user.age > 18 ? 'Adult' : 'Minor'}]] [[/]]",
                        Map.of("users", List.of(
                                new User("Alice", 22),
                                new User("Bob", 17)
                        )),
                        "User List: Alice Adult Bob Minor"
                )
        );
    }

    @Data
    @AllArgsConstructor
    static class User {
        private String name;
        private int age;
    }
}