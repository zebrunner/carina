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
package com.qaprosoft.carina.core.foundation.utils.tag;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.zebrunner.agent.core.registrar.domain.LabelDTO;
import com.zebrunner.agent.core.registrar.label.LabelResolver;

public class PriorityManager implements LabelResolver {

    @Override
    public List<LabelDTO> resolve(Class<?> clazz, Method method) {
        TestPriority priority = method.getAnnotation(TestPriority.class);
        if (priority == null) {
            priority = clazz.getAnnotation(TestPriority.class);
        }
        return priority != null
                ? Collections.singletonList(new LabelDTO(SpecialKeywords.TEST_PRIORITY_TAG, priority.value().name()))
                : Collections.emptyList();
    }

}