package com.davidrandoll.automation.engine.spring.utils;

import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.AopProxyUtils;

import java.io.Serializable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AutomationProxyUtilTest {

    @Test
    void markWithAutomationOrigin_ShouldAddInterface() {
        TestObject target = new TestObject();
        TestObject proxy = AutomationProxyUtil.markWithAutomationOrigin(target, Serializable.class);

        assertThat(proxy).isInstanceOf(Serializable.class);
        assertThat(proxy).isInstanceOf(TestObject.class);
    }

    @Test
    void markWithAutomationOrigin_ShouldReturnSameObjectIfAlreadyMarked() {
        TestObject target = new TestObject();
        TestObject proxy1 = AutomationProxyUtil.markWithAutomationOrigin(target, Serializable.class);
        TestObject proxy2 = AutomationProxyUtil.markWithAutomationOrigin(proxy1, Serializable.class);

        assertThat(proxy1).isSameAs(proxy2);
    }

    @Test
    void markWithAutomationOriginOrThrow_ShouldThrowForFinalClass() {
        FinalTestObject target = new FinalTestObject();
        assertThatThrownBy(() -> AutomationProxyUtil.markWithAutomationOriginOrThrow(target, Serializable.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot mark final class");
    }

    @Test
    void markWithAutomationOriginOrThrow_ShouldThrowForNoDefaultConstructor() {
        NoDefaultConstructorTestObject target = new NoDefaultConstructorTestObject("test");
        assertThatThrownBy(() -> AutomationProxyUtil.markWithAutomationOriginOrThrow(target, Serializable.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no default constructor found");
    }

    @Test
    void markWithAutomationOriginOrThrow_ShouldSucceedForValidClass() {
        TestObject target = new TestObject();
        TestObject proxy = AutomationProxyUtil.markWithAutomationOriginOrThrow(target, Serializable.class);

        assertThat(proxy).isInstanceOf(Serializable.class);
    }

    public static class TestObject {
    }

    public static final class FinalTestObject {
    }

    public static class NoDefaultConstructorTestObject {
        public NoDefaultConstructorTestObject(String arg) {
        }
    }
}
