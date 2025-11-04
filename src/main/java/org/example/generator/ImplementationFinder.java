package org.example.generator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class ImplementationFinder {

    private ImplementationFinder() {}

    private static final String SCAN_PACKAGE = "org.example.classes";

    private static final ConcurrentHashMap<Class<?>, List<Class<?>>> IMPLEMENTATIONS_CACHE = new ConcurrentHashMap<>();

    public static List<Class<?>> findImplementations(Class<?> targetType) {
        return IMPLEMENTATIONS_CACHE.computeIfAbsent(targetType, t -> {
            List<Class<?>> found = new ArrayList<>();
            String path = SCAN_PACKAGE.replace('.', '/');
            try {
                var resources = Thread.currentThread().getContextClassLoader().getResources(path);
                while (resources.hasMoreElements()) {
                    URL url = resources.nextElement();
                    if ("file".equals(url.getProtocol())) {
                        File dir = new File(url.toURI());
                        File[] files = dir.listFiles((d, name) -> name.endsWith(".class") && !name.contains("$"));
                        if (files == null) continue;
                        for (File f : files) {
                            String className = SCAN_PACKAGE + "." + f.getName().substring(0, f.getName().length() - 6);
                            try {
                                Class<?> candidate = Class.forName(className);
                                if (!Modifier.isAbstract(candidate.getModifiers())
                                        && candidate.getAnnotation(Generatable.class) != null
                                        && t.isAssignableFrom(candidate)) {
                                    found.add(candidate);
                                }
                            } catch (ClassNotFoundException ignored) {
                            }
                        }
                    }
                }
            } catch (IOException | URISyntaxException ignored) {
            }
            return found;
        });
    }
}


