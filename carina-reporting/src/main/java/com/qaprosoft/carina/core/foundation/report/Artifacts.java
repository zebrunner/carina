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

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.qaprosoft.amazon.client.AmazonS3Client;
import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.zafira.models.dto.TestArtifactType;
import org.springframework.util.StringUtils;

/**
 * Artifacts - represented by logs, screenshots, videos recorder by tests.
 *
 * @author akhursevich
 */
final public class Artifacts {

	private static final Logger LOGGER = Logger.getLogger(Artifacts.class);

	private static final ThreadLocal<Set<TestArtifactType>> testArtifacts = ThreadLocal.withInitial(HashSet::new);
	private static final ThreadLocal<Map<String, Set<AsyncArtifact>>> testArtifactsAsync = ThreadLocal.withInitial(HashMap::new);

	public static void clearArtifacts() {
		testArtifacts.remove();
        testArtifactsAsync.remove();
	}

	public synchronized static Set<TestArtifactType> getArtifacts() {
		Set<TestArtifactType> artifacts = testArtifacts.get();
		artifacts.addAll(getArtifactsAsync());
		return artifacts;
	}

    private synchronized static Set<TestArtifactType> getArtifactsAsync() {
        Map<String, Set<AsyncArtifact>> asyncArtifacts = testArtifactsAsync.get();
        return asyncArtifacts.keySet().stream().map(correlationId -> {
            Set<AsyncArtifact> processingArtifacts = asyncArtifacts.get(correlationId);
            if (!processingArtifacts.isEmpty()) {
                AsyncArtifact af = processingArtifacts.iterator().next();
                String joinedUrl = getJoinedUrl(processingArtifacts);
                return new TestArtifactType(af.getName(), joinedUrl, af.getExpiresIn());
            }
            return null;
        }).filter(testArtifact -> testArtifact != null && testArtifact.getLink() != null)
                             .collect(Collectors.toSet());
    }

	public static void add(String name, String link) {
		add(name, link, getArtifactExpirationSeconds());
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

	public static void add(String name, File file) {
		add(name, file, getArtifactExpirationSeconds());
	}

	public static void add(String name, File file, Integer expiresIn) {
		AmazonS3Client.upload(file).ifPresent(urlFuture -> add(urlFuture, name, expiresIn));
	}

	private static int getArtifactExpirationSeconds() {
		return Configuration.getInt(Configuration.Parameter.ARTIFACTS_EXPIRATION_SECONDS);
	}

	public static void add(CompletableFuture<String> urlFuture, String name, String correlationId) {
		add(urlFuture, name, getArtifactExpirationSeconds(), correlationId);
	}

	private static void add(CompletableFuture<String> urlFuture, String name, Integer expiresIn) {
		add(new AsyncArtifact(urlFuture, name, expiresIn), null);
	}

	private static void add(CompletableFuture<String> urlFuture, String name, Integer expiresIn, String correlationId) {
		add(new AsyncArtifact(urlFuture, name, expiresIn), correlationId);
	}

	private static void add(AsyncArtifact asyncArtifact, String correlationId) {
		LOGGER.debug("Adding async artifact");
		if(correlationId == null) {
			correlationId = UUID.randomUUID().toString();
		}
		Map<String, Set<AsyncArtifact>> asyncArtifacts = testArtifactsAsync.get();
		asyncArtifacts.computeIfAbsent(correlationId, k -> new HashSet<>());
		testArtifactsAsync.get().get(correlationId).add(asyncArtifact);
	}

	private static String getJoinedUrl(Set<AsyncArtifact> artifacts) {
		String joinedUrl = artifacts.stream().map(asyncArtifact -> {
			String url = null;
			try {
				url = asyncArtifact.getUrlFuture().get();
			} catch (InterruptedException | ExecutionException e) {
				LOGGER.error(e.getMessage(), e);
			}
			return url;
		}).filter(Objects::nonNull).collect(Collectors.joining(" "));
		return StringUtils.isEmpty(joinedUrl) ? null : joinedUrl;
	}
}
