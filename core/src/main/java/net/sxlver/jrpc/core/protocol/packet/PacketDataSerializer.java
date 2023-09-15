package net.sxlver.jrpc.core.protocol.packet;

import com.google.gson.Gson;
import net.sxlver.jrpc.core.serialization.CentralGson;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class PacketDataSerializer {
    public static byte[] serialize(Object toSerialize) {
        final Gson gson = CentralGson.PROTOCOL_INSTANCE.getGson();
        final String json = gson.toJson(toSerialize);
        return Base64.getEncoder().encode(json.getBytes(StandardCharsets.UTF_8));
    }

    public static <T> T deserialize(final byte[] data, final Class<T> cls) {
        final Gson gson = CentralGson.PROTOCOL_INSTANCE.getGson();
        final String json = new String(Base64.getDecoder().decode(data), StandardCharsets.UTF_8);
        return gson.fromJson(json, cls);
    }
}
