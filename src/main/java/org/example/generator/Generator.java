package org.example.generator;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public class Generator {

    private static final int DEFAULT_MAX_DEPTH = 3;
    private static final int DEFAULT_MAX_COLLECTION_SIZE = 3;

    private final Random random;
    private final int maxDepth;
    private final int maxCollectionSize;

    public Generator() {
        this(new Random(), DEFAULT_MAX_DEPTH, DEFAULT_MAX_COLLECTION_SIZE);
    }

    public Generator(Random random, int maxDepth, int maxCollectionSize) {
        this.random = Objects.requireNonNull(random, "random");
        this.maxDepth = Math.max(1, maxDepth);
        this.maxCollectionSize = Math.max(0, maxCollectionSize);
    }

    public Object generateValueOfType(Class<?> clazz) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return generateValueOfType(clazz, 0);
    }

    private Object generateValueOfType(Class<?> clazz, int depth) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (clazz == null) {
            return null;
        }

        Object simpleValue = tryGenerateSimpleValue(clazz);
        if (simpleValue != null) {
            return simpleValue;
        }

        if (depth >= maxDepth) {
            return null;
        }

        Class<?> concreteClass = resolveConcreteClass(clazz);
        Object instance = instantiate(concreteClass, depth);
        populateFields(instance, depth + 1);
        return instance;
    }

    private Object tryGenerateSimpleValue(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return RandomValues.generatePrimitiveValue(clazz, random);
        }

        Object basicValue = RandomValues.generateWrapperOrCommon(clazz, random);
        if (basicValue != null) {
            return basicValue;
        }

        if (clazz.isEnum()) {
            Object[] constants = clazz.getEnumConstants();
            if (constants.length == 0) {
                throw new IllegalArgumentException("Cannot instantiate enum without constants: " + clazz.getName());
            }
            return constants[random.nextInt(constants.length)];
        }

        return null;
    }

    private Class<?> resolveConcreteClass(Class<?> clazz) {
        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            List<Class<?>> candidates = ImplementationFinder.findImplementations(clazz);
            if (candidates.isEmpty()) {
                throw new IllegalArgumentException("No generatable implementations found for type: " + clazz.getName());
            }
            return candidates.get(random.nextInt(candidates.size()));
        }

        if (clazz.getAnnotation(Generatable.class) == null) {
            throw new IllegalArgumentException("Type is not marked as generatable: " + clazz.getName());
        }

        return clazz;
    }

    private Object instantiate(Class<?> clazz, int depth) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        List<Constructor<?>> constructors = new ArrayList<>(Arrays.asList(clazz.getDeclaredConstructors()));
        if (constructors.isEmpty()) {
            throw new IllegalArgumentException("Type has no accessible constructors: " + clazz.getName());
        }
        Collections.shuffle(constructors, random);

        for (Constructor<?> constructor : constructors) {
            Parameter[] parameters = constructor.getParameters();
            Object[] args = new Object[parameters.length];
            boolean success = true;

            for (int i = 0; i < parameters.length; i++) {
                try {
                    args[i] = generateParameterValue(parameters[i], depth + 1);
                } catch (IllegalArgumentException e) {
                    success = false;
                    break;
                }
            }

            if (success) {
                return constructor.newInstance(args);
            }
        }

        throw new IllegalArgumentException("Unable to instantiate type: " + clazz.getName());
    }

    private Object generateParameterValue(Parameter parameter, int depth) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> rawType = parameter.getType();

        if (rawType.isPrimitive()) {
            return RandomValues.generatePrimitiveValue(rawType, random);
        }

        Object basicValue = RandomValues.generateWrapperOrCommon(rawType, random);
        if (basicValue != null) {
            return basicValue;
        }

        // For constructor parameters: Collections/Maps/Arrays should be empty
        if (Collection.class.isAssignableFrom(rawType)) {
            return TypeHelpers.createEmptyCollection(rawType);
        }
        if (Map.class.isAssignableFrom(rawType)) {
            return TypeHelpers.createEmptyMap(rawType);
        }
        if (rawType.isArray()) {
            return Array.newInstance(rawType.getComponentType(), 0);
        }

        if (depth >= maxDepth) {
            return null;
        }

        return generateValueOfType(rawType, depth);
    }

    private Collection<?> generateCollection(Type genericType, Class<?> rawType, int depth) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        int size = maxCollectionSize == 0 ? 0 : random.nextInt(maxCollectionSize + 1);
        Collection<Object> collection;

        if (rawType.isInterface()) {
            if (List.class.isAssignableFrom(rawType)) {
                collection = new ArrayList<>();
            } else if (Set.class.isAssignableFrom(rawType)) {
                collection = new HashSet<>();
            } else if (Queue.class.isAssignableFrom(rawType)) {
                collection = new LinkedList<>();
            } else {
                throw new IllegalArgumentException("Unsupported collection interface: " + rawType.getName());
            }
        } else {
            try {
                Constructor<?> ctor = rawType.getDeclaredConstructor();
                collection = (Collection<Object>) ctor.newInstance();
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Collection type requires no-arg constructor: " + rawType.getName(), e);
            }
        }

        Class<?> elementClass = TypeHelpers.resolveCollectionElementType(genericType);
        for (int i = 0; i < size; i++) {
            collection.add(generateCollectionElement(elementClass, depth + 1));
        }

        return collection;
    }

    private Map<?, ?> generateMap(Type genericType, Class<?> rawType, int depth) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        int size = 1 + random.nextInt(Math.max(1, Math.min(2, maxCollectionSize)));
        Map<Object, Object> map;

        if (rawType.isInterface()) {
            map = new HashMap<>();
        } else {
            try {
                Constructor<?> ctor = rawType.getDeclaredConstructor();
                map = (Map<Object, Object>) ctor.newInstance();
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Map type requires no-arg constructor: " + rawType.getName(), e);
            }
        }

        Class<?>[] kv = TypeHelpers.resolveMapKeyValueTypes(genericType);
        Class<?> keyClass = kv[0];
        Class<?> valueClass = kv[1];
        for (int i = 0; i < size; i++) {
            Object key = TypeHelpers.isImmutableKeyType(keyClass) ? generateElementForType(keyClass, depth + 1) : null;
            if (key == null) continue;
            Object value = generateElementForType(valueClass, depth + 1);
            map.put(key, value);
        }
        return map;
    }

    private Object generateCollectionElement(Class<?> elementClass, int depth) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (elementClass == Object.class) {
            return null;
        }
        if (elementClass.isPrimitive()) {
            return RandomValues.generatePrimitiveValue(elementClass, random);
        }
        Object basicValue = RandomValues.generateWrapperOrCommon(elementClass, random);
        if (basicValue != null) {
            return basicValue;
        }
        if (depth >= maxDepth) {
            return null;
        }
        return generateValueOfType(elementClass, depth);
    }

    private void populateFields(Object instance, int depth) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (instance == null || depth > maxDepth) return;

        Class<?> type = instance.getClass();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            Field[] fields = c.getDeclaredFields();
            for (Field field : fields) {
                int mod = field.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) continue;

                field.setAccessible(true);
                Class<?> fType = field.getType();
                Type gType = field.getGenericType();

                Object value;
                if (fType.isArray()) {
                    Class<?> component = fType.getComponentType();
                    int size = 1 + random.nextInt(2);
                    Object array = Array.newInstance(component, size);
                    for (int i = 0; i < size; i++) {
                        Array.set(array, i, generateElementForType(component, depth));
                    }
                    value = array;
                } else if (Collection.class.isAssignableFrom(fType)) {
                    Collection<?> coll = generateCollection(gType, fType, depth);
                    value = coll;
                } else if (Map.class.isAssignableFrom(fType)) {
                    Map<?, ?> m = generateMap(gType, fType, depth);
                    value = m;
                } else {
                    Object simple = tryGenerateSimpleValue(fType);
                    if (simple != null) {
                        value = simple;
                    } else if (depth < maxDepth) {
                        value = generateValueOfType(fType, depth);
                    } else {
                        value = null;
                    }
                }

                try {
                    field.set(instance, value);
                } catch (IllegalAccessException ignored) {
                }
            }
        }
    }

    private Object generateElementForType(Class<?> elementClass, int depth) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (elementClass == Object.class) return null;
        Object simple = tryGenerateSimpleValue(elementClass);
        if (simple != null) return simple;
        if (depth >= maxDepth) return null;
        return generateValueOfType(elementClass, depth);
    }
}
