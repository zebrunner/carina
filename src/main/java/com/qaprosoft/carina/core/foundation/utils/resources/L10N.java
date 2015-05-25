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
package com.qaprosoft.carina.core.foundation.utils.resources;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;

public class L10N {
	protected static final Logger LOGGER = Logger.getLogger(L10N.class);

	private static ArrayList<ResourceBundle> resBoundles = new ArrayList<ResourceBundle>();

	public static void init(Locale locale) {
		List<String> loadedResources = new ArrayList<String>();

		for (URL u : Resources.getResourceURLs(new ResourceURLFilter() {
			public @Override
			boolean accept(URL u) {
				String s = u.getPath();
				return s.contains(SpecialKeywords.L10N);
			}
		})) {
			LOGGER.debug(String.format(
					"Analyzing '%s' L10N resource for loading...", u));

			// workable examples for resource loading are
			// ResourceBundle.getBundle("L10N.messages", locale);
			// ResourceBundle.getBundle("L10N.system.data-access.resources.gwt.datasourceAdminDialog",
			// locale);

			/*
			 * 2. Exclude localization resources like such L10N.messages_de,
			 * L10N.messages_ptBR etc... Note: we ignore valid resources if 3rd
			 * or 5th char from the end is "_". As designed :(
			 */
			String fileName = FilenameUtils.getBaseName(u.getPath());

			if (u.getPath().endsWith("L10N.class") ||
					u.getPath().endsWith("L10N$1.class")) {
				//separate conditions to support core JUnit tests
				continue;
			}
				
			if (fileName.lastIndexOf('_') == fileName.length() - 3
					|| fileName.lastIndexOf('_') == fileName.length() - 5) {
				LOGGER.debug(String
						.format("'%s' resource IGNORED as it looks like localized resource!",
								fileName));
				continue;
			}
			/*
			 * convert "file:
			 * E:\pentaho\qa-automation\target\classes\L10N\messages.properties"
			 * to "L10N.messages"
			 */
			String filePath = FilenameUtils.getPath(u.getPath());
			String resource = filePath.substring(
					filePath.indexOf(SpecialKeywords.L10N))
					.replaceAll("/", ".")
					+ fileName;

			if (!loadedResources.contains(resource)) {
				loadedResources.add(resource);
				try {
					LOGGER.debug(String.format("Adding '%s' resource...",
							resource));
					resBoundles.add(ResourceBundle.getBundle(resource, locale));
					LOGGER.debug(String
							.format("Resource '%s' added.", resource));
				} catch (MissingResourceException e) {
					LOGGER.debug(e);
				}
			} else {
				LOGGER.debug(String
						.format("Requested resource '%s' is already loaded into the ResourceBundle!",
								resource));
			}
		}
		
		LOGGER.debug("init: L10N bundle size: " + resBoundles.size());
	}

	public static String getText(String key) {
		LOGGER.debug("getText: L10N bundle size: " + resBoundles.size());
		Iterator<ResourceBundle> iter = resBoundles.iterator();
		while (iter.hasNext()) {
			ResourceBundle bundle = (ResourceBundle) iter.next();
			try {
				String value = bundle.getString(key);
				return value;
			} catch (MissingResourceException e) {
				// do nothing
			}
		}
		return key;
	}

}