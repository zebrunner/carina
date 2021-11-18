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
package com.qaprosoft.apitools.builder;

import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.crypto.CryptoTool;
import com.qaprosoft.carina.core.foundation.utils.Configuration;

public class CryptoProcessor implements PropertiesProcessor {

    private static CryptoTool cryptoTool = new CryptoTool(Configuration.get(Configuration.Parameter.CRYPTO_KEY_PATH));
    private static Pattern CRYPT_PATTERN = Pattern.compile(SpecialKeywords.CRYPT);

    @Override
    public Properties process(Properties in) {
        Properties out = new Properties();
        for (Entry<Object, Object> entry : in.entrySet()) {
            Matcher cryptoMatcher = CRYPT_PATTERN.matcher(entry.getValue().toString());
            String tmp = entry.getValue().toString();
            boolean crypted = false;

            while (cryptoMatcher.find()) {
                String toReplace = cryptoMatcher.group();
                tmp = tmp.replace(toReplace, cryptoTool.decryptByPattern(toReplace, CRYPT_PATTERN));
                crypted = true;
            }

            if (crypted)
                out.put(entry.getKey(), tmp);
        }
        return out;
    }
}
