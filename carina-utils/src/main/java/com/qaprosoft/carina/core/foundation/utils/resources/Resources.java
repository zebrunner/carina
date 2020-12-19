/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.core.foundation.utils.resources;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Resources {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static void collectURL(ResourceURLFilter f, Set<URL> s, URL u) {
        if (f == null || f.accept(u)) {
            LOGGER.debug("adding resource url by filter: " + u);
            s.add(u);
        }
    }

    private static void iterateFileSystem(File r, ResourceURLFilter f,
            Set<URL> s) {
        File[] files = r.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                iterateFileSystem(file, f, s);
            } else if (file.isFile()) {
                try {
                    collectURL(f, s, file.toURI().toURL());
                } catch (MalformedURLException e) {
                    LOGGER.debug(e.getMessage(), e);
                }
            }
        }
    }

    private static void iterateEntry(File p, ResourceURLFilter f, Set<URL> s) {
        if (p.isDirectory()) {
            iterateFileSystem(p, f, s);
        }
    }

    // To scan the entire class path and return all its resources as URLs
    public static Set<URL> getResourceURLs() {
        return getResourceURLs((ResourceURLFilter) null);
    }

    // To scan the class path starting with the location from which a specific
    // class was loaded, provide the getResourceURLs method with the root-class
    @SuppressWarnings("rawtypes")
    public static Set<URL> getResourceURLs(Class rootClass) {
        return getResourceURLs(rootClass, null);
    }

    public static Set<URL> getResourceURLs(ResourceURLFilter filter) {
        Set<URL> collectedURLs = new HashSet<>();
        try(URLClassLoader ucl = new URLClassLoader(new URL[] {(ClassLoader.getSystemClassLoader()).getResource("L10N")}, Resources.class.getClassLoader())) {
            for (URL url : ucl.getURLs()) {
                try {
                    iterateEntry(new File(url.toURI()), filter, collectedURLs);
                } catch (URISyntaxException e) {
                    LOGGER.debug(e.getMessage(), e);
                }
            }
            return collectedURLs;
        } catch (IOException e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return collectedURLs;
    }

    @SuppressWarnings("rawtypes")
    public static Set<URL> getResourceURLs(Class rootClass,
            ResourceURLFilter filter) {
        Set<URL> collectedURLs = new HashSet<>();
        CodeSource src = rootClass.getProtectionDomain().getCodeSource();
        try {
            iterateEntry(new File(src.getLocation().toURI()), filter,
                    collectedURLs);
        } catch (URISyntaxException e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return collectedURLs;
    }
}