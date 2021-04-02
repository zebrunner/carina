package com.qaprosoft.apitools.validation.method;

import com.qaprosoft.carina.core.foundation.api.AbstractApiMethodV2;
import com.qaprosoft.carina.core.foundation.api.annotation.*;
import com.qaprosoft.carina.core.foundation.api.http.HttpMethodType;
import com.qaprosoft.carina.core.foundation.api.http.HttpResponseStatusType;

@ContentType(type = "application/xml")
@Endpoint(methodType = HttpMethodType.GET, url = "http://localhost:8080/users")
@SuccessfulHttpStatus(status = HttpResponseStatusType.OK_200)
public class GetUsersMethod extends AbstractApiMethodV2 {

    public GetUsersMethod() {
        super(null, "src/test/resources/validation/xml_file/object/expected_res.xml");
    }
}
