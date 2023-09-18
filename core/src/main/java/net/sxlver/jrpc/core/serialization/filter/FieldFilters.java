package net.sxlver.jrpc.core.serialization.filter;

import com.google.gson.FieldAttributes;
import net.sxlver.jrpc.core.serialization.JsonIgnore;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public enum FieldFilters implements FieldFilter {
    DEFAULT {
        @Override
        public boolean test(final FieldAttributes attributes) {

            if (attributes.hasModifier(0x00001000)) { // synthetic
                return false;
            }

            return !(attributes.hasModifier(Modifier.FINAL) ||
                    attributes.hasModifier(Modifier.STATIC) ||
                    attributes.hasModifier(Modifier.TRANSIENT) ||
                    attributes.getAnnotation(JsonIgnore.class) != null);
        }
    }
}
