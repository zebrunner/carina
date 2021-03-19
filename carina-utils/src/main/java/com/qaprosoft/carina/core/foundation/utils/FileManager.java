/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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

            if (fileArray == null) {
                return files;
            }
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
    
    /**
     * Archive list of files into the single zip archive.
     *
     * @param output
     *          String zip file path.
     * @param files 
     *          List of files to archive
     */
    public static void zipFiles(String output, File... files) {
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(output))) {
            for (File fileToZip : files) {
                zipOut.putNextEntry(new ZipEntry(fileToZip.getName()));
                Files.copy(fileToZip.toPath(), zipOut);
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Unable to find file for archive operation!", e);
        } catch (IOException e) {
            LOGGER.error("IO exception for archive operation!", e);
        }
    }

}
