package org.endeavourhealth.sftpreader.utilities;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamExtension
{
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

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map)
    {
        Map<K, V> result = new LinkedHashMap<>();
        Stream<Map.Entry<K, V>> st = map.entrySet().stream();

        st.sorted(Map.Entry.comparingByValue())
                .forEachOrdered( e -> result.put(e.getKey(), e.getValue()) );

        return result;
    }
}
