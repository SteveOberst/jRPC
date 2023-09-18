package net.sxlver.jrpc.core.serialization;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import net.sxlver.jrpc.core.serialization.filter.FieldFilters;

public class SerializationExclusionStrategy implements ExclusionStrategy {
    @Override
    public boolean shouldSkipField(final FieldAttributes attributes) {
        return !FieldFilters.DEFAULT.test(attributes);
    }

    @Override
    public boolean shouldSkipClass(final Class<?> clazz) {
        return clazz.isAnnotationPresent(JsonIgnore.class);
    }
}
