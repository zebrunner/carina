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

import com.azure.storage.blob.models.BlobProperties;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.FileManager;
import org.apache.commons.codec.binary.Base64;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class AzureClientTest {

    @Test()
    public void testAzureManagerInit() {
        Assert.assertNotNull(AzureManager.getInstance(), "Singleton for AzureManager is null!");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testPutKeyNull() {
        AzureManager.getInstance().put("", null, null);
        Assert.fail("Key verification doesn't work!");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testPutKeyEmpty() {
        AzureManager.getInstance().put("", "", null);
        Assert.fail("Key verification doesn't work!");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testPutFilePathNull() {
        AzureManager.getInstance().put("", "test", null);
        Assert.fail("FilePath verification doesn't work!");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testPutFilePathEmpty() {
        AzureManager.getInstance().put("", "test", "");
        Assert.fail("FilePath verification doesn't work!");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testPutFilePathNotExist() {
        AzureManager.getInstance().put("", "test", "test");
        Assert.fail("File existence verification doesn't work!");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetKeyNull() {
        String localPath = Configuration.get(Configuration.Parameter.AZURE_LOCAL_STORAGE);
        AzureManager.getInstance().download("resources", "apk-StableDev.apk", new File(localPath + "/apk-StableDev.apk"));
        Assert.fail("Key verification doesn't work!");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testDeleteKeyNull() {
        AzureManager.getInstance().delete("", null);
        Assert.fail("Key verification doesn't work!");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testDeleteKeyEmpty() {
        AzureManager.getInstance().delete("", "");
        Assert.fail("Key verification doesn't work!");
    }

    @Test(expectedExceptions = {RuntimeException.class, IOException.class, NoSuchAlgorithmException.class})
    public void testGetPropsNull() throws IOException, NoSuchAlgorithmException {
        String localPath = Configuration.get(Configuration.Parameter.AZURE_LOCAL_STORAGE);
        BlobProperties value = AzureManager.getInstance().get("resources", "apk-StableDev.apk");

        String remoteFileMD5 = Base64.encodeBase64String(value.getContentMd5());

        File file = new File("./apk-StableDev.apk");
        String localFileMD5 = FileManager.getFileChecksum(FileManager.Checksum.MD5, file);

        System.out.println(remoteFileMD5);
        System.out.println(localFileMD5);
        System.out.println(remoteFileMD5.equals(localFileMD5));

        Assert.fail("Key verification doesn't work!");
    }
}
