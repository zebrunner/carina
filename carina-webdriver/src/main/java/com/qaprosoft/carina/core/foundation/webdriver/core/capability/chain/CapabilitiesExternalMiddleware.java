package com.qaprosoft.carina.core.foundation.webdriver.core.capability.chain;

import java.lang.invoke.MethodHandles;

import org.openqa.selenium.Capabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CapabilitiesExternalMiddleware {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private CapabilitiesExternalMiddleware next;

    public static CapabilitiesExternalMiddleware link(CapabilitiesExternalMiddleware first, CapabilitiesExternalMiddleware... chain) {

        CapabilitiesExternalMiddleware head = first;
        for (CapabilitiesExternalMiddleware nextInChain : chain) {
            head.next = nextInChain;
            head = nextInChain;
        }
        return first;
    }

    protected abstract boolean isDetected(Capabilities capabilities);

    protected abstract Capabilities upgradeCapabilities(Capabilities capabilities);

    public Capabilities analyze(Capabilities capabilities) {
        if (!isDetected(capabilities)) {
            if (next == null) {
                LOGGER.debug("Integrations was not detected");
                return capabilities;
            }
            return next.analyze(capabilities);
        }
        return upgradeCapabilities(capabilities);
    }
}
