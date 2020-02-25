package com.qaprosoft.carina.core.foundation.webdriver.useragentparser;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public interface IParsable {

    /**
     * This contract is created to get the browser version from the
     * user agent response.
     * Some browser like IE, has customized this method.
     *
     * @param userAgent
     * @return String: Object()
     */
    default String getBrowserVersion(String userAgentKey, String userAgent) {
        Stream<String> streamVersion = Arrays.stream(userAgent.split(" "));
        Optional<String[]> version = streamVersion
                .filter(str -> Utils.isRequiredBrowser(userAgentKey, str))
                .findFirst().map(str -> Utils.splitVersion(str)[1].split("\\."));
        streamVersion.close();
        return Utils.buildBrowserVersion(version).replaceAll("[^\\d.]", "");
    }

}
