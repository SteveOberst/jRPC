package net.sxlver.jrpc.core.serialization.filter;

import com.google.gson.FieldAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@FunctionalInterface
public interface FieldFilter extends Predicate<FieldAttributes> {

    @Override
    default FieldFilter and(Predicate<? super FieldAttributes> other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) && other.test(t);
    }
}
