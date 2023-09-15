package net.sxlver.jrpc.server.util;

import java.util.function.Supplier;

public class LazyInitVar<T> {
    private Supplier<T> supplier;
    private T futureValue;

    public LazyInitVar(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        Supplier<T> ref = this.supplier;
        if (ref != null) {
            this.futureValue = ref.get();
            this.supplier = null;
        }
        return this.futureValue;
    }
}