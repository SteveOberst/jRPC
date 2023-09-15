package net.sxlver.jrpc.core.serialization;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class SerializationExclusionStrategy implements ExclusionStrategy {
    @Override
    public boolean shouldSkipField(final FieldAttributes attributes) {
        return attributes.getAnnotation(JsonIgnore.class) != null;
    }

    @Override
    public boolean shouldSkipClass(final Class<?> clazz) {
        return clazz.isAnnotationPresent(JsonIgnore.class);
    }
}
