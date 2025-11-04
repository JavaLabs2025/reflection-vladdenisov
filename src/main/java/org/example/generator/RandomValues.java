package org.example.generator;

import java.util.Objects;
import java.util.Random;

public final class RandomValues {

    private RandomValues() {}

    public static Object generateWrapperOrCommon(Class<?> cl, Random random) {
        Objects.requireNonNull(random, "random");
        if (cl == String.class) {
            return randomString(random);
        }
        if (cl == Integer.class) {
            return random.nextInt(201) - 100;
        }
        if (cl == Long.class) {
            return (long) (random.nextInt(2001) - 1000);
        }
        if (cl == Double.class) {
            return (random.nextDouble() * 200.0) - 100.0;
        }
        if (cl == Float.class) {
            return (random.nextFloat() * 200.0f) - 100.0f;
        }
        if (cl == Short.class) {
            return (short) (random.nextInt(2001) - 1000);
        }
        if (cl == Byte.class) {
            return (byte) (random.nextInt(201) - 100);
        }
        if (cl == Boolean.class) {
            return random.nextBoolean();
        }
        if (cl == Character.class) {
            return (char) (random.nextInt(26) + 'a');
        }
        return null;
    }

    public static Object generatePrimitiveValue(Class<?> cl, Random random) {
        Objects.requireNonNull(random, "random");
        if (cl == int.class) {
            return random.nextInt(201) - 100;
        }
        if (cl == long.class) {
            return (long) (random.nextInt(2001) - 1000);
        }
        if (cl == double.class) {
            return (random.nextDouble() * 200.0) - 100.0;
        }
        if (cl == float.class) {
            return (random.nextFloat() * 200.0f) - 100.0f;
        }
        if (cl == short.class) {
            return (short) (random.nextInt(2001) - 1000);
        }
        if (cl == byte.class) {
            return (byte) (random.nextInt(201) - 100);
        }
        if (cl == boolean.class) {
            return random.nextBoolean();
        }
        if (cl == char.class) {
            return (char) (random.nextInt(26) + 'a');
        }
        throw new IllegalArgumentException("Unsupported primitive type: " + cl.getName());
    }

    public static String randomString(Random random) {
        Objects.requireNonNull(random, "random");
        int length = random.nextInt(10) + 1;
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append((char) ('a' + random.nextInt(26)));
        }
        return builder.toString();
    }
}


