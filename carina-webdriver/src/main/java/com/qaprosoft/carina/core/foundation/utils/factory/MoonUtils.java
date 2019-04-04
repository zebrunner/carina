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
package com.qaprosoft.carina.core.foundation.utils.factory;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.zafira.client.ZafiraSingleton;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

public class MoonUtils {

    private static final Logger LOGGER = Logger.getLogger(MoonUtils.class);

    private static final String VIDEO_NAME_PATTERN = "%s.mp4";
    private static final HttpClient HTTP_CLIENT;

    static {
        HTTP_CLIENT = HttpClientBuilder.create().build();
    }

    public static boolean isMoonEnabled() {
        boolean result = false;
        HttpGet request = new HttpGet(Configuration.get(Configuration.Parameter.SELENIUM_HOST));
        try {
            HttpResponse response = HTTP_CLIENT.execute(request);
            if(response.getStatusLine().getStatusCode() == 200) {
                String moonInfo = EntityUtils.toString(response.getEntity());
                result = moonInfo.toLowerCase().contains("moon");
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }

    public static String buildVideoName(String videoName) {
        return String.format(VIDEO_NAME_PATTERN, videoName);
    }

    public static String getVideoLink(String sessionId, String videoName) {
        String link;
        if(MoonUtils.isMoonEnabled()) {
            String s3Key = sessionId + "/" + MoonUtils.buildVideoName(videoName);
            link = ZafiraSingleton.INSTANCE.getClient().getServiceURL() + "/moon/" + s3Key;
        } else {
            link = String.format(R.CONFIG.get("screen_record_host"), videoName);
        }
        return link;
    }

}
