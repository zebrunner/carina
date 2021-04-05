package com.qaprosoft.apitools.validation.method;

import com.qaprosoft.carina.core.foundation.api.AbstractApiMethodV2;
import com.qaprosoft.carina.core.foundation.api.annotation.*;
import com.qaprosoft.carina.core.foundation.api.http.HttpResponseStatusType;
import com.qaprosoft.carina.core.foundation.utils.Configuration;

@ContentType(type = "application/xml")
@SuccessfulHttpStatus(status = HttpResponseStatusType.OK_200)
public class GetUsersMethod extends AbstractApiMethodV2 {

    public GetUsersMethod() {
        super(null, "src/test/resources/validation/xml_file/object/expected_res.xml");
        replaceUrlPlaceholder("base_url", Configuration.getEnvArg("api_url"));
    }
}
