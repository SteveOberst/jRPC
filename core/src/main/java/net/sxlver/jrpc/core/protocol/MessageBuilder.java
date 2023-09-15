package net.sxlver.jrpc.core.protocol;

import lombok.NonNull;

public interface MessageBuilder<T extends Message> {

    MessageBuilder<T> data(final byte[] data);

    MessageBuilder<T> target(final @NonNull String target);

    MessageBuilder<T> targetType(final @NonNull Message.TargetType targetType);

    T build();
}
