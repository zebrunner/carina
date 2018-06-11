/*******************************************************************************
 * Copyright 2013-2018 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.carina.grid;

import java.util.Map;
import java.util.logging.Logger;

import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.GridRegistry;
import org.openqa.grid.internal.TestSession;
import org.openqa.grid.internal.TestSlot;
import org.openqa.grid.selenium.proxy.DefaultRemoteProxy;

import com.qaprosoft.carina.grid.integration.STF;

/**
 * Mobile proxy that connects/disconnects STF devices.
 * 
 * @author Alex Khursevich (alex@qaprosoft.com)
 */
public class MobileRemoteProxy extends DefaultRemoteProxy {

    private static final Logger LOGGER = Logger.getLogger(MobileRemoteProxy.class.getName());

    public MobileRemoteProxy(RegistrationRequest request, GridRegistry registry) {
        super(request, registry);
    }

    @Override
    public TestSession getNewSession(Map<String, Object> requestedCapability) {

        LOGGER.fine("Trying to create a new session on node " + this);

        if (isDown()) {
            return null;
        }

        if (!hasCapability(requestedCapability)) {
            LOGGER.fine("Node " + this + " has no matching capability");
            return null;
        }

        // any slot left at all?
        if (getTotalUsed() >= config.maxSession) {
            LOGGER.fine("Node " + this + " has no free slots");
            return null;
        }

        // any slot left for the given app ?
        for (TestSlot testslot : getTestSlots()) {

            // Check if device is busy in STF
            if (STF.isSTFRequired(testslot.getCapabilities(), requestedCapability)
                    && !STF.isDeviceAvailable((String) testslot.getCapabilities().get("udid"))) {
                return null;
            }

            TestSession session = testslot.getNewSession(requestedCapability);

            if (session != null) {
                return session;
            }
        }
        return null;
    }

    @Override
    public void beforeSession(TestSession session) {
        super.beforeSession(session);
        if (STF.isSTFRequired(session.getSlot().getCapabilities(), session.getRequestedCapabilities())) {
            STF.reserveDevice(String.valueOf(session.getSlot().getCapabilities().get("udid")));
        }
    }

    @Override
    public void afterSession(TestSession session) {
        super.afterSession(session);
        if (STF.isSTFRequired(session.getSlot().getCapabilities(), session.getRequestedCapabilities())) {
            STF.returnDevice(String.valueOf(session.getSlot().getCapabilities().get("udid")));
        }
    }
}