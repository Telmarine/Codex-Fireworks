package com.tac5studios.codexfireworks.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Small reflection helpers for optional mods (claim mods). Nothing here is needed when those mods are absent.
 * Every failure is thrown as an IllegalStateException so callers can catch one type.
 */
public final class Reflect {

    private static final Map<String, Class<?>> CLASSES = new ConcurrentHashMap<>();

    private Reflect() {}

    public static Class<?> cls(String name) {
        return CLASSES.computeIfAbsent(name, n -> {
            try {
                return Class.forName(n);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("class not found: " + n);
            }
        });
    }

    public static Object callStatic(String className, String method, Object... args) {
        return invoke(find(cls(className), method, args), null, args);
    }

    public static Object call(Object target, String method, Object... args) {
        if (target == null) throw new IllegalStateException("null target for " + method);
        return invoke(find(target.getClass(), method, args), target, args);
    }

    public static Object create(String className, Object... args) {
        for (Constructor<?> c : cls(className).getConstructors()) {
            if (matches(c.getParameterTypes(), args)) {
                try {
                    return c.newInstance(args);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalStateException(className + ": " + e);
                }
            }
        }
        throw new IllegalStateException("no matching constructor in " + className);
    }

    private static Object invoke(Method m, Object target, Object[] args) {
        try {
            return m.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(m.getName() + ": " + e);
        }
    }

    private static Method find(Class<?> c, String name, Object[] args) {
        for (Method m : c.getMethods()) {
            if (m.getName().equals(name) && matches(m.getParameterTypes(), args)) return m;
        }
        throw new IllegalStateException("no method " + name + " on " + c.getName());
    }

    private static boolean matches(Class<?>[] types, Object[] args) {
        if (types.length != args.length) return false;
        for (int i = 0; i < types.length; i++) {
            if (args[i] == null) {
                if (types[i].isPrimitive()) return false;
                continue;
            }
            if (!box(types[i]).isInstance(args[i])) return false;
        }
        return true;
    }

    private static Class<?> box(Class<?> c) {
        if (!c.isPrimitive()) return c;
        if (c == int.class) return Integer.class;
        if (c == long.class) return Long.class;
        if (c == boolean.class) return Boolean.class;
        if (c == double.class) return Double.class;
        if (c == float.class) return Float.class;
        if (c == short.class) return Short.class;
        if (c == byte.class) return Byte.class;
        return Character.class;
    }
}
