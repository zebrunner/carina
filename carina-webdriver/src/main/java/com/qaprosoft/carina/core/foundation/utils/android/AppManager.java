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
package com.qaprosoft.carina.core.foundation.utils.android;

import java.util.HashMap;
import java.util.Map;

import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;

public class AppManager {

    private Map<String, String> packagesByName = new HashMap<String, String>();

    private static AppManager instance;

    private AppManager() {
    }

    public static AppManager getInstance() {
        if (instance == null) {
            synchronized (AppManager.class) {
                if (instance == null) {
                    instance = new AppManager();
                }
            }
        }
        return instance;
    }

    public String getFullPackageByName(final String name) {
        if (!packagesByName.containsKey(name)) {
            String resultPackage = IDriverPool.getDefaultDevice().getFullPackageByName(name);
            packagesByName.put(name, resultPackage.replace("package:", ""));
        }
        return packagesByName.get(name);
    }

}
