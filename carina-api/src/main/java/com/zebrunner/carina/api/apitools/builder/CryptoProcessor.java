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
package com.zebrunner.carina.api.apitools.builder;

import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.SkipException;

import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.crypto.Algorithm;
import com.zebrunner.carina.crypto.CryptoTool;
import com.zebrunner.carina.crypto.CryptoToolBuilder;

public class CryptoProcessor implements PropertiesProcessor {

    private CryptoTool cryptoTool = null;
    private final String cryptoPatternAsText = Configuration.get(Configuration.Parameter.CRYPTO_PATTERN);
    private final Pattern cryptoPattern = Pattern.compile(cryptoPatternAsText);

    @Override
    public Properties process(Properties in) {
        Properties out = new Properties();
        for (Entry<Object, Object> entry : in.entrySet()) {
            Matcher cryptoMatcher = cryptoPattern.matcher(entry.getValue().toString());
            String tmp = entry.getValue().toString();
            boolean crypted = false;

            while (cryptoMatcher.find()) {
                initCryptoTool();
                String toReplace = cryptoMatcher.group();
                tmp = tmp.replace(toReplace, cryptoTool.decrypt(toReplace, cryptoPatternAsText));
                crypted = true;
            }

            if (crypted)
                out.put(entry.getKey(), tmp);
        }
        return out;
    }

    private void initCryptoTool() {
        if (this.cryptoTool == null) {
            String cryptoKey = Configuration.get(Configuration.Parameter.CRYPTO_KEY_VALUE);
            if (cryptoKey.isEmpty()) {
                throw new SkipException("Encrypted data detected, but the crypto key is not found!");
            }
            this.cryptoTool = CryptoToolBuilder.builder()
                    .chooseAlgorithm(Algorithm.find(Configuration.get(Configuration.Parameter.CRYPTO_ALGORITHM)))
                    .setKey(cryptoKey)
                    .build();
        }
    }
}
