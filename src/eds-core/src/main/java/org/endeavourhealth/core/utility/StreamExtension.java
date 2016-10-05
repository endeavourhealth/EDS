package org.endeavourhealth.core.utility;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamExtension {
    public static <T> Collector<T, ?, T> singleCollector() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() != 1) {
                        throw new IllegalStateException();
                    }
                    return list.get(0);
                }
        );
    }

    public static <T> Collector<T, ?, T> singleOrNullCollector() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() == 0) {
                        return null;
                    }
                    else if (list.size() == 1) {
                        return list.get(0);
                    }
                    else {
                        throw new IllegalStateException();
                    }
                }
        );
    }

    public static <T> Collector<T, ?, T> firstCollector() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() == 0) {
                        throw new IllegalStateException();
                    }
                    return list.get(0);
                }
        );
    }

    public static <T> Collector<T, ?, T> firstOrNullCollector() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() == 0) {
                        return null;
                    }
                    else {
                        return list.get(0);
                    }
                }
        );
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T,Object> keyExtractor) {
        Map<Object,Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public static <T> Stream<T> concat(Stream<T> stream1, Stream<T> stream2) {
        return Stream.concat(stream1, stream2);
    }

    public static <T> Stream<T> concat(Stream<T> stream1, Stream<T> stream2, Stream<T> stream3) {
        return concat(concat(stream1, stream2), stream3);
    }

    public static <T> Stream<T> concat(Stream<T> stream1, Stream<T> stream2, Stream<T> stream3, Stream<T> stream4) {
        return concat(concat(stream1, stream2, stream3), stream4);
    }

    public static <T> Stream<T> concat(Stream<T> stream1, Stream<T> stream2, Stream<T> stream3, Stream<T> stream4, Stream<T> stream5) {
        return concat(concat(stream1, stream2, stream3, stream4), stream5);
    }

    public static <T> Stream<T> concat(Stream<T> stream1, Stream<T> stream2, Stream<T> stream3, Stream<T> stream4, Stream<T> stream5, Stream<T> stream6) {
        return concat(concat(stream1, stream2, stream3, stream4, stream5), stream6);
    }

    public static <T> Stream<T> concat(Stream<T> stream1, Stream<T> stream2, Stream<T> stream3, Stream<T> stream4, Stream<T> stream5, Stream<T> stream6, Stream<T> stream7) {
        return concat(concat(stream1, stream2, stream3, stream4, stream5, stream6), stream7);
    }

    public static <T> Stream<T> concat(Stream<T> stream1, Stream<T> stream2, Stream<T> stream3, Stream<T> stream4, Stream<T> stream5, Stream<T> stream6, Stream<T> stream7, Stream<T> stream8) {
        return concat(concat(stream1, stream2, stream3, stream4, stream5, stream6, stream7), stream8);
    }
}
