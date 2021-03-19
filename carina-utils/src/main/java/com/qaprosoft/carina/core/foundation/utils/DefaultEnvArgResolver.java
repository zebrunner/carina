/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.core.foundation.utils;

import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

/**
 * DefaultEnvArgsResolver
 * 
 * @author Aliaksei_Khursevich
 *         <a href="mailto:hursevich@gmail.com">Aliaksei_Khursevich</a>
 *
 */
public class DefaultEnvArgResolver implements IEnvArgResolver {

    @Override
    public String get(String env, String key) {
        if (Configuration.isNull(Parameter.ENV)) {
            throw new RuntimeException("Configuration parameter 'env' should be set!");
        }
        return R.CONFIG.get(Configuration.get(Parameter.ENV) + "." + key);
    }
}
