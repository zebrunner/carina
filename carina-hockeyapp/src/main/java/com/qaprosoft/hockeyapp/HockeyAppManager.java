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
package com.qaprosoft.hockeyapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        // TODO: incorporate file exists and size verification to skip re download the same build artifacts

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
    private List<String> getAppId(String appName, String platformName) {

        List<String> appList = new ArrayList<String>();

        RequestEntity<String> retrieveApps = buildRequestEntity(
                HOCKEY_APP_URL,
                "/api/2/apps",
                HttpMethod.GET);
        JsonNode appResults = restTemplate.exchange(retrieveApps, JsonNode.class).getBody();

        for (JsonNode node : appResults.get("apps")) {
            if (platformName.equalsIgnoreCase(node.get("platform").asText())
                    && node.get("title").asText().toLowerCase().contains(appName.toLowerCase())) {
                LOGGER.info(String.format("Found App: %s (%s)", node.get("title"), node.get("public_identifier")));
                appList.add(node.get("public_identifier").asText());
            }
        }

        if (!appList.isEmpty()) {
            return appList;
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
    private String scanAppForBuild(List<String> appIds, String buildType, String version) {

        for (String appId : appIds) {
            MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
            queryParams.add("page", "1");
            queryParams.add("include_build_urls", "true");

            RequestEntity<String> retrieveBuilds = buildRequestEntity(
                    HOCKEY_APP_URL,
                    "api/2/apps/" + appId + "/app_versions",
                    queryParams,
                    HttpMethod.GET);

            JsonNode buildResults = restTemplate.exchange(retrieveBuilds, JsonNode.class).getBody();

            for (JsonNode node : buildResults.get("app_versions")) {
                if (checkBuild(version, node) && checkTitleForCorrectPattern(buildType.toLowerCase(), node) || checkNotesForCorrectBuild(buildType.toLowerCase(), node)) {
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

        if (version.equalsIgnoreCase(node.get("shortversion").asText())) {
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

        String nodeField = node.get(nodeName).asText().toLowerCase();

        if (nodeField.contains(pattern)) {
            return true;
        }

        String[] patternList = pattern.split("[^\\w']+");

        if (patternList.length <= 1) {
            throw new RuntimeException("Expected Multiple Word Pattern, Actual: " + pattern);
        }

        String patternToReplace = ".*[ -]%s[ -].*";
        String patternToMatch = String.format(patternToReplace, patternList[0]);
        String patternToMatchTwo = String.format(patternToReplace, patternList[1]);

        if (patternList.length > 1 && searchFieldsForString(patternToMatch, nodeField)
                && searchFieldsForString(patternToMatchTwo, nodeField)) {
            return true;
        }

        return false;
    }

    private boolean searchFieldsForString(String pattern, String stringToSearch) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(stringToSearch);

        return m.find();
    }
}
