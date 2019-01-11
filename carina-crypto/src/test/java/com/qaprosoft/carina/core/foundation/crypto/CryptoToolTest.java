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
package com.qaprosoft.carina.core.foundation.crypto;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import javax.crypto.SecretKey;

import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;

/**
 * Tests for {@link CryptoTool}
 */
public class CryptoToolTest {
    private static Pattern CRYPTO_PATTERN = Pattern.compile(SpecialKeywords.CRYPT);
    private SecretKey key;
    private CryptoTool cryptoTool;
    private final static String cryptoFileName = "crypto.key";

    @Test(priority = 1)
    public void testGenerateKey() throws NoSuchAlgorithmException {
        key = SecretKeyManager.generateKey(SpecialKeywords.CRYPTO_KEY_TYPE, SpecialKeywords.CRYPTO_KEY_SIZE);
        Assert.assertEquals(SpecialKeywords.CRYPTO_KEY_TYPE, key.getAlgorithm());
    }

    @Test(priority = 2)
    public void testSaveKey() throws NoSuchAlgorithmException, IOException {
        File keyFile = new File(cryptoFileName);
        SecretKeyManager.saveKey(key, keyFile);

        Assert.assertTrue(keyFile.exists());
    }

    @Test(priority = 3)
    public void testLoadKey() throws NoSuchAlgorithmException, IOException {
        File keyFile = new File(cryptoFileName);
        key = SecretKeyManager.loadKey(keyFile, SpecialKeywords.CRYPTO_KEY_TYPE);
        Assert.assertEquals(SpecialKeywords.CRYPTO_KEY_TYPE, key.getAlgorithm());
    }

    @Test(priority = 4)
    public void testInitializationFromKeyFile() {
        cryptoTool = new CryptoTool(cryptoFileName);
        Assert.assertNotNull(cryptoTool.getAlgorithm());
        Assert.assertNotNull(cryptoTool.getCipher());
        Assert.assertEquals(SpecialKeywords.CRYPTO_ALGORITHM, cryptoTool.getAlgorithm());
    }

    @Test(priority = 5)
    public void testEncrypt() {
        String input = "EncryptMe";
        String encrypted = cryptoTool.encrypt(input);
        Assert.assertNotNull(encrypted);
        Assert.assertFalse(encrypted.equals(input));
    }

    @Test(priority = 6)
    public void testDecrypt() {
        String input = "EncryptMe";
        String encrypted = cryptoTool.encrypt(input);
        String decrypted = cryptoTool.decrypt(encrypted);
        Assert.assertNotNull(decrypted);
        Assert.assertEquals(input, decrypted);
    }

    @Test(priority = 7)
    public void testInitializationFromKey() {
        cryptoTool = new CryptoTool(SpecialKeywords.CRYPTO_ALGORITHM, SpecialKeywords.CRYPTO_KEY_TYPE, key);
        Assert.assertNotNull(cryptoTool.getAlgorithm());
        Assert.assertNotNull(cryptoTool.getCipher());
        Assert.assertEquals(SpecialKeywords.CRYPTO_ALGORITHM, cryptoTool.getAlgorithm());
    }

    @Test(priority = 8)
    public void testEncrypt2() {
        String input = "EncryptMe";
        String encrypted = cryptoTool.encrypt(input);
        Assert.assertNotNull(encrypted);
        Assert.assertFalse(encrypted.equals(input));
    }

    @Test(priority = 8)
    public void testDecrypt2() {
        String input = "EncryptMe";
        String encrypted = cryptoTool.encrypt(input);
        String decrypted = cryptoTool.decrypt(encrypted);
        Assert.assertNotNull(decrypted);
        Assert.assertEquals(input, decrypted);
    }

    @Test(priority = 8)
    public void testEncryptByPattern() {
        String input = "{crypt: EncryptMe}";
        String encrypted = cryptoTool.encryptByPattern(input, CRYPTO_PATTERN);
        Assert.assertNotNull(encrypted);
        Assert.assertFalse(encrypted.equals(input));
    }

    @Test(priority = 8)
    public void testDecryptByPattern() {
        String input = "{crypt:EncryptMe}";
        String resultInput = "EncryptMe";
        String encrypted = cryptoTool.encryptByPattern(input, CRYPTO_PATTERN);
        String encryptedWithPattern = String.format("{crypt:%s}", encrypted);

        String decrypted = cryptoTool.decryptByPattern(encryptedWithPattern, CRYPTO_PATTERN);
        Assert.assertNotNull(decrypted);
        Assert.assertEquals(resultInput, decrypted);
    }

    @Test(priority = 8)
    public void testEncryptByPatternAndWrap() {
        String input = "{custom_crypt: EncryptMe}";
        String customWrapper = "{custom_crypt:%s}";
        Pattern customPattern = Pattern.compile("\\{custom_crypt:[^\\{\\}]*\\}");

        String encrypted = cryptoTool.encryptByPatternAndWrap(input, customPattern, customWrapper);
        Assert.assertNotNull(encrypted);
        Assert.assertFalse(encrypted.equals(input));
    }

    @Test(priority = 8)
    public void testDecryptByPatternAndWrap() {
        String input = "{custom_crypt: EncryptMe}";
        String customWrapper = "{custom_crypt:%s}";
        Pattern customPattern = Pattern.compile("\\{custom_crypt:[^\\{\\}]*\\}");

        String encrypted = cryptoTool.encryptByPatternAndWrap(input, customPattern, customWrapper);
        String decrypted = cryptoTool.decryptByPatternAndWrap(encrypted, customPattern, customWrapper);
        Assert.assertNotNull(decrypted);
        Assert.assertEquals(input, decrypted);
    }

    @AfterSuite
    public void cleanup() {
        File keyFile = new File(cryptoFileName);
        Assert.assertTrue(keyFile.exists());
        keyFile.delete();
    }
}
