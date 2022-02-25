package com.qaprosoft.mock.apimethod;

import com.qaprosoft.carina.core.foundation.api.AbstractApiMethodV2;
import com.qaprosoft.carina.core.foundation.api.annotation.Endpoint;
import com.qaprosoft.carina.core.foundation.api.http.HttpMethodType;

@Endpoint(methodType = HttpMethodType.GET, url = "${config.env.base_url}/mock/part/${config.some_id}")
public class AutoReplaceUrlPartsMethod extends AbstractApiMethodV2 {

}
