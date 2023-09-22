package net.sxlver.jrpc.core.util;

import java.util.function.Consumer;

/**
 * Represents a consumer that accepts three arguments. This is the three-arity
 * specialization of {@link java.util.function.Consumer}.
 *
 * @param <T>  the type of the first argument to the consumer
 * @param <U>  the type of the second argument to the consumer
 * @param <V>  the type of the third argument to the consumer
 *
 * @see Consumer
 */
@FunctionalInterface
public interface TriConsumer<T, U, V> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t  the first consumer argument
     * @param u  the second consumer argument
     * @param v  the third consumer argument
     */
    void accept(T t, U u, V v);
}