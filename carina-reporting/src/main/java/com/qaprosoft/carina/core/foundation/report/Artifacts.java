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
package com.qaprosoft.carina.core.foundation.report;

import java.util.*;

import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.zafira.models.dto.TestArtifactType;

/**
 * Artifacts - represented by logs, screenshots, videos recorder by tests.
 * 
 * @author akhursevich
 */
final public class Artifacts {
	
	private static final Logger LOGGER = Logger.getLogger(Artifacts.class);
	
	private static final ThreadLocal<Set<TestArtifactType>> testArtifacts = ThreadLocal.withInitial(HashSet::new);

	public static void clearArtifacts() {
		testArtifacts.remove();
	}

	public synchronized static Set<TestArtifactType> getArtifacts() {
		return testArtifacts.get();
	}

	public static void add(String name, String link) {
		add(name, link, Configuration.getInt(Configuration.Parameter.ARTIFACTS_EXPIRATION_SECONDS));
	}

	/**
	 * Adds new artifact to test context.
	 * 
	 * @param name - artifact name: Log, Demo
	 * @param link - URL to the artifact
	 * @param expiresIn - expiration in seconds
	 */
	public static void add(String name, String link, Integer expiresIn) {
		LOGGER.debug("Adding artifact name: " + name + "; link: " + link + "; expiresIn: " + expiresIn);

		if (name == null || name.isEmpty()) {
			return;
		}

		if (link == null || link.isEmpty()) {
			return;
		}

		testArtifacts.get().add(new TestArtifactType(name, link, expiresIn));
	}
}
