package net.sxlver.jrpc.core.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;

public enum CentralGson {
    PROTOCOL_INSTANCE;

    private final GsonBuilder gsonBuilder;
    private Gson gson;

    private static final Object lock = new Object();

    CentralGson() {
        this.gsonBuilder = new GsonBuilder()
                .setExclusionStrategies(new SerializationExclusionStrategy())
                .enableComplexMapKeySerialization()
                .disableInnerClassSerialization()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .setObjectToNumberStrategy(CustomToNumberPolicy.INT_LONG_DOUBLE);

        this.gson = gsonBuilder.create();
    }

    public GsonBuilder getGsonBuilder() {
        synchronized (lock) {
            return gsonBuilder;
        }
    }

    public Gson getGson() {
        synchronized (lock) {
            return gson;
        }
    }

    public void registerTypeAdapter(final TypeAdapterFactory factory) {
        synchronized (lock) {
            gsonBuilder.registerTypeAdapterFactory(factory);
            rebuild();
        }
    }

    public void rebuild() {
        this.gson = gsonBuilder.create();
    }
}
