package com.davidrandoll.automation.engine.lua;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for LuaScriptEngine functionality.
 */
class LuaScriptEngineTest {
    private LuaScriptEngine luaEngine;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        List<ILuaFunctionContributor> contributors = List.of(
                new LogLuaFunctionContributor(),
                new JsonLuaFunctionContributor(objectMapper)
        );
        luaEngine = new LuaScriptEngine(objectMapper, contributors);
    }

    @Test
    void testSimpleReturnValue() {
        Object result = luaEngine.execute("return 42", Map.of());
        assertThat(result).isEqualTo(42);
    }

    @Test
    void testReturnString() {
        Object result = luaEngine.execute("return 'Hello, World!'", Map.of());
        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    void testReturnBoolean() {
        Object result = luaEngine.execute("return true", Map.of());
        assertThat(result).isEqualTo(true);
    }

    @Test
    void testReturnTable() {
        Object result = luaEngine.execute("return {name = 'John', age = 30}", Map.of());
        assertThat(result).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        assertThat(map).containsEntry("name", "John");
        assertThat(map).containsEntry("age", 30);
    }

    @Test
    void testReturnArray() {
        Object result = luaEngine.execute("return {1, 2, 3, 4, 5}", Map.of());
        assertThat(result).isInstanceOf(List.class);
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) result;
        assertThat(list).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    void testAccessEventVariable() {
        Map<String, Object> event = Map.of("username", "Alice", "count", 10);
        Object result = luaEngine.execute("return event.username", Map.of("event", event));
        assertThat(result).isEqualTo("Alice");
    }

    @Test
    void testAccessNestedVariable() {
        Map<String, Object> event = Map.of("user", Map.of("name", "Bob", "email", "bob@example.com"));
        Object result = luaEngine.execute("return event.user.email", Map.of("event", event));
        assertThat(result).isEqualTo("bob@example.com");
    }

    @Test
    void testStringConcatenation() {
        Map<String, Object> bindings = Map.of("name", "World");
        Object result = luaEngine.execute("return 'Hello, ' .. name .. '!'", bindings);
        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    void testMathOperations() {
        Map<String, Object> bindings = Map.of("price", 100, "quantity", 5);
        Object result = luaEngine.execute("return price * quantity", bindings);
        assertThat(result).isEqualTo(500);
    }

    @Test
    void testConditionalLogic() {
        Map<String, Object> bindings = Map.of("value", 15);
        Object result = luaEngine.execute("""
                if value > 10 then
                    return 'high'
                else
                    return 'low'
                end
                """, bindings);
        assertThat(result).isEqualTo("high");
    }

    @Test
    void testLoopLogic() {
        Object result = luaEngine.execute("""
                local sum = 0
                for i = 1, 5 do
                    sum = sum + i
                end
                return sum
                """, Map.of());
        assertThat(result).isEqualTo(15);
    }

    @Test
    void testTableIteration() {
        Map<String, Object> bindings = Map.of("items", List.of(10, 20, 30));
        Object result = luaEngine.execute("""
                local sum = 0
                for i, v in ipairs(items) do
                    sum = sum + v
                end
                return sum
                """, bindings);
        assertThat(result).isEqualTo(60);
    }

    @Test
    void testExecuteAsBoolean_True() {
        boolean result = luaEngine.executeAsBoolean("return true", Map.of());
        assertThat(result).isTrue();
    }

    @Test
    void testExecuteAsBoolean_False() {
        boolean result = luaEngine.executeAsBoolean("return false", Map.of());
        assertThat(result).isFalse();
    }

    @Test
    void testExecuteAsBoolean_Nil() {
        boolean result = luaEngine.executeAsBoolean("return nil", Map.of());
        assertThat(result).isFalse();
    }

    @Test
    void testExecuteAsBoolean_Number() {
        boolean result = luaEngine.executeAsBoolean("return 1", Map.of());
        assertThat(result).isTrue();
    }

    @Test
    void testExecuteAsBoolean_Zero() {
        boolean result = luaEngine.executeAsBoolean("return 0", Map.of());
        assertThat(result).isFalse();
    }

    @Test
    void testExecuteAsBoolean_String() {
        boolean result = luaEngine.executeAsBoolean("return 'hello'", Map.of());
        assertThat(result).isTrue();
    }

    @Test
    void testExecuteAsBoolean_EmptyString() {
        boolean result = luaEngine.executeAsBoolean("return ''", Map.of());
        assertThat(result).isFalse();
    }

    @Test
    void testExecuteAsMap_ReturnsTable() {
        Map<String, Object> result = luaEngine.executeAsMap(
                "return {status = 'ok', count = 5}", Map.of());
        assertThat(result).containsEntry("status", "ok");
        assertThat(result).containsEntry("count", 5);
    }

    @Test
    void testExecuteAsMap_ReturnsNonTable() {
        Map<String, Object> result = luaEngine.executeAsMap("return 42", Map.of());
        assertThat(result).isEmpty();
    }

    @Test
    void testLogFunctions() {
        // Just verifying that log functions don't throw exceptions
        assertThatNoException().isThrownBy(() -> 
            luaEngine.execute("""
                log.info("Info message")
                log.warn("Warning message")
                log.error("Error message")
                log.debug("Debug message")
                return true
                """, Map.of())
        );
    }

    @Test
    void testJsonEncode() {
        Object result = luaEngine.execute("""
                local data = {name = 'test', value = 123}
                return json.encode(data)
                """, Map.of());
        assertThat(result).isInstanceOf(String.class);
        String json = (String) result;
        assertThat(json).contains("name");
        assertThat(json).contains("test");
    }

    @Test
    void testJsonDecode() {
        Object result = luaEngine.execute("""
                local data = json.decode('{"name":"test","value":123}')
                return data.name
                """, Map.of());
        assertThat(result).isEqualTo("test");
    }

    @Test
    void testScriptError_ThrowsException() {
        assertThatThrownBy(() -> 
            luaEngine.execute("invalid lua syntax {{{", Map.of())
        ).isInstanceOf(LuaScriptEngine.LuaScriptExecutionException.class);
    }

    @Test
    void testRuntimeError_ThrowsException() {
        assertThatThrownBy(() -> 
            luaEngine.execute("return undefinedFunction()", Map.of())
        ).isInstanceOf(LuaScriptEngine.LuaScriptExecutionException.class);
    }

    @ParameterizedTest
    @MethodSource("provideBooleanExpressions")
    void testBooleanExpressions(String script, Map<String, Object> bindings, boolean expected) {
        boolean result = luaEngine.executeAsBoolean(script, bindings);
        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> provideBooleanExpressions() {
        return Stream.of(
                Arguments.of("return event.count > 5", Map.of("event", Map.of("count", 10)), true),
                Arguments.of("return event.count > 5", Map.of("event", Map.of("count", 3)), false),
                Arguments.of("return event.status == 'active'", Map.of("event", Map.of("status", "active")), true),
                Arguments.of("return event.status == 'active'", Map.of("event", Map.of("status", "inactive")), false),
                Arguments.of("return event.priority == 'HIGH' and event.count > 10",
                        Map.of("event", Map.of("priority", "HIGH", "count", 15)), true),
                Arguments.of("return event.priority == 'HIGH' and event.count > 10",
                        Map.of("event", Map.of("priority", "LOW", "count", 15)), false)
        );
    }

    @Test
    void testComplexObjectConversion() {
        Map<String, Object> complexEvent = new HashMap<>();
        complexEvent.put("name", "Test");
        complexEvent.put("numbers", List.of(1, 2, 3));
        complexEvent.put("nested", Map.of("key", "value"));
        
        Object result = luaEngine.execute("""
                return {
                    eventName = event.name,
                    firstNumber = event.numbers[1],
                    nestedValue = event.nested.key
                }
                """, Map.of("event", complexEvent));
        
        assertThat(result).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        assertThat(map).containsEntry("eventName", "Test");
        assertThat(map).containsEntry("firstNumber", 1);
        assertThat(map).containsEntry("nestedValue", "value");
    }

    @Test
    void testNullHandling() {
        Map<String, Object> bindings = new HashMap<>();
        bindings.put("nullValue", null);
        
        boolean result = luaEngine.executeAsBoolean("return nullValue == nil", bindings);
        assertThat(result).isTrue();
    }

    @Test
    void testFloatingPointNumbers() {
        Map<String, Object> bindings = Map.of("price", 19.99);
        Object result = luaEngine.execute("return price * 2", bindings);
        assertThat(result).isEqualTo(39.98);
    }
}
