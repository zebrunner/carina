package com.zebrunner.carina.api.apitools.annotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum FirstElementDuplicateStrategy {

    ON_SAME_LEVEL(collector -> collector.values().stream()
            .takeWhile(items -> items.size() != 1)
            .filter(items -> items.size() > 1)
            .flatMap(Collection::stream)
            .collect(Collectors.toList())),
    ANY(collector -> {
        List<?> duplicates = collector.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        if (duplicates.size() <= 1) {
            duplicates = new ArrayList<>();
        }
        return duplicates;
    });

    private final Function<Map<?, List<?>>, List<?>> duplicatesRecognizer;

    FirstElementDuplicateStrategy(Function<Map<?, List<?>>, List<?>> duplicatesRecognizer) {
        this.duplicatesRecognizer = duplicatesRecognizer;
    }

    public Function<Map<?, List<?>>, List<?>> getDuplicatesRecognizer() {
        return duplicatesRecognizer;
    }
}
