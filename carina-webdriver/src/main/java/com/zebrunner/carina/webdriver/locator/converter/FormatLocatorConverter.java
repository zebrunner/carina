package com.zebrunner.carina.webdriver.locator.converter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FormatLocatorConverter implements LocatorConverter {
    private final List<Object> arguments;

    public FormatLocatorConverter(Object... objects) {
        arguments = Arrays.stream(objects)
                .collect(Collectors.toList());
    }

    @Override
    public String convert(String by) {
        return String.format(by, arguments.toArray());
    }
}
