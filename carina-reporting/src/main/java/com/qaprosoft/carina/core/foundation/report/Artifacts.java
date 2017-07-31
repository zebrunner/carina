/*
 * Copyright 2013-2015 QAPROSOFT (http://qaprosoft.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qaprosoft.carina.core.foundation.report;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.utils.Configuration.DriverMode;
import com.qaprosoft.zafira.models.dto.TestArtifactType;

/*
 * Link
 */
final public class Artifacts {
	protected static final Logger LOGGER = Logger.getLogger(Artifacts.class);
	private static final ThreadLocal<Set<TestArtifactType>> testArtifacts = ThreadLocal.withInitial(HashSet::new);
	private static final ThreadLocal<Set<TestArtifactType>> classArtifacts = ThreadLocal.withInitial(HashSet::new);
	private static final ThreadLocal<Set<TestArtifactType>> suiteArtifacts = ThreadLocal.withInitial(HashSet::new);

	public static void clearArtifacts() {
		clearArtifacts(DriverMode.METHOD_MODE);
	}

	public static void clearArtifacts(DriverMode mode) {
		switch (mode) {
		case METHOD_MODE:
			testArtifacts.remove();
			break;
		case CLASS_MODE:
			classArtifacts.remove();
			break;
		case SUITE_MODE:
			suiteArtifacts.remove();
			break;
		}
	}

	public synchronized static Set<TestArtifactType> getArtifacts() {
		Set<TestArtifactType> currentArtifacts = testArtifacts.get();
		currentArtifacts.addAll(classArtifacts.get());
		currentArtifacts.addAll(suiteArtifacts.get());
		return currentArtifacts;
	}

	public static void add(String name, String link) {
		add(name, link, null, DriverMode.METHOD_MODE);
	}

	public static void add(String name, String link, Date expires_at, DriverMode mode) {
		LOGGER.debug("Adding artifact name: " + name + "; link: " + link + "; expires_at: " + expires_at );
		if (name == null || name.isEmpty()) {
			return;
		}
		
		if (link == null || link.isEmpty()) {
			return;
		}
		
		TestArtifactType testArtifactType = new TestArtifactType(name, link, expires_at);
		switch (mode) {
		case METHOD_MODE:
			if (!testArtifacts.get().contains(testArtifactType)) {
				testArtifacts.get().add(new TestArtifactType(name, link, expires_at));
			}
			break;
		case CLASS_MODE:
			if (!classArtifacts.get().contains(testArtifactType)) {
				classArtifacts.get().add(new TestArtifactType(name, link, expires_at));
			}
			break;
		case SUITE_MODE:
			if (!suiteArtifacts.get().contains(testArtifactType)) {
				suiteArtifacts.get().add(new TestArtifactType(name, link, expires_at));
			}
			break;
		}
	}
}
