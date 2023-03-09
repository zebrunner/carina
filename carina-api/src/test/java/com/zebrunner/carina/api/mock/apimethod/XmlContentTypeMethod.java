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
package com.zebrunner.carina.api.mock.apimethod;

import com.zebrunner.carina.api.AbstractApiMethodV2;
import com.zebrunner.carina.api.annotation.ContentType;
import com.zebrunner.carina.api.annotation.SuccessfulHttpStatus;
import com.zebrunner.carina.api.http.HttpResponseStatusType;
import com.zebrunner.carina.utils.Configuration;

@ContentType(type = "application/xml")
@SuccessfulHttpStatus(status = HttpResponseStatusType.OK_200)
public class XmlContentTypeMethod extends AbstractApiMethodV2 {

    public XmlContentTypeMethod() {
        super(null, "src/test/resources/validation/xml_file/object/expected_res.xml");
        replaceUrlPlaceholder("base_url", Configuration.getEnvArg("api_url"));
    }
}
