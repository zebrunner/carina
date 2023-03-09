package com.zebrunner.carina.api.mock.apimethod;

import com.zebrunner.carina.api.AbstractApiMethodV2;
import com.zebrunner.carina.api.annotation.Endpoint;
import com.zebrunner.carina.api.http.HttpMethodType;

@Endpoint(methodType = HttpMethodType.GET, url = "${config.env.base_url}/mock/part/${config.some_id}")
public class AutoReplaceUrlPartsMethod extends AbstractApiMethodV2 {

}
