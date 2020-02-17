package com.qaprosoft.carina.core.foundation.webdriver.useragentparser;

import com.qaprosoft.carina.core.foundation.exception.NotSupportedOperationException;
import com.qaprosoft.carina.core.foundation.webdriver.useragentparser.browsers.Chrome;
import com.qaprosoft.carina.core.foundation.webdriver.useragentparser.browsers.*;

public class BrowserFactory {

    private BrowserFactory() {
    }

    // This could be handle as enums
    public static Browser create(String browserName) {
        switch (browserName) {
        case "Chrome":
            return new Chrome(browserName);
        case "Firefox":
            return new Firefox(browserName);
        case "IE":
            return new IExplorer(browserName);
        case "Safari":
            return new Safari(browserName);
        case "Opera":
            return new Opera(browserName);
        case "Edge":
            return new Edge(browserName);
        default:
            throw new NotSupportedOperationException();
        }
    }

}
