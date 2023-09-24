package net.sxlver.jrpc.core.protocol;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public enum MessageType {
    HANDSHAKE(1),
    MESSAGE(2);

    private final int id;

    MessageType(final int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Nullable
    public static MessageType of(final int id) {
        return Arrays.stream(values()).filter(messageType -> messageType.getId() == id).findFirst().orElse(null);
    }
}
