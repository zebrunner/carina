package com.qaprosoft.carina.core.foundation.api.annotation;

import com.qaprosoft.carina.core.foundation.api.AbstractApiMethodV2;
import com.qaprosoft.carina.core.foundation.api.http.HttpMethodType;
import com.qaprosoft.carina.core.foundation.api.http.HttpResponseStatusType;

@ContentType(type = "application/xml")
@Endpoint(methodType = HttpMethodType.POST, url = "http://test.api.com")
@RequestTemplatePath(path = "/testdata/api/rq.json")
@ResponseTemplatePath(path = "/testdata/api/rs.json")
@SuccessfulHttpStatus(status = HttpResponseStatusType.OK_200)
public class ApiMethodWAnnotation extends AbstractApiMethodV2 {

}