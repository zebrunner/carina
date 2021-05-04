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

    @Test()
    public void testPutKeyNull() {
        try {
            AzureManager.getInstance().put("", null, null);
            Assert.fail("Key verification doesn't work!");
        } catch (RuntimeException e) {
            // Expected failure
        }
    }

    @Test()
    public void testPutKeyEmpty() {
        try {
            AzureManager.getInstance().put("", "", null);
            Assert.fail("Key verification doesn't work!");
        } catch (RuntimeException e) {
            // Expected failure
        }
    }

    @Test()
    public void testPutFilePathNull() {
        try {
            AzureManager.getInstance().put("", "test", null);
            Assert.fail("FilePath verification doesn't work!");
        } catch (RuntimeException e) {
            // Expected failure
        }
    }

    @Test()
    public void testPutFilePathEmpty() {
        try {
            AzureManager.getInstance().put("", "test", "");
            Assert.fail("FilePath verification doesn't work!");
        } catch (RuntimeException e) {
            // Expected failure
        }
    }

    @Test()
    public void testPutFilePathNotExist() {
        try {
            AzureManager.getInstance().put("", "test", "test");
            Assert.fail("File existence verification doesn't work!");
        } catch (RuntimeException e) {
            // Expected failure
        }
    }

    @Test()
    public void testGetKeyNull() {
        try {
            String localPath = Configuration.get(Configuration.Parameter.AZURE_LOCAL_STORAGE);
            AzureManager.getInstance().download( "resources", "apk-StableDev.apk", new File(localPath + "/apk-StableDev.apk"));
            Assert.fail("Key verification doesn't work!");
        } catch (RuntimeException e) {
            // Expected failure
        }
    }

    @Test()
    public void testDeleteKeyNull() {
        try {
            AzureManager.getInstance().delete("", null);
            Assert.fail("Key verification doesn't work!");
        } catch (RuntimeException e) {
            // Expected failure
        }
    }

    @Test()
    public void testDeleteKeyEmpty() {
        try {
            AzureManager.getInstance().delete("", "");
            Assert.fail("Key verification doesn't work!");
        } catch (RuntimeException e) {
            // Expected failure
        }
    }

    @Test()
    public void testGetPropsNull() {
        try {
            String localPath = Configuration.get(Configuration.Parameter.AZURE_LOCAL_STORAGE);
            BlobProperties value = AzureManager.getInstance().get("resources", "apk-StableDev.apk");

            String remoteFileMD5 = Base64.encodeBase64String(value.getContentMd5());

            File file = new File("./apk-StableDev.apk");
            String localFileMD5 = FileManager.getFileChecksum(FileManager.Checksum.MD5, file);

            System.out.println(remoteFileMD5);
            System.out.println(localFileMD5);
            System.out.println(remoteFileMD5.equals(localFileMD5));

            Assert.fail("Key verification doesn't work!");
        } catch (RuntimeException e) {
            // Expected failure
        } catch (IOException | NoSuchAlgorithmException e) {
            // Expected failure
            // e.printStackTrace();
        }
    }
}
