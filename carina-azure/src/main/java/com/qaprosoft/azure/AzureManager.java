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

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.crypto.CryptoTool;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.regex.Pattern;

public class AzureManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static volatile AzureManager instance = null;
    private static BlobServiceClient blobServiceClient = null;

    private AzureManager() {}

    public synchronized static AzureManager getInstance() {
        if (instance == null) {
            instance = new AzureManager();
            CryptoTool cryptoTool = new CryptoTool(Configuration.get(Configuration.Parameter.CRYPTO_KEY_PATH));
            Pattern CRYPTO_PATTERN = Pattern.compile(SpecialKeywords.CRYPT);

            String accountName = Configuration.get(Configuration.Parameter.AZURE_ACCOUNT_NAME);
            String endpoint = cryptoTool.decryptByPattern(Configuration.get(Configuration.Parameter.AZURE_BLOB_URL), CRYPTO_PATTERN);
            String secretKey = cryptoTool.decryptByPattern(Configuration.get(Configuration.Parameter.AZURE_ACCESS_KEY_TOKEN), CRYPTO_PATTERN);

            // Create a SharedKeyCredential
            StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, secretKey);

            // Create a blobServiceClient
            blobServiceClient = new BlobServiceClientBuilder()
                    .endpoint(endpoint)
                    .credential(credential)
                    .buildClient();
        }

        return instance;
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
