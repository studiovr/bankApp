package com.bankapp.utils;
import java.util.HashMap;
import java.util.Map;

public class ServiceLocator {
    private static final Map<Class<?>, Object> dependencies = new HashMap<>();

    public static <T> void register(Class<T> clazz, T instance) {
        dependencies.put(clazz, instance);
    }

    public static <T> T get(Class<T> clazz) {
        return clazz.cast(dependencies.get(clazz));
    }
}
