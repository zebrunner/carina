/*
 * Copyright 2013-2019 QAPROSOFT (http://qaprosoft.com/).
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
 */
package com.qaprosoft.carina.core.foundation.jenkins;

import org.testng.Assert;
import org.testng.annotations.Test;

public class JenkinsClientTest {

    @Test
    public void testJenkinsClientUrl() {
        String jenkinsUrl = "http://localhost:8080/jenkins";
        JenkinsClient client = new JenkinsClient(jenkinsUrl);
        Assert.assertEquals(client.getJenkinsURL(), jenkinsUrl);
    }

    @Test
    public void testSetJenkinsUrl() {
        String jenkinsUrl = "http://localhost:8080/jenkins/";
        JenkinsClient client = new JenkinsClient(jenkinsUrl);
        Assert.assertEquals(client.getJenkinsURL(), "http://localhost:8080/jenkins");
    }

}
