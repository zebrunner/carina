/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
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
package com.qaprosoft.azure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zebrunner.carina.utils.FileManager;
import com.zebrunner.carina.utils.R;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.zebrunner.carina.utils.Configuration;

public class AzureManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Pattern AZURE_CONTAINER_PATTERN = Pattern.compile(
            "\\/\\/(?<accountName>[a-z0-9]{3,24})\\.blob.core.windows.net\\/(?:(?<containerName>\\$root|(?:[a-z0-9](?!.*--)[a-z0-9-]{1,61}[a-z0-9]))\\/)?(?<remoteFilePath>.{1,1024})");
    private static volatile AzureManager instance = null;
    private BlobServiceClient blobServiceClient = null;

    private AzureManager() {}

    public static synchronized AzureManager getInstance() {
        if (instance == null) {
            AzureManager azureManager = new AzureManager();
            String accountName = Configuration.getDecrypted(Configuration.Parameter.AZURE_ACCOUNT_NAME);
            String endpoint = Configuration.getDecrypted(Configuration.Parameter.AZURE_BLOB_URL);
            BlobServiceClientBuilder blobServiceClientBuilder = new BlobServiceClientBuilder()
                    .endpoint(endpoint);

            String secretKey = Configuration.getDecrypted(Configuration.Parameter.AZURE_ACCESS_KEY_TOKEN);
            if (!secretKey.isEmpty()) {
                StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, secretKey);
                blobServiceClientBuilder.credential(credential);
            }

            azureManager.blobServiceClient = blobServiceClientBuilder.buildClient();
            instance = azureManager;
        }

        return instance;
    }

    /**
     * see CloudManager interface
     */
    public boolean put(Path from, String to) throws FileNotFoundException {
        if (!ObjectUtils.allNotNull(from, to) || to.isEmpty()) {
            throw new IllegalArgumentException("Arguments cannot be null or empty.");
        }

        if (!Files.exists(from)) {
            throw new FileNotFoundException(String.format("File '%s' does not exist!", from));
        }
        boolean isSuccessful = false;
        BlobUrlParts blobUrlParts = BlobUrlParts.parse(to);
        LOGGER.debug("Uploading a new object to Azure from a file: {}.", from);
        try {
            this.blobServiceClient.getBlobContainerClient(blobUrlParts.getBlobContainerName())
                    .getBlobClient(blobUrlParts.getBlobName())
                    .uploadFromFile(from.toAbsolutePath().toString());
            LOGGER.debug("Uploaded to Azure: '{}' with remotePath '{}'.", from, to);
            isSuccessful = true;
        } catch (BlobStorageException bse) {
            LOGGER.error(
                    "Caught an BlobStorageException, which "
                            + "means your request made it "
                            + "to Azure, but was rejected with an error response for some reason.\n"
                            + "Error Message:    " + bse.getMessage() + "\n"
                            + "HTTP Status Code: " + bse.getStatusCode() + "\n"
                            + "Azure Error Code: " + bse.getErrorCode() + "\n"
                            + "Service Message:  " + bse.getServiceMessage());
        } catch (Exception e) {
            LOGGER.error("Something went wrong when try to put artifact to the Azure.", e);
        }
        return isSuccessful;
    }

    /**
     * see CloudManager interface
     */
    public boolean download(String from, Path to) {
        if (!ObjectUtils.allNotNull(from, to) || from.isEmpty()) {
            throw new IllegalArgumentException("Arguments cannot be null or empty");
        }
        boolean isSuccessful = false;
        BlobUrlParts blobUrlParts = null;
        try {
            blobUrlParts = BlobUrlParts.parse(from);
        } catch (Exception e) {
            throw new IllegalArgumentException("Incorrect format of link.", e);
        }
        try {
            BlobContainerClient blobContainerClient = this.blobServiceClient.getBlobContainerClient(blobUrlParts.getBlobContainerName());
            BlobClient blobClient = blobContainerClient.getBlobClient(blobUrlParts.getBlobName());
            LOGGER.info("Downloading: {} To: {}", blobUrlParts.getBlobName(), to);
            blobClient.downloadToFile(to.toFile().getAbsolutePath());
            LOGGER.info("Download completed");
            isSuccessful = true;
        } catch (BlobStorageException bse) {
            LOGGER.error(
                    "Caught an BlobStorageException, which "
                            + "means your request made it "
                            + "to Azure, but was rejected with an error response for some reason.\n"
                            + "Error Message:    " + bse.getMessage() + "\n"
                            + "HTTP Status Code: " + bse.getStatusCode() + "\n"
                            + "Azure Error Code: " + bse.getErrorCode() + "\n"
                            + "Service Message:  " + bse.getServiceMessage());
        } catch (Exception e) {
            LOGGER.error("Something went wrong when try to download artifact from Azure.", e);
        }
        return isSuccessful;
    }

    /**
     * see CloudManager interface
     */
    public boolean delete(String url) {
        if (Objects.isNull(url) || url.isEmpty()) {
            throw new IllegalArgumentException("Argument cannot be null or empty");
        }
        boolean isSuccessful = false;
        BlobUrlParts blobUrlParts = BlobUrlParts.parse(url);
        try {
            LOGGER.info("Finding an File..");
            BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(blobUrlParts.getBlobContainerName());
            BlobClient blobClient = blobContainerClient.getBlobClient(blobUrlParts.getBlobName());
            blobClient.delete();
            isSuccessful = true;
        } catch (BlobStorageException bse) {
            LOGGER.error("Caught an BlobStorageException, which "
                    + "means your request made it "
                    + "to Azure, but was rejected with an error response for some reason.\n"
                    + "Error Message:    " + bse.getMessage() + "\n"
                    + "HTTP Status Code: " + bse.getStatusCode() + "\n"
                    + "Azure Error Code: " + bse.getErrorCode() + "\n"
                    + "Service Message:  " + bse.getServiceMessage());
        } catch (Exception e) {
            LOGGER.error("Something went wrong when try to delete artifact from the Azure.", e);
        }
        return isSuccessful;
    }

    /**
     * see CloudManager interface
     */
    public String updateAppPath(String url) {
        if (Objects.isNull(url) || url.isEmpty()) {
            throw new IllegalArgumentException("Argument cannot be null");
        }
        Matcher matcher = AZURE_CONTAINER_PATTERN.matcher(url);
        if (matcher.find()) {
            String accountName = matcher.group("accountName");
            String containerName = matcher.group("containerName") == null ? "$root" : matcher.group("containerName");
            String remoteFilePath = matcher.group("remoteFilePath");

            LOGGER.info("Account: {}\nContainer: {}\nRemotePath: {}", accountName, containerName, remoteFilePath);
            R.CONFIG.put(Configuration.Parameter.AZURE_ACCOUNT_NAME.getKey(), accountName);

            BlobProperties blobProperties = get(containerName, remoteFilePath);
            String azureLocalStorage = Configuration.get(Configuration.Parameter.AZURE_LOCAL_STORAGE);
            String localFilePath = azureLocalStorage + File.separator + StringUtils.substringAfterLast(remoteFilePath, "/");

            File file = new File(localFilePath);

            try {
                // verify requested artifact by checking the checksum
                if (file.exists() && FileManager.getFileChecksum(FileManager.Checksum.MD5, file)
                        .equals(Base64.encodeBase64String(blobProperties.getContentMd5()))) {
                    LOGGER.info("build artifact with the same checksum already downloaded: {}", file.getAbsolutePath());
                } else {
                    LOGGER.info("Following data was extracted: container: {}, remotePath: {}, local file: {}",
                                    containerName, remoteFilePath, file.getAbsolutePath());
                    download(containerName, remoteFilePath, file);
                }

            } catch (Exception exception) {
                LOGGER.error("Azure app path update exception detected!", exception);
            }

            // try to redefine app_version if it's value is latest or empty
            String appVersion = Configuration.get(Configuration.Parameter.APP_VERSION);
            if (appVersion.equals("latest") || appVersion.isEmpty()) {
                Configuration.setBuild(file.getName());
            }
            return file.getAbsolutePath();

        } else {
            throw new RuntimeException(String.format("Unable to parse '%s' path using Azure pattern", url));
        }
    }

    public BlobServiceClient getClient() {
        return blobServiceClient;
    }

    /**
     * Get any file from Azure storage as BlobProperties.
     *
     * @param container
     *            - Azure Storage name.
     * @param remotePath
     *            - Azure Storage path. Example:
     *            DEMO/TestSuiteName/TestMethodName/file.txt
     * @return BlobProperties
     */
    public BlobProperties get(String container, String remotePath) {
        if (container == null) {
            throw new RuntimeException("Container is null!");
        }
        if (container.isEmpty()) {
            throw new RuntimeException("Container is empty!");
        }

        if (remotePath == null) {
            throw new RuntimeException("RemotePath is null!");
        }
        if (remotePath.isEmpty()) {
            throw new RuntimeException("RemotePath is empty!");
        }

        try {
            BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(container);
            BlobClient blobClient = blobContainerClient.getBlobClient(remotePath);

            return blobClient.getProperties();
        } catch (BlobStorageException bse) {
            LOGGER.error("Caught an BlobStorageException, which "
                    + "means your request made it "
                    + "to Azure, but was rejected with an error response for some reason.\n"
                    + "Error Message:    " + bse.getMessage() + "\n"
                    + "HTTP Status Code: " + bse.getStatusCode() + "\n"
                    + "Azure Error Code: " + bse.getErrorCode() + "\n"
                    + "Service Message:  " + bse.getServiceMessage());
        }

        throw new RuntimeException("Unable to download '" + remotePath + "' from Azure Storage '" + container + "'");
    }

    /**
     * Put any file to Azure storage.
     *
     * @param container     - Azure container name
     * @param remotePath    - Azure storage path. Example:
     *                      DEMO/TestSuiteName/TestMethodName/file.txt
     * @param localFilePath - local storage path. Example: C:/Temp/file.txt
     */
    public void put(String container, String remotePath, String localFilePath) {

        if (remotePath == null) {
            throw new RuntimeException("remotePath is null!");
        }
        if (remotePath.isEmpty()) {
            throw new RuntimeException("remotePath is empty!");
        }

        if (localFilePath == null) {
            throw new RuntimeException("FilePath is null!");
        }
        if (localFilePath.isEmpty()) {
            throw new RuntimeException("FilePath is empty!");
        }

        File file = new File(localFilePath);
        if (!file.exists()) {
            throw new RuntimeException("File does not exist! " + localFilePath);
        }

        LOGGER.debug("Uploading a new object to Azure from a file: " + localFilePath);
        try {
            BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(container);
            BlobClient blobClient = blobContainerClient.getBlobClient(remotePath);

            LOGGER.debug("Uploaded to Azure: '" + localFilePath + "' with remotePath '" + remotePath + "'");

            blobClient.uploadFromFile(file.getPath());
        } catch (BlobStorageException bse) {
            LOGGER.error("Caught an BlobStorageException, which "
                    + "means your request made it "
                    + "to Azure, but was rejected with an error response for some reason.\n"
                    + "Error Message:    " + bse.getMessage() + "\n"
                    + "HTTP Status Code: " + bse.getStatusCode() + "\n"
                    + "Azure Error Code: " + bse.getErrorCode() + "\n"
                    + "Service Message:  " + bse.getServiceMessage());
        }
    }

    /**
     * Download file from Azure storage.
     *
     * @param container
     *            - Azure container name.
     * @param remoteFile
     *            - Azure storage path. Example:
     *            DEMO/TestSuiteName/TestMethodName/file.txt
     * @param localFilePath
     *            - local storage path. Example: C:/Temp/file.txt
     *
     */
    public void download(String container, String remoteFile, final File localFilePath) {

        if (container == null) {
            throw new RuntimeException("Container is null!");
        }
        if (container.isEmpty()) {
            throw new RuntimeException("Container is empty!");
        }

        if (remoteFile == null) {
            throw new RuntimeException("File Path is null!");
        }
        if (remoteFile.isEmpty()) {
            throw new RuntimeException("File Path is empty!");
        }

        try {
            BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(container);
            BlobClient blobClient = blobContainerClient.getBlobClient(remoteFile);

            LOGGER.info("Downloading: " + blobContainerClient.getBlobContainerUrl() + " To: " + localFilePath);
            blobClient.downloadToFile(localFilePath.getAbsolutePath());
            LOGGER.info("Download completed");

        } catch (BlobStorageException bse) {
            LOGGER.error("Caught an BlobStorageException, which "
                    + "means your request made it "
                    + "to Azure, but was rejected with an error response for some reason.\n"
                    + "Error Message:    " + bse.getMessage() + "\n"
                    + "HTTP Status Code: " + bse.getStatusCode() + "\n"
                    + "Azure Error Code: " + bse.getErrorCode() + "\n"
                    + "Service Message:  " + bse.getServiceMessage());
        }
    }

    /**
     * Delete file from Azure storage.
     *
     * @param container
     *            - Azure container name.
     * @param file
     *            - Azure storage path. Example:
     *            DEMO/TestSuiteName/TestMethodName/file.txt
     */
    public void delete(String container, String file) {
        if (file == null) {
            throw new RuntimeException("Key is null!");
        }

        if (file.isEmpty()) {
            throw new RuntimeException("Key is empty!");
        }

        try {
            LOGGER.info("Finding an File..");
            BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(container);

            BlobClient blobClient = blobContainerClient.getBlobClient(file);
            blobClient.delete();

        } catch (BlobStorageException bse) {
            LOGGER.error("Caught an BlobStorageException, which "
                    + "means your request made it "
                    + "to Azure, but was rejected with an error response for some reason.\n"
                    + "Error Message:    " + bse.getMessage() + "\n"
                    + "HTTP Status Code: " + bse.getStatusCode() + "\n"
                    + "Azure Error Code: " + bse.getErrorCode() + "\n"
                    + "Service Message:  " + bse.getServiceMessage());
        }
    }
}
