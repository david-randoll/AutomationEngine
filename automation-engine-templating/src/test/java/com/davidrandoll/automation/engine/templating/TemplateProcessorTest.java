package com.davidrandoll.automation.engine.templating;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AutomationEngineApplication.class)
@ExtendWith(SpringExtension.class)
class TemplateProcessorTest {
    @Autowired
    private TemplateProcessor templateProcessor;

    @ParameterizedTest
    @MethodSource("provideTemplatesAndData")
    void testProcessTemplate(String template, Map<String, Object> data, String expectedOutput) throws Exception {
        String result = templateProcessor.process(template, data);
        Assertions.assertEquals(expectedOutput, result);
    }

    @Test
    void testProcessWithTemplatingType() throws Exception {
        Map<String, Object> data = Map.of("name", "Alice");

        // Test Pebble (explicit)
        String pebbleResult = templateProcessor.process("Hello, {{ name }}!", data, "pebble");
        Assertions.assertEquals("Hello, Alice!", pebbleResult);

        // Test SpEL (explicit)
        String spelResult = templateProcessor.process("Hello, #{name}!", data, "spel");
        Assertions.assertEquals("Hello, Alice!", spelResult);

        // Test Default (null) - should use default engine (pebble)
        String defaultResult = templateProcessor.process("Hello, {{ name }}!", data, null);
        Assertions.assertEquals("Hello, Alice!", defaultResult);
    }

