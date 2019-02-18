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
package com.qaprosoft.hockeyapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.hockeyapp.http.resttemplate.RestTemplateBuilder;

/**
 * Created by boyle on 8/16/17.
 */
public class HockeyAppManager {
    private static final Logger LOGGER = Logger.getLogger(HockeyAppManager.class);
    protected RestTemplate restTemplate;

    private String revision;
    private String versionNumber;

    private static final String HOCKEY_APP_URL = "rink.hockeyapp.net";

    private static volatile HockeyAppManager instance = null;

    private HockeyAppManager() {
    }

    public static HockeyAppManager getInstance() {
        if (instance == null) {
            synchronized (HockeyAppManager.class) {
                if (instance == null) {
                    instance = new HockeyAppManager();
                }
            }
        }
        return instance;
    }

    private void disableRestTemplateSsl() {
        restTemplate = RestTemplateBuilder.newInstance().withDisabledSslChecking().withSpecificJsonMessageConverter().build();
    }

    /**
     *
     * @param folder to which upload build artifact.
     * @param appName takes in the HockeyApp Name tЩo look for.
     * @param platformName takes in the platform we wish to download for.
     * @param buildType takes in the particular build to download (i.e. Prod.AdHoc, QA.Debug, Prod-Release, QA-Internal etc...)
     * @param version takes in either "latest" to take the first build that matches the criteria or allows to consume a version to download that
     *            build.
     * @return file to the downloaded build artifact
     */
    public File getBuild(String folder, String appName, String platformName, String buildType, String version) {
        disableRestTemplateSsl();

        String buildToDownload = scanAppForBuild(getAppId(appName, platformName), buildType, version);

        if (!buildToDownload.contains("api")) {
            buildToDownload = new StringBuilder(buildToDownload).insert(buildToDownload.indexOf("/apps"), "/api/2").toString()
                    + "?format=" + returnProperPlatformExtension(platformName);
        }

        String fileName = folder + "/" + createFileName(appName, buildType, platformName);
        File fileToLocate = null;

        //TODO:  Make sure to use the correct paths here
        try {
            File file = new File(folder);
            File[] listOfFiles = file.listFiles();

            for(int i = 0; i < listOfFiles.length; ++i){
                if(listOfFiles[i].isFile() && listOfFiles[i].getName().contains(fileName)){
                    LOGGER.info("File has been Located Locally.  File path is: " + listOfFiles[i].getAbsolutePath());
                    fileToLocate = listOfFiles[i];
                }
            }
        } catch (Exception ex) {
            LOGGER.error(String.format("Error Attempting to Look for Existing File: %s", ex.getMessage()), ex);
        }

        if (fileToLocate == null) {
            try {
                LOGGER.debug("Beginning Transfer of HockeyApp Build");
                URL downloadLink = new URL(buildToDownload);
                int retryCount = 0;
                boolean retry = true;
                while (retry && retryCount <= 5) {
                    retry = downloadBuild(fileName, downloadLink);
                    retryCount = retryCount + 1;
                }
                LOGGER.debug(String.format("HockeyApp Build (%s) was retrieved", fileName));
            } catch (Exception ex) {
                LOGGER.error(String.format("Error Thrown When Attempting to Transfer HockeyApp Build (%s)", ex.getMessage()), ex);
            }
        } else {
            LOGGER.info("Preparing to use local version of HockeyApp Build...");
        }

        return new File(fileName);
    }

    /**
     *
     * @param fileName will be the name of the downloaded file.
     * @param downloadLink will be the URL to retrieve the build from.
     * @return brings back a true/false on whether or not the build was successfully downloaded.
     * @throws IOException throws a non Interruption Exception up.
     */
    private boolean downloadBuild(String fileName, URL downloadLink) throws IOException {
        ReadableByteChannel readableByteChannel = null;
        FileOutputStream fos = null;
        try {
            if (Thread.currentThread().isInterrupted()) {
                LOGGER.debug(String.format("Current Thread (%s) is interrupted, clearing interruption.", Thread.currentThread().getId()));
                Thread.interrupted();
            }
            readableByteChannel = Channels.newChannel(downloadLink.openStream());
            fos = new FileOutputStream(fileName);
            fos.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            LOGGER.info("Successfully Transferred...");
            return false;
        } catch (ClosedByInterruptException ie1) {
            LOGGER.info("Retrying....");
            LOGGER.error("Getting Error: " + ie1.getMessage(), ie1);
            return true;
        } finally {
            if (fos != null) {
                fos.close();
            }
            if (readableByteChannel != null) {
                readableByteChannel.close();
            }
        }
    }

    /**
     *
     * @param appName takes in the HockeyApp Name to look for.
     * @param platformName takes in the platform we wish to download for.
     * @return
     */
    private Map<String, String> getAppId(String appName, String platformName) {

        Map<String, String> appMap = new HashMap<>();

        RequestEntity<String> retrieveApps = buildRequestEntity(
                HOCKEY_APP_URL,
                "/api/2/apps",
                HttpMethod.GET);
        JsonNode appResults = restTemplate.exchange(retrieveApps, JsonNode.class).getBody();

        for (JsonNode node : appResults.get("apps")) {
            if (platformName.equalsIgnoreCase(node.get("platform").asText())
                    && node.get("title").asText().toLowerCase().contains(appName.toLowerCase())) {
                LOGGER.info(String.format("Found App: %s (%s)", node.get("title"), node.get("public_identifier")));
                appMap.put(node.get("public_identifier").asText(), node.get("updated_at").asText());
            }
        }

        if (!appMap.isEmpty()) {
            Map<String, String> sortedMap = appMap.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            return sortedMap;
        }

        throw new RuntimeException(String.format("Application Not Found in HockeyApp for Name (%s), Platform (%s)", appName, platformName));
    }

