package com.zebrunner.carina.cloud;

import java.io.FileNotFoundException;
import java.nio.file.Path;

/**
 * Single interface for Amazon S3, Azure, AppCenter<br>
 * Specifies the type of the implementation to call, depending on the link.<br>
 * For AppCenter link should be like: {@code appcenter://appName/platformName/buildType/version}<br>
 * For Amazon S3 and Azure link as is
 */
public interface CloudManager {

    /**
     * Get instance of cloud manager
     * 
     * @return {@link CloudManager}
     */
    public static CloudManager getInstance() {
        return CloudManagerHandler.getProxyHandler();
    }

    /**
     * Download artifact from the cloud
     * 
     * @param from link to artifact in the cloud
     * @param to where to save the file
     * @return true if download was successful, false otherwise
     * @throws UnsupportedOperationException if the method is not supported by the implementation
     */
    boolean download(String from, Path to);

    /**
     * Put artifact into the cloud
     * 
     * @param from path to the file to upload to the cloud
     * @param to where to save the file in the cloud
     * @return true if put was successful, false otherwise
     * @throws UnsupportedOperationException if the method is not supported by the implementation
     */
    boolean put(Path from, String to) throws FileNotFoundException;

    /**
     * Delete artifact in the cloud
     *
     * @param url path in the cloud to the file to be deleted
     * @return true if deleting was successful, false otherwise
     * @throws UnsupportedOperationException if the method is not supported by the implementation
     */
    boolean delete(String url);

    /**
     * Method to update MOBILE_APP path<br>
     * <b>for internal usage only</b><br>
     * Or if you want to get direct download link for AppCenter app
     */
     String updateAppPath(String url);
 }
