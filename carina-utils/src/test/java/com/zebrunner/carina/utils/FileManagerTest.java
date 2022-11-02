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
package com.zebrunner.carina.utils;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.zebrunner.carina.utils.FileManager;
import com.zebrunner.carina.utils.ZipManager;

public class FileManagerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String ZIP_FOLDER_PATH = "src/test/resources/zip";
    private static final String ZIP_FILE_PATH = ZIP_FOLDER_PATH + "/zipFile.zip";
    private static final String TEXT_FILE_PATH = ZIP_FOLDER_PATH + "/textFile.txt";

    private static final String CONTENT = "File with content\n" + "Second line\n" + "Third line";

    @Test
    public void testZipFile() {
        FileManager.zipFiles(ZIP_FILE_PATH, new File(TEXT_FILE_PATH));

        Assert.assertTrue(isFileExist(ZIP_FILE_PATH), "Zip file doesn't exist by the path: src/test/resources/zip/zipFile.zip");
    }

    @Test
    public void testUnzipFile() {
        ZipManager.unzip(ZIP_FILE_PATH, ZIP_FOLDER_PATH);

        Assert.assertTrue(isFileExist(TEXT_FILE_PATH), "File doesn't exist in the folder: src/test/resources/zip");
    }

    @Test
    public void testCreateFileWithContent() {
        FileManager.createFileWithContent(TEXT_FILE_PATH, CONTENT);

        String readContent = readFile(TEXT_FILE_PATH);

        Assert.assertEquals(readContent, CONTENT, "File wasn't created with content: " + CONTENT);
    }

    @Test
    public void testRemoveDirRecurs() {
        String dirPath = ZIP_FOLDER_PATH + "/dirToRemove";

        new File(dirPath).mkdir();

        FileManager.removeDirRecurs(dirPath);

        Assert.assertFalse(isDirectoryExist(dirPath), "Directory wasn't removed");
    }

    private String readFile(String path) {
        Path filePath = Paths.get(path);
        String content = "";
        try {
            List<String> lines = Files.readAllLines(filePath);
            content = String.join("\n", lines);
        } catch (IOException e) {
            LOGGER.error("Error while reading from file!", e);
        }
        return content;
    }

    private boolean isFileExist(String path) {
        File f = new File(path);
        return f.exists() && !f.isDirectory();
    }

    private boolean isDirectoryExist(String path) {
        File f = new File(path);
        return f.exists() && f.isDirectory();
    }
}
