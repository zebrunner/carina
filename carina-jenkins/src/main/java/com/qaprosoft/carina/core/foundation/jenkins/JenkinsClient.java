package com.qaprosoft.carina.core.foundation.jenkins;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.restassured.path.xml.XmlPath;

public class JenkinsClient {
    private static final Logger LOGGER = Logger.getLogger(JenkinsClient.class);

    private static final String JOB = "%s/job/%s/%s/console";
    private static final String JOB_API = "%s/job/%s/api/xml?depth=1";

    private String jenkinsURL;

    public JenkinsClient(String jenkinsURL) {
        setJenkinsURL(jenkinsURL);
    }

    public String getCurrentJobURL(String job) {
        String url = null;
        try {
            URL obj = new URL(String.format(JOB_API, jenkinsURL, job));
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            try {
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            } catch (Exception e) {
                LOGGER.debug("Error during FileWriter append. " + e.getMessage(), e.getCause());
            } finally {
                try {
                    in.close();
                } catch (Exception e) {
                    LOGGER.debug("Error during FileWriter close. " + e.getMessage(), e.getCause());
                }

            }

            XmlPath xmlPath = new XmlPath(response.toString());
            if (xmlPath.getBoolean("freeStyleProject.lastBuild.building")) {
                url = String.format(JOB, jenkinsURL, job, xmlPath.getString("freeStyleProject.lastBuild.number").trim());
            }
        } catch (Exception e) {
            url = "";
            LOGGER.error(e.getMessage());
        }
        return url;
    }

    public String getJenkinsURL() {
        return jenkinsURL;
    }

    public void setJenkinsURL(String jenkinsURL) {
        this.jenkinsURL = !StringUtils.isEmpty(jenkinsURL) ? StringUtils.removeEnd(jenkinsURL, "/") : jenkinsURL;
    }
}
