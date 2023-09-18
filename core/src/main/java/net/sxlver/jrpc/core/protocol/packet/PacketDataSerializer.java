package net.sxlver.jrpc.core.protocol.packet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.core.serialization.CentralGson;
import net.sxlver.jrpc.core.serialization.exception.DeserializationException;

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

    public static <T> T deserializePacket(final byte[] data) {
        final Gson gson = CentralGson.PROTOCOL_INSTANCE.getGson();
        final JsonObject json = deserializeJson(data);
        final Class<? extends Packet> packetCls = extractClassPath(json);
        return (T) gson.fromJson(json.toString(), packetCls);
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends Packet> extractClassPath(final JsonObject json) {
        final String packetCls = json.get("packetCls").getAsString();
        try {
            return (Class<? extends Packet>) Class.forName(json.get("packetCls").getAsString());
        } catch (final ClassNotFoundException exception) {
            throw new DeserializationException(String.format("Cannot deserialize packet because the class at path '%s' could not be found.", packetCls));
        }
    }
    @SuppressWarnings("unchecked")
    public static Class<? extends Packet> extractClassPath(final byte[] data) {
        try {
            return (Class<? extends Packet>) Class.forName(deserializeJson(data).get("packetCls").getAsString());
        } catch (final ClassNotFoundException exception) {
            throw new DeserializationException("Cannot deserialize packet because the source class could not be found.");
        }
    }

    public static JsonObject deserializeJson(final byte[] data) {
        final String json = new String(Base64.getDecoder().decode(data), StandardCharsets.UTF_8);
        return JsonParser.parseString(json).getAsJsonObject();
    }
}
