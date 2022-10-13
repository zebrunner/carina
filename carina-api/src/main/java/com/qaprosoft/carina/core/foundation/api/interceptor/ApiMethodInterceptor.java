package com.qaprosoft.carina.core.foundation.api.interceptor;

import com.qaprosoft.carina.core.foundation.api.AbstractApiMethodV2;

public interface ApiMethodInterceptor {

    void onInstantiation(AbstractApiMethodV2 apiMethod);

    void onBeforeCall(AbstractApiMethodV2 apiMethod);

}
