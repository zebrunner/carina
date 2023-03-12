/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
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
package com.zebrunner.carina.utils.appletv;

import java.lang.invoke.MethodHandles;

import org.openqa.selenium.JavascriptExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.zebrunner.carina.webdriver.IDriverPool;

public interface IRemoteControllerAppleTV extends IDriverPool {
	static final Logger RC_LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Click the button as on the remote control
	 * 
	 * @param controlKeyword RemoteControlKeyword
	 */
	default public void remoteControlAction(RemoteControlKeyword controlKeyword) {
		((JavascriptExecutor) getDriver()).executeScript("mobile: pressButton", ImmutableMap.of("name", controlKeyword.getControlKeyword()));
		RC_LOGGER.info(String.format("TV OS RemoteController '%s' clicked", controlKeyword.name()));
	}

	/**
	 * Click the 'Home' button as on the remote control
	 *
	 */
	default public void clickHome() {
		remoteControlAction(RemoteControlKeyword.HOME);
	}

	/**
	 * Click the 'Left' button as on the remote control
	 *
	 */
	default public void clickLeft() {
		remoteControlAction(RemoteControlKeyword.LEFT);
	}

	/**
	 * Click the 'Right' button as on the remote control
	 *
	 */
	default public void clickRight() {
		remoteControlAction(RemoteControlKeyword.RIGHT);
	}

	/**
	 * Click the 'Up' button as on the remote control
	 *
	 */
	default public void clickUp() {
		remoteControlAction(RemoteControlKeyword.UP);
	}

	/**
	 * Click the 'Down' button as on the remote control
	 *
	 */
	default public void clickDown() {
		remoteControlAction(RemoteControlKeyword.DOWN);
	}

	/**
	 * Click the 'Menu' button as on the remote control
	 *
	 */
	default public void clickMenu() {
		remoteControlAction(RemoteControlKeyword.MENU);
	}

	/**
	 * Click the 'Back' button as on the remote control
	 *
	 */
	default public void clickBack() {
		clickMenu();
	}

	/**
	 * Click the 'Select' button as on the remote control
	 *
	 */
	default public void clickSelect() {
		remoteControlAction(RemoteControlKeyword.SELECT);
	}

	/**
	 * Click the 'Play/pause' button as on the remote control
	 *
	 */
	default public void clickPlay() {
		remoteControlAction(RemoteControlKeyword.PLAY);
	}
}