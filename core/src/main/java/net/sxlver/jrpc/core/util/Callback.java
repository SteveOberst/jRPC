package net.sxlver.jrpc.core.util;

import lombok.NonNull;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

public class Callback<T> {
    private Consumer<T> onCompleteConsumer = t -> {};
    private Consumer<Throwable> onExceptConsumer = throwable -> {};

    private Callback() {
    }

    private Callback(final @NonNull Consumer<T> onComplete) {
        this.onCompleteConsumer = onComplete;
    }

    private Callback(final @NonNull Consumer<T> onComplete, final @NonNull Consumer<Throwable> onExcept) {
        this.onCompleteConsumer = onComplete;
        this.onExceptConsumer = onExcept;
    }

    void complete(final T result) {
        onCompleteConsumer.accept(result);
    }

    public Callback<T> onComplete(final @NonNull Consumer<T> onComplete) {
        this.onCompleteConsumer = onComplete;
        return this;
    }

    void except(final Throwable throwable) {
        onExceptConsumer.accept(throwable);
    }

    public Callback<T> onExcept(final @NonNull Consumer<Throwable> onExcept) {
        this.onExceptConsumer = onExcept;
        return this;
    }

    public static <T> Callback<T> newCallback() {
        return new Callback<>();
    }

    public static <T> Callback<T> newCallback(final Consumer<T> onComplete) {
        return new Callback<>(onComplete);
    }

    public static <T> Callback<T> newCallback(final Consumer<T> onComplete, final Consumer<Throwable> onExcept) {
        return new Callback<>(onComplete, onExcept);
    }

    @ApiStatus.Internal
    public static class Internal {
        public static <T> void complete(final Callback<T> callback, T result) {
            callback.complete(result);
        }

        public static <T> void except(final Callback<T> callback, final Throwable throwable) {
            callback.except(throwable);
        }
    }
}
