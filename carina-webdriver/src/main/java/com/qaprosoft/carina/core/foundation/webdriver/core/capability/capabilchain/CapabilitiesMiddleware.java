package com.qaprosoft.carina.core.foundation.webdriver.core.capability.capabilchain;

import java.lang.invoke.MethodHandles;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CapabilitiesMiddleware {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private CapabilitiesMiddleware next;

    public static CapabilitiesMiddleware link(CapabilitiesMiddleware first, CapabilitiesMiddleware... chain) {

        CapabilitiesMiddleware head = first;
        for (CapabilitiesMiddleware nextInChain : chain) {
            head.next = nextInChain;
            head = nextInChain;
        }
        return first;
    }

    protected abstract boolean isDetected(Capabilities capabilities);

    protected abstract MutableCapabilities upgradeCapabilities(MutableCapabilities capabilities);

    public MutableCapabilities analyze(MutableCapabilities capabilities) {
        if (next == null) {
            return capabilities;
        }

        if (isDetected(capabilities)) {
            return next.analyze(upgradeCapabilities(capabilities));
        }
        return next.analyze(capabilities);
    }
}
