package com.zebrunner.carina.core.registrar.artifacts;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.decorators.Decorated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.zebrunner.agent.core.registrar.Artifact;

public class ArtifactsHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final String ARTIFACTS_FOLDER = "downloads"; // renamed to downloads to avoid automatic upload on our old Zebrunner ci-pipeline
    // versions

    public static synchronized File getArtifactsFolder() {
        File dir = null;
        try {
            // artifacts directory should use canonical path otherwise auto download feature is broken in browsers
            if (!Configuration.get(Configuration.Parameter.CUSTOM_ARTIFACTS_FOLDER).isEmpty()) {
                dir = new File(Configuration.get(Configuration.Parameter.CUSTOM_ARTIFACTS_FOLDER)).getCanonicalFile();
            } else {
                dir = new File(ReportContext.getTestDir().getCanonicalPath() + File.separator + ARTIFACTS_FOLDER);
            }

            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    throw new RuntimeException("Artifacts folder not created: " + dir.getAbsolutePath());
                } else {
                    LOGGER.debug(("Artifacts folder created: " + dir.getAbsolutePath()));
                }
            } else {
                LOGGER.debug("Artifacts folder already exists: " + dir.getAbsolutePath());
            }

            if (!dir.isDirectory()) {
                throw new RuntimeException("Artifacts folder is not a folder: " + dir.getAbsolutePath());
            }

        } catch (IOException e) {
            throw new RuntimeException("Artifacts folder not created!");
        }
        return dir;
    }

    /**
     * Returns consolidated list of auto downloaded filenames from local artifacts folder or from remote Selenium session
     *
     * @param driver WebDriver
     * @return list of file and directories names
     */
    public static List<String> listArtifacts(WebDriver driver) {
        List<String> artifactNames = Arrays.stream(Objects.requireNonNull(getArtifactsFolder().listFiles()))
                .map(File::getName)
                .collect(Collectors.toList());

        String hostUrl = getUrl(driver, "");
        String username = getField(hostUrl, 1);
        String password = getField(hostUrl, 2);

        try {
            HttpURLConnection con = (HttpURLConnection) new URL(hostUrl).openConnection();
            con.setInstanceFollowRedirects(true); // explicitly define as true because default value doesn't work and return 301 status
            con.setRequestMethod("GET");

            if (!username.isEmpty() && !password.isEmpty()) {
                String usernameColonPassword = username + ":" + password;
                String basicAuthPayload = "Basic " + Base64.getEncoder().encodeToString(usernameColonPassword.getBytes());
                con.addRequestProperty("Authorization", basicAuthPayload);
            }

            int responseCode = con.getResponseCode();
            String responseBody = readStream(con.getInputStream());
            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND &&
                    responseBody.contains("\"error\":\"invalid session id\",\"message\":\"unknown session")) {
                throw new RuntimeException("Invalid session id. Something wrong with driver");
            }

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String hrefAttributePattern = "href=([\"'])((?:(?!\\1)[^\\\\]|(?:\\\\\\\\)*\\\\[^\\\\])*)\\1";
                Pattern pattern = Pattern.compile(hrefAttributePattern);
                Matcher matcher = pattern.matcher(responseBody);
                while (matcher.find()) {
                    if (!artifactNames.contains(matcher.group(2))) {
                        artifactNames.add(matcher.group(2));
                    }
                }
            }

        } catch (IOException e) {
            LOGGER.debug("Something went wrong when try to get artifacts from remote", e);
        }

        return artifactNames;
    }

    /**
     * Get artifacts from auto download folder of local or remove driver session by pattern
     *
     * @param driver WebDriver
     * @param pattern String - regex for artifacts
     * @return list of artifact files
     */
    public static List<File> getArtifacts(WebDriver driver, String pattern) {
        List<String> filteredFilesNames = listArtifacts(driver)
                .stream()
                // ignore directories
                .filter(fileName -> !fileName.endsWith("/"))
                .filter(fileName -> fileName.matches(pattern))
                .collect(Collectors.toList());

        List<File> artifacts = new ArrayList<>();

        for (String fileName : filteredFilesNames) {
            artifacts
                    .add(getArtifact(driver, fileName));
        }
        return artifacts;
    }

    /**
     * Get artifact from auto download folder of local or remove driver session by name
     *
     * @param driver WebDriver
     * @param name String - filename with extension
     * @return artifact File
     */
    public static File getArtifact(WebDriver driver, String name) {
        File file = new File(getArtifactsFolder() + File.separator + name);
        if (file.exists()) {
            return file;
        }

        String path = file.getAbsolutePath();
        LOGGER.debug("artifact file to download: " + path);

        String url = getUrl(driver, name);
        String username = getField(url, 1);
        String password = getField(url, 2);

        if (!username.isEmpty() && !password.isEmpty()) {
            Authenticator.setDefault(new ReportContext.CustomAuthenticator(username, password));
        }

        if (checkArtifactUsingHttp(url, username, password)) {
            try {
                FileUtils.copyURLToFile(new URL(url), file);
                LOGGER.debug("Successfully downloaded artifact: {}", name);
            } catch (IOException e) {
                LOGGER.error("Artifact: " + url + " wasn't downloaded to " + path, e);
            }
        } else {
            Assert.fail("Unable to find artifact: " + name);
        }

        // publish as test artifact to Zebrunner Reporting
        Artifact.attachToTest(name, file);

        return file;
    }

    /**
     * check if artifact exists using http
     *
     * @param url String
     * @param username String
     * @param password String
     * @return boolean
     */
    private static boolean checkArtifactUsingHttp(String url, String username, String password) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            // note : you may also need
            // HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("HEAD");

            if (!username.isEmpty() && !password.isEmpty()) {
                String usernameColonPassword = username + ":" + password;
                String basicAuthPayload = "Basic " + Base64.getEncoder().encodeToString(usernameColonPassword.getBytes());
                con.addRequestProperty("Authorization", basicAuthPayload);
            }

            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            LOGGER.debug("Artifact doesn't exist: " + url, e);
            return false;
        }
    }

    /**
     * get username or password from url
     *
     * @param url String
     * @param position int
     * @return String
     */
    private static String getField(String url, int position) {
        Pattern pattern = Pattern.compile(".*:\\/\\/(.*):(.*)@");
        Matcher matcher = pattern.matcher(url);

        return matcher.find() ? matcher.group(position) : "";

    }

    /**
     * Generate file in artifacts location and register in Zebrunner Reporting
     *
     * @param name String
     * @param source InputStream
     */
    public static void saveArtifact(String name, InputStream source) throws IOException {
        File artifact = new File(String.format("%s/%s", getArtifactsFolder(), name));
        artifact.createNewFile();
        FileUtils.writeByteArrayToFile(artifact, IOUtils.toByteArray(source));

        Artifact.attachToTest(name, IOUtils.toByteArray(source));
    }

    /**
     * Copy file into artifacts location and register in Zebrunner Reporting
     *
     * @param source File
     */

    public static void saveArtifact(File source) throws IOException {
        File artifact = new File(String.format("%s/%s", getArtifactsFolder(), source.getName()));
        artifact.createNewFile();
        FileUtils.copyFile(source, artifact);

        Artifact.attachToTest(source.getName(), artifact);
    }

    /**
     * generate url for artifact by name
     *
     * @param driver WebDriver
     * @param name String
     * @return String
     */
    private static String getUrl(WebDriver driver, String name) {
        String seleniumHost = Configuration.getSeleniumUrl().replace("wd/hub", "download/");
        RemoteWebDriver drv = driver instanceof Decorated ? (RemoteWebDriver) (((Decorated<WebDriver>) driver).getOriginal())
                : (RemoteWebDriver) driver;
        String sessionId = drv.getSessionId().toString();
        String url = seleniumHost + sessionId + "/" + name;
        LOGGER.debug("url: " + url);
        return url;
    }

    // Converting InputStream to String
    private static String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            // do noting
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
        return response.toString();
    }

}
