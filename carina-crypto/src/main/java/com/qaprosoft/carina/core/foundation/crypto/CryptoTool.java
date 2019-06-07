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
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;

public class CryptoTool {
    private static final Logger LOGGER = Logger.getLogger(CryptoTool.class);

    private String algorithm;
    private Cipher cipher;
    private Key key;

    public CryptoTool(String cryptoAlgorithm, String cryptoKeyType, Key key) {
        this.algorithm = cryptoAlgorithm;

        this.key = key;

        try {
            this.cipher = Cipher.getInstance(algorithm);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public CryptoTool() {
        this(SpecialKeywords.CRYPTO_ALGORITHM, SpecialKeywords.CRYPTO_KEY_TYPE, SpecialKeywords.CRYPTO_KEY_PATH);
    }

    public CryptoTool(String cryptoKeyPath) {
        this(SpecialKeywords.CRYPTO_ALGORITHM, SpecialKeywords.CRYPTO_KEY_TYPE, cryptoKeyPath);
    }

    public CryptoTool(String cryptoAlgorithm, String cryptoKeyType, String cryptoKeyPath) {
        this.algorithm = cryptoAlgorithm;

        try {
            this.key = SecretKeyManager.loadKey(new File(cryptoKeyPath), cryptoKeyType);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        try {
            this.cipher = Cipher.getInstance(algorithm);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    // Encrypt/decrypt
    public String encrypt(String strToEncrypt) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            final String encryptedString = new String(Base64.encodeBase64(cipher.doFinal(strToEncrypt.getBytes())));
            return encryptedString;
        } catch (Exception e) {
            throw new RuntimeException("Error while encrypting, check your crypto key! " + e.getMessage(), e);
        }
    }

    public String decrypt(String strToDecrypt) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            final String decryptedString = new String(cipher.doFinal(Base64.decodeBase64(strToDecrypt.getBytes())));
            return decryptedString;
        } catch (Exception e) {
            throw new RuntimeException("Error while decrypting, check your crypto key! " + e.getMessage(), e);
        }
    }

    public String encryptByPattern(String content, Pattern pattern) {
        String wildcard = pattern.pattern().substring(pattern.pattern().indexOf("{") + 1,
                pattern.pattern().indexOf(":"));
        if (content != null && content.contains(wildcard)) {
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                String group = matcher.group();
                String crypt = StringUtils.removeStart(group, "{" + wildcard + ":").replace("}", "");
                content = StringUtils.replace(content, group, encrypt(crypt));
            }
        }
        return content;
    }

    public String decryptByPattern(String content, Pattern pattern) {
        String wildcard = pattern.pattern().substring(pattern.pattern().indexOf("{") + 1,
                pattern.pattern().indexOf(":"));
        if (content != null && content.contains(wildcard)) {
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                String group = matcher.group();
                String crypt = StringUtils.removeStart(group, "{" + wildcard + ":").replace("}", "");
                content = StringUtils.replace(content, group, decrypt(crypt));
            }
        }
        if (content == null) {
            // fix potential null pointer exception in doType
            content = "";
        }
        return content;
    }

    public String encryptByPatternAndWrap(String content, Pattern pattern, String wrapper) {
        String wildcard = pattern.pattern().substring(pattern.pattern().indexOf("{") + 1,
                pattern.pattern().indexOf(":"));
        if (content != null && content.contains(wildcard)) {
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                String group = matcher.group();
                String crypt = StringUtils.removeStart(group, "{" + wildcard + ":").replace("}", "");
                content = StringUtils.replace(content, group, String.format(wrapper, encrypt(crypt)));
            }
        }
        return content;
    }

    public String decryptByPatternAndWrap(String content, Pattern pattern, String wrapper) {
        String wildcard = pattern.pattern().substring(pattern.pattern().indexOf("{") + 1,
                pattern.pattern().indexOf(":"));
        if (content != null && content.contains(wildcard)) {
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                String group = matcher.group();
                String crypt = StringUtils.removeStart(group, "{" + wildcard + ":").replace("}", "");
                content = StringUtils.replace(content, group, String.format(wrapper, decrypt(crypt)));
            }
        }
        return content;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public Cipher getCipher() {
        return cipher;
    }

    public void setCipher(Cipher cipher) {
        this.cipher = cipher;
    }
}