    /**
     *
     * @param appIds takes in the application Ids
     * @param buildType takes in the particular build to download (i.e. Prod.AdHoc, QA.Debug, Prod-Release, QA-Internal etc...)
     * @param version takes in either "latest" to take the first build that matches the criteria or allows to consume a version to download that
     *            build.
     * @return
     */
    private String scanAppForBuild(Map<String, String> appIds, String buildType, String version) {

        for (String appId : appIds.keySet()) {
            LOGGER.info("\nHockeyApp Id: " + appId);
            MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
            queryParams.add("page", "1");
            queryParams.add("include_build_urls", "true");
            queryParams.add("per_page", "50");

            RequestEntity<String> retrieveBuilds = buildRequestEntity(
                    HOCKEY_APP_URL,
                    "api/2/apps/" + appId + "/app_versions",
                    queryParams,
                    HttpMethod.GET);

            JsonNode buildResults = restTemplate.exchange(retrieveBuilds, JsonNode.class).getBody();

            for (JsonNode node : buildResults.get("app_versions")) {
                if (checkBuild(version, node) && (checkTitleForCorrectPattern(buildType.toLowerCase(), node) || checkNotesForCorrectBuild(buildType.toLowerCase(), node))) {
                    LOGGER.info("Downloading Version: " + node);
                    versionNumber = node.get("shortversion").asText();
                    revision = node.get("version").asText();

                    List<String> packageUrls = new ArrayList<>();
                    packageUrls.add("build_url");
                    packageUrls.add("download_url");

                    for (String packageUrl : packageUrls) {
                        if (node.has(packageUrl)) {
                            return node.get(packageUrl).asText();
                        }
                    }
                }
            }
        }

        throw new RuntimeException(String.format("Unable to find build to download, version provided (%s)", version));
    }

    private boolean checkBuild(String version, JsonNode node) {

        if ("latest".equalsIgnoreCase(version)) {
            return true;
        }

        if (version.equalsIgnoreCase(node.get("shortversion").asText() + "." + node.get("version").asText()) || version.equalsIgnoreCase(node.get("shortversion").asText())) {
            return true;
        }
        return false;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private RequestEntity<String> buildRequestEntity(String hostUrl, String path,
                                                     HttpMethod httpMethod) {

        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(hostUrl)
                .path(path)
                .build();

        return new RequestEntity(setHeaders(), httpMethod, uriComponents.toUri());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private RequestEntity<String> buildRequestEntity(String hostUrl, String path,
                                                     MultiValueMap<String, String> listQueryParams, HttpMethod httpMethod) {

        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(hostUrl)
                .path(path)
                .queryParams(listQueryParams)
                .build();

        return new RequestEntity(setHeaders(), httpMethod, uriComponents.toUri());
    }

    private HttpHeaders setHeaders() {

        HttpHeaders httpHeader = new HttpHeaders();
        httpHeader.add("X-HockeyAppToken", Configuration.get(Parameter.HOCKEYAPP_TOKEN));

        return httpHeader;
    }

    private String createFileName(String appName, String buildType, String platformName) {

        String fileName = String.format("%s.%s.%s.%s", appName, buildType, versionNumber, revision)
                .replace(" ", "");

        return fileName + "." + returnProperPlatformExtension(platformName);
    }

    private String returnProperPlatformExtension(String platformName) {

        if (platformName.toLowerCase().contains("ios")) {
            return "ipa";
        }
        return "apk";
    }

    private boolean checkNotesForCorrectBuild(String pattern, JsonNode node) {

        return checkForPattern("notes", pattern, node);
    }

    private boolean checkTitleForCorrectPattern(String pattern, JsonNode node) {

        return checkForPattern("title", pattern, node);
    }

    private boolean checkForPattern(String nodeName, String pattern, JsonNode node) {

        LOGGER.debug("\nPattern to be checked: " + pattern);
        String nodeField = node.get(nodeName).asText().toLowerCase();

        if (nodeField.contains(pattern)) {
            LOGGER.info("\nPattern match found!! This is the buildType to be used: " + nodeField);
            return true;
        }

//        String[] patternList = pattern.split("[^\\w']+");

//        if (patternList.length <= 1) {
//            throw new RuntimeException("Expected Multiple Word Pattern, Actual: " + pattern);
//        }

//        List<String> updatedPatternlist = new ArrayList<>();

//        String patternToReplace = ".*[ ->\\S]%s[ -<\\S].*";
//        for (String currentPattern : patternList) {
//            updatedPatternlist.add(String.format(patternToReplace, currentPattern));
//        }

//        for (String str : updatedPatternlist) {
//            LOGGER.info("Updated Pattern List, pattern: " + str);
//        }
        String patternToReplace = ".*[ ->\\S]%s[ -<\\S].*";

//        if (patternList.length > 1 && scanningAllNotes(Arrays.asList(patternList), nodeField)) {
//            return true;
//        }
        if (!pattern.isEmpty() && scanningAllNotes(String.format(patternToReplace, pattern), nodeField)){
            return true;
        }

        return false;
    }

    private boolean searchFieldsForString(String pattern, String stringToSearch) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(stringToSearch);

        return m.find();
    }

    private boolean scanningAllNotes(String pattern, String noteField) {
        boolean foundMessages = false;

//        LOGGER.debug(String.format("Message to Scan: %s", noteField));
//        for (String pattern : patternList) {
//            foundMessages &= searchFieldsForString(pattern, noteField);
//            LOGGER.debug(String.format("Checking Found Messages for (%s).  Initial Result (%s), Full Reset (%s)", pattern, searchFieldsForString(pattern, noteField), foundMessages));
//        }
        foundMessages = searchFieldsForString(pattern, noteField);

        return foundMessages;
    }
}
