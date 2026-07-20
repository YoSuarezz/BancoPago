package com.bancopago.backend.crosscutting.helpers;

import java.util.function.Supplier;

public final class ObjectHelper {
    private ObjectHelper() {}

    public static <T> T getDefault(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    public static <T> T getDefault(T value, Supplier<T> defaultValueSupplier) {
        return value != null ? value : defaultValueSupplier.get();
    }

    public static <T> T requireNonNull(T value, Supplier<RuntimeException> exceptionSupplier) {
        if (value == null) throw exceptionSupplier.get();
        return value;
    }
}
