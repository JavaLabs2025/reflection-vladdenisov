package org.example.generator;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public final class TypeHelpers {

    private TypeHelpers() {}

    private static final Set<Class<?>> IMMUTABLE_KEY_TYPES = Set.of(
            String.class, Integer.class, Long.class, Double.class,
            Float.class, Short.class, Byte.class, Character.class
    );

    public static Class<?> resolveCollectionElementType(Type genericType) {
        if (genericType instanceof ParameterizedType parameterizedType) {
            Type[] arguments = parameterizedType.getActualTypeArguments();
            if (arguments.length == 1) {
                Type argument = arguments[0];
                if (argument instanceof Class<?> aClass) {
                    return aClass;
                }
                if (argument instanceof ParameterizedType parameterizedArgument) {
                    Type raw = parameterizedArgument.getRawType();
                    if (raw instanceof Class<?> aClass) {
                        return aClass;
                    }
                }
            }
        }
        return Object.class;
    }

    public static Class<?>[] resolveMapKeyValueTypes(Type genericType) {
        Class<?> key = Object.class;
        Class<?> value = Object.class;
        if (genericType instanceof ParameterizedType parameterizedType) {
            Type[] args = parameterizedType.getActualTypeArguments();
            if (args.length == 2) {
                if (args[0] instanceof Class<?> k) key = k;
                if (args[1] instanceof Class<?> v) value = v;
            }
        }
        return new Class<?>[]{key, value};
    }

    public static boolean isImmutableKeyType(Class<?> cl) {
        if (cl == null) return false;
        if (cl.isEnum()) return true;
        return IMMUTABLE_KEY_TYPES.contains(cl) || cl.isPrimitive();
    }

    public static Collection<?> createEmptyCollection(Class<?> rawType) {
        if (List.class.isAssignableFrom(rawType)) return new ArrayList<>();
        if (Set.class.isAssignableFrom(rawType)) return new HashSet<>();
        if (Queue.class.isAssignableFrom(rawType)) return new LinkedList<>();
        if (Collection.class.isAssignableFrom(rawType)) return new ArrayList<>();
        throw new IllegalArgumentException("Unsupported collection type: " + rawType.getName());
    }

    public static Map<?, ?> createEmptyMap(Class<?> rawType) {
        if (Map.class.isAssignableFrom(rawType)) return new HashMap<>();
        throw new IllegalArgumentException("Unsupported map type: " + rawType.getName());
    }
}


