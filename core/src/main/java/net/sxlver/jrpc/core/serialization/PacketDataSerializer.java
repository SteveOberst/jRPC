package net.sxlver.jrpc.core.serialization;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.sxlver.jrpc.core.protocol.Packet;
import net.sxlver.jrpc.core.protocol.impl.HeaderData;
import net.sxlver.jrpc.core.protocol.impl.JRPCMessage;
import net.sxlver.jrpc.core.serialization.exception.DeserializationException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class PacketDataSerializer {
    public static byte[] serialize(final Object toSerialize) {
        try {
            final Gson gson = CentralGson.PROTOCOL_INSTANCE.getGson();
            final String json = gson.toJson(toSerialize);
            return Base64.getEncoder().encode(json.getBytes(StandardCharsets.UTF_8));
        } catch(final Exception exception) {
            exception.printStackTrace();
            //throw new DeserializationException(exception);
        }
        return null;
    }

    public static <T> T deserialize(final JsonObject jsonObject, final Class<T> cls) {
        try {
            final Gson gson = CentralGson.PROTOCOL_INSTANCE.getGson();
            final String json = jsonObject.toString();
            return gson.fromJson(json, cls);
        }catch(final Exception exception) {
            exception.printStackTrace();
            //throw new DeserializationException(exception);
        }
        return null;
    }

    public static <T> T deserialize(final byte[] data, final Class<T> cls) {
        try {
            final Gson gson = CentralGson.PROTOCOL_INSTANCE.getGson();
            final String json = new String(Base64.getDecoder().decode(data), StandardCharsets.UTF_8);
            return gson.fromJson(json, cls);
        }catch(final Exception exception) {
            exception.printStackTrace();
            //throw new DeserializationException(exception);
        }
        return null;
    }

    public static <T> T deserializePacket(final byte[] data) {
        try {
            final Gson gson = CentralGson.PROTOCOL_INSTANCE.getGson();
            final JsonObject json = deserializeJson(data);
            final Class<? extends Packet> packetCls = extractClass(json);
            return (T) gson.fromJson(json.toString(), packetCls);
        }catch(final Exception exception) {
            exception.printStackTrace();
            //throw new DeserializationException(exception);
        }
        return null;
    }

    public static String extractClassPath(final byte[] data) {
        return deserializeJson(data).get("packetCls").getAsString();
    }

    public static String extractClassPath(final JsonObject json) {
        return json.get("packetCls").getAsString();
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends Packet> extractClass(final JsonObject json) {
        final String packetCls = json.get("packetCls").getAsString();
        try {
            return (Class<? extends Packet>) Class.forName(json.get("packetCls").getAsString());
        } catch (final ClassNotFoundException exception) {
            throw new DeserializationException(String.format("Cannot deserialize packet because the class at path '%s' could not be found.", packetCls));
        }
    }

    public static Class<? extends Packet> extractClass(final byte[] data) {
        return extractClass(deserializeJson(data));
    }

    public static JsonObject deserializeJson(final byte[] data) {
        final String json = new String(Base64.getDecoder().decode(data), StandardCharsets.UTF_8);
        return JsonParser.parseString(json).getAsJsonObject();
    }
}
