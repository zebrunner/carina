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
package com.zebrunner.carina.api.apitools.message;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.zebrunner.carina.api.apitools.builder.PropertiesProcessor;
import com.zebrunner.carina.api.apitools.builder.MessageBuilder;
import com.zebrunner.carina.api.apitools.util.PropertiesUtil;
import org.apache.commons.configuration2.CompositeConfiguration;

import com.zebrunner.carina.api.apitools.builder.PropertiesProcessorMain;

public class TemplateMessage extends Message {

    private String templatePath;

    private CompositeConfiguration compositeConfiguration;

    private Properties[] propertiesArr;

    private Properties propertiesStorage;

    private List<Class<? extends PropertiesProcessor>> ignoredPropertiesProcessorClasses;

    private String propertiesPath;

    public TemplateMessage() {
        propertiesStorage = new Properties();
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    public Properties[] getPropertiesArr() {
        return propertiesArr;
    }

    public void setPropertiesArr(Properties... propertiesArr) {
        this.propertiesArr = propertiesArr;
        for (Properties properties : propertiesArr) {
            propertiesStorage.putAll(properties);
        }
    }

    public CompositeConfiguration getCompositeConfiguration() {
        return compositeConfiguration;
    }

    public void setEnvironmentConfiguration(CompositeConfiguration compositeConfiguration) {
        this.compositeConfiguration = compositeConfiguration;
        Iterator<?> keys = compositeConfiguration.getKeys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            propertiesStorage.put(key, compositeConfiguration.getProperty(key));
        }
    }

    public String getPropertiesPath() {
        return propertiesPath;
    }

    public void setPropertiesPath(String propertiesPath) {
        this.propertiesPath = propertiesPath;
        propertiesStorage.putAll(PropertiesUtil.readProperties(propertiesPath));
    }

    public void setPropertiesStorage(Properties propertiesStorage) {
        this.propertiesStorage = propertiesStorage;
    }

    public Properties getPropertiesStorage() {
        return propertiesStorage;
    }

    public void putItemToPropertiesStorage(String key, Object value) {
        propertiesStorage.put(key, value);
    }

    public void removeItemFromPropertiesStorage(String key) {
        propertiesStorage.remove(key);
    }

    public void setIgnoredPropertiesProcessorClasses(List<Class<? extends PropertiesProcessor>> ignoredPropertiesProcessorClasses) {
        this.ignoredPropertiesProcessorClasses = ignoredPropertiesProcessorClasses;
    }

    @Override
    public String getMessageText() {
        propertiesStorage = PropertiesProcessorMain.processProperties(propertiesStorage, ignoredPropertiesProcessorClasses);
        return MessageBuilder.buildStringMessage(templatePath, propertiesStorage);
    }
}
