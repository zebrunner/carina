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
package com.qaprosoft.carina.core.foundation.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class FileManager {

    private static final Logger LOGGER = Logger.getLogger(FileManager.class);

    public static void removeDirRecurs(String directory) {
        File dir = new File(directory);
        if (dir.exists() && dir.isDirectory()) {
            try {
                FileUtils.deleteDirectory(dir);
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    public synchronized static List<File> getFilesInDir(File directory) {
        List<File> files = new ArrayList<File>();
        try {
            File[] fileArray = directory.listFiles();

            for (int i = 0; i < fileArray.length; i++) {
                files.add(fileArray[i]);
            }
        } catch (Exception e) {
            LOGGER.error("Unable to get files in dir!", e);
        }
        return files;
    }

    public static void createFileWithContent(String filePath, String content) {
        File file = new File(filePath);

        try {
            file.createNewFile();
            FileWriter fw = new FileWriter(file);
            try {
                fw.write(content);
            } catch (Exception e) {
                LOGGER.debug("Error during FileWriter append. " + e.getMessage(), e.getCause());
            } finally {
                try {
                    fw.close();
                } catch (Exception e) {
                    LOGGER.debug("Error during FileWriter close. " + e.getMessage(), e.getCause());
                }
            }

        } catch (IOException e) {
            LOGGER.debug(e.getMessage(), e.getCause());
        }
    }

}
