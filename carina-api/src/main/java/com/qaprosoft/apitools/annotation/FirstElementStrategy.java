package com.qaprosoft.apitools.annotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum FirstElementStrategy {

    ON_SAME_LEVEL(collector -> collector.values().stream()
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

    FirstElementStrategy(Function<Map<?, List<?>>, List<?>> duplicatesRecognizer) {
        this.duplicatesRecognizer = duplicatesRecognizer;
    }

    public Function<Map<?, List<?>>, List<?>> getDuplicatesRecognizer() {
        return duplicatesRecognizer;
    }
}