    static Stream<Arguments> provideTemplatesAndData() {
        return Stream.of(
                Arguments.of(
                        "Hello, {{ name }}!",
                        Map.of("name", "Alice"),
                        "Hello, Alice!"
                ),
                Arguments.of(
                        "Today is {{ day }}.",
                        Map.of("day", "Monday"),
                        "Today is Monday."
                ),
                Arguments.of(
                        "Price: {{ price }}, Discount: {{ discount }}.",
                        Map.of("price", "100", "discount", "10%"),
                        "Price: 100, Discount: 10%."
                ),
                Arguments.of(
                        "No placeholders here.",
                        Map.of("irrelevantKey", "irrelevantValue"),
                        "No placeholders here."
                ),
                Arguments.of(
                        "Multiple variables: {{ var1 }}, {{ var2 }}.",
                        Map.of("var1", "Value1", "var2", "Value2"),
                        "Multiple variables: Value1, Value2."
                ),
                // Conditional Example: "if"
                Arguments.of(
                        "Hello, {{ name }}! {% if isAdult %}You are an adult{% else %}You are a minor{% endif %}",
                        Map.of("name", "Bob", "isAdult", true),
                        "Hello, Bob! You are an adult"
                ),
                Arguments.of(
                        "Hello, {{ name }}! {% if isAdult %}You are an adult{% else %}You are a minor{% endif %}",
                        Map.of("name", "Charlie", "isAdult", false),
                        "Hello, Charlie! You are a minor"
                ),
                // Loop Example: Iterating over a list
                Arguments.of(
                        "Items: {% if items.size > 0 %}Available items: {% for item in items %}{{ item }} {% endfor %}{% else %}No items available.{% endif %}",
                        Map.of("items", List.of("Apple", "Banana", "Cherry")),
                        "Items: Available items: Apple Banana Cherry "
                ),
                // Conditional and Loop Combined
                Arguments.of(
                        "Greetings: {% if users.size == 0 %}No users available{% else %}Users are: {{ users | join(', ') }}{% endif %}",
                        Map.of("users", List.of()),
                        "Greetings: No users available"
                ),
                Arguments.of(
                        "Greetings: {% if users.size == 0 %}No users available{% else %}{{ users | join(', ') }}{% endif %}",
                        Map.of("users", List.of("Alice", "Bob")),
                        "Greetings: Alice, Bob"
                ),
                // Nested condition: "if" inside a loop
                Arguments.of(
                        "User List: {% for user in users %}{{ user.name }} {% if user.age > 18 %}Adult{% else %}Minor{% endif %} {% endfor %}",
                        Map.of("users", List.of(
                                new User("Alice", 22, 0),
                                new User("Bob", 17, 0)
                        )),
                        "User List: Alice Adult Bob Minor "
                ),
                // Math operations
                Arguments.of(
                        "Total: {{ price * quantity }}",
                        Map.of("price", 25, "quantity", 4),
                        "Total: 100"
                ),

                // Conditional with math
                Arguments.of(
                        "{% if discount > 0 %}Discounted Price: {{ price - (price * discount / 100) }}{% else %}No Discount{% endif %}",
                        Map.of("price", 200, "discount", 15),
                        "Discounted Price: 170"
                ),
                // Conditional with math and formatting
                Arguments.of(
                        "{% if discount > 0 %}Discounted Price: {{ (price - (price * discount / 100)) | number_format(decimalPlaces=1) }}{% else %}No Discount{% endif %}",
                        Map.of("price", 200, "discount", 15),
                        "Discounted Price: 170.0"
                ),

                // Formatting to currency
                Arguments.of(
                        "Price: {{ price | number_format(decimalPlaces=2) }}",
                        Map.of("price", 1234.5678),
                        "Price: 1234.57"
                ),

                // Combining math and currency formatting
                Arguments.of(
                        "Final Price: {{ (price - discount) | number_format(decimalPlaces=2) }}",
                        Map.of("price", 1000.0, "discount", 200.0),
                        "Final Price: 800.00"
                ),

                // Using conditions and math for different tax rates
                Arguments.of(
                        "Tax: {% if taxRate > 0 %}{{ (amount * taxRate / 100) | number_format(decimalPlaces=2) }}{% else %}No Tax{% endif %}",
                        Map.of("amount", 500.0, "taxRate", 10.0),
                        "Tax: 50.00"
                ),

                // Loop with math
                Arguments.of(
                        "Items: {% for item in items %}{{ item.name }}: {{ (item.price * item.quantity) | number_format(decimalPlaces=2) }} {% endfor %}",
                        Map.of("items", List.of(
                                new Item("Apple", 2.5, 4),
                                new Item("Banana", 1.2, 3)
                        )),
                        "Items: Apple: 10.00 Banana: 3.60 "
                ),


                // Loop with condition and formatting
                Arguments.of(
                        "{% for user in users %}{{ user.name }} - {% if user.balance > 0 %}Balance: {{ user.balance | number_format(decimalPlaces=2) }}{% else %}No Balance{% endif %} {% endfor %}",
                        Map.of("users", List.of(
                                new User("Alice", 17, 150.75),
                                new User("Bob", 30, 0.0)
                        )),
                        "Alice - Balance: 150.75 Bob - No Balance "
                ),

                //for loop within for
                Arguments.of(
                        "Items: {% for order in orders %}Order ID: {{ order.id }} - {% for item in order.items %}{{ item.name }}: {{ (item.price * item.quantity) | number_format(2) }} {% endfor %}{% endfor %}",
                        Map.of("orders", List.of(
                                new Order(1, List.of(new Item("Apple", 2.5, 4), new Item("Banana", 1.2, 3))),
                                new Order(2, List.of(new Item("Orange", 3.0, 2), new Item("Grape", 1.5, 5)))
                        )),
                        "Items: Order ID: 1 - Apple: 10.00 Banana: 3.60 Order ID: 2 - Orange: 6.00 Grape: 7.50 "
                ),

                // Test time_format filter with LocalTime
                Arguments.of(
                        "Time: {{ time | time_format }}",
                        Map.of("time", java.time.LocalTime.of(14, 30)),
                        "Time: 02:30 PM"
                ),
                Arguments.of(
                        "Time: {{ time | time_format(pattern='HH:mm:ss') }}",
                        Map.of("time", java.time.LocalTime.of(9, 15)),
                        "Time: 09:15:00"
                ),
                Arguments.of(
                        "Meeting at {{ time | time_format(pattern='hh:mm a') }}",
                        Map.of("time", java.time.LocalTime.of(15, 45)),
                        "Meeting at 03:45 PM"
                ),

                // Test time_format filter with String
                Arguments.of(
                        "Time: {{ timeStr | time_format }}",
                        Map.of("timeStr", "14:30"),
                        "Time: 02:30 PM"
                ),

                // Test int filter
                Arguments.of(
                        "Integer: {{ value | int }}",
                        Map.of("value", 42.7),
                        "Integer: 42"
                ),
                Arguments.of(
                        "Integer: {{ value | int }}",
                        Map.of("value", "123"),
                        "Integer: 123"
                ),
                Arguments.of(
                        "Integer: {{ value | int }}",
                        Map.of("value", 99),
                        "Integer: 99"
                ),

                // Test number_format with different decimal places
                Arguments.of(
                        "Price: {{ price | number_format }}",
                        Map.of("price", 99.999),
                        "Price: 100.00"
                ),
                Arguments.of(
                        "Price: {{ price | number_format(decimalPlaces=3) }}",
                        Map.of("price", 99.456),
                        "Price: 99.456"
                ),
                Arguments.of(
                        "Quantity: {{ quantity | number_format(decimalPlaces=0) }}",
                        Map.of("quantity", 42.7),
                        "Quantity: 43"
                ),

                // Test empty template
                Arguments.of(
                        "",
                        Map.of("key", "value"),
                        ""
                ),

                // Test whitespace handling
                Arguments.of(
                        "   {{ value }}   ",
                        Map.of("value", "test"),
                        "   test   "
                ),

                // Test nested objects
                Arguments.of(
                        "User: {{ user.name }}, Age: {{ user.age }}",
                        Map.of("user", new User("Alice", 25, 100.0)),
                        "User: Alice, Age: 25"
                )
        );
    }

    @Data
    @AllArgsConstructor
    static class User {
        private String name;
        private int age;
        private double balance;
    }

    @Data
    @AllArgsConstructor
    static class Item {
        private String name;
        private double price;
        private int quantity;
    }

    @Data
    @AllArgsConstructor
    static class Order {
        private int id;
        private List<Item> items;
    }
}
