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
package com.qaprosoft.amazon.client;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.zafira.client.ZafiraSingleton;
import com.qaprosoft.zafira.models.dto.aws.FileUploadType.Type;

public class AmazonS3Client {

    private static final Logger LOGGER = Logger.getLogger(AmazonS3Client.class);

    private static final ExecutorService executorService = Executors.newFixedThreadPool(50);
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd-yyyy");

    public static Optional<CompletableFuture<String>> upload(File file) {
        return upload(file, () -> {}, link -> {}, Type.COMMON);
    }
    
    public static Optional<CompletableFuture<String>> upload(File file, Type fileType) {
        return upload(file, () -> {}, link -> {}, fileType);
    }

    public static Optional<CompletableFuture<String>> upload(File file, Consumer<String> callback, Type fileType) {
        return upload(file, () -> {}, callback, fileType);
    }

    /**
     * Uploads file to amazon S3
     * 
     * @param file           - file to upload
     * @param preparedAction - action will be execute in the same thread before uploading starting
     * @param callback       - triggers on operaion finish
     * @return {@code Optional<CompleatableFuture<url>> if success, Optional<CompleatableFuture<null>> } if there are any problems on async uploading stage
     */
    
    public static Optional<CompletableFuture<String>> upload(File file, Runnable preparedAction, Consumer<String> callback) {
        return upload(file, preparedAction, callback, Type.COMMON);
    }
    
    public static Optional<CompletableFuture<String>> upload(File file, Runnable preparedAction, Consumer<String> callback, Type fileType) {
        preparedAction.run();
        return Optional.ofNullable(CompletableFuture.supplyAsync(() -> {
            String url = null;
            try {
                int expiresIn = Configuration.getInt(Configuration.Parameter.ARTIFACTS_EXPIRATION_SECONDS);
                LOGGER.debug("Uploading to AWS: " + file.getName() + ". Expires in " + expiresIn + " seconds.");
                url = ZafiraSingleton.INSTANCE.getClient().uploadFile(file, expiresIn, String.format(fileType.getPath() + "/%s/", DATE_FORMAT.format(new Date())));
                LOGGER.debug("Uploaded to AWS: " + file.getName());
                callback.accept(url);
                LOGGER.debug("Updated AWS metadata: " + file.getName());
            } catch (Exception e) {
                LOGGER.debug("Can't save file to Amazon S3!", e);
            }
            return url;
        }, executorService).exceptionally(e -> {
            LOGGER.debug("Can't save file to Amazon S3!", e);
            return null;
        }));
    }
}
