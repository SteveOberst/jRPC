package net.sxlver.jrpc.core.protocol;

import lombok.NonNull;

public interface MessageBuilder<T extends Message> {
    MessageBuilder<T> target(final @NonNull String target);

    MessageBuilder<T> targetType(final @NonNull Message.TargetType targetType);

    MessageBuilder<T> source(final @NonNull DataSource dataSource);

    MessageBuilder<T> data(final byte[] data);

    T build();
}
