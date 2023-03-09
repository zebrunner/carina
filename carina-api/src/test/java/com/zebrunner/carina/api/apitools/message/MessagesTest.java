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

import com.zebrunner.carina.api.apitools.message.Message;
import com.zebrunner.carina.api.apitools.message.TemplateMessage;
import com.zebrunner.carina.api.apitools.message.TextMessage;
import com.zebrunner.carina.utils.R;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Properties;

public class MessagesTest {
    
    private static final String MESSAGE_TEXT = "Test message";
    private static final String PROPERTIES_PATH = "testdata.properties";

    @Test
    public void testTextMessage() {
        Message message = new TextMessage(MESSAGE_TEXT);

        Assert.assertEquals(message.getMessageText(), MESSAGE_TEXT, message.getMessageText() + " doesn't equal to " + MESSAGE_TEXT);
    }

    @Test
    public void testSetTextMessage() {
        Message message = new TextMessage(MESSAGE_TEXT);
        Assert.assertEquals(message.getMessageText(), MESSAGE_TEXT, message.getMessageText() + " doesn't equal to " + MESSAGE_TEXT);

        message.setMessageText("Updated test message");
        Assert.assertEquals(message.getMessageText(), "Updated test message", message.getMessageText() + " wasn't set to Updated test message");
    }

    @Test
    public void testTemplatePath() {
        TemplateMessage message = new TemplateMessage();
        message.setTemplatePath(PROPERTIES_PATH);

        Assert.assertEquals(message.getTemplatePath(), PROPERTIES_PATH, message.getTemplatePath() + "doesn't equal to " + PROPERTIES_PATH);
    }

    @Test
    public void testPropertiesArr() {
        TemplateMessage message = new TemplateMessage();
        message.setPropertiesArr(R.TESTDATA.getProperties());

        Assert.assertEquals(message.getPropertiesArr()[0], R.TESTDATA.getProperties(), "testdata.properties wasn't set to propertiesArr");
    }

    @Test
    public void testSetPropertiesPath() {
        TemplateMessage message = new TemplateMessage();

        message.setPropertiesPath(PROPERTIES_PATH);

        Assert.assertEquals(message.getPropertiesPath(), PROPERTIES_PATH, message.getTemplatePath() + "doesn't equal to " + PROPERTIES_PATH);
        Assert.assertEquals(message.getPropertiesStorage(), R.TESTDATA.getProperties(), "testdata.properties wasn't set to propertiesStorage");
    }

    @Test
    public void testSetPropertiesStorage() {
        TemplateMessage message = new TemplateMessage();

        message.setPropertiesStorage(R.TESTDATA.getProperties());

        Assert.assertEquals(message.getPropertiesStorage(), R.TESTDATA.getProperties(), "testdata.properties wasn't set to propertiesStorage");
    }

    @Test
    public void testItemToPropertiesStorage() {
        TemplateMessage message = new TemplateMessage();
        message.setPropertiesPath(PROPERTIES_PATH);

        message.putItemToPropertiesStorage("someKey", "someValue");
        Assert.assertEquals(message.getPropertiesStorage().get("someKey"), "someValue", "someKey=someValue wasn't set to PropertiesStorage");

        message.removeItemFromPropertiesStorage("someKey");
        Assert.assertNull(message.getPropertiesStorage().get("someKey"), "someKey wasn't removed from PropertiesStorage");
    }

    @Test
    public void testGetTemplateMessageText() {
        TemplateMessage message = new TemplateMessage();
        message.setPropertiesPath(PROPERTIES_PATH);
        message.setTemplatePath(PROPERTIES_PATH);

        String expectedStringMessage = getStringProperties(R.TESTDATA.getProperties());
        String actualStringMessage = message.getMessageText();

        Assert.assertEquals(actualStringMessage, expectedStringMessage, "StringMessage wasn't generated properly");
    }

    private String getStringProperties(Properties properties) {
        StringBuilder sb = new StringBuilder();

        properties.forEach((key, value) -> sb.append(key).append("=").append(value).append(System.lineSeparator()));

        return sb.toString();
    }
}
