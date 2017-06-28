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

import com.qaprosoft.zafira.models.dto.TestArtifactType;

/*
 * Link
 * 
 */
final public class Link
{
    private static final ThreadLocal<Set<TestArtifactType>> testLinks = ThreadLocal.withInitial(HashSet::new);

	public static void clearLinks() {
		testLinks.remove();
	}
	
	public synchronized static Set<TestArtifactType> getLinks()
	{
		return testLinks.get();
	}

	public static void addLink(String name, String link) {
		addLink(name, link, null);
	}
	
	public static void addLink(String name, String link, Date expires_at) {
		// TODO: organize valid constructor for below class
		TestArtifactType artifact = new TestArtifactType();
		artifact.setName(name);
		artifact.setLink(link);
		artifact.setExpiresAt(expires_at);
		
		testLinks.get().add(artifact);
	}
}
