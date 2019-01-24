/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.GridRegistry;
import org.openqa.grid.internal.TestSession;
import org.openqa.grid.internal.TestSlot;
import org.openqa.grid.selenium.proxy.DefaultRemoteProxy;

import com.qaprosoft.carina.grid.integration.STF;
import com.qaprosoft.zafira.models.stf.STFDevice;

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
        
        String udid = String.valueOf(session.getSlot().getCapabilities().get("udid"));
        if (STF.isSTFRequired(session.getSlot().getCapabilities(), session.getRequestedCapabilities())) {
            STF.reserveDevice(udid);
            //session.getRequestedCapabilities().put("slotCapabilities", getSlotCapabilities(session, udid));
        }
        
        if (!StringUtils.isEmpty(udid)) {
            	// this is our mobile Android or iOS device
            	session.getRequestedCapabilities().put("slotCapabilities", getSlotCapabilities(session, udid));
        }
        
    }

    @Override
    public void afterSession(TestSession session) {
        super.afterSession(session);
        if (STF.isSTFRequired(session.getSlot().getCapabilities(), session.getRequestedCapabilities())) {
            STF.returnDevice(String.valueOf(session.getSlot().getCapabilities().get("udid")));
        }
    }
    
	private Map<String, Object> getSlotCapabilities(TestSession session, String udid) {
		//obligatory create new map as original object is UnmodifiableMap
		Map<String, Object> slotCapabilities = new HashMap<String, Object>();
		
		// get existing slot capabilities from session
		slotCapabilities.putAll(session.getSlot().getCapabilities());

		if (STF.isSTFRequired(session.getSlot().getCapabilities(), session.getRequestedCapabilities())) {
			// get remoteURL from STF device and add into custom slotCapabilities map
			String remoteURL = null;
			STFDevice stfDevice = STF.getDevice(udid);
			if (stfDevice != null) {
				LOGGER.fine("Identified '" + stfDevice.getModel() + "' device by udid: " + udid);
				remoteURL = (String) stfDevice.getRemoteConnectUrl();
				LOGGER.fine("Identified remoteURL '" + remoteURL + "' by udid: " + udid);
				slotCapabilities.put("remoteURL", remoteURL);
			}
		}

		return slotCapabilities;
	}

}