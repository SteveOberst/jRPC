package net.sxlver.jrpc.core.serialization;

import com.google.gson.JsonParseException;
import com.google.gson.ToNumberStrategy;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;

import java.io.IOException;

public enum CustomToNumberPolicy implements ToNumberStrategy {
    INT_LONG_DOUBLE {
        @Override
        public Number readNumber(JsonReader in) throws IOException, JsonParseException {
            String value = in.nextString();
            try {
                final long l = Long.parseLong(value);
                if(l < Integer.MAX_VALUE && l > Integer.MIN_VALUE) {
                    return (int) l;
                }
                return l;
            } catch (final NumberFormatException integerE) {
                try {
                    Double d = Double.valueOf(value);
                    if ((d.isInfinite() || d.isNaN()) && !in.isLenient()) {
                        throw new MalformedJsonException("JSON forbids NaN and infinities: " + d + "; at path " + in.getPath());
                    }
                    return d;
                } catch (NumberFormatException doubleE) {
                    throw new JsonParseException("Cannot parse " + value + "; at path " + in.getPath(), doubleE);
                }
            }
        }
    }
}
