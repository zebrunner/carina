package com.qaprosoft.apitools.validation.method;

import com.qaprosoft.carina.core.foundation.api.AbstractApiMethodV2;
import com.qaprosoft.carina.core.foundation.api.annotation.*;
import com.qaprosoft.carina.core.foundation.api.http.HttpMethodType;
import com.qaprosoft.carina.core.foundation.api.http.HttpResponseStatusType;

@Endpoint(methodType = HttpMethodType.GET, url = "http://localhost:8080/docs")
@SuccessfulHttpStatus(status = HttpResponseStatusType.OK_200)
@ResponseTemplatePath(path = "validation/array/duplicate/array_act.json")
public class GetDocMethod extends AbstractApiMethodV2 {
}
