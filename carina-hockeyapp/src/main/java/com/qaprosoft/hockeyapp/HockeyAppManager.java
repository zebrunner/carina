/*******************************************************************************
 * Copyright 2013-2018 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

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
    
    private static final String hockeyAppUrl = "rink.hockeyapp.net";
    
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
     * @param version takes in either "latest" to take the first build that matches the criteria or allows to consume a version to download that build.
     * @return file to the downloaded build artifact
     */
    public File getBuild(String folder, String appName, String platformName, String buildType, String version) {
        disableRestTemplateSsl();

        String buildToDownload = scanAppForBuild(getAppId(appName, platformName), buildType, version);

        String fileName = folder + "/" + createFileName(appName, buildType,platformName);
        
        //TODO: incorporate file exists and size verification to skip re download the same build artifacts

        try {
            LOGGER.debug("Beginning Transfer of HockeyApp Build");
            URL downloadLink = new URL(buildToDownload);
            ReadableByteChannel readableByteChannel = Channels.newChannel(downloadLink.openStream());
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.getChannel().transferFrom(readableByteChannel, 0 , Long.MAX_VALUE);
            fos.close();
            readableByteChannel.close();
            LOGGER.debug(String.format("HockeyApp Build (%s) was retrieved", fileName));
        } catch (Exception ex) {
            LOGGER.error(String.format("Error Thrown When Attempting to Transfer HockeyApp Build (%s)", ex.getMessage()), ex);
        }
        
        return new File(fileName);

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
        		hockeyAppUrl,
                "/api/2/apps",
                HttpMethod.GET);
        JsonNode appResults = restTemplate.exchange(retrieveApps, JsonNode.class).getBody();

        for (JsonNode node : appResults.get("apps")) {
            if (platformName.equalsIgnoreCase(node.get("platform").asText()) && node.get("title").asText().toLowerCase().contains(appName.toLowerCase())) {
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
     * @param version takes in either "latest" to take the first build that matches the criteria or allows to consume a version to download that build.
     * @return
     */
    private String scanAppForBuild(List<String> appIds, String buildType, String version) {

        for (String appId : appIds) {
            MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
            queryParams.add("page", "1");
            queryParams.add("include_build_urls", "true");

            RequestEntity<String> retrieveBuilds = buildRequestEntity(
            		hockeyAppUrl,
                    "api/2/apps/" + appId + "/app_versions",
                    queryParams,
                    HttpMethod.GET
            );

            JsonNode buildResults = restTemplate.exchange(retrieveBuilds, JsonNode.class).getBody();

            for (JsonNode node : buildResults.get("app_versions")) {
                if (checkNotesForCorrectBuild(buildType.toLowerCase(), node) && checkBuild(version, node)) {
                    LOGGER.info("Downloading Version: " + node);
                    versionNumber = node.get("shortversion").asText();
                    revision = node.get("version").asText();

                    return node.get("build_url").asText();
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

        if (platformName.toLowerCase().contains("ios")) {
            return fileName + ".ipa";
        }
        return fileName + ".apk";
    }

    private boolean checkNotesForCorrectBuild(String pattern, JsonNode node) {

        String noteField = node.get("notes").asText().toLowerCase();

        if (noteField.contains(pattern)) {
            return true;
        }

        String[] patternList = pattern.split("[^\\w']+");

        if (patternList.length > 1 && noteField.contains(patternList[0]) && noteField.contains(patternList[1])) {
            return true;
        }

        return false;
    }
}
