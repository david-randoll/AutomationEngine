package com.davidrandoll.automation.engine.utils;

import lombok.experimental.UtilityClass;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@UtilityClass
public class AutomationProxyUtil {
    /**
     * Marks the target object with one or more additional interfaces using a CGLIB proxy.
     * This allows downstream consumers to detect automation-originated events via interface checks (e.g., instanceof).
     *
     * @param target     The original object to proxy.
     * @param interfaces Additional interfaces to mark on the proxy.
     * @param <T>        The type of the target object.
     * @return A proxied version of the object with the specified interfaces.
     */
    public static <T> T markWithAutomationOrigin(T target, Class<?>... interfaces) {
        // If already marked, return as-is
        for (Class<?> marker : interfaces) {
            if (marker.isInstance(target)) return target;
        }

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(target.getClass());

        // Collect all interfaces from target + extras to avoid duplication

        // Add current interfaces on the object
        Set<Class<?>> interfaceSet = new LinkedHashSet<>(Arrays.asList(target.getClass().getInterfaces()));
        Collections.addAll(interfaceSet, interfaces);

        enhancer.setInterfaces(interfaceSet.toArray(Class<?>[]::new));

        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> proxy.invoke(target, args));
        return (T) enhancer.create();
    }

    public static <T> T markWithAutomationOriginOrThrow(T target, Class<?>... interfaces) {
        if (Modifier.isFinal(target.getClass().getModifiers())) {
            throw new IllegalArgumentException("Cannot mark final class " + target.getClass().getName() + " with automation origin.");
        }

        // check if there is a default constructor
        try {
            target.getClass().getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Cannot mark class " + target.getClass().getName() + " with automation origin, no default constructor found.");
        }

        return markWithAutomationOrigin(target, interfaces);
    }
}